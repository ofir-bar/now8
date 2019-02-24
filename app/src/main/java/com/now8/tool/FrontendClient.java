package com.now8.tool;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FrontendClient {

    @POST("create-ride/")
    Call<ResponseBody> createRide(@Header("Authorization") String driverIdToken);

    @POST("join_ride/{usernameTextView}/{location}/{uid}")
    Call<ResponseBody> joinRide(@Path("usernameTextView") String username, @Path("location")String location, @Path("uid") String uid);

}
