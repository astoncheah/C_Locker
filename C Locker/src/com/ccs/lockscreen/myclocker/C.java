package com.ccs.lockscreen.myclocker;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.CheckLongPressHelper;
import com.ccs.lockscreen_pro.ServiceMain;

import java.lang.reflect.Field;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public final class C{
    public static final String LOG = "C_Locker";
    private static final int
        DEFAULT_DEN_DP_X = 74;//72+2 = 1 span
        public static final int DEFAULT_DEN_DP_Y = 102;//72+2 = 1 span
    public static final int PROMOTION_TIME_OK = 1;
    public static final int PROMOTION_TIME_NG = 2;
    public static final int PROMOTION_TIME_ERROR = 3;
    public static final int ICON_HEIGHT = 50;

    public static final int WIDGET_LEFT = 1, WIDGET_RIGHT = 2, WIDGET_TOP = 3, WIDGET_BOTTOM = 4, PROFILE_DEFAULT = 1, PROFILE_MUSIC = 2, PROFILE_LOCATION = 3, PROFILE_TIME = 4, PROFILE_WIFI = 5, PROFILE_BLUETOOTH = 6, PROFILE_NOTICE = 100, PROFILE_PIN_SETTINGS = 1, PROFILE_SMART_UNLOCK_SETTINGS = 2;
    public static final int SIMPLE_UNLOCK = 4, GESTURE_UNLOCK = 5, FINGERPRINT_UNLOCK = 6, CENTER_RING_UNLOCK = 1, BOTTOM_RING_UNLOCK = 3, BOTTOM_TRIANGLE_UNLOCK = 2;
    //General
    public static final int
        //WALLPAPER_DEFAULT 	= 1,
        WALLPAPER_HOME = 2, WALLPAPER_CUSTOM = 3, //animation type
        BLINKING_TYPE_1 = 1, BLINKING_TYPE_2 = 2, BLINKING_TYPE_3 = 3, BLINKING_TYPE_4 = 4, BLINKING_TYPE_5 = 5, SECURITY_UNLOCK_NONE = 0, SECURITY_UNLOCK_PIN = 1, SECURITY_UNLOCK_PATTERN = 2,

        FOLLOW_SYSTEM_TIMEOUT = 9999, DEFAULT_SCREEN_TIMEOUT = 60000, MIN_SCREEN_TIMEOUT = 4000, UNTOUCH_SCREEN_TIMEOUT = 10500, TYPE_LOCKER_SCREEN_TIMEOUT = 0, TYPE_SYSTEM_SCREEN_TIMEOUT = 1, //PAGER
        LAUNCH_SINGLE_NOTICE = 1, LAUNCH_SINGLE_SMS = 2, LAUNCH_SINGLE_CALLLOG = 3, LAUNCH_SINGLE_EVENT = 4, LAUNCH_SINGLE_RSS = 5, //Clock Widget
        APPWIDGET_HOST_ID = 2222, CLOCK_ANALOG_CENTER = -1001, //CLOCK_ANALOG_LEFT 		= -1002,
        CLOCK_DIGITAL_CENTER = -1003, //CLOCK_DIGITAL_LEFT 		= -1004,
        //CLOCK_MUSIC = -1005, //CLOCK_WEATHER_INFO 		= -1006,
        CLOCK_EVENT_LIST = -1007, CLOCK_RSS = -1008, CLOCK_OWNER_MSG = -1009, //Bottom touch
        BOTTOM_TOUCH_TAP = 0, BOTTOM_TOUCH_TOP = 1, BOTTOM_TOUCH_LEFT = 2, BOTTOM_TOUCH_RIGHT = 3, //background color
        REC_COLOR_BTTY_BAR = 1, REC_COLOR_BG = 2, //Sound type lock/unlock
        SOUND_TYPE_CLICK = 1, SOUND_TYPE_PRESS = 2, SOUND_TYPE_SYSTEM_RINGTONE = 3, SOUND_TYPE_CUSTOM_RINGTONE = 4;
    //Shortcut actions, cant be same val!!
    public static final int
        LIST_LAUNCHER = 199,
        LIST_LAUNCHER_RUN = 299,
        LIST_SETTINGS = 399,
        //LIST_MUSIC_WIDGETS = 499,
        LIST_ALL_APPS_WIDGETS = 599,
        LIST_SEARCH_LONG_PRESS = 699;
    //String
    public static final String
        ADMOB_ID_BANNER = "KEY_IS_HIDDEN",
        ADMOB_ID_INTERS = "KEY_IS_HIDDEN",
        C_LOCKER_OK = "cBoxLockerOk",
        LICENCE_CHECK_TRY = "licenseNeedsRetry",
        PROMOTION_APP = "promotionProUnlockApp",
        PKG_NAME_PRO = "com.ccs.lockscreen_pro",
        PKG_NAME_FREE = "com.ccs.lockscreen",
        PREFS_NAME = "com.systeminfo.widget.LockScreenSettings", //Wrong string, but cannot change
        DATE_FORMAT_LOG = "MM-dd-yy/HH:mm:ss.SSS ",
        EXT_STORAGE_DIR = Environment.getExternalStorageDirectory().toString()+"/.C Lock Screen",
        EXT_BACKUP_DIR_FREE = Environment.getExternalStorageDirectory().toString()+"/.C Lock Screen/C Locker Free Backup",
        EXT_BACKUP_DIR_PRO = Environment.getExternalStorageDirectory().toString()+"/.C Lock Screen/C Locker Pro Backup",
        AUTO_SELFIE_DIR = Environment.getExternalStorageDirectory().toString()+"/C Locker",
        FILE_SYSTEM_LOG = "/Systemlog.txt",
        FILE_MY_LOG = "/CLockerlog.txt",
        FILE_BACKUP_RECORD = "/BackupRecord.txt",
        FILE_PREFS = "/prefs.xml",
        TAB_SELECTED = "/tabSelected.png",
        //TAB_UNSELECTED = "/tabUnselected.png",
        CLOCK_DIAL = "/clockDial.png",
        CLOCK_HOUR = "/clockHour.png",
        CLOCK_MIN = "/clockMin.png",
        //CLOCK_SEC = "/clockSec.png",
        WALL_PAPER_01 = "/wallpaper01.pg",//default
        WALL_PAPER_02 = "/wallpaper02.pg",//music
        WALL_PAPER_03 = "/wallpaper03.pg",//location
        WALL_PAPER_04 = "/wallpaper04.pg",//time
        WALL_PAPER_05 = "/wallpaper05.pg",//wifi
        WALL_PAPER_06 = "/wallpaper06.pg",//bluetooth
        ICON_LOCK = "/iconLock.pg",
        ICON_SHORTCUT_01 = "/iconShortcut01.pg",
        ICON_SHORTCUT_02 = "/iconShortcut02.pg",
        ICON_SHORTCUT_03 = "/iconShortcut03.pg",
        ICON_SHORTCUT_04 = "/iconShortcut04.pg",
        ICON_SHORTCUT_05 = "/iconShortcut05.pg",
        ICON_SHORTCUT_06 = "/iconShortcut06.pg",
        ICON_SHORTCUT_07 = "/iconShortcut07.pg",
        ICON_SHORTCUT_08 = "/iconShortcut08.pg", //NEW
        ICON_VOLUME_UP = "/iconVolumeUp.pg",
        ICON_VOLUME_DOWN = "/iconVolumeDown.pg",
        ICON_GESTURE_UP = "/iconGestureUp.pg",
        ICON_GESTURE_DOWN = "/iconGestureDown.pg",
        ICON_GESTURE_LEFT = "/iconGestureLeft.pg",
        ICON_GESTURE_RIGHT = "/iconGestureRight.pg",
        //ICON_HOME_2_CLICK = "/iconHome2Click.pg",
        ICON_GESTURE_UP_2 = "/iconGestureUp2.pg",
        ICON_GESTURE_DOWN_2 = "/iconGestureDown2.pg",
        ICON_GESTURE_LEFT_2 = "/iconGestureLeft2.pg",
        ICON_GESTURE_RIGHT_2 = "/iconGestureRight2.pg",
        ICON_DOUBLE_TAP1 = "/iconDoubleTap1.pg",
        ICON_DOUBLE_TAP2 = "/iconDoubleTap2.pg",
        ICON_TRIPLE_TAP1 = "/iconTripleTap1.pg",
        ICON_TRIPLE_TAP2 = "/iconTripleTap2.pg";
    //String Package/Class Name, Broadcast Receiver
    public static final String
        //RECENT APPS
        //RECENT_APPS_PKG = "com.android.systemui",
        S8      = "SM-G950",
        S8_PLUS = "SM-G955",
        RECENT_APPS_CLASS = "com.android.systemui.recent.RecentsActivity",//old
        RECENT_APPS_CLASS_V2 = "com.android.systemui.recent.cardholder.CardHolderRecentsActivity",//samsung 4.4.2
        RECENT_APPS_CLASS_V3 = "com.android.systemui.recentsactivity",    //lollipop
        RECENT_APPS_CLASS_V4 = "com.android.systemui.recents.RecentsActivity",    //lollipop
        //RECENT_APPS_TOGGLE = "com.android.systemui.recent.action.TOGGLE_RECENTS",
        RESOLVER_CLASS = "com.android.internal.app.ResolverActivity",
        EMERGENCY_CALL = "com.android.phone.EmergencyDialer.DIAL", //excluded blocked apps	for samsung device
        PHONE_APK = "com.android.phone", ALARM_APK_SAM = "com.sec.android.app.clockpackage", CAMERA = "com.sec.android.app.camera", ALARM_APK_LG = "com.lge.clock", GO_SMS_POP_UP = "com.jb.gosms.smspopup.SmsPopupActivity",//"com.jb.gosms",
        //SONY_CAMERA_FAST_ON = "com.sonyericsson.android.camera.fastcapturing.FastCapturingActivity",//sony fast cam class
        //SETTINGS_APK = "com.android.settings", //music player name
        PLAYER_GOOGLE = "com.google.android.music",
        PLAYER_STOCK = "com.sec.android.app.music",
        PLAYER_POWER_AMP = "com.maxmpz.audioplayer",
        //PLAYER_MUSIC_X_MATCH = "com.musixmatch.android.lyrify",
        //PLAYER_GONEMAD = "gonemad.gmmp", //My Classes
        //CAPTURE_HOME_CLASS = "com.ccs.lockscreen_pro.CaptureHome",
        //LOCKER_CLASS = "com.ccs.lockscreen_pro.Locker",
        //CUSTOM_WIDGET = "com.ccs.lockscreen_pro.ListWidgetApps", //DISABLE LOCKER FREE
        DISABLE_LOCKER_SEND = "com.ccs.lockscreen_pro.mDISABLE_LOCKER_SEND",
        DISABLE_BOTH_LOCKER_SEND = "com.ccs.lockscreen_pro.mDISABLE_BOTH_LOCKER_SEND",
        //DISABLE_LOCKER_RESULT_OK = "com.ccs.lockscreen_pro.mDISABLE_LOCKER_RESULT_OK", //NOTICE
        SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED",
        WAP_PUSH_RECEIVED_ACTION = "android.provider.Telephony.WAP_PUSH_RECEIVED",
        MMS_DATA_TYPE = "application/vnd.wap.mms-message",
        //HTC_SHUTDOWN = "android.intent.action.QUICKBOOT_POWEROFF",
        //=====
        DELAY_LOCKER = ".mDELAY_LOCKER",
        PLAY_SOUND = ".mPLAY_SOUND",
        RUN_LAUNCHER = ".mRUN_LAUNCHER",
        UNEXPECTED_UNLOCK = ".mUNEXPECTED_UNLOCK",
        BLOCK_SEARCH_LONG_PRESS = ".mBLOCK_SEARCH_LONG_PRESS",
        BLOCK_SYSTEM_SETTINGS = ".mBLOCK_SYSTEM_SETTINGS",
        RUN_SETTINGS_NOTI = ".mRUN_SETTINGS_NOTI",
        RUN_SETTINGS_LOCKER = ".mRUN_SETTINGS_LOCKER",
        RUN_BLOCK_APP = ".mRUN_BLOCK_APP",
        STOP_BLOCK_APP = ".mSTOP_BLOCK_APP",
        RSS_FEED = ".mRSS_FEED",
        RESTORE_COMPLETE = ".mRESTORE_COMPLETE",
        PROMOTION_CHECK = ".mPROMOTION_CHECK",
        //KILL_CAPTURE_HOME = ".mKILL_CAPTURE_HOME",
        UPDATE_CONFIGURE = ".mUPDATE_CONFIGURE",
        UPDATE_RSS = ".mUPDATE_RSS",
        RESET_ALL_PROFILE = ".mRESET_ALL_PROFILE",
        SET_UNLOCK_PIN = ".mSET_UNLOCK_PIN",
        TASKER = ".TASKER.",
        ON_LOCKER_RESUME = ".ON_LOCKER_RESUME",
        ON_LOCKER_PAUSE = ".ON_LOCKER_PAUSE",
        ON_LOCKER_START = ".ON_LOCKER_START",
        ON_LOCKER_STOP = ".ON_LOCKER_STOP",
        //FINISH_HOME_CAP = ".FINISH_HOME_CAP",
        OPEN_PIN = ".OPEN_PIN",
        CLOSE_PIN = ".CLOSE_PIN",
        SIMPLE_UNLOCK_CALL = ".SIMPLE_UNLOCK_CALL",
        BOTTOM_BAR_CALL = ".BOTTOM_BAR_CALL",
        SHOW_VIEW = ".SHOW_VIEW",
        FINISH_PIN = ".FINISH_PIN",
        PIN_UNLOCK = ".PIN_UNLOCK",
        VERIFY_GOOGLE_PIN = ".VERIFY_GOOGLE_PIN",
        CANCEL_SCREEN_OFF = ".CANCEL_SCREEN_OFF",
        LAUNCH_LOCKER = ".LAUNCH_LOCKER",
        EXTRA_INFO = ".EXTRA_INFO",
        BOTTOM_TOUCH = ".BOTTOM_TOUCH",
        //HIDE_SET_HOME_DIALOG = ".HIDE_SET_HOME_DIALOG",//dangerous, so not set
        SCROLLING_ENABLE = ".SCROLLING_ENABLE", SCROLLING_DISABLE = ".SCROLLING_DISABLE", REMOVE_RESIZE_VIEWS = "REMOVE_RESIZE_VIEWS", REMOVE_RESIZE_VIEWS_ALL = "REMOVE_RESIZE_VIEWS_ALL", CANCEL_SCREEN_TIMEOUT = ".mCANCEL_SCREEN_TIMEOUT", //SMS URI
        //"content://mms-sms/conversations?simple=true",
        //"content://mms-sms/conversations/",
        SMS_URI = "content://mms-sms/conversations?simple=true", //PAGER ACTION
        LOCKERSCREEN_ON = ".mLOCKERSCREEN_ON",//NEW
        PAGER_TAB_NOTICE = ".mPAGER_TAB_NOTICE",//NEW
        PAGER_CLICK_NOTICE = ".mPAGER_CLICK_NOTICE",//NEW
        RSS_NOTICE = ".mRSS_NOTICE",//NEW
    //=====
    //START_ANALOG_CLOCK_ALARM= ".mSTART_ANALOG_CLOCK_ALARM",//NEW
    //STOP_ANALOG_CLOCK_ALARM	= ".mSTOP_ANALOG_CLOCK_ALARM",//NEW
    //=====
        HEADSET_UPDATE = ".mHEADSET_EVENT", WIFI_UPDATE = ".mWIFI_UPDATE", BLUETOOTH_UPDATE = ".mBLUETOOTH_UPDATE", LOCATION_UPDATE = ".mLOCATION_UPDATE",
        KILL_LOCKER = ".mKILL_LOCKER", PROFILE_TYPE = "PROFILE_TYPE", //=====
        LIST_APPS_ID = "listAppsID", LIST_APPS_PROFILE = "listAppsProfile", LIST_APP_WIDGET_TYPE = "listAppWidgetType", SECURITY_TYPE = "securityType", PATTERN_LOCK = "strPattern2nd";
    //TabpageIndicator
    public static final int LAUNCH_CALENDAR = 1, LAUNCH_GMAIL = 2, LAUNCH_CALLLOG = 3, LAUNCH_SMS = 4, LAUNCH_RSS = 5;
    public static final String
        TAB_ACTION = "tabAction",
        //NOTICE_PKG_NAME = "noticePkgName",
        SINGE_CLICK_ACTION = "singleClickAction",
        EVENT_ID = "eventID",
        CONTACT_ID = "contactID",
        PHONE_NO = "phoneNo",
        SMS_ID = "smsId",
        RSS_LINK = "rssLink";
    //Bottom Notice Manager
    public static final int NOTICE_NEW = 1, NOTICE_UPDATE = 2, NOTICE_ID_CLEAR = -18000, NOTICE_ID_MORE = -18001, NOTICE_IMG_ID = -8000;//ramdon, make sure dont crash with other
    public static final String
        REMOVE_STARTUP_VIEW = ".mREMOVE_STARTUP_VIEW",
        NOTICE_LISTENER_LOCKER = ".mNOTICE_LISTENER_LOCKER",
        NOTICE_LISTENER_SERVICE = ".mNOTICE_LISTENER_SERVICE",
        NOTICE_COMMAND = "noticeCommand",
        NOTICE_FROM = "noticeRequestFrom",
        NOTICE_COUNT = "noticeCount",
        //NOTICE_ACTIVE_APPS = "noticeActiveApps",
        //NOTICE_NEW_POST = "noticeNewPost",
        //NOTICE_REMOVE_POST = "noticeRemovePost",
        //NOTICE_APP_INFO = "noticeAppInfo",
        NOTICE_APP_PKG_NAME = "noticePkgName",
        NOTICE_APP_PKG_TAG = "noticePkgTag",
        NOTICE_APP_PKG_ID = "noticePkgId",
        //NOTICE_POST_TIME = "noticePostTime",
        NOTICE_ALL_APP_LIST = "noticeAllAppList",
        RUN_PENDING_INTENT = "runPendingIntent",
        RUN_ACTION_PENDING_INTENT = "runActionPendingIntent",
        ACTION_PENDING_INTENT_TITLE = "actionPendingIntentTitle",
        ACTION_PENDING_INTENT_MSG = "actionPendingIntentMsg",
        CLEAR_ALL_NOTICE = "clearAllNotice",
        CLEAR_SINGLE_NOTICE = "clearSingleNotice",
        SHOW_NOTICE_APPS_LIST = "showNoticeAppsList",
        NOTICE_BLOCK_CLASS = "NOTICE_BLOCK_CLASS";

    public interface WidgetOnTouchListener{
        boolean onWidgetTouchEvent(View v,MotionEvent m,CheckLongPressHelper widgetLongPressHelper,String pkgName);
    }
    public interface CallBack{
        boolean callKeyEvent(KeyEvent event);

        boolean callTouchEvent(MotionEvent m);

        void callWindowAlert(boolean isAdd);
    }

    public static boolean isAndroid(int version){
        return Build.VERSION.SDK_INT>=version;
    }
    public static boolean isInternetConnected(Context context){
        try{
            final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(netInfo!=null && netInfo.isConnected()){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public static boolean isAccessibilityEnabled(Context context){
        try{
            final String str = Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            //Log.e("isAccessibilityEnabled","ServiceAccessibility>Active Accessibility Apps: "+str);
            if(str!=null && str.contains(context.getPackageName()+"/")){
                return true;
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return false;
    }
    public static boolean isNotificationEnabled(Context context){
        try{
            final String str = Settings.Secure.getString(context.getContentResolver(),"enabled_notification_listeners");
            if(str.contains(context.getPackageName()+"/")){
                return true;
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return false;
    }
    public static boolean isScreenPortrait(Context context){
        return context.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT;
    }
    public static boolean isScreenOn(Context context,WindowManager windowManager){
        try{
            final PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            return pm.isScreenOn();
        }catch(Exception e){
            e.printStackTrace();
        }
        if(isAndroid(Build.VERSION_CODES.KITKAT_WATCH)){
            final Display display = windowManager.getDefaultDisplay();
            final int state = display.getState();
            return state!=Display.STATE_OFF;
        }
        return true;
        /*
        final PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(32,"Locker");//PROXIMITY_SCREEN_OFF_WAKE_LOCK = 32
		wakeLock.acquire();
        //wakeLock.release();
		new Handler().postDelayed(new Runnable(){@Override public void run() {
			if(wakeLock!=null && wakeLock.isHeld()){
				wakeLock.release();
				Log.e("info","wakeLock");
			}
		}},10000);
		*/
    }
    @SuppressLint("NewApi")
    public static boolean isGetRunningTaskPermission(Context context){
        try{
            UsageStatsManager usm = (UsageStatsManager)context.getSystemService("usagestats");
            long time = System.currentTimeMillis();
            List<UsageStats> queryUsageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,0,time);
            return !queryUsageStats.isEmpty();
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return false;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean hasNaviKey(Context context){
        final int naviKeysH = getNaviKeyHeight(context);
        if(naviKeysH>0){
            return true;
        }
        boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        return !hasMenuKey && !hasBackKey;
    }
    //
    public static ComponentName getRunningTask(Context context,int appIndex){
        ComponentName componentName = null;
        try{
            if(isAccessibilityEnabled(context)){
                componentName = getRunningTaskFromServiceAccess(context);
            }else if(isAndroid(Build.VERSION_CODES.LOLLIPOP_MR1)){//22 = 5.1.1v
                componentName = getRunningTask5_1_1v(context);
            }else if(isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                componentName = getRunningTask5v(context);
            }else{
                final int MAX_LIST = 2;
                final ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
                @SuppressWarnings("deprecation")
                final List<RunningTaskInfo> list = am.getRunningTasks(MAX_LIST);//Integer.MAX_VALUE

                if(list!=null && list.size()>appIndex){
                    componentName = list.get(appIndex).topActivity;//0 = top, current running
                }
            }
        }catch(SecurityException e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return componentName;
    }
    private static ComponentName getRunningTaskFromServiceAccess(Context context){
        SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        String pkgName = prefs.getString(P.STR_SERVICE_ACCESS_PKG_NAME,null);
        String className = prefs.getString(P.STR_SERVICE_ACCESS_CLASS_NAME,null);
        if(pkgName==null){
            return null;
        }
        return new ComponentName(pkgName,className);
    }
    @SuppressLint("NewApi")
    private static ComponentName getRunningTask5_1_1v(Context context){
        ComponentName componentName = null;
        UsageStatsManager usm = (UsageStatsManager)context.getSystemService("usagestats");
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,time-(1000*60*2),time);//get last 2 mins apps
        if(appList!=null && appList.size()>0){
            SortedMap<Long,UsageStats> mySortedMap = new TreeMap<>();
            for(UsageStats usageStats : appList){
                mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
            }
            if(mySortedMap!=null && !mySortedMap.isEmpty()){
                String pkg = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                componentName = new ComponentName(pkg,pkg);
            }
        }
        Log.e("C","getRunningTask5_1_1v: "+componentName);
        return componentName;
    }
    private static ComponentName getRunningTask5v(Context context){
        ComponentName componentName = null;
        try{
            final int START_TASK_TO_FRONT = 2;
            final Field field = RunningAppProcessInfo.class.getDeclaredField("processState");
            final ActivityManager manager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
            for(ActivityManager.RunningAppProcessInfo app : list){
                if(app.importance==RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                    Integer state = field.getInt(app);
                    if(state!=null && state==START_TASK_TO_FRONT){
                        if(app.processName!=null){
                            //new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,log+">C>getRunningTask5v Class: "+app.processName);
                            //return app.importanceReasonComponent; not accurate this one.
                            componentName = new ComponentName(app.processName,app.processName);//add pkg name as class name
                        }
                    }
                }
            }
        }catch(Exception e){
            //new MyCLocker(context).saveErrorLog(null,e);
            e.printStackTrace();
        }
        Log.e("C","getRunningTask5v: "+componentName);
        return componentName;
    }
    //
    public static void setBackgroundRipple(Context context,View v){
        if (isAndroid(Build.VERSION_CODES.HONEYCOMB)) {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            v.setBackgroundResource(outValue.resourceId);
        }
    }
    public static void setCustomBackgroundRipple(Context context,View v){
        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
            v.setBackgroundResource(R.drawable.ripple_effect);
        }else{
            v.setBackgroundColor(Color.WHITE);
        }
    }
    public static Drawable setRectangleColor(int name,String strColor,boolean isCenter){
        try{
            GradientDrawable gd = null;
            final int color01 = Color.parseColor("#"+strColor);
            switch(name){
                case REC_COLOR_BG:
                    final int color02 = Color.parseColor("#99"+strColor);
                    final int color03 = Color.parseColor("#66"+strColor);
                    final int color04 = Color.parseColor("#22"+strColor);
                    final int color05 = Color.parseColor("#00"+strColor);
                    if(isCenter){
                        gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color05,color04,color03,color02,color03,color04,color05});
                    }else{
                        gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,color05});
                    }
                    gd.setShape(GradientDrawable.RECTANGLE);
                    break;
                case REC_COLOR_BTTY_BAR:
                    final int color07 = Color.parseColor("#cc"+strColor);
                    final int color08 = Color.parseColor("#dd"+strColor);
                    if(isCenter){
                        gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color08,color01,color07});
                    }else{
                        gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color07,color01,color08});
                    }
                    gd.setShape(GradientDrawable.RECTANGLE);
                    break;
            }
            return gd;
            //gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
            //gd.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
            //gd.setColors(new int[]{color01,color02,color03,color04,color05});
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static Drawable getScaledMenuIcon(Context context,Drawable drawable){
        try{
            Configuration config = context.getResources().getConfiguration();
            if(config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE)){
                //return getScaledImage(context,drawable,192,192);
                //new AppHandler(context).writeToFile(C.FILE_BACKUP_RECORD,"isLayoutSizeAtLeast: SCREENLAYOUT_SIZE_XLARGE/"+config.densityDpi);
                return getScaledImage(context,drawable,144,144);
            }else if(config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_LARGE)){
                //new AppHandler(context).writeToFile(C.FILE_BACKUP_RECORD,"isLayoutSizeAtLeast: SCREENLAYOUT_SIZE_LARGE/"+config.densityDpi);
                return getScaledImage(context,drawable,144,144);
            }else if(config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_NORMAL)){
                //new AppHandler(context).writeToFile(C.FILE_BACKUP_RECORD,"isLayoutSizeAtLeast: SCREENLAYOUT_SIZE_NORMAL/"+config.densityDpi);
                if(isAndroid(Build.VERSION_CODES.JELLY_BEAN_MR1)){
                    if(config.densityDpi<500){
                        return getScaledImage(context,drawable,72,72);
                    }else{
                        return getScaledImage(context,drawable,96,96);
                    }
                }
            }else if(config.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_SMALL)){
                //new AppHandler(context).writeToFile(C.FILE_BACKUP_RECORD,"isLayoutSizeAtLeast: SCREENLAYOUT_SIZE_SMALL/"+config.densityDpi);
                return getScaledImage(context,drawable,72,72);
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return getScaledImage(context,drawable,96,96);
    }
    private static Drawable getScaledImage(Context context,Drawable drawable,int dstWidth,int dstHeight){
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap,dstWidth,dstHeight,false);
        return new BitmapDrawable(context.getResources(),bitmapScaled);
    }
    public static Point getRealDisplaySize(Context context){
        return getRealDisplaySize(getWindowManager(context));
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static Point getRealDisplaySize(WindowManager windowManager){
        Point size = new Point();
        windowManager.getDefaultDisplay().getRealSize(size);
        return size;
    }
    private static Point getUsableDisplaySize(Context context){
        SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        int h = prefs.getInt(ServiceMain.USABLE_DISPLAY_HEIGHT,getRealDisplaySize(context).y);

        Point size = new Point();
        size.x = getRealDisplaySize(context).x;
        size.y = h;
        return size;
    }
    public static int getStatusBarHeight(Context context){
        int result = 0;
        try{
            int resourceId = context.getResources().getIdentifier("status_bar_height","dimen","android");
            if(resourceId>0){
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }catch(NotFoundException e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        //Log.e("getStatusBarHeight","getStatusBarHeight: "+result);
        return result;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getNaviKeyHeight(Context context){
        //S8 & S8+ screen = 1440 x 2960
        //E/getNaviKeyHeight1: usableHeight+"/"+realHeight: 2076/2220
        //E/getNaviKeyHeight1: usableHeight+"/"+realHeight: 2008/2008
        //E/getStatusBarHeight: getStatusBarHeight: 72

        DisplayMetrics metrics = new DisplayMetrics();
        if(isScreenPortrait(context)){
            getWindowManager(context).getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            getWindowManager(context).getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;

            //Log.e("getNaviKeyHeight1","usableHeight+\"/\"+realHeight: "+usableHeight+"/"+realHeight);
            int result = realHeight-usableHeight;
            if(realHeight>usableHeight){
                return result;
            }else{
                return 0;
            }
        }else{
            getWindowManager(context).getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.widthPixels;
            getWindowManager(context).getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.widthPixels;

            //Log.e("getNaviKeyHeight2","usableHeight+\"/\"+realHeight: "+usableHeight+"/"+realHeight);
            int result = realHeight-usableHeight;
            if(realHeight>usableHeight){
                return result;
            }else{
                return 0;
            }
        }
    }
    private static WindowManager getWindowManager(Context context){
        return (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
    }
    public static boolean isS8(){
        String model = Build.MODEL;
        if(model.contains(C.S8)||
            model.contains(C.S8_PLUS)){
            return true;
        }

        return false;
    }
    public static int dpToPx(Context context,int dp){
        //return (int) (dp * context.getResources().getDisplayMetrics().density);
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,context.getResources().getDisplayMetrics());
    }
    public static int pxToDp(Context context,int px){
        //return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,px,context.getResources().getDisplayMetrics());
        return (int)(px/context.getResources().getDisplayMetrics().density);
    }
    public static int getSpanToPxX(Context context,int span){
        int maxWidgetSpanX = getMaxWidgetSpanX(context);
        int w = getUsableDisplaySize(context).x;
        if(!isScreenPortrait(context)){
            w = w/2;
        }
        return (w/maxWidgetSpanX)*span;
    }
    public static int getSpanToPxY(Context context,int span){
        int maxWidgetSpanY = getMaxWidgetSpanY(context);
        //int defaultDenSize = dpToPx(context,DEFAULT_DEN_DP_Y);
        int h = getUsableDisplaySize(context).y;
        if(isScreenPortrait(context)){
            //h = h-defaultDenSize;
        }
        return (h/maxWidgetSpanY)*span;
    }
    public static int getDefaultSpanToPxY(Context context,int span){    //for notification widget use
        int maxWidgetSpanY = getDefaultMaxWidgetSpanY(context);
        //int defaultDenSize = dpToPx(context,DEFAULT_DEN_DP_Y);
        int h = getUsableDisplaySize(context).y;
        if(isScreenPortrait(context)){
            //h = h-defaultDenSize;
        }
        return (h/maxWidgetSpanY)*span;
    }
    public static int getPxToSpanX(Context context,boolean allow0,int w){
        final int maxWidgetSpanX = getMaxWidgetSpanX(context);
        int intDisplayWidth = getUsableDisplaySize(context).x;
        if(!isScreenPortrait(context)){
            intDisplayWidth = intDisplayWidth/2;
        }
        final float size = (float)w/intDisplayWidth;
        int result = Math.round(maxWidgetSpanX*size);
        if(allow0){
            return result;
        }
        if(result<1){
            result = 1;
        }
        return result;
    }
    public static int getPxToSpanY(Context context,boolean allow0,int h){
        final int maxWidgetSpanY = getMaxWidgetSpanY(context);
        int intDisplayHeight = getUsableDisplaySize(context).y;
        //int defaultDenSize = dpToPx(context,DEFAULT_DEN_DP_Y);
        if(isScreenPortrait(context)){
            //intDisplayHeight = intDisplayHeight-defaultDenSize;
        }
        final float size = (float)h/intDisplayHeight;
        int result = Math.round(maxWidgetSpanY*size);
        if(allow0){
            return result;
        }
        if(result<1){
            result = 1;
        }
        return result;
    }
    public static int getMaxWidgetSpanX(Context context){
        SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        return prefs.getInt("intWidgetGridWidth",getDefaultMaxWidgetSpanX(context));
    }
    public static int getMaxWidgetSpanY(Context context){
        SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        return prefs.getInt("intWidgetGridHeight",getDefaultMaxWidgetSpanY(context));
    }
    public static int getDefaultMaxWidgetSpanX(Context context){
        final int perDenSize = dpToPx(context,DEFAULT_DEN_DP_X);
        int intDisplayWidth = getUsableDisplaySize(context).x;
        if(!isScreenPortrait(context)){
            intDisplayWidth = intDisplayWidth/2;
        }
        return intDisplayWidth/perDenSize;
    }
    public static int getDefaultMaxWidgetSpanY(Context context){
        final int perDenSize = dpToPx(context,C.DEFAULT_DEN_DP_Y);
        int intDisplayHeight = getUsableDisplaySize(context).y;
        int maxsize = intDisplayHeight/perDenSize;
        if(isScreenPortrait(context)){
            if(maxsize>1){
                //maxsize = maxsize - 1;// reserve 1 den for lock icon
            }
        }
        return maxsize;
    }
    public static int getWidgetSpanX(Context context,int w){
        //72+2 = 1 span
        //72dp x 72dp per 1x1 (standard)
        final int maxWidgetSpanX = getMaxWidgetSpanX(context);
        final int den = getSpanToPxX(context,1);//=1.5 span
        float a = (float)w/den%1;
        float b = 1-a;
        int result = (int)((float)w/den+b);
        if(result<=0){
            result = 1;
        }
        if(result>maxWidgetSpanX){
            result = maxWidgetSpanX;
        }
        return result;
    }
    public static int getWidgetSpanY(Context context,int h){
        //72+2 = 1 span
        //72dp x 72dp per 1x1 (standard)
        final int maxWidgetSpanY = getMaxWidgetSpanY(context);
        final int den = getSpanToPxY(context,1);//=1.5 span
        float a = (float)h/den%1;
        float b = 1-a;
        int result = (int)((float)h/den+b);
        if(result<=0){
            result = 1;
        }
        if(result>maxWidgetSpanY){
            result = maxWidgetSpanY;
        }
        return result;
    }
    public static int getDefaultWidgetSpanY(Context context,int h){
        //72+2 = 1 span
        //72dp x 72dp per 1x1 (standard)
        final int maxWidgetSpanY = getDefaultMaxWidgetSpanY(context);
        final int den = getDefaultSpanToPxY(context,1);//=1.5 span
        float a = (float)h/den%1;
        float b = 1-a;
        int result = (int)((float)h/den+b);
        if(result<=0){
            result = 1;
        }
        if(result>maxWidgetSpanY){
            result = maxWidgetSpanY;
        }
        return result;
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("RtlHardcoded")
    public static WindowManager.LayoutParams paramsStartup(){
        int what = WindowManager.LayoutParams.FLAG_FULLSCREEN |//not really useful
            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |//on top of status bar
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |//prevent wrong y position on touch not
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;//nothing, just easy to test

        try{
            what = what |
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION |//not really useful
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;//not really useful
        }catch(Exception e){
            e.printStackTrace();
        }

        WindowManager.LayoutParams windowPr;
        windowPr = new WindowManager.LayoutParams();
        windowPr.format = PixelFormat.RGBA_8888;
        if(C.isAndroid(Build.VERSION_CODES.O)){
            windowPr.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            //windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }else{
            windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// overlay status bar
        }
        windowPr.width = WindowManager.LayoutParams.MATCH_PARENT;
        windowPr.height = WindowManager.LayoutParams.MATCH_PARENT;
        windowPr.flags = what;
        windowPr.gravity = Gravity.LEFT | Gravity.TOP;
        windowPr.x = 0;
        windowPr.y = 0;
        return windowPr;
    }
    public static String getHomeLauncher(Context context){
        try{
            final Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            return context.getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return "null";
    }
}