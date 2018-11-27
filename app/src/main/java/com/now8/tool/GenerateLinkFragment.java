package com.now8.tool;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


public class GenerateLinkFragment extends Fragment {
    private static final String TAG = "GenerateLinkFragment";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";

    //Request Codes
    private static final int WHATSAPP_REQUEST_CODE = 1;


    public GenerateLinkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_generate_link, container, false);

        Button generateLink = fragmentView.findViewById(R.id.linkgen);
        generateLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareGeneratedLinkToApp("THIS IS JUST SOME LINK", WHATSAPP_PACKAGE);
            }
        });

        // Inflate the layout for this fragment
        return fragmentView;
    }


    private void makeToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * method share the generated link to another app
     * @param generatedLink = the unique generated link which will be shared on external app
     * @param appPackage = the app which will be started
     */
    public void shareGeneratedLinkToApp(String generatedLink,String appPackage){

        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, generatedLink);
        whatsappIntent.setPackage(appPackage);
        whatsappIntent.setType("text/plain");
        startActivityForResult(whatsappIntent,WHATSAPP_REQUEST_CODE);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK) {
            if (requestCode == WHATSAPP_REQUEST_CODE) {
                makeToast(getContext(), "shared successfully");
            }
        }

        if (resultCode == RESULT_CANCELED){
            if(requestCode == WHATSAPP_REQUEST_CODE ){
                makeToast(getContext(), "failed to share generated link");
            }
        }
    }

}
