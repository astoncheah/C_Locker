package com.ccs.lockscreen.appwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

public class MyAnalogClock extends View{
    private Calendar mCalendar;
    private Drawable mDial, mHourArrow, mMinuteArrow, mSecondArrow;
    private int clockLayoutWidth, clockLayoutHeight, mDialWidth, mDialHeight;
    private boolean mAttached, mChanged, cBoxEnableUpdateInSecond;
    private Handler mHandler = new Handler();
    private float mHour, mMinutes, mSeconds;
    private Context context;

    //Dial Measure= (300x300,(big x 4)12x30,(small x 8)4x25)
    //Hour Measure= (300x300,10x120,0x15)
    //Minute Measure= (300x300,8x155,0x15)
    private Runnable updateTime = new Runnable(){
        @Override
        public void run(){
            onTimeChanged();
            invalidate();
            startTimeUpdate();
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIME_TICK)){
                onTimeChanged();
                invalidate();
            }else if(action.equals(Intent.ACTION_TIMEZONE_CHANGED)){
                TimeZone timeZone = TimeZone.getTimeZone("UTC");
                mCalendar = Calendar.getInstance(timeZone);
                onTimeChanged();
                invalidate();
            }
        }
    };

    public MyAnalogClock(Context context){
        this(context,null);
    }

    public MyAnalogClock(Context context,AttributeSet attrs){
        this(context,attrs,0);
    }

    public MyAnalogClock(Context context,AttributeSet attrs,int defStyle){
        super(context,attrs,defStyle);
        this.context = context;
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        cBoxEnableUpdateInSecond = prefs.getBoolean("cBoxEnableUpdateInSecond",false);
        getClockColor(context.getResources());

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();
        //Log.e("MyAnalogClock", "mDialWidth/mDialHeight: "+mDialWidth+"/"+mDialHeight);
    }

    @SuppressWarnings("deprecation")
    private void getClockColor(Resources res){
        try{
            Bitmap bm = null;
            File file = new File(C.EXT_STORAGE_DIR,C.CLOCK_DIAL);
            ;
            if(file.getAbsoluteFile().exists()){
                bm = BitmapFactory.decodeFile(file.getPath());
                mDial = new BitmapDrawable(res,bm);
            }else{
                mDial = res.getDrawable(R.drawable.a_analog_clock_dial_backup);
            }

            file = new File(C.EXT_STORAGE_DIR,C.CLOCK_HOUR);
            if(file.getAbsoluteFile().exists()){
                bm = BitmapFactory.decodeFile(file.getPath());
                mHourArrow = new BitmapDrawable(res,bm);
            }else{
                mHourArrow = res.getDrawable(R.drawable.a_analog_clock_hour_wht);
            }


            file = new File(C.EXT_STORAGE_DIR,C.CLOCK_MIN);
            if(file.getAbsoluteFile().exists()){
                bm = BitmapFactory.decodeFile(file.getPath());
                mMinuteArrow = new BitmapDrawable(res,bm);
            }else{
                mMinuteArrow = res.getDrawable(R.drawable.a_analog_clock_minute_wht);
            }
            if(cBoxEnableUpdateInSecond){
                bm = ((BitmapDrawable)getResources().getDrawable(R.drawable.a_analog_clock_second_small)).getBitmap();
                if(mDial.getIntrinsicWidth()<bm.getWidth()){
                    final Bitmap bitmapResized = Bitmap.createScaledBitmap(bm,mDial.getIntrinsicWidth(),mDial.getIntrinsicHeight(),false);
                    mSecondArrow = new BitmapDrawable(context.getResources(),bitmapResized);
                }else{
                    mSecondArrow = new BitmapDrawable(context.getResources(),bm);
                }
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView,int visibility){
        super.onVisibilityChanged(changedView,visibility);
        if(visibility==View.INVISIBLE){
            //Log.e(ServiceMain.LOG, "Locker>onVisibilityChanged>INVISIBLE");
        }else if(visibility==View.GONE){
            //Log.e(ServiceMain.LOG, "Locker>onVisibilityChanged>GONE");
        }else if(visibility==View.VISIBLE){
            //Log.e(ServiceMain.LOG, "Locker>onVisibilityChanged>VISIBLE");
        }
    }

    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);
        /*
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(h,-1);//-1 = LayoutParams.MATCH_PARENT
        pr.setMargins(C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5));
        if(!isLeft){
            pr.addRule(RelativeLayout.BELOW,R.id.lytAnalogClockCenterAlarm);
        	pr.addRule(RelativeLayout.CENTER_HORIZONTAL);
        }
        setLayoutParams(pr);*/
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        boolean changed = mChanged;
        if(changed){
            //Log.e("MyAnalogClock","onDraw: "+mChanged);
            mChanged = false;
        }

        int availableWidth = clockLayoutWidth; //clock width = 150dp
        int availableHeight = clockLayoutHeight; //clock height = 150dp

        int x = availableWidth/2;
        int y = availableHeight/2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth(); //drawable width
        int h = dial.getIntrinsicHeight(); //drawable height

        //boolean scaled = false;

        //if(availableWidth < w || availableHeight < h){
        //scaled = true;
        float scale = Math.min((float)availableWidth/(float)w,(float)availableHeight/(float)h);
        //Log.e("MyAnalogClock", "onDraw>scale: "+scale);
        canvas.save();
        canvas.scale(scale,scale,x,y);
        //}

        if(changed){
            dial.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));
        }
        dial.draw(canvas);
        //hours
        canvas.save();
        canvas.rotate(mHour/12.0f*360.0f,x,y);
        final Drawable hourHand = mHourArrow;
        if(changed){
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));
        }
        hourHand.draw(canvas);
        canvas.restore();
        //minutes
        canvas.save();
        canvas.rotate(mMinutes/60.0f*360.0f,x,y);
        final Drawable minuteHand = mMinuteArrow;
        if(changed){
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));
        }
        minuteHand.draw(canvas);
        canvas.restore();
        //seconds
        if(cBoxEnableUpdateInSecond){
            canvas.save();
            canvas.rotate(mSeconds/60.0f*360.0f,x,y);
            final Drawable secondHand = mSecondArrow;
            if(changed){
                w = secondHand.getIntrinsicWidth();
                h = secondHand.getIntrinsicHeight();
                secondHand.setBounds(x-(w/2),y-(h/2),x+(w/2),y+(h/2));
            }
            secondHand.draw(canvas);
            canvas.restore();
        }

        //if(scaled){
        //canvas.restore();
        //}
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(!mAttached){
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            if(cBoxEnableUpdateInSecond){
                //nothing
            }else{
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            }
            getContext().registerReceiver(mIntentReceiver,filter,null,mHandler);
        }
        onTimeChanged();
    }

    @Override
    protected void onDetachedFromWindow(){
        try{
            if(mAttached){
                stopTimeUpdate();
                getContext().unregisterReceiver(mIntentReceiver);
                mAttached = false;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        clockLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        clockLayoutHeight = MeasureSpec.getSize(heightMeasureSpec);
        //Log.e("MyAnalogClock", "onMeasure>widthMode/widthSize: "+widthMode+"/"+clockLayoutWidth);
        //Log.e("MyAnalogClock", "onMeasure>heightMode/heightSize: "+heightMode+"/"+clockLayoutHeight);
        float hScale = 1.0f;
        float vScale = 1.0f;

        if(widthMode!=MeasureSpec.UNSPECIFIED && clockLayoutWidth<mDialWidth){
            hScale = (float)clockLayoutWidth/(float)mDialWidth;
        }

        if(heightMode!=MeasureSpec.UNSPECIFIED && clockLayoutHeight<mDialHeight){
            vScale = (float)clockLayoutHeight/(float)mDialHeight;
        }

        float scale = Math.min(hScale,vScale);
        //Log.e("MyAnalogClock", "onMeasure>scale: "+scale);
        setMeasuredDimension(resolveSize((int)(mDialWidth*scale),widthMeasureSpec),resolveSize((int)(mDialHeight*scale),heightMeasureSpec));
    }

    public final void stopTimeUpdate(){
        mHandler.removeCallbacks(updateTime);
        //Log.e("","stopTimeUpdateAlarm");
    }

    private void onTimeChanged(){
        mCalendar = Calendar.getInstance();

        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);

        mSeconds = second;
        mMinutes = minute+second/60.0f;
        mHour = hour+mMinutes/60.0f;
        mChanged = true;
    }

    public final void startTimeUpdate(){
        mHandler.postDelayed(updateTime,1000);
        //Log.e("","startTimeUpdateAlarm");
    }
}
