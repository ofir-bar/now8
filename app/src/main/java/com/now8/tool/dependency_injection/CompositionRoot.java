package com.now8.tool.dependency_injection;

import com.now8.tool.model.common.Constants;
import com.now8.tool.model.networking.Now8Api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CompositionRoot {

    private Retrofit mRetrofit;

    // Allow only 1 instance of Retrofit to be used through the entire app
    private Retrofit getRetrofit(){

        if (mRetrofit == null){
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.AWS_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofit;
    }

    public Now8Api getNow8Api(){
        return getRetrofit().create(Now8Api.class);
    }


}
