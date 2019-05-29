package com.now8.tool.login;

public class LoginPresenter {

    LoginView mView;

    void attachView(LoginView view){
        mView = view;
    }

    void detachView(){
        mView = null;
    }

}
