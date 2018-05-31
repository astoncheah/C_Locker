package com.ccs.lockscreen.appwidget;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;

public class LockerWidgetLayout<L> extends RelativeLayout{
    private Context context;
    private L vL;
    private OnResizeCallBack callback;
    private boolean isOnWidgetResize;
    private int widgetId, mIndex, mSpanX, mSpanY;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals(context.getPackageName()+C.REMOVE_RESIZE_VIEWS)){
                removeResizeImage(false);
            }else if(action.equals(context.getPackageName()+C.REMOVE_RESIZE_VIEWS_ALL)){
                removeResizeImage(true);
            }
        }
    };

    // errors layout
    @SuppressWarnings("unchecked")
    public LockerWidgetLayout(Context context,String pkgName,String className){
        super(context);
        this.context = context;

        vL = (L)errorLayoutMain(pkgName,className);
        addView(((ErrorLayout)vL),new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    }

    private ErrorLayout errorLayoutMain(String pkgName,String className){
        final int padding = C.dpToPx(context,12);
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        final ErrorLayout lyt = new ErrorLayout(context);
        lyt.setLayoutParams(pr);
        lyt.setPadding(padding,padding,padding,padding);
        lyt.addView(errorLayout(pkgName));
        lyt.setContentDescription(pkgName+";"+className);
        return lyt;
    }

    private LinearLayout errorLayout(String pkgName){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        final LinearLayout lyt = new LinearLayout(context);
        final ImageView img = imgView(pkgName);

        lyt.setLayoutParams(pr);
        lyt.setOrientation(LinearLayout.VERTICAL);
        lyt.setGravity(Gravity.CENTER);
        lyt.setBackgroundColor(Color.BLACK);
        if(img!=null){
            lyt.addView(imgView(pkgName));
            lyt.addView(txtView(context.getString(R.string.settings_widget_error_text1)+"\n"+
                    context.getString(R.string.settings_widget_error_text2)));
        }else{
            lyt.addView(txtView(context.getString(R.string.settings_widget_error_text1)+"\n"+
                    context.getString(R.string.settings_widget_error_text2)));
        }
        return lyt;
    }

    private ImageView imgView(String pkgName){
        final int size = C.dpToPx(context,30);
        final int padding = C.dpToPx(context,4);
        final Drawable img = getWidgetIcon(pkgName);
        if(img!=null){
            final ImageView imgView = new ImageView(context);
            imgView.setLayoutParams(new LinearLayout.LayoutParams(size,size));
            imgView.setBackground(getWidgetIcon(pkgName));
            imgView.setPadding(padding,padding,padding,padding);
            return imgView;
        }
        return null;
    }

    private TextView txtView(String msg){
        final TextView txtView = new TextView(context);
        txtView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        txtView.setText(msg);
        txtView.setGravity(Gravity.CENTER);
        return txtView;
    }

    private Drawable getWidgetIcon(final String pkgName){
        try{
            final PackageManager pm = context.getPackageManager();
            final ApplicationInfo info = pm.getApplicationInfo(pkgName,0);
            return info.loadIcon(pm);
        }catch(NameNotFoundException e){
            //saveErrorLog(null,e);
        }catch(Exception e){
            //saveErrorLog(null,e);
        }
        return null;
    }

    // custom widgets
    @SuppressWarnings("unchecked")
    public LockerWidgetLayout(Context context,LockerAppWidgetHostView hostView){
        super(context);
        this.context = context;

        vL = (L)hostView;
        addView(((LockerAppWidgetHostView)vL),new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    }

    // locker widgets
    @SuppressWarnings("unchecked")
    public LockerWidgetLayout(Context context,int widgetId,final WidgetOnTouchListener callback){
        super(context);
        this.context = context;
        this.widgetId = widgetId;

        switch(widgetId){
            case C.CLOCK_ANALOG_CENTER:
                vL = (L)new MyAnalogClockCenter(context);
                break;
            case C.CLOCK_DIGITAL_CENTER:
                vL = (L)new MyDigitalClockCenter(context);
                break;
            case C.CLOCK_EVENT_LIST:
                vL = (L)new MyCalendarEventWidget(context,callback);
                break;
            case C.CLOCK_RSS:
                vL = (L)new MyRssWidget(context,callback);
                break;
            case C.CLOCK_OWNER_MSG:
                vL = (L)new MyPersonalMsg(context,false);
                break;
        }
        addView((LinearLayout)vL,new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    }

    public LockerAppWidgetHostView getHostView(){
        return (LockerAppWidgetHostView)vL;
    }

    public void updateAppWidgetOptions(Bundle b){
        ((LockerAppWidgetHostView)vL).updateAppWidgetOptions(b);
    }

    public boolean isWidgetLoadingError(){
        return ((LockerAppWidgetHostView)vL).isWidgetLoadingError();
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(context.getPackageName()+C.REMOVE_RESIZE_VIEWS);
        filter.addAction(context.getPackageName()+C.REMOVE_RESIZE_VIEWS_ALL);
        getContext().registerReceiver(mReceiver,filter);
    }    @Override
    public int getId(){
        return widgetId;
    }

    @Override
    protected void onDetachedFromWindow(){
        try{
            getContext().unregisterReceiver(mReceiver);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDetachedFromWindow();
    }    @Override
    public void setId(int widgetId){
        this.widgetId = widgetId;
    }

    public int getIndex(){
        return mIndex;
    }

    public void setIndex(int index){
        mIndex = index;
    }

    public int getSpanX(){
        return mSpanX;
    }

    public void setSpanX(int spanX){
        mSpanX = spanX;
    }

    public int getSpanY(){
        return mSpanY;
    }

    public void setSpanY(int spanY){
        mSpanY = spanY;
    }

    public void setParam(int w,int h){
        switch(widgetId){
            case C.CLOCK_ANALOG_CENTER:
                ((MyAnalogClockCenter)vL).setParam(w,h);
                break;
        }
    }

    public L getView(){
        return vL;
    }

    public boolean isOnWidgetResize(){
        return isOnWidgetResize;
    }

    public void setOnWidgetResize(boolean isOnWidgetResize){
        this.isOnWidgetResize = isOnWidgetResize;
        View v = getChildAt(0);
        if(v instanceof LockerAppWidgetHostView){
            ((LockerAppWidgetHostView)v).setOnWidgetResize(isOnWidgetResize);
        }
    }

    public void addResizeImage(OnResizeCallBack callback){
        this.callback = callback;
        setOnWidgetResize(true);
        context.sendBroadcast(new Intent(context.getPackageName()+C.REMOVE_RESIZE_VIEWS));

        if(this.getChildCount()==1){
            this.addView(resizeImage(C.WIDGET_LEFT));
            this.addView(resizeImage(C.WIDGET_RIGHT));
            this.addView(resizeImage(C.WIDGET_TOP));
            this.addView(resizeImage(C.WIDGET_BOTTOM));
        }
    }

    private View resizeImage(final int gravity){
        View v = new View(context);
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(C.dpToPx(context,26),C.dpToPx(context,26));
        switch(gravity){
            case C.WIDGET_LEFT:
                pr.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                pr.addRule(RelativeLayout.CENTER_VERTICAL);
                break;
            case C.WIDGET_RIGHT:
                pr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                pr.addRule(RelativeLayout.CENTER_VERTICAL);
                break;
            case C.WIDGET_TOP:
                pr.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                pr.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case C.WIDGET_BOTTOM:
                pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                pr.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
        }
        resizeImageLine(gravity);
        v.setLayoutParams(pr);
        v.setBackgroundResource(R.drawable.a_resize_point);
        v.setOnTouchListener(new OnTouchListener(){
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v,MotionEvent m){
                if(callback!=null){
                    callback.onResize(m,gravity);
                }
                return true;
            }
        });

        return v;
    }

    private void resizeImageLine(int gravity){
        View v = new View(context);
        RelativeLayout.LayoutParams pr = null;
        final int mar = C.dpToPx(context,12);
        switch(gravity){
            case C.WIDGET_LEFT:
                pr = new RelativeLayout.LayoutParams(C.dpToPx(context,2),LayoutParams.MATCH_PARENT);
                pr.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                pr.setMargins(mar,mar,mar,mar);
                break;
            case C.WIDGET_RIGHT:
                pr = new RelativeLayout.LayoutParams(C.dpToPx(context,2),LayoutParams.MATCH_PARENT);
                pr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                pr.setMargins(mar,mar,mar,mar);
                break;
            case C.WIDGET_TOP:
                pr = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,C.dpToPx(context,2));
                pr.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                pr.setMargins(mar,mar,mar,mar);
                break;
            case C.WIDGET_BOTTOM:
                pr = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,C.dpToPx(context,2));
                pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                pr.setMargins(mar,mar,mar,mar);
                break;
        }
        v.setLayoutParams(pr);
        v.setBackgroundColor(Color.CYAN);

        addView(v);
    }

    public void removeResizeImage(boolean isRemoveAll){
        if(isRemoveAll){
            setOnWidgetResize(false);
            for(int i = getChildCount(); i>1; i--){
                this.removeViewAt(i-1);
            }
            return;
        }
        if(!isOnWidgetResize){
            for(int i = getChildCount(); i>1; i--){
                this.removeViewAt(i-1);
            }
        }
    }

    public interface OnResizeCallBack{
        void onResize(MotionEvent m,int gravity);
    }

    public class ErrorLayout extends LinearLayout{//add this class to detect error layout

        public ErrorLayout(Context context){
            super(context);
        }
    }
}