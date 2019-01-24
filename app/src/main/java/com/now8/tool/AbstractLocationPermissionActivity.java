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
    abstract protected void onReady(Bundle state);

    private static final int REQUEST_REQUIRED_PERMISSIONS_FROM_USER =61125;
    private static final String STATE_IN_PERMISSION="inPermission";
    private boolean isPermissionRequestDialogInForeground =false;
    private Bundle generateRideFragmentState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.generateRideFragmentState = savedInstanceState;

        if (generateRideFragmentState != null) {
            isPermissionRequestDialogInForeground = generateRideFragmentState.getBoolean(STATE_IN_PERMISSION, false);
        }

        if (hasAllRequiredPermissions(getRequiredUserPermissions())) {
            onReady(generateRideFragmentState);
        }

        else if (!isPermissionRequestDialogInForeground) {
            isPermissionRequestDialogInForeground =true;


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

        isPermissionRequestDialogInForeground = false;

        if (requestCode== REQUEST_REQUIRED_PERMISSIONS_FROM_USER) {

            if (hasAllRequiredPermissions(getRequiredUserPermissions())) {
                onReady(generateRideFragmentState);
            }
            else {
                onUserDeniedRequiredPermissions();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IN_PERMISSION, isPermissionRequestDialogInForeground);
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
