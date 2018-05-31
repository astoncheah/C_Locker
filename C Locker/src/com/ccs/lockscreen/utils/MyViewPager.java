package com.ccs.lockscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager{

    private boolean enabled;

    public MyViewPager(Context context){
        super(context);
        this.enabled = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        try{
            if(this.enabled){
                return super.onInterceptTouchEvent(event);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(this.enabled){
            return super.onTouchEvent(event);
        }

        return false;
    }

    public void setScrollingEnabled(boolean enabled){
        this.enabled = enabled;
    }
}