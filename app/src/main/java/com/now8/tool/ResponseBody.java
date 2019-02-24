package com.now8.tool;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseBody {

    @SerializedName("ride_uid")
    @Expose
    private String rideUid;
    @SerializedName("driver_uid")
    @Expose
    private String driverUid;
    @SerializedName("users_uid")
    @Expose
    private String usersUid;

    public String getRideUid() {
        return rideUid;
    }

    public void setRideUid(String rideUid) {
        this.rideUid = rideUid;
    }

    public String getDriverUid() {
        return driverUid;
    }

    public void setDriverUid(String driverUid) {
        this.driverUid = driverUid;
    }

    public String getUsersUid() {
        return usersUid;
    }

    public void setUsersUid(String usersUid) {
        this.usersUid = usersUid;
    }

}