package com.now8.tool.model.networking;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RideSchema {

    @SerializedName("rideUID")
    @Expose
    private String rideUID;
    @SerializedName("driver")
    @Expose
    private String driver;
    @SerializedName("passengers")
    @Expose
    private List<Object> passengers = null;
    @SerializedName("time_created")
    @Expose
    private String timeCreated;
    @SerializedName("join_ride_url")
    @Expose
    private String joinRideUrl;

    public String getRideUID() {
        return rideUID;
    }

    public void setRideUID(String rideUID) {
        this.rideUID = rideUID;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public List<Object> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Object> passengers) {
        this.passengers = passengers;
    }

    public String getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getJoinRideUrl() {
        return joinRideUrl;
    }

    public void setJoinRideUrl(String joinRideUrl) {
        this.joinRideUrl = joinRideUrl;
    }

}