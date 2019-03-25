package com.now8.tool;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FrontendClient {

    @POST("create-ride/")
    Call<ResponseBody> createRide();

    @POST("join_ride/{ride_uid}")
    Call<ResponseBody> joinRide(@Path("ride_uid") String rideUID);

}
