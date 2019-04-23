package com.now8.tool.screens.login;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.provider.AuthCallback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.now8.tool.R;
import com.now8.tool.screens.common.BaseActivity;
import com.now8.tool.screens.generate_ride.GenerateRideActivity;

public class LoginActivity extends BaseActivity implements LoginViewMvcImpl.Listener {
    private static final String TAG = "LoginActivity";

    private Auth0 auth0;
    private LoginViewMvcImpl mViewMvc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewMvc = new LoginViewMvcImpl(LayoutInflater.from(this), null);
        mViewMvc.registerListener(this);

        auth0 = new Auth0(this);
        auth0.setOIDCConformant(true);

        setContentView(mViewMvc.getRootView());
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

                                Intent generateRideActivity = new Intent(LoginActivity.this, GenerateRideActivity.class);
                                LoginActivity.this.startActivity(generateRideActivity);

                            }
                        });
                    }
                });
    }


    @Override
    public void onLoginClicked() {
        Toast.makeText(this, "Hello World", Toast.LENGTH_SHORT).show();
    }
}
