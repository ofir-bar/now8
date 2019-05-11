package com.now8.tool.login;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.now8.tool.R;
import com.now8.tool.base.BaseActivity;
import com.now8.tool.networking.NetworkUtils;
import com.now8.tool.networking.Now8Api;
import com.now8.tool.networking.RideSchema;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private Button createRide;
    private Button loginButton;
    private Now8Api retrofitNetworkRequest;

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
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> login());

        auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);

        createRide = findViewById(R.id.btn_create_ride);
        createRide.setOnClickListener(v -> createRideRequest());
        retrofitNetworkRequest = NetworkUtils.getNow8Api();

        if(getRideUIDFromJoinRideLink() != null){
            try{
                popJoinRideAlertDialog();
            }catch (NullPointerException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void joinRideRequest(String rideUID){
        Call<RideSchema> call = retrofitNetworkRequest.joinRide(userAuthIdToken, rideUID);
        call.enqueue(new Callback<RideSchema>() {
            @Override
            public void onResponse(Call<RideSchema> call, Response<RideSchema> response) {
                Log.e("onResponse", "onResponse Worked");

            }

            @Override
            public void onFailure(Call<RideSchema> call, Throwable t) {
                Log.e(TAG,"onFailure");
                Log.e(TAG,t.getMessage());
            }
        });
    }

    private void createRideRequest(){

        if (userAuthIdToken != null){
            Call<RideSchema> call = retrofitNetworkRequest.createRide("Bearer " + userAuthIdToken);
            call.enqueue(new Callback<RideSchema>() {
                @Override
                public void onResponse(Call<RideSchema> call, Response<RideSchema> response) {
                    Log.e("onResponse", "onResponse Worked");
                    try{
                        shareRide((response.body().getJoinRideUrl()));
                    }catch (NullPointerException e){
                        Log.e("onResponse", e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<RideSchema> call, Throwable t) {
                    Log.e(TAG,"onFailure");
                    Log.e(TAG,t.getMessage());
                }
            });
        }
        else Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();


    }

    private void login() {
        WebAuthProvider.init(auth0)
                .withScheme("demo")
                .withScope("openid profile")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(LoginActivity.this, new AuthCallback() {
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
                                Toast.makeText(LoginActivity.this, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(@NonNull final Credentials credentials) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, credentials.getIdToken());
                                userAuthIdToken = credentials.getIdToken();
                            }
                        });
                    }
                });
    }

}
