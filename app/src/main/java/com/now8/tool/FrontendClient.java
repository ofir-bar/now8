package com.now8.tool;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface FrontendClient {

    @POST("create_ride/")
    Call<ResponseBody> createRide(@Header("Authorization") String driverIdToken);

    @PUT("join_ride/{ride_uid}")
    Call<ResponseBody> joinRide(@Header("Authorization") String passengerIdToken, @Path("ride_uid") String rideUID);

}
