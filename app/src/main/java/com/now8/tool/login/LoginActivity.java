package com.now8.tool.login;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.now8.tool.R;
import com.now8.tool.home.HomeActivity;
import com.now8.tool.networking.NetworkUtils;
import com.now8.tool.networking.Now8Api;
import com.now8.tool.networking.RideSchema;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.now8.tool.Base.getUSER_ID_TOKEN;
import static com.now8.tool.Base.setUSER_ID_TOKEN;

public class LoginActivity extends AppCompatActivity implements LoginView {
    private static final String TAG = "LoginActivity";

    private Button loginButton;
    LoginPresenter mPresentor;

    private Auth0 auth0;

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

        mPresentor = new LoginPresenter();
        mPresentor.attachView(this);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> login());

        auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);

        /*
          if(getRideUIDFromJoinRideLink() != null){
            try{
                popJoinRideAlertDialog();
            }catch (NullPointerException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
         */


    }

    private void joinRideRequest(String rideUID){
        Call<RideSchema> call = NetworkUtils.getNow8Api().joinRide(getUSER_ID_TOKEN(), rideUID);
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
                                Log.e(TAG, credentials.getIdToken());
                                setUSER_ID_TOKEN(credentials.getIdToken());
                                navigateToHome();
                            }
                        });
                    }
                });
    }

    @Override
    public void navigateToHome() {
    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
    finish();
    }
}
