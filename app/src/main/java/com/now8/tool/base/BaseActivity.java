package com.now8.tool.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.now8.tool.R;

public class BaseActivity extends AppCompatActivity implements BaseView {

    @Override
    public Context getContext() {
        return this;
    }
    @Override
    public void shareRide(String rideUID) {
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

    protected String getRideUIDFromJoinRideLink(){
        try{
            return this.getIntent().getDataString();
        }
        catch (NullPointerException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    protected void popJoinRideAlertDialog(){
        new AlertDialog.Builder(getContext())
                .setTitle("Would you like to join this ride?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        switch (requestCode){
            case Constants.SHARED_IN_SLACK_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, R.string.info_share_ride_success, Toast.LENGTH_LONG).show();
                    break;
                }
                else if (resultCode == RESULT_CANCELED){
                    Toast.makeText(this, R.string.err_share_ride_fail, Toast.LENGTH_LONG).show();
                    break;
                }

            default: super.onActivityResult(requestCode, resultCode, data);
        }

    }
}
