package com.now8.tool.screens.generate_ride;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.now8.tool.FrontendClient;
import com.now8.tool.R;
import com.now8.tool.Ride;
import com.now8.tool.common.Constants;

public class GenerateRideActivity extends FragmentActivity {
    private static final String TAG = "GenerateRideActivity";

    private Button createRide;

    private Auth0 auth0;
    private String userAuthIdToken;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");

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
