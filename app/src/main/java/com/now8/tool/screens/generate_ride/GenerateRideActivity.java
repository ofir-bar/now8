package com.now8.tool.screens.generate_ride;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.now8.tool.AbstractUserPermissions;
import com.now8.tool.FrontendClient;
import com.now8.tool.R;
import com.now8.tool.Ride;
import com.now8.tool.common.Constants;

public class GenerateRideActivity extends AbstractUserPermissions {
    private static final String TAG = "GenerateRideActivity";


    private static final String STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY ="STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY";
    private boolean turnOnLocationInSettingsDialogInForeground = false;

    private Location initialLocation;
    private Button createRide;

    private Auth0 auth0;
    private String userAuthIdToken;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");

        outState.putBoolean(STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY, turnOnLocationInSettingsDialogInForeground);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_ride);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);


        createRide = findViewById(R.id.btn_create_ride);
        createRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    sendNetworkRequest();
            }
        });

        if(handleJoinRideRequest() != null){
            try{
                String rideUID = "";
                joinRideAlertDialog(this, rideUID);

            }catch (NullPointerException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    }

    //TODO : do not work with permissions in any activity. Move them to a "BaseActivity" so in all
    // activities you will need them you will have them without duplicating code
    // I saw that you have the "AbstractUserPermissions" but you have to deal with the 'results" too.
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
                ((ResolvableApiException)e).startResolutionForResult(this, Constants.REQUEST_TO_TURN_ON_LOCATION_IN_SETTINGS_DIALOG);
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
            initialLocation = getUserLocationTask.getResult();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Log.d(TAG,"onActivityResult");

        if (requestCode == Constants.REQUEST_TO_TURN_ON_LOCATION_IN_SETTINGS_DIALOG) {
            turnOnLocationInSettingsDialogInForeground = false; // Avoid showing multiply dialogs. (e.g. due to configuration change - screen rotate)

            if (resultCode==RESULT_OK) {
                getDeviceLocation();
            }
            else {
                Toast.makeText(this, R.string.err_user_denied_system_location_turn_on, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        else if (requestCode == Constants.SHARED_IN_SLACK_REQUEST_CODE){

            if (resultCode == RESULT_OK){
                Toast.makeText(this, R.string.info_share_ride_success, Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, R.string.err_share_ride_fail, Toast.LENGTH_LONG).show();
            }
        }

        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void shareRideToSlack(String rideUID) {
        Intent shareToSlack = new Intent(Intent.ACTION_SEND);
        if(rideUID == null){
            Toast.makeText(this, "rideUID is null", Toast.LENGTH_SHORT).show();
            return;
        }
        shareToSlack.putExtra(Intent.EXTRA_TEXT, rideUID);
        shareToSlack.setPackage(Constants.SLACK_PACKAGE);
        shareToSlack.setType("text/plain");
        startActivityForResult(shareToSlack, Constants.SHARED_IN_SLACK_REQUEST_CODE);
    }

    private String handleJoinRideRequest(){
        Intent intent  = this.getIntent();
        try
        {
            String rideUID = intent.getDataString();
            //TODO : strings to resources
            Toast.makeText(this, "handleJoinRideRequest: " + rideUID, Toast.LENGTH_SHORT).show();
            return rideUID;
        }
        catch (NullPointerException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void joinRideAlertDialog(Context context, String rideUID){
        //TODO: strings to resources
        new AlertDialog.Builder(context)
            .setTitle("Would you like to join this ride?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    joinRideNetworkRequest(rideUID);
                    dialog.dismiss();
                }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.cancel();
                }
            })
            .show();

            }


            //TODO: retrofit calls and everything should be "in one place"
    private void joinRideNetworkRequest(String rideUID){

        Retrofit.Builder retrofitConf = new Retrofit.Builder()
                .baseUrl(Constants.AWS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = retrofitConf.build();

        FrontendClient retrofitNetworkRequest = retrofit.create(FrontendClient.class);

        Call<Ride> call = retrofitNetworkRequest.joinRide(userAuthIdToken, rideUID);

        call.enqueue(new Callback<Ride>() {
            @Override
            public void onResponse(Call<Ride> call, Response<Ride> response) {
                Log.e("onResponse", "onResponse Worked");

            }

            @Override
            public void onFailure(Call<Ride> call, Throwable t) {
                Log.e(TAG,"onFailure");
                Log.e(TAG,t.getMessage());
            }
        });
    }


    private void sendNetworkRequest(){
        Retrofit.Builder retrofitConf = new Retrofit.Builder()
                        .baseUrl(Constants.AWS_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = retrofitConf.build();

        FrontendClient retrofitNetworkRequest = retrofit.create(FrontendClient.class);


        if (userAuthIdToken != null){

            Call<Ride> call = retrofitNetworkRequest.createRide("Bearer " + userAuthIdToken);

            call.enqueue(new Callback<Ride>() {
                @Override
                public void onResponse(Call<Ride> call, Response<Ride> response) {
                    Log.e("onResponse", "onResponse Worked");

                    try{
                        shareRideToSlack((response.body().getJoinRideUrl()));
                    }catch (NullPointerException e){
                        Log.e("onResponse", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<Ride> call, Throwable t) {
                    Log.e(TAG,"onFailure");
                    Log.e(TAG,t.getMessage());
                }
            });
        }
        else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
        }


    }

    private void login() {
        WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withScope("openid profile")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(GenerateRideActivity.this, new AuthCallback() {
                    @Override
                    public void onFailure(@NonNull final Dialog dialog) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(final AuthenticationException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GenerateRideActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(@NonNull final Credentials credentials) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GenerateRideActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, credentials.getIdToken());
                                userAuthIdToken = credentials.getIdToken();
                            }
                        });
                    }
                });
    }

}
