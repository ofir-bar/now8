package com.now8.tool.login;

public class LoginPresentor {

    LoginView mView;

    void attachView(LoginView view){
        mView = view;
    }

    void detachView(){
        mView = null;
    }

}
