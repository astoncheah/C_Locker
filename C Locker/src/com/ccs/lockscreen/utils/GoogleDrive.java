package com.ccs.lockscreen.utils;

import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.ccs.lockscreen.myclocker.C;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * An abstract activity that handles authorization and connection to the Drive
 * services.
 */
public abstract class GoogleDrive implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    public static final int REQUEST_CODE_RESOLUTION = 9999;
    public static final int RESULT_OK = 1;
    public static final int RESULT_DELETE = 2;
    public static final int RESULT_ERROR = 3;
    private static final String TAG = "GoogleDriveActivity";
    private BaseActivity activity;
    private GoogleApiClient mGoogleApiClient;

    public GoogleDrive(BaseActivity activity){
        this.activity = activity;
    }

    public void connect(){
        if(mGoogleApiClient==null){
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        }
        mGoogleApiClient.connect();
    }

    public void disconnect(){
        if(mGoogleApiClient!=null){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint){

    }

    @Override
    public void onConnectionSuspended(int cause){
        activity.showToastLong("GoogleApiClient connection suspended: "+cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result){
        Log.e(TAG,"GoogleApiClient connection failed: "+result.toString());
        if(!result.hasResolution()){
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(activity,result.getErrorCode(),0).show();
            return;
        }
        try{
            result.startResolutionForResult(activity,REQUEST_CODE_RESOLUTION);
        }catch(SendIntentException e){
            activity.saveErrorLogs(TAG+">Error while starting resolution activity",e);
            activity.showToastLong("GoogleApiClient connection failed");
        }
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    public BaseActivity getActivity(){
        return activity;
    }

    public String getBackupDate(){
        DateFormat DATE_FORMAT = new SimpleDateFormat("MMMdd_yy_HH:mm:ss",Locale.getDefault());
        //DateFormat DATE = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
        //DateFormat TIME = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());
        String date = DATE_FORMAT.format(new Date());
        String name = "CL_Pro_";
        if(activity.getPackageName().equals(C.PKG_NAME_FREE)){
            name = "CL_Free_";
        }
        return name+date;
    }

    public String getBackupDir(){
        if(activity.getPackageName().equals(C.PKG_NAME_FREE)){
            return C.EXT_BACKUP_DIR_FREE;
        }
        return C.EXT_BACKUP_DIR_PRO;
    }

    public interface DriveListener{
        void onDriveResult(int result,String title,String details);

        void onDriveProgress(int totalSize,int progress,String msg);
    }

    public interface DriveListListener{
        void onListResult(int result,DriveId item);
    }
}