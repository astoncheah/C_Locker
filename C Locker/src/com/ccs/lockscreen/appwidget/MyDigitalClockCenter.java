package com.ccs.lockscreen.appwidget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyDigitalClockCenter extends LinearLayout{
    private SharedPreferences prefs;
    private Context context;
    private Handler handler = new Handler();
    private Typeface mTypeface;
    private LinearLayout main;
    private LinearLayout lytAlarm;
    private ImageView imgDigitalClockCenter;
    private TextView txtDigitalClockCenter, txtAm, txtDate, txtAlarm;
    private boolean cBoxShowAlarm, cBoxShowAM, is1stRun;
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

    public MyDigitalClockCenter(final Context context){
        super(context);
        this.context = context;
        is1stRun = true;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.locker_clock_digital_center,this,true);

        main = (LinearLayout)v.findViewById(R.id.lytDigitalClockCenter);
        lytAlarm = (LinearLayout)v.findViewById(R.id.lytDigitalClockCenterAlarm);
        imgDigitalClockCenter = (ImageView)v.findViewById(R.id.imgDigitalClockCenter);
        txtDigitalClockCenter = (TextView)v.findViewById(R.id.txtDigitalClockCenter);
        txtAm = (TextView)v.findViewById(R.id.txtDigitalClockCenterAmPm);
        txtDate = (TextView)v.findViewById(R.id.txtDigitalClockCenterDate);
        txtAlarm = (TextView)v.findViewById(R.id.txtDigitalClockCenterAlarm);

        txtDigitalClockCenter.setVisibility(View.GONE);//never use

        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        loadReceiver(true);
        loadSettings();
        loadLayoutClock();
        loadTextSettings();
        setTime();
    }

    @Override
    protected void onDetachedFromWindow(){
        close();
        super.onDetachedFromWindow();
    }

    private void close(){
        loadReceiver(false);
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
        intTextColor = Color.parseColor("#"+prefs.getString("strColorLockerMainText","ffffff"));
        strTextStyle = prefs.getString("strTextStyleLocker","txtStyle03.ttf");
        strTimeFormat = prefs.getString("strTimeFormat","HH:mm");
    }

    private void loadLayoutClock(){
        if(!cBoxShowAlarm){
            lytAlarm.setVisibility(View.GONE);
            setParam(-1,C.dpToPx(context,80));
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
        if(strTextStyle.equals("txtStyle03.ttf") ||
                strTextStyle.equals("txtStyle25.ttf") ||
                strTextStyle.equals("txtStyle26.ttf")){
            intTextStyle = Typeface.BOLD;
        }else{
            intTextStyle = Typeface.NORMAL;
        }
        loadFontStyle();
        loadFontColor();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void setTime(){
        final DateFormat tmf = new SimpleDateFormat(strTimeFormat,Locale.getDefault());
        final DateFormat dtfCenter = DateFormat.getDateInstance(DateFormat.FULL,Locale.getDefault());

        String currentTime = tmf.format(new Date());
        String currentDate = dtfCenter.format(new Date());
        setTimeTransition(imgDigitalClockCenter,currentTime);
        txtDate.setText(currentDate);

        if(cBoxShowAlarm){
            String nextAlarm = null;
            if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                try{
                    final DateFormat df = new SimpleDateFormat("EEE",Locale.getDefault());
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
            if(nextAlarm!=null && !nextAlarm.equals("")){
                txtAlarm.setText(nextAlarm);
                setParam(-1,C.dpToPx(context,95));
            }else{
                txtAlarm.setText(" ");
                lytAlarm.setVisibility(View.INVISIBLE);
                setParam(-1,C.dpToPx(context,80));
            }
        }
        if(cBoxShowAM){
            setAM();
        }
    }

    public final void setParam(int w,int h){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(w,h);
        main.setLayoutParams(pr);
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

    private void setTimeTransition(final ImageView v,final String currentTime){
        if(is1stRun){
            is1stRun = false;
            v.setImageBitmap(getFontType(currentTime,mTypeface,intTextColor,200));
        }else{
            int no = 0;
            ObjectAnimator outX = ObjectAnimator.ofFloat(v,"y",no,200).setDuration(300);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v,"alpha",0).setDuration(0);
            ObjectAnimator inX = ObjectAnimator.ofFloat(v,"y",no).setDuration(0);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v,"alpha",0,1).setDuration(500);
            AnimatorSet animSet = new AnimatorSet();
            animSet.playSequentially(outX,fadeOut,inX,fadeIn);
            animSet.start();

            handler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    v.setImageBitmap(getFontType(currentTime,mTypeface,intTextColor,200));
                }
            },320);
        }
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

    private Bitmap getFontType(String strText,final Typeface textType,final int textColor,
            final int textSize){
        if(strText==null || strText.equals("")){
            strText = "..";
        }
        Paint paint = new Paint();
        Rect rect = new Rect();

        paint.setTypeface(textType);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        paint.setTextAlign(Align.CENTER);
        paint.getTextBounds(strText,0,strText.length(),rect); // Measure the text

        Float bitmapWidth = rect.width()*1.15f;
        Float bitmapHeight = rect.height()*1.15f;
        Bitmap finalBitmap = Bitmap.createBitmap(bitmapWidth.intValue(),bitmapHeight.intValue(),Bitmap.Config.ARGB_8888);
        Canvas myCanvas = new Canvas(finalBitmap);

        myCanvas.drawText(strText,bitmapWidth/2,rect.height(),paint);

        return finalBitmap;
    }
}