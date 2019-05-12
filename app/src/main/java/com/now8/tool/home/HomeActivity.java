package com.now8.tool.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import com.now8.tool.R;
import com.now8.tool.login.LoginActivity;

import static com.now8.tool.Base.getUSER_ID_TOKEN;
import static com.now8.tool.Constants.SHARED_IN_SLACK_REQUEST_CODE;
import static com.now8.tool.Constants.SLACK_PACKAGE;


public class HomeActivity extends AppCompatActivity implements HomeView{
    private static final String TAG = "HomeActivity";
    HomePresenter mPresenter;
    Button createRide;
    String rideUID;

    @Override
    protected void onStart() {
        super.onStart();
        rideUID = getRideUidFromRideInvite();
        if(rideUID != null){
            try{
                popJoinRideAlertDialog(this, rideUID);

            }catch (NullPointerException e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Checks if the user is logged in
        if(getUSER_ID_TOKEN() == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

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
        Toast.makeText(this, R.string.home_ride_create_success, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCreateRideFailed() {
        Toast.makeText(this, R.string.home_ride_create_fail, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCreateRideNetworkError() {
        Toast.makeText(this, R.string.home_ride_create_network_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onJoinRideSuccess() {
        Toast.makeText(this, R.string.home_ride_join_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void popJoinRideAlertDialog(Context context, String rideUID) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.home_ride_join_dialog_title)
                .setPositiveButton(R.string.home_ride_join_dialog_response_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mPresenter.joinRide(rideUID);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.home_ride_join_dialog_response_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                })
                .show();
    }
    private String getRideUidFromRideInvite(){
        try
        {
            return this.getIntent().getDataString();
        }
        catch (NullPointerException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
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
