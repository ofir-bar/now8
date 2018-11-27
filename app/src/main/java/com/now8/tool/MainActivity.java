package com.now8.tool;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.device.yearclass.YearClass;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();

    }


    private void initializeUI(){

        //Log the device year for statistics
        int deviceYearClass = YearClass.get(getApplicationContext());
        Log.d(TAG,"Device year: " + String.valueOf(deviceYearClass) );


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GenerateLinkFragment generateLinkFragment = new GenerateLinkFragment();

        fragmentTransaction.add(R.id.main_activity_frame_layout, generateLinkFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}
