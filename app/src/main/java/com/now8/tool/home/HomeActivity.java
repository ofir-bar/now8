package com.now8.tool.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.now8.tool.R;
import com.now8.tool.networking.NetworkUtils;
import com.now8.tool.networking.Now8Api;
import com.now8.tool.networking.RideSchema;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.now8.tool.Base.getUSER_ID_TOKEN;
import static com.now8.tool.Constants.SHARED_IN_SLACK_REQUEST_CODE;
import static com.now8.tool.Constants.SLACK_PACKAGE;


public class HomeActivity extends AppCompatActivity implements HomeView{
    private static final String TAG = "HomeActivity";
    HomePresenter mPresenter;
    Button createRide;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mPresenter = new HomePresenter();
        mPresenter.attachView(this);

        createRide = findViewById(R.id.btn_create_ride);
        createRide.setOnClickListener(v -> mPresenter.createRide());

    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onCreateRideSuccess() {
        Toast.makeText(this, "Ride created successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateRideFailed() {
        Toast.makeText(this, "Ride creation failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreateRideNetworkError() {
        Toast.makeText(this, "network error: ride create failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void shareRide(String rideUID) {
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


}
