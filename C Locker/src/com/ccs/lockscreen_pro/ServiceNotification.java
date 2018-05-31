package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.WindowManager;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen.utils.StartupView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressLint("NewApi")
public class ServiceNotification extends NotificationListenerService{
    //new MyCLocker(this).writeToFile(C.FILE_BACKUP_RECORD,"MyBackupData>copyFile: No file found");
    public static final String IMAGE_FORMAT = ".pg_c_locker";
    private static final long SCREEN_ON_TIME_GAP = DateUtils.MINUTE_IN_MILLIS*10;
    private static final long NEW_POST_TIME_GAP = 200;
    private ArrayList<InfoAppsSelection> mPendingNoticeList;
    private SensorManager sm;
    private boolean isRemoveRunning = false, isScreenOn = true;
    private Handler handler = new Handler();
    private MyCLocker mLocker;
    private WindowManager windowManager;
    private StartupView startupBlockView;
    private PowerManager.WakeLock mWakeLock;

    private Runnable turnOffScreen = new Runnable(){
        @Override
        public void run(){
            turnOffScreen("turnOffScreen");
        }
    };
    private SensorEventListener listener = new SensorEventListener(){
        private float f = 0;
        private boolean isHandlerRunning = false;

        @Override
        public void onSensorChanged(SensorEvent event){
            //event.values[0] = left
            //event.values[1] = top
            //event.values[2] = front
            try{
                switch(event.sensor.getType()){
                    case Sensor.TYPE_PROXIMITY:
                        f = event.values[0];
                        //appHandler.writeToFile("ServiceNotification>listener>TYPE_PROXIMITY 1: "+f+"/");
                        if(!isHandlerRunning){
                            isHandlerRunning = true;
                            handler.postDelayed(new Runnable(){
                                @Override
                                public void run(){
                                    if(f<1.0f){//object is bloking the sensor
                                        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>SensorEventListener>object is bloking the PROXIMITY sensor");
                                    }else{
                                        //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>SensorEventListener>turnOnScreen");
                                        handleScreenOn();//turn on screen if not in pocket
                                    }
                                    sm.unregisterListener(listener);
                                    f = 0;
                                    isHandlerRunning = false;
                                }
                            },500);
                        }
                        break;
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor arg0,int arg1){
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            try{
                final String action = intent.getAction();
                if(action.equals(getPackageName()+C.REMOVE_STARTUP_VIEW)){
                    int val = intent.getIntExtra("REMOVE_STARTUP_VIEW",3000);
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            removeStartupWindow();
                        }
                    },val);
                    return;
                }else if(action.equals(Intent.ACTION_SCREEN_ON)){
                    isScreenOn = true;
                    //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>ACTION_SCREEN_ON");
                    return;
                }else if(action.equals(Intent.ACTION_SCREEN_OFF)){
                    isScreenOn = false;
                    //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>ACTION_SCREEN_OFF");
                    return;
                }else if(action.equals(getPackageName()+C.CANCEL_SCREEN_TIMEOUT)){
                    handler.removeCallbacks(turnOffScreen);
                    turnOffScreen("CANCEL_SCREEN_TIMEOUT");
                    return;
                }
                final String command = intent.getStringExtra(C.NOTICE_COMMAND);
                switch(command){
                    case C.RUN_PENDING_INTENT:
                        runPendingIntent(intent);
                        break;
                    case C.RUN_ACTION_PENDING_INTENT:
                        runActionPendingIntent(context,intent);
                        break;
                    case C.CLEAR_ALL_NOTICE:
                        if(!isRemoveRunning){
                            removeList("",-1);
                        }
                        break;
                    case C.CLEAR_SINGLE_NOTICE:
                        final String pkg = intent.getStringExtra(C.NOTICE_APP_PKG_NAME);
                        final long imgId = intent.getLongExtra(C.NOTICE_APP_PKG_ID,-1);//post time
                        //final String tag = intent.getStringExtra(C.NOTICE_APP_PKG_TAG);
                        //final int id = intent.getIntExtra(C.NOTICE_APP_PKG_ID,-1);
                        //Log.e("CLEAR_SINGLE_NOTICE",pkg+"/"+tag+"/"+id);
                        //if(pkg!=null && tag!=null && id!=-1){
                        if(!isRemoveRunning){
                            removeList(pkg,imgId);
                        }
                        break;
                    case C.SHOW_NOTICE_APPS_LIST:
                        final int requestFrom = intent.getIntExtra(C.NOTICE_FROM,-1);//-1 = no one
                        if(requestFrom==C.NOTICE_NEW){
                            //mLocker.writeToFile("ServiceNotification>NOTICE_NEW");
                            ArrayList<InfoAppsSelection> list = filterList();
                            Intent i = new Intent(context.getPackageName()+C.NOTICE_LISTENER_LOCKER);
                            i.putExtra(C.NOTICE_FROM,C.NOTICE_NEW);
                            i.putExtra(C.NOTICE_COUNT,0);
                            i.putParcelableArrayListExtra(C.NOTICE_ALL_APP_LIST,list);
                            sendBroadcast(i);
                        }else if(requestFrom==C.NOTICE_UPDATE){
                            //mLocker.writeToFile("ServiceNotification>NOTICE_UPDATE");
                            ArrayList<InfoAppsSelection> list = filterList();
                            Intent i = new Intent(context.getPackageName()+C.NOTICE_LISTENER_LOCKER);
                            i.putExtra(C.NOTICE_FROM,C.NOTICE_UPDATE);
                            i.putExtra(C.NOTICE_COUNT,0);
                            i.putParcelableArrayListExtra(C.NOTICE_ALL_APP_LIST,list);
                            sendBroadcast(i);
                        }
                        break;
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private void runPendingIntent(Intent intent){
            final String pkg = intent.getStringExtra(C.NOTICE_APP_PKG_NAME);
            final long imgId = intent.getLongExtra(C.NOTICE_APP_PKG_ID,-1);//post time
            if(pkg==null){
                return;
            }
            if(mPendingNoticeList==null || mPendingNoticeList.size()==0){
                return;
            }
            try{
                for(int i = mPendingNoticeList.size(); i>0; i--){//reverse
                    int index = i-1;
                    final String appPkg = mPendingNoticeList.get(index).getAppPkg();
                    final long id = mPendingNoticeList.get(index).getAppPostTime();
                    if(appPkg.equals(pkg) && id==imgId){
                        Log.e("RUN_PENDING_INTENT",appPkg);
                        //if(appPkg.equals("com.google.android.gm")){
                        //context.sendBroadcast(new Intent(context.getPackageName()+C.LAUNCH_LOCKER));
                        //}
                        final PendingIntent pi = mPendingNoticeList.get(index).getPi();
                        if(pi==null){
                            return;
                        }
                        pi.send();
                        if(!isRemoveRunning){
                            removeList(pkg,imgId);
                        }
                        return;
                    }
                }
            }catch(CanceledException e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private void runActionPendingIntent(Context context,Intent intent){
            final String pkg = intent.getStringExtra(C.NOTICE_APP_PKG_NAME);
            final long imgId = intent.getLongExtra(C.NOTICE_APP_PKG_ID,-1);//post time
            final String title = intent.getStringExtra(C.ACTION_PENDING_INTENT_TITLE);
            final String msg = intent.getStringExtra(C.ACTION_PENDING_INTENT_MSG);
            if(pkg==null){
                return;
            }
            if(imgId==-1){
                return;
            }
            if(title==null){
                return;
            }
            if(mPendingNoticeList==null || mPendingNoticeList.size()==0){
                return;
            }
            try{
                for(int i = mPendingNoticeList.size(); i>0; i--){//reverse
                    int index = i-1;
                    final String appPkg = mPendingNoticeList.get(index).getAppPkg();
                    final long id = mPendingNoticeList.get(index).getAppPostTime();
                    if(appPkg.equals(pkg) && id==imgId){
                        Notification.Action[] actions = mPendingNoticeList.get(index).getActions();
                        if(actions==null){
                            return;
                        }
                        int count = actions.length;
                        for(Notification.Action action : actions){
                            if(count<0){
                                break; //No infinite loops, has happened once
                            }
                            if(action.title.equals(title)){
                                if(C.isAndroid(Build.VERSION_CODES.KITKAT_WATCH)){
                                    RemoteInput[] inputs = action.getRemoteInputs();
                                    if(inputs!=null){
                                        for(RemoteInput input : inputs){
                                            if(input.getAllowFreeFormInput() && msg!=null){
                                                sendDirectReply(context,action,inputs,input,msg);
                                                if(!isRemoveRunning){
                                                    removeList(pkg,imgId);
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                                if(actionIntent(action)){
                                    if(!isRemoveRunning){
                                        removeList(pkg,imgId);
                                    }
                                    return;
                                }
                            }
                            i--;
                        }
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
        @TargetApi(Build.VERSION_CODES.KITKAT)
        private boolean actionIntent(Notification.Action action) throws Exception{
            PendingIntent pi = action.actionIntent;
            if(pi==null){
                return false;
            }
            pi.send();
            return true;
        }
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        private void sendDirectReply(Context context,Notification.Action action,RemoteInput[] rInputs,RemoteInput input,String msg) throws CanceledException{
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putCharSequence(input.getResultKey(),msg);
            RemoteInput.addResultsToIntent(rInputs, intent, bundle);
            action.actionIntent.send(context,0,intent);
        }
        private ArrayList<InfoAppsSelection> filterList(){
            final ArrayList<InfoAppsSelection> list = new ArrayList<>();
            try{
                final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
                final boolean cBoxGroupNotices = prefs.getBoolean("cBoxGroupNotices",true);
                if(mPendingNoticeList==null || mPendingNoticeList.size()==0){
                    initialList();
                }
                if(!cBoxGroupNotices){
                    return mPendingNoticeList;
                }
                if(mPendingNoticeList!=null){
                    /*Collections.sort(mPendingNoticeList, new Comparator<InfoAppsSelection>() {
                        @Override
        				public int compare(InfoAppsSelection val1, InfoAppsSelection val2) {
        					Long a = val1.getAppPostTime();
        					Long b = val2.getAppPostTime();
        					return b.compareTo(a);// the newest notice on top
        				}
        		    });//*/
                    //Collections.reverse(mPendingNoticeList);
                    for(InfoAppsSelection item : mPendingNoticeList){//reverse
                        //Log.e("filterList 0",item.getAppPkg()+"/"+item.getAppPostCount()+"/"+getTime(item.getAppPostTime()));
                        if(!isRepeatedNotice(item,list)){
                            //Log.e("filterList 1",item.getAppPkg()+"/"+item.getAppPostCount()+"/"+getTime(item.getAppPostTime()));
                            final InfoAppsSelection app = new InfoAppsSelection();
                            app.setAppPkg(item.getAppPkg());
                            app.setAppTag(item.getAppTag());
                            app.setNoticeAppId(item.getNoticeAppId());
                            app.setAppPostCount(item.getAppPostCount());
                            app.setAppPostTime(item.getAppPostTime());
                            app.setAppSubInfo(item.getAppSubInfo());//title
                            app.setAppSubInfoContent(item.getAppSubInfoContent());//content
                            app.setActionTitles(item.getActionTitles());
                            app.setActionIcons(item.getActionIcons());
                            app.setAllowDirectInput(item.getAllowDirectInput());
                            list.add(app);//info content not used now));
                        }
                    }
                }
                Collections.sort(list,new Comparator<InfoAppsSelection>(){
                    @Override
                    public int compare(InfoAppsSelection val1,InfoAppsSelection val2){
                        Long a = val1.getAppPostTime();
                        Long b = val2.getAppPostTime();
                        return b.compareTo(a);// the newest notice on top
                    }
                });//*/
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return list;
        }
        private boolean isRepeatedNotice(InfoAppsSelection item,
            final ArrayList<InfoAppsSelection> appCheckList){
            try{
                for(InfoAppsSelection list : appCheckList){
                    if(list.getAppPkg().equals(item.getAppPkg()) && item.getAppPostCount()==0){
                        return true;
                    }
                    if(list.getAppPkg().equals(item.getAppPkg())){
                        if(list.getAppPostCount()==item.getAppPostCount()){
                            return true;
                        }
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return false;
        }
    };
    @Override
    public void onCreate(){
        super.onCreate();
        try{
            mLocker = new MyCLocker(this);
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>onCreate>loaded");

            IntentFilter filter = new IntentFilter();
            filter.addAction(getPackageName()+C.NOTICE_LISTENER_SERVICE);
            filter.addAction(getPackageName()+C.REMOVE_STARTUP_VIEW);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(getPackageName()+C.CANCEL_SCREEN_TIMEOUT);
            registerReceiver(receiver,filter);

            if(SystemClock.elapsedRealtime()<1000*60){//less than 1mins is considered reboot
                SaveData s = new SaveData(getBaseContext());
                s.setOnReboot(true);
                s.setLockerRunning(false);
                addStartupWindow();
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void addStartupWindow(){
        final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final boolean cBoxEnableLsService = prefs.getBoolean(P.BLN_ENABLE_SERVICE,false);
        if(!cBoxEnableLsService){
            return;
        }
        if(ServiceMain.isServiceMainRunning()){
            removeStartupWindow();
            return;
        }
        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        if(startupBlockView==null){
            startupBlockView = new StartupView(getBaseContext());
            windowManager.addView(startupBlockView,C.paramsStartup());

            SharedPreferences.Editor editor = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE).edit();
            editor.putBoolean("startupBlockView",true);
            editor.commit();
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>addWindow");
            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    removeStartupWindow();
                }
            },1000*60*2);//will remove in 3 mins if any bug.
        }
    }
    private void removeStartupWindow(){
        try{
            if(startupBlockView==null){
                return;
            }
            windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
            windowManager.removeView(startupBlockView);
            startupBlockView = null;

            SharedPreferences.Editor editor = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE).edit();
            editor.putBoolean("startupBlockView",false);
            editor.commit();
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>removeWindow");
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        try{
            if(!sbn.getPackageName().equals(getPackageName()) && !sbn.isOngoing()){
                if(!isBlockedApp(sbn.getPackageName())){
                    if(!isMyNoticeAppPending(sbn,SCREEN_ON_TIME_GAP)){
                        registerSensor();
                    }
                    initialList();
                    final Intent i = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
                    i.putExtra(C.NOTICE_COMMAND,C.SHOW_NOTICE_APPS_LIST);
                    i.putExtra(C.NOTICE_FROM,C.NOTICE_UPDATE);
                    sendBroadcast(i);
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @Override
    public void onNotificationRemoved(final StatusBarNotification sbn){
        try{
            if(!sbn.getPackageName().equals(getPackageName()) && !sbn.isOngoing()){
                if(!isRemoveRunning){
                    if(!isMyNoticeAppPending(sbn,NEW_POST_TIME_GAP)){
                        removeList(sbn.getPackageName(),sbn.getPostTime());
                        if(mPendingNoticeList==null){
                            removeAllBitmap();
                        }
                        if(mPendingNoticeList!=null && mPendingNoticeList.size()==0){
                            removeAllBitmap();
                        }
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            unregisterReceiver(receiver);
            if(sm!=null){
                sm.unregisterListener(listener);
            }
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>onDestroy>loaded");
            mLocker.close();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private boolean isBlockedApp(final String pkgName){
        boolean isBlockedApp = false;
        try{
            if(pkgName.equals("com.android.server.telecom")){//new missed call apk
                return isBlockedApp;// calling
            }
            if(pkgName.equals(C.PHONE_APK)){
                return isBlockedApp;// calling
            }
            final DataAppsSelection db = new DataAppsSelection(this);
            final List<InfoAppsSelection> list = db.getApps(DataAppsSelection.APP_TYPE_BLOCKED_NOTICE_APPS);
            if(list==null || list.size()==0){
                return false;
            }else{
                for(InfoAppsSelection item : list){
                    if(item.getAppPkg().equals(pkgName)){
                        return true;
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return isBlockedApp;
    }
    private boolean isMyNoticeAppPending(StatusBarNotification sbnNew,long timeGap){
        try{
            if(mPendingNoticeList!=null){
                if(!isBlockedApp(sbnNew.getPackageName())){
                    for(int i = mPendingNoticeList.size(); i>0; i--){//reverse
                        int index = i-1;
                        if(mPendingNoticeList.get(index).getAppPkg().equals(sbnNew.getPackageName())){
                            long postTime = mPendingNoticeList.get(index).getAppPostTime();
                            long now = System.currentTimeMillis();
                            long gap = now-postTime;
                            //Log.e("isMyNoticeAppPending",postTime+"/"+now+"/"+gap+"/"+timeGap);
                            if(gap<timeGap){
                                return true;
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return false;
    }
    private void registerSensor(){
        try{
            if(sm==null){
                sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            }
            sm.registerListener(listener,sm.getDefaultSensor(Sensor.TYPE_PROXIMITY),SensorManager.SENSOR_DELAY_UI);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void initialList(){
        try{
            if(mPendingNoticeList!=null){
                mPendingNoticeList.clear();
            }
            mPendingNoticeList = new ArrayList<>();
            final StatusBarNotification[] sbnPending = getActiveNotifications();
            if(sbnPending!=null){
                //int count = 1;
                for(StatusBarNotification sbn : sbnPending){
                    //mLocker.writeToFile(C.FILE_BACKUP_RECORD,count+") ServiceNotification>initialList>all>"+log+
                    //"\n  pkg: "+sbn.getPackageName()+
                    //"/id: "+sbn.getId()+
                    //"/key: "+sbn.getKey());
                    if(!sbn.getPackageName().equals(getPackageName()) && !sbn.isOngoing()){
                        if(!isBlockedApp(sbn.getPackageName())){
                            //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>initialList>add>"+log+
                            //"/pkg: "+sbn.getPackageName()+
                            //"/id: "+sbn.getId()+
                            //"/key: "+sbn.getKey());
                            addList(sbn);
                        }
                    }
                    //count++;
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void addList(StatusBarNotification sbn){
        try{
            String title = null;
            String content = null;
            int inboxCount = -1;
            final Notification n = sbn.getNotification();
            if(C.isAndroid(Build.VERSION_CODES.KITKAT)){
                final Bundle extras = n.extras;
                try{
                    title = extras.get(Notification.EXTRA_TITLE).toString();
                }catch(Exception ignored){
                }
                try{
                    content = extras.get(Notification.EXTRA_TEXT).toString();
                }catch(Exception ignored){
                    content = "";
                }

                String bigText = null;
                try{
                    if(extras.getString(Notification.EXTRA_TEMPLATE,"").equals("android.app.Notification$InboxStyle") && extras.containsKey(Notification.EXTRA_TEXT_LINES)){
                        CharSequence[] textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES);
                        bigText = "";
                        if(textLines!=null){
                            if(sbn.getPackageName().equals("com.whatsapp")){
                                inboxCount = 0;
                                for(int i = textLines.length; i>=0; i--){
                                    CharSequence line = textLines[i-1];
                                    bigText += line+"\n";
                                    inboxCount++;
                                }
                            }else{
                                inboxCount = 0;
                                for(CharSequence line : textLines){
                                    bigText += line+"\n";
                                    inboxCount++;
                                }
                            }
                        }
                    }else{
                        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                            bigText = extras.get(Notification.EXTRA_BIG_TEXT).toString();
                        }
                    }
                    if(extras.containsKey(Notification.EXTRA_TITLE_BIG)){
                        title = extras.getCharSequence(Notification.EXTRA_TITLE_BIG,title).toString();
                    }
                }catch(Exception ignored){
                    inboxCount = -1;
                }
                if(bigText!=null && bigText.length()>3){
                    content = bigText.trim();
                }

                final Bitmap bm = extras.getParcelable(Notification.EXTRA_LARGE_ICON);
                saveImage(bm,sbn.getPackageName(),sbn.getPostTime());
            }else{
                if(n.tickerText!=null){
                    content = n.tickerText.toString();
                }
                if(n.largeIcon!=null){
                    saveImage(n.largeIcon,sbn.getPackageName(),sbn.getPostTime());
                }
            }
            //mLocker.writeToFile("ServiceNotification>addList>tickerText: "+title+"/"+content);

            final InfoAppsSelection app = new InfoAppsSelection();
            app.setAppPkg(sbn.getPackageName());
            app.setAppTag(sbn.getTag());
            if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                app.setAppKey(sbn.getKey());
            }
            app.setAppSubInfoContent(content);
            app.setNoticeAppId(sbn.getId());
            app.setAppPostTime(sbn.getPostTime());
            app.setAppSubInfo(title);//title
            //Log.e(sbn.getPackageName(),inboxCount+"/"+n.number);
            if(inboxCount>0 && n.number<=0){
                app.setAppPostCount(inboxCount);
            }else{
                app.setAppPostCount(n.number);
            }
            app.setPi(n.contentIntent);

            Notification.Action[] actions = n.actions;
            ArrayList<String> actionTitles = new ArrayList<>();
            ArrayList<Integer> actionIcons = new ArrayList<>();
            //add block notice action
            actionTitles.add("");
            actionIcons.add(R.drawable.ic_not_interested_white_48dp);
            if(actions!=null){
                int i = actions.length;
                for(Notification.Action action : actions){
                    if(i<0){
                        break; //No infinite loops, has happened once
                    }
                    if(C.isAndroid(Build.VERSION_CODES.KITKAT_WATCH)){
                        RemoteInput[] inputs = action.getRemoteInputs();
                        if(inputs!=null){
                            for(RemoteInput input : inputs){
                                if(input.getAllowFreeFormInput()){
                                    app.setAllowDirectInput(action.title.toString());
                                }else{
                                    actionTitles.add(action.title.toString());
                                    actionIcons.add(action.icon);
                                }
                            }
                        }else{
                            actionTitles.add(action.title.toString());
                            actionIcons.add(action.icon);
                        }
                    }else{
                        actionTitles.add(action.title.toString());
                        actionIcons.add(action.icon);
                    }
                    i--;
                }
            }

            app.setActions(actions);
            app.setActionTitles(actionTitles);
            app.setActionIcons(actionIcons);

            mPendingNoticeList.add(app);//info content not used now
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @SuppressWarnings("deprecation")
    private void removeList(final String pkgName,long imgId){
        if(mPendingNoticeList==null){
            return;
        }
        if(pkgName==null){
            return;
        }
        try{
            isRemoveRunning = true;
            if(pkgName.equals("")){
                removeAllBitmap();
                cancelAllNotifications();
                mPendingNoticeList.clear();

                final Intent intent = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
                intent.putExtra(C.NOTICE_COMMAND,C.SHOW_NOTICE_APPS_LIST);
                intent.putExtra(C.NOTICE_FROM,C.NOTICE_UPDATE);
                sendBroadcast(intent);
            }else{
                final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
                final boolean cBoxGroupNotices = prefs.getBoolean("cBoxGroupNotices",true);
                if(!cBoxGroupNotices){
                    for(int i = mPendingNoticeList.size(); i>0; i--){//reverse
                        int index = i-1;
                        final String appPkg = mPendingNoticeList.get(index).getAppPkg();
                        final String key = mPendingNoticeList.get(index).getAppKey();
                        final long id = mPendingNoticeList.get(index).getAppPostTime();

                        if(appPkg.equals(pkgName) && id==imgId){
                            removeBitmap(pkgName,imgId);
                            if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                                cancelNotification(key);
                            }else{
                                cancelNotification(pkgName,mPendingNoticeList.get(index).getAppTag(),mPendingNoticeList.get(index).getNoticeAppId());
                            }
                            mPendingNoticeList.remove(index);

                            final Intent intent = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
                            intent.putExtra(C.NOTICE_COMMAND,C.SHOW_NOTICE_APPS_LIST);
                            intent.putExtra(C.NOTICE_FROM,C.NOTICE_UPDATE);
                            sendBroadcast(intent);
                        }
                    }
                }else{
                    for(int i = mPendingNoticeList.size(); i>0; i--){//reverse
                        int index = i-1;
                        if(mPendingNoticeList.get(index).getAppPkg().equals(pkgName)){
                            removeBitmap(pkgName,mPendingNoticeList.get(index).getAppPostTime());
                            if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                                String key = mPendingNoticeList.get(index).getAppKey();
                                cancelNotification(key);
                            }else{
                                cancelNotification(pkgName,mPendingNoticeList.get(index).getAppTag(),mPendingNoticeList.get(index).getNoticeAppId());
                            }
                            mPendingNoticeList.remove(index);

                            final Intent intent = new Intent(getPackageName()+C.NOTICE_LISTENER_SERVICE);
                            intent.putExtra(C.NOTICE_COMMAND,C.SHOW_NOTICE_APPS_LIST);
                            intent.putExtra(C.NOTICE_FROM,C.NOTICE_UPDATE);
                            sendBroadcast(intent);
                        }
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        isRemoveRunning = false;
    }
    private void saveImage(Bitmap bmIcon,String pkgName,long imgId){
        try{
            removeBitmap(pkgName,imgId);
            if(bmIcon!=null){
                //dont save if image quality too low, 72x72 hangout/gmail notice
                //if(bmIcon.getWidth()<100 && bmIcon.getHeight()<100){
                //return;
                //}
                final Bitmap resizeBm = Bitmap.createScaledBitmap(getRoundedCornerBitmap(bmIcon),144,144,false);
                final File fileIcon = new File(C.EXT_STORAGE_DIR,"/"+pkgName+"_"+imgId+IMAGE_FORMAT);
                if(!fileIcon.getParentFile().exists()){
                    fileIcon.getParentFile().mkdir();
                }
                final OutputStream outStream = new FileOutputStream(fileIcon);
                resizeBm.compress(Bitmap.CompressFormat.PNG,100,outStream);

                outStream.flush();
                outStream.close();
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void removeBitmap(final String pkgName,long imgId){
        try{
            final File fileIcon = new File(C.EXT_STORAGE_DIR,"/"+pkgName+"_"+imgId+IMAGE_FORMAT);
            if(fileIcon.exists()){
                fileIcon.delete();
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private void removeAllBitmap(){
        try{
            final File file = new File(C.EXT_STORAGE_DIR);
            if(file.exists()){
                for(File item : file.listFiles()){
                    if(item.getAbsolutePath().contains(IMAGE_FORMAT)){
                        item.delete();
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    private Bitmap getRoundedCornerBitmap(Bitmap bitmap){
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = 12;

        paint.setAntiAlias(true);
        canvas.drawARGB(0,0,0,0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF,roundPx,roundPx,paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap,rect,rect,paint);

        return output;
    }
    private void handleScreenOn(){
        try{
            //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>turnOnScreen: "+new SaveData(this).isLockerRunning());
            if(new SaveData(this).isLockerRunning()){
                ComponentName cn = C.getRunningTask(this,0);
                //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>turnOnScreen: "+cn);
                if(cn!=null){
                    if(cn.getPackageName().equals(C.PHONE_APK) ||        // dont block phone.apk "com.android.phone/com.android.phone.InCallScreen"
                        cn.getPackageName().equals(C.ALARM_APK_SAM) ||    // dont block alarm.apk "com.sec.android.app.clockpackage/com.sec.android.app.clockpackage.alarm.AlarmAlert"
                        cn.getPackageName().equals(C.ALARM_APK_LG)){    // dont block alarm.apk "com.lge.clock/com.lge.clock.alarmclock.AlarmAlert"
                        return;
                    }
                }
                final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
                final boolean cBoxNoticeScreenOn = prefs.getBoolean("cBoxNoticeScreenOn",false);
                if(!isScreenOn && cBoxNoticeScreenOn){
                    sendBroadcast(new Intent(getPackageName()+C.LAUNCH_LOCKER));
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            //new SaveData(ServiceNotification.this).setTurnScreenOff(true);
                            //new SaveData(ServiceNotification.this).setScreenTimeoutNotice(5000);
                            turnOnScreen();
                            handler.postDelayed(turnOffScreen,8000);//turn off screen after 5sec
                        }
                    },500);//slight delay to turn on screen
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @SuppressWarnings("deprecation")
    private void turnOnScreen(){
        try{
            PowerManager mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"ServiceNotification");
            mWakeLock.acquire();
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>turnOnScreen>wl.acquire()");
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
    private void turnOffScreen(String log){
        try{
            if(mWakeLock!=null && mWakeLock.isHeld()){
                mWakeLock.release();
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ServiceNotification>turnOnScreen>wl.release(): "+log);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
}