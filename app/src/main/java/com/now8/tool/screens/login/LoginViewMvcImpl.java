package com.now8.tool.screens.login;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.now8.tool.R;

import java.util.ArrayList;
import java.util.List;

public class LoginViewMvcImpl implements LoginViewMvc.Listener {

    private final View mRootView;

    public interface Listener{
        void onLoginClicked();
    }

    private Button login;

    List<Listener> mListeners = new ArrayList<>(1);

    public LoginViewMvcImpl(LayoutInflater inflater, @Nullable ViewGroup parent) {
        mRootView = (inflater.inflate(R.layout.login, parent, false));

        login = findViewById(R.id.loginButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    public void registerListener(Listener listener){
        mListeners.add(listener);
    }


    public void unregisterListener(Listener listener){
        mListeners.remove(listener);
    }


    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    @Override
    public void onLoginClicked() {

        for (Listener listener : mListeners) {
            listener.onLoginClicked();
        }

    }

    public View getRootView() {
        return mRootView;
    }




}
