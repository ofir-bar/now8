package com.now8.tool.networking;

import com.now8.tool.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkUtils {

    private NetworkUtils(){}

    private static Retrofit mRetrofit;
    private static Retrofit getRetrofit() {
        if (mRetrofit == null) {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl(Constants.AWS_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mRetrofit;
    }

    public static Now8Api getNow8Api(){
        return getRetrofit().create(Now8Api.class);
    }

}
