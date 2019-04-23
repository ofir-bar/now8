package com.now8.tool.screens.common;

import android.support.v7.app.AppCompatActivity;

import com.now8.tool.CustomApplication;
import com.now8.tool.dependency_injection.CompositionRoot;

public class BaseActivity extends AppCompatActivity {

    public String userAuthIdToken;

    protected CompositionRoot getCompositionRoot() {
        return ((CustomApplication) getApplication()).getCompositionRoot();
    }

}
