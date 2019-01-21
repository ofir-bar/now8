package com.now8.tool;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private static final int REQUEST_RESOLUTION=61124;
    private static final String STATE_IN_RESOLUTION="inResolution";
    private static final String[] PERMS=
            {Manifest.permission.ACCESS_FINE_LOCATION};
    private GenerateLinkFragment fragment;
    private boolean isSystemSettingsChangeRequestInProgress=false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IN_RESOLUTION, isSystemSettingsChangeRequestInProgress);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logDeviceYear();

    }



    /*

    By extending AbstractLocationPermissionActivity we need to implement:
    getDesiredPermissions()
    onReady()
    onPermissionDenied()

     */

    //  getDesiredPermissions(), returning the array of permission names that the activity needs in order to proceed
    @Override
    protected String[] getDesiredPermissions() {
        return(PERMS);
    }

    //  onReady(), called by AbstractPermissionActivity once we get all of the requested permissions
    @Override
    protected void onReady(Bundle state) {
        if (state!=null) {
            isSystemSettingsChangeRequestInProgress=state.getBoolean(STATE_IN_RESOLUTION, false);
        }

        fragment=
                (GenerateLinkFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);

        // TODO: YOU SHOULD NOT USE commitAllowingStateLoss in production.
        // This is just a temporary fix. please see medium article:
        // https://medium.com/@elye.project/handling-illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-d4ee8b630066

        if (fragment==null) {
            fragment=new GenerateLinkFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, fragment).commitAllowingStateLoss();

        }

        if (!isSystemSettingsChangeRequestInProgress) {
            isSystemSettingsChangeRequestInProgress=true;
            Toast.makeText(this, "Hello nigger", Toast.LENGTH_LONG).show();

            // AVOID ISSUES IF USER-LOCATION IS TURNED OFF
            // A LocationSettingsRequest makes a request to Play Services API to find out if the location settings on
            // the device matches the requirements of a supplied LocationRequest

            LocationSettingsRequest request=new LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationRequest.create())
                    .build();

            LocationServices.getSettingsClient(this)
                    .checkLocationSettings(request)
                    .addOnCompleteListener(this::handleSettingsResponse);
                    /*
                    There is no addOnFailureListener here.
                    Page 4846 for explanation
                     */
        }
    }

    // ask the user for location or prompt him to enable location on the device system
    private void handleSettingsResponse(Task<LocationSettingsResponse> task) {
        try {
            LocationSettingsResponse response=task.getResult(ApiException.class);
            LocationSettingsStates states=response.getLocationSettingsStates();

            if (states.isLocationPresent() && states.isLocationUsable()) {
                findLocation(); // Location settings turned on - find the user location
            }
            else {
                deviceLocationServiceUnavailable(); // Location settings turned off - exit and inform the user
            }
        }
        catch (ApiException e) {
            copeWithFailure(e); // something with call to .getResult() on Google Services API is fucked up, so get that exception for examination.
        }
    }

    @SuppressLint("MissingPermission")
    private void findLocation() {
        FusedLocationProviderClient client=
                LocationServices.getFusedLocationProviderClient(this);


        /*
        calling getLastLocation() on the FusedLocationProviderClient does not hand over the current location.
        The Play Services SDK talks to a separate Play Services Framework app, which in turn handles all of the Play Services work.
        That app may or may not be running, and even if it is, there may or may not be a current location.
        So, getLastLocation() returns a Task object instead.
         */

        client.getLastLocation()

                /*
                On the Task object retrieved, we can either add a success or failure listener.
                Success will bring us the
                 */
                .addOnCompleteListener(this, this::useResult)
                .addOnFailureListener(this, this::copeWithFailure);
    }
    private void deviceLocationServiceUnavailable() {
        Toast.makeText(this, R.string.msg_location_service_unavailable, Toast.LENGTH_LONG)
                .show();
        finish();
    }
    private void copeWithFailure(Exception e) {

        // Prompt the user a Google Services dialog to turn on his Location
        if (e instanceof ResolvableApiException) {
            try {
                //Note: startResolutionForResult calls startActivityForResult under the hood
                ((ResolvableApiException)e).startResolutionForResult(this, REQUEST_RESOLUTION);
                return;
            }
            catch (IntentSender.SendIntentException e1) {
                e=e1;
            }
        }

        // Couldn't get the user to turn on his location.
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        Log.e(getClass().getSimpleName(), "Exception getting location", e);
        finish();
    }
    private void useResult(Task<Location> task) {
        if (task.getResult()==null) {
            Toast
                    .makeText(this, R.string.msg_no_location, Toast.LENGTH_LONG)
                    .show();
            finish();
        }
        else {
            fragment.printUserLocation(task.getResult());
        }
    }
    //  onPermissionDenied(), called by AbstractPermissionActivity if the user did not grant us all of the requested permissions when we asked for them
    @Override
    protected void onPermissionDenied() {
        Toast
                .makeText(this, R.string.msg_no_perm, Toast.LENGTH_LONG)
                .show();
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        // Request to get the user location via Google Play API
        if (requestCode==REQUEST_RESOLUTION) {
            isSystemSettingsChangeRequestInProgress=false;

            if (resultCode==RESULT_OK) {
                findLocation();
            }
            else {
                deviceLocationServiceUnavailable();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //Log the device high-end year for statistics
    private void logDeviceYear(){

        int deviceYearClass = YearClass.get(getApplicationContext());
        Log.d(TAG,"Device year: " + String.valueOf(deviceYearClass) );
    }

}
