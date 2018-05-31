package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

public class CaptureSearchLongPress extends Activity{
    @Override
    protected void onResume(){
        super.onResume();
        if(!ServiceMain.isServiceMainRunning()){
            startService(new Intent(getBaseContext(),ServiceMain.class));
            new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"CaptureSearchLongPress>startService");
        }
        Intent i = new Intent(getPackageName()+C.BLOCK_SEARCH_LONG_PRESS);
        sendBroadcast(i);
    }

    @Override
    protected void onPause(){
        super.onPause();
        finish();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
