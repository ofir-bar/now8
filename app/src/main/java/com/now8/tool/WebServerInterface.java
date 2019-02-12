package com.now8.tool;

import android.location.Location;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServerInterface {

    @POST("/new_ride/{usernameTextView}/{location}")
    Call<Ride> createRide(@Path("usernameTextView") String username, @Path("location")String location);

    @POST("/join_ride/{usernameTextView}/{location}/{uid}")
    Call<Ride> joinRide(@Path("usernameTextView") String username, @Path("location")String location, @Path("uid") String uid);

}
