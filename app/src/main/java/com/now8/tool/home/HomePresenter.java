package com.now8.tool.home;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.now8.tool.networking.NetworkUtils;
import com.now8.tool.networking.RideSchema;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.now8.tool.Base.getUSER_ID_TOKEN;
import static com.now8.tool.Constants.SHARED_IN_SLACK_REQUEST_CODE;
import static com.now8.tool.Constants.SLACK_PACKAGE;

public class HomePresenter {
    HomeView mView;

    void attachView(HomeView view){
        mView = view;
    }

    void detachView(){
        mView = null;
    }

    void createRide(){

        if (getUSER_ID_TOKEN() != null){
            Call<RideSchema> call = NetworkUtils.getNow8Api().createRide("Bearer " + getUSER_ID_TOKEN());
            call.enqueue(new Callback<RideSchema>() {
                @Override
                public void onResponse(Call<RideSchema> call, Response<RideSchema> response) {

                    try{
                        mView.onCreateRideSuccess();
                        mView.shareRide((response.body().getJoinRideUrl()));
                    }catch (NullPointerException e){
                        mView.onCreateRideNetworkError();
                    }
                }

                @Override
                public void onFailure(Call<RideSchema> call, Throwable t) {
                    mView.onCreateRideFailed();
                }
            });
        }
        else mView.onCreateRideNetworkError();
}


}
