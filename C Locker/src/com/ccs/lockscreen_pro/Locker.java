package com.ccs.lockscreen_pro;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.CheckLongPressHelper;
import com.ccs.lockscreen.appwidget.MyWidgetHelper;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.data.InfoWidget;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.C.CallBack;
import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen.security.SecurityUnlockView;
import com.ccs.lockscreen.utils.BottomBarManager;
import com.ccs.lockscreen.utils.DeviceAdminHandler;
import com.ccs.lockscreen.utils.LockerAction;
import com.ccs.lockscreen.utils.MusicActions;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;
import com.ccs.lockscreen.utils.MyAlertWindow;
import com.ccs.lockscreen.utils.MyRingUnlock;
import com.ccs.lockscreen.utils.MySideShortcutLayout;
import com.ccs.lockscreen.utils.MySimpleUnlock;
import com.ccs.lockscreen.utils.MySimpleUnlock.MySimpleUnlockCallBack;
import com.ccs.lockscreen.utils.NotificationView;
import com.ccs.lockscreen.utils.NotificationView.MyViewGroupCallBack;
import com.ccs.lockscreen.utils.ProfileHandler;
import com.ccs.lockscreen.utils.ProfileHandler.OnPriorityCheck;
import com.ccs.lockscreen.utils.WallpaperHandler;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Locker extends FragmentActivity implements
    CallBack,
    WidgetOnTouchListener,
    MyViewGroupCallBack,
    OnPriorityCheck,
    MySimpleUnlockCallBack{

    public static final int DEVICE_ADMIN_SET = 1;
    private static final int PROFILE_ANIMATION = 300;
    private static final int MOBILE_DATA_RESULT = 2;
    //status bar options
    private static final int
        //NAVIGATION_BAR_TRANSIENT 				= 0x08000000,
        //NAVIGATION_BAR_TRANSLUCENT 			= 0x80000000,
        //STATUS_BAR_DISABLE_NOTIFICATION_TICKER= 0x00080000,
        //STATUS_BAR_DISABLE_SYSTEM_INFO 		= 0x00100000;
        //STATUS_BAR_TRANSIENT 					= 0x04000000,
        STATUS_BAR_TRANSLUCENT = 0x40000000, STATUS_BAR_DISABLE_NONE = 0x00000000, STATUS_BAR_DISABLE_HOME = 0x00200000, STATUS_BAR_DISABLE_BACK = 0x00400000, STATUS_BAR_DISABLE_RECENT = 0x01000000, STATUS_BAR_DISABLE_SEARCH = 0x02000000, STATUS_BAR_DISABLE_EXPAND = 0x00010000, STATUS_BAR_DISABLE_CLOCK = 0x00800000, STATUS_BAR_DISABLE_NOTIFICATION_ALERTS = 0x00040000, STATUS_BAR_DISABLE_NOTIFICATION_ICONS = 0x00020000;
    //writeToFile(C.FILE_BACKUP_RECORD,"MyBackupData>copyFile: No file found");
    private static boolean inFrontground = false;
    //=====
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Handler handler = new Handler();
    private DeviceAdminHandler deviceAdminHandler;
    private MyFingerprintManager fingerprintManager;
    private Camera mCam;
    //my classes
    private MyCLocker mLocker;
    private SaveData mSaveData;
    private MyWidgetHelper myWidgetHelper;
    private ProfileHandler profileHandler;
    private MusicActions musicAction;
    private DataAppsSelection shortcutData;
    private BottomBarManager bottomBarManager;
    private NotificationView lytNotice;
    private MyAlertWindow lytAlertWindow;
    private MyRingUnlock lytRingUnlock;
    private MySimpleUnlock lytSimpleUnlock;
    private MySideShortcutLayout lytLockSide;
    private SecurityUnlockView securityUnlockView;
    //
    private View viewLastShortcut;
    private RelativeLayout lytMainActivity, lytWidgets;
    private LinearLayout lytWidgetMain, lytSideNotices, lytNoticePost, lytBottomBar;
    private ImageView imgScreenWallPaper, imgNoticeRemove, imgFingerPrint;
    private long eventID, noticeImgId, lastNoticeUpdateTime;//proximityStartTime
    //boolean
    private boolean isProxySensorOn = false, isScreenOffTimerRunning = false, isTopEdgeTouch = false, isBottomEdgeTouch = false, isLeftEdgeTouch = false, isRightEdgeTouch = false, //PIN Unlock
        cBoxEnablePassUnlock, //Display
        isWidgetClickEnabled = false,
        isLayoutAnmationPending = true, isLayoutAnmationRunning = false, cBoxCtrTvAnmation, cBoxDisplayFullScreen, cBoxPortrait, cBoxIconBgOnPressEffect, rBtnUnlockAnimZoomOut, rBtnUnlockAnimZoomIn, rBtnUnlockAnimRotation, //Shortcut
        isLockerShortcut, isRecentAppsClick, isHomeClick, cBoxHideCenterLockIcon, enableFlashLight = false, //Others
        cBoxReturnLS, cBoxUnlockToHome, cBoxEnableVibration, cBoxBackKeyUnlock, cBoxEnableProximitySensor, cBoxTaskerShortcutCount;
    //int
    private int
        //PIN Unlock
        securityType, intDelayPin, fingerprintAttempt = 5, //Display
        intDisplayWidth, intDisplayHeight, intIconBgOnPressType, getWallpaperType, seekBarWallpaperDimLevel, //Shortcut
        intLockStyle, intGestureUnlockDirection, //Others
        intScreenTimeOut, intScreenSystemTimeOut, intDefaultScreenTimeOutVal, intTimeoutVal;
    //string
    private String callId, phoneNo, smsId, rssLink, strColorIconBgOnPress;
    private Drawable shapeSquareEmptyEnter, shapeNoticeEnter, shapeNoticeExit, shapeEnterSmall;
    //statusbar notices
    private ArrayList<InfoAppsSelection> appNoticeList;
    private int noticeId;
    private String noticePkgName, noticeTag, noticeActionTitle;
    private boolean cBoxEnableStatusBarNotices, cBoxNoticeStyleLollipop, isViewsLoaded = false, isLollipopNoticeViewShown = false, isNoticeLayoutLeft, isActivityOnFocus, isNoticeOnDragging = false, isSideNoticePending = true;
    //app widgets
    private int intLockerWidgetProfile = -1, intProfileManualChanged = -1;//do Not put default value!!!
    private int savedLaunchAction, savedShortcutId;
    //boolean
    private boolean longPress = false;
    //onCreate Load
    private WindowManager.LayoutParams windowPr;
    //MotionOntouch
    private int FINAL_X, FINAL_Y, STARTING_RAW_X, STARTING_RAW_Y;
    private int GESTURE_LENGTH, FINGER_COUNT, TOUCH_COUNT = 0;
    private boolean isTapping = false, isMultiTouch = false, is1stTouchValueContained = false;
    //Others
    private int intAppShortcutRunCount = 0;
    //Classes
    private boolean unlock = false, oneTimeStart = true, oneTimeEnd = true;

    //receivers, listener
    private AccountManagerCallback<Bundle> accCallback = new AccountManagerCallback<Bundle>(){
        @Override
        public void run(AccountManagerFuture<Bundle> future){
            try{
                if(future!=null){
                    boolean isAccVerified = (Boolean)future.getResult().get(AccountManager.KEY_BOOLEAN_RESULT);
                    if(isAccVerified){
                        onFinish(false,getUnlockAnim(),"loadKillLocker");
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };
    private SensorEventListener sensorListener = new SensorEventListener(){
        @Override
        public void onSensorChanged(SensorEvent event){
            switch(event.sensor.getType()){
                case Sensor.TYPE_PROXIMITY:
                    float f = event.values[0];
                    if(f<1.0f){
                        if(C.isScreenPortrait(getBaseContext())){
                            if(!isProxySensorOn){
                                isProxySensorOn = true;
                                //final long timeGap = System.currentTimeMillis()-proximityStartTime;
                                //if(timeGap<300){
                                    //let ServiceMain handle it, added 7.1.0v
                                    //setTurnScreenOff(true,2000,"onSensorChanged");
                                //}
                                setTurnScreenOff(true,5000,"onSensorChanged");
                            }
                        }
                    }else{
                        if(isProxySensorOn){
                            isProxySensorOn = false;
                            setTurnScreenOff(false,0,"onSensorChanged");
                        }
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor arg0,int arg1){
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(Intent.ACTION_TIME_CHANGED) ||
                    action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                    action.equals(Intent.ACTION_TIME_TICK) ||
                    action.equals(getPackageName()+C.HEADSET_UPDATE) ||
                    action.equals(getPackageName()+C.WIFI_UPDATE) ||
                    action.equals(getPackageName()+C.BLUETOOTH_UPDATE) ||
                    action.equals(getPackageName()+C.LOCATION_UPDATE)){
                    //if(isScreenOn()){
                    loadProfile(C.LOCATION_UPDATE+"...");
                    //}
                }else if(action.equals(Intent.ACTION_DOCK_EVENT)){
                    int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,-1);
                    boolean isDocked = dockState!=Intent.EXTRA_DOCK_STATE_UNDOCKED;
                    if(isDocked){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }else{
                        setPortrait(cBoxPortrait);
                    }
                }else if(action.equals(Intent.ACTION_DOCK_EVENT)){
                    int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,-1);
                    boolean isDocked = dockState!=Intent.EXTRA_DOCK_STATE_UNDOCKED;
                    if(isDocked){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }else{
                        setPortrait(cBoxPortrait);
                    }
                }else if(action.equals(Intent.ACTION_DOCK_EVENT)){
                    int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,-1);
                    boolean isDocked = dockState!=Intent.EXTRA_DOCK_STATE_UNDOCKED;
                    if(isDocked){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }else{
                        setPortrait(cBoxPortrait);
                    }
                }else if(action.equals(Intent.ACTION_DOCK_EVENT)){
                    int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE,-1);
                    boolean isDocked = dockState!=Intent.EXTRA_DOCK_STATE_UNDOCKED;
                    if(isDocked){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    }else{
                        setPortrait(cBoxPortrait);
                    }
                }else if(action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
                    onReceiveSystemDialog(intent);
                }else if(action.equals(getPackageName()+C.LOCKERSCREEN_ON)){
                    bottomBarManager.runViewPager();
                }else if(action.equals(getPackageName()+C.PAGER_TAB_NOTICE)){
                    new TabActionHandler().onReceivePagerTab(intent);
                }else if(action.equals(getPackageName()+C.PAGER_CLICK_NOTICE)){
                    new TabActionHandler().onReceivePagerClick(intent);
                }else if(action.equals(getPackageName()+C.NOTICE_LISTENER_LOCKER)){
                    if(cBoxEnableStatusBarNotices){
                        isSideNoticePending = true;
                        lastNoticeUpdateTime = System.currentTimeMillis();

                        if(appNoticeList!=null){
                            appNoticeList.clear();
                        }
                        appNoticeList = intent.getParcelableArrayListExtra(C.NOTICE_ALL_APP_LIST);
                        removeSideNotice();

                        if(C.isScreenOn(context,getWindowManager()) && isActivityOnFocus && !isNoticeOnDragging){
                            loadNoticeLayout(NotificationView.ON_NEW_UPDATE,lastNoticeUpdateTime);
                        }
                    }
                }else if(action.equals(getPackageName()+C.CANCEL_SCREEN_TIMEOUT)){
                    if(deviceAdminHandler.isDeviceAdminActivated()){
                        handler.removeCallbacks(turnScreenOff);
                    }else{
                        handler.removeCallbacks(screenTimeout);
                    }
                }else if(action.equals(getPackageName()+C.RUN_SETTINGS_LOCKER)){
                    setLaunchType(DataAppsSelection.ACTION_SETTINGS,DataAppsSelection.TRIGGER_SETTINGS);
                }else if(action.equals(getPackageName()+C.KILL_LOCKER)){
                    handleKillLocker();
                }else if(action.equals(getPackageName()+C.SIMPLE_UNLOCK_CALL)){
                    int actionType = intent.getIntExtra(C.SIMPLE_UNLOCK_CALL,-1);
                    callLockerAction(actionType);
                }else if(action.equals(getPackageName()+C.BOTTOM_BAR_CALL)){
                    bringToFront(lytBottomBar);
                    int actionType = intent.getIntExtra(C.BOTTOM_BAR_CALL,-1);
                    bottomBarManager.setBottomTouch(actionType);
                }else if(action.equals(getPackageName()+C.OPEN_PIN)){
                    //checkLayout(View.INVISIBLE,"OPEN_PIN");
                }else if(action.equals(getPackageName()+C.CLOSE_PIN)){
                    handleClosePIN();
                }else if(action.equals(getPackageName()+C.VERIFY_GOOGLE_PIN)){
                    checkLayout(View.VISIBLE,"VERIFY_GOOGLE_PIN");
                    verifyAccount();
                }else if(action.equals(getPackageName()+C.SHOW_VIEW)){
                    checkLayout(View.VISIBLE,"SHOW_VIEW");
                }else if(action.equals(getPackageName()+C.PIN_UNLOCK)){
                    mSaveData.setLockerActivationTime(System.currentTimeMillis());//reset PIN delay once PIN verified
                    ///checkLayout(View.VISIBLE);// dont put this
                    setLaunchType(false,savedLaunchAction,savedShortcutId);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };
    //runnable
    private Runnable runTapping = new Runnable(){
        @Override
        public void run(){
            if(isMultiTouch){
                if(TOUCH_COUNT>2){
                    setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.TRIPLE_TAP2);
                }else{
                    if(TOUCH_COUNT>1){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.DOUBLE_TAP2);
                    }
                }
            }else{
                if(TOUCH_COUNT>2){
                    setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.TRIPLE_TAP1);
                }else{
                    if(TOUCH_COUNT>1){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.DOUBLE_TAP1);
                    }
                }
            }
            TOUCH_COUNT = 0;
            isTapping = false;
            isMultiTouch = false;
        }
    };
    private Runnable setFullScreen = new Runnable(){
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void run(){
            try{
                if(C.isAndroid(Build.VERSION_CODES.KITKAT)){//		 && hasNaviKey()){//
                    int what = getWindow().getDecorView().getSystemUiVisibility();
                    what = what |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;//just add it to be safe
                    if(cBoxDisplayFullScreen){
                        what = what | View.SYSTEM_UI_FLAG_FULLSCREEN;
                    }
                    getWindow().getDecorView().setSystemUiVisibility(what);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };
    private Runnable resetAnimation = new Runnable(){
        @Override
        public void run(){
            isLayoutAnmationRunning = false;
        }
    };
    private Runnable runTaskerCenterIconPressed = new Runnable(){
        @Override
        public void run(){
            if(cBoxTaskerShortcutCount){
                Intent i = new Intent("com.ccs.lockscreen_pro.mTASKER_ACTION_CENTER_ICON_PRESSED");
                sendBroadcast(i);
            }
        }
    };
    private Runnable screenTimeout = new Runnable(){
        @Override
        public void run(){
            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT,intTimeoutVal);
        }
    };
    private Runnable turnScreenOff = new Runnable(){
        @Override
        public void run(){
            deviceAdminHandler.turnScreenOff();
        }
    };

    private class TabActionHandler{
        private void onReceivePagerTab(final Intent intent){
            final int cldAction = intent.getIntExtra(C.TAB_ACTION,0);//0 = no action
            switch(cldAction){
                case C.LAUNCH_GMAIL:
                    setLaunchType(DataAppsSelection.ACTION_DEFAULT,DataAppsSelection.TRIGGER_GMAIL);
                    break;
                case C.LAUNCH_SMS:
                    setLaunchType(DataAppsSelection.ACTION_DEFAULT,DataAppsSelection.TRIGGER_MISS_SMS);
                    break;
                case C.LAUNCH_CALLLOG:
                    setLaunchType(DataAppsSelection.ACTION_DEFAULT,DataAppsSelection.TRIGGER_MISS_CALL);
                    break;
                case C.LAUNCH_CALENDAR:
                    setLaunchType(DataAppsSelection.ACTION_DEFAULT,DataAppsSelection.TRIGGER_CALENDAR);
                    break;
                case C.LAUNCH_RSS:
                    setLaunchType(DataAppsSelection.ACTION_DEFAULT,DataAppsSelection.TRIGGER_RSS);
                    break;
            }
        }
        private void onReceivePagerClick(final Intent intent){
            final int cldAction = intent.getIntExtra(C.SINGE_CLICK_ACTION,0);//0 = no action
            switch(cldAction){
                case C.LAUNCH_SINGLE_NOTICE:
                    setLaunchType(DataAppsSelection.ACTION_DEFAULT,DataAppsSelection.TRIGGER_GMAIL);
                    break;
                case C.LAUNCH_SINGLE_SMS:
                    smsId = intent.getStringExtra(C.SMS_ID);
                    setLaunchType(DataAppsSelection.ACTION_SMS_DETAIL,-1);
                    break;
                case C.LAUNCH_SINGLE_CALLLOG:
                    callId = intent.getStringExtra(C.CONTACT_ID);
                    phoneNo = intent.getStringExtra(C.PHONE_NO);
                    setLaunchType(DataAppsSelection.ACTION_CALLLOG_DETAIL,-1);
                    break;
                case C.LAUNCH_SINGLE_EVENT:
                    eventID = intent.getIntExtra(C.EVENT_ID,1);
                    setLaunchType(DataAppsSelection.ACTION_EVENT_DETAIL,-1);
                    break;
                case C.LAUNCH_SINGLE_RSS:
                    rssLink = intent.getStringExtra(C.RSS_LINK);
                    setLaunchType(DataAppsSelection.ACTION_RSS_DETAIL,-1);
                    break;
            }
        }
    }
    private void onReceiveSystemDialog(Intent intent){
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"; //Long Press Home
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"; //Home Button
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions"; //Power Button
        try{
            if(intent.hasExtra(SYSTEM_DIALOG_REASON_KEY)){
                final String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                switch(reason){
                    case SYSTEM_DIALOG_REASON_RECENT_APPS:
                        isRecentAppsClick = true;
                        if(prefs.getBoolean("cBoxBlockRecentApps",false)){
                            resetActionUnlock();
                            //sendBroadcast(new Intent(getPackageName()+C.RUN_BLOCK_APP));
                        }
                        //else{
                            //phone rotation
                        //}
                        break;
                    case SYSTEM_DIALOG_REASON_HOME_KEY:
                        isHomeClick = true;
                        if(!isLockerDefaultHome()){
                            //final Intent i = new Intent(getPackageName()+C.RUN_BLOCK_APP);
                            //sendBroadcast(i);
                        //}else{
                            //home key not default
                            final Intent i = new Intent(getPackageName()+C.UNEXPECTED_UNLOCK);
                            sendBroadcast(i);

                            boolean cBoxHomeKeyBypassMsg = prefs.getBoolean("cBoxHomeKeyBypassMsg",true);
                            if(!cBoxHomeKeyBypassMsg){
                                onFinish(false,0,"SYSTEM_DIALOG_REASON_HOME_KEY");
                            }
                        }
                        break;
                    case SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS:
                        //mLocker.writeToFile("Locker>onReceiveSystemDialog: SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS");
                        break;
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    //utils
    private boolean isWidgetBlock(final String pkgName){
        try{
            if(shortcutData==null){
                shortcutData = new DataAppsSelection(this);
            }
            final List<InfoAppsSelection> list = shortcutData.getApps(DataAppsSelection.APP_TYPE_WIDGET_BLOCK);
            if(list!=null){
                for(InfoAppsSelection item : list){
                    if(item.getAppPkg().equals(pkgName)){
                        return false;
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return true;
    }
    private boolean isFileAvailable(String dir){
        final File file = new File(C.EXT_STORAGE_DIR+dir);
        return file.getAbsoluteFile().exists();
    }
    private boolean isSystemApp(){
        try{
            final ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),0);
            final String currentDir = appInfo.sourceDir;
            if(currentDir!=null && currentDir.contains("/system/")){
                return true;
            }
        }catch(NameNotFoundException e){
            mLocker.saveErrorLog(null,e);
        }
        return false;
    }
    private boolean isLockerDefaultHome(){
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        final String homeAppPkgName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        final String homeAppClassName = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name;

        if(homeAppPkgName.equals(getPackageName())){
            return true;
        }else{
            if(homeAppPkgName.equals("android") ||
                homeAppPkgName.equals("org.cyanogenmod.resolver") ||
                homeAppPkgName.equals("com.huawei.android.internal.app")){
                // SettingsSecurity,SettingsHomeKey,ServiceMain,Locker classes all need to change together!!!!!
                // do nothing
            }else{
                editor.putString(P.STR_HOME_LAUNCHER_PKG_NAME,homeAppPkgName);
                editor.putString(P.STR_HOME_LAUNCHER_CLASS_NAME,homeAppClassName);
                editor.commit();
            }
        }
        return false;
    }
    private boolean isPinDelayDue(){
        if(mSaveData.isOnReboot()){
            return true;
        }
        long lockTime = mSaveData.getLockerActivationTime();
        long now = System.currentTimeMillis();
        return (now-lockTime)>intDelayPin;
    }
    private boolean isPinRequired(final boolean requirePIN,final int actionType,final int shortcutId){
        return requirePIN ||
            actionType==DataAppsSelection.ACTION_CALL ||
            actionType==DataAppsSelection.ACTION_CAM ||
            actionType==DataAppsSelection.ACTION_UNLOCK ||
            actionType==DataAppsSelection.ACTION_SETTINGS ||
            actionType==DataAppsSelection.ACTION_WIDGET_CLICK ||
            actionType==DataAppsSelection.ACTION_SMS_DETAIL ||
            actionType==DataAppsSelection.ACTION_CALLLOG_DETAIL ||
            actionType==DataAppsSelection.ACTION_EVENT_DETAIL ||
            actionType==DataAppsSelection.ACTION_RSS_DETAIL ||
            actionType==DataAppsSelection.ACTION_NOTICES ||
            shortcutId==DataAppsSelection.TRIGGER_GMAIL ||
            shortcutId==DataAppsSelection.TRIGGER_MISS_SMS ||
            shortcutId==DataAppsSelection.TRIGGER_MISS_CALL ||
            shortcutId==DataAppsSelection.TRIGGER_CALENDAR ||
            shortcutId==DataAppsSelection.TRIGGER_RSS;
    }
    private boolean isSystemLockEnabled(){
        try{
            final KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            if(km.isKeyguardLocked()){
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>isSystemLockEnabled: "+true);
                return true;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return false;
    }
    private boolean isSimcardLockEnabled(){
        final TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        int state = manager.getSimState();
        if(state==TelephonyManager.SIM_STATE_PIN_REQUIRED){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>isSimcardLockEnabled>SIM_STATE_PIN_REQUIRED: "+true);
            return true;
        }else{
            if(state==TelephonyManager.SIM_STATE_PUK_REQUIRED){
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>isSimcardLockEnabled>SIM_STATE_PUK_REQUIRED: "+true);
                return true;
            }
        }
        return false;
    }
    private void handleFlashLight(boolean turnOn){
        try{
            if(turnOn){
                if(mCam!=null){
                    mCam.stopPreview();
                    mCam.release();
                    mCam = null;
                    enableFlashLight = false;
                    if(lytRingUnlock!=null){
                        lytRingUnlock.setFlashLightIcon(View.INVISIBLE);
                    }
                }else{
                    //CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    //manager.openCamera(cameraId, callback, handler);

                    mCam = Camera.open();
                    Parameters p = mCam.getParameters();
                    p.setFlashMode(Parameters.FLASH_MODE_TORCH);
                    mCam.setParameters(p);
                    mCam.startPreview();
                    enableFlashLight = true;
                }
            }else{
                if(mCam!=null){
                    mCam.stopPreview();
                    mCam.release();
                    mCam = null;
                    enableFlashLight = false;
                    if(lytRingUnlock!=null){
                        lytRingUnlock.setFlashLightIcon(View.INVISIBLE);
                    }
                }
            }
        }catch(Exception e){
            Toast.makeText(Locker.this,"Flashlight not support in Android Lollipop, i am working hard to fix it",Toast.LENGTH_LONG).show();
            mLocker.saveErrorLog(null,e);
        }
    }
    private void handleKillLocker(){//tasker
        if(mSaveData.isKillLocker()){
            mSaveData.setKillLocker(false);
            loadScreenTimeOut(intScreenSystemTimeOut,0,"loadKillLocker");
            onFinish(false,getUnlockAnim(),"loadKillLocker");
        }
    }
    private void setDismissKeyguard(){
        getWindow().addFlags(
            //WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    }
    private void initialAlertWindow(){
        if(windowPr==null){
            windowPr = new WindowManager.LayoutParams();
            //windowPr.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
            windowPr.format = PixelFormat.TRANSPARENT;//PixelFormat.RGBA_4444; for testing background color
            if(C.isAndroid(Build.VERSION_CODES.O)){
                windowPr.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                //windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }

            windowPr.width = 1;
            windowPr.height = 1;
            windowPr.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                // full screen covering statusbar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowPr.gravity = Gravity.TOP;
        }
    }
    private void resetActionUnlock(){
        savedLaunchAction = DataAppsSelection.ACTION_UNLOCK;
        savedShortcutId = -1;
    }
    //verify account
    private void verifyAccount(){
        try{
            if(!C.isInternetConnected(this)){
                new MyAlertDialog(this).simpleMsg(getString(R.string.required_internet),new OnDialogListener(){
                    @Override
                    public void onDialogClickListener(int dialogType,int result){
                        if(result==MyAlertDialog.OK){
                            openMobileData();
                        }
                    }
                });
                return;
            }
            AccountManager accountManager = (AccountManager)getSystemService(Context.ACCOUNT_SERVICE);
            Account[] accounts = accountManager.getAccounts();
            for(Account item : accounts){
                //Log.e("Account",""+item.name+"/"+item.type);
                if(item.type.equals("com.google")){
                    isLockerShortcut = true;
                    Toast.makeText(Locker.this,R.string.checking_connection,Toast.LENGTH_SHORT).show();
                    accountManager.confirmCredentials(item,null,this,accCallback,handler);
                    break;
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        //GoogleAccountCredential cred = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(SettingsSecuritySelfie.SCOPE_GMAIL_FULL));
    }
    private void openMobileData(){
        isLockerShortcut = true;
        Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
        //ComponentName cn = new ComponentName("com.android.phone","com.android.phone.Settings");
        //intent.setComponent(cn);
        //startActivity(intent);
        this.startActivityForResult(intent,MOBILE_DATA_RESULT);
    }
    //actions
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setLockerTheme(){
        //special loading for wallpaper
        getWallpaperType = prefs.getInt("setWallpaperType",C.WALLPAPER_HOME);
        cBoxDisplayFullScreen = prefs.getBoolean("cBoxDisplayFullScreen",false);

        if(getWallpaperType==C.WALLPAPER_CUSTOM && !isFileAvailable(C.WALL_PAPER_01)){
            getWallpaperType = C.WALLPAPER_HOME;
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>onCreate: wallpaper file not found");
        }
        setTheme(R.style.Theme_LockerHomeWallpaperTransparentStatusBar);
        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
    private void setStatusbarClock(boolean enable,boolean hideClockAndNaviKeys){
        if(isSystemApp()){
            boolean cBoxBlockStatusbar = prefs.getBoolean("cBoxBlockStatusbar",false);
            boolean cBoxHideStatusbarClock = prefs.getBoolean("cBoxHideStatusbarClock",false);
            boolean cBoxHideStatusbarIcon = prefs.getBoolean("cBoxHideStatusbarIcon",false);
            boolean cBoxHideStatusbarAlert = prefs.getBoolean("cBoxHideStatusbarAlert",false);
            boolean cBoxHideNavigationKeys = prefs.getBoolean("cBoxHideNavigationKeys",false);
            int what = STATUS_BAR_DISABLE_NONE;
            if(enable){
                what = STATUS_BAR_TRANSLUCENT;
                if(cBoxBlockStatusbar){
                    what = what | STATUS_BAR_DISABLE_EXPAND;
                }
                if(cBoxHideStatusbarClock && hideClockAndNaviKeys){
                    what = what | STATUS_BAR_DISABLE_CLOCK;
                }
                if(cBoxHideStatusbarIcon){
                    what = what | STATUS_BAR_DISABLE_NOTIFICATION_ICONS;
                }
                if(cBoxHideStatusbarAlert){
                    what = what | STATUS_BAR_DISABLE_NOTIFICATION_ALERTS;
                }
                if(cBoxHideNavigationKeys && hideClockAndNaviKeys){
                    what = what |
                        //NAVIGATION_BAR_TRANSIENT|
                        //NAVIGATION_BAR_TRANSLUCENT|
                        STATUS_BAR_DISABLE_HOME |
                        STATUS_BAR_DISABLE_BACK |
                        STATUS_BAR_DISABLE_RECENT |
                        STATUS_BAR_DISABLE_SEARCH;
                }
            }
            try{
                final Object service = getSystemService("statusbar");
                final Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method expand;
                if(service!=null){
                    expand = statusbarManager.getMethod("disable",int.class);
                    expand.setAccessible(true);
                    expand.invoke(service,what);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    }
    private void setUnlockSound(){
        final Intent i = new Intent(getPackageName()+C.PLAY_SOUND);
        sendBroadcast(i);
    }
    private Drawable setRoundColor(int type,String strColor,int size){
        try{
            GradientDrawable gd;
            if(type==C.BLINKING_TYPE_1){
                gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setStroke(2,Color.parseColor("#"+strColor));
            }else{
                if(type==C.BLINKING_TYPE_2){
                    int color01 = Color.parseColor("#"+strColor);
                    int color02 = Color.parseColor("#88"+strColor);
                    int color03 = Color.parseColor("#55"+strColor);
                    int color04 = Color.parseColor("#22"+strColor);

                    gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,0});
                    gd.setShape(GradientDrawable.OVAL);
                    gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                    gd.setGradientRadius(size);
                }else{
                    if(type==C.BLINKING_TYPE_3){
                        int color01 = Color.parseColor("#00"+strColor);
                        int color02 = Color.parseColor("#11"+strColor);
                        int color03 = Color.parseColor("#33"+strColor);
                        int color04 = Color.parseColor("#"+strColor);

                        gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,0});
                        gd.setShape(GradientDrawable.OVAL);
                        gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                        gd.setGradientRadius(size);
                    }else{
                        if(type==C.BLINKING_TYPE_4){
                            int color01 = Color.parseColor("#"+strColor);

                            gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{0,0});//transparent
                            gd.setShape(GradientDrawable.OVAL);
                            gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                            gd.setGradientRadius(size);
                            gd.setStroke(8,color01,20,40);
                        }else{
                            int color01 = Color.parseColor("#"+strColor);
                            int color02 = Color.parseColor("#22"+strColor);

                            gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,color02,color02,color02,color01});
                            gd.setShape(GradientDrawable.OVAL);
                            gd.setGradientType(GradientDrawable.SWEEP_GRADIENT);
                            gd.setGradientRadius(size);
                        }
                    }
                }
            }
            return gd;
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return null;
    }
    private void setAnimationRotation(final View v){
        float dest = 360;
        if(v.getRotation()==360){
            dest = 0;
        }
        final ObjectAnimator animation = ObjectAnimator.ofFloat(v,"rotation",dest);
        animation.setDuration(PROFILE_ANIMATION);
        animation.start();
    }
    private void setPortrait(final boolean bln){
        if(bln){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }
    }
    private void setVibration(final boolean bln){
        //final Vibrator vb = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if(bln){
            //vb.vibrate(vibraVal);
            setHapticFeedback();
        }
    }
    private void setHapticFeedback(){
        lytMainActivity.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }
    private void setLaunchType(int actionType,final int shortcutId){
        setLaunchType(cBoxEnablePassUnlock,actionType,shortcutId);
    }
    private void setLaunchType(final boolean enablePassUnlock,int actionType,final int shortcutId){
        //refreshNoticeLayout(NotificationView.ON_LAUNCH_ACTION);
        isLockerShortcut = true;
        if(cBoxTaskerShortcutCount){
            Intent i = new Intent("com.ccs.lockscreen_pro.mTASKER_ACTION");
            i.putExtra("shortcutAppCount",intAppShortcutRunCount);
            i.putExtra("shortcutLaunchType",actionType);
            i.putExtra("shortcutAppIndex",shortcutId);
            sendBroadcast(i);
            intAppShortcutRunCount++;
        }
        //
        int profile = checkProfile(intLockerWidgetProfile);
        boolean requirePIN = false;
        if(actionType==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP || actionType==DataAppsSelection.ACTION_FAVORITE_SHORTCUT){
            actionType = getShortcutAction(shortcutId,profile);
            final int PIN = shortcutData.getShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,profile);
            requirePIN = (PIN==DataAppsSelection.PIN_REQUIRED);

            switch(shortcutId){
                case DataAppsSelection.GESTURE_UP:
                case DataAppsSelection.GESTURE_DOWN:
                case DataAppsSelection.GESTURE_LEFT:
                case DataAppsSelection.GESTURE_RIGHT:
                case DataAppsSelection.GESTURE_UP_2:
                case DataAppsSelection.GESTURE_DOWN_2:
                case DataAppsSelection.GESTURE_LEFT_2:
                case DataAppsSelection.GESTURE_RIGHT_2:
                case DataAppsSelection.DOUBLE_TAP1:
                case DataAppsSelection.DOUBLE_TAP2:
                case DataAppsSelection.TRIPLE_TAP1:
                case DataAppsSelection.TRIPLE_TAP2:
                    if(actionType==DataAppsSelection.ACTION_DEFAULT){
                        return;
                    }else{
                        if(actionType==DataAppsSelection.ACTION_OFF_SCREEN){
                            if(deviceAdminHandler.isDeviceAdminActivated()){
                                setTurnScreenOff(true,0,"setLaunchType");
                            }else{
                                deviceAdminHandler.launchDeviceAdmin(this,DEVICE_ADMIN_SET);
                            }
                            return;
                        }else{
                            if(actionType==DataAppsSelection.ACTION_SOUND_MODE){
                                setSoundMode();
                                return;
                            }else{
                                if(actionType==DataAppsSelection.ACTION_DIRECT_SMS ||
                                    actionType==DataAppsSelection.ACTION_DIRECT_CALL ||
                                    actionType==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP ||
                                    actionType==DataAppsSelection.ACTION_FAVORITE_SHORTCUT ||
                                    actionType==DataAppsSelection.ACTION_CALL ||
                                    actionType==DataAppsSelection.ACTION_CAM ||
                                    actionType==DataAppsSelection.ACTION_UNLOCK){
                                    setVibration(cBoxEnableVibration);
                                }
                            }
                        }
                    }
            }
        }
        if(shortcutId==DataAppsSelection.SHORTCUT_APP_01 && actionType==DataAppsSelection.ACTION_DEFAULT){
            actionType = DataAppsSelection.ACTION_UNLOCK;
        }
        if(shortcutId==DataAppsSelection.SHORTCUT_APP_02 && actionType==DataAppsSelection.ACTION_DEFAULT){
            handleFlashLight(true);
            return;// launch default flashlight
        }
        final LockerAction action = new LockerAction(this);
        boolean isProfilePinEnable = profileHandler.isProfilePinEnable(intLockerWidgetProfile);
        if(!isProfilePinEnable){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setLaunchType>isProfilePinEnable>no PIN required: "+intLockerWidgetProfile);
        }
        if(enablePassUnlock &&
            securityType!=C.SECURITY_UNLOCK_NONE &&
            !isSystemLockEnabled() &&
            !isSimcardLockEnabled() &&
            isPinDelayDue() &&
            isProfilePinEnable){//dont use inner field "profile"

            if(isPinRequired(requirePIN,actionType,shortcutId)){
                this.savedLaunchAction = actionType;
                this.savedShortcutId = shortcutId;
                if(!ServiceMain.isServiceMainRunning()){
                    startService(new Intent(this,ServiceMain.class));
                }
                if(intLockStyle==C.FINGERPRINT_UNLOCK){
                    fingerprintManager.setFingerprint(true,actionType,shortcutId,true); //stop this
                }else{
                    handleOpenPIN();
                }
            }else{
                switch(actionType){
                    case DataAppsSelection.ACTION_DEFAULT:
                    case DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP:
                    case DataAppsSelection.ACTION_FAVORITE_SHORTCUT:
                    case DataAppsSelection.ACTION_DIRECT_SMS:
                    case DataAppsSelection.ACTION_DIRECT_CALL:
                    case DataAppsSelection.ACTION_EXPAND_STATUS_BAR:
                    case DataAppsSelection.ACTION_CALL:
                    case DataAppsSelection.ACTION_CAM:
                        action.launchAction(shortcutId,profile);
                        break;
                }
            }
        }else{
            switch(actionType){
                case DataAppsSelection.ACTION_DEFAULT:
                case DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP:
                case DataAppsSelection.ACTION_FAVORITE_SHORTCUT:
                case DataAppsSelection.ACTION_DIRECT_SMS:
                case DataAppsSelection.ACTION_DIRECT_CALL:
                case DataAppsSelection.ACTION_EXPAND_STATUS_BAR:
                case DataAppsSelection.ACTION_SETTINGS:
                case DataAppsSelection.ACTION_CALL:
                case DataAppsSelection.ACTION_CAM:
                    action.launchAction(shortcutId,profile);
                    onFinish("ACTION_DEFAULT");
                    //new CheckUnlock().execute();// check if any unlock shortcut, if no, unlock locker
                    break;
                case DataAppsSelection.ACTION_SMS_DETAIL:
                    action.launchSmsDetail(smsId);
                    onFinish("ACTION_SMS_DETAIL");
                    break;
                case DataAppsSelection.ACTION_CALLLOG_DETAIL:
                    action.launchCallLogDetail(callId,phoneNo);
                    onFinish("ACTION_CALLLOG_DETAIL");
                    break;
                case DataAppsSelection.ACTION_EVENT_DETAIL:
                    action.launchEventDetail(eventID);
                    onFinish("ACTION_EVENT_DETAIL");
                    break;
                case DataAppsSelection.ACTION_RSS_DETAIL:
                    action.launchRssDetail(rssLink);
                    onFinish("ACTION_RSS_DETAIL");
                    break;
                case DataAppsSelection.ACTION_NOTICES:
                    openNotice(noticePkgName,noticeImgId);
                    onFinish("ACTION_NOTICES");
                    break;
                case DataAppsSelection.ACTION_WIDGET_CLICK:
                    checkLayout(View.VISIBLE,"ACTION_WIDGET_CLICK");
                    Toast t = Toast.makeText(this,getString(R.string.locker_widget_click_enabled),Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.BOTTOM,0,0);
                    t.show();
                    isWidgetClickEnabled = true;
                    break;
                case DataAppsSelection.ACTION_UNLOCK:
                    switch(shortcutId){
                        case DataAppsSelection.GESTURE_UP:
                        case DataAppsSelection.GESTURE_DOWN:
                        case DataAppsSelection.GESTURE_LEFT:
                        case DataAppsSelection.GESTURE_RIGHT:
                        case DataAppsSelection.GESTURE_UP_2:
                        case DataAppsSelection.GESTURE_DOWN_2:
                        case DataAppsSelection.GESTURE_LEFT_2:
                        case DataAppsSelection.GESTURE_RIGHT_2:
                            onGestureUnlock(shortcutId);
                            break;
                        default:
                            onFinish(false,getUnlockAnim(),"ACTION_UNLOCK");
                            break;
                    }
                    break;
            }
        }
    }
    private void onFinish(final String log){
        onFinish(cBoxReturnLS,getUnlockAnim(),log);
    }
    private void onFinish(final boolean enableReturnLS,final int anim,final String log){
        try{
            if(!enableReturnLS){
                mSaveData.setRssUpdated(false);
                //OnReboot
                if(mSaveData.isOnReboot()){
                    launchDefaultLauncher();
                    mSaveData.setOnReboot(false);
                }else{
                    isLockerDefaultHome();//get other launcher name as well
                }
                setUnlockSound();

                boolean cBoxSecuritySelfie = prefs.getBoolean("cBoxSecuritySelfie",false);
                boolean isNewSecuritySelfieTaken = prefs.getBoolean("isNewSecuritySelfieTaken",false);
                if(cBoxSecuritySelfie && isNewSecuritySelfieTaken){
                    editor.putBoolean("isNewSecuritySelfieTaken",false);
                    editor.commit();
                    this.startActivity(new Intent(this,SecuritySelfie.class));
                }else{
                    if(cBoxUnlockToHome){
                        launchDefaultLauncher();
                    }
                }

                mSaveData.setLockerRunning(false);

                sendBroadcast(new Intent(getPackageName()+C.STOP_BLOCK_APP));
                sendBroadcast(new Intent(getPackageName()+C.FINISH_PIN));//if PIN is showing, close it

                finish();
                overridePendingTransition(0,anim);
                //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>onFinish: "+log);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void launchDefaultLauncher(){
        try{
            final String pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
            final String className = prefs.getString(P.STR_HOME_LAUNCHER_CLASS_NAME,"");

            if(pkgName.equals("")){
                final Intent i = new Intent(this,ListInstalledApps.class);
                i.putExtra(C.LIST_APPS_ID,C.LIST_LAUNCHER_RUN);
                startActivity(i);
            }else{
                final Intent i = new Intent(Intent.ACTION_MAIN);
                i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                i.setClassName(pkgName,className);
                startActivity(i);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
            //open home launcher lists if any errors
            final Intent i = new Intent(this,ListInstalledApps.class);
            i.putExtra(C.LIST_APPS_ID,C.LIST_LAUNCHER_RUN);
            startActivity(i);
        }
    }
    private void resetUnlockStyle(){
        if(!cBoxEnablePassUnlock){
            securityType = C.SECURITY_UNLOCK_NONE;
            editor.putInt(C.SECURITY_TYPE,securityType);
        }
        intLockStyle = C.SIMPLE_UNLOCK;
        editor.putInt("intLockStyle",intLockStyle);
        editor.putBoolean("cBoxEnableKeyboardTyping",true);
        editor.commit();

        if(imgFingerPrint!=null){
            imgFingerPrint.setImageResource(0);
        }

        intLockerWidgetProfile = -1;// must reset profile value
        loadProfile("resetUnlockStyle");
    }
    private void setSoundMode(){
        AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        if(audioManager.getRingerMode()==AudioManager.RINGER_MODE_NORMAL){
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Toast.makeText(this,getString(R.string.sound_mode_silent),Toast.LENGTH_SHORT).show();

            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setSoundMode: RINGER_MODE_SILENT");
        }else{
            if(audioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT){
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                Toast.makeText(this,getString(R.string.sound_mode_vibration),Toast.LENGTH_SHORT).show();

                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setSoundMode: RINGER_MODE_VIBRATE");
            }else{
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                Toast.makeText(this,getString(R.string.sound_mode_normal),Toast.LENGTH_SHORT).show();

                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setSoundMode: RINGER_MODE_NORMAL");
            }
        }
    }
    private void setViewBackground(int color){
        getWindow().getDecorView().setBackgroundColor(color);
    }
    private int checkProfile(int profile){
        if(shortcutData==null){
            shortcutData = new DataAppsSelection(this);
        }
        if(profile!=C.PROFILE_DEFAULT && shortcutData.isAllDefaultAction(profile)){
            profile = C.PROFILE_DEFAULT;
        }
        return profile;
    }
    private void handleOpenPIN(){
        if(C.isAndroid(Build.VERSION_CODES.O)){
            checkLayout(View.INVISIBLE,"OPEN_PIN");
            if(securityUnlockView==null){
                securityUnlockView = new SecurityUnlockView(this,securityType,false);
            }
            lytMainActivity.addView(securityUnlockView);
        }else{
            sendBroadcast(new Intent(getPackageName()+C.OPEN_PIN));
            checkLayout(View.INVISIBLE,"OPEN_PIN");
        }
    }
    private void handleClosePIN(){
        //checkLayout(View.VISIBLE);
        if(securityUnlockView!=null){
            lytMainActivity.removeView(securityUnlockView);
            securityUnlockView=null;
        }
    }
    private boolean handleMotionEvent(final MotionEvent m,CheckLongPressHelper widgetLongPressHelper,String pkgName){
        int MOVE_GAP = C.dpToPx(this,10);
        int EDGE_GAP = C.dpToPx(this,25);//default 15

        if(bottomBarManager!=null){
            bottomBarManager.setBottomBarHide();
        }
        if(isProxySensorOn){// cancel all touch event if proximity sensor is on
            return true;
        }
        if(isScreenOffTimerRunning){//dont waste cpu process
            setTurnScreenOff(false,0,"onTouchEvent");//cancel screen off once touch on locker
        }
        switch(m.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                refreshNoticeLayout(NotificationView.ON_TOUCH_DOWN);
                onTapping(true);

                is1stTouchValueContained = false;

                STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP

                if(STARTING_RAW_Y<EDGE_GAP){
                    isTopEdgeTouch = true;
                }else{
                    if(STARTING_RAW_Y>(intDisplayHeight-EDGE_GAP)){
                        isBottomEdgeTouch = true;
                    }else{
                        if(STARTING_RAW_X<EDGE_GAP){
                            isLeftEdgeTouch = true;
                        }else{
                            if(STARTING_RAW_X>(intDisplayWidth-EDGE_GAP)){
                                isRightEdgeTouch = true;
                            }else{
                                isTopEdgeTouch = false;
                                isBottomEdgeTouch = false;
                                isLeftEdgeTouch = false;
                                isRightEdgeTouch = false;
                            }
                        }
                    }
                }
                //Log.e("handleMotionEvent","STARTING_RAW_X: "+MOVE_GAP);

                if(widgetLongPressHelper!=null){
                    widgetLongPressHelper.postCheckForLongPress();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                isMultiTouch = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int gapX = (int)(STARTING_RAW_X-m.getRawX());
                int gapY = (int)(STARTING_RAW_Y-m.getRawY());
                //Log.e("handleMotionEvent","gapX: "+gapX+"/"+gapY+"/"+MOVE_GAP+"/"+m.getActionIndex());

                if(m.getActionIndex()==0){// get the 1st finger valuesX,Y
                    if(gapX>MOVE_GAP || gapX<(-MOVE_GAP)){
                        onTapping(false);//has moved
                    }
                    if(gapY>MOVE_GAP || gapY<(-MOVE_GAP)){
                        onTapping(false);//has moved
                    }
                }

                if(m.getPointerCount()>FINGER_COUNT){
                    FINGER_COUNT = m.getPointerCount();
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if(m.getActionIndex()==0){// get the 1st finger valuesX,Y
                    is1stTouchValueContained = true;
                    FINAL_X = (int)(m.getRawX()-STARTING_RAW_X);
                    FINAL_Y = (int)(m.getRawY()-STARTING_RAW_Y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(!is1stTouchValueContained){
                    FINAL_X = (int)(m.getRawX()-STARTING_RAW_X);
                    FINAL_Y = (int)(m.getRawY()-STARTING_RAW_Y);
                }

                boolean isGestureConsumed = onTouchUpGesture(FINGER_COUNT,FINAL_X,FINAL_Y);
                if(widgetLongPressHelper!=null){//widget click handling
                    widgetLongPressHelper.cancelLongPress();
                    if(isGestureConsumed){
                        return true;//widget click not allow
                    }
                    if(pkgName==null){
                        return true;//widget click not allow
                    }
                    if(isWidgetClickEnabled){
                        return false;//security verified
                    }
                    return isWidgetBlock(pkgName);
                    //need security verification else widget clickable
                }
                break;
        }
        return false;
    }
    //c floating, c notice
    private void handleCFloating(boolean enable){
        Intent i;
        if(enable){
            i = new Intent("com.ccs.floating_info.RESUME_WINDOWS");
        }else{
            i = new Intent("com.ccs.floating_info.PAUSE_WINDOWS");
        }
        i.putExtra("callFrom","CLocker");
        sendBroadcast(i);
    }
    private void handleCNotice(boolean enable){
        Intent i;
        if(enable){
            i = new Intent("com.ccs.notice.mNOTICE_RESUME_ALL");
        }else{
            i = new Intent("com.ccs.notice.mNOTICE_PAUSE_ALL");
        }
        i.putExtra("callFrom","CLocker");
        sendBroadcast(i);
    }
    //activity cycle
    @Override public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final Bundle b = getIntent().getExtras();
        if(b!=null){
            String extra = b.getString(C.EXTRA_INFO);
            if(extra!=null && extra.equals("onBackPressed")){
                //nothing
            }
        }
        mLocker = new MyCLocker(this);
        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>onCreate>instance: ");

        mSaveData = new SaveData(this);
        mSaveData.setLockerRunning(true);
        mSaveData.setKillLocker(false);
        profileHandler = new ProfileHandler(this,this);

        prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        editor = prefs.edit();

        setDismissKeyguard();
        setLockerTheme();
        setContentView(R.layout.locker);
        setStatusbarClock(true,true);
        initialAlertWindow();
        //do not include above in try and catch, let it FC if error
        try{
            //return display width/height NOT activity size
            intDisplayWidth = C.getRealDisplaySize(this).x;
            intDisplayHeight = C.getRealDisplaySize(this).y;
            //Log.e("Locker",intDisplayWidth+"/"+intDisplayHeight);
            if(C.isScreenPortrait(getBaseContext())){
                GESTURE_LENGTH = (int)(intDisplayWidth*0.3f);
            }else{
                GESTURE_LENGTH = (int)(intDisplayHeight*0.3f);
            }

            shortcutData = new DataAppsSelection(this);
            myWidgetHelper = new MyWidgetHelper(this);
            musicAction = new MusicActions(this);
            deviceAdminHandler = new DeviceAdminHandler(this);

            loadViewIds();
            loadSettings();
            loadInitialWallpaper();
            //loadWidgetLayout(C.WIDGET_PROFILE_DEFAULT);
            //loadDimensions();
            loadLicenseCheck();
            loadReceiver(true);
            requestNoticeBroadcast();
            loadUiChangeListener();
            //sendBroadcast(new Intent(getPackageName()+C.BLOCK_STATUSBAR));
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @Override protected void onStart(){
        super.onStart();
        handleKillLocker();
        handleCFloating(false);
        handleCNotice(false);
        if(!ServiceMain.isServiceMainRunning()){
            startService(new Intent(this,ServiceMain.class));
        }
        sendBroadcast(new Intent(getPackageName()+C.ON_LOCKER_START));
    }
    @Override protected void onResume(){
        super.onResume();
        try{
            inFrontground = true;
            isHomeClick = false;
            isRecentAppsClick = false;
            isLockerShortcut = false;
            isActivityOnFocus = true;
            //Log.e("Locker>onResume",
            //isScreenOn()+"/"+isLayoutAnmationPending+"/"+isLayoutAnmationRunning);
            if(C.isScreenOn(this,getWindowManager())){
                loadProxiSensorReceiver(true);
                loadFingerprintUnlock(true);//must run before loadProfile();
                loadProfile("onResume");
                refreshNoticeLayout(NotificationView.ON_RESUME);
                //=====
                setPortrait(cBoxPortrait);
                bottomBarManager.setBottomBarVisibility();
                bottomBarManager.loadRssData();
                //=====
                mSaveData.setScreenTimeoutType(C.TYPE_LOCKER_SCREEN_TIMEOUT);
                loadScreenTimeOut(mSaveData.isTurnScreenOff(),intScreenTimeOut,mSaveData.getScreenTimeoutNotice(),"onResume");
                setStatusbarClock(true,true);
                callWindowAlert(true);
                handler.post(setFullScreen);

                if(lytSimpleUnlock!=null){lytSimpleUnlock.resetImages();}
            }
            sendBroadcast(new Intent(getPackageName()+C.ON_LOCKER_RESUME));
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @Override protected void onPause(){
        try{
            inFrontground = false;

            callWindowAlert(false);
            isActivityOnFocus = false;
            setTurnScreenOff(false,0,"onPause");
            mSaveData.setTurnScreenOff(false); //added 24/9/16
            mSaveData.setScreenTimeoutType(C.TYPE_SYSTEM_SCREEN_TIMEOUT);
            loadScreenTimeOut(intScreenSystemTimeOut,0,"onPause");
            loadProxiSensorReceiver(false);
            loadFingerprintUnlock(false);
            //=====
            bottomBarManager.setRssSlideDisplay(false);
            bottomBarManager.setBottomBarHide();
            //=====
            setStatusbarClock(true,false);
            handleBlockedApps();
            sendBroadcast(new Intent(getPackageName()+C.ON_LOCKER_PAUSE));
            handleClosePIN();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        super.onPause();
    }
    @Override protected void onStop(){
        if(!C.isScreenOn(this,getWindowManager())){
            isWidgetClickEnabled = false;
            if(isLollipopNoticeViewShown){
                //lytWidgets.removeAllViews();
            }else{
                isLayoutAnmationPending = true;
                lytMainActivity.setVisibility(View.INVISIBLE);
            }
            if(cBoxCtrTvAnmation){
                setViewBackground(Color.BLACK);
            }
        }
        if(!ServiceMain.isServiceMainRunning()){
            startService(new Intent(this,ServiceMain.class));
        }
        sendBroadcast(new Intent(getPackageName()+C.ON_LOCKER_STOP));
        super.onStop();
    }
    @Override protected void onDestroy(){
        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>onDestroy>inFrontground: "+inFrontground); //8.2.5v
        try{
            //imgScreenWallPaper.setImageBitmap(null); //8.2.5v
            loadReceiver(false);
            setStatusbarClock(false,false);
            if(enableFlashLight){
                handleFlashLight(false);
            }
            //
            handleCFloating(true);
            handleCNotice(true);
            if(fingerprintManager!=null){
                fingerprintManager.clearFingerprintService(true);
            }
            //animSet.cancel();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        try{
            if(myWidgetHelper!=null){
                myWidgetHelper.closeAll();
                myWidgetHelper = null;
            }
            //if(profileHandler!=null){profileHandler=null;}
            if(musicAction!=null){
                musicAction.close();
                musicAction = null;
            }
            if(shortcutData!=null){
                shortcutData.close();
                shortcutData = null;
            }
            if(bottomBarManager!=null){
                bottomBarManager.closeAll();
            }//bottomBarManager=null;}
            //if(lytRingUnlock!=null){lytRingUnlock = null;} //8.2.5v
            //if(lytSimpleUnlock!=null){lytSimpleUnlock = null;} //8.2.5v
            //if(lytLockSide!=null){lytLockSide = null;} //8.2.5v
            if(mLocker!=null){
                mLocker.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        //System.gc();
        super.onDestroy();
    }
    //activity override
    @Override public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case DEVICE_ADMIN_SET:
                    if(resultCode==Activity.RESULT_OK){
                        editor.putBoolean("cBoxDoubleTabOffScreen",true);
                        editor.commit();
                        deviceAdminHandler.turnScreenOff();
                    }
                    break;
                case MOBILE_DATA_RESULT:
                    Toast.makeText(Locker.this,R.string.checking_connection,Toast.LENGTH_SHORT).show();
                    if(C.isInternetConnected(Locker.this)){
                        verifyAccount();
                    }else{
                        handler.postDelayed(new Runnable(){
                            @Override
                            public void run(){
                                if(C.isInternetConnected(Locker.this)){
                                    verifyAccount();
                                }else{
                                    Toast.makeText(Locker.this,R.string.network_error,Toast.LENGTH_SHORT).show();
                                }
                            }
                        },3000);//dont be too short
                    }
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    @Override public boolean onKeyDown(int keyCode,KeyEvent event){
        try{
            Log.e("Locker>onKeyDown","keyCode: "+keyCode);
            if(keyCode==KeyEvent.KEYCODE_HOME){
                //Log.e("Locker>onKeyDown","KEYCODE_HOME");
                return true;
            }else{
                if(keyCode==KeyEvent.KEYCODE_HEADSETHOOK || keyCode==KeyEvent.KEYCODE_INSERT){
                    return super.onKeyDown(keyCode,event);
                }else{
                    if(keyCode==KeyEvent.KEYCODE_VOLUME_UP ||
                        keyCode==KeyEvent.KEYCODE_VOLUME_DOWN ||
                        keyCode==KeyEvent.KEYCODE_MENU ||
                        keyCode==KeyEvent.KEYCODE_BACK){
                        event.startTracking();
                        if(event.getRepeatCount()==1){
                            longPress = true;
                        }
                        int profile = checkProfile(intLockerWidgetProfile);
                        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
                            int action = getShortcutAction(DataAppsSelection.VOLUME_UP,profile);
                            if(action==DataAppsSelection.ACTION_DEFAULT){
                                return super.onKeyDown(keyCode,event);
                            }
                        }
                        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
                            int action = getShortcutAction(DataAppsSelection.VOLUME_UP,profile);
                            if(action==DataAppsSelection.ACTION_DEFAULT){
                                return super.onKeyDown(keyCode,event);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return true;
    }
    @Override public boolean onKeyLongPress(int keyCode,KeyEvent event){
        try{
            if(keyCode==KeyEvent.KEYCODE_BACK){
                if(cBoxEnablePassUnlock){
                    resetActionUnlock();
                    handleOpenPIN();
                }
            }
            if(longPress){
                if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
                    if(musicAction.isMusicPlaying()){
                        setVibration(cBoxEnableVibration);
                        musicAction.musicNext();
                    }
                }else{
                    if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
                        if(musicAction.isMusicPlaying()){
                            setVibration(cBoxEnableVibration);
                            musicAction.musicBack();
                        }
                    }else{
                        if(keyCode==KeyEvent.KEYCODE_MENU){
                        }
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return true;
    }
    @Override public boolean onKeyUp(int keyCode,KeyEvent event){
        try{
            if(longPress){
                longPress = false;
            }else{
                if(keyCode==KeyEvent.KEYCODE_HEADSETHOOK || keyCode==KeyEvent.KEYCODE_INSERT){
                    return super.onKeyUp(keyCode,event);
                }else{
                    if(enableFlashLight){
                        handleFlashLight(false);
                        if(cBoxReturnLS){
                            return true;
                        }
                    }else{
                        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
                            int profile = checkProfile(intLockerWidgetProfile);
                            int action = getShortcutAction(DataAppsSelection.VOLUME_UP,profile);
                            if(musicAction.isMusicPlaying()){
                                if(action==DataAppsSelection.ACTION_DEFAULT){
                                    return super.onKeyUp(keyCode,event);
                                }else{
                                    bottomBarManager.setVolume(DataAppsSelection.VOLUME_UP);
                                    return true;
                                }
                            }else{
                                if(action==DataAppsSelection.ACTION_SOUND_MODE){
                                    setSoundMode();
                                    return true;
                                }else{
                                    return setVolumeRocker(DataAppsSelection.VOLUME_UP);
                                }
                            }
                        }else{
                            if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
                                int profile = checkProfile(intLockerWidgetProfile);
                                int action = getShortcutAction(DataAppsSelection.VOLUME_DOWN,profile);
                                if(musicAction.isMusicPlaying()){
                                    if(action==DataAppsSelection.ACTION_DEFAULT){
                                        return super.onKeyUp(keyCode,event);
                                    }else{
                                        bottomBarManager.setVolume(DataAppsSelection.VOLUME_DOWN);
                                        return true;
                                    }
                                }else{
                                    if(action==DataAppsSelection.ACTION_SOUND_MODE){
                                        setSoundMode();
                                        return true;
                                    }else{
                                        return setVolumeRocker(DataAppsSelection.VOLUME_DOWN);
                                    }
                                }
                            }else{
                                if(keyCode==KeyEvent.KEYCODE_BACK){
                                    isHomeClick = false;
                                    isRecentAppsClick = false;
                                    isLockerShortcut = false;
                                    refreshNoticeLayout(NotificationView.ON_KEY_BACK);
                                    bottomBarManager.setBottomBarHide();
                                    sendBroadcast(new Intent(getPackageName()+C.FINISH_PIN));
                                    sendBroadcast(new Intent(getPackageName()+C.CANCEL_SCREEN_OFF));

                                    if(cBoxBackKeyUnlock){
                                        setVibration(cBoxEnableVibration);
                                        setLaunchType(DataAppsSelection.ACTION_UNLOCK,-1);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return super.onKeyUp(keyCode,event);
    }
    //custom interface
    @Override public boolean onTouchEvent(MotionEvent m){
        return handleMotionEvent(m,null,null);
    }
    @Override public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            handler.post(setFullScreen);
        }else{
            if(isActivityOnFocus && !isProxySensorOn){
                //setTurnScreenOff(false,0,"onWindowFocusChanged");//if pulling down statusbar or others..
            }
        }
    }
    @Override public boolean onWidgetTouchEvent(View v,MotionEvent m,CheckLongPressHelper widgetLongPressHelper,String pkgName){
        isLockerShortcut = true;
        return handleMotionEvent(m,widgetLongPressHelper,pkgName);
    }
    @Override public boolean callKeyEvent(KeyEvent event){
        return dispatchKeyEvent(event);
    }
    @Override public boolean callTouchEvent(MotionEvent m){
        return onTouchEvent(m);
    }
    @Override public void callWindowAlert(boolean add){
        if(add){
            addBlockHomeKey();
        }else{
            removeBlockHomeKey();
        }
    }
    @Override public void callLockerAction(int actionType){
        if(actionType==DataAppsSelection.TRIGGER_CALL){
            boolean cBoxCall = prefs.getBoolean("simpleUnlockRequiredPINCall",true);
            if(cBoxCall){
                setVibration(cBoxEnableVibration);
                setLaunchType(DataAppsSelection.ACTION_CALL,actionType);
            }else{
                setVibration(cBoxEnableVibration);
                setLaunchType(false,DataAppsSelection.ACTION_CALL,actionType);
            }
        }else{
            if(actionType==DataAppsSelection.TRIGGER_CAM){
                boolean cBoxCamera = prefs.getBoolean("simpleUnlockRequiredPINCamera",true);
                if(cBoxCamera){
                    setVibration(cBoxEnableVibration);
                    setLaunchType(DataAppsSelection.ACTION_CAM,actionType);
                }else{
                    setVibration(cBoxEnableVibration);
                    setLaunchType(false,DataAppsSelection.ACTION_CAM,actionType);
                }
            }else{
                if(actionType==MotionEvent.ACTION_DOWN){
                    //do nothing
                }else{
                    setVibration(cBoxEnableVibration);
                    setLaunchType(DataAppsSelection.ACTION_UNLOCK,-1);
                }
            }
        }
    }
    @Override public final void addTouchListener(final View v){
        v.setOnTouchListener(new MyTouchListener());
    }
    @Override public final void addDragListener(final View v){
        v.setOnDragListener(new LockDragListener());
    }
    @Override public void onLollipopNoticeTouchEvent(int touchAction,int result,String noticePkgName,
        String noticeTag,long noticeImgId){
        if(touchAction==MotionEvent.ACTION_DOWN){
            if(lytLockSide!=null){
                lytLockSide.hideView();
            }
        }else{
            if(touchAction==MotionEvent.ACTION_UP){
                if(result==NotificationView.REMOVE_NOTICE){
                    clearNoticeSingle(noticePkgName,noticeTag,noticeImgId);
                }else{
                    if(result==NotificationView.OPEN_NOTICE){
                        this.noticePkgName = noticePkgName;
                        this.noticeTag = noticeTag;
                        this.noticeImgId = noticeImgId;
                        this.noticeActionTitle = null;
                        setLaunchType(DataAppsSelection.ACTION_NOTICES,-1);
                    }
                }
            }else{
                if(touchAction==MotionEvent.ACTION_OUTSIDE){//action title pending intent
                    if(result==R.drawable.ic_not_interested_white_48dp){
                        final InfoAppsSelection app = new InfoAppsSelection();
                        app.setAppType(DataAppsSelection.APP_TYPE_BLOCKED_NOTICE_APPS);
                        app.setAppPkg(noticePkgName);
                        app.setAppClass(C.NOTICE_BLOCK_CLASS);
                        app.setAppName(noticePkgName);
                        shortcutData.addApp(app);
                        clearNoticeSingle(noticePkgName,noticeTag,noticeImgId);
                    }else{
                        this.noticePkgName = noticePkgName;
                        this.noticeTag = noticeTag;
                        this.noticeImgId = noticeImgId;
                        this.noticeActionTitle = noticeTag;
                        setLaunchType(DataAppsSelection.ACTION_NOTICES,-1);

                    }
                }
            }
        }
    }
    //load settings
    private void loadViewIds(){
        shapeSquareEmptyEnter = getResources().getDrawable(R.drawable.shape_square_empty_enter);
        shapeEnterSmall = getResources().getDrawable(R.drawable.shape_drop_oval_small);
        //
        lytMainActivity = findViewById(R.id.lytActivityMain);//Relative Layout
        lytWidgetMain = findViewById(R.id.lytWidgetMain);
        lytBottomBar = findViewById(R.id.lytBottomBar); //Linear Layout
        //
        imgScreenWallPaper = findViewById(R.id.img_lockScreenWallPaper);
    }
    private void loadUiChangeListener(){
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){
            @Override
            public void onSystemUiVisibilityChange(int visibility){
                //if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                handler.post(setFullScreen);
                //}
            }
        });
    }
    private void loadSettings(){
        //tasker
        cBoxTaskerShortcutCount = prefs.getBoolean("cBoxTaskerShortcutCount",false);
        //Security
        loadPrefsSecurity();
        //Colors
        loadPrefsColor();
        //Display
        loadPrefsDisplay();
        //Shortcut
        loadPrefsAppShortcut();
        //Other Options
        loadPrefsOtherOptions();
        //statusbar notices
        loadPrefsNotice();
        //load bottom bar
        bottomBarManager = new BottomBarManager(this);
    }
    private void loadLicenseCheck(){
        handler.post(new Runnable(){
            @Override
            public void run(){
                try{
                    boolean cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);
                    boolean licenseNeedsRetry = prefs.getBoolean(C.LICENCE_CHECK_TRY,true);
                    boolean isPromotionApp = prefs.getBoolean(C.PROMOTION_APP,false);

                    if(licenseNeedsRetry){
                        if(!isPromotionApp){
                            mLocker.checkLicense();//use getApplicationContext to prevent leak memory
                        }
                    }
                    if(!cBoxLockerOk && getPackageName().equals(C.PKG_NAME_PRO)){
                        Toast.makeText(Locker.this,"Unlicensed Software, please download C Locker from Google Play Store",Toast.LENGTH_LONG).show();
                    }
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }
        });
    }
    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                if(profileHandler.isProfileTimeEnabled()){
                    intentFilter.addAction(Intent.ACTION_TIME_TICK);
                    intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                    intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
                }
                if(profileHandler.isProfileWifiEnabled()){
                    intentFilter.addAction(getPackageName()+C.WIFI_UPDATE);
                }
                if(profileHandler.isProfileBluetoothEnabled()){
                    intentFilter.addAction(getPackageName()+C.BLUETOOTH_UPDATE);
                }
                if(profileHandler.isProfileLocationEnabled()){
                    intentFilter.addAction(getPackageName()+C.LOCATION_UPDATE);
                }

                //intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                intentFilter.addAction(Intent.ACTION_DOCK_EVENT);
                intentFilter.addAction(getPackageName()+C.LOCKERSCREEN_ON);
                //bottom bar
                intentFilter.addAction(getPackageName()+C.PAGER_TAB_NOTICE);
                intentFilter.addAction(getPackageName()+C.PAGER_CLICK_NOTICE);
                intentFilter.addAction(getPackageName()+C.NOTICE_LISTENER_LOCKER);
                intentFilter.addAction(getPackageName()+C.BOTTOM_TOUCH);
                //
                intentFilter.addAction(getPackageName()+C.HEADSET_UPDATE);
                intentFilter.addAction(getPackageName()+C.CANCEL_SCREEN_TIMEOUT);
                intentFilter.addAction(getPackageName()+C.RUN_SETTINGS_LOCKER);
                //PIN unlock
                intentFilter.addAction(getPackageName()+C.SIMPLE_UNLOCK_CALL);
                intentFilter.addAction(getPackageName()+C.BOTTOM_BAR_CALL);
                intentFilter.addAction(getPackageName()+C.OPEN_PIN);
                intentFilter.addAction(getPackageName()+C.CLOSE_PIN);
                intentFilter.addAction(getPackageName()+C.VERIFY_GOOGLE_PIN);
                intentFilter.addAction(getPackageName()+C.SHOW_VIEW);
                //tasker
                intentFilter.addAction(getPackageName()+C.KILL_LOCKER);
                intentFilter.addAction(getPackageName()+C.PIN_UNLOCK);

                registerReceiver(mReceiver,intentFilter);
            }else{
                unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void loadProxiSensorReceiver(boolean runSensor){
        try{
            if(cBoxEnableProximitySensor){
                final SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
                if(runSensor){
                    final Sensor proxSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    if(proxSensor!=null){
                        //proximityStartTime = System.currentTimeMillis();
                        sensorManager.registerListener(sensorListener,proxSensor,SensorManager.SENSOR_DELAY_NORMAL);
                    }
                }else{
                    isProxySensorOn = false;
                    sensorManager.unregisterListener(sensorListener);
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void loadPrefsSecurity(){
        cBoxEnablePassUnlock = prefs.getBoolean("cBoxEnablePassUnlock",false);
        securityType = prefs.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_NONE);
        intDelayPin = prefs.getInt("intDelayPin",0);
        //cBoxBlockNoneShortcutApps = prefs.getBoolean("cBoxBlockNoneShortcutApps",false);
    }
    private void loadPrefsColor(){
        strColorIconBgOnPress = prefs.getString(P.STR_COLOR_ICON_BG_ON_PRESS,"ffffff");
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void loadPrefsDisplay(){
        cBoxPortrait = prefs.getBoolean("cBoxPortrait",false);
        cBoxCtrTvAnmation = prefs.getBoolean("cBoxCtrTvAnmation",false);

        rBtnUnlockAnimZoomOut = prefs.getBoolean("rBtnUnlockAnimZoomOut",false);
        rBtnUnlockAnimZoomIn = prefs.getBoolean("rBtnUnlockAnimZoomIn",false);
        rBtnUnlockAnimRotation = prefs.getBoolean("rBtnUnlockAnimRotation",false);

        seekBarWallpaperDimLevel = prefs.getInt("seekBarWallpaperDimLevel",3);

        if(cBoxDisplayFullScreen){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        //always set full layout eventhough is not full screen
        int what = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(what | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        final int statusbarH = C.getStatusBarHeight(this);//+c.dpToPx(context,5);
        final int naviKeysH = C.getNaviKeyHeight(this);//+c.dpToPx(context,5);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        if(cBoxDisplayFullScreen){
            pr.setMargins(0,0,0,naviKeysH);
        }else{
            pr.setMargins(0,statusbarH,0,naviKeysH);
        }
        lytWidgetMain.setLayoutParams(pr);
    }
    private void loadPrefsAppShortcut(){
        cBoxHideCenterLockIcon = prefs.getBoolean("cBoxHideCenterLockIcon",false);
        cBoxIconBgOnPressEffect = prefs.getBoolean("cBoxIconBgOnPressEffect",false);

        intLockStyle = prefs.getInt("intLockStyle",C.SIMPLE_UNLOCK);//1 = center, 2&3 = bottom
        intGestureUnlockDirection = prefs.getInt("intGestureUnlockDirection",DataAppsSelection.GESTURE_UP);//1 = center, 2&3 = bottom

        intIconBgOnPressType = prefs.getInt("intIconBgOnPressType",C.BLINKING_TYPE_2);

        if(intLockStyle==C.FINGERPRINT_UNLOCK){
            fingerprintManager = new MyFingerprintManager(this);
        }
    }
    private void loadPrefsOtherOptions(){
        cBoxReturnLS = prefs.getBoolean("cBoxReturnLS",true);
        cBoxUnlockToHome = prefs.getBoolean("cBoxUnlockToHome",false);
        cBoxEnableVibration = prefs.getBoolean("cBoxEnableVibration",true);
        cBoxBackKeyUnlock = prefs.getBoolean("cBoxBackKeyUnlock",false);
        cBoxEnableProximitySensor = deviceAdminHandler.isDeviceAdminActivated() && prefs.getBoolean("cBoxEnableProximitySensor",true);
        intScreenTimeOut = prefs.getInt("intScreenTimeOut",C.DEFAULT_SCREEN_TIMEOUT);
        intScreenSystemTimeOut = prefs.getInt("intScreenSystemTimeOut",C.FOLLOW_SYSTEM_TIMEOUT);
        intDefaultScreenTimeOutVal = prefs.getInt("intDefaultScreenTimeOutVal",C.DEFAULT_SCREEN_TIMEOUT);
    }
    private void loadPrefsNotice(){
        cBoxEnableStatusBarNotices = prefs.getBoolean("cBoxEnableStatusBarNotices",false);
        cBoxNoticeStyleLollipop = prefs.getBoolean("cBoxNoticeStyleLollipop",true);
        isNoticeLayoutLeft = prefs.getBoolean("cBoxNoticeLayoutLeft",true);
        if(cBoxEnableStatusBarNotices){
            shapeNoticeEnter = getResources().getDrawable(R.drawable.shape_notice_enter);
            shapeNoticeExit = getResources().getDrawable(R.drawable.shape_notice_exit);
        }
    }
    private void requestNoticeBroadcast(){
        if(cBoxEnableStatusBarNotices){
            //Log.e("Locker","requestNoticeBroadcast: "+log);
            Intent i = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
            i.putExtra(C.NOTICE_COMMAND,C.SHOW_NOTICE_APPS_LIST);
            i.putExtra(C.NOTICE_FROM,C.NOTICE_NEW);
            sendBroadcast(i);
        }
    }
    //profiles
    @Override
    public void onProfilePriorityCallBack(int id){
        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>onProfilePriorityCallBack>id: "+id);
        switch(id){
            case ProfileHandler.ID_DEFAULT:
                handleProfileLoad(intLockerWidgetProfile,C.PROFILE_DEFAULT);
                break;
            case ProfileHandler.ID_MUSIC:
                handleProfileLoad(intLockerWidgetProfile,C.PROFILE_MUSIC);
                break;
            case ProfileHandler.ID_LOCATION:
                handleProfileLoad(intLockerWidgetProfile,C.PROFILE_LOCATION);
                break;
            case ProfileHandler.ID_TIME:
                handleProfileLoad(intLockerWidgetProfile,C.PROFILE_TIME);
                break;
            case ProfileHandler.ID_WIFI:
                handleProfileLoad(intLockerWidgetProfile,C.PROFILE_WIFI);
                break;
            case ProfileHandler.ID_BLUETOOTH:
                handleProfileLoad(intLockerWidgetProfile,C.PROFILE_BLUETOOTH);
                break;
        }
    }
    private void loadProfile(String log){
        //Log.e("Locker>loadProfile","log: "+log);
        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,
        //"Locker>loadProfile: "+intLockerWidgetProfile+"/"+isLollipopNoticeViewShown+
        //"/"+isViewsLoaded+"/"+log);
        if(isLollipopNoticeViewShown && isViewsLoaded){
            checkLayout(View.VISIBLE,"loadProfile");
            return;
        }
        //check if user had manually changed the profile
        if(intProfileManualChanged==-1){//-1 = no change
            profileHandler.runPriority();
            return;
        }
        //if user had manually change the profile, run this
        handleProfileLoad(intLockerWidgetProfile,intProfileManualChanged);
    }
    private void handleProfileLoad(int currentProfile,int newProfile){
        //.e("Locker>handleProfileL",currentProfile+"/"+newProfile);
        if(currentProfile!=newProfile){
            if(prefs.getBoolean("simpleAnimation",true)){
                prepareWidgetLayout(newProfile);
            }else{
                if(currentProfile==C.PROFILE_NOTICE){
                    setWidgetLayoutOutTransition(newProfile,true);
                }else{
                    setWidgetLayoutOutTransition(newProfile,false);
                }
            }
        }else{
            if((isLayoutAnmationPending && !isLayoutAnmationRunning)){
                if(cBoxCtrTvAnmation){
                    loadCrtTvAnimation(newProfile);
                }else{
                    loadZoomInAnimation(newProfile);
                }
            }else{
                //added 7.0.0.-4.2.15
                checkLayout(View.VISIBLE,"handleProfileLoad 2");
            }
        }
    }
    private void checkLayout(final int visible, String log){
        //Log.e("Locker>checkLayout","log: "+log);
        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>checkLayout: "+"/"+log);
        if(!lytMainActivity.isShown()){
            lytMainActivity.setVisibility(View.VISIBLE);
        }
        lytMainActivity.setAlpha(1);
        for(int i = 0; i<lytMainActivity.getChildCount(); i++){
            View v = lytMainActivity.getChildAt(i);
            if(v!=imgScreenWallPaper && v!=lytNoticePost){
                v.setVisibility(visible);
            }
        }
        if(cBoxCtrTvAnmation){
            setViewBackground(0);
        }
    }
    //widgets layout
    private void prepareWidgetLayout(int profile){
        if(C.isScreenPortrait(getBaseContext())){
            if(lytMainActivity!=null){
                if(lytRingUnlock!=null){
                    lytMainActivity.removeView(lytRingUnlock);
                    lytRingUnlock = null;
                }
            }
        }else{
            if(lytWidgetMain!=null){
                if(lytRingUnlock!=null){
                    lytWidgetMain.removeView(lytRingUnlock);
                    lytRingUnlock = null;
                }
            }
        }
        if(lytLockSide!=null){
            lytMainActivity.removeView(lytLockSide);
            lytLockSide = null;
        }
        if(lytWidgetMain!=null){
            if(lytWidgets!=null){
                lytWidgetMain.removeView(lytWidgets);
                lytWidgets = null;
            }
        }
        //imgScreenWallPaper.setVisibility(View.INVISIBLE);
        imgScreenWallPaper.requestLayout();
        imgScreenWallPaper.setX(0);
        imgScreenWallPaper.setY(0);
        loadWidgetLayout(profile);
    }
    private void loadWidgetLayout(final int profile){
        try{
            if(lytWidgets==null){
                lytWidgets = lytWidgets();
                //lytWidgets.setBackgroundColor(Color.BLUE); //testing
            }
            lytWidgetMain.addView(lytWidgets);
            loadWallpaper(profile);
            loadWidgets(profile);
            loadLockLayout();
            loadShortcutImages(profile);
            loadListener(true);
            setWidgetLayoutInTransition(profile);
            if(lytRingUnlock!=null){
                lytRingUnlock.setShortcutImageVisibility(false,enableFlashLight);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void loadWallpaper(final int profile){
        try{
            if(getWallpaperType==C.WALLPAPER_CUSTOM){
                String file = WallpaperHandler.getWallpaperNo(profile);
                if(isFileAvailable(file)){
                    final Bitmap bm = BitmapFactory.decodeFile(C.EXT_STORAGE_DIR+file);
                    imgScreenWallPaper.setImageBitmap(bm);
                }else{
                    setViewBackground(0);
                    imgScreenWallPaper.setImageBitmap(null);// show home wallpaper if on files found
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }catch(OutOfMemoryError e){
            mLocker.saveErrorLog(e.toString(),null);
        }
    }
    private void loadWidgets(int profile){
        ArrayList<InfoWidget> widgetList = myWidgetHelper.getWidgetList(profile);
        // check if user already setup widget, if not set to default widget profile
        if(profile!=C.PROFILE_DEFAULT && (widgetList==null || widgetList.size()==0)){
            myWidgetHelper.loadWidgets(C.PROFILE_DEFAULT,lytWidgets,this,false);
            return;
        }
        myWidgetHelper.loadWidgets(profile,lytWidgets,this,false);
    }
    private void loadLockLayout(){
        if(intLockStyle==C.SIMPLE_UNLOCK ||
            intLockStyle==C.GESTURE_UNLOCK ||
            intLockStyle==C.FINGERPRINT_UNLOCK){//no need to load unlock view
            if(lytLockSide==null){
                lytLockSide = new MySideShortcutLayout(this);
            }
            if(lytLockSide.getParent()==null){
                lytMainActivity.addView(lytLockSide);
            }
            if(intLockStyle==C.SIMPLE_UNLOCK){
                boolean cBoxEnableKeyboardTyping = prefs.getBoolean("cBoxEnableKeyboardTyping",false);
                if(!C.hasNaviKey(this) || C.isAndroid(Build.VERSION_CODES.O)){
                    cBoxEnableKeyboardTyping = true;
                }
                if(cBoxEnableKeyboardTyping){
                    if(lytSimpleUnlock==null){
                        lytSimpleUnlock = new MySimpleUnlock(this,lytMainActivity,this);
                        if(bottomBarManager!=null){
                            bottomBarManager.disableBottomBar();
                        }
                    }
                }else{
                    if(bottomBarManager!=null){
                        bottomBarManager.disableBottomBar();
                    }
                }
            }else{
                if(intLockStyle==C.FINGERPRINT_UNLOCK){
                    if(!prefs.getBoolean("hideFingerprintIcon",false)){
                        int size;
                        int top;
                        int left;
                        int mar;
                        if(C.isScreenPortrait(getBaseContext())){
                            size = (int)(intDisplayWidth*0.12);
                            top = RelativeLayout.ALIGN_PARENT_BOTTOM;
                            left = RelativeLayout.CENTER_HORIZONTAL;
                        }else{
                            size = (int)(intDisplayHeight*0.12);
                            top = RelativeLayout.ALIGN_PARENT_RIGHT;
                            left = RelativeLayout.CENTER_VERTICAL;
                        }
                        mar = (int)(size*0.1);
                        imgFingerPrint = new ImageView(this);
                        final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size,size);
                        params.addRule(top);
                        params.addRule(left);
                        params.setMargins(0,0,0,mar);

                        imgFingerPrint.setLayoutParams(params);
                        imgFingerPrint.setOnClickListener(new OnClickListener(){
                            @Override
                            public void onClick(View v){
                                if(fingerprintAttempt<=1){
                                    resetActionUnlock();
                                    handleOpenPIN();
                                }
                            }
                        });
                        lytMainActivity.addView(imgFingerPrint);
                    }

                    fingerprintManager.setFingerPrintIcon();
                    fingerprintManager.setFingerPrintIconAnimation();
                    //bottomBarManager.disableBottomBar();
                }
            }
        }else{
            if(lytRingUnlock==null){
                lytRingUnlock = new MyRingUnlock(this,intDisplayHeight,intLockStyle);
            }
            if(C.isScreenPortrait(getBaseContext())){
                if(lytRingUnlock.getParent()==null){
                    lytMainActivity.addView(lytRingUnlock);
                }
            }else{
                if(lytRingUnlock.getParent()==null){
                    final LinearLayout lyt = new LinearLayout(this);
                    final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
                    lyt.setLayoutParams(params);
                    if(intLockStyle==1){
                        lyt.setGravity(Gravity.CENTER);
                    }else{
                        lyt.setGravity(Gravity.BOTTOM);
                    }
                    lyt.addView(lytRingUnlock);
                    lytWidgetMain.addView(lyt);
                }
            }
        }
    }
    private void loadShortcutImages(int profile){
        try{
            profile = checkProfile(profile);
            if(lytLockSide!=null){
                setShortcutLayoutVisibility(lytLockSide.imgApps01,getShortcutAction(DataAppsSelection.SHORTCUT_APP_01,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps02,getShortcutAction(DataAppsSelection.SHORTCUT_APP_02,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps03,getShortcutAction(DataAppsSelection.SHORTCUT_APP_03,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps04,getShortcutAction(DataAppsSelection.SHORTCUT_APP_04,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps05,getShortcutAction(DataAppsSelection.SHORTCUT_APP_05,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps06,getShortcutAction(DataAppsSelection.SHORTCUT_APP_06,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps07,getShortcutAction(DataAppsSelection.SHORTCUT_APP_07,profile));
                setShortcutLayoutVisibility(lytLockSide.imgApps08,getShortcutAction(DataAppsSelection.SHORTCUT_APP_08,profile));

                setShortcutImage(lytLockSide.imgApps01,C.ICON_SHORTCUT_01+profile,R.drawable.ic_lock_open_white_48dp);
                setShortcutImage(lytLockSide.imgApps02,C.ICON_SHORTCUT_02+profile,R.drawable.ic_flashlight_white_48dp);//flashlight
                setShortcutImage(lytLockSide.imgApps03,C.ICON_SHORTCUT_03+profile,R.drawable.ic_camera_alt_white_48dp);
                setShortcutImage(lytLockSide.imgApps04,C.ICON_SHORTCUT_04+profile,R.drawable.ic_youtube_play_white_48dp);//youtube
                setShortcutImage(lytLockSide.imgApps05,C.ICON_SHORTCUT_05+profile,R.drawable.ic_earth_white_48dp);//internet
                setShortcutImage(lytLockSide.imgApps06,C.ICON_SHORTCUT_06+profile,R.drawable.ic_location_on_white_48dp);//map
                setShortcutImage(lytLockSide.imgApps07,C.ICON_SHORTCUT_07+profile,R.drawable.ic_call_white_48dp);
                setShortcutImage(lytLockSide.imgApps08,C.ICON_SHORTCUT_08+profile,R.drawable.ic_textsms_white_48dp);

                lytLockSide.setLayoutTop();
            }else{
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut01,getShortcutAction(DataAppsSelection.SHORTCUT_APP_01,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut02,getShortcutAction(DataAppsSelection.SHORTCUT_APP_02,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut03,getShortcutAction(DataAppsSelection.SHORTCUT_APP_03,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut04,getShortcutAction(DataAppsSelection.SHORTCUT_APP_04,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut05,getShortcutAction(DataAppsSelection.SHORTCUT_APP_05,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut06,getShortcutAction(DataAppsSelection.SHORTCUT_APP_06,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut07,getShortcutAction(DataAppsSelection.SHORTCUT_APP_07,profile));
                setShortcutLayoutVisibility(lytRingUnlock.lytShortcut08,getShortcutAction(DataAppsSelection.SHORTCUT_APP_08,profile));

                setShortcutImage(lytRingUnlock.imgLock,C.ICON_LOCK,R.drawable.ic_https_white_48dp);//lock
                setShortcutImage(lytRingUnlock.imgApps01,C.ICON_SHORTCUT_01+profile,R.drawable.ic_lock_open_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps02,C.ICON_SHORTCUT_02+profile,R.drawable.ic_flashlight_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps03,C.ICON_SHORTCUT_03+profile,R.drawable.ic_camera_alt_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps04,C.ICON_SHORTCUT_04+profile,R.drawable.ic_youtube_play_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps05,C.ICON_SHORTCUT_05+profile,R.drawable.ic_earth_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps06,C.ICON_SHORTCUT_06+profile,R.drawable.ic_location_on_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps07,C.ICON_SHORTCUT_07+profile,R.drawable.ic_call_white_48dp);
                setShortcutImage(lytRingUnlock.imgApps08,C.ICON_SHORTCUT_08+profile,R.drawable.ic_textsms_white_48dp);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void loadListener(final boolean bln){
        if(bln){
            bottomBarManager.txtBtty.setOnTouchListener(new MyTouchListener());
            bottomBarManager.imgBottomBarArrow.setOnTouchListener(new MyTouchListener());
            bottomBarManager.viewRunSettings.setOnDragListener(new LockDragListener());
            lytMainActivity.setOnDragListener(new LockDragListener());

            if(lytLockSide!=null){
                lytLockSide.imgApps01.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_01);
                    }
                });
                lytLockSide.imgApps02.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_02);
                    }
                });
                lytLockSide.imgApps03.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_03);
                    }
                });
                lytLockSide.imgApps04.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_04);
                    }
                });
                lytLockSide.imgApps05.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_05);
                    }
                });
                lytLockSide.imgApps06.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_06);
                    }
                });
                lytLockSide.imgApps07.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_07);
                    }
                });
                lytLockSide.imgApps08.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_08);
                    }
                });
            }else{
                lytRingUnlock.imgLock.setOnTouchListener(new MyTouchListener());
                lytRingUnlock.lytLock.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut01.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut02.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut03.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut04.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut05.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut06.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut07.setOnDragListener(new LockDragListener());
                lytRingUnlock.lytShortcut08.setOnDragListener(new LockDragListener());
            }
        }
    }
    private void setWidgetLayoutOutTransition(final int profile,boolean moveLeft){
        try{
            if(lytWidgets!=null){
                int width;
                if(C.isScreenPortrait(getBaseContext())){
                    if(!moveLeft){
                        width = 1500;
                    }else{
                        width = -1500;
                    }
                }else{
                    if(!moveLeft){
                        width = 3000;
                    }else{
                        width = -3000;
                    }
                }

                final ObjectAnimator outX1 = ObjectAnimator.ofFloat(lytWidgets,"x",0,width).setDuration(PROFILE_ANIMATION);
                ObjectAnimator outX2 = null;
                if(lytRingUnlock!=null){
                    outX2 = ObjectAnimator.ofFloat(lytRingUnlock,"x",0,width).setDuration(PROFILE_ANIMATION);
                }

                ObjectAnimator outX3 = null;
                if(getWallpaperType==C.WALLPAPER_CUSTOM){
                    outX3 = ObjectAnimator.ofFloat(imgScreenWallPaper,"x",0,width).setDuration(PROFILE_ANIMATION);
                }

                final AnimatorSet animSet = new AnimatorSet();
                if(outX2==null){
                    if(outX3==null){
                        animSet.play(outX1);
                    }else{
                        animSet.playTogether(outX1,outX3);
                    }
                }else{
                    if(outX3==null){
                        animSet.playTogether(outX1,outX2);
                    }else{
                        animSet.playTogether(outX1,outX2,outX3);
                    }
                }
                animSet.addListener(new Animator.AnimatorListener(){
                    @Override
                    public void onAnimationStart(Animator arg0){
                    }                    @Override
                    public void onAnimationCancel(Animator arg0){
                    }

                    @Override
                    public void onAnimationEnd(Animator arg0){
                        prepareWidgetLayout(profile);
                    }

                    @Override
                    public void onAnimationRepeat(Animator arg0){
                    }


                });
                animSet.start();
            }else{
                prepareWidgetLayout(profile);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void setWidgetLayoutInTransition(final int profile){
        try{
            if(cBoxCtrTvAnmation){
                loadCrtTvAnimation(profile);
            }else{
                loadZoomInAnimation(profile);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void loadCrtTvAnimation(final int profile){
        isLayoutAnmationRunning = true;
        final ObjectAnimator x = ObjectAnimator.ofFloat(lytMainActivity,"scaleX",0,1).setDuration(100);
        final ObjectAnimator y = ObjectAnimator.ofFloat(lytMainActivity,"scaleY",0,0.05f,1).setDuration(PROFILE_ANIMATION);
        startAnimationListener(x,y,profile);
    }
    private void loadZoomInAnimation(final int profile){
        isLayoutAnmationRunning = true;
        final ObjectAnimator obj1 = ObjectAnimator.ofFloat(lytMainActivity,"alpha",0,1).setDuration(PROFILE_ANIMATION);
        //final ObjectAnimator x = ObjectAnimator.ofFloat(lytMainActivity,"scaleX",0.8f,1).setDuration(300);
        //final ObjectAnimator y = ObjectAnimator.ofFloat(lytMainActivity,"scaleY",0.8f,1).setDuration(300);
        startAnimationListener(obj1,null,profile);
    }
    private void startAnimationListener(final ObjectAnimator obj1,final ObjectAnimator obj2,final int profile){
        try{
            final AnimatorSet animSet = new AnimatorSet();
            if(obj2==null){
                animSet.playTogether(obj1);
            }else{
                animSet.playTogether(obj1,obj2);
            }
            animSet.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationCancel(Animator arg0){
                }

                @Override
                public void onAnimationRepeat(Animator arg0){
                }

                @Override
                public void onAnimationStart(Animator arg0){
                    if(!lytMainActivity.isShown()){
                        lytMainActivity.setVisibility(View.VISIBLE);
                    }
                    handler.postDelayed(resetAnimation,PROFILE_ANIMATION);//backup reset if animation listener fails
                }

                @Override
                public void onAnimationEnd(Animator arg0){
                    isLayoutAnmationPending = false;
                    isLayoutAnmationRunning = false;
                    intLockerWidgetProfile = profile;
                    resetFrontView();
                    bottomBarManager.setBottomBarText(false);
                    if(cBoxCtrTvAnmation){
                        setViewBackground(0);
                    }
                    isViewsLoaded = true;
                }
            });
            animSet.start();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
            isLayoutAnmationPending = false;
            isLayoutAnmationRunning = false;
            intLockerWidgetProfile = profile;
            resetFrontView();
            bottomBarManager.setBottomBarText(false);
            if(cBoxCtrTvAnmation){
                setViewBackground(0);
            }
            isViewsLoaded = true;
            checkLayout(View.VISIBLE,"startAnimationListener");
        }
    }
    private RelativeLayout lytWidgets(){
        final LinearLayout.LayoutParams pr;
        if(C.isScreenPortrait(getBaseContext())){
            pr = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,myWidgetHelper.getLayoutHeight());
        }else{
            pr = new LinearLayout.LayoutParams(0,myWidgetHelper.getLayoutHeight(),1);
        }
        final RelativeLayout lyt = new RelativeLayout(this);
        lyt.setLayoutParams(pr);
        return lyt;
    }
    private void resetFrontView(){
        bringToFront(lytRingUnlock);
        bringToFront(lytNoticePost);
        bringToFront(lytSideNotices);
        bringToFront(lytBottomBar);
        try{
            if(lytSimpleUnlock!=null){
                bringToFront(lytSimpleUnlock.lytCall);
                bringToFront(lytSimpleUnlock.lytCam);
                bringToFront(lytSimpleUnlock.lytUnlock);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void bringToFront(View v){
        if(v!=null){
            lytMainActivity.bringChildToFront(v);
        }
    }
    private void changeProfile(){
        setVibration(cBoxEnableVibration);
        if(intLockerWidgetProfile==C.PROFILE_DEFAULT){
            intProfileManualChanged = C.PROFILE_MUSIC;
        }else{
            if(intLockerWidgetProfile==C.PROFILE_MUSIC){
                intProfileManualChanged = C.PROFILE_LOCATION;
            }else{
                if(intLockerWidgetProfile==C.PROFILE_LOCATION){
                    intProfileManualChanged = C.PROFILE_TIME;
                }else{
                    if(intLockerWidgetProfile==C.PROFILE_TIME){
                        intProfileManualChanged = C.PROFILE_WIFI;
                    }else{
                        if(intLockerWidgetProfile==C.PROFILE_WIFI){
                            intProfileManualChanged = C.PROFILE_BLUETOOTH;
                        }else{
                            intProfileManualChanged = C.PROFILE_DEFAULT;
                        }
                    }
                }
            }
        }
        prepareWidgetLayout(intProfileManualChanged);
        //setWidgetLayoutOutTransition(intProfileManualChanged,left);
    }
    //unlock styles, Shortcut
    private int getShortcutAction(final int shortcutId,final int profile){
        if(shortcutData==null){
            shortcutData = new DataAppsSelection(this);
        }
        //Log.e("getShortcutAction",shortcutId+"/"+profile+"/"+action+"/"+log);
        return shortcutData.getShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_ACTION,profile);
    }
    private void setShortcutLayoutVisibility(View v,int action){
        if(intLockStyle==C.SIMPLE_UNLOCK || intLockStyle==C.GESTURE_UNLOCK){
            if(v==lytLockSide.imgApps01){
                if(action==DataAppsSelection.ACTION_DEFAULT){
                    v.setVisibility(View.GONE);
                    return;
                }
            }
            if(action==DataAppsSelection.ACTION_UNLOCK){
                v.setVisibility(View.GONE);
                return;
            }
        }
        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
            v.setVisibility(View.GONE);
        }else{
            v.setVisibility(View.VISIBLE);
            v.setContentDescription("showIcon");
        }
    }
    private void setShortcutImage(final ImageView imgV,final String imgName,final int drawableId){
        if(lytRingUnlock!=null){
            if(imgV==lytRingUnlock.imgLock && cBoxHideCenterLockIcon){
                imgV.setImageBitmap(null);
                return;
            }
        }

        final File fileIcon = new File(C.EXT_STORAGE_DIR,imgName);
        if(!fileIcon.getParentFile().exists()){
            fileIcon.getParentFile().mkdir();
        }
        Bitmap bm;
        try{
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                bm = setShortcutDefault(false,drawableId);
            }else{
                if(fileIcon.getAbsoluteFile().exists()){
                    bm = BitmapFactory.decodeFile(fileIcon.getPath());
                }else{
                    bm = setShortcutDefault(true,drawableId);
                    saveShortcutIcon(bm,fileIcon);
                }
            }
        }catch(OutOfMemoryError e){
            bm = setShortcutDefault(false,drawableId);
            mLocker.saveErrorLog(e.toString(),null);
        }
        imgV.setImageBitmap(bm);
    }
    private void saveShortcutIcon(final Bitmap bm,final File fileIcon){
        try{
            final OutputStream outStream = new FileOutputStream(fileIcon);
            bm.compress(Bitmap.CompressFormat.PNG,100,outStream);
            outStream.flush();
            outStream.close();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void loadFingerprintUnlock(boolean load){
        try{
            if(intLockStyle==C.FINGERPRINT_UNLOCK){//no need to load unlock view
                fingerprintManager.setFingerprint(load,DataAppsSelection.ACTION_UNLOCK,-1,false);
            }
        }catch(Exception e){
            mLocker.saveErrorLog("loadFingerprintUnlock error",e);
        }
    }
    private boolean setVolumeRocker(final int shortcutId){
        int profile = checkProfile(intLockerWidgetProfile);
        int action = getShortcutAction(shortcutId,profile);
        if(action!=DataAppsSelection.ACTION_DEFAULT){
            if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,shortcutId);
            }else{
                if(action==DataAppsSelection.ACTION_FAVORITE_SHORTCUT){
                    setLaunchType(DataAppsSelection.ACTION_FAVORITE_SHORTCUT,shortcutId);
                }
            }
            return true;
        }else{
            return false;
        }
    }
    private Bitmap setShortcutDefault(final boolean changeDefaultShortcut,final int drawableId){
        final Bitmap bm;
        int action;
        switch(drawableId){
            case R.drawable.ic_lock_open_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_01,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_01,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_flashlight_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_02,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_02,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_camera_alt_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_03,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_03,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_youtube_play_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_04,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_04,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_earth_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_05,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_05,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_location_on_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_06,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_06,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_call_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_07,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_07,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            case R.drawable.ic_textsms_white_48dp:
                action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_08,intLockerWidgetProfile);
                if(action==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP){
                    bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                    if(changeDefaultShortcut){
                        shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_08,DataAppsSelection.KEY_SHORTCUT_ACTION,DataAppsSelection.ACTION_DEFAULT);
                    }
                }else{
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_lock_open_white_48dp)).getBitmap();
                    }else{
                        if(action==DataAppsSelection.ACTION_SHORTCUT_HIDE){
                            bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.ic_mode_edit_white_48dp)).getBitmap();
                        }else{
                            bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                        }
                    }
                }
                break;
            default:
                bm = ((BitmapDrawable)getResources().getDrawable(drawableId)).getBitmap();
                break;
        }
        editor.commit();
        return bm;
    }
    //visual
    private void loadInitialWallpaper(){
        try{
            String lvl = "00";
            if(seekBarWallpaperDimLevel!=0){
                lvl = (seekBarWallpaperDimLevel*10)+"";
            }
            final View viewDimWallpaper = findViewById(R.id.viewDimWallpaper);
            viewDimWallpaper.setBackgroundColor(Color.parseColor("#"+lvl+"000000"));
            if(getWallpaperType==C.WALLPAPER_HOME){
                if(cBoxCtrTvAnmation){
                    setViewBackground(Color.BLACK);
                }else{
                    setViewBackground(0);
                }
            }else{
                setViewBackground(Color.BLACK);
            }
            imgScreenWallPaper.setScaleType(ScaleType.FIT_XY);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }catch(OutOfMemoryError e){
            mLocker.saveErrorLog(e.toString(),null);
        }
    }
    private int getUnlockAnim(){
        if(securityType!=C.SECURITY_UNLOCK_NONE){
            //return 0;//flicking bug
        }
        if(rBtnUnlockAnimZoomOut){
            return R.anim.zoom_out_exit;
        }else{
            if(rBtnUnlockAnimZoomIn){
                return R.anim.zoom_in_exit;
            }else{
                if(rBtnUnlockAnimRotation){
                    return R.anim.rotation_exit;
                }else{
                    return 0;//R.anim.default_exit;
                }
            }
        }
    }
    //notifications
    private void loadNoticeLayout(int state,long updateTime){
        try{
            //Log.e("Locker","loadNoticeLayout 1: "+state);
            if(appNoticeList==null){
                isLollipopNoticeViewShown = false;
                removeSideNotice();
                return;
            }
            if(appNoticeList.size()>0){
                if(lytNotice==null){
                    lytNotice = new NotificationView(this,this);
                }
                //lollipop notice
                //checkLayout(View.VISIBLE);
                if(cBoxNoticeStyleLollipop){
                    lytNotice.loadLollipopNoticeView(myWidgetHelper,lytWidgets,appNoticeList,state,updateTime);
                    isLollipopNoticeViewShown = true;
                    bringToFront(lytBottomBar);
                    return;
                }//*/
                //before bottom bar
                //addNoticeTextViewDetail must add 1st, dont know why...
                if(lytSideNotices!=null){
                    return;
                }
                if(!isSideNoticePending){
                    return;
                }
                lytNoticePost = lytNotice.lytNoticeDetail();
                lytSideNotices = lytNotice.lytSideNotice();

                lytMainActivity.addView(lytNoticePost);
                lytMainActivity.addView(lytSideNotices);

                //bringToFront(lytLockCenter);
                //bringToFront(lytNoticePost);
                //bringToFront(lytNotices);
                bringToFront(lytBottomBar);

                imgNoticeRemove = lytNotice.imgNotice(C.NOTICE_ID_CLEAR,R.drawable.ic_clear_white_48dp);
                imgNoticeRemove.setVisibility(View.INVISIBLE);
                lytSideNotices.addView(imgNoticeRemove);
                int count = 0;
                final int size = C.dpToPx(this,C.ICON_HEIGHT);
                for(int i = appNoticeList.size(); i>0; i--){// put in reverse
                    count++;
                    if(count<6){
                        int index = i-1;
                        lytSideNotices.addView(lytNotice.lytRelativeIcon(appNoticeList.get(index),index,size,true,true));
                    }else{
                        lytSideNotices.addView(lytNotice.imgNotice(C.NOTICE_ID_MORE,R.drawable.arrow_down));
                        break;
                    }
                }
                runNoticeAnimation();
            }else{
                if(cBoxNoticeStyleLollipop){
                    if(isLollipopNoticeViewShown){
                        //change to notice profile to reload the profile widgets
                        isLollipopNoticeViewShown = false;
                        intLockerWidgetProfile = C.PROFILE_NOTICE;
                        loadProfile("loadNoticeLayout");
                    }
                }else{
                    removeSideNotice();
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void runNoticeAnimation(){
        try{
            final int startPos;
            final int posX;
            if(isNoticeLayoutLeft){
                posX = 0;
                startPos = posX+(-200);
            }else{
                posX = intDisplayWidth-C.dpToPx(this,60);
                startPos = posX+200;
            }
            final ObjectAnimator xIn = ObjectAnimator.ofFloat(lytSideNotices,"x",startPos,posX).setDuration(PROFILE_ANIMATION);
            final AnimatorSet animSet = new AnimatorSet();
            animSet.play(xIn);
            animSet.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationCancel(Animator arg0){
                }

                @Override
                public void onAnimationRepeat(Animator arg0){
                }

                @Override
                public void onAnimationStart(Animator arg0){
                }

                @Override
                public void onAnimationEnd(Animator arg0){
                    isSideNoticePending = false;
                    if(lytSideNotices!=null){
                        lytSideNotices.requestLayout();
                        //lytNotices.invalidate();
                    }
                }
            });
            animSet.start(); //must add listener 1st

            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    if(lytSideNotices!=null){
                        lytSideNotices.setVisibility(View.VISIBLE);
                        //lytNotices.requestLayout();
                    }
                }
            },50);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private String getNoticePkgTag(final int id){
        final int val = id-C.NOTICE_IMG_ID;
        return appNoticeList.get(val).getAppTag();
    }
    private long getNoticePkgId(final int id){
        final int val = id-C.NOTICE_IMG_ID;
        return appNoticeList.get(val).getAppPostTime();
    }
    private void refreshNoticeLayout(int state){
        if(lytLockSide!=null){
            lytLockSide.hideView();
        }//*/
        loadNoticeLayout(state,lastNoticeUpdateTime);
    }
    private void clearNoticeSingle(String noticePkgName,String noticeTag,long noticeImgId){
        removeSideNotice();
        Intent i = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
        i.putExtra(C.NOTICE_COMMAND,C.CLEAR_SINGLE_NOTICE);
        //i.putExtra(C.NOTICE_COMMAND,C.CLEAR_ALL_NOTICE);
        i.putExtra(C.NOTICE_APP_PKG_NAME,noticePkgName);
        i.putExtra(C.NOTICE_APP_PKG_TAG,noticeTag);
        i.putExtra(C.NOTICE_APP_PKG_ID,noticeImgId);
        sendBroadcast(i);
    }
    private void clearNoticeAll(){
        removeSideNotice();
        Intent i = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
        //i.putExtra(C.NOTICE_COMMAND,C.CLEAR_SINGLE_NOTICE);
        i.putExtra(C.NOTICE_COMMAND,C.CLEAR_ALL_NOTICE);
        i.putExtra(C.NOTICE_APP_PKG_NAME,noticePkgName);
        i.putExtra(C.NOTICE_APP_PKG_TAG,noticeTag);
        i.putExtra(C.NOTICE_APP_PKG_ID,noticeImgId);
        sendBroadcast(i);
    }
    private void removeSideNotice(){
        if(lytSideNotices!=null){
            lytSideNotices.removeAllViews();
            lytMainActivity.removeView(lytSideNotices);
            lytMainActivity.removeView(lytNoticePost);
            lytSideNotices = null;
        }
    }
    private void openNotice(String noticePkgName,long noticeImgId){
        editor.putString("unblockNoticePkgName",noticePkgName);
        editor.commit();
        if(noticeActionTitle==null){
            checkLayout(View.VISIBLE,"openNotice 1");
            Intent i = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
            i.putExtra(C.NOTICE_COMMAND,C.RUN_PENDING_INTENT);
            i.putExtra(C.NOTICE_APP_PKG_NAME,noticePkgName);
            i.putExtra(C.NOTICE_APP_PKG_ID,noticeImgId);
            sendBroadcast(i);
        }else{
            checkLayout(View.VISIBLE,"openNotice 2");
            Intent i = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
            i.putExtra(C.NOTICE_COMMAND,C.RUN_ACTION_PENDING_INTENT);
            i.putExtra(C.NOTICE_APP_PKG_NAME,noticePkgName);
            i.putExtra(C.NOTICE_APP_PKG_ID,noticeImgId);//pkgname
            i.putExtra(C.ACTION_PENDING_INTENT_TITLE,noticeActionTitle);//pkgname
            sendBroadcast(i);
        }
    }
    //security
    private void addBlockHomeKey(){
        if(C.hasNaviKey(this)){
            return;
        }
        if(!inFrontground){
            return;
        }
        if(C.isAccessibilityEnabled(this)){
            return;
        }
        if(lytAlertWindow==null){
            //Log.e("Locker>addAlertWindow: ",isLockerShortcut+"");
            lytAlertWindow = new MyAlertWindow(this);
            lytAlertWindow.setCallBack(this);// must set this, else the keyevent will not trigger
            getWindowManager().addView(lytAlertWindow,windowPr);
        }
    }
    private void removeBlockHomeKey(){
        if(lytAlertWindow!=null){
            //Log.e("Locker>removeAlertWindow: ",isLockerShortcut+"");
            getWindowManager().removeView(lytAlertWindow);
            lytAlertWindow = null;
        }
    }
    private void handleBlockedApps(){
        if(!isFinishing()){
            if(C.isScreenOn(this,getWindowManager())){
                //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>handleBlockedApps: "+
                //"isLockerShortcut/isRecentAppsClick/isHomeClick: "+
                //isLockerShortcut+"/"+isRecentAppsClick+"/"+isHomeClick);
                if(!isLockerShortcut){
                    if(CaptureHome.isCaptureHomeRunning()){
                        sendBroadcast(new Intent(getPackageName()+C.RUN_BLOCK_APP));
                    }else{
                        if(isRecentAppsClick){
                            if(prefs.getBoolean("cBoxBlockRecentApps",false)){
                                resetActionUnlock();
                                //sendBroadcast(new Intent(getPackageName()+C.RUN_BLOCK_APP));
                            }else{
                                //phone rotation
                            }
                        }else{
                            if(!isHomeClick){
                                if(prefs.getBoolean("cBoxBlockNoneShortcutApps",false)){
                                    resetActionUnlock();
                                    sendBroadcast(new Intent(getPackageName()+C.RUN_BLOCK_APP));
                                }else{
                                    //phone rotation
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    //Options
    private void loadScreenTimeOut(int timeoutVal,final int delayVal,final String log){
        loadScreenTimeOut(false,timeoutVal,delayVal,log);
    }
    private void loadScreenTimeOut(final boolean setOff,int timeoutVal,final int delayVal,final String log){
        if(setOff){
            if(deviceAdminHandler.isDeviceAdminActivated()){
                setScreenTimeOut(timeoutVal,delayVal);
                setTurnScreenOff(true,delayVal,"loadScreenTimeOut/"+log);
            }else{
                setScreenTimeOut(0,delayVal);
            }
        }else{
            if(timeoutVal==C.FOLLOW_SYSTEM_TIMEOUT){
                timeoutVal = mSaveData.getScreenTimeOutVal();
                if(timeoutVal<=C.MIN_SCREEN_TIMEOUT){
                    if(intDefaultScreenTimeOutVal<=C.MIN_SCREEN_TIMEOUT){
                        setScreenTimeOut(C.DEFAULT_SCREEN_TIMEOUT,delayVal);
                    }else{
                        setScreenTimeOut(intDefaultScreenTimeOutVal,delayVal);
                    }
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>loadScreenTimeOut>MIN_SCREEN_TIMEOUT>"+
                        "\n  -intDefaultScreenTimeOutVal: "+intDefaultScreenTimeOutVal+
                        "\n  -intScreenTimeOut: "+intScreenTimeOut+
                        "\n  -intScreenSystemTimeOut: "+intScreenSystemTimeOut);
                }else{
                    if(timeoutVal==C.FOLLOW_SYSTEM_TIMEOUT){
                        if(intDefaultScreenTimeOutVal<=C.MIN_SCREEN_TIMEOUT){
                            setScreenTimeOut(C.DEFAULT_SCREEN_TIMEOUT,delayVal);
                        }else{
                            setScreenTimeOut(intDefaultScreenTimeOutVal,delayVal);
                        }
                        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>loadScreenTimeOut>FOLLOW_SYSTEM_TIMEOUT>"+
                            "\n  -intDefaultScreenTimeOutVal: "+intDefaultScreenTimeOutVal+
                            "\n  -intScreenTimeOut: "+intScreenTimeOut+
                            "\n  -intScreenSystemTimeOut: "+intScreenSystemTimeOut);
                    }else{
                        setScreenTimeOut(timeoutVal,delayVal);
                    }
                }
            }else{
                setScreenTimeOut(timeoutVal,delayVal);
            }
        }
        //mSaveData.setTurnScreenOff(false);
    }
    private void setScreenTimeOut(final int value,final int delayVal){
        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setScreenTimeOut: "+value+"/"+delayVal+"/"+log);
        intTimeoutVal = value;
        handler.removeCallbacks(screenTimeout);
        handler.postDelayed(screenTimeout,delayVal);
    }
    private void setTurnScreenOff(final boolean setOff,final int delayVal,final String log){
        isScreenOffTimerRunning = setOff;
        handler.removeCallbacks(turnScreenOff);
        if(setOff){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setTurnScreenOff: "+log+"/delayVal/"+delayVal);
            handler.postDelayed(turnScreenOff,delayVal);
        }
    }
    private boolean onTouchUpGesture(final int fingerCount,final float fltX,final float fltY){
        if(fingerCount>2){//3 fingers swipe
            FINGER_COUNT = 0;
            if(fltY<-GESTURE_LENGTH){ //up
                if(profileHandler.isProfileLocationEnabled()){
                    setVibration(cBoxEnableVibration);
                    //mLocation.runLocationChecker();
                }
                return true;
            }else{
                if(fltY>GESTURE_LENGTH){//down unlock widget touch
                    setVibration(cBoxEnableVibration);
                    setLaunchType(DataAppsSelection.ACTION_WIDGET_CLICK,DataAppsSelection.TRIGGER_WIDGET_CLICK);
                    return true;
                }else{
                    if(fltX<-(GESTURE_LENGTH)){//left
                        changeProfile();
                        return true;
                    }else{
                        if(fltX>(GESTURE_LENGTH)){//right
                            changeProfile();
                            return true;
                        }
                    }
                }
            }
            return false;
        }else{
            if(fingerCount>1){//2 fingers swipe
                FINGER_COUNT = 0;
                if(fltY<-GESTURE_LENGTH){
                    setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.GESTURE_UP_2);
                    return true;
                }else{
                    if(fltY>GESTURE_LENGTH){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.GESTURE_DOWN_2);
                        return true;
                    }else{
                        if(fltX<-(GESTURE_LENGTH)){
                            setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.GESTURE_LEFT_2);
                            return true;
                        }else{
                            if(fltX>(GESTURE_LENGTH)){
                                setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.GESTURE_RIGHT_2);
                                return true;
                            }
                        }
                    }
                }
                return false;
            }else{//1 fingers swipe
                if(fltY<-GESTURE_LENGTH){
                    if(isBottomEdgeTouch){//swipe up
                        return true;
                    }
                    int actionType = DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP;
                    if(intLockStyle==C.GESTURE_UNLOCK && intGestureUnlockDirection==DataAppsSelection.GESTURE_UP){
                        actionType = DataAppsSelection.ACTION_UNLOCK;
                        setVibration(cBoxEnableVibration);
                    }
                    setLaunchType(actionType,DataAppsSelection.GESTURE_UP);
                    return true;
                }else{
                    if(fltY>GESTURE_LENGTH){
                        if(isTopEdgeTouch){//swipe statusbar down
                            return true;
                        }
                        int actionType = DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP;
                        if(intLockStyle==C.GESTURE_UNLOCK && intGestureUnlockDirection==DataAppsSelection.GESTURE_DOWN){
                            actionType = DataAppsSelection.ACTION_UNLOCK;
                            setVibration(cBoxEnableVibration);
                        }
                        setLaunchType(actionType,DataAppsSelection.GESTURE_DOWN);
                        return true;
                    }else{
                        if(fltX<-(GESTURE_LENGTH/2) && isRightEdgeTouch && lytLockSide!=null){
                            isSideNoticePending = true;
                            removeSideNotice();
                            bringToFront(lytLockSide);
                            lytLockSide.showView(false,intDisplayWidth);
                            return true;
                        }else{
                            if(fltX<-GESTURE_LENGTH){
                                int actionType = DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP;
                                if(intLockStyle==C.GESTURE_UNLOCK && intGestureUnlockDirection==DataAppsSelection.GESTURE_LEFT){
                                    actionType = DataAppsSelection.ACTION_UNLOCK;
                                    setVibration(cBoxEnableVibration);
                                }
                                setLaunchType(actionType,DataAppsSelection.GESTURE_LEFT);
                                return true;
                            }else{
                                if(fltX>(GESTURE_LENGTH/2) && isLeftEdgeTouch && intLockStyle==C.FINGERPRINT_UNLOCK){
                                    if(!SecurityUnlockView.isPINRunning()){
                                        resetActionUnlock();
                                        handleOpenPIN();
                                    }
                                    return true;
                                }else{
                                    if(fltX>(GESTURE_LENGTH/2) && isLeftEdgeTouch && lytLockSide!=null){
                                        isSideNoticePending = true;
                                        removeSideNotice();
                                        bringToFront(lytLockSide);
                                        lytLockSide.showView(true,intDisplayWidth);
                                        return true;
                                    }else{
                                        if(fltX>GESTURE_LENGTH){
                                            int actionType = DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP;
                                            if(intLockStyle==C.GESTURE_UNLOCK && intGestureUnlockDirection==DataAppsSelection.GESTURE_RIGHT){
                                                actionType = DataAppsSelection.ACTION_UNLOCK;
                                                setVibration(cBoxEnableVibration);
                                            }
                                            setLaunchType(actionType,DataAppsSelection.GESTURE_RIGHT);
                                            return true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        }
    }
    private void onTapping(boolean set){
        //Log.e("onTapping","set1: "+set);
        if(set){
            TOUCH_COUNT++;
            if(!isTapping){
                //Log.e("onTapping","set2: "+set);
                isTapping = true;
                handler.removeCallbacks(runTapping);
                handler.postDelayed(runTapping,600);
            }
        }else{
            handler.removeCallbacks(runTapping);
            TOUCH_COUNT = 0;
            isTapping = false;
            isMultiTouch = false;
        }
    }
    private void onGestureUnlock(final int direction){
        int anim;
        if(direction==DataAppsSelection.GESTURE_UP){
            anim = R.anim.slide_up_exit;
        }else{
            if(direction==DataAppsSelection.GESTURE_DOWN){
                anim = R.anim.slide_down_exit;
            }else{
                if(direction==DataAppsSelection.GESTURE_LEFT){
                    anim = R.anim.slide_left_exit;
                }else{
                    anim = R.anim.slide_right_exit;
                }
            }
        }
        onFinish(false,anim,"onGestureUnlock: "+direction);
    }
    //classes
    private class MyTouchListener implements OnTouchListener{
        private static final int GAP_LIMIT = 100;
        private int STARTING_RAW_X, STARTING_RAW_Y, BOTTOM_TOUCH_DIRECTION;

        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View v,MotionEvent m){
            try{
                if(isProxySensorOn){// cancel all touch event if proximity sensor is on
                    return true;
                }
                if(isScreenOffTimerRunning){//dont waste cpu process
                    setTurnScreenOff(false,0,"onTouchEvent");//cancel screen off once touch on locker
                }
                //
                if(v==bottomBarManager.txtBtty || v==bottomBarManager.imgBottomBarArrow){
                    return handleBottomTouch(m);
                }else{
                    if(v.getId()<(C.NOTICE_IMG_ID+100)){// +100 should be safe, notices cant be over 100apps
                        bottomBarManager.setBottomBarHide();
                        return dragActionNotice(m.getAction(),v);
                    }else{
                        bottomBarManager.setBottomBarHide();
                        return dragAction(m.getAction(),v);
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return true;
        }

        private boolean handleBottomTouch(MotionEvent m){
            switch(m.getActionMasked()){
                case MotionEvent.ACTION_DOWN:
                    BOTTOM_TOUCH_DIRECTION = -1;
                    STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                    STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(STARTING_RAW_Y-m.getRawY()>GAP_LIMIT){
                        BOTTOM_TOUCH_DIRECTION = C.BOTTOM_TOUCH_TOP;
                    }else{
                        if(STARTING_RAW_X-m.getRawX()>GAP_LIMIT){
                            BOTTOM_TOUCH_DIRECTION = C.BOTTOM_TOUCH_LEFT;
                        }else{
                            if(STARTING_RAW_X-m.getRawX()<-GAP_LIMIT){
                                BOTTOM_TOUCH_DIRECTION = C.BOTTOM_TOUCH_RIGHT;
                            }else{
                                BOTTOM_TOUCH_DIRECTION = C.BOTTOM_TOUCH_TAP;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(BOTTOM_TOUCH_DIRECTION!=-1){
                        bottomBarManager.setBottomTouch(BOTTOM_TOUCH_DIRECTION);
                    }
                    break;
            }
            return true;
        }

        private boolean dragActionNotice(final int getAction,final View v){
            if(getAction==MotionEvent.ACTION_DOWN){
                final ClipData data = ClipData.newPlainText("","");
                final DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
                v.startDrag(data,shadowBuilder,v,0);
                v.setVisibility(View.VISIBLE);

                noticeId = v.getId();
                isNoticeOnDragging = true;
                return true;
            }else{
                return false;
            }
        }

        private boolean dragAction(final int getAction,View v){
            if(getAction==MotionEvent.ACTION_DOWN){
                final ClipData data = ClipData.newPlainText("","");
                final DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
                v.startDrag(data,shadowBuilder,v,0);
                v.setVisibility(View.VISIBLE);

                oneTimeStart = true;
                oneTimeEnd = true;
                return true;
            }else{
                return false;
            }
        }
    }
    private class LockDragListener implements OnDragListener{
        private static final int POST_TIME = 0;
        private static final int POST_TITLE = 1;
        private static final int POST_CONTENT = 2;

        @Override
        public boolean onDrag(View v,DragEvent event){
            final int action = event.getAction();
            switch(action){
                case DragEvent.ACTION_DRAG_STARTED:
                    setACTION_STARTED(v);
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    setACTION_ENTERED(v);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    setACTION_EXITED(v);
                    break;
                case DragEvent.ACTION_DROP:
                    setACTION_DROP(v);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    setACTION_ENDED(v);
                    break;
                default:
                    break;
            }
            return true;
        }

        private void setACTION_STARTED(final View v){
            try{
                if(isNoticeOnDragging){
                    if(v.getId()==noticeId){
                        if(v.getId()==C.NOTICE_ID_CLEAR){
                            //time
                            lytNoticePost.getChildAt(POST_TIME).setVisibility(View.GONE);
                            //content
                            lytNoticePost.getChildAt(POST_CONTENT).setVisibility(View.GONE);
                            //title
                            ((TextView)lytNoticePost.getChildAt(POST_TITLE)).setText(getString(R.string.notice_tap_to_clear_all));
                        }else{
                            final int val = v.getId()-C.NOTICE_IMG_ID;
                            final long time = appNoticeList.get(val).getAppPostTime();
                            final String pkgName = appNoticeList.get(val).getAppPkg();
                            final String time2 = lytNotice.getNoticePostTime(time,pkgName);

                            noticePkgName = (String)v.getContentDescription();
                            noticeTag = getNoticePkgTag(v.getId());
                            noticeImgId = getNoticePkgId(v.getId());
                            //time
                            lytNoticePost.getChildAt(POST_TIME).setVisibility(View.VISIBLE);
                            ((TextView)lytNoticePost.getChildAt(POST_TIME)).setText(time2);
                            //title
                            ((TextView)lytNoticePost.getChildAt(POST_TITLE)).setText(lytNotice.getNoticeTitle(appNoticeList.get(v.getId()-C.NOTICE_IMG_ID).getAppSubInfo(),noticePkgName));
                            //content
                            lytNoticePost.getChildAt(POST_CONTENT).setVisibility(View.VISIBLE);
                            ((TextView)lytNoticePost.getChildAt(POST_CONTENT)).setText(lytNotice.getNoticeContent(appNoticeList.get(v.getId()-C.NOTICE_IMG_ID).getAppSubInfoContent()));
                            setVibration(cBoxEnableVibration);
                        }
                        lytNoticePost.setBackground(shapeNoticeExit);
                        lytNoticePost.setVisibility(View.VISIBLE);
                        imgNoticeRemove.setVisibility(View.VISIBLE);
                    }
                }else{
                    if(oneTimeStart){
                        bottomBarManager.setRssSlideDisplay(false);
                        if(lytRingUnlock!=null){
                            lytRingUnlock.setLockAnimation(false);
                            lytRingUnlock.setShortcutImageVisibility(true,enableFlashLight);
                        }
                        setVibration(cBoxEnableVibration);
                        oneTimeStart = false;
                    }
                    if(v==lytMainActivity){
                        //do nothing
                    }else{
                        if(v==bottomBarManager.viewRunSettings || v==bottomBarManager.imgBottomBarArrow){
                            if(cBoxIconBgOnPressEffect){
                                v.setBackground(C.setRectangleColor(C.REC_COLOR_BG,strColorIconBgOnPress,false));
                            }else{
                                v.setBackground(null);
                            }
                        }else{
                            if(v==lytRingUnlock.lytLock){
                                handler.postDelayed(runTaskerCenterIconPressed,1000);
                                v.setBackground(shapeEnterSmall);
                            }else{
                                if(v.getId()>(C.NOTICE_IMG_ID+100)){//prevent crashing with my notice ids, +100 should be safe, notices cant be over 100apps
                                    if(cBoxIconBgOnPressEffect){
                                        v.setBackground(setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
                                    }else{
                                        v.setBackground(null);
                                    }
                                }
                            }
                        }
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private void setACTION_ENTERED(final View v){
            try{
                if(isNoticeOnDragging){
                    if(v.getId()==C.NOTICE_ID_CLEAR){
                        setAnimationRotation(v);
                        v.setBackground(shapeEnterSmall);
                        setVibration(cBoxEnableVibration);
                    }else{
                        if(v==lytNoticePost){
                            lytNoticePost.setBackground(shapeNoticeEnter);
                            setVibration(cBoxEnableVibration);
                        }
                    }
                    return;
                }
                if(v==lytMainActivity){
                    //do nothing
                }else{
                    if(v==bottomBarManager.viewRunSettings || v==bottomBarManager.imgBottomBarArrow){
                        v.setBackground(shapeSquareEmptyEnter);
                        setVibration(cBoxEnableVibration);
                    }else{
                        if(v==lytRingUnlock.lytLock){
                            v.setBackground(shapeEnterSmall);
                        }else{
                            if(v.getId()>(C.NOTICE_IMG_ID+100)){
                                setAnimationRotation(v);
                                v.setBackground(shapeEnterSmall);
                                setVibration(cBoxEnableVibration);
                            }
                        }
                    }
                }
                //ready for easy drag
                if(v!=lytMainActivity){
                    viewLastShortcut = v;
                }
                bottomBarManager.setBottomBarTransition(true,false,getInfoText(v.getId()));
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private void setACTION_EXITED(final View v){
            try{
                if(isNoticeOnDragging){
                    if(v.getId()==C.NOTICE_ID_CLEAR){
                        v.setBackground(null);
                    }else{
                        if(v==lytNoticePost){
                            lytNoticePost.setBackground(shapeNoticeExit);
                        }
                    }
                    return;
                }
                if(v==lytMainActivity){
                    //do nothing
                }else{
                    if(v==bottomBarManager.viewRunSettings || v==bottomBarManager.imgBottomBarArrow){
                        if(cBoxIconBgOnPressEffect){
                            v.setBackground(C.setRectangleColor(C.REC_COLOR_BG,strColorIconBgOnPress,false));
                        }else{
                            v.setBackground(null);
                        }
                    }else{
                        if(v==lytRingUnlock.lytLock){
                            v.setBackground(null);
                        }else{
                            if(v.getId()>(C.NOTICE_IMG_ID+100)){
                                if(cBoxIconBgOnPressEffect){
                                    v.setBackground(setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
                                }else{
                                    v.setBackground(null);
                                }
                            }
                        }
                    }
                }
                unlock = true;
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private void setACTION_DROP(final View v){
            try{
                if(isNoticeOnDragging){
                    if(v.getId()==C.NOTICE_ID_CLEAR){
                        if(noticeId!=C.NOTICE_ID_CLEAR){
                            clearNoticeSingle(noticePkgName,noticeTag,noticeImgId);
                        }
                    }else{
                        if(v==lytNoticePost){
                            if(noticeId==C.NOTICE_ID_CLEAR){
                                clearNoticeAll();
                            }else{
                                noticeActionTitle = null;//prevent RUN_ACTION_PENDING_INTENT
                                setLaunchType(DataAppsSelection.ACTION_NOTICES,-1);
                            }
                        }
                    }
                    return;
                }
                if(unlock){
                    if(v==lytMainActivity){
                        runShortcutApp(viewLastShortcut);
                    }else{
                        if(v==lytRingUnlock.lytShortcut01 ||
                            v==lytRingUnlock.lytShortcut02 ||
                            v==lytRingUnlock.lytShortcut03 ||
                            v==lytRingUnlock.lytShortcut04 ||
                            v==lytRingUnlock.lytShortcut05 ||
                            v==lytRingUnlock.lytShortcut06 ||
                            v==lytRingUnlock.lytShortcut07 ||
                            v==lytRingUnlock.lytShortcut08 ||
                            v==bottomBarManager.viewRunSettings ||
                            v==bottomBarManager.imgBottomBarArrow){
                            runShortcutApp(v);
                        }
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
                Toast.makeText(Locker.this,"App not supported",Toast.LENGTH_LONG).show();
            }
        }

        private void setACTION_ENDED(final View v){
            try{
                v.setBackground(null);
                if(isNoticeOnDragging){
                    lytNoticePost.setBackground(null);
                    lytNoticePost.setVisibility(View.GONE);
                    imgNoticeRemove.setVisibility(View.INVISIBLE);
                    isNoticeOnDragging = false;
                    refreshNoticeLayout(NotificationView.ON_DRAG_ENDED);
                }
                if(oneTimeEnd){
                    if(lytRingUnlock!=null){
                        lytRingUnlock.setLockAnimation(true);
                        lytRingUnlock.setShortcutImageVisibility(false,enableFlashLight);
                    }
                    bottomBarManager.setBottomBarText(false);
                    oneTimeEnd = false;
                    handler.removeCallbacks(runTaskerCenterIconPressed);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private String getInfoText(final int shortcutId){
            int profile = checkProfile(intLockerWidgetProfile);
            int action;
            switch(shortcutId){
                case R.id.lyt_lock:
                    return getString(R.string.lock);
                case R.id.lyt_apps01:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_01,profile);
                    if(action==DataAppsSelection.ACTION_DEFAULT || action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_01);
                    }
                case R.id.lyt_apps02:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_02,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            if(!enableFlashLight){
                                return getString(R.string.flash);
                            }else{
                                return getString(R.string.off_flash);
                            }
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_02);
                        }
                    }
                case R.id.lyt_apps03:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_03,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            return getString(R.string.camera);
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_03);
                        }
                    }
                case R.id.lyt_apps04:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_04,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            return getString(R.string.youtube);
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_04);
                        }
                    }
                case R.id.lyt_apps05:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_05,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            return getString(R.string.internet);
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_05);
                        }
                    }
                case R.id.lyt_apps06:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_06,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            return getString(R.string.map);
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_06);
                        }
                    }
                case R.id.lyt_apps07:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_07,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            return getString(R.string.call);
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_07);
                        }
                    }
                case R.id.lyt_apps08:
                    action = getShortcutAction(DataAppsSelection.SHORTCUT_APP_08,profile);
                    if(action==DataAppsSelection.ACTION_UNLOCK){
                        return getString(R.string.unlock);
                    }else{
                        if(action==DataAppsSelection.ACTION_DEFAULT){
                            return getString(R.string.sms);
                        }else{
                            return getString(R.string.launch_app)+": "+getShortcutName(DataAppsSelection.SHORTCUT_APP_08);
                        }
                    }
                case -1://-1 = shadowlayout id
                    return getString(R.string.locker_widget_click_enable);
                case R.id.viewRunSettings:
                    return getString(R.string.launch_lockscreen_settings);
                case R.id.imgBottomBarArrow:
                    return getString(R.string.launch_lockscreen_settings);
                default:
                    return getString(R.string.lock);
            }
        }

        private void runShortcutApp(final View v){
            if(v==lytRingUnlock.lytShortcut01){
                setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_01);
            }else{
                if(v==lytRingUnlock.lytShortcut02){
                    setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_02);
                }else{
                    if(v==lytRingUnlock.lytShortcut03){
                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_03);
                    }else{
                        if(v==lytRingUnlock.lytShortcut04){
                            setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_04);
                        }else{
                            if(v==lytRingUnlock.lytShortcut05){
                                setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_05);
                            }else{
                                if(v==lytRingUnlock.lytShortcut06){
                                    setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_06);
                                }else{
                                    if(v==lytRingUnlock.lytShortcut07){
                                        setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_07);
                                    }else{
                                        if(v==lytRingUnlock.lytShortcut08){
                                            setLaunchType(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP,DataAppsSelection.SHORTCUT_APP_08);
                                        }else{
                                            if(v==bottomBarManager.viewRunSettings){
                                                setLaunchType(DataAppsSelection.ACTION_SETTINGS,DataAppsSelection.TRIGGER_SETTINGS);
                                            }else{
                                                if(v==bottomBarManager.imgBottomBarArrow){
                                                    setLaunchType(DataAppsSelection.ACTION_SETTINGS,DataAppsSelection.TRIGGER_SETTINGS);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private String getShortcutName(int shortcutId){
            int profile = checkProfile(intLockerWidgetProfile);
            return shortcutData.getShortcutName(shortcutId,profile);
        }
    }
    private class MyFingerprintManager{
        private Context context;
        private SpassFingerprint mSpassFingerprint;
        private CancellationSignal fpCancellation;
        //private static final String KEY_NAME = "locker_key";
        //private KeyStore keyStore;
        //private KeyGenerator keyGenerator;
        //private Cipher cipher;
        //private FingerprintManager.CryptoObject cryptoObject;
        private boolean isTouchFingerprint = Build.MODEL.contains("SM-G920") ||
            Build.MODEL.contains("SM-G925") ||
            Build.MODEL.contains("SM-G930") ||
            Build.MODEL.contains("SM-G935") ||
            Build.MODEL.contains("SM-G937") ||
            Build.MODEL.contains("SM-G940") ||
            Build.MODEL.contains("N920");
        private Runnable cancelFingerprint = new Runnable(){
            @Override
            public void run(){
                Toast.makeText(context,getString(R.string.fingerprint_shortcut_cancelled),Toast.LENGTH_LONG).show();
                fingerprintAttempt = 5;
                setVibration(cBoxEnableVibration);
                resetActionUnlock();
                setFingerprintM(savedLaunchAction,savedShortcutId,false);
            }
        };

        private MyFingerprintManager(Context context){
            this.context = context;
        }

        private void setFingerprint(final boolean enable,final int actionType,final int shortcutId,
            boolean showDialog){
            try{
                if(enable){
                    if(setFingerprintSamsung(actionType,shortcutId,showDialog)){
                        //run samsung fingerprint
                    }else{
                        if(setFingerprintM(actionType,shortcutId,showDialog)){
                            //run anroid fingerprint
                        }else{
                            Toast.makeText(context,getString(R.string.fingerprint_unsupport),Toast.LENGTH_LONG).show();
                            resetUnlockStyle();
                        }
                    }
                }else{
                    if(fpCancellation!=null){
                        fpCancellation.cancel();
                        fpCancellation = null;
                        handler.removeCallbacks(cancelFingerprint);
                    }
                    if(showDialog){
                        callWindowAlert(true);
                    }
                    clearFingerprintService(false);
                }//IllegalStateExceptio
            }catch(Exception e){
                fingerprintAttempt = 5;
                setVibration(cBoxEnableVibration);
                resetActionUnlock();
                mLocker.saveErrorLog("setFingerprint Exception: "+e,e);

                final String attemptsError = "Identify request is denied because 5 identify attempts are failed";
                if(e.toString().contains(attemptsError)){
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>attemptsError");
                    setFingerprint(true,actionType,shortcutId,true); //stop this
                }
            }
        }

        private void setFingerPrintIconAnimation(){
            if(imgFingerPrint==null){
                return;
            }
            final ObjectAnimator obj1 = ObjectAnimator.ofFloat(imgFingerPrint,"rotation",0,5,-5,0,5,-5,0);
            final AnimatorSet animSet = new AnimatorSet();
            animSet.play(obj1);
            animSet.setDuration(500);
            animSet.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationCancel(Animator arg0){
                }

                @Override
                public void onAnimationRepeat(Animator arg0){
                }

                @Override
                public void onAnimationStart(Animator arg0){
                }

                @Override
                public void onAnimationEnd(Animator arg0){
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            setFingerPrintIconAnimation();
                        }
                    },2000);
                }
            });
            animSet.start();
        }

        private void clearFingerprintService(boolean clearAll){
            try{
                if(mSpassFingerprint!=null){
                    if(clearAll){
                        mSpassFingerprint = null;
                    }else{
                        mSpassFingerprint.cancelIdentify();
                    }
                }
            }catch(Exception e){
                final String noRequestError = "No Identify request";
                if(e.toString().contains(noRequestError)){
                    return;
                }
                mLocker.saveErrorLog("clearFingerprintService Exception: "+e,e);
            }
        }

        private void setFingerPrintIcon(){
            if(imgFingerPrint==null){
                return;
            }
            switch(fingerprintAttempt){
                case 1:
                    imgFingerPrint.setImageResource(R.drawable.icon_fpr_1);
                    break;
                case 2:
                    imgFingerPrint.setImageResource(R.drawable.icon_fpr_2);
                    break;
                case 3:
                    imgFingerPrint.setImageResource(R.drawable.icon_fpr_3);
                    break;
                case 4:
                    imgFingerPrint.setImageResource(R.drawable.icon_fpr_4);
                    break;
                case 5:
                    imgFingerPrint.setImageResource(R.drawable.icon_fpr_5);
                    break;
                default:
                    imgFingerPrint.setImageResource(R.drawable.icon_fpr_5);
                    break;
            }
        }

        private void fingerprintFailedAction(){
            fingerprintAttempt = 5;
            setVibration(cBoxEnableVibration);
            resetActionUnlock();
            handleOpenPIN();
        }

        private boolean setFingerprintSamsung(final int actionType,final int shortcutId,
            boolean showDialog){
            try{
                Spass spass = isSamsungFingerPrintSupported();
                if(spass!=null){
                    if(mSpassFingerprint==null){
                        mSpassFingerprint = new SpassFingerprint(context);
                    }
                    if(!mSpassFingerprint.hasRegisteredFinger()){
                        Toast.makeText(context,getString(R.string.firnger_print_not_registered),Toast.LENGTH_LONG).show();
                        resetUnlockStyle();
                    }else{
                        final int DELAY = 800;
                        if(showDialog){
                            try{
                                if(spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_CUSTOMIZED_DIALOG)){
                                    mSpassFingerprint.setDialogBgTransparency(10);
                                    mSpassFingerprint.setCanceledOnTouchOutside(true);
                                }
                            }catch(UnsupportedOperationException e){
                                mLocker.saveErrorLog(null,e);
                            }
                            callWindowAlert(false);
                            mSpassFingerprint.startIdentifyWithDialog(context,new SpassFingerprint.IdentifyListener(){
                                @Override
                                public void onFinished(int eventStatus){
                                    if(eventStatus==SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS){
                                        setLaunchType(false,savedLaunchAction,savedShortcutId);
                                    }else{
                                        if(eventStatus==SpassFingerprint.STATUS_TIMEOUT_FAILED ||//4 after 20sec
                                            eventStatus==SpassFingerprint.STATUS_USER_CANCELLED ||//8
                                            eventStatus==SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE){//13
                                            callWindowAlert(true);
                                            handler.postDelayed(new Runnable(){
                                                @Override
                                                public void run(){
                                                    loadFingerprintUnlock(true);
                                                }
                                            },DELAY);
                                        }else{
                                            handleOpenPIN();
                                            callWindowAlert(true);
                                        }
                                    }
                                }

                                @Override
                                public void onReady(){
                                }

                                @Override
                                public void onStarted(){
                                }

                                @Override
                                public void onCompleted(){
                                }
                            },false);
                        }else{
                            setFingerPrintIcon();
                            mSpassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener(){
                                @Override
                                public void onFinished(int eventStatus){
                                    if(eventStatus==SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS){
                                        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: STATUS_AUTHENTIFICATION_SUCCESS: ");
                                        setVibration(cBoxEnableVibration);
                                        setLaunchType(false,actionType,shortcutId);
                                    }else{
                                        if(eventStatus==SpassFingerprint.STATUS_QUALITY_FAILED ||//12 wrong scan
                                            eventStatus==SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED){//16 wrong fingerprint
                                            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: STATUS_QUALITY_FAILED");
                                            if(!profileHandler.isProfilePinEnable(intLockerWidgetProfile)){//unlock even if fails
                                                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: STATUS_QUALITY_FAILED, "+
                                                    "profile pin disabled: "+intLockerWidgetProfile);
                                                if(!isTouchFingerprint){
                                                    setVibration(cBoxEnableVibration);
                                                }
                                                setLaunchType(false,actionType,shortcutId);
                                                return;
                                            }
                                            if(fingerprintAttempt<=1){
                                                fingerprintFailedAction();
                                                return;
                                            }
                                            fingerprintAttempt--;
                                            handler.postDelayed(new Runnable(){
                                                @Override
                                                public void run(){
                                                    //loadFingerprintUnlock(true,"startIdentify: STATUS_QUALITY_FAILED");
                                                    setFingerprint(true,DataAppsSelection.ACTION_UNLOCK,-1,false);
                                                }
                                            },DELAY);
                                        }else{
                                            if(eventStatus==SpassFingerprint.STATUS_TIMEOUT_FAILED){//4 after 20sec
                                                //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: STATUS_TIMEOUT_FAILED");
                                                handler.postDelayed(new Runnable(){
                                                    @Override
                                                    public void run(){
                                                        //loadFingerprintUnlock(true,"startIdentify: STATUS_TIMEOUT_FAILED");
                                                        setFingerprint(true,DataAppsSelection.ACTION_UNLOCK,-1,false);
                                                    }
                                                },DELAY);
                                            }else{
                                                if(eventStatus==SpassFingerprint.STATUS_USER_CANCELLED){//8 startIdentifyWithDialog running
                                                    //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: STATUS_USER_CANCELLED");
                                                }else{
                                                    if(eventStatus==SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE){//13
                                                        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE");
                                                    }else{
                                                        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>startIdentify: others");
                                                        fingerprintFailedAction();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onReady(){
                                }

                                @Override
                                public void onStarted(){
                                    sendBroadcast(new Intent(getPackageName()+C.CANCEL_SCREEN_OFF));
                                }

                                @Override
                                public void onCompleted(){
                                }
                            });
                        }
                        return true;// everything is ok, samsung fingerprint is working
                    }
                }
            }catch(Exception e){
                fingerprintAttempt = 5;
                setVibration(cBoxEnableVibration);
                resetActionUnlock();
                mLocker.saveErrorLog("setFingerprint Exception: "+e,e);

                final String inProgressError = "Identify request is denied because a previous request is still in progress";
                if(e.toString().contains(inProgressError)){
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint: "+inProgressError);
                    return true;
                }
                final String attemptsError = "Identify request is denied because 5 identify attempts are failed";
                if(e.toString().contains(attemptsError)){
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprint>attemptsError");
                    setFingerprint(true,actionType,shortcutId,true); //stop this
                    return true;
                }
            }
            return false;
        }

        @TargetApi(23)
        private boolean setFingerprintM(final int actionType,final int shortcutId,
            boolean showDialog){
            try{
                if(!C.isAndroid(Build.VERSION_CODES.M)){//23
                    return false;
                }
                FingerprintManager fm = (FingerprintManager)context.getSystemService(Context.FINGERPRINT_SERVICE);
                if(!fm.isHardwareDetected()){
                    Toast.makeText(context,getString(R.string.firnger_print_not_registered),Toast.LENGTH_LONG).show();
                    resetUnlockStyle();
                }else{
                    if(!fm.hasEnrolledFingerprints()){
                        Toast.makeText(context,getString(R.string.firnger_print_not_registered),Toast.LENGTH_LONG).show();
                        resetUnlockStyle();
                    }else{
                        //Log.e("setFingerprintM","onAuthenticationStarted");
                        //generateKey();
                        //if (cipherInit()) {
                        //cryptoObject = new FingerprintManager.CryptoObject(cipher);
                        //}
                        if(showDialog){
                            fingerprintAttempt = 5;
                            Toast.makeText(context,getString(R.string.fingerprint_scan),Toast.LENGTH_LONG).show();
                            handler.removeCallbacks(cancelFingerprint);
                            handler.postDelayed(cancelFingerprint,10000);
                        }
                        setFingerPrintIcon();
                        sendBroadcast(new Intent(getPackageName()+C.CANCEL_SCREEN_OFF));
                        if(fpCancellation!=null){
                            fpCancellation.cancel();
                            fpCancellation = null;
                        }
                        fpCancellation = new CancellationSignal();
                        fm.authenticate(null,fpCancellation,0,new AuthenticationCallback(){
                            public void onAuthenticationError(int errorCode,CharSequence errString){
                                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprintM>onAuthenticationError: "+errorCode+"/"+errString);
                                if(errorCode==FingerprintManager.FINGERPRINT_ERROR_CANCELED || errorCode==FingerprintManager.FINGERPRINT_ERROR_TIMEOUT){
                                    resetActionUnlock();
                                    setFingerprintM(savedLaunchAction,savedShortcutId,false);
                                    return;//fingerprint cancelled
                                }else{
                                    if(errorCode==FingerprintManager.FINGERPRINT_ERROR_LOCKOUT){

                                    }
                                }
                                Toast.makeText(context,"Error("+errorCode+") "+errString,Toast.LENGTH_LONG).show();
                                //fingerprintFailedAction();
                            }

                            public void onAuthenticationHelp(int helpCode,CharSequence helpString){
                                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprintM>onAuthenticationHelp: "+helpCode+"/"+helpString);
                                Toast.makeText(context,"("+helpCode+") "+helpString,Toast.LENGTH_LONG).show();
                            }

                            public void onAuthenticationSucceeded(
                                FingerprintManager.AuthenticationResult result){
                                //Log.e("setFingerprintM","onAuthenticationSucceeded");
                                fingerprintAttempt = 5;
                                handler.removeCallbacks(cancelFingerprint);
                                setVibration(cBoxEnableVibration);
                                setLaunchType(false,actionType,shortcutId);
                            }

                            public void onAuthenticationFailed(){
                                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprintM>onAuthenticationFailed");
                                if(!profileHandler.isProfilePinEnable(intLockerWidgetProfile)){//unlock even if fails
                                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"Locker>setFingerprintM>onAuthenticationFailed>profile pin disabled: "+intLockerWidgetProfile);
                                    setVibration(cBoxEnableVibration);
                                    setLaunchType(false,actionType,shortcutId);
                                    return;
                                }
                                if(fingerprintAttempt<=1){
                                    fingerprintAttempt = 5;
                                    fingerprintFailedAction();
                                    return;
                                }
                                fingerprintAttempt--;
                                setFingerPrintIcon();
                            }
                        },null);
                        return true;
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog("setFingerprintM Exception: "+e,e);
            }
            return false;
        }

        /*
        @TargetApi(23)
		private void generateKey() {
	        try {
	            keyStore = KeyStore.getInstance("AndroidKeyStore");
	        } catch (Exception e) {
	        	throw new RuntimeException("generateKey errors 1", e);
	        }

	        try {
	            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
	        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
	            throw new RuntimeException("Failed to get KeyGenerator instance", e);
	        }
	        try {
	            keyStore.load(null);
	            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
	            		KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
	                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
	                    .setUserAuthenticationRequired(true)
	                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).build());
	            keyGenerator.generateKey();
	        } catch (NoSuchAlgorithmException|InvalidAlgorithmParameterException|
	        		CertificateException|IOException e) {
	            throw new RuntimeException("generateKey errors 2",e);
	        }
	    }
	    @TargetApi(23)
	    private boolean cipherInit() {
	        try {
	            cipher = Cipher.getInstance(
	            		KeyProperties.KEY_ALGORITHM_AES + "/" +
	            		KeyProperties.BLOCK_MODE_CBC + "/" +
	            		KeyProperties.ENCRYPTION_PADDING_PKCS7);
	        } catch (NoSuchAlgorithmException|NoSuchPaddingException e) {
	            throw new RuntimeException("Failed to get Cipher 1", e);
	        }

	        try {
	            keyStore.load(null);
	            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,null);
	            cipher.init(Cipher.ENCRYPT_MODE, key);
	            return true;
	        } catch (KeyPermanentlyInvalidatedException e) {
	            return false;
	        } catch (KeyStoreException|CertificateException|
	        		UnrecoverableKeyException|IOException|
	        		NoSuchAlgorithmException|InvalidKeyException e) {
	            throw new RuntimeException("Failed to init Cipher 2", e);
	        }
	    }
		 */
        private Spass isSamsungFingerPrintSupported(){
            try{
                final Spass spass = new Spass();
                spass.initialize(context);
                if(spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT)){
                    return spass;
                }
            }catch(SsdkUnsupportedException | UnsupportedOperationException e){
                mLocker.saveErrorLog("setFingerprint SsdkUnsupportedException: "+e,e);
            }
            return null;
        }
    }
}