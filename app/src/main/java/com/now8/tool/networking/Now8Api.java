package com.now8.tool.networking;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface Now8Api {

    @POST("create_ride/")
    Call<RideSchema> createRide(@Header("Authorization") String driverIdToken);

    @PUT("join_ride/")
    Call<RideSchema> joinRide(@Header("Authorization") String passengerIdToken, @Query("rideUID") String rideUID);

}