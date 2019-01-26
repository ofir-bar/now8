package com.now8.tool;

import android.location.Location;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface WebServerInterface {

    @POST("/new_ride/{username}/{location}")
    Call<Ride> createRide(@Path("username") String username, @Path("location")Location location);

    @POST("/join_ride/{username}/{location}/{uid}")
    Call<Ride> joinRide(@Path("username") String username, @Path("location")Location location, @Path("uid") String uid);
}
