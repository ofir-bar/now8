package com.now8.tool.screens.login;

import android.app.Activity;
import android.view.View;

import java.util.List;

public interface LoginViewMvc{

    public interface Listener {
        void onLoginClicked();
    }

    void registerListener(Listener listener);

    void unregisterListener(Listener listener);

    View getRootView();

}
