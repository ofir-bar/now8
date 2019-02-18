package com.now8.tool;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServerInterface {

    @GET("tests")
    Call<ResponseBody> pingServer();

    @POST("join_ride/{usernameTextView}/{location}/{uid}")
    Call<Ride> joinRide(@Path("usernameTextView") String username, @Path("location")String location, @Path("uid") String uid);

}
