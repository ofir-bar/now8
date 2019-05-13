package com.now8.tool.networking;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Now8Api {
    static final String BEARER = "Bearer ";
    @POST("create_ride/")
    Call<RideSchema> createRide(@Header("Authorization") String driverIdToken);

    @PUT("join_ride/{ride_uid}")
    Call<RideSchema> joinRide(@Header("Authorization") String passengerIdToken, @Path("ride_uid") String rideUID);

}
