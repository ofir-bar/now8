package com.now8.tool;
/*
This class is intended to deal with all the runtime permission requests.

getDesiredPermissions(), which returns the names of the permissions that the app wants
onReady(), which will be called once permission is granted by the user, and serves as an onCreate() substitute for the subclass
onPermissionDenied(), which will be called if the user declines granting the permission, so the subclass can do something (e.g., show a Toast, then finish() and go away)

 */

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;

abstract public class AbstractLocationPermissionActivity
        extends FragmentActivity {

    abstract protected String[] getDesiredPermissions();
    abstract protected void onPermissionDenied();
    abstract protected void onReady(Bundle state);

    private static final int REQUEST_PERMISSION=61125;
    private static final String STATE_IN_PERMISSION="inPermission";
    private boolean isPermissionRequestInProgress=false; // tracks whether or not we are in the middle of requesting permissions, to avoid asking multiply permission requests on screen rotate
    private Bundle state;


    // onCreate() will see if we have the desired permissions, and if not, it will call requestPermissions() to ask for those that we do not already hold
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.state=savedInstanceState;

        /*
        If we do not hold all of the desired permissions, but isInPermission is true,
        we skip requesting the permissions, since we are in the middle of doing so already.
         */
        if (state!=null) {
            isPermissionRequestInProgress=state.getBoolean(STATE_IN_PERMISSION, false);
        }

        if (hasAllPermissions(getDesiredPermissions())) {
            onReady(state);
        }
        else if (!isPermissionRequestInProgress) {
            isPermissionRequestInProgress=true;


            /* If we don't have all the permissions, we call requestPermissions() on ActivityCompat,
             using a netPermissions() method to identify those permissions that we do not already hold
             */
            ActivityCompat
                    .requestPermissions(this,
                            netPermissions(getDesiredPermissions()),
                            REQUEST_PERMISSION);
        }
    }

    // In onRequestPermissionResult(), depending on whether we now hold all the desired permissions, we call onReady() or onPermissionDenied():
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        isPermissionRequestInProgress=false;

        if (requestCode==REQUEST_PERMISSION) {

            // If we hold all of the permissions, we go ahead and call onReady(), so the activity can start its real work
            if (hasAllPermissions(getDesiredPermissions())) {
                onReady(state);
            }
            else {
                onPermissionDenied();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_IN_PERMISSION, isPermissionRequestInProgress);
    }


    // hasAllPermissions() iterates over the permission array from getDesiredPermissions() and returns true if we hold them all, false otherwise
    private boolean hasAllPermissions(String[] perms) {
        for (String perm : perms) {
            if (!hasPermission(perm)) {
                return(false);
            }
        }

        return(true);
    }

    private boolean hasPermission(String perm) {
        return(ContextCompat.checkSelfPermission(this, perm)==
                PackageManager.PERMISSION_GRANTED);
    }

    private String[] netPermissions(String[] wanted) {
        ArrayList<String> result=new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return(result.toArray(new String[result.size()]));
    }
}
