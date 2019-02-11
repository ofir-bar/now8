package com.now8.tool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
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

public class MainActivityUser extends AbstractUserLocationPermissions {

    private static final String TAG = "MainActivityUser";

    private CreateRideFragment generateRideFragment;

    private static final String STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY ="STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY";
    private boolean turnOnLocationInSettingsDialogInForeground = false;

    private static final int REQUEST_TO_TURN_ON_LOCATION_IN_SETTINGS_DIALOG = 61124;
    private static final String[] REQUIRED_PERMISSIONS=
            {Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");

        outState.putBoolean(STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY, turnOnLocationInSettingsDialogInForeground);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        setContentView(R.layout.activity_main);
        logDeviceHighEndYear();
    }
    private void logDeviceHighEndYear(){

        int deviceYearClass = YearClass.get(getApplicationContext());
        Log.d(TAG,"Device high-end year: " + String.valueOf(deviceYearClass) );
    }

    @Override
    protected String[] getRequiredUserPermissions() {
        Log.d(TAG,"getRequiredUserPermissions");

        return(REQUIRED_PERMISSIONS);
    }
    @Override
    protected void onRequiredPermissionsGranted(Bundle mainActivityState) {
        Log.d(TAG,"onRequiredPermissionsGranted");

        if (mainActivityState != null) {
            turnOnLocationInSettingsDialogInForeground = mainActivityState.getBoolean(STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY);
        }


        if (!turnOnLocationInSettingsDialogInForeground) {
            turnOnLocationInSettingsDialogInForeground = true;

            LocationSettingsRequest requiredDeviceSettingsForLocationRequests = new LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationRequest.create())
                    .build();

            LocationServices.getSettingsClient(this)
                    .checkLocationSettings(requiredDeviceSettingsForLocationRequests)
                    .addOnCompleteListener(this::requestLocationViaFusedApi);
        }

        initializeCreateRideFragment();

    }
    private void initializeCreateRideFragment(){
        Log.d(TAG,"initializeCreateRideFragment");

        generateRideFragment =
                (CreateRideFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);

        if (generateRideFragment == null) {
            generateRideFragment = new CreateRideFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, generateRideFragment).commitAllowingStateLoss(); //TODO: you should not use commitAllowingStateLoss() in production

        }

    }
    @Override
    protected void onUserDeniedRequiredPermissions() {
        Log.d(TAG,"onUserDeniedRequiredPermissions");

        Toast
                .makeText(this, R.string.err_no_perm, Toast.LENGTH_LONG)
                .show();
        finish();
    }

    private boolean requestLocationViaFusedApi(Task<LocationSettingsResponse> task) {
        Log.d(TAG,"requestLocationViaFusedApi");

        try {
            if(isLocationAvailable(task)){
                getDeviceLocation();
            }
        }
        catch (ApiException fusedApiException) {
            turnOnLocationInSettingsDialogOrToastLocationIsNotUsable(fusedApiException);
            return false;
        }
        return false;
    }

    // checks that GPS or network location provider is usable and present
    private boolean isLocationAvailable(Task<LocationSettingsResponse> task) throws ApiException{
        Log.d(TAG,"isLocationAvailable");

        LocationSettingsResponse locationSettingsResponseTaskFromGoogleAPI = task.getResult(ApiException.class);
        LocationSettingsStates userSettingsLocationState = locationSettingsResponseTaskFromGoogleAPI.getLocationSettingsStates();

        return ( userSettingsLocationState.isLocationPresent() && userSettingsLocationState.isLocationUsable() );

    }
    private void turnOnLocationInSettingsDialogOrToastLocationIsNotUsable(Exception e) {
        Log.d(TAG,"turnOnLocationInSettingsDialogOrToastLocationIsNotUsable");


        if (e instanceof ResolvableApiException) {
            try {
                //Note: startResolutionForResult calls startActivityForResult, and that opens a turnOnLocationInSettingsDialog Activity
                ((ResolvableApiException)e).startResolutionForResult(this, REQUEST_TO_TURN_ON_LOCATION_IN_SETTINGS_DIALOG);
            }
            // Couldn't pop a turnOnLocationInSettingsDialog
            catch (IntentSender.SendIntentException intentSenderException) {
                locationUnavailable(this, intentSenderException);
            }
        }

    }

    private void locationUnavailable(Context context, Exception e){

        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        Log.e(getClass().getSimpleName(), Integer.toString(R.string.err_failed_to_request_settings_change_dialog), e);
        finish();
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        Log.d(TAG,"getDeviceLocation");

        FusedLocationProviderClient client=
                LocationServices.getFusedLocationProviderClient(this);

        // Note: getLastLocation() does no give a Location object, It returns a Task.
        // The Play Services SDK talks to a separate Play Services Framework app, which in turn handles all of the Play Services work
        // Using the response from that Play Service we can either get location or fail to.

        client.getLastLocation()
        .addOnCompleteListener(this, this::locationRequestSuccess);
                // TODO: add a failure scenario
    }
    private void locationRequestSuccess(Task<Location> getUserLocationTask) {
        Log.d(TAG,"locationRequestSuccess");

        if (getUserLocationTask.getResult() == null) {
            Toast
                    .makeText(this, R.string.err_location_null, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        else {
            generateRideFragment.setDeviceInitialLocation(getUserLocationTask.getResult());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.d(TAG,"onActivityResult");

        if (requestCode == REQUEST_TO_TURN_ON_LOCATION_IN_SETTINGS_DIALOG) {
            turnOnLocationInSettingsDialogInForeground = false; // Avoid showing multiply dialogs. (e.g. due to configuration change - screen rotate)

            if (resultCode==RESULT_OK) {
                getDeviceLocation();
            }
            else {
                Toast.makeText(this, R.string.err_user_denied_system_location_turn_on, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
