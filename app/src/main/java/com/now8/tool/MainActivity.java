package com.now8.tool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.device.yearclass.YearClass;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.tasks.Task;


public class MainActivity extends AbstractLocationPermissionActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_REQUIRED_SETTINGS_FROM_USER = 61124;
    private static final String[] REQUIRED_PERMISSIONS=
            {Manifest.permission.ACCESS_FINE_LOCATION};

    private static final String SYSTEM_SETTINGS_DIALOG_KEY ="SYSTEM_SETTINGS_DIALOG_KEY";
    private boolean systemSettingsDialogInForeground = false;
    private CreateRideFragment generateRideFragment;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SYSTEM_SETTINGS_DIALOG_KEY, systemSettingsDialogInForeground);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logDeviceHighEndYear();
    }

    @Override
    protected String[] getRequiredUserPermissions() {
        return(REQUIRED_PERMISSIONS);
    }

    @Override
    protected void onRequiredPermissionsGranted(Bundle mainActivityState) {

        if (mainActivityState != null) {
            systemSettingsDialogInForeground = mainActivityState.getBoolean(SYSTEM_SETTINGS_DIALOG_KEY);
        }


        if (!systemSettingsDialogInForeground) {
            systemSettingsDialogInForeground = true;
            Toast.makeText(this, R.string.test_required_settings_turned_on, Toast.LENGTH_LONG).show();

            // A LocationSettingsRequest makes a request to Play Services API to find out if the location settings is on
            LocationSettingsRequest getUserLocation=new LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationRequest.create())
                    .build();

            LocationServices.getSettingsClient(this)
                    .checkLocationSettings(getUserLocation)
                    .addOnCompleteListener(this::handleMissingSystemSettingsResponse);
                    /*
                    There is no addOnFailureListener here.
                    Page 4846 for explanation
                     */
        }


        generateRideFragment =
                (CreateRideFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);

        if (generateRideFragment == null) {
            generateRideFragment = new CreateRideFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, generateRideFragment).commitAllowingStateLoss(); // TODO: YOU SHOULD NOT USE commitAllowingStateLoss in production.

        }

    }

    // get the user location or prompt him to enable location on his device settings
    private void handleMissingSystemSettingsResponse(Task<LocationSettingsResponse> task) {
        try {
            LocationSettingsResponse locationSettingsResponseTaskFromGoogleAPI=task.getResult(ApiException.class);
            LocationSettingsStates userSettingsLocationState=locationSettingsResponseTaskFromGoogleAPI.getLocationSettingsStates();

            if (userSettingsLocationState.isLocationPresent() && userSettingsLocationState.isLocationUsable()) {
                findLocation();
            }

        }
        catch (ApiException fusedApiException) {
            dealWithFusedApiRequestFailure(fusedApiException); // call to .getResult() on Google Services API returned exception, get that for examination.
        }
    }

    @SuppressLint("MissingPermission")
    private void findLocation() {
        FusedLocationProviderClient googleFusedLocationProviderClient=
                LocationServices.getFusedLocationProviderClient(this);


        /*
        calling getLastLocation() on the FusedLocationProviderClient does not hand over the current location.
        The Play Services SDK talks to a separate Play Services Framework app, which in turn handles all of the Play Services work.
        That app may or may not be running, and even if it is, there may or may not be a current location.
        So, getLastLocation() returns a Task object instead.
         */

        googleFusedLocationProviderClient.getLastLocation()

                /*
                On the Task object retrieved, we can either add a success or failure listener.
                Success will bring us the
                 */
                .addOnCompleteListener(this, this::useResult)
                .addOnFailureListener(this, this::dealWithFusedApiRequestFailure);
    }

    private void dealWithFusedApiRequestFailure(Exception e) {

        // Prompt the user a Google Services dialog to turn on his Location
        if (e instanceof ResolvableApiException) {
            try {
                //Note: startResolutionForResult calls startActivityForResult under the hood
                ((ResolvableApiException)e).startResolutionForResult(this, REQUEST_REQUIRED_SETTINGS_FROM_USER);
                return;
            }
            catch (IntentSender.SendIntentException intentSenderException) {
                e=intentSenderException;
            }
        }

        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        Log.e(getClass().getSimpleName(), Integer.toString(R.string.err_prompt_settings_change), e);
        finish();
    }

    private void useResult(Task<Location> task) {
        if (task.getResult()==null) {
            Toast
                    .makeText(this, R.string.err_no_location, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        else {
            generateRideFragment.printUserLocation(task.getResult());
        }
    }

    @Override
    protected void onUserDeniedRequiredPermissions() {
        Toast
                .makeText(this, R.string.err_no_perm, Toast.LENGTH_LONG)
                .show();
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        // Request to get the user location via Google Play API
        if (requestCode == REQUEST_REQUIRED_SETTINGS_FROM_USER) {
            systemSettingsDialogInForeground =false;

            if (resultCode==RESULT_OK) {
                findLocation();
            }
            else {
                Toast.makeText(this, R.string.err_location_service_unavailable, Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void logDeviceHighEndYear(){

        int deviceYearClass = YearClass.get(getApplicationContext());
        Log.d(TAG,"Device year: " + String.valueOf(deviceYearClass) );
    }

}
