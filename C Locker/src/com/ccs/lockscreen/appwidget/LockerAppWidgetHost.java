package com.ccs.lockscreen.appwidget;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;

public class LockerAppWidgetHost extends AppWidgetHost{
    private static LockerAppWidgetHost instance = null;
    private WidgetOnTouchListener callBack;
    private String pkgName;

    public LockerAppWidgetHost(Context context,int hostId){
        super(context,hostId);
    }

    public static synchronized LockerAppWidgetHost getInstance(Context context,int hostId){
        if(instance==null){
            instance = new LockerAppWidgetHost(context,hostId);
        }
        return instance;
    }

    public AppWidgetHostView setupWidget(Context context,int intAppWidgetId,
            AppWidgetProviderInfo appWidgetInfo,WidgetOnTouchListener callBack){
        this.pkgName = appWidgetInfo.provider.getPackageName();
        this.callBack = callBack;
        //TODO warning!! must use getApplicationContext!! else will FC
        return createView(context.getApplicationContext(),intAppWidgetId,appWidgetInfo);
    }

    @Override
    public void stopListening(){
        super.stopListening();
        clearViews();
    }

    @Override
    protected AppWidgetHostView onCreateView(Context context,int intAppWidgetId,
            AppWidgetProviderInfo appWidgetInfo){
        final LockerAppWidgetHostView hostView = new LockerAppWidgetHostView(context,pkgName,callBack);
        hostView.setAppWidget(intAppWidgetId,appWidgetInfo);
        return hostView;
    }
}