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

public class MainActivityUser extends AbstractUserLocationPermissions {

    private static final String TAG = "MainActivityUser";

    private CreateRideFragment generateRideFragment;

    private static final String SYSTEM_SETTINGS_DIALOG_KEY ="SYSTEM_SETTINGS_DIALOG_KEY";
    private boolean systemSettingsDialogInForeground = false;

    private static final int REQUEST_REQUIRED_SETTINGS_FROM_USER = 61124;
    private static final String[] REQUIRED_PERMISSIONS=
            {Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");

        outState.putBoolean(SYSTEM_SETTINGS_DIALOG_KEY, systemSettingsDialogInForeground);
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
            systemSettingsDialogInForeground = mainActivityState.getBoolean(SYSTEM_SETTINGS_DIALOG_KEY);
        }


        if (!systemSettingsDialogInForeground) {
            systemSettingsDialogInForeground = true;

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
            isFusedApiLocationSettingsAvailable(task);
        }
        catch (ApiException fusedApiException) {
            requestLocationSystemDialogOrToastLocationUnavailable(fusedApiException);
            return false;
        }
        return false;
    }
    private boolean isFusedApiLocationSettingsAvailable(Task<LocationSettingsResponse> task) throws ApiException{
        Log.d(TAG,"isFusedApiLocationSettingsAvailable");

        LocationSettingsResponse locationSettingsResponseTaskFromGoogleAPI=task.getResult(ApiException.class);
        LocationSettingsStates userSettingsLocationState=locationSettingsResponseTaskFromGoogleAPI.getLocationSettingsStates();

        return (userSettingsLocationState.isLocationPresent() && userSettingsLocationState.isLocationUsable());

    }
    private void requestLocationSystemDialogOrToastLocationUnavailable(Exception e) {
        Log.d(TAG,"requestLocationSystemDialogOrToastLocationUnavailable");

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

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        Log.d(TAG,"getUserLocation");

        FusedLocationProviderClient googleFusedLocationProviderClient=
                LocationServices.getFusedLocationProviderClient(this);

        googleFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(this, this::fusedApiReturnedLocation)
                .addOnFailureListener(this, this::requestLocationSystemDialogOrToastLocationUnavailable);
    }
    private void fusedApiReturnedLocation(Task<Location> getUserLocationTask) {
        Log.d(TAG,"fusedApiReturnedLocation");

        if (getUserLocationTask.getResult()==null) {
            Toast
                    .makeText(this, R.string.err_no_location, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        else {
            generateRideFragment.userInitialLocation = getUserLocationTask.getResult();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.d(TAG,"onActivityResult");

        if (requestCode == REQUEST_REQUIRED_SETTINGS_FROM_USER) {
            systemSettingsDialogInForeground =false;

            if (resultCode==RESULT_OK) {
                getUserLocation();
            }
            else {
                Toast.makeText(this, R.string.err_system_location_service_disabled, Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
