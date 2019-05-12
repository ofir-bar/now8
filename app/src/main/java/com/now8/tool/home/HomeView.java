package com.now8.tool.home;

import android.content.Context;

public interface HomeView {

    void onCreateRideSuccess();
    void onCreateRideFailed();
    void onCreateRideNetworkError();

    void onJoinRideSuccess();

    void popJoinRideAlertDialog(Context context, String rideUID);

    void shareRide(String rideUID);

}
