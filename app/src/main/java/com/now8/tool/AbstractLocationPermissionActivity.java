package com.now8.tool;


import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;


// This class deals with requested user permission on runtime, which are must-have for Android 6.0 or API > 23
abstract public class AbstractLocationPermissionActivity
        extends FragmentActivity {

    abstract protected String[] getRequiredUserPermissions();
    abstract protected void onUserDeniedRequiredPermissions();
    abstract protected void onRequiredPermissionsGranted(Bundle state);

    private static final int REQUEST_REQUIRED_PERMISSIONS_FROM_USER = 61125;

    private static final String REQUEST_PERMISSIONS_DIALOG_KEY = null;
    private boolean isRequestPermissionDialogInForeground = false;
    private Bundle createRideFragmentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.createRideFragmentState = savedInstanceState;

        if (createRideFragmentState != null) {
            isRequestPermissionDialogInForeground = createRideFragmentState.getBoolean(REQUEST_PERMISSIONS_DIALOG_KEY);
        }

        if (hasAllRequiredPermissions(getRequiredUserPermissions())) {
            onRequiredPermissionsGranted(createRideFragmentState);
        }

        else if (!isRequestPermissionDialogInForeground) {
            isRequestPermissionDialogInForeground =true;


            ActivityCompat
                    .requestPermissions(this,
                            getMissingRequiredPermissions(getRequiredUserPermissions()),
                            REQUEST_REQUIRED_PERMISSIONS_FROM_USER);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        isRequestPermissionDialogInForeground = false;

        if (requestCode== REQUEST_REQUIRED_PERMISSIONS_FROM_USER) {

            if (hasAllRequiredPermissions(getRequiredUserPermissions())) {
                onRequiredPermissionsGranted(createRideFragmentState);
            }
            else {
                onUserDeniedRequiredPermissions();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(REQUEST_PERMISSIONS_DIALOG_KEY, isRequestPermissionDialogInForeground);
    }

    private boolean hasAllRequiredPermissions(String[] permissionsList) {
        for (String requiredPermission : permissionsList) {
            if (!hasPermission(requiredPermission)) {
                return(false);
            }
        }
        return(true);
    }

    private boolean hasPermission(String userGrantedPermission) {
        return(ContextCompat.checkSelfPermission(this, userGrantedPermission) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private String[] getMissingRequiredPermissions(String[] requiredPermissions) {
        ArrayList<String> missingRequiredPermissions=new ArrayList<String>();

        for (String permission : requiredPermissions) {
            if (!hasPermission(permission)) {
                missingRequiredPermissions.add(permission);
            }
        }

        return(missingRequiredPermissions.toArray(new String[missingRequiredPermissions.size()]));
    }
}
