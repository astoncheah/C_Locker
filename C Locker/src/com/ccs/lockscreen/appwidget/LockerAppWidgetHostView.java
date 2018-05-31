package com.ccs.lockscreen.appwidget;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;

public class LockerAppWidgetHostView extends AppWidgetHostView{
    private CheckLongPressHelper mLongPressHelper;
    private WidgetOnTouchListener callBack = null;
    private String pkgName;
    private boolean isWidgetError = false;
    private boolean isOnWidgetResize = false;

    public LockerAppWidgetHostView(Context context,String pkgName,WidgetOnTouchListener callBack){
        super(context);
        this.pkgName = pkgName;
        this.callBack = callBack;
        mLongPressHelper = new CheckLongPressHelper(this);
    }

    @Override
    protected View getErrorView(){
        return null;
    }

    @Override
    public void cancelLongPress(){
        super.cancelLongPress();
        if(mLongPressHelper!=null){
            mLongPressHelper.cancelLongPress();
        }
    }

    @Override
    public int getDescendantFocusability(){
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean onInterceptTouchEvent(MotionEvent m){
        // Consume any touch events for ourselves after longpress is triggered
        if(mLongPressHelper!=null){
            if(mLongPressHelper.hasPerformedLongPress()){
                mLongPressHelper.cancelLongPress();
                return true;
            }
        }
        if(isOnWidgetResize){
            return true;
        }
        if(callBack!=null){
            ViewParent v = this.getParent();
            if(v instanceof LockerWidgetLayout){
                return callBack.onWidgetTouchEvent((LockerWidgetLayout)this.getParent(),m,mLongPressHelper,pkgName);
            }
            return callBack.onWidgetTouchEvent(null,m,mLongPressHelper,pkgName);
        }
        return true;
    }

    public void setWidgetOnTouchListener(WidgetOnTouchListener callBack){
        this.callBack = callBack;
    }

    public boolean isWidgetLoadingError(){
        return isWidgetError;
    }

    public String getPkgName(){
        return pkgName;
    }

    public boolean isOnWidgetResize(){
        return isOnWidgetResize;
    }

    public void setOnWidgetResize(boolean isOnWidgetResize){
        this.isOnWidgetResize = isOnWidgetResize;
    }
}