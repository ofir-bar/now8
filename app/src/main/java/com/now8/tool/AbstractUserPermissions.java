package com.now8.tool;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;

// This abstract class deals with required user permissions, which are requested on runtime for Android 6.0 or API > 23
abstract public class AbstractUserPermissions
        extends FragmentActivity {

    private static final String TAG = "AbstractUserPermissions";

    abstract protected void onUserDeniedRequiredPermissions();
    abstract protected void onRequiredPermissionsGranted(Bundle state);

    private static final int REQUEST_REQUIRED_PERMISSIONS_FROM_USER = 61125;
    private static final String REQUEST_PERMISSIONS_DIALOG_KEY = null;
    private boolean isRequestPermissionDialogInForeground = false;
    private static final String[] REQUIRED_PERMISSIONS=
            {Manifest.permission.ACCESS_FINE_LOCATION};

    private Bundle createRideFragmentState;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG,"onSaveInstanceState");

        outState.putBoolean(REQUEST_PERMISSIONS_DIALOG_KEY, isRequestPermissionDialogInForeground);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);

        this.createRideFragmentState = savedInstanceState;

        if (createRideFragmentState != null) {
            isRequestPermissionDialogInForeground = createRideFragmentState.getBoolean(REQUEST_PERMISSIONS_DIALOG_KEY);
        }

        if (hasAllRequiredPermissions(REQUIRED_PERMISSIONS)) {
            onRequiredPermissionsGranted(createRideFragmentState);
        }

        else if (!isRequestPermissionDialogInForeground) {
            isRequestPermissionDialogInForeground =true;


            ActivityCompat
                    .requestPermissions(this,
                            getMissingRequiredPermissions(REQUIRED_PERMISSIONS),
                            REQUEST_REQUIRED_PERMISSIONS_FROM_USER);
        }

    }
    private String[] getMissingRequiredPermissions(String[] requiredPermissions) {
        Log.d(TAG,"getMissingRequiredPermissions");
        ArrayList<String> missingRequiredPermissions=new ArrayList<String>();

        for (String permission : requiredPermissions) {
            if (!hasPermission(permission)) {
                missingRequiredPermissions.add(permission);
            }
        }

        return(missingRequiredPermissions.toArray(new String[missingRequiredPermissions.size()]));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionsResult");
        isRequestPermissionDialogInForeground = false;

        if (requestCode== REQUEST_REQUIRED_PERMISSIONS_FROM_USER) {

            if (hasAllRequiredPermissions(REQUIRED_PERMISSIONS)) {
                onRequiredPermissionsGranted(createRideFragmentState);
            }
            else {
                onUserDeniedRequiredPermissions();
            }
        }
    }

    private boolean hasAllRequiredPermissions(String[] permissionsList) {
        Log.d(TAG,"hasAllRequiredPermissions");
        for (String requiredPermission : permissionsList) {
            if (!hasPermission(requiredPermission)) {
                return(false);
            }
        }
        return(true);
    }
    private boolean hasPermission(String userGrantedPermission) {
        Log.d(TAG,"hasPermission");
        return(ContextCompat.checkSelfPermission(this, userGrantedPermission) ==
                PackageManager.PERMISSION_GRANTED);
    }

}
