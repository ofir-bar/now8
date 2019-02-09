package com.now8.tool;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ride {

    @SerializedName("uid")
    @Expose
    private String uid;

    @SerializedName("driver")
    @Expose
    private String driver;

    @SerializedName("passenger_users")
    @Expose
    private List<Object> passengerUsers = null;

    @SerializedName("time_created")
    @Expose
    private String timeCreated;


    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDriver() {
        return driver;
    }
    public void setDriver(String driver) {
        this.driver = driver;
    }

    public List<Object> getPassengerUsers() {
        return passengerUsers;
    }
    public void setPassengerUsers(List<Object> passengerUsers) {
        this.passengerUsers = passengerUsers;
    }

    public String getTimeCreated() {
        return timeCreated;
    }
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = timeCreated;
    }

}