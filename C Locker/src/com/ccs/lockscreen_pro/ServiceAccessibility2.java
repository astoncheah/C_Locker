package com.ccs.lockscreen_pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.DataWidgets;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.data.InfoWidget;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;

import java.util.List;

public class ServiceAccessibility2 extends AccessibilityService{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String strCurrentActivity = "";//,strCurrentPkg = "";

    @Override protected void onServiceConnected(){
        prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.commit();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes =
            //AccessibilityEvent.TYPES_ALL_MASK;
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType =
            //AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
            AccessibilityServiceInfo.FEEDBACK_VISUAL;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
        super.onServiceConnected();
        new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"ServiceAccessibility2>onServiceConnected: "+strCurrentActivity);

        final boolean cBoxEnableLsService = prefs.getBoolean(P.BLN_ENABLE_SERVICE,false);
        if(!cBoxEnableLsService){
            return;
        }
        if(ServiceMain.isServiceMainRunning()){
            final Intent i = new Intent(getPackageName()+C.REMOVE_STARTUP_VIEW);
            //i.putExtra("REMOVE_STARTUP_VIEW",3000);
            sendBroadcast(i);
            return;
        }
        if(SystemClock.elapsedRealtime()<1000*60){//less than 1mins is considered reboot
            SaveData s = new SaveData(getBaseContext());
            s.setOnReboot(true);
            s.setLockerRunning(false);
            startService(new Intent(getBaseContext(),ServiceMain.class));
            new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"ServiceAccessibility2>ACTION_BOOT_COMPLETED");
        }
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        /*
        new AppHandler(getBaseContext()).writeToFile(C.FILE_BACKUP_RECORD,
				"ServiceAccessibility>onAccessibilityEvent: "+
				//event.getEventType()+"/"+event.isFullScreen()+"/"+
				//event.getWindowId()+"/"+event.getContentDescription()+"\n"+
				//event.getSource()+"/\n"+
				event.getPackageName()+"/"+event.getClassName()+"\n"+
				AccessibilityEvent.eventTypeToString(event.getEventType()));//*/
        //strCurrentPkg = event.getPackageName()+"";
        //editor.putString("strNoticeCurrentPkg",strCurrentPkg);
        //editor.commit();

        //Log.e("ServiceAccessibility: ","onAccessibilityEvent1: "+AccessibilityEvent.eventTypeToString(event.getEventType()));
        //Log.e("ServiceAccessibility: ","onAccessibilityEvent2: "+event.getPackageName());
        //Log.e("ServiceAccessibility: ","onAccessibilityEvent3: "+event.getClassName());
        try{
            if(new SaveData(this).isLockerRunning()){
                String pkgName = event.getPackageName()+"";
                String clsName = event.getClassName()+"";
                strCurrentActivity = pkgName+"/"+clsName;

                editor.putString(P.STR_SERVICE_ACCESS_PKG_NAME,pkgName);
                editor.putString(P.STR_SERVICE_ACCESS_CLASS_NAME,clsName);
                editor.commit();

                if(isShortcut(pkgName)){
                    return;
                }
                if(isWidget(pkgName)){
                    return;
                }
                if(isNoticeApp(pkgName)){
                    return;
                }
                if(pkgName.equals(this.getPackageName())){
                    return;
                }
                if(pkgName.equalsIgnoreCase(C.PHONE_APK) ||
                    pkgName.equalsIgnoreCase(C.ALARM_APK_SAM) ||
                    pkgName.equalsIgnoreCase(C.ALARM_APK_LG) ||
                    clsName.equalsIgnoreCase(C.EMERGENCY_CALL) ||
                    clsName.equalsIgnoreCase(C.GO_SMS_POP_UP) ||
                    clsName.equalsIgnoreCase("com.google.android.gms.auth.uiflows.minutemaid.MinuteMaidActivity")){
                    new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"ServiceAccessibility2>unblock app: "+strCurrentActivity);
                    return;
                }
                String home = getHomeLauncher();
                if(home.equalsIgnoreCase(strCurrentActivity) ||
                    home.equalsIgnoreCase("android/com.android.internal.app.ResolverActivity") ||
                    home.contains("org.cyanogenmod.resolver") ||
                    home.contains("com.huawei.android.internal.app")){
                    new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"ServiceAccessibility2>blockHome: "+strCurrentActivity);
                    launchLocker();
                    return;
                }

                boolean cBoxBlockRecentApps = prefs.getBoolean("cBoxBlockRecentApps",false);
                if(cBoxBlockRecentApps && isRecentApps(clsName)){
                    new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"ServiceAccessibility2>isRecentApps: "+clsName);
                    launchLocker();
                }
                if(isBlockApp(pkgName)){
                    new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"ServiceAccessibility2>blockApp: "+strCurrentActivity);
                    launchLocker();
                }
            }
        }catch(Exception e){
            new MyCLocker(getBaseContext()).saveErrorLog("ServiceAccessibility2 Errors: ",e);
        }
        //com.google.android.gms/com.google.android.gms.auth.uiflows.minutemaid.MinuteMaidActivity
        //com.google.android.gm/com.google.android.gm.ui.MailActivityGmail
        //com.google.android.talk/com.google.android.apps.hangouts.phone.BabelHomeActivity
        //com.samsung.android.fingerprint.service/com.samsung.android.fingerprint.service.IdentifyBackgroundDialog
        //com.android.systemui/com.android.systemui.recents.SeparatedRecentsActivity
        //com.android.systemui/android.widget.FrameLayout = pull down statusbar
        //com.android.settings/com.android.settings.Settings = system settings
    }
    @Override protected boolean onKeyEvent(KeyEvent event){
        //Log.e("ServiceAccessibility: ","onKeyEvent: "+KeyEvent.keyCodeToString(event.getKeyCode()));
        /*
        new MyCLocker(getBaseContext()).writeToFile(C.FILE_BACKUP_RECORD,
				"ServiceAccessibility>onKeyEvent: "+
				//event.getKeyCode()+"/"+
				KeyEvent.keyCodeToString(event.getKeyCode()));//*/
        if(new SaveData(this).isLockerRunning()){
            int keyCode = event.getKeyCode();
            if(keyCode==KeyEvent.KEYCODE_HOME){
                if(C.isScreenOn(this,(WindowManager)getSystemService(Context.WINDOW_SERVICE))){
                    launchLocker();
                }
                return true;
            }else if(keyCode==KeyEvent.KEYCODE_APP_SWITCH){
                return prefs.getBoolean("cBoxBlockRecentApps",false);
            }else if(keyCode==KeyEvent.KEYCODE_BACK){
                return super.onKeyEvent(event);
            }else if(keyCode==KeyEvent.KEYCODE_HEADSETHOOK || keyCode==KeyEvent.KEYCODE_INSERT){
                return super.onKeyEvent(event);
            }

            if(event.getAction()==KeyEvent.ACTION_DOWN){
            }else if(event.getAction()==KeyEvent.ACTION_UP){
            }
        }
        return super.onKeyEvent(event);
    }
    @Override public void onInterrupt(){
        //Log.e("ServiceAccessibility: ","onInterrupt: ");
    }
    @Override protected boolean onGesture(int gestureId){
        //Log.e("ServiceAccessibility: ","onGesture: ");
        return super.onGesture(gestureId);
    }
    //
    private void launchLocker(){
        final boolean cBoxEnableLsService = prefs.getBoolean(P.BLN_ENABLE_SERVICE,false);
        if(!cBoxEnableLsService){
            return;
        }
        if(!ServiceMain.isServiceMainRunning()){
            startService(new Intent(this,ServiceMain.class));
        }
        this.sendBroadcast(new Intent(getPackageName()+C.LAUNCH_LOCKER));
    }
    private boolean isShortcut(String pkg){
        boolean isShortcut = false;
        try{
            final DataAppsSelection db = new DataAppsSelection(getBaseContext());
            final List<InfoAppsSelection> list = db.getApps(DataAppsSelection.APP_TYPE_SHORTCUT);
            for(InfoAppsSelection item : list){
                if(pkg.equals(item.getAppPkg())){
                    isShortcut = true;
                }
            }
            db.close();
        }catch(Exception e){
            new MyCLocker(getBaseContext()).saveErrorLog(null,e);
        }
        return isShortcut;
    }
    private boolean isWidget(String pkg){
        boolean isShortcut = false;
        try{
            final DataWidgets db = new DataWidgets(getBaseContext());
            final List<InfoWidget> list = db.getAllWidgets();
            for(InfoWidget item : list){
                if(pkg.equals(item.getWidgetPkgsName())){
                    isShortcut = true;
                }
            }
            db.close();
        }catch(Exception e){
            new MyCLocker(getBaseContext()).saveErrorLog(null,e);
        }
        return isShortcut;
    }
    private boolean isNoticeApp(String pkg){
        boolean isNotice = false;
        String noticePkg = prefs.getString("unblockNoticePkgName",null);
        if(noticePkg!=null && noticePkg.equals(pkg)){
            isNotice = true;
        }
        return isNotice;
    }
    private String getHomeLauncher(){
        try{
            final Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            return getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName+"/"+
                getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.name;
        }catch(Exception e){
            new MyCLocker(getBaseContext()).saveErrorLog(null,e);
        }
        return null;
    }
    private boolean isRecentApps(final String clsName){
        switch (clsName){
            case C.RECENT_APPS_CLASS:
            case C.RECENT_APPS_CLASS_V2:
            case C.RECENT_APPS_CLASS_V3:
            case C.RECENT_APPS_CLASS_V4:
                return true;
        }
        return false;
    }
    private boolean isBlockApp(final String pkgName){
        boolean isBlockApp = false;
        try{
            final boolean cBoxBlockNoneShortcutApps = prefs.getBoolean("cBoxBlockNoneShortcutApps",false);
            if(cBoxBlockNoneShortcutApps){
                final DataAppsSelection db = new DataAppsSelection(this);
                final List<InfoAppsSelection> list = db.getApps(DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                for(InfoAppsSelection item : list){
                    if(item.getAppPkg().equals(pkgName)){
                        isBlockApp = true;
                    }
                }
                db.close();
            }
        }catch(Exception e){
            new MyCLocker(getBaseContext()).saveErrorLog(null,e);
        }
        return isBlockApp;
    }
}