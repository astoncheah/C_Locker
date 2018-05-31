package com.ccs.lockscreen_pro;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.DownloadRSS;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen.security.GmailSender;
import com.ccs.lockscreen.security.SecurityUnlockView;
import com.ccs.lockscreen.utils.ComponentSetting;
import com.ccs.lockscreen.utils.DeviceAdminHandler;
import com.ccs.lockscreen.utils.MusicActions;
import com.ccs.lockscreen.utils.MyLocationManager;
import com.ccs.lockscreen.utils.MyLocationManager.LocationCallBack;
import com.ccs.lockscreen.utils.MySimpleUnlock;
import com.ccs.lockscreen.utils.MySimpleUnlock.MySimpleUnlockCallBack;
import com.ccs.lockscreen.utils.ProfileHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ServiceMain extends Service implements LocationCallBack{
    public static final String USABLE_DISPLAY_HEIGHT = "USABLE_DISPLAY_HEIGHT";
    private static final int UNLOCK_SOUND = 1, LOCK_SOUND = 2;//,NOTICE_SCREEN_TIMEOUT = 3000;
    //writeToFile(C.FILE_BACKUP_RECORD,"MyBackupData>copyFile: No file found");
    private static boolean instance = false;
    //=====
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private MyCLocker utils;
    private WindowManager windowManager;
    private ImageView blockStatusbarView;
    private ImageView blockNaviBarView;
    private RelativeLayout simpleUnlockView;
    private MySimpleUnlock simpleUnlock;
    private SecurityUnlockView securityUnlockView;
    private LinearLayout lytInactiveScreenOff;
    private MyUnexpectedUnlockDialog unexpectedUnlockDialog;
    private SaveData mSaveData;
    private MyLocationManager mLocation;
    private boolean isFirstTimeRunning;
    private boolean isScreenOn = true;
    private boolean isInCalling = false;
    private boolean hasCallAnswered = true;
    private boolean isCallFromScreenOff = false;//isProximityOn = false,
    private boolean isCheckingLocation = false;
    private boolean cBoxSmartWifiUnlock;
    private boolean cBoxSmartBluetoothUnlock;
    private boolean cBoxSmartLocationUnlock;
    private boolean isTrustedWifiConnected;
    private boolean isTrustedBtConnected;
    private boolean isTrustedLocation;
    private boolean cBoxProfileWifi;
    private boolean cBoxProfileBluetooth;
    private boolean cBoxBlockStatusbar;
    private boolean cBoxInactiveScreenOff;
    private boolean cBoxEnableProximitySensor;
    private boolean cBoxVolumeControlScreenOff;
    private boolean cBoxNoLockShortcut;
    private boolean cBoxLockSound;
    private boolean cBoxUnlockSound;
    private boolean cBoxShowRSSUpdateNotice;
    private int intRunnerCount, intLockStyle, intDelayLock, lockSoundType, unlockSoundType, intPrevVolume, intSpnRssUpdateInterval;
    private String //tasker
        strTaskerUnlock, strTaskerUnPIN, strRingtoneUri1, strRingtoneUri2;
    private int currentScreenTimeOutVal;//,intDefaultScreenTimeOutVal;
    private int STARTING_RAW_X, STARTING_RAW_Y, GESTURE_LENGTH;
    private Handler handler = new Handler();
    private MusicActions musicAction;
    private List<String> wifiSavedlist = new ArrayList<>();
    private List<String> bluetoothSavedlist = new ArrayList<>();
    private Uri uriRingtone;

    private BroadcastReceiver mainReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            final String action = intent.getAction();
            try{
                onSystemReceiver(intent);
                if(action.equals(getPackageName()+C.RSS_FEED)){
                    if(C.isInternetConnected(context)){
                        new DownloadRSS(context,false).execute();
                    }else{
                        msg(cBoxShowRSSUpdateNotice,getString(R.string.network_error));
                    }
                }else if(action.equals(getPackageName()+C.DELAY_LOCKER)){
                    if(!isScreenOn){
                        launchLocker("DELAY_LOCKER",true,true,0);
                    }
                }else if(action.equals(getPackageName()+C.PLAY_SOUND)){
                    playSound(cBoxUnlockSound,UNLOCK_SOUND);
                }else if(action.equals(getPackageName()+C.UNEXPECTED_UNLOCK)){
                    handleUnexpectedUnlock(true);
                }else if(action.equals(getPackageName()+C.RUN_LAUNCHER)){
                    handleHomeKey();
                }else if(action.equals(getPackageName()+C.BLOCK_SEARCH_LONG_PRESS)){
                    handleSearchLongPressKey();
                }else if(action.equals(getPackageName()+C.BLOCK_SYSTEM_SETTINGS)){
                    handleBlockSystemSettings();
                }else if(action.equals(getPackageName()+C.RUN_SETTINGS_NOTI)){
                    handleSettingsNotice();
                }else if(action.equals(getPackageName()+C.RUN_BLOCK_APP)){
                    if(C.isAccessibilityEnabled(context)){
                        return;
                    }
                    intRunnerCount = 0;
                    handler.removeCallbacks(blockApp);
                    handler.post(blockApp);
                }else if(action.equals(getPackageName()+C.STOP_BLOCK_APP)){
                    if(C.isAccessibilityEnabled(context)){
                        return;
                    }
                    intRunnerCount = 0;
                    handler.removeCallbacks(blockApp);
                }else if(action.equals(getPackageName()+C.TASKER+strTaskerUnlock)){
                    if(!strTaskerUnlock.equals("")){
                        mSaveData.setKillLocker(true);
                        sendBroadcast(new Intent(getPackageName()+C.KILL_LOCKER));
                    }
                }else if(action.equals(getPackageName()+C.TASKER+strTaskerUnPIN)){
                    //
                }else if(action.equals(getPackageName()+C.ON_LOCKER_RESUME)){
                    handleTopView(true);
                    handleBottomView(true);
                    handleUnexpectedUnlock(false);
                }else if(action.equals(getPackageName()+C.ON_LOCKER_PAUSE)){
                    handleTopView(false);
                    handleBottomView(false);
                }else if(action.equals(getPackageName()+C.ON_LOCKER_START)){
                    //handleTopView(true);
                    //handleBottomView(true);
                }else if(action.equals(getPackageName()+C.ON_LOCKER_STOP)){
                    //handleTopView(false);
                    //handleBottomView(false);
                }else if(action.equals(getPackageName()+C.OPEN_PIN)){
                    handlePinUnlock(true,false);//no need to relaunch locker
                }else if(action.equals(getPackageName()+C.CLOSE_PIN)){
                    if(securityUnlockView!=null){//view already removed by window manager
                        securityUnlockView = null;
                    }
                    //handlePinUnlock(false);
                }else if(action.equals(getPackageName()+C.CANCEL_SCREEN_OFF)){
                    handleInactiveScreenOffWindow(false);
                }else if(action.equals(getPackageName()+C.LAUNCH_LOCKER)){
                    launchLocker("LAUNCH_LOCKER",false,false,"onBackPressed",0);
                }else if(action.equals(getPackageName()+C.EMERGENCY_CALL)){
                    //launchLocker("LAUNCH_LOCKER",false,false,"onBackPressed",0);

                    Intent i = new Intent(C.EMERGENCY_CALL);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }else if(action.equals(C.DISABLE_LOCKER_SEND)){
                    if(getPackageName().equals(C.PKG_NAME_FREE)){
                        //disable home category
                        utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>DISABLE_LOCKER_SEND");
                        new ComponentSetting(context).setAllComponentSettings(false,C.DISABLE_LOCKER_SEND);

                        mSaveData.setKillLocker(true);//kill locker if it's running
                        editor.putBoolean(P.BLN_ENABLE_SERVICE,false);
                        editor.commit();
                        stopSelf();
                    }
                }else if(action.equals(C.DISABLE_BOTH_LOCKER_SEND)){
                    utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>DISABLE_LOCKER_SEND");
                    new ComponentSetting(context).setAllComponentSettings(false,C.DISABLE_LOCKER_SEND);

                    mSaveData.setKillLocker(true);//kill locker if it's running
                    editor.putBoolean(P.BLN_ENABLE_SERVICE,false);
                    editor.commit();
                    stopSelf();
                }else if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                    onSystemDialog(intent);
                }
            }catch(Exception e){
                utils.saveErrorLog(null,e);
            }
        }
    };
    private BroadcastReceiver smsReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            final String action = intent.getAction();
            if(action.equals(C.SMS_RECEIVED)){
                try{
                    final Bundle extras = intent.getExtras();
                    if(extras!=null){
                        Object[] smsextras = (Object[])extras.get("pdus");
                        assert smsextras!=null;
                        if(smsextras.length>0){
                            String strMsgBody = "";
                            for(Object smsextra : smsextras){
                                @SuppressWarnings("deprecation") SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextra);
                                strMsgBody = smsmsg.getMessageBody();
                            }
                            //Log.e("info","strMsgBody: "+strMsgBody);
                            //String strIncomingNumber = smsmsg.getOriginatingAddress();
                            if(strMsgBody.equalsIgnoreCase("cLockerUnlock")){
                                System.exit(0);
                            }else if(strMsgBody.equalsIgnoreCase("resetHomeLauncher")){
                                new ComponentSetting(context).setHomeLauncher(false,"resetHomeLauncher");

                                final Intent i = new Intent(Intent.ACTION_MAIN);
                                i.addCategory(Intent.CATEGORY_HOME);
                                startActivity(i);
                            }else if(!strMsgBody.equalsIgnoreCase("")){
                                final String unlockPass = prefs.getString("emergencyUnlock","cLockerUnlock");
                                if(strMsgBody.equalsIgnoreCase(unlockPass)){
                                    System.exit(0);
                                }
                            }
                            if(mSaveData.isLockerRunning()){
                                if(isScreenOn){
                                    //Intent i = new Intent(getPackageName()+SMS_UPDATE);
                                    //sendBroadcast(i);
                                }else{
                                    //mSaveData.setTurnScreenOff(true);
                                    //mSaveData.setScreenTimeoutNotice(NOTICE_SCREEN_TIMEOUT);//default 2000
                                }
                            }
                        }
                    }
                }catch(Exception e){
                    utils.saveErrorLog(null,e);
                }
            }
        }
    };
    private BroadcastReceiver mmsReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            final String action = intent.getAction();
            final String type = intent.getType();
            if(action.equals(C.WAP_PUSH_RECEIVED_ACTION) && type.equals(C.MMS_DATA_TYPE)){
                try{
                    final Bundle extras = intent.getExtras();
                    if(extras!=null && mSaveData.isLockerRunning()){
                        byte[] buffer = extras.getByteArray("data");
                        assert buffer!=null;
                        String strIncomingNumber = new String(buffer);
                        int indx = strIncomingNumber.indexOf("/TYPE");
                        if(indx>0 && (indx-15)>0){
                            int newIndx = indx-15;
                            strIncomingNumber = strIncomingNumber.substring(newIndx,indx);
                            indx = strIncomingNumber.indexOf("+");
                            if(indx>0){
                                //strIncomingNumber = strIncomingNumber.substring(indx);
                            }
                            if(isScreenOn){
                                //Intent i = new Intent(getPackageName()+SMS_UPDATE);
                                //sendBroadcast(i);
                            }else{
                                //mSaveData.setTurnScreenOff(true);
                                //mSaveData.setScreenTimeoutNotice(NOTICE_SCREEN_TIMEOUT);
                            }
                        }
                    }
                }catch(Exception e){
                    utils.saveErrorLog(null,e);
                }
            }
        }
    };
    private BroadcastReceiver callReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            final String callAction = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            try{
                if(callAction!=null){
                    if(callAction.equals(TelephonyManager.EXTRA_STATE_RINGING)){//Phone ringing....
                        isInCalling = true;
                        hasCallAnswered = false;
                        isCallFromScreenOff = !isScreenOn;
                    }else if(callAction.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){//Answering....
                        isInCalling = true;
                        hasCallAnswered = true;
                    }else if(callAction.equals(TelephonyManager.EXTRA_STATE_IDLE)){//Finishing....
                        if(!hasCallAnswered && mSaveData.isLockerRunning()){
                            if(isCallFromScreenOff){
                                //mSaveData.setTurnScreenOff(true);
                                //mSaveData.setScreenTimeoutNotice(NOTICE_SCREEN_TIMEOUT);
                            }
                        }
                        isInCalling = false;
                    }
                }
            }catch(Exception e){
                utils.saveErrorLog(null,e);
            }
        }
    };
    private SensorEventListener sensorListener = new SensorEventListener(){
        boolean isProxiOn = false;
        boolean isLightLow = false;

        @Override
        public void onSensorChanged(final SensorEvent event){
            switch(event.sensor.getType()){
                case Sensor.TYPE_PROXIMITY:
                    float f = event.values[0];//values[0]: Proximity sensor distance measured in centimeters
                    //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>TYPE_PROXIMITY: "+f);
                    //showToastMsg(ServiceMain.this,true,"ServiceMain>TYPE_PROXIMITY: "+f);
                    if(f<1.0f){
                        isProxiOn = true;
                        setTurnScreenOff(true,2000,"TYPE_PROXIMITY");
                    }else{
                        isProxiOn = false;
                        if(!isLightLow){
                            setTurnScreenOff(false,0,"TYPE_LIGHT");
                        }
                    }
                    break;
                case Sensor.TYPE_LIGHT:
                    float l = event.values[0];//values[0]: Ambient light level in SI lux units
                    //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>TYPE_LIGHT: "+l);
                    //showToastMsg(ServiceMain.this,true,"ServiceMain>TYPE_LIGHT: "+l);
                    if(l==0.0f){
                        isLightLow = true;
                        //setTurnScreenOff(true,2000,"TYPE_LIGHT");
                    }else{
                        isLightLow = false;
                        if(!isProxiOn){
                            //setTurnScreenOff(false,0,"TYPE_LIGHT");
                        }
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor arg0,int arg1){
        }
    };
    private ContentObserver volumeLevelObserver = new ContentObserver(new Handler()){
        @Override
        public boolean deliverSelfNotifications(){
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            final AudioManager myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            final int intCurrentVolume = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            final int intMaxVolume = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            final int delta = intCurrentVolume-intPrevVolume;
            if(delta>0){
                if(delta==1){
                    if(intCurrentVolume==intMaxVolume){
                        intPrevVolume = intMaxVolume-2;
                        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    }else if(intCurrentVolume==(intMaxVolume-1)){
                        intPrevVolume = intMaxVolume-1;
                        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    }else{
                        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    }
                    musicAction.musicNext();
                    setVibration(true,100);
                }else{
                    if(intCurrentVolume==intMaxVolume){
                        intPrevVolume = intMaxVolume-2;
                        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    }else if(intCurrentVolume==(intMaxVolume-1)){
                        intPrevVolume = intMaxVolume-1;
                        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    }else{
                        intPrevVolume = (intCurrentVolume-1);
                        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    }
                }
            }else if(delta<0){
                if(delta==-1 && intCurrentVolume==0){
                    intPrevVolume = intCurrentVolume;
                    myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                }else if(delta==-1){
                    myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                    musicAction.musicBack();
                    setVibration(true,100);
                }else{
                    intPrevVolume = (intCurrentVolume+1);
                    myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intPrevVolume,0);
                }
            }
        }
    };
    private ContentObserver screenTimeoutObserver = new ContentObserver(new Handler()){
        @Override
        public boolean deliverSelfNotifications(){
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange){
            super.onChange(selfChange);
            try{
                final int screenTimeOutVal = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT);
                if(currentScreenTimeOutVal!=screenTimeOutVal){
                    currentScreenTimeOutVal = screenTimeOutVal;
                    setScreenTimeOutValue(currentScreenTimeOutVal);
                }
            }catch(SettingNotFoundException e){
                utils.saveErrorLog(null,e);
                mSaveData.setScreenTimeOutVal(C.DEFAULT_SCREEN_TIMEOUT);
            }
        }
    };
    //
    private Runnable turnScreenOff = new Runnable(){
        @Override
        public void run(){
            ComponentName cn = C.getRunningTask(ServiceMain.this,0);
            String pkgName;
            if(cn!=null){
                pkgName = cn.getPackageName();
                if(pkgName.equalsIgnoreCase(C.PHONE_APK) ||        // dont block phone.apk "com.android.phone/com.android.phone.InCallScreen"
                    pkgName.equalsIgnoreCase(C.ALARM_APK_SAM) ||    // dont block alarm.apk "com.sec.android.app.clockpackage/com.sec.android.app.clockpackage.alarm.AlarmAlert"
                    pkgName.equalsIgnoreCase(C.ALARM_APK_LG)){ //class name go sms pop up
                    return;
                }
            }
            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>turnScreenOff");
            final DeviceAdminHandler deviceAdminHandler = new DeviceAdminHandler(ServiceMain.this);
            deviceAdminHandler.turnScreenOff();
        }
    };
    private Runnable turnInactiveScreenOff = new Runnable(){
        @Override
        public void run(){
            ComponentName cn = C.getRunningTask(ServiceMain.this,0);
            String pkgName;
            if(cn!=null){
                pkgName = cn.getPackageName();
                if(pkgName.equalsIgnoreCase(C.PHONE_APK) ||
                    pkgName.equalsIgnoreCase(C.CAMERA) ||
                    pkgName.equalsIgnoreCase(C.ALARM_APK_SAM) ||
                    pkgName.equalsIgnoreCase(C.ALARM_APK_LG)){
                    return;
                }
            }
            //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>turnInactiveScreenOff");
            final DeviceAdminHandler deviceAdminHandler = new DeviceAdminHandler(ServiceMain.this);
            deviceAdminHandler.turnScreenOff();
        }
    };
    private Runnable runLauncher = new Runnable(){
        @Override
        public void run(){
            try{
                final String pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
                final String className = prefs.getString(P.STR_HOME_LAUNCHER_CLASS_NAME,"");
                final Intent i = getAppIntent(pkgName,className);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
            }catch(Exception e){
                utils.saveErrorLog(null,e);
            }
        }
    };
    private Runnable blockApp = new Runnable(){
        @Override
        public void run(){
            try{
                ComponentName cn = C.getRunningTask(ServiceMain.this,0);
                String pkgName;
                String className;
                if(cn!=null){
                    pkgName = cn.getPackageName();
                    className = cn.getClassName();
                    boolean cBoxEnablePassUnlock = prefs.getBoolean("cBoxEnablePassUnlock",false);

                    if(pkgName.equalsIgnoreCase(getPackageName()) ||
                        pkgName.equalsIgnoreCase(C.PHONE_APK) ||        // dont block phone.apk "com.android.phone/com.android.phone.InCallScreen"
                        pkgName.equalsIgnoreCase(C.ALARM_APK_SAM) ||    // dont block alarm.apk "com.sec.android.app.clockpackage/com.sec.android.app.clockpackage.alarm.AlarmAlert"
                        pkgName.equalsIgnoreCase(C.ALARM_APK_LG) ||    // dont block alarm.apk "com.lge.clock/com.lge.clock.alarmclock.AlarmAlert"
                        className.equalsIgnoreCase(C.EMERGENCY_CALL) ||
                        className.equalsIgnoreCase(C.GO_SMS_POP_UP)){ //class name go sms pop up
                    }else if(className.equalsIgnoreCase(C.RECENT_APPS_CLASS) ||
                        className.equalsIgnoreCase(C.RECENT_APPS_CLASS_V2) ||//samsung 4.4 version
                        className.equalsIgnoreCase(C.RECENT_APPS_CLASS_V3)){ //lollipop
                        if(cBoxEnablePassUnlock){
                            handlePinUnlock(true,true);
                        }else{
                            launchLocker("blockApp>RECENT_APPS_CLASS",false,false,0);
                        }
                    }else if(className.equalsIgnoreCase(C.RESOLVER_CLASS)){
                        if(cBoxEnablePassUnlock){
                            handlePinUnlock(true,true);
                        }else{
                            launchLocker("blockApp>RESOLVER_CLASS",false,false,0);
                        }
                    }else if(pkgName.equalsIgnoreCase("com.oasisfeng.greenify")){//dont block this
                        return;
                    }else if(!pkgName.equalsIgnoreCase(getPackageName())){
                        if(!isBlockApp(pkgName)){
                            return;
                        }
                        if(cBoxEnablePassUnlock){
                            handlePinUnlock(true,true);
                        }else{
                            launchLocker("blockApp>isBlockApp",false,false,0);
                        }
                    }
                    utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>blockApp: ("+intRunnerCount+")"+pkgName+"/"+className);
                    //launchLocker("blockApp",false,false);
                    if(intRunnerCount<4){//run 5 times
                        intRunnerCount++;
                        handler.postDelayed(blockApp,100); //100 stable
                    }else{
                        intRunnerCount = 0;
                        handler.removeCallbacks(blockApp);
                    }
                    //handlePinUnlock(true);
                }
            }catch(Exception e){
                utils.saveErrorLog(null,e);
            }
        }
    };

    public static boolean isServiceMainRunning(){
        return instance;
    }

    private void onSystemReceiver(final Intent intent){
        if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            if(cBoxSmartWifiUnlock && isTrustedWifiConnected){
                msg(false,"Smart Unlock: Trusted Wifi");
            }else if(cBoxSmartBluetoothUnlock && isTrustedBtConnected){
                msg(false,"Smart Unlock: Trusted Bluetooth");
            }else if(cBoxSmartLocationUnlock && isTrustedLocation){
                msg(false,"Smart Unlock: Trusted Location");
            }
            isScreenOn = true;
            loadVolumeKeyObserver(true,false);
            if(!isInCalling){
                loadProxiSensorReceiver(true);
                handleInactiveScreenOffWindow(true);
                sendBroadcast(new Intent(getPackageName()+C.LOCKERSCREEN_ON));
                checkSecurityEmailPending();
                checkLocation(1000*60*5);//5 mins
            }
        }else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            isScreenOn = false;
            handlePinUnlock(false,false);
            if(!isInCalling){
                if(!isVoiceMailActive()){
                    if(cBoxNoLockShortcut){
                        if(!mSaveData.isLockerRunning()){
                            if(intDelayLock==0){
                                launchLocker(null,true,true,0);
                            }else{
                                cancelAlarm(pendingIntent(C.DELAY_LOCKER));
                                setAlarmWakeup(intDelayLock,C.DELAY_LOCKER);
                            }
                        }
                    }else{
                        if(intDelayLock==0){
                            launchLocker(null,true,true,0);
                        }else{
                            cancelAlarm(pendingIntent(C.DELAY_LOCKER));
                            setAlarmWakeup(intDelayLock,C.DELAY_LOCKER);
                        }
                    }
                }
            }
            playSound(cBoxLockSound,LOCK_SOUND);
            loadProxiSensorReceiver(false);
            loadVolumeKeyObserver(cBoxVolumeControlScreenOff,true);
            handleInactiveScreenOffWindow(false);
            checkLocation(1000*60*15);//15 mins
        }else if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){
            if(intent.hasExtra("state")){
                if(mSaveData.isHeadsetConnected() && intent.getIntExtra("state",0)==0){
                    mSaveData.setHeadsetConnected(false);
                }else if(!mSaveData.isHeadsetConnected() && intent.getIntExtra("state",0)==1){
                    mSaveData.setHeadsetConnected(true);
                }
                sendBroadcast(new Intent(getPackageName()+C.HEADSET_UPDATE));
            }
        }else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            handleWifiProfile(intent);
        }else if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
            handleBluetoothProfile(true,intent);
        }else if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            handleBluetoothProfile(false,intent);
        }
    }
    private void onSystemDialog(Intent intent){
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"; //Long Press Home
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"; //Home Button
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions"; //Power Button
        try{
            if(!mSaveData.isLockerRunning()){
                return;
            }
            if(intent.hasExtra(SYSTEM_DIALOG_REASON_KEY)){
                final String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                switch(reason){
                    case SYSTEM_DIALOG_REASON_RECENT_APPS:
                        if(C.isAccessibilityEnabled(this)){
                            return;
                        }
                        utils.writeToFile("ServiceMain>onReceiveSystemDialog: SYSTEM_DIALOG_REASON_RECENT_APPS");
                        //isRecentAppsClick = true;
                        if(prefs.getBoolean("cBoxBlockRecentApps",false)){
                            utils.writeToFile("ServiceMain>handleBlockedApps: cBoxBlockRecentApps");
                            boolean cBoxEnablePassUnlock = prefs.getBoolean("cBoxEnablePassUnlock",false);
                            if(cBoxEnablePassUnlock){
                                if(securityUnlockView==null){
                                    handlePinUnlock(true,true);
                                }else{
                                    launchLocker("onReceiveSystemDialog>SYSTEM_DIALOG_REASON_KEY",false,false,500);
                                }
                            }else{
                                launchLocker("onReceiveSystemDialog>SYSTEM_DIALOG_REASON_KEY",false,false,500);
                            }
                        }else{
                            //phone rotation
                        }
                        break;
                    case SYSTEM_DIALOG_REASON_HOME_KEY:
                    /*ComponentName cn = C.getRunningTask5v(ServiceMain.this,"ServiceMain>blockApp");
                    String pkgName = null;
	            	String className = null;
	                if(cn != null) {
	                	pkgName = cn.getPackageName();
	                	className = cn.getClassName();
	                }
	                Log.e("","SYSTEM_DIALOG_REASON_HOME_KEY 1: "+pkgName+"/"+className);

					pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
					className = prefs.getString(P.STR_HOME_LAUNCHER_CLASS_NAME,"");

					Log.e("","SYSTEM_DIALOG_REASON_HOME_KEY 2: "+pkgName+"/"+className);*/
                        break;
                    case SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS:
                        break;
                }
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void handlePinUnlock(boolean show,boolean relaunchLocker){
        checkWindowManager();
        if(show){
            if(C.isAndroid(Build.VERSION_CODES.O)){
                return;
            }
            if(securityUnlockView==null){
                int securityType = prefs.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_NONE);
                securityUnlockView = new SecurityUnlockView(this,securityType,relaunchLocker);
                securityUnlockView.setOnTouchListener(new OnTouchListener(){
                    @Override
                    public boolean onTouch(View v,MotionEvent event){
                        return true;
                    }
                });
                addOverlayWindow(securityUnlockView,prOverlayView(securityUnlockView));
            }
        }else{
            if(securityUnlockView!=null){
                removeOverlayWindow(securityUnlockView);
                securityUnlockView = null;
            }
        }
    }

    //overlay view
    private void checkWindowManager(){
        if(windowManager==null){
            windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("RtlHardcoded")
    private WindowManager.LayoutParams prOverlayView(View view){
        int what = WindowManager.LayoutParams.FLAG_FULLSCREEN |//not really useful
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |//on top of status bar
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |//prevent wrong y position on touch not
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//nothing, just easy to test

        if(C.isAndroid(Build.VERSION_CODES.KITKAT)){
            what = what |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION |//not really useful
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;//not really useful
        }

        WindowManager.LayoutParams windowPr = new WindowManager.LayoutParams();
        int w = C.getRealDisplaySize(this).x;
        int h = C.getRealDisplaySize(this).y;
        int gravity;
        if(view==blockStatusbarView){
            h = C.dpToPx(this,30);
            gravity = Gravity.TOP;
        }else if(view==blockNaviBarView){
            final int naviKeysH = C.getNaviKeyHeight(this);
            if(C.isScreenPortrait(getBaseContext())){
                gravity = Gravity.BOTTOM;
                if(naviKeysH<h/2){
                    h = naviKeysH;
                }else{
                    h = (int)(h*0.2);
                }
            }else{
                gravity = Gravity.RIGHT;
                if(naviKeysH<w/2){
                    w = naviKeysH;
                }else{
                    w = (int)(w*0.2);
                }
            }
        }else if(view==simpleUnlockView){
            int viewH = C.dpToPx(getBaseContext(),MySimpleUnlock.MAIN_HEIGHT);
            int iconMargin = C.dpToPx(getBaseContext(),MySimpleUnlock.MAIN_HEIGHT/4);
            if(C.isScreenPortrait(getBaseContext())){
                gravity = Gravity.BOTTOM;
                h = viewH-iconMargin;
            }else{
                gravity = Gravity.RIGHT;
                w = viewH-iconMargin;
            }
        }else if(view==securityUnlockView){
            h = WindowManager.LayoutParams.MATCH_PARENT;
            gravity = Gravity.TOP;
            //windowPr.dimAmount=0.0f;
        }else{
            //w = (int) (w*0.8);
            //h = WindowManager.LayoutParams.WRAP_CONTENT;
            //gravity = Gravity.CENTER;
            h = WindowManager.LayoutParams.MATCH_PARENT;
            gravity = Gravity.TOP;
        }

        windowPr.format = PixelFormat.TRANSLUCENT;
        //windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        if(C.isAndroid(Build.VERSION_CODES.O)){
            windowPr.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// overlay status bar
        }else{
            windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// overlay status bar
        }
        windowPr.width = w;
        windowPr.height = h;
        windowPr.flags = what;
        windowPr.gravity = gravity;

        return windowPr;
    }
    @SuppressLint("RtlHardcoded")
    private WindowManager.LayoutParams prInactiveScreenOff(){
        int what = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |//on top of status bar
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |//prevent wrong y position on touch not
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;// |
        //0;//nothing, just easy to test

        WindowManager.LayoutParams windowPr;
        windowPr = new WindowManager.LayoutParams();
        windowPr.format = PixelFormat.RGBA_8888;
        if(C.isAndroid(Build.VERSION_CODES.O)){
            windowPr.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// overlay status bar
        }else{
            windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// overlay status bar
        }
        windowPr.width = 1;
        windowPr.height = 1;
        windowPr.flags = what;
        windowPr.gravity = Gravity.LEFT | Gravity.TOP;
        windowPr.x = 0;
        windowPr.y = 0;
        return windowPr;
    }
    private void handleTopView(boolean show){
        checkWindowManager();
        if(show){
            if(cBoxBlockStatusbar){
                if(blockStatusbarView==null){
                    blockStatusbarView = new ImageView(this);
                    //blockStatusbarView.setBackgroundColor(Color.RED);
                    blockStatusbarView.setOnTouchListener(new OnTouchListener(){
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v,MotionEvent event){
                            return true;
                        }
                    });
                    addOverlayWindow(blockStatusbarView,prOverlayView(blockStatusbarView));
                }
            }
        }else{
            if(blockStatusbarView!=null){
                removeOverlayWindow(blockStatusbarView);
                blockStatusbarView = null;
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void handleBottomView(boolean show){
        int naviKeysH = C.getNaviKeyHeight(this);
        if(naviKeysH==0 || C.isAndroid(Build.VERSION_CODES.O)){
            return;
        }
        if(prefs.getBoolean("cBoxEnableKeyboardTyping",false)){
            return;
        }
        checkWindowManager();
        if(intLockStyle==C.SIMPLE_UNLOCK){
            if(show){
                if(simpleUnlockView==null){
                    simpleUnlockView = new RelativeLayout(this);
                    //simpleUnlockView.setBackgroundColor(Color.BLACK);
                    if(simpleUnlock==null){
                        simpleUnlock = new MySimpleUnlock(this,simpleUnlockView,new MySimpleUnlockCallBack(){
                            @Override
                            public void callLockerAction(int actionType){
                                Intent i = new Intent(getPackageName()+C.SIMPLE_UNLOCK_CALL);
                                i.putExtra(C.SIMPLE_UNLOCK_CALL,actionType);
                                sendBroadcast(i);
                            }
                        });
                    }
                    addOverlayWindow(simpleUnlockView,prOverlayView(simpleUnlockView));
                }
            }else{
                if(simpleUnlockView!=null){
                    removeOverlayWindow(simpleUnlockView);
                    simpleUnlockView = null;
                    simpleUnlock = null;
                }
            }
        }else{
            if(show){
                if(blockNaviBarView==null){
                    blockNaviBarView = new ImageView(this);
                    //blockNaviBarView.setBackgroundColor(Color.RED);
                    blockNaviBarView.setOnTouchListener(new OnTouchListener(){
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v,MotionEvent m){
                            if(C.isScreenPortrait(getBaseContext())){
                                switch(m.getActionMasked()){
                                    case MotionEvent.ACTION_DOWN:
                                        STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                                        STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        int fltX = (int)(m.getRawX()-STARTING_RAW_X);
                                        int fltY = (int)(m.getRawY()-STARTING_RAW_Y);
                                        Intent i = new Intent(getPackageName()+C.BOTTOM_BAR_CALL);
                                        if(fltY<-GESTURE_LENGTH){
                                            i.putExtra(C.BOTTOM_BAR_CALL,C.BOTTOM_TOUCH_TOP);
                                            sendBroadcast(i);
                                        }else if(fltX<-GESTURE_LENGTH){
                                            i.putExtra(C.BOTTOM_BAR_CALL,C.BOTTOM_TOUCH_LEFT);
                                            sendBroadcast(i);
                                        }else if(fltX>GESTURE_LENGTH){
                                            i.putExtra(C.BOTTOM_BAR_CALL,C.BOTTOM_TOUCH_RIGHT);
                                            sendBroadcast(i);
                                        }else{
                                            i.putExtra(C.BOTTOM_BAR_CALL,C.BOTTOM_TOUCH_TAP);
                                            sendBroadcast(i);
                                        }
                                        break;
                                }
                            }
                            return true;
                        }
                    });
                    addOverlayWindow(blockNaviBarView,prOverlayView(blockNaviBarView));
                }
            }else{
                if(blockNaviBarView!=null){
                    removeOverlayWindow(blockNaviBarView);
                    blockNaviBarView = null;
                }
            }
        }
    }
    private void addOverlayWindow(View view, ViewGroup.LayoutParams params){
        if(!checkSystemWindowPermission()){
            //Toast.makeText(this,R.string.system_window_permission_need,Toast.LENGTH_LONG).show();
            //requestSystemWindowPermission();
            editor.putBoolean("cBoxEnableKeyboardTyping",true);
            editor.commit();
            return;
        }
        windowManager.addView(view,params);
    }
    private void removeOverlayWindow(View view){
        if(view==null){
            return;
        }
        windowManager.removeView(view);
    }
    //
    private void launchLocker(final String name,final boolean resetTime,final boolean clearTask,int delayTime){
        launchLocker(name,resetTime,clearTask,null,delayTime);
    }
    private void launchLocker(final String name,final boolean resetTime,final boolean clearTask,final String extra,int delayTime){
        if(cBoxSmartWifiUnlock && isTrustedWifiConnected){
            //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>launchLocker: isTrustedWifiConnected"+name);
            return;
        }
        if(cBoxSmartBluetoothUnlock && isTrustedBtConnected){
            //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>launchLocker: isTrustedBtConnected"+name);
            return;
        }
        if(cBoxSmartLocationUnlock && isTrustedLocation){
            //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>launchLocker: isTrustedLocation"+name);
            return;
        }
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){//use handler will prevent locker double launch
                if(resetTime && !mSaveData.isLockerRunning()){
                    mSaveData.setLockerActivationTime(System.currentTimeMillis());
                }

                final Intent i = new Intent(getBaseContext(),Locker.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//new 7.0.2
                //i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//new 7.1.0
                //i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);//new 7.1.0
                if(clearTask){
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//new 7.0.9 clear c locker other activities like settings..
                }
                if(extra!=null){
                    i.putExtra(C.EXTRA_INFO,extra);
                }
                startActivity(i);
                if(name!=null){
                    //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>launchLocker: "+name);
                }
            }
        },delayTime);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId){
        try{
            instance = true;

            utils = new MyCLocker(this);
            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>onStartCommand");
            mSaveData = new SaveData(this);
            mSaveData.setKillLocker(false);//backup
            prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
            editor = prefs.edit();
            securityUnlockView = null;//must set null.
            mLocation = new MyLocationManager(getBaseContext(),this);

            loadSettings();
            loadReceiver(true);
            loadScreenTimeoutObserver(true);
            loadLockerProInstalledCheck();

            setRSSInterval();
            if(musicAction==null){
                musicAction = new MusicActions(this);
            }
            if(isFirstTimeRunning){
                editor.putBoolean("isFirstTimeRunning",false);
                editor.commit();
            }
            loadNotification();
            if(mSaveData.isOnReboot()){
                final Intent i = new Intent(getPackageName()+C.REMOVE_STARTUP_VIEW);
                sendBroadcast(i);
                launchLocker("mSaveData.isOnReboot() && !isLockerDefaultHome()",true,true,0);
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
        return START_STICKY;
    }
    @Override public void onDestroy(){
        try{
            loadReceiver(false);
            loadScreenTimeoutObserver(false);
            cancelAlarm(pendingIntent(C.RSS_FEED));
            handlePinUnlock(false,false);

            stopForeground(true);
            instance = false;
            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>onDestroy>Loaded");
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
        try{
            if(musicAction!=null){
                musicAction.close();
            }
            if(utils!=null){
                utils.close();
            }
            if(mLocation!=null){
                mLocation.cancelUpdate();
                mLocation = null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }
    @Override public void onConfigurationChanged(Configuration newConfig){
        try{
            handlePinUnlock(false,false);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override public IBinder onBind(Intent intent){
        return null;
    }
    private void loadSettings(){
        isFirstTimeRunning = prefs.getBoolean("isFirstTimeRunning",true);
        boolean cBoxDisplayFullScreen = prefs.getBoolean("cBoxDisplayFullScreen",false);
        int h;
        if(cBoxDisplayFullScreen){
            h = C.getRealDisplaySize(getBaseContext()).y;
        }else{
            h = C.getRealDisplaySize(getBaseContext()).y-C.getStatusBarHeight(getBaseContext());
        }
        editor.putInt(USABLE_DISPLAY_HEIGHT,h);
        editor.commit();

        cBoxBlockStatusbar = prefs.getBoolean("cBoxBlockStatusbar",true);
        cBoxLockSound = prefs.getBoolean("cBoxLockSound",false);
        cBoxUnlockSound = prefs.getBoolean("cBoxUnlockSound",false);
        cBoxShowRSSUpdateNotice = prefs.getBoolean("cBoxShowRSSUpdateNotice",true);
        cBoxVolumeControlScreenOff = prefs.getBoolean("cBoxVolumeControlScreenOff",false);
        cBoxNoLockShortcut = prefs.getBoolean("cBoxNoLockShortcut",false);

        intSpnRssUpdateInterval = prefs.getInt("intSpnUpdateInterval",0);
        intDelayLock = prefs.getInt("intDelayLock",0);
        intLockStyle = prefs.getInt("intLockStyle",C.SIMPLE_UNLOCK);//1 = center, 2&3 = bottom

        lockSoundType = prefs.getInt("lockSoundType",C.SOUND_TYPE_CLICK);
        unlockSoundType = prefs.getInt("unlockSoundType",C.SOUND_TYPE_CLICK);

        final DeviceAdminHandler deviceAdminHandler = new DeviceAdminHandler(this);
        if(!deviceAdminHandler.isDeviceAdminActivated()){
            cBoxInactiveScreenOff = false;
            cBoxEnableProximitySensor = false;
        }else{
            cBoxInactiveScreenOff = prefs.getBoolean("cBoxInactiveScreenOff",true);
            cBoxEnableProximitySensor = prefs.getBoolean("cBoxEnableProximitySensor",true);
        }

        strRingtoneUri1 = prefs.getString("strRingtoneUri1","");
        strRingtoneUri2 = prefs.getString("strRingtoneUri2","");
        //tasker
        strTaskerUnlock = prefs.getString("strTaskerUnlock","");
        strTaskerUnPIN = prefs.getString("strTaskerUnPIN","");

        cBoxProfileWifi = prefs.getBoolean("cBoxProfileWifi",false);
        cBoxProfileBluetooth = prefs.getBoolean("cBoxProfileBluetooth",false);

        cBoxSmartWifiUnlock = prefs.getBoolean("cBoxSmartWifiUnlock",false);
        cBoxSmartBluetoothUnlock = prefs.getBoolean("cBoxSmartBluetoothUnlock",false);
        cBoxSmartLocationUnlock = prefs.getBoolean("cBoxSmartLocationUnlock",false);
        //cBoxSmartTimeUnlock = prefs.getBoolean("cBoxSmartTimeUnlock",true);
        //cBoxSmartMusicUnlock = prefs.getBoolean("cBoxSmartMusicUnlock",true);

        isTrustedWifiConnected = false;
        isTrustedBtConnected = false;
        isTrustedLocation = false;

        String str = prefs.getString("strTrustedWifi","");
        String[] items = str.split(";");
        wifiSavedlist.clear();
        Collections.addAll(wifiSavedlist,items);

        str = prefs.getString("strTrustedBluetooth","");
        items = str.split(";");
        bluetoothSavedlist.clear();
        Collections.addAll(bluetoothSavedlist,items);
        GESTURE_LENGTH = (int)(C.getRealDisplaySize(this).x*0.2f);
    }
    private void loadReceiver(final boolean on){
        try{
            if(on){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_SCREEN_ON);
                intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
                intentFilter.addAction(C.DISABLE_LOCKER_SEND);
                intentFilter.addAction(C.DISABLE_BOTH_LOCKER_SEND);

                if(cBoxProfileWifi){
                    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                }
                if(cBoxProfileBluetooth){
                    intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
                    intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
                }
                //intentFilter.addAction(Intent.ACTION_SHUTDOWN);
                //intentFilter.addAction(Intent.ACTION_REBOOT);
                //intentFilter.addAction(C.HTC_SHUTDOWN);
                //
                intentFilter.addAction(getPackageName()+C.DELAY_LOCKER);
                intentFilter.addAction(getPackageName()+C.PLAY_SOUND);
                intentFilter.addAction(getPackageName()+C.UNEXPECTED_UNLOCK);
                intentFilter.addAction(getPackageName()+C.RUN_LAUNCHER);
                intentFilter.addAction(getPackageName()+C.BLOCK_SEARCH_LONG_PRESS);
                intentFilter.addAction(getPackageName()+C.BLOCK_SYSTEM_SETTINGS);
                intentFilter.addAction(getPackageName()+C.RUN_SETTINGS_NOTI);
                intentFilter.addAction(getPackageName()+C.RUN_BLOCK_APP);
                intentFilter.addAction(getPackageName()+C.STOP_BLOCK_APP);
                intentFilter.addAction(getPackageName()+C.RSS_FEED);
                intentFilter.addAction(getPackageName()+C.RSS_NOTICE);
                //tasker
                intentFilter.addAction(getPackageName()+C.TASKER+strTaskerUnlock);
                intentFilter.addAction(getPackageName()+C.TASKER+strTaskerUnPIN);
                //block statusbar
                intentFilter.addAction(getPackageName()+C.ON_LOCKER_RESUME);
                intentFilter.addAction(getPackageName()+C.ON_LOCKER_PAUSE);
                intentFilter.addAction(getPackageName()+C.ON_LOCKER_START);
                intentFilter.addAction(getPackageName()+C.ON_LOCKER_STOP);
                //PIN unlock
                intentFilter.addAction(getPackageName()+C.OPEN_PIN);
                intentFilter.addAction(getPackageName()+C.CLOSE_PIN);
                intentFilter.addAction(getPackageName()+C.LAUNCH_LOCKER);
                intentFilter.addAction(getPackageName()+C.CANCEL_SCREEN_OFF);
                intentFilter.addAction(getPackageName()+C.EMERGENCY_CALL);

                registerReceiver(mainReceiver,intentFilter);

                final IntentFilter intentSmsFilter = new IntentFilter();
                intentSmsFilter.addAction(C.SMS_RECEIVED);
                registerReceiver(smsReceiver,intentSmsFilter);

                IntentFilter intentMmsFilter = IntentFilter.create(C.WAP_PUSH_RECEIVED_ACTION,C.MMS_DATA_TYPE);
                intentMmsFilter.setPriority(Integer.MAX_VALUE);
                registerReceiver(mmsReceiver,intentMmsFilter);

                final IntentFilter intentCallFilter = new IntentFilter();
                intentCallFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
                registerReceiver(callReceiver,intentCallFilter);
            }else{
                unregisterReceiver(mainReceiver);
                unregisterReceiver(smsReceiver);
                unregisterReceiver(mmsReceiver);
                unregisterReceiver(callReceiver);
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private void loadProxiSensorReceiver(boolean runSensor){
        try{
            if(cBoxEnableProximitySensor){
                final SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
                if(runSensor){
                    final Sensor proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    final Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                    if(proxSensor!=null){
                        sensorManager.registerListener(sensorListener,proxSensor,SensorManager.SENSOR_DELAY_NORMAL);
                        sensorManager.registerListener(sensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
                    }
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            loadProxiSensorReceiver(false);
                        }
                    },300);
                }else{
                    sensorManager.unregisterListener(sensorListener);
                }
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private void loadVolumeKeyObserver(final boolean enable,final boolean setOn){
        try{
            if(enable){
                if(setOn){
                    if(cBoxLockSound){//if lock sound is enabled, disable this. this is to prevent auto mute bug
                        return;
                    }
                    if(cBoxUnlockSound){//unlock sound is not affected
                        //return;
                    }
                    final AudioManager myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    if(musicAction.isMusicPlaying()){
                        final int intCurrentVolume = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        final int intMaxVolume = myAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        if(intCurrentVolume==intMaxVolume){
                            intPrevVolume = intMaxVolume-2;
                            myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intCurrentVolume,0);

                            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>loadVolumeKeyObserver: 1");
                        }else if(intCurrentVolume==(intMaxVolume-1)){
                            intPrevVolume = intMaxVolume-1;
                            myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,intCurrentVolume,0);

                            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>loadVolumeKeyObserver: 2");
                        }
                        intPrevVolume = myAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        getContentResolver().registerContentObserver(Settings.System.CONTENT_URI,true,volumeLevelObserver);
                    }
                }else{
                    getContentResolver().unregisterContentObserver(volumeLevelObserver);
                }
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private void loadScreenTimeoutObserver(final boolean setOn){
        try{
            if(setOn){
                getContentResolver().registerContentObserver(Settings.System.CONTENT_URI,true,screenTimeoutObserver);
            }else{
                getContentResolver().unregisterContentObserver(screenTimeoutObserver);
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private void loadLockerProInstalledCheck(){
        if(getPackageName().equals(C.PKG_NAME_FREE)){
            if(isAppInstalled(C.PKG_NAME_PRO)){
                utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>loadLockerProInstalledCheck>ProVersion detected, stopping FreeVersion");
                new ComponentSetting(this).setAllComponentSettings(false,"loadLockerProInstalledCheck");

                mSaveData.setKillLocker(true);//kill locker if it's running
                editor.putBoolean(P.BLN_ENABLE_SERVICE,false);
                editor.commit();
                stopSelf();
            }
        }
    }
    private void loadNotification(){
        try{
            final Intent i = new Intent(getPackageName()+C.RUN_SETTINGS_NOTI);
            final PendingIntent pi = PendingIntent.getBroadcast(this,0,i,0);
            //final Bitmap bmp  = BitmapFactory.decodeResource(getResources(),loadAppIcon());
            //final Bitmap bmpResized = Bitmap.createScaledBitmap(bmp,144,144,false);
            final Notification.Builder builder = new Notification.Builder(this)
                //.setOngoing(true)
                .setSmallIcon(loadAppIcon())
                //.setLargeIcon(bmp)
                .setContentTitle(getString(R.string.prevent_locker_kill_notice)).setContentText(getString(R.string.prevent_locker_kill_notice_desc))
                //.setSubText(getString(R.string.prevent_locker_kill_notice))
                .setContentIntent(pi);
            boolean cBoxServicePriorityMid = prefs.getBoolean("cBoxServicePriorityMid",true);
            boolean cBoxServicePriorityHigh = prefs.getBoolean("cBoxServicePriorityHigh",false);
            if(cBoxServicePriorityHigh){
                if(!isSystemApp()){
                    builder.setPriority(Notification.PRIORITY_DEFAULT);
                    startForeground(1,builder.build());
                }else{
                    builder.setPriority(Notification.PRIORITY_MIN);
                    startForeground(1,builder.build());
                }
            }else if(cBoxServicePriorityMid){
                if(!isSystemApp()){
                    builder.setPriority(Notification.PRIORITY_MIN);
                    startForeground(1,builder.build());
                }
            }else{
                stopForeground(true);
            }
        }catch(NoSuchMethodError e){
            utils.writeToFile(C.FILE_BACKUP_RECORD,"NoSuchMethodError: "+e);
            e.printStackTrace();//dont delete
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private PendingIntent pendingIntent(String pendingIntent){
        final Intent intent = new Intent(getPackageName()+pendingIntent);
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //smart unlock
    @Override public void updateLocation(Location loc){
    }
    @Override public void updateLocationFinal(Location loc){
        handleLocationProfile(mLocation,loc);
    }
    @Override public void updateLocationGpsError(){
    }
    @Override public void updateLocationError(String msg){
        utils.writeToFile(C.FILE_BACKUP_RECORD,msg);
    }
    private void checkLocation(long time){
        if(!mSaveData.isLockerRunning()){
            return;
        }
        if(!new ProfileHandler(this,null).isProfileLocationEnabled()){
            return;
        }
        if(isCheckingLocation){
            return;
        }
        final Location loc = mLocation.getLastLocation();
        if(loc!=null){
            isTrustedLocation = false;
            isCheckingLocation = true;
            if(loc.getAccuracy()<50){
                final long timeNow = System.currentTimeMillis();
                final long gap = timeNow-loc.getTime();
                if(gap>time){//5min
                    mLocation.runLocationChecker();
                    return;
                }
            }else{
                mLocation.runLocationChecker();
                return;
            }
            handleLocationProfile(mLocation,loc);
        }
    }
    private void handleLocationProfile(MyLocationManager mLocation,Location loc){
        try{
            //AppPkg = lat
            //SppClass = lon
            //AppName = city name
            //ShortcutAction = distance coverage
            final DataAppsSelection shortcutData = new DataAppsSelection(getBaseContext());
            final List<InfoAppsSelection> list = shortcutData.getApps(DataAppsSelection.APP_TYPE_SAVED_LOCATION);
            final Location profileLoc = new Location("Profile Location");
            for(InfoAppsSelection item : list){
                /*
                utils.writeToFile(C.FILE_BACKUP_RECORD,
						"ProfileHandler>isProfileLocation 0: "+
						item.getAppPkg()+"/"+item.getAppClass()+"/"+
						item.getAppName()+"/"+item.getAppPostCount());//*/

                profileLoc.setLatitude(Double.parseDouble(item.getAppPkg()));
                profileLoc.setLongitude(Double.parseDouble(item.getAppClass()));

                final int distance;
                if(mLocation!=null && loc!=null){
                    distance = mLocation.compareDistance(profileLoc,loc);
                    if(distance<item.getShortcutAction()){
                        isTrustedLocation = true;
                        break;
                    }
                }
            }
            if(cBoxSmartLocationUnlock){
                if(isTrustedLocation){
                    mSaveData.setKillLocker(true);
                    sendBroadcast(new Intent(getPackageName()+C.KILL_LOCKER));
                    msg(false,"Smart Unlock: Trusted Location");
                }else{
                    if(!isScreenOn){
                        launchLocker(null,true,true,0);
                    }
                }
            }
            if(isTrustedLocation!=mSaveData.isProfileLocationActive()){
                mSaveData.setProfileLocationActive(isTrustedLocation);
                sendBroadcast(new Intent(getPackageName()+C.LOCATION_UPDATE));
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
        isCheckingLocation = false;
    }
    private void handleWifiProfile(Intent intent){
        final NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(netInfo.getType()==ConnectivityManager.TYPE_WIFI){
            isTrustedWifiConnected = false;
            if(netInfo.isConnected()){
                final WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                final WifiInfo info = wifiManager.getConnectionInfo();
                final String ssid = info.getSSID();
                for(String item : wifiSavedlist){
                    if(item.equals(ssid)){
                        isTrustedWifiConnected = true;
                        break;
                    }
                }
            }
            if(cBoxSmartWifiUnlock){
                if(isTrustedWifiConnected){
                    mSaveData.setKillLocker(true);
                    sendBroadcast(new Intent(getPackageName()+C.KILL_LOCKER));
                    msg(false,"Smart Unlock: Trusted Wifi");
                }else{
                    if(!isScreenOn){
                        launchLocker(null,true,true,0);
                    }
                }
            }
            if(isTrustedWifiConnected!=mSaveData.isProfileWifiActive()){
                mSaveData.setProfileWifiActive(isTrustedWifiConnected);
                sendBroadcast(new Intent(getPackageName()+C.WIFI_UPDATE));
            }
        }
    }
    private void handleBluetoothProfile(boolean isConnected,Intent intent){
        if(isConnected){
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            isTrustedBtConnected = false;
            for(String item : bluetoothSavedlist){
                if(item.equals(device.getName())){
                    isTrustedBtConnected = true;
                    break;
                }
            }
            if(cBoxSmartBluetoothUnlock){
                if(isTrustedBtConnected){
                    mSaveData.setKillLocker(true);
                    sendBroadcast(new Intent(getPackageName()+C.KILL_LOCKER));
                    msg(false,"Smart Unlock: Trusted Bluetooth");
                }else{
                    if(!isScreenOn){
                        launchLocker(null,true,true,0);
                    }
                }
            }
            if(isTrustedBtConnected){
                mSaveData.setProfileBluetoothActive(true);
                sendBroadcast(new Intent(getPackageName()+C.BLUETOOTH_UPDATE));
            }
        }else{
            isTrustedBtConnected = false;
            mSaveData.setProfileBluetoothActive(false);
            sendBroadcast(new Intent(getPackageName()+C.BLUETOOTH_UPDATE));
            if(!isScreenOn){
                launchLocker(null,true,true,0);
            }
        }
    }
    //
    @TargetApi(Build.VERSION_CODES.M)
    private void handleInactiveScreenOffWindow(boolean show){
        //if(!cBoxInactiveScreenOff)return;
        checkWindowManager();
        handler.removeCallbacks(turnInactiveScreenOff);
        try{
            if(show){
                if(lytInactiveScreenOff==null){
                    //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleInactiveScreenOffWindow in 10 seconds");
                    lytInactiveScreenOff = lytInactiveScreenOff();
                    addOverlayWindow(lytInactiveScreenOff,prInactiveScreenOff());
                    if(cBoxInactiveScreenOff){
                        handler.postDelayed(turnInactiveScreenOff,C.UNTOUCH_SCREEN_TIMEOUT);
                    }
                }
            }else{
                if(lytInactiveScreenOff!=null){
                    //utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleInactiveScreenOffWindow cancelled: "+mSaveData.isTurnScreenOff());
                    removeOverlayWindow(lytInactiveScreenOff);
                    lytInactiveScreenOff = null;

                    //reset screen time out if new sms, call received and screen is touched
                    //if(mSaveData.isTurnScreenOff()){
                    mSaveData.setTurnScreenOff(false);
                    final Intent i = new Intent(getPackageName()+C.CANCEL_SCREEN_TIMEOUT);
                    sendBroadcast(i);
                    //}
                }
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private LinearLayout lytInactiveScreenOff(){
        final LinearLayout lyt = new LinearLayout(this);
        lyt.setOnTouchListener(new OnTouchListener(){
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v,MotionEvent m){
                switch(m.getActionMasked()){
                    case MotionEvent.ACTION_OUTSIDE:
                        handleInactiveScreenOffWindow(false);
                }
                return true;
            }
        });
        return lyt;
    }
    //
    private void setAlarmWakeup(int time,String pendingIntent){
        final AlarmManager almg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        almg.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+time,pendingIntent(pendingIntent));
    }
    private void cancelAlarm(PendingIntent pi){
        try{
            final AlarmManager almg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
            almg.cancel(pi);
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private void setRSSInterval(){
        if(intSpnRssUpdateInterval!=0){
            final Calendar cldRightNow = Calendar.getInstance();
            final int hours = cldRightNow.get(Calendar.HOUR_OF_DAY);
            final int min = cldRightNow.get(Calendar.MINUTE);
            final int sec = cldRightNow.get(Calendar.SECOND);
            final int updateHours = intSpnRssUpdateInterval-hours%intSpnRssUpdateInterval;

            final Calendar cldRSSUpdate = Calendar.getInstance();
            cldRSSUpdate.setTimeInMillis(System.currentTimeMillis());
            cldRSSUpdate.add(Calendar.HOUR_OF_DAY,updateHours);
            cldRSSUpdate.add(Calendar.MINUTE,-min);
            cldRSSUpdate.add(Calendar.SECOND,-sec);

            final long time = cldRSSUpdate.getTimeInMillis();
            final long interval = intSpnRssUpdateInterval*1000*60*60;
            setRepeatingAlarm(time,interval,C.RSS_FEED);
        }
    }
    private void setRepeatingAlarm(long time,long interval,String pendingIntent){
        final AlarmManager almg = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        almg.setRepeating(AlarmManager.RTC,time,interval,pendingIntent(pendingIntent));
    }
    //
    private void setTurnScreenOff(final boolean setOff,final int delayVal,final String log){
        handler.removeCallbacks(turnScreenOff);
        if(setOff){
            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>setTurnScreenOff: "+setOff+"/"+log);
            handler.postDelayed(turnScreenOff,delayVal);
        }
    }
    private void setScreenTimeOutValue(int screenTimeOutVal){
        final int scVal = mSaveData.getScreenTimeoutType();
        //final int intDefaultScreenTimeOutVal = prefs.getInt("intDefaultScreenTimeOutVal",C.DEFAULT_SCREEN_TIMEOUT);
        final int intScreenTimeOut = prefs.getInt("intScreenTimeOut",C.DEFAULT_SCREEN_TIMEOUT);
        final int intScreenSystemTimeOut = prefs.getInt("intScreenSystemTimeOut",C.FOLLOW_SYSTEM_TIMEOUT);

        if(scVal==C.TYPE_LOCKER_SCREEN_TIMEOUT){
            if(intScreenTimeOut!=C.FOLLOW_SYSTEM_TIMEOUT){
                return;
            }
        }else if(scVal==C.TYPE_SYSTEM_SCREEN_TIMEOUT){
            if(intScreenSystemTimeOut!=C.FOLLOW_SYSTEM_TIMEOUT){
                return;
            }
        }
        if(screenTimeOutVal<=C.MIN_SCREEN_TIMEOUT){
            screenTimeOutVal = mSaveData.getScreenTimeOutVal();
            if(screenTimeOutVal<=C.MIN_SCREEN_TIMEOUT){
                screenTimeOutVal = C.DEFAULT_SCREEN_TIMEOUT;
            }
        }
        if(screenTimeOutVal==C.FOLLOW_SYSTEM_TIMEOUT){
            screenTimeOutVal = mSaveData.getScreenTimeOutVal();
            if(screenTimeOutVal<=C.MIN_SCREEN_TIMEOUT){
                screenTimeOutVal = C.DEFAULT_SCREEN_TIMEOUT;
            }else if(screenTimeOutVal==C.FOLLOW_SYSTEM_TIMEOUT){
                screenTimeOutVal = C.DEFAULT_SCREEN_TIMEOUT;
            }
        }
        mSaveData.setScreenTimeOutVal(screenTimeOutVal);
        /*
        utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>\n  -setScreenTimeOutValue: "+screenTimeOutVal+
                "\n  -intDefaultScreenTimeOutVal: "+intDefaultScreenTimeOutVal+
                "\n  -intScreenTimeOut: "+intScreenTimeOut+
                "\n  -intScreenSystemTimeOut: "+intScreenSystemTimeOut);//*/
    }
    private void setVibration(final boolean bln,final long vibraVal){
        final Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if(bln){
            if(C.isAndroid(Build.VERSION_CODES.O)){
                vb.vibrate(VibrationEffect.createOneShot (vibraVal,VibrationEffect.DEFAULT_AMPLITUDE));
            }else{
                vb.vibrate(vibraVal);
            }
        }
    }
    private void handleHomeKey(){
        try{
            utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleHomeKey");
            if(mSaveData.isOnReboot()){
                utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleHomeKey>isOnReboot");
                launchLocker("isOnReboot",false,false,0);
            }else if(SettingsHomeKey.isSettingsHomeKey()){
                utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleHomeKey>SettingsHomeKey");

                if(C.hasNaviKey(this)){
                    final Intent i = new Intent(getBaseContext(),SettingsHomeKey.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//must
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }
            }else if(!mSaveData.isLockerRunning() && isLockerDefaultHome()){
                utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleHomeKey>isLockerDefaultHome");
                final String pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
                final String className = prefs.getString(P.STR_HOME_LAUNCHER_CLASS_NAME,"");
                final Intent i = getAppIntent(pkgName,className);
                startActivity(i);
                handler.post(runLauncher);
            }else if(mSaveData.isLockerRunning()){
                utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleHomeKey>isLockerRunning");
                final String pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
                final String className = prefs.getString(P.STR_HOME_LAUNCHER_CLASS_NAME,"");
                final Intent i = getAppIntent(pkgName,className);
                startActivity(i);//launch home launcher 1st will prevent locker double launch
                launchLocker("handleHomeKey",false,false,0);//system will delay 5 secs...
            }
        }catch(Exception e){
            utils.saveErrorLog("ServiceMain>handleHomeKey",e);
            new ComponentSetting(this).setHomeLauncher(false,"handleHomeKey");

            final Intent i = new Intent(Intent.ACTION_MAIN);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
        }
    }
    private boolean isLockerDefaultHome(){
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        final String homeAppPkgName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        final String homeAppClassName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name;

        if(homeAppPkgName.equals(getPackageName())){
            return true;
        }else if(homeAppPkgName.equals("android") ||
            homeAppPkgName.equals("org.cyanogenmod.resolver") ||
            homeAppPkgName.equals("com.huawei.android.internal.app")){
            // SettingsSecurity,SettingsHomeKey,ServiceMain,Locker classes all need to change together!!!!!
            // do nothing
            //utils.writeToFile("Locker>isLockerDefaultHome>android: "+homeAppPkgName+"/"+homeAppClassName);
        }else{
            editor.putString(P.STR_HOME_LAUNCHER_PKG_NAME,homeAppPkgName);
            editor.putString(P.STR_HOME_LAUNCHER_CLASS_NAME,homeAppClassName);
            editor.commit();
            //utils.writeToFile("Locker>isLockerDefaultHome: "+homeAppPkgName+"/"+homeAppClassName);
        }
        return false;
    }
    private Intent getAppIntent(String pkgName,String className){
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        i.setClassName(pkgName,className);
    	/*if(pkgName.equals("com.teslacoilsw.launcher")||
    		pkgName.equals("com.google.android.googlequicksearchbox")){
    		i = new Intent(Intent.ACTION_MAIN);
    		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    		i.setClassName(pkgName,className);

    		Log.e("","getAppIntent 1: "+i);
    	}else{
        	i = getPackageManager().getLaunchIntentForPackage(pkgName);
        	Log.e("","getAppIntent 2: "+i);
        	if(i==null){
        		i = new Intent(Intent.ACTION_MAIN);
        		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        		i.setClassName(pkgName,className);
            	Log.e("","getAppIntent 3: "+i);
        	}
    	}*/
        return i;
    }
    private void handleSearchLongPressKey(){
        try{
            if(C.isAccessibilityEnabled(this)){
                return;
            }
            if(mSaveData.isLockerRunning()){
                launchLocker("handleSearchLongPressKey 2",false,false,0);
            }else if(!mSaveData.isLockerRunning() && isLockerDefaultSearchLongPress()){
                final String pkgName = prefs.getString(P.STR_SEARCH_LONG_PRESS_PKG_NAME,"");
                final String className = prefs.getString(P.STR_SEARCH_LONG_PRESS_CLASS_NAME,"");

                final Intent i = getAppIntent(pkgName,className);
                startActivity(i);
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
            new ComponentSetting(this).setSearchLongPress(false,"handleSearchLongPressKey");

            final Intent i = new Intent(Intent.ACTION_ASSIST);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(i);
        }
    }
    private boolean isLockerDefaultSearchLongPress(){
        final Intent i = new Intent(Intent.ACTION_ASSIST);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        final String homeAppPkgName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        final String homeAppClassName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name;

        if(homeAppPkgName.equals(getPackageName())){
            return true;
        }else if(homeAppPkgName.equals("android")){
            // do nothing
            //utils.writeToFile("Locker>isLockerDefaultHome>android: "+homeAppPkgName+"/"+homeAppClassName);
        }else{
            editor.putString(P.STR_SEARCH_LONG_PRESS_PKG_NAME,homeAppPkgName);
            editor.putString(P.STR_SEARCH_LONG_PRESS_CLASS_NAME,homeAppClassName);
            editor.commit();
            //utils.writeToFile("Locker>isLockerDefaultHome: "+homeAppPkgName+"/"+homeAppClassName);
        }
        return false;
    }
    private void handleBlockSystemSettings(){
        try{
            if(C.isAccessibilityEnabled(this)){
                return;
            }
            if(mSaveData.isLockerRunning()){
                launchLocker("handleSearchLongPressKey 2",false,false,0);
            }else if(!mSaveData.isLockerRunning() && isLockerDefaultSystemSettings()){
                final String pkgName = prefs.getString(P.STR_SETTINGS_PKG_NAME,"");
                final String className = prefs.getString(P.STR_SETTINGS_CLASS_NAME,"");

                final Intent i = getAppIntent(pkgName,className);
                startActivity(i);
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
            new ComponentSetting(this).setSystemSettings(false,"handleBlockSystemSettings");

            final Intent i = new Intent(Settings.ACTION_SETTINGS);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(i);
        }
    }
    private boolean isLockerDefaultSystemSettings(){
        Intent i = new Intent(Settings.ACTION_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        final String homeAppPkgName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        final String homeAppClassName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name;

        if(homeAppPkgName.equals(getPackageName())){
            return true;
        }else if(homeAppPkgName.equals("android")){
            // do nothing
            //utils.writeToFile("Locker>isLockerDefaultHome>android: "+homeAppPkgName+"/"+homeAppClassName);
        }else{
            editor.putString(P.STR_SETTINGS_PKG_NAME,homeAppPkgName);
            editor.putString(P.STR_SETTINGS_CLASS_NAME,homeAppClassName);
            editor.commit();
            //utils.writeToFile("Locker>isLockerDefaultHome: "+homeAppPkgName+"/"+homeAppClassName);
        }
        return false;
    }
    private void handleSettingsNotice(){
        try{
            if(mSaveData.isLockerRunning()){
                final Intent i = new Intent(getPackageName()+C.RUN_SETTINGS_LOCKER);
                sendBroadcast(i);
            }else{
                launchLocker(null,true,true,0);
                //mSaveData.setSettingsCalledByLocker(true);
                //startActivity(getLockerClassIntent(SettingsLockerMain.class));
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private boolean isVoiceMailActive(){
        try{
            ComponentName cn = C.getRunningTask(ServiceMain.this,0);
            if(cn!=null){
                String pkgName = cn.getPackageName();
                if(pkgName.equals("com.samsung.vvm")){
                    return true;
                }
            }
        }catch(SecurityException e){
            utils.saveErrorLog(null,e);
        }
        return false;
    }
    private boolean isBlockApp(final String pkgName){
        try{
            final boolean cBoxBlockNoneShortcutApps = prefs.getBoolean("cBoxBlockNoneShortcutApps",false);
            if(cBoxBlockNoneShortcutApps){
                final DataAppsSelection db = new DataAppsSelection(this);
                final List<InfoAppsSelection> list = db.getApps(DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                if(list!=null){
                    for(InfoAppsSelection item : list){
                        if(item.getAppPkg().equals(pkgName)){
                            return true;
                        }
                    }
                }
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
        return false;
    }
    private boolean isSystemApp(){
        try{
            final ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),0);
            final String currentDir = appInfo.sourceDir;
            if(currentDir!=null && currentDir.contains("/system/")){
                return true;
            }
        }catch(NameNotFoundException e){
            utils.saveErrorLog(null,e);
        }
        return false;
    }
    private boolean isAppInstalled(String appPkg){
        try{
            final PackageManager pm = getPackageManager();
            final Intent it = new Intent(Intent.ACTION_MAIN);
            it.addCategory(Intent.CATEGORY_HOME);
            pm.queryIntentActivities(it,0);
            final List<ResolveInfo> list = pm.queryIntentActivities(it,0);
            for(int i = 0; i<list.size(); i++){
                final ResolveInfo app = list.get(i);
                if(app.activityInfo.packageName.equals(appPkg)){
                    //utils.writeToFile("ServiceMain>isAppInstalled: "+appPkg);
                    return true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    private void checkSecurityEmailPending(){
        try{
            boolean isSecurityEmailPending = prefs.getBoolean("isNewSecuritySelfieSendEmailPending",false);
            if(isSecurityEmailPending){
                new GmailSender(this).sendEmail();
            }
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }
    private void msg(boolean bln,String str){
        if(bln){
            Toast.makeText(this,str,Toast.LENGTH_LONG).show();
        }
    }
    private boolean checkSystemWindowPermission(){
        return !C.isAndroid(Build.VERSION_CODES.M) || Settings.canDrawOverlays(this);
    }
    private int loadAppIcon(){
        if(getPackageName().equals(C.PKG_NAME_PRO)){
            return R.drawable.c_lockscreen_icon_small_x_pro;
        }
        return R.drawable.c_lockscreen_icon_small_x;
    }
    private void playSound(final boolean bln,final int no){
        if(isInCalling){
            return;
        }
        if(bln){
            new Thread(){
                public void run(){//play sound
                    try{
                        final Ringtone ringtone;
                        final AudioManager myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                        if(no==LOCK_SOUND){
                            switch(lockSoundType){
                                case C.SOUND_TYPE_CLICK:
                                    myAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK,(float)1.5);
                                    break;
                                case C.SOUND_TYPE_PRESS:
                                    myAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,(float)1.5);
                                    break;
                                case C.SOUND_TYPE_SYSTEM_RINGTONE:
                                    ringtone = RingtoneManager.getRingtone(ServiceMain.this,RingtoneManager.getActualDefaultRingtoneUri(ServiceMain.this,RingtoneManager.TYPE_NOTIFICATION));
                                    ringtone.play();
                                    break;
                                case C.SOUND_TYPE_CUSTOM_RINGTONE:
                                    uriRingtone = Uri.parse(strRingtoneUri1);
                                    ringtone = RingtoneManager.getRingtone(ServiceMain.this,uriRingtone);
                                    ringtone.play();
                                    break;
                            }
                        }else{
                            switch(unlockSoundType){
                                case C.SOUND_TYPE_CLICK:
                                    myAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK,(float)1.5);
                                    break;
                                case C.SOUND_TYPE_PRESS:
                                    myAudioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,(float)1.5);
                                    break;
                                case C.SOUND_TYPE_SYSTEM_RINGTONE:
                                    ringtone = RingtoneManager.getRingtone(ServiceMain.this,RingtoneManager.getActualDefaultRingtoneUri(ServiceMain.this,RingtoneManager.TYPE_NOTIFICATION));
                                    ringtone.play();
                                    break;
                                case C.SOUND_TYPE_CUSTOM_RINGTONE:
                                    uriRingtone = Uri.parse(strRingtoneUri2);
                                    ringtone = RingtoneManager.getRingtone(ServiceMain.this,uriRingtone);
                                    ringtone.play();
                                    break;
                            }
                        }
                    }catch(Exception e){
                        utils.saveErrorLog(null,e);
                    }
                }
            }.start();
        }
    }
    //
    private void handleUnexpectedUnlock(boolean show){
    	/*boolean dontShow = prefs.getBoolean("dontShowMyUnexpectedUnlockDialog",false);
    	if(dontShow){
    		mSaveData.setShortcutRunning(false);//to prevent c locker not launching
    		mSaveData.setLockerRunning(false);
    		utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleUnexpectedUnlock>dontShow");
    		return;
    	}//*/
        boolean cBoxHomeKeyBypassMsg = prefs.getBoolean("cBoxHomeKeyBypassMsg",true);
        if(!cBoxHomeKeyBypassMsg){
            return;
        }
        if(show){
            if(mSaveData.isLockerRunning()){
                if(unexpectedUnlockDialog==null){
                    checkWindowManager();
                    unexpectedUnlockDialog = new MyUnexpectedUnlockDialog(this,getString(R.string.home_key_bypass_warn));
                    addOverlayWindow(unexpectedUnlockDialog,prOverlayView(unexpectedUnlockDialog));
                    launchLocker("blockApp>RECENT_APPS_CLASS",false,false,0);
                    utils.writeToFile(C.FILE_BACKUP_RECORD,"ServiceMain>handleUnexpectedUnlock>unexpectedUnlockDialog");
                }
            }
        }else{
            if(unexpectedUnlockDialog!=null){
                checkWindowManager();
                removeOverlayWindow(unexpectedUnlockDialog);
                unexpectedUnlockDialog = null;
            }
        }
    }
    private class MyUnexpectedUnlockDialog extends LinearLayout{
        private Runnable run = new Runnable(){
            @Override
            public void run(){
                launchLocker("MyUnexpectedUnlockDialog",true,true,0);
            }
        };

        private MyUnexpectedUnlockDialog(final Context context,String content){
            super(context);
            final TextView title = new TextView(context);
            final View lineTop = new View(context);
            final TextView desc = new TextView(context);
            final CheckBox cBox = new CheckBox(context);
            final LinearLayout selection = new LinearLayout(context);
            final Button ok = new Button(context);
            final Button no = new Button(context);
            final int pad = C.dpToPx(context,16);

            title.setText(context.getString(R.string.app_name)+" "+context.getString(R.string.security));
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setPadding(0,0,0,pad/2);
            lineTop.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,2));
            lineTop.setBackgroundColor(Color.GRAY);
            desc.setText(content);
            desc.setTextColor(Color.WHITE);
            title.setPadding(0,pad/2,0,0);
            cBox.setText(R.string.settings_dont_show_again);
            cBox.setTextColor(Color.WHITE);
            cBox.setPadding(0,pad,0,pad);
            //selection
            no.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
            no.setText(getString(android.R.string.no));
            no.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            no.setPadding(pad,pad,pad,pad);
            no.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    removeOverlayWindow(unexpectedUnlockDialog);
                    unexpectedUnlockDialog = null;

                    mSaveData.setLockerRunning(false);
                    editor.putBoolean("dontShowMyUnexpectedUnlockDialog",cBox.isChecked());
                    editor.commit();
                }
            });

            ok.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
            ok.setText(getString(android.R.string.ok));
            ok.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
            ok.setPadding(pad,pad,pad,pad);
            ok.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    launchLocker("blockApp>RECENT_APPS_CLASS",false,false,0);
                    //startActivity(getLockerClassIntent(SettingsHomeKey.class));

                    removeOverlayWindow(unexpectedUnlockDialog);
                    unexpectedUnlockDialog = null;

                    editor.putBoolean("dontShowMyUnexpectedUnlockDialog",cBox.isChecked());
                    editor.commit();
                }
            });

            selection.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            selection.setOrientation(LinearLayout.HORIZONTAL);
            selection.addView(no);
            selection.addView(ok);

            //add layout
            //setGravity(Gravity.CENTER);
            LayoutTransition layoutTransition = new LayoutTransition();
            setLayoutTransition(layoutTransition);
            setPadding(pad,pad,pad,pad);
            //setBackgroundColor(Color.parseColor("#222222"));
            setBackgroundColor(Color.parseColor("#bb000000"));
            setOrientation(LinearLayout.VERTICAL);
            setGravity(Gravity.CENTER);
            setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    launchLocker("MyUnexpectedUnlockDialog",true,true,0);
                }
            });

            addView(title);
            addView(lineTop);
            addView(desc);
            //addView(cBox);
            //addView(selection);
        }
        @Override
        protected void onAttachedToWindow(){
            super.onAttachedToWindow();
            handler.postDelayed(run,10000);
        }
        @Override
        protected void onDetachedFromWindow(){
            handler.removeCallbacks(run);
            super.onDetachedFromWindow();
        }
    }
    /*private Intent getLockerClassIntent(Class<?> mClass){
    	final Intent i = new Intent(getBaseContext(),mClass);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		return i;
    }//*/
    /*
    private void requestSystemWindowPermission(){
        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }*/
    /*
    public final void hideRecentApps(){
        try{
            final Intent i = new Intent(C.RECENT_APPS_TOGGLE);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            i.setComponent(new ComponentName(C.RECENT_APPS_PKG,C.RECENT_APPS_CLASS));
            startActivity(i);
        }catch(Exception e){
            utils.saveErrorLog(null,e);
        }
    }//*/
    //
}