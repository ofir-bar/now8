package com.now8.tool;

import android.content.Context;
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


import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;




public class GenerateLinkFragment extends Fragment {
    private static final String TAG = "GenerateLinkFragment";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String BASE_URL = "http://10.0.0.45:8000/";
    private static final String GOOGLE_MAPS_API_KEY = "AIzaSyCZi1vM-znzbHeys2suFJPeBJP5giqyS2U";
    private static final String GOOGLE_MAPS_DISTANCE_MATRIX_BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/" + "json?";

    //Request Codes
    private static final int WHATSAPP_REQUEST_CODE = 1;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_generate_link, container, false);

        // Inflate the layout for this fragment
        return fragmentView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

        ServerInterface si =
                retrofit.create(ServerInterface.class);

        Button btn_generate = getActivity().findViewById(R.id.btn_new_ride);
        EditText driver_username = getActivity().findViewById(R.id.edit_text_driver);

        btn_generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


//                si.newRide(driver_username.getText().toString()).enqueue(new retrofit2.Callback<Ride>() {
//                    @Override
//                    public void onResponse(retrofit2.Call<Ride> call, retrofit2.Response<Ride> response) {
//                        Log.e(TAG, "Connection Succeed");
//                        shareRideToWhatsapp(BASE_URL + response.body().getUid());
//                    }
//
//                    @Override
//                    public void onFailure(retrofit2.Call<Ride> call, Throwable t) {
//                        Log.e(TAG, "Connection Failed", t);
//                    }
//                });


            }
        });


    }

    /**
     * method share the generated link to another app
     *
     * @param rideUID = the unique generated link which will be shared on external app
     */
    public void shareRideToWhatsapp(String rideUID) {

        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, rideUID);
        whatsappIntent.setPackage(WHATSAPP_PACKAGE);
        whatsappIntent.setType("text/plain");
        startActivityForResult(whatsappIntent, WHATSAPP_REQUEST_CODE);
    }

    void printUserLocation(Location location) {
        double roundedLat=(double)Math.round(location.getLatitude()*10000d)/10000d;
        double roundedLon=(double)Math.round(location.getLongitude()*10000d)/10000d;

        Toast.makeText(this.getContext(), "Lat: "+ Double.toString(roundedLat) + "\n Lon: "+ Double.toString(roundedLon), Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == WHATSAPP_REQUEST_CODE) {
                Toast.makeText(getContext(), "shared successfully", Toast.LENGTH_LONG).show();
            }
        }

        if (resultCode == RESULT_CANCELED) {
            if (requestCode == WHATSAPP_REQUEST_CODE) {
                Toast.makeText(getContext(), "failed to share generated link", Toast.LENGTH_LONG).show();
            }
        }
    }




}
