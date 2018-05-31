package com.ccs.lockscreen.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.CheckLongPressHelper;
import com.ccs.lockscreen.appwidget.LockerAppWidgetHost;
import com.ccs.lockscreen.appwidget.LockerAppWidgetHostView;
import com.ccs.lockscreen.appwidget.MyWidgetHelper;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.T;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen_pro.ListAppsSelector;
import com.ccs.lockscreen_pro.ListWidgetApps;
import com.ccs.lockscreen_pro.SettingsBottombar;

import java.util.List;

public class SettingsNotices extends Fragment{
    private static final int ENABLE_STATUSBAR_NOTICES = 1;
    private MyCLocker appHandler;
    private Activity context;
    private Fragment fragment;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private AppWidgetManager mAppWidgetManager;
    private LockerAppWidgetHost mAppWidgetHost;
    private LinearLayout lytEnableStatusBarNotices, lytBlockNoticeApps, lytLollipopStyleOptions, lytNoticeSideIconStyleOptions,
        lytLollipopStyleWidget, lytLollipopStyleWidgeSample, lytNoticeScreenOn, lytShowSenderPicture, lytShowNoticeContent,
        lytGroupNotices,lytDisableHeadsUp, lytBottombar;
    private CheckBox cBoxEnableStatusBarNotices, cBoxNoticeScreenOn, cBoxShowSenderPicture, cBoxShowNoticeContent, cBoxGroupNotices;
    private RadioButton cBoxNoticeStyleLollipop, cBoxNoticeSideIconStyle, cBoxNoticeLayoutLeft, cBoxNoticeLayoutRight, cBoxNoticeLayoutBgEmpty, cBoxNoticeLayoutBgBlack;

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case MyWidgetHelper.REQUEST_PICK_APPWIDGET_RESULT:
                    if(resultCode==Activity.RESULT_OK){
                        requestBindWidget(data);
                    }
                    break;
                case MyWidgetHelper.REQUEST_BIND_APPWIDGET_RESULT:
                    if(resultCode==Activity.RESULT_OK){
                        configureWidget(data);
                    }else{
                        if(data!=null){
                            final Bundle extras = data.getExtras();
                            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                            deleteWidget(appWidgetId);
                        }
                    }
                    break;
                case MyWidgetHelper.REQUEST_CREATE_APPWIDGET_RESULT:
                    if(resultCode==Activity.RESULT_OK){
                        saveAppsWidget(data);
                    }else{
                        if(data!=null){
                            final Bundle extras = data.getExtras();
                            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                            deleteWidget(appWidgetId);
                        }
                    }
                    break;
                case ENABLE_STATUSBAR_NOTICES:
                    if(C.isNotificationEnabled(context)){
                        cBoxEnableStatusBarNotices.setChecked(true);
                        lytEnableStatusBarNotices.setBackgroundColor(0);
                        return;
                    }
                    cBoxEnableStatusBarNotices.setChecked(false);
                    lytEnableStatusBarNotices.setBackgroundColor(Color.parseColor("#66ff0000"));
                    break;
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        appHandler = new MyCLocker(context);
        fragment = this;
        //container.setLayoutTransition(new LayoutTransition());

        try{
            if(mAppWidgetManager==null){
                mAppWidgetManager = AppWidgetManager.getInstance(context);
            }
            if(mAppWidgetHost==null){
                mAppWidgetHost = LockerAppWidgetHost.getInstance(context,C.APPWIDGET_HOST_ID);
                mAppWidgetHost.startListening();
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }

        final View view = inflater.inflate(R.layout.settings_notices,container,false);

        lytEnableStatusBarNotices = (LinearLayout)view.findViewById(R.id.lytEnableStatusBarNotices);
        lytBlockNoticeApps = (LinearLayout)view.findViewById(R.id.lytBlockNoticeApps);
        lytLollipopStyleOptions = (LinearLayout)view.findViewById(R.id.lytLollipopStyleOptions);
        lytNoticeSideIconStyleOptions = (LinearLayout)view.findViewById(R.id.lytNoticeSideIconStyleOptions);
        lytLollipopStyleWidget = (LinearLayout)view.findViewById(R.id.lytLollipopStyleWidget);
        lytLollipopStyleWidgeSample = (LinearLayout)view.findViewById(R.id.lytLollipopStyleWidgeSample);
        lytNoticeScreenOn = (LinearLayout)view.findViewById(R.id.lytNoticeScreenOn);
        lytShowSenderPicture = (LinearLayout)view.findViewById(R.id.lytShowSenderPicture);
        lytShowNoticeContent = (LinearLayout)view.findViewById(R.id.lytShowNoticeContent);
        lytGroupNotices = (LinearLayout)view.findViewById(R.id.lytGroupNotices);

        cBoxEnableStatusBarNotices = (CheckBox)view.findViewById(R.id.cBoxEnableStatusBarNotices);
        cBoxNoticeStyleLollipop = (RadioButton)view.findViewById(R.id.rBtnNoticeLollipopStyle);
        cBoxNoticeSideIconStyle = (RadioButton)view.findViewById(R.id.rBtnNoticeSideIconStyle);
        cBoxNoticeLayoutLeft = (RadioButton)view.findViewById(R.id.rBtnNoticeLayoutLeft);
        cBoxNoticeLayoutRight = (RadioButton)view.findViewById(R.id.rBtnNoticeLayoutRight);
        cBoxNoticeLayoutBgEmpty = (RadioButton)view.findViewById(R.id.rBtnNoticeLayoutBgEmpty);
        cBoxNoticeLayoutBgBlack = (RadioButton)view.findViewById(R.id.rBtnNoticeLayoutBgBlack);
        cBoxNoticeScreenOn = (CheckBox)view.findViewById(R.id.cBoxNoticeScreenOn);
        cBoxShowSenderPicture = (CheckBox)view.findViewById(R.id.cBoxShowSenderPicture);
        cBoxShowNoticeContent = (CheckBox)view.findViewById(R.id.cBoxShowNoticeContent);
        cBoxGroupNotices = (CheckBox)view.findViewById(R.id.cBoxGroupNotices);

        lytDisableHeadsUp = (LinearLayout)view.findViewById(R.id.lytDisableHeadsUp);
        lytBottombar = (LinearLayout)view.findViewById(R.id.lytBottombar);

        //if(!C.isAndroid(Build.VERSION_CODES.M)){
            lytDisableHeadsUp.setVisibility(View.GONE);
        //}
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try{
            prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            onClickFunction();
            loadSettings();
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    @Override
    public void onDestroy(){
        if(mAppWidgetHost==null){
            mAppWidgetHost.stopListening();
        }
        super.onDestroy();
    }

    private void saveSettings(){
        editor.putBoolean("cBoxEnableStatusBarNotices",cBoxEnableStatusBarNotices.isChecked());
        editor.putBoolean("cBoxNoticeStyleLollipop",cBoxNoticeStyleLollipop.isChecked());
        editor.putBoolean("cBoxNoticeSideIconStyle",cBoxNoticeSideIconStyle.isChecked());
        editor.putBoolean("cBoxNoticeLayoutLeft",cBoxNoticeLayoutLeft.isChecked());
        editor.putBoolean("cBoxNoticeLayoutRight",cBoxNoticeLayoutRight.isChecked());
        editor.putBoolean("cBoxNoticeLayoutBgEmpty",cBoxNoticeLayoutBgEmpty.isChecked());
        editor.putBoolean("cBoxNoticeLayoutBgBlack",cBoxNoticeLayoutBgBlack.isChecked());
        editor.putBoolean("cBoxNoticeScreenOn",cBoxNoticeScreenOn.isChecked());
        editor.putBoolean("cBoxShowSenderPicture",cBoxShowSenderPicture.isChecked());
        editor.putBoolean("cBoxShowNoticeContent",cBoxShowNoticeContent.isChecked());
        editor.putBoolean("cBoxGroupNotices",cBoxGroupNotices.isChecked());
        editor.commit();
    }

    private void onClickFunction(){
        lytEnableStatusBarNotices.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxEnableStatusBarNotices.performClick();
            }
        });
        lytBlockNoticeApps.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                if(cBoxEnableStatusBarNotices.isChecked()){
                    Intent i = new Intent(context,ListAppsSelector.class);
                    i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_BLOCKED_NOTICE_APPS);
                    startActivity(i);
                }else{
                    Toast.makeText(context,getString(R.string.bottom_bar_set_notices_app_desc),Toast.LENGTH_LONG).show();
                }
            }
        });
        lytLollipopStyleWidget.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(context,ListWidgetApps.class);
                i.putExtra(C.LIST_APP_WIDGET_TYPE,C.LIST_ALL_APPS_WIDGETS);

                if(fragment!=null){
                    fragment.startActivityForResult(i,MyWidgetHelper.REQUEST_PICK_APPWIDGET_RESULT);
                }else{
                    context.startActivityForResult(i,MyWidgetHelper.REQUEST_PICK_APPWIDGET_RESULT);
                }
            }
        });
        lytLollipopStyleWidget.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                int oldWidgetId = prefs.getInt("lytLollipopStyleWidget",AppWidgetManager.INVALID_APPWIDGET_ID);
                deleteWidget(oldWidgetId);
                lytLollipopStyleWidgeSample.removeAllViews();
                return true;
            }
        });
        lytNoticeScreenOn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxNoticeScreenOn.performClick();
            }
        });
        lytShowSenderPicture.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxShowSenderPicture.performClick();
            }
        });
        lytShowNoticeContent.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxShowNoticeContent.performClick();
            }
        });
        lytGroupNotices.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxGroupNotices.performClick();
            }
        });
        lytDisableHeadsUp.setOnClickListener(new OnClickListener(){
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v){
                if(!C.isAndroid(Build.VERSION_CODES.M)){
                    Toast.makeText(context,"Android version not supported",Toast.LENGTH_LONG).show();
                    return;
                }
                try{
                    //"adb shell settings put global heads_up_notifications_enabled 0"// failed need root
                    //Settings.Global.putInt(contentResolver, "heads_up_notifications_enabled", 0)need secure settings permission


                    final String HEADS_UP = "heads_up_notifications_enabled";
                    int val = Settings.Global.getInt(context.getContentResolver(),HEADS_UP);
                    Log.e("lytDisableHeadsUp","heads_up_notifications_enabled: "+val);
                    if(val==0){//1 == Enabled, 0 == Disabled
                        runShell("settings put global heads_up_notifications_enabled 1");
                        Toast.makeText(context,"Heads Up Successfully enabled",Toast.LENGTH_LONG).show();
                    }else{
                        runShell("settings put global heads_up_notifications_enabled 0");
                        Toast.makeText(context,"Heads Up Successfully disabled",Toast.LENGTH_LONG).show();
                    }
                }catch(Exception e){
                    Toast.makeText(context,"Action failed",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
        lytBottombar.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(context,SettingsBottombar.class);
                context.startActivity(i);
            }
        });

        cBoxEnableStatusBarNotices.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEnableStatusBarNotices.isChecked()){
                    if(C.isAndroid(Build.VERSION_CODES.JELLY_BEAN_MR2)){
                        if(!C.isNotificationEnabled(context)){
                            Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            fragment.startActivityForResult(i,ENABLE_STATUSBAR_NOTICES);

                            new MyAlertDialog(context).showServiceDialog(R.string.notice_desc_dialog);
                        }
                    }else{
                        cBoxEnableStatusBarNotices.setChecked(false);
                        Toast.makeText(context,context.getString(R.string.bottom_bar_statusbar_notices_desc4),Toast.LENGTH_LONG).show();
                    }
                }else{
                    if(C.isNotificationEnabled(context)){
                        Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                        fragment.startActivityForResult(i,ENABLE_STATUSBAR_NOTICES);
                    }
                }
            }
        });
        cBoxNoticeStyleLollipop.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                lytLollipopStyleOptions.setVisibility(View.VISIBLE);
                lytNoticeSideIconStyleOptions.setVisibility(View.GONE);
                cBoxNoticeSideIconStyle.setChecked(false);
                new MyAlertDialog(context).usage("USAGE_LOLLIPOP",R.drawable.notice_lollipop,T.LOLLIPOP_STYLE,T.USAGE_LOLLIPOP,null);
            }
        });
        cBoxNoticeSideIconStyle.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                lytLollipopStyleOptions.setVisibility(View.GONE);
                lytNoticeSideIconStyleOptions.setVisibility(View.VISIBLE);
                cBoxNoticeStyleLollipop.setChecked(false);
                new MyAlertDialog(context).usage("USAGE_SIDE_ICON",R.drawable.notice_side,T.CLASSIC_SIDE_ICON,T.USAGE_SIDE_ICON,null);
            }
        });

        cBoxNoticeLayoutLeft.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxNoticeLayoutRight.setChecked(false);
            }
        });
        cBoxNoticeLayoutRight.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxNoticeLayoutLeft.setChecked(false);
            }
        });
        cBoxNoticeLayoutBgEmpty.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxNoticeLayoutBgBlack.setChecked(false);
            }
        });
        cBoxNoticeLayoutBgBlack.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxNoticeLayoutBgEmpty.setChecked(false);
            }
        });
    }
    private void runShell(String cmd) throws Exception{
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
    }
    private void loadSettings(){
        if(C.isNotificationEnabled(context)){
            cBoxEnableStatusBarNotices.setChecked(true);
            lytEnableStatusBarNotices.setBackgroundColor(0);
        }else{
            cBoxEnableStatusBarNotices.setChecked(false);
            lytEnableStatusBarNotices.setBackgroundColor(Color.parseColor("#66ff0000"));
        }
        cBoxNoticeStyleLollipop.setChecked(prefs.getBoolean("cBoxNoticeStyleLollipop",true));
        cBoxNoticeSideIconStyle.setChecked(prefs.getBoolean("cBoxNoticeSideIconStyle",false));
        cBoxNoticeLayoutLeft.setChecked(prefs.getBoolean("cBoxNoticeLayoutLeft",false));
        cBoxNoticeLayoutRight.setChecked(prefs.getBoolean("cBoxNoticeLayoutRight",true));
        cBoxNoticeLayoutBgEmpty.setChecked(prefs.getBoolean("cBoxNoticeLayoutBgEmpty",true));
        cBoxNoticeLayoutBgBlack.setChecked(prefs.getBoolean("cBoxNoticeLayoutBgBlack",false));
        cBoxNoticeScreenOn.setChecked(prefs.getBoolean("cBoxNoticeScreenOn",false));
        cBoxShowSenderPicture.setChecked(prefs.getBoolean("cBoxShowSenderPicture",true));
        cBoxShowNoticeContent.setChecked(prefs.getBoolean("cBoxShowNoticeContent",true));
        cBoxGroupNotices.setChecked(prefs.getBoolean("cBoxGroupNotices",true));

        if(cBoxNoticeStyleLollipop.isChecked()){
            lytLollipopStyleOptions.setVisibility(View.VISIBLE);
            lytNoticeSideIconStyleOptions.setVisibility(View.GONE);

            final int intAppWidgetId = prefs.getInt("lytLollipopStyleWidget",AppWidgetManager.INVALID_APPWIDGET_ID);
            final AppWidgetProviderInfo appWidgetInfo = getAppWidgetInfo(intAppWidgetId);
            if(appWidgetInfo!=null){
                addWidgetView(intAppWidgetId,appWidgetInfo);
            }
        }else{
            lytLollipopStyleOptions.setVisibility(View.GONE);
            lytNoticeSideIconStyleOptions.setVisibility(View.VISIBLE);
        }
    }

    private void deleteWidget(int widgetId){
        if(widgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
            mAppWidgetHost.deleteAppWidgetId(widgetId);

            editor.putInt("lytLollipopStyleWidget",AppWidgetManager.INVALID_APPWIDGET_ID);
            editor.commit();
        }
    }

    private AppWidgetProviderInfo getAppWidgetInfo(int id){
        return mAppWidgetManager.getAppWidgetInfo(id);
    }

    private void addWidgetView(final int intAppWidgetId,final AppWidgetProviderInfo appWidgetInfo){
        int spanX = C.getMaxWidgetSpanX(context);
        int spanY = C.getDefaultWidgetSpanY(context,appWidgetInfo.minHeight);
        if(spanY>2){
            spanY = 2;
        }
        final int w = C.getSpanToPxX(context,spanX);
        final int h = C.getDefaultSpanToPxY(context,spanY);

        final LockerAppWidgetHostView hostView = (LockerAppWidgetHostView)mAppWidgetHost.setupWidget(context,intAppWidgetId,appWidgetInfo,new WidgetOnTouchListener(){
            @Override
            public boolean onWidgetTouchEvent(View v,MotionEvent m,CheckLongPressHelper widgetLongPressHelper,String pkgName){
                return false;
            }
        });
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(w,h);
        hostView.setLayoutParams(pr);
        lytLollipopStyleWidgeSample.addView(hostView);
        updateWidget(hostView,spanX,spanY);
    }

    private void updateWidget(LockerAppWidgetHostView hostView,int spanX,int spanY){
        try{
            int wDp = C.pxToDp(context,C.getSpanToPxX(context,spanX))-20;
            int hDp = C.pxToDp(context,C.getSpanToPxY(context,spanY))-20;
            Bundle b = new Bundle();
            b.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,wDp);
            b.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH,wDp);
            b.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT,hDp);
            b.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,hDp);
            hostView.updateAppWidgetOptions(b);
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    //widget setup
    private void requestBindWidget(Intent data){
        try{
            if(data!=null){
                final Bundle extras = data.getExtras();
                final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                if(appWidgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
                    final String strGetWidgetPkgName = data.getExtras().getString("strGetWidgetPkgName","");
                    final String strGetWidgetClassName = data.getExtras().getString("strGetWidgetClassName","");
                    final List<AppWidgetProviderInfo> listwidget = mAppWidgetManager.getInstalledProviders();
                    final int intWidgetIndex = getBindWidgetIndex(strGetWidgetPkgName,strGetWidgetClassName,listwidget);
                    if(intWidgetIndex!=-1){
                        final AppWidgetProviderInfo appWidgetInfo = listwidget.get(intWidgetIndex);
                        boolean success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,appWidgetInfo.provider);
                        if(success){
                            configureWidget(data);
                        }else{
                            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,appWidgetInfo.provider);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS,data.getExtras());
                            if(fragment!=null){
                                fragment.startActivityForResult(intent,MyWidgetHelper.REQUEST_BIND_APPWIDGET_RESULT);
                            }else{
                                context.startActivityForResult(intent,MyWidgetHelper.REQUEST_BIND_APPWIDGET_RESULT);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    private int getBindWidgetIndex(String strGetWidgetPkgName,String strGetWidgetClassName,
            List<AppWidgetProviderInfo> listwidget){
        try{
            if(strGetWidgetPkgName!=null && strGetWidgetClassName!=null && listwidget!=null){
                for(int i = 0; i<listwidget.size(); i++){
                    if(strGetWidgetPkgName.equals(listwidget.get(i).provider.getPackageName()) && strGetWidgetClassName.equals(listwidget.get(i).provider.getClassName())){
                        return i;
                    }
                }
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
        return -1;
    }

    private void configureWidget(Intent data){
        final Bundle extras = data.getExtras();
        final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
        final AppWidgetProviderInfo appWidgetInfo = getAppWidgetInfo(appWidgetId);
        if(appWidgetInfo.configure!=null){
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
            if(fragment!=null){
                fragment.startActivityForResult(intent,MyWidgetHelper.REQUEST_CREATE_APPWIDGET_RESULT);
            }else{
                context.startActivityForResult(intent,MyWidgetHelper.REQUEST_CREATE_APPWIDGET_RESULT);
            }
        }else{
            saveAppsWidget(data);
        }
    }

    private void saveAppsWidget(Intent data){
        try{
            if(data!=null){
                final Bundle extras = data.getExtras();
                final int intAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                final AppWidgetProviderInfo appWidgetInfo = getAppWidgetInfo(intAppWidgetId);
                if(appWidgetInfo==null){
                    return;
                }

                //delete old widget if successfully added new
                int oldWidgetId = prefs.getInt("lytLollipopStyleWidget",AppWidgetManager.INVALID_APPWIDGET_ID);
                deleteWidget(oldWidgetId);

                editor.putInt("lytLollipopStyleWidget",intAppWidgetId);
                editor.commit();

                lytLollipopStyleWidgeSample.removeAllViews();
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run(){
                        addWidgetView(intAppWidgetId,appWidgetInfo);
                    }
                },500);
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }
}