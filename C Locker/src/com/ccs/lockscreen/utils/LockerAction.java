package com.ccs.lockscreen.utils;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen_pro.ListRSS;
import com.ccs.lockscreen_pro.SettingsLockerMain;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;

public class LockerAction{
    private Context context;
    private MyCLocker mLocker;

    public LockerAction(Context context){
        this.context = context;
        mLocker = new MyCLocker(context);
    }

    public void launchEventDetail(final long eventID){
        final Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI,eventID);
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(uri);
        mStartActivity(i,true);
    }

    private void mStartActivity(final Intent i,boolean isShortcut){
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(isShortcut){
            i.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        }
        context.startActivity(i);
    }

    public void launchCallLogDetail(final String callId,final String phoneNo){
        try{
            if(callId==null){
                final Intent i = new Intent(Intent.ACTION_DIAL);
                if(phoneNo.contains("#")){
                    i.setData(Uri.parse("tel:"+Uri.encode(phoneNo)));
                }else{
                    i.setData(Uri.parse("tel:"+phoneNo));
                }
                mStartActivity(i,true);
            }else{
                final Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,callId);
                final Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(uri);
                mStartActivity(i,true);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    public void launchSmsDetail(final String smsId){
        final Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse(smsId));
        mStartActivity(i,true);
    }

    public void launchRssDetail(final String rssLink){
        Log.e("rss.getRssLink()3",rssLink);
        final Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(rssLink));
        mStartActivity(i,false);//dont put true!!
    }

    public void launchNoticeApp(final String pkgName){
        if(pkgName.equals(C.PHONE_APK)){
            launchAction(DataAppsSelection.TRIGGER_MISS_CALL,-1);// no need profile val
            return;
        }
        final PackageManager pm = context.getPackageManager();
        final Intent i = pm.getLaunchIntentForPackage(pkgName);
        if(i!=null){
            mStartActivity(i,true);
        }
    }

    public void launchAction(int shortcutId,int profile){
        try{
            Intent i;
            switch(shortcutId){
                case DataAppsSelection.SHORTCUT_APP_01:
                case DataAppsSelection.SHORTCUT_APP_02:
                case DataAppsSelection.SHORTCUT_APP_03:
                case DataAppsSelection.SHORTCUT_APP_04:
                case DataAppsSelection.SHORTCUT_APP_05:
                case DataAppsSelection.SHORTCUT_APP_06:
                case DataAppsSelection.SHORTCUT_APP_07:
                case DataAppsSelection.SHORTCUT_APP_08:
                case DataAppsSelection.VOLUME_UP:
                case DataAppsSelection.VOLUME_DOWN:
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
                    final DataAppsSelection shortcutData = new DataAppsSelection(context);
                    final List<InfoAppsSelection> appsList = shortcutData.getShortcutApps(shortcutId,profile);
                    final int action = appsList.get(0).getShortcutAction();
                    switch(action){
                        case DataAppsSelection.ACTION_TYPE_DO_NOTHING:
                        case DataAppsSelection.ACTION_SHORTCUT_HIDE:
                        case DataAppsSelection.ACTION_UNLOCK:
                            break;
                        case DataAppsSelection.ACTION_DEFAULT:
                            launchDefaultApp(shortcutId);
                            break;
                        case DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP:
                            launchCustomApp(appsList);
                            break;
                        case DataAppsSelection.ACTION_FAVORITE_SHORTCUT:
                            launchFavoriteShortcut(appsList);
                            break;
                        case DataAppsSelection.ACTION_DIRECT_SMS:
                            sendSMS(appsList);
                            break;
                        case DataAppsSelection.ACTION_DIRECT_CALL:
                            call(appsList);
                            break;
                        case DataAppsSelection.ACTION_EXPAND_STATUS_BAR:
                            openStatusBar();
                            break;
                    }
                    break;
                case DataAppsSelection.TRIGGER_CALL:
                    i = new Intent(Intent.ACTION_DIAL);
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.TRIGGER_CAM:
                    i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.TRIGGER_SETTINGS:
                    new SaveData(context).setSettingsCalledByLocker(true);
                    i = new Intent(context,SettingsLockerMain.class);
                    mStartActivity(i,false);
                    break;
                case DataAppsSelection.TRIGGER_CALENDAR:
                    long startMillis = System.currentTimeMillis();
                    Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                    builder.appendPath("time");
                    ContentUris.appendId(builder,startMillis);
                    i = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.TRIGGER_GMAIL:
                    i = new Intent(Intent.ACTION_MAIN);
                    i.setClassName("com.google.android.gm","com.google.android.gm.ConversationListActivityGmail");
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.TRIGGER_MISS_CALL:
                    i = new Intent(Intent.ACTION_VIEW);
                    i.setType(CallLog.Calls.CONTENT_TYPE);
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.TRIGGER_MISS_SMS:
                    i = new Intent(Intent.ACTION_MAIN);
                    i.setType("vnd.android-dir/mms-sms");
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.TRIGGER_RSS:
                    i = new Intent(context,ListRSS.class);
                    mStartActivity(i,false);
                    break;
                case DataAppsSelection.TRIGGER_STATUSBAR:
                    openStatusBar();
                    break;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    public void launchDefaultApp(int intAppVal){
        try{
            Intent i;
            switch(intAppVal){
                case DataAppsSelection.SHORTCUT_APP_03:
                    //i = new Intent("android.media.action.IMAGE_CAPTURE");
                    //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    //context.startActivity(i);
                    i = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.SHORTCUT_APP_04:
                    i = context.getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.SHORTCUT_APP_05:
                    i = new Intent(Intent.ACTION_VIEW,Uri.parse("http://google.com"));
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.SHORTCUT_APP_06:
                    i = context.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.SHORTCUT_APP_07:
                    i = new Intent(Intent.ACTION_DIAL);
                    mStartActivity(i,true);
                    break;
                case DataAppsSelection.SHORTCUT_APP_08:
                    i = new Intent(Intent.ACTION_MAIN);
                    i.setType("vnd.android-dir/mms-sms");
                    mStartActivity(i,true);
                    break;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void launchCustomApp(final List<InfoAppsSelection> appsList){
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.setComponent(new ComponentName(appsList.get(0).getAppPkg(),appsList.get(0).getAppClass()));
        //i.setClassName(appsList.get(0).getAppPkg(),appsList.get(0).getAppClass());
        mStartActivity(i,true);
    }

    private void launchFavoriteShortcut(final List<InfoAppsSelection> appsList){
        try{
            //dont use Intent.URI_INTENT_SCHEME
            Intent i = Intent.parseUri(appsList.get(0).getAppPkg(),0);
            mStartActivity(i,true);
        }catch(URISyntaxException e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void sendSMS(final List<InfoAppsSelection> appsList){
        final Intent i = new Intent(Intent.ACTION_SENDTO);
        i.setData(Uri.parse("smsto:"+appsList.get(0).getAppSubInfo()));
        mStartActivity(i,true);
    }

    private void call(final List<InfoAppsSelection> appsList){
        try{
            final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            final boolean directCall = prefs.getBoolean("directCall",false);
            Intent i;
            if(directCall){
                i = new Intent(Intent.ACTION_CALL); //CAN CALL DIRECTLY, DANGEROUS!!
            }else{
                i = new Intent(Intent.ACTION_DIAL);
            }
            final String phoneNo = appsList.get(0).getAppSubInfo();
            if(phoneNo.contains("#")){
                i.setData(Uri.parse("tel:"+Uri.encode(phoneNo)));
            }else{
                i.setData(Uri.parse("tel:"+phoneNo));
            }
            mStartActivity(i,true);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void openStatusBar(){
        final int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        try{
            final Object service = context.getSystemService("statusbar");
            final Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method expand = null;
            if(service!=null){
                if(currentApiVersion<=Build.VERSION_CODES.JELLY_BEAN){
                    expand = statusbarManager.getMethod("expand");
                }else{
                    expand = statusbarManager.getMethod("expandNotificationsPanel");
                }
                expand.setAccessible(true);
                expand.invoke(service);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
}
