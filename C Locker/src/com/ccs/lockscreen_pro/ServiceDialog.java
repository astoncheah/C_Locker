package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.ccs.lockscreen.myclocker.C;

public class ServiceDialog extends Service{
    private TextView txt;

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null && intent.hasExtra("desc")){
            String desc = intent.getStringExtra("desc");
            final WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);

            txt = new TextView(this);
            txt.setBackgroundColor(Color.parseColor("#aa000000"));
            txt.setGravity(Gravity.CENTER);
            txt.setText(desc);
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
            txt.setOnTouchListener(new OnTouchListener(){
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v,MotionEvent m){
                    Log.e("ServiceDialog",">onTouch: ");
                    windowManager.removeView(txt);
                    ServiceDialog.this.stopSelf();
                    return true;
                }
            });

            int what = WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |//on top of status bar
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;//nothing, just easy to test

            WindowManager.LayoutParams windowPr = new WindowManager.LayoutParams();
            windowPr.format = PixelFormat.TRANSLUCENT;
            if(C.isAndroid(Build.VERSION_CODES.O)){
                windowPr.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
                //windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            }else{
                windowPr.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;// overlay status bar
            }
            windowPr.width = WindowManager.LayoutParams.MATCH_PARENT;
            ;
            windowPr.height = WindowManager.LayoutParams.MATCH_PARENT;
            ;
            windowPr.flags = what;
            windowPr.gravity = Gravity.TOP;

            windowManager.addView(txt,windowPr);
        }else{
            ServiceDialog.this.stopSelf();
        }
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
}
