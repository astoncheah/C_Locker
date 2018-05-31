package com.ccs.lockscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.ccs.lockscreen.myclocker.C.CallBack;

public class MyAlertWindow extends RelativeLayout{
    private CallBack cb;

    public MyAlertWindow(Context context){
        super(context);
    }

    public void setCallBack(CallBack cb){
        this.cb = cb;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent m){
        if(cb!=null){
            return cb.callTouchEvent(m);
        }
        return super.onTouchEvent(m);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        //Log.e("MyLockerWindow>dispatchKeyEvent","keyCode: "+event.getKeyCode());
        if(cb!=null){
            return cb.callKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
    }
}