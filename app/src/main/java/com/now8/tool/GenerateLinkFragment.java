package com.now8.tool;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class GenerateLinkFragment extends Fragment {


    public GenerateLinkFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Button generateLink = container.findViewById(R.id.linkgen);
        generateLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(),"heyyyy" ,Toast.LENGTH_SHORT).show();
            }
        });


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_generate_link, container, false);
    }

}
