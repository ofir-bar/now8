package com.now8.tool;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ServerInterface {

    //Driver create a new ride
    @POST("/new_ride/{username}")
    Call<Ride> newRide(@Path("username") String username);

    //User join a ride
    @POST("/join_ride/{username}/{uid}")
    Call<Ride> joinRide(@Path("username") String username, @Path("uid") String uid);
}
