package com.now8.tool;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
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

public class MainActivity extends AbstractUserPermissions {
    private static final String TAG = "MainActivity";


    private static final String STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY ="STATE_OF_SYSTEM_SETTINGS_DIALOG_KEY";
    private boolean turnOnLocationInSettingsDialogInForeground = false;

    private static final int REQUEST_TO_TURN_ON_LOCATION_IN_SETTINGS_DIALOG = 61124;

    private static final String SLACK_PACKAGE = "com.Slack";
    private static final int SHARED_IN_SLACK_REQUEST_CODE = 1;

    private static final String AWS_BASE_URL = "https://i4lyu11ra8.execute-api.eu-west-1.amazonaws.com/development/";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyCZi1vM-znzbHeys2suFJPeBJP5giqyS2U";
    private static final String GOOGLE_MAPS_DISTANCE_MATRIX_BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/" + "json?";

    private Location initialLocation;
    private Button createRide;

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
        setContentView(R.layout.activity_main);

        if (!AWSMobileClient.getInstance().isSignedIn()){
            Log.e("onCreate", "User is not signed in!");
            finish();
            Intent i = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(i);
        }

        TextView helloUser = findViewById(R.id.hello_user);
        try{
            helloUser.setText(AWSMobileClient.getInstance().getUsername());
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        createRide = findViewById(R.id.btn_create_ride);
        createRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    sendNetworkRequest();
            }
        });

        TextView logOut = findViewById(R.id.log_out);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            AWSMobileClient.getInstance().signOut();
            Intent i = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(i);
            finish();
            }
        });

        if(handleJoinRideRequest() != null){
            try{
                String fullJoinRideRequest = handleJoinRideRequest();
                String rideUID = stripRideUIDFromFullJoinRideRequest(fullJoinRideRequest);
                joinRideAlertDialog(this, rideUID);

            }catch (NullPointerException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }


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
            initialLocation = getUserLocationTask.getResult();
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

        else if (requestCode == SHARED_IN_SLACK_REQUEST_CODE){

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
        shareToSlack.setPackage(SLACK_PACKAGE);
        shareToSlack.setType("text/plain");
        startActivityForResult(shareToSlack, SHARED_IN_SLACK_REQUEST_CODE);
    }

    private String processRideUID(String rideUID){
        String SCHEME = "now8";
        String HOST = "join_ride";
        return SCHEME + "://" + HOST + "/" + rideUID;
    }

    private String handleJoinRideRequest(){
        Intent intent  = this.getIntent();
        try
        {
            String rideUID = intent.getDataString();
            Toast.makeText(this, "handleJoinRideRequest: " + rideUID, Toast.LENGTH_SHORT).show();
            return rideUID;
        }
        catch (NullPointerException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String stripRideUIDFromFullJoinRideRequest(String fullJoinRideRequest){

        String rideUIDTemp[] = fullJoinRideRequest.split("now8://join_ride/");
        return rideUIDTemp[1];
    }

    private void joinRideAlertDialog(Context context, String rideUID){
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

    private void joinRideNetworkRequest(String rideUID){

        Retrofit.Builder retrofitConf = new Retrofit.Builder()
                .baseUrl(AWS_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = retrofitConf.build();

        FrontendClient retrofitNetworkRequest = retrofit.create(FrontendClient.class);
        String tokenId = "";
        try{
            tokenId = AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
        }catch (Exception e){
            Log.e("tokenId", e.getMessage());
        }

        Call<ResponseBody> call = retrofitNetworkRequest.joinRide(tokenId, rideUID);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("onResponse", "onResponse Worked");

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG,"onFailure");
                Log.e(TAG,t.getMessage());
            }
        });
    }


    private void sendNetworkRequest(){
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
//        // avoid logging network data (may be sensitive info) in production
//            if(BuildConfig.DEBUG){
//                okHttpBuilder.addInterceptor(logging);
//            }

        Retrofit.Builder retrofitConf = new Retrofit.Builder()
                        .baseUrl(AWS_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = retrofitConf.build();

        FrontendClient retrofitNetworkRequest = retrofit.create(FrontendClient.class);
        String tokenId = "";
        try{
            tokenId = AWSMobileClient.getInstance().getTokens().getIdToken().getTokenString();
        }catch (Exception e){
            Log.e("tokenId", e.getMessage());
        }

        Call<ResponseBody> call = retrofitNetworkRequest.createRide(tokenId);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("onResponse", "onResponse Worked");

                try{
                    shareRideToSlack(processRideUID(response.body().getRideUid()));
                }catch (NullPointerException e){
                    Log.e("onResponse", e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG,"onFailure");
                Log.e(TAG,t.getMessage());
            }
        });
    }

}
