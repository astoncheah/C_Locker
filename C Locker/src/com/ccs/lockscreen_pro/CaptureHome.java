package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;

public class CaptureHome extends Activity{
    private static CaptureHome instance = null;

    public static boolean isCaptureHomeRunning(){
        return instance!=null;
    }

    @Override
    protected void onResume(){
        super.onResume();
        new MyCLocker(this).writeToFile("CaptureHome>onResume: ");
        instance = this;
        if(!ServiceMain.isServiceMainRunning()){
            SaveData s = new SaveData(this);
            s.setOnReboot(true);
            s.setLockerRunning(false);
            startService(new Intent(getBaseContext(),ServiceMain.class));
            new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"CaptureHome>OnReboot");
        }
        final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final String pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
        if(pkgName.equals("")){
            Intent i = new Intent(this,ListInstalledApps.class);
            i.putExtra(C.LIST_APPS_ID,C.LIST_LAUNCHER_RUN);
            startActivity(i);
        }

        sendBroadcast(new Intent(getPackageName()+C.RUN_LAUNCHER));
        if(SettingsHomeKey.isSettingsHomeKey()){
            finish();
        }
        //if(Locker.isLockerRunning())finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        new MyCLocker(this).writeToFile("CaptureHome>onPause: ");
        instance = null;
        finish();
    }

    @Override
    protected void onDestroy(){
        new MyCLocker(this).writeToFile("CaptureHome>onDestroy: ");
        instance = null;
        super.onDestroy();
        //sendBroadcast(new Intent(getPackageName()+C.RUN_LAUNCHER));
    }
}