package com.now8.tool.home;

public interface HomeView {

    void onCreateRideSuccess();
    void onCreateRideFailed();
    void onCreateRideNetworkError();

    void shareRide(String rideUID);
}
