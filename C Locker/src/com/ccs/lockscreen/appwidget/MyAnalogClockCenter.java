package com.ccs.lockscreen.appwidget;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyAnalogClockCenter extends LinearLayout{
    private SharedPreferences prefs;
    private Context context;
    private Typeface mTypeface;
    private RelativeLayout main;
    private MyAnalogClock anlgClock;
    private LinearLayout lytAlarm;
    private TextView txtAm, txtDate, txtAlarm;
    private boolean cBoxShowAlarm, cBoxShowAM, cBoxEnableUpdateInSecond;
    private int intTextColor, intTextStyle;
    private String strTextStyle, strTimeFormat;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,Intent intent){
            String action = intent.getAction();
            try{
                if(action.equals(Intent.ACTION_TIME_CHANGED) ||
                        action.equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                        action.equals(Intent.ACTION_TIME_TICK)){
                    setTime();
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }
    };

    public MyAnalogClockCenter(Context context){
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.locker_clock_analog_center,this,true);

        main = (RelativeLayout)v.findViewById(R.id.lytAnalogClockCenter);
        anlgClock = (MyAnalogClock)v.findViewById(R.id.analogClockCenter);
        txtAm = (TextView)v.findViewById(R.id.txtAnalogClockCenterAmPm);
        lytAlarm = (LinearLayout)v.findViewById(R.id.lytAnalogClockCenterAlarm);
        txtDate = (TextView)v.findViewById(R.id.txtAnalogClockCenterDay);
        txtAlarm = (TextView)v.findViewById(R.id.txtAnalogClockCenterAlarm);

        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        loadReceiver(true);
        loadSettings();
        loadLayoutClock();
        loadTextSettings();
        loadAlarm(true);
        setTime();
    }

    @Override
    protected void onDetachedFromWindow(){
        close();
        super.onDetachedFromWindow();
    }

    private void close(){
        loadReceiver(false);
        loadAlarm(false);
        anlgClock.stopTimeUpdate();//2 times for safe
    }

    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_TIME_TICK);
                intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
                context.registerReceiver(mReceiver,intentFilter);
            }else{
                context.unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    private void loadSettings(){
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        cBoxShowAlarm = prefs.getBoolean("cBoxShowAlarm",true);
        cBoxShowAM = prefs.getBoolean("cBoxShowAM",false);
        cBoxEnableUpdateInSecond = prefs.getBoolean("cBoxEnableUpdateInSecond",false);
        intTextColor = Color.parseColor("#"+prefs.getString(P.STR_COLOR_ANALOG_CLOCK,"ffffff"));
        strTextStyle = prefs.getString("strTextStyleLocker","txtStyle03.ttf");
        strTimeFormat = prefs.getString("strTimeFormat","HH:mm");
    }

    private void loadLayoutClock(){
        if(!cBoxShowAlarm){
            lytAlarm.setVisibility(View.GONE);
        }
        if(cBoxShowAM){
            txtAm.setVisibility(View.VISIBLE);
        }else{
            txtAm.setVisibility(View.GONE);
        }
    }

    private void loadTextSettings(){
        try{
            if(strTextStyle.equals("Default")){
                mTypeface = Typeface.DEFAULT;
            }else{
                mTypeface = Typeface.createFromAsset(context.getAssets(),strTextStyle);
            }
        }catch(Exception e){
            mTypeface = Typeface.DEFAULT;
        }
        if(strTextStyle.equals("txtStyle03.ttf")){
            intTextStyle = Typeface.BOLD;
        }else{
            intTextStyle = Typeface.NORMAL;
        }
        loadFontStyle();
        loadFontColor();
    }

    private void loadAlarm(final boolean load){
        if(cBoxEnableUpdateInSecond){
            if(load){
                anlgClock.startTimeUpdate();
            }else{
                anlgClock.stopTimeUpdate();
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setTime(){
        final DateFormat dtfCenter = DateFormat.getDateInstance(DateFormat.FULL,Locale.getDefault());

        String currentDateCenter = dtfCenter.format(new Date());
        String nextAlarm = null;
        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
            try{
                final DateFormat df = new SimpleDateFormat("EEE",Locale.getDefault());
                final DateFormat tmf = new SimpleDateFormat(strTimeFormat,Locale.getDefault());
                final AlarmManager aMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                final AlarmManager.AlarmClockInfo info = aMgr.getNextAlarmClock();
                if(info!=null){
                    final long time = info.getTriggerTime();
                    nextAlarm = df.format(new Date(time))+" "+tmf.format(new Date(time));
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }else{
            nextAlarm = Settings.System.getString(context.getContentResolver(),Settings.System.NEXT_ALARM_FORMATTED);
        }
        txtDate.setText(currentDateCenter);
        if(nextAlarm!=null && !nextAlarm.equals("")){
            txtAlarm.setText(nextAlarm);
        }else{
            txtAlarm.setText(" ");
            lytAlarm.setVisibility(View.INVISIBLE);
        }
        if(cBoxShowAM){
            setAM();
        }
    }

    private void loadFontStyle(){
        txtAm.setTypeface(mTypeface,intTextStyle);
        txtDate.setTypeface(mTypeface,intTextStyle);
        txtAlarm.setTypeface(mTypeface,intTextStyle);
    }

    private void loadFontColor(){
        txtAm.setTextColor(intTextColor);
        txtDate.setTextColor(intTextColor);
        txtAlarm.setTextColor(intTextColor);
    }

    private void setAM(){
        final Calendar rightNow = Calendar.getInstance();
        int ampm = rightNow.get(Calendar.AM_PM);
        switch(ampm){
            case 0:
                txtAm.setText("AM");
                break;
            default:
                txtAm.setText("PM");
                break;
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView,int visibility){
        super.onVisibilityChanged(changedView,visibility);
    }

    /*
    private MyAnalogClock addClock(){
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(C.dpToPx(context,160),-1);//-1 = LayoutParams.MATCH_PARENT
        pr.setMargins(C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5));
        pr.addRule(RelativeLayout.BELOW,lytAlarm.getId());
        pr.addRule(RelativeLayout.CENTER_HORIZONTAL);
        anlgClock = new MyAnalogClock(context);
        anlgClock.setLayoutParams(pr);
        anlgClock.setId(-10);
        return anlgClock;
    }
    private TextView addClockAmPm(){
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(-2,-1);//-1 = LayoutParams.MATCH_PARENT
        pr.addRule(RelativeLayout.ALIGN_TOP,anlgClock.getId());
        pr.addRule(RelativeLayout.RIGHT_OF,anlgClock.getId());
        txtAm = new TextView(context);
        txtAm.setLayoutParams(pr);
        txtAm.setGravity(Gravity.CENTER);
        txtAm.setTextSize(15);//12sp
        return txtAm;
    }*/
    public final void setParam(int w,int h){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(w,h);
        main.setLayoutParams(pr);
    }
}
