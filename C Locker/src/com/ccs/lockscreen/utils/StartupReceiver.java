package com.ccs.lockscreen.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen_pro.ServiceMain;

public class StartupReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context,Intent intent){
        try{
            final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            final boolean cBoxEnableLsService = prefs.getBoolean(P.BLN_ENABLE_SERVICE,false);

            new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"StartupReceiver>intent: "+intent.getAction());
            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
                if(!cBoxEnableLsService){
                    return;
                }
                if(ServiceMain.isServiceMainRunning()){
                    final Intent i = new Intent(context.getPackageName()+C.REMOVE_STARTUP_VIEW);
                    //i.putExtra("REMOVE_STARTUP_VIEW",3000);
                    context.sendBroadcast(i);
                    return;
                }
                new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"StartupReceiver>ACTION_BOOT_STARTED");
                SaveData s = new SaveData(context);
                s.setOnReboot(true);
                s.setLockerRunning(false);
                context.startService(new Intent(context,ServiceMain.class));
            }else if(intent.getAction().equals(Intent.ACTION_MY_PACKAGE_REPLACED)){
                if(!cBoxEnableLsService){
                    return;
                }
                if(ServiceMain.isServiceMainRunning()){
                    final Intent i = new Intent(context.getPackageName()+C.REMOVE_STARTUP_VIEW);
                    //i.putExtra("REMOVE_STARTUP_VIEW",3000);
                    context.sendBroadcast(i);
                    return;
                }
                new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"StartupReceiver>ACTION_MY_PACKAGE_REPLACED");
                SaveData s = new SaveData(context);
                //s.setOnReboot(true);
                s.setLockerRunning(false);
                context.startService(new Intent(context,ServiceMain.class));
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }
}