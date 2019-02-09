package com.now8.tool;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class CreateRideFragment extends Fragment {

    private static final String TAG = "CreateRideFragment";

    private static final String WEBSERVER_BASE_URL = "http://10.0.0.45:8000/";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyCZi1vM-znzbHeys2suFJPeBJP5giqyS2U";
    private static final String GOOGLE_MAPS_DISTANCE_MATRIX_BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/" + "json?";

    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final int WHATSAPP_REQUEST_CODE = 1;

    Button createRideButton;
    EditText driverUsername;
    Location userInitialLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        return inflater.inflate(R.layout.fragment_create_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG,"onViewCreated");

        Retrofit retrofitConf =
                new Retrofit.Builder()
                        .baseUrl(WEBSERVER_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

        WebServerInterface clientNetworkRequest =
                retrofitConf.create(WebServerInterface.class);

        createRideButton = getActivity().findViewById(R.id.btn_create_ride);
        driverUsername = getActivity().findViewById(R.id.edit_text_username);

        createRideButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                retrofitNetworkRequest(clientNetworkRequest);
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult");

        if (requestCode == WHATSAPP_REQUEST_CODE){

            if (resultCode == RESULT_OK){
                Toast.makeText(getContext(), R.string.info_share_ride_success, Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(getContext(), R.string.err_share_ride_fail, Toast.LENGTH_LONG).show();
            }
        }

    }


    private void retrofitNetworkRequest(WebServerInterface clientNetworkRequest){
        Log.d(TAG,"retrofitNetworkRequest");

        clientNetworkRequest.createRide(driverUsername.getText().toString(), userInitialLocation).enqueue(new retrofit2.Callback<Ride>() {
            @Override
            public void onResponse(retrofit2.Call<Ride> call, retrofit2.Response<Ride> responseRide) {
                Log.e(TAG, "Connection succeed");
//                        shareRideToWhatsapp(WEBSERVER_BASE_URL + responseRide.body().getUid());
            }

            @Override
            public void onFailure(retrofit2.Call<Ride> call, Throwable t) {
                Log.e(TAG, "Connection Failed", t);
            }
        });

    }

    private void shareRideToWhatsapp(String rideUID) {

        Intent shareToWhatsapp = new Intent(Intent.ACTION_SEND);
        shareToWhatsapp.putExtra(Intent.EXTRA_TEXT, rideUID);
        shareToWhatsapp.setPackage(WHATSAPP_PACKAGE);
        shareToWhatsapp.setType("text/plain");
        startActivityForResult(shareToWhatsapp, WHATSAPP_REQUEST_CODE);
    }

    private void printUserLocation(Location location) {
        double roundedLat=(double)Math.round(location.getLatitude()*10000d)/10000d;
        double roundedLon=(double)Math.round(location.getLongitude()*10000d)/10000d;
    }

}
