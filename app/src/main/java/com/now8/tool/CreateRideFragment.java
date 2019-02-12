package com.now8.tool;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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

    private static final String SLACK_PACKAGE = "com.Slack";
    private static final int SLACK_REQUEST_CODE = 1;

    Button createRideButton;
    ImageButton changeUsername;
    TextView usernameTextView;
    String username;
    private Location deviceInitialLocation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");

        if(savedInstanceState != null){
            username = savedInstanceState.getString("username_state");
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"onCreateView");
        return inflater.inflate(R.layout.fragment_create_ride, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(username != null){
            outState.putString("username_state",username);
        }

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
        usernameTextView = getActivity().findViewById(R.id.username);

        if(username != null){
            usernameTextView.setText(username);
        }

        createRideButton = getActivity().findViewById(R.id.btn_create_ride);
        changeUsername = getActivity().findViewById(R.id.change_username);

        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText chooseUsername = new EditText(v.getContext());
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Choose your usernameTextView")
                        .setView(chooseUsername)
                        .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                username = chooseUsername.getText().toString();
                                usernameTextView.setText(username);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });
        createRideButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                retrofitNetworkRequestCreateRide(v.getContext(), clientNetworkRequest);
            }
        });



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult");

        if (requestCode == SLACK_REQUEST_CODE){

            if (resultCode == RESULT_OK){
                Toast.makeText(getContext(), R.string.info_share_ride_success, Toast.LENGTH_LONG).show();
            }
            else if (resultCode == RESULT_CANCELED){
                Toast.makeText(getContext(), R.string.err_share_ride_fail, Toast.LENGTH_LONG).show();
            }
        }

    }


    private void retrofitNetworkRequestCreateRide(Context context, WebServerInterface clientNetworkRequest){
        Log.d(TAG,"retrofitNetworkRequestCreateRide");
        clientNetworkRequest.createRide(usernameTextView.getText().toString(), "some fake location").enqueue(new retrofit2.Callback<Ride>() {
            @Override
            public void onResponse(retrofit2.Call<Ride> call, retrofit2.Response<Ride> responseRide) {
                Log.e(TAG, "Connection succeed");
                        shareRideToSlack(WEBSERVER_BASE_URL + responseRide.body().getUid());
            }

            @Override
            public void onFailure(retrofit2.Call<Ride> call, Throwable t) {
                Log.e(TAG, "Connection Failed", t);
            }
        });

    }

    private void shareRideToSlack(String rideUID) {

        Intent shareToSlack = new Intent(Intent.ACTION_SEND);
        shareToSlack.putExtra(Intent.EXTRA_TEXT, rideUID);
        shareToSlack.setPackage(SLACK_PACKAGE);
        shareToSlack.setType("text/plain");
        startActivityForResult(shareToSlack, SLACK_REQUEST_CODE);
    }

    private void printUserLocation(Location location) {
        double roundedLat=(double)Math.round(location.getLatitude()*10000d)/10000d;
        double roundedLon=(double)Math.round(location.getLongitude()*10000d)/10000d;
    }

    public Location getDeviceInitialLocation() {
        return deviceInitialLocation;
    }

    public void setDeviceInitialLocation(Location deviceInitialLocation) {
        this.deviceInitialLocation = deviceInitialLocation;
    }
}
