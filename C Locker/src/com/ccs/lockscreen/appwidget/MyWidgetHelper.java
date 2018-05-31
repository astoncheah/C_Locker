package com.ccs.lockscreen.appwidget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataWidgets;
import com.ccs.lockscreen.data.InfoWidget;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;
import com.ccs.lockscreen.utils.WallpaperHandler;
import com.ccs.lockscreen_pro.ListWidgetApps;
import com.ccs.lockscreen_pro.SettingsCalendar;
import com.ccs.lockscreen_pro.SettingsClockAnalog;
import com.ccs.lockscreen_pro.SettingsClockDigital;
import com.ccs.lockscreen_pro.SettingsPersonalMsg;
import com.ccs.lockscreen_pro.SettingsRSS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyWidgetHelper implements WidgetOnTouchListener{
    public static final int REQUEST_PICK_APPWIDGET_RESULT = 2001;
    public static final int REQUEST_BIND_APPWIDGET_RESULT = 2002;
    public static final int REQUEST_CREATE_APPWIDGET_RESULT = 2003;
    public static final int WIDGET_SETUP_SUCCESSFUL = 1;
    public static final int WIDGET_SETUP_FALIED = 2;
    public static final int DELETE_ALL = -999;
    public static final int SLOT_FULL = -9999;
    private MyCLocker appHandler;
    private DataWidgets dbWidgets;
    private AppWidgetManager mAppWidgetManager;
    private LockerAppWidgetHost mAppWidgetHost;
    private Context context;
    private Fragment fragment;
    private WidgetSetupListener listener;
    private MyAlertDialog myAlertDialog;
    private int intWidgetProfile, intHostViewIndex, intLayoutWidth, intLayoutHeight;
    //utils
    private int TOUCH_GAP_X, TOUCH_GAP_Y, STARTING_RAW_X, STARTING_RAW_Y;
    private int maxWidgetSpanX, maxWidgetSpanY, moveGap;
    private int vLeft, vTop, vWidth, vHeight;
    private int viewX, viewY, newX, newY;
    private int viewSpanX, viewSpanY, newSpanY, newSpanX;
    private boolean hasMoved;

    public MyWidgetHelper(Context context){
        this(context,null);
    }

    public MyWidgetHelper(Context context,WidgetSetupListener listener){
        this(context,null,listener);
    }

    public MyWidgetHelper(Context context,Fragment fragment,WidgetSetupListener listener){
        try{
            this.context = context;
            this.fragment = fragment;
            this.listener = listener;

            appHandler = new MyCLocker(context);
            myAlertDialog = new MyAlertDialog(context);

            setupWidgetHost();
            dbWidgets = new DataWidgets(context);

            intLayoutWidth = C.getRealDisplaySize(context).x;
            intLayoutHeight = C.getMaxWidgetSpanY(context)*C.getSpanToPxY(context,1);
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    public final void setupWidgetHost(){
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
    }

    public final void closeAlertDialog(){
        try{
            if(myAlertDialog!=null){
                myAlertDialog.close();
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    public final void closeAll(){
        try{
            if(myAlertDialog!=null){
                myAlertDialog.close();
            }
            if(dbWidgets!=null){
                dbWidgets.close();
                dbWidgets = null;
            }
            if(mAppWidgetHost!=null){
                //mAppWidgetHost.stopListening(); //TODO temporary disable this
            }
            closeData();
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    public final void closeData(){
        try{
            if(dbWidgets!=null){
                dbWidgets.close();
                dbWidgets = null;
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    public final void handleActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case REQUEST_PICK_APPWIDGET_RESULT:
                    if(resultCode==Activity.RESULT_OK){
                        requestBindWidget(data);
                    }
                    break;
                case REQUEST_BIND_APPWIDGET_RESULT:
                    if(resultCode==Activity.RESULT_OK){
                        configureWidget(data);
                    }else{
                        if(data!=null){
                            final Bundle extras = data.getExtras();
                            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                            if(appWidgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
                                deleteWidgetData(intWidgetProfile,appWidgetId);
                            }
                        }
                    }
                    break;
                case REQUEST_CREATE_APPWIDGET_RESULT:
                    if(resultCode==Activity.RESULT_OK){
                        saveAppsWidget(data);
                    }else{
                        if(data!=null){
                            final Bundle extras = data.getExtras();
                            final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                            if(appWidgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
                                deleteWidgetData(intWidgetProfile,appWidgetId);
                            }
                        }
                    }
                    break;
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    public final LockerAppWidgetHostView getHostView(int intAppWidgetId,
            WidgetOnTouchListener callBack){
        try{
            AppWidgetProviderInfo appWidgetInfo = getAppWidgetInfo(intAppWidgetId);
            if(appWidgetInfo==null){
                return null;
            }

            final InfoWidget widget = dbWidgets.getWidget(intAppWidgetId);
            int spanX = widget.getWidgetSpanX();
            int spanY = widget.getWidgetSpanY();

            int width = C.getSpanToPxX(context,spanX)-C.dpToPx(context,10);
            int height = C.getSpanToPxY(context,spanY)-C.dpToPx(context,10);

            final LockerAppWidgetHostView hostView = (LockerAppWidgetHostView)mAppWidgetHost.setupWidget(context,intAppWidgetId,appWidgetInfo,callBack);

            //Log.e("MyAppWidgetHostView","getWidget: "+width+"/"+height);
            final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(width,height);
            if(hostView!=null){
                hostView.setId(intAppWidgetId);
                hostView.setLayoutParams(pr);
                updateWidget(hostView,spanX,spanY);
                return hostView;
            }
        }catch(OutOfMemoryError e){
            appHandler.saveErrorLog("MyWidgetHelper>OutOfMemoryError: "+e,null);
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
        return null;
    }

    public final LockerAppWidgetHostView getHostViewSimple(int intAppWidgetId,
            WidgetOnTouchListener callBack){
        try{
            AppWidgetProviderInfo appWidgetInfo = getAppWidgetInfo(intAppWidgetId);
            if(appWidgetInfo==null){
                return null;
            }
            int spanX = C.getMaxWidgetSpanX(context);
            int spanY = C.getDefaultWidgetSpanY(context,appWidgetInfo.minHeight);
            if(spanY>2){
                spanY = 2;
            }
            final int w = C.getSpanToPxX(context,spanX);
            final int h = C.getDefaultSpanToPxY(context,spanY);

            final LockerAppWidgetHostView hostView = (LockerAppWidgetHostView)mAppWidgetHost.setupWidget(context,intAppWidgetId,appWidgetInfo,callBack);
            final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(w,h);
            hostView.setLayoutParams(pr);
            updateWidget(hostView,spanX,spanY);
            return hostView;
        }catch(OutOfMemoryError e){
            appHandler.saveErrorLog("MyWidgetHelper>OutOfMemoryError: "+e,null);
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
        return null;
    }

    private AppWidgetProviderInfo getAppWidgetInfo(int id){
        return mAppWidgetManager.getAppWidgetInfo(id);
    }

    public final void updateWidget(LockerAppWidgetHostView hostView,int spanX,int spanY){
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

    public final ArrayList<InfoWidget> getWidgetList(int intWidgetProfile){
        return (ArrayList<InfoWidget>)dbWidgets.getAllWidgets(intWidgetProfile);
    }

    public final ArrayList<InfoWidget> getWidgetList(){
        return (ArrayList<InfoWidget>)dbWidgets.getAllWidgets();
    }

    public final List<AppWidgetProviderInfo> getInstalledProviders(){
        return mAppWidgetManager.getInstalledProviders();
    }

    public final int allocateAppWidgetId(){
        return mAppWidgetHost.allocateAppWidgetId();
    }

    public final int getWidgetCount(int intWidgetProfile){
        final ArrayList<InfoWidget> widgetList = getWidgetList(intWidgetProfile);
        int count = 0;
        if(widgetList!=null){
            for(InfoWidget infoWidget : widgetList){
                if(infoWidget.getWidgetId()>0){
                    count++;
                }
            }
        }
        return count;
    }

    public final int getWidgetIndex(InfoWidget currentWidget,RelativeLayout v){
        int getHostViewIndex = v.getChildCount()-1;
        if(currentWidget!=null){
            getHostViewIndex = currentWidget.getWidgetIndex();
        }
        return getHostViewIndex;
    }

    public final int getLayoutHeight(){
        return intLayoutHeight;
    }

    public final void saveWidgetData(InfoWidget widgets){
        dbWidgets.addWidget(widgets);
    }

    public final void updateWidgetIndex(int intWidgetProfile,int widgetId,int getHostViewIndex){
        dbWidgets.updateWidgetIndex(intWidgetProfile,widgetId,getHostViewIndex);
    }

    public final void updateWidgetSize(int intWidgetProfile,int widgetId,int x,int y,int spanX,
            int spanY){
        dbWidgets.updateWidgetSize(intWidgetProfile,widgetId,x,y,spanX,spanY);
    }

    public final void emptyLayoutLongPressAction(final int intWidgetProfile,
            final RelativeLayout lytWidgets,final WallpaperHandler wallpaperHandler){
        context.sendBroadcast(new Intent(context.getPackageName()+C.REMOVE_RESIZE_VIEWS_ALL));
        this.intWidgetProfile = intWidgetProfile;
        myAlertDialog.addNewWidgets(new OnDialogListener(){
            @Override
            public void onDialogClickListener(int dialogType,int result){
                if(result==0){//add default widget
                    selectDefaultWidgets();
                }else if(result==1){//add app widgets
                    selectCustomWidget();
                }else if(result==2){//delete all
                    removeWidget(intWidgetProfile,DELETE_ALL,lytWidgets,null);
                }else if(result==3){//widget grid settings
                    myAlertDialog.selectWidgetGridWidth(new OnDialogListener(){
                        @Override
                        public void onDialogClickListener(int dialogType,int result){
                            myAlertDialog.selectWidgetGridHeight(new OnDialogListener(){
                                @Override
                                public void onDialogClickListener(int dialogType,int result){
                                    context.sendBroadcast(new Intent(context.getPackageName()+C.UPDATE_CONFIGURE));
                                }
                            });
                        }
                    });
                }else if(result==4){//wallpaper
                    wallpaperHandler.pickPictures(WallpaperHandler.getWallpaperNo(intWidgetProfile));
                }
            }
        });
    }

    @SuppressWarnings("rawtypes")
    public final void widgetLongPressAction(final int intWidgetProfile,
            final RelativeLayout lytWidgets,final LockerWidgetLayout widget){
        context.sendBroadcast(new Intent(context.getPackageName()+C.REMOVE_RESIZE_VIEWS_ALL));
        this.intWidgetProfile = intWidgetProfile;
        final int widgetId = widget.getId();

        myAlertDialog.widgetLongPress(widgetId,new OnDialogListener(){
            @Override
            public void onDialogClickListener(int dialogType,int result){
                try{
                    if(result==0){//resize/move
                        widget.addResizeImage(new LockerWidgetLayout.OnResizeCallBack(){
                            @Override
                            public void onResize(MotionEvent m,int gravity){
                                onWidgetResize(m,widget,gravity);
                            }
                        });
                    }else if(result==1){//delete one
                        removeWidget(intWidgetProfile,widgetId,lytWidgets,widget);
                    }else if(result==2){// delete all
                        removeWidget(intWidgetProfile,DELETE_ALL,lytWidgets,widget);
                    }else if(result==3){//settings
                        Intent i = getSettingsIntent(widgetId);
                        if(i!=null){
                            context.startActivity(i);
                        }
                    }
                }catch(Exception e){
                    appHandler.saveErrorLog(null,e);
                }
            }
        });
    }

    public final void loadWidgets(int intWidgetProfile,final RelativeLayout lytWidgets,
            WidgetOnTouchListener callback,boolean isDeleteAll){
        this.intWidgetProfile = intWidgetProfile;
        ArrayList<InfoWidget> widgetList = getWidgetList(intWidgetProfile);
        if(widgetList!=null && widgetList.size()>0){
            Collections.sort(widgetList,new Comparator<InfoWidget>(){
                @Override
                public int compare(InfoWidget arg0,InfoWidget arg1){
                    String str1 = arg0.getWidgetIndex()+"";
                    String str2 = arg1.getWidgetIndex()+"";
                    return str1.compareTo(str2);
                }
            });
            for(InfoWidget item : widgetList){
                if(item.getWidgetType()==DataWidgets.WIDGET_TYPE_DEFAULT){
                    loadDefaulWidget(lytWidgets,item,callback);
                }else if(item.getWidgetType()==DataWidgets.WIDGET_TYPE_CUSTOM){
                    loadAppsWidget(lytWidgets,item,callback);
                }
            }
        }else{
            if(!isDeleteAll){
                //saveDefaulWidget(C.CLOCK_DIGITAL_CENTER,getMaxWidgetSpanX(),1);
            }
            //loadDefaulWidget(lytWidgets,C.CLOCK_DIGITAL_CENTER,LayoutParams.MATCH_PARENT,1);
        }
    }

    //
    private void selectDefaultWidgets(){
        myAlertDialog.selectDefaultWidgets(new OnDialogListener(){
            @Override
            public void onDialogClickListener(int dialogType,int result){
                if(result==0){
                    saveDefaulWidget(C.CLOCK_ANALOG_CENTER,C.getMaxWidgetSpanX(context),2);
                }else if(result==1){
                    saveDefaulWidget(C.CLOCK_DIGITAL_CENTER,C.getMaxWidgetSpanX(context),1);
                }else if(result==2){
                    saveDefaulWidget(C.CLOCK_OWNER_MSG,C.getMaxWidgetSpanX(context),1);
                }else if(result==3){
                    saveDefaulWidget(C.CLOCK_EVENT_LIST,C.getMaxWidgetSpanX(context),1);
                }else if(result==4){
                    saveDefaulWidget(C.CLOCK_RSS,C.getMaxWidgetSpanX(context),1);
                }
            }
        });
    }

    private void selectCustomWidget(){
        try{
            final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            boolean cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);
            if(!cBoxLockerOk && getWidgetCount(intWidgetProfile)>=2){
                myAlertDialog.upgradeLocker();
                Toast.makeText(context,context.getString(R.string.max_widget),Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(context,ListWidgetApps.class);
            i.putExtra(C.LIST_APP_WIDGET_TYPE,C.LIST_ALL_APPS_WIDGETS);

            if(fragment!=null){
                fragment.startActivityForResult(i,REQUEST_PICK_APPWIDGET_RESULT);
            }else{
                ((Activity)context).startActivityForResult(i,REQUEST_PICK_APPWIDGET_RESULT);
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

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
                        boolean success = false;
                        success = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId,appWidgetInfo.provider);
                        if(success){
                            configureWidget(data);
                        }else{
                            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,appWidgetInfo.provider);
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS,data.getExtras());
                            if(fragment!=null){
                                fragment.startActivityForResult(intent,REQUEST_BIND_APPWIDGET_RESULT);
                            }else{
                                ((Activity)context).startActivityForResult(intent,REQUEST_BIND_APPWIDGET_RESULT);
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
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
                fragment.startActivityForResult(intent,REQUEST_CREATE_APPWIDGET_RESULT);
            }else{
                ((Activity)context).startActivityForResult(intent,REQUEST_CREATE_APPWIDGET_RESULT);
            }
        }else{
            saveAppsWidget(data);
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

    private boolean isWidgetInstalled(InfoWidget widgets){
        final List<AppWidgetProviderInfo> listwidget = mAppWidgetManager.getInstalledProviders();
        String pkgNm = widgets.getWidgetPkgsName();
        String classNm = widgets.getWidgetClassName();
        if(pkgNm!=null && classNm!=null && listwidget!=null){
            for(int i = 0; i<listwidget.size(); i++){
                if(pkgNm.equals(listwidget.get(i).provider.getPackageName()) && classNm.equals(listwidget.get(i).provider.getClassName())){
                    return true;
                }
            }
        }
        return false;
    }

    private void restoreWidget(InfoWidget widgets){
        final int appWidgetId = widgets.getWidgetId();
        deleteWidgetData(intWidgetProfile,appWidgetId);
        //restoreWidget(final LockerWidgetLayout widget,View v)
        //String conbinedName = v.getContentDescription().toString();
        //String[] name = conbinedName.split(";");//split pkg and class name = com.ccs.blah;com.ccs.blah.Class

        int id = allocateAppWidgetId();
        Intent i = new Intent();
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        i.putExtra("strGetWidgetPkgName",widgets.getWidgetPkgsName());
        i.putExtra("strGetWidgetClassName",widgets.getWidgetClassName());
        requestBindWidget(i);
    }

    private void removeWidget(final int intWidgetProfile,int widgetId,RelativeLayout lytWidgets,
            View widget){
        if(widgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
            //mAppWidgetHost.deleteAppWidgetId(intAppWidgetId);
            if(widgetId==DELETE_ALL){
                deleteWidgetData(intWidgetProfile,DELETE_ALL);
                ArrayList<InfoWidget> widgetList = getWidgetList(intWidgetProfile);
                if(widgetList!=null && widgetList.size()>0){
                    for(InfoWidget item : widgetList){
                        if(item.getWidgetType()==DataWidgets.WIDGET_TYPE_CUSTOM){
                            removeWidget(intWidgetProfile,item.getWidgetId(),lytWidgets,null);
                        }
                    }
                }
                if(listener!=null){
                    listener.onWidgetDeleteAll();
                }
            }else{
                deleteWidgetData(intWidgetProfile,widgetId);
                if(lytWidgets!=null && lytWidgets.getChildCount()>0){
                    if(widget!=null){
                        lytWidgets.removeView(widget);
                    }
                }
            }
        }
    }

    private void deleteWidgetData(int intWidgetProfile,int widgetId){
        if(widgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
            if(widgetId==DELETE_ALL){
                dbWidgets.deleteAllWidgets(intWidgetProfile);
            }else{
                mAppWidgetHost.deleteAppWidgetId(widgetId);
                dbWidgets.deleteWidgets(intWidgetProfile,widgetId);
            }
        }
    }

    private void saveDefaulWidget(final int widgetId,int spanX,int spanY){
        try{
            final ArrayList<InfoWidget> widgetList = getWidgetList(intWidgetProfile);
            if(widgetList!=null){
                for(InfoWidget infoWidget : widgetList){
                    if(infoWidget.getWidgetId()==widgetId){
                        Toast.makeText(context,context.getString(R.string.settings_widget_not_allow),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }

            AvailableSlot slot = getAvailableSlot(spanX,spanY);
            if(slot.getX()==SLOT_FULL){
                Toast.makeText(context,context.getString(R.string.settings_widget_size_over),Toast.LENGTH_SHORT).show();
                return;
            }
            InfoWidget widgets = new InfoWidget(-1,//primary id not using
                    intWidgetProfile,widgetId,DataWidgets.WIDGET_TYPE_DEFAULT,context.getPackageName(),//no use
                    context.getPackageName(),0,slot.getX(),slot.getY(),spanX,spanY,//x,y,span x,span y not using
                    0,0);
            saveWidgetData(widgets);
            if(listener!=null){
                listener.onWidgetSetupFinished(WIDGET_SETUP_SUCCESSFUL,widgetId);
            }
            if(widgetId==C.CLOCK_OWNER_MSG){
                Intent i = getSettingsIntent(widgetId);
                if(i!=null){
                    context.startActivity(i);
                }
            }
            return;
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
        if(listener!=null){
            listener.onWidgetSetupFinished(WIDGET_SETUP_FALIED,widgetId);
        }
    }

    private void saveAppsWidget(Intent data){
        try{
            AppWidgetProviderInfo appWidgetInfo = null;
            int intAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            int spanX = 0;
            int spanY = 0;
            if(data!=null){
                Bundle extras = data.getExtras();
                intAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
                appWidgetInfo = getAppWidgetInfo(intAppWidgetId);

                spanX = C.getWidgetSpanX(context,appWidgetInfo.minWidth);
                spanY = C.getWidgetSpanY(context,appWidgetInfo.minHeight);
                AvailableSlot slot = getAvailableSlot(spanX,spanY);
                if(slot.getX()==SLOT_FULL){
                    Toast.makeText(context,context.getString(R.string.settings_widget_size_over),Toast.LENGTH_SHORT).show();
                    deleteWidgetData(intWidgetProfile,intAppWidgetId);
                    return;
                }
                InfoWidget widgets = new InfoWidget();
                widgets.setWidgetProfile(intWidgetProfile);
                widgets.setWidgetId(intAppWidgetId);
                widgets.setWidgetType(DataWidgets.WIDGET_TYPE_CUSTOM);
                widgets.setWidgetPkgsName(appWidgetInfo.provider.getPackageName());
                widgets.setWidgetClassName(appWidgetInfo.provider.getClassName());
                widgets.setWidgetIndex(intHostViewIndex);
                widgets.setWidgetX(slot.getX());
                widgets.setWidgetY(slot.getY());
                widgets.setWidgetSpanX(spanX);
                widgets.setWidgetSpanY(spanY);
                widgets.setWidgetMinWidth(appWidgetInfo.minWidth);
                widgets.setWidgetMinHeight(appWidgetInfo.minHeight);

                saveWidgetData(widgets);
                if(listener!=null){
                    listener.onWidgetSetupFinished(WIDGET_SETUP_SUCCESSFUL,intAppWidgetId);
                }
                return;
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
        if(listener!=null){
            listener.onWidgetSetupFinished(WIDGET_SETUP_FALIED,AppWidgetManager.INVALID_APPWIDGET_ID);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadDefaulWidget(final RelativeLayout lytWidgets,InfoWidget widgets,
            final WidgetOnTouchListener callback){
        @SuppressWarnings("rawtypes") LockerWidgetLayout mView = null;
        final int widgetId = widgets.getWidgetId();
        final int x = widgets.getWidgetX();
        final int y = widgets.getWidgetY();
        int spanX = widgets.getWidgetSpanX();
        int spanY = widgets.getWidgetSpanY();

        switch(widgetId){
            case C.CLOCK_ANALOG_CENTER:
                mView = new LockerWidgetLayout<>(context,widgetId,callback);
                break;
            case C.CLOCK_DIGITAL_CENTER:
                mView = new LockerWidgetLayout<>(context,widgetId,callback);
                break;
            case C.CLOCK_EVENT_LIST:
                mView = new LockerWidgetLayout<>(context,widgetId,callback);
                break;
            case C.CLOCK_RSS:
                mView = new LockerWidgetLayout<>(context,widgetId,callback);
                break;
            case C.CLOCK_OWNER_MSG:
                mView = new LockerWidgetLayout<>(context,widgetId,callback);
                break;
        }
        if(mView!=null){
            if(spanX==LayoutParams.MATCH_PARENT){
                spanX = C.getMaxWidgetSpanX(context);
            }
            mView.setId(widgetId);
            mView.setX(C.getSpanToPxX(context,x));
            mView.setY(C.getSpanToPxY(context,y));
            mView.setSpanX(spanX);
            mView.setSpanY(spanY);
            mView.setOnTouchListener(new OnTouchListener(){
                @SuppressWarnings("rawtypes")
                @Override
                public boolean onTouch(View v,MotionEvent m){
                    if(((LockerWidgetLayout)v).isOnWidgetResize()){
                        onWidgetMove(m,v);//dont put hostview
                        return true;
                    }else{
                        if(callback!=null){
                            callback.onWidgetTouchEvent((LockerWidgetLayout)v,m,null,null);
                        }
                    }
                    return false;
                }
            });
            if(callback==null){//no call back means not from Locker
                mView.setOnLongClickListener(new OnLongClickListener(){
                    @SuppressWarnings("rawtypes")
                    @Override
                    public boolean onLongClick(View v){
                        widgetLongPressAction(intWidgetProfile,lytWidgets,(LockerWidgetLayout)v);
                        return true;
                    }
                });
            }
            if(lytWidgets!=null){
                //Log.e("loadDefaulWidget",intWidgetProfile+"/"+widgetId+"/"+spanX+"/"+spanY);
                lytWidgets.addView(mView,new RelativeLayout.LayoutParams(C.getSpanToPxX(context,spanX),C.getSpanToPxY(context,spanY)));
            }
            return;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void loadAppsWidget(final RelativeLayout lytWidgets,final InfoWidget widgets,
            WidgetOnTouchListener callback){
        try{
            if(widgets!=null){
                final int intAppWidgetId = widgets.getWidgetId();
                if(intAppWidgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){
                    final LockerAppWidgetHostView hostView = getHostView(widgets.getWidgetId(),callback);
                    if(hostView==null){//no widget found
                        if(!isWidgetInstalled(widgets)){//no need to add layout if widget not available
                            deleteWidgetData(intWidgetProfile,widgets.getWidgetId());
                            return;
                        }
                        @SuppressWarnings("rawtypes") final LockerWidgetLayout errorLayout = new LockerWidgetLayout<>(context,widgets.getWidgetPkgsName(),widgets.getWidgetClassName());

                        final int x = widgets.getWidgetX();
                        final int y = widgets.getWidgetY();
                        final int spanX = widgets.getWidgetSpanX();
                        final int spanY = widgets.getWidgetSpanY();

                        errorLayout.setId(intAppWidgetId);
                        errorLayout.setX(C.getSpanToPxX(context,x));
                        errorLayout.setY(C.getSpanToPxY(context,y));
                        errorLayout.setSpanX(spanX);
                        errorLayout.setSpanY(spanY);
                        if(callback==null){//no call back means not from Locker
                            errorLayout.setOnLongClickListener(new OnLongClickListener(){
                                @SuppressWarnings("rawtypes")
                                @Override
                                public boolean onLongClick(View v){
                                    View err = ((LockerWidgetLayout)v).getChildAt(0);
                                    if(err instanceof LockerWidgetLayout.ErrorLayout){
                                        removeWidget(intWidgetProfile,v.getId(),lytWidgets,v);
                                        restoreWidget(widgets);
                                    }
                                    return true;
                                }
                            });
                        }
                        if(lytWidgets!=null){
                            lytWidgets.addView(errorLayout,new RelativeLayout.LayoutParams(C.getSpanToPxX(context,spanX),C.getSpanToPxY(context,spanY)));
                        }
                    }else{
                        @SuppressWarnings("rawtypes") final LockerWidgetLayout mView = new LockerWidgetLayout<LockerAppWidgetHostView>(context,hostView);

                        final int x = widgets.getWidgetX();
                        final int y = widgets.getWidgetY();
                        final int spanX = widgets.getWidgetSpanX();
                        final int spanY = widgets.getWidgetSpanY();

                        mView.setId(intAppWidgetId);
                        mView.setX(C.getSpanToPxX(context,x));
                        mView.setY(C.getSpanToPxY(context,y));
                        mView.setSpanX(spanX);
                        mView.setSpanY(spanY);
                        if(callback==null){
                            hostView.setWidgetOnTouchListener(this);
                            hostView.setOnTouchListener(new OnTouchListener(){
                                @Override
                                public boolean onTouch(View v,MotionEvent m){
                                    if(mView.isOnWidgetResize()){
                                        onWidgetMove(m,mView);//dont put hostview
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            hostView.setOnLongClickListener(new OnLongClickListener(){
                                @Override
                                public boolean onLongClick(View v){
                                    widgetLongPressAction(intWidgetProfile,lytWidgets,mView);//dont put hostview
                                    return true;
                                }
                            });
                        }
                        if(lytWidgets!=null){
                            lytWidgets.addView(mView,new RelativeLayout.LayoutParams(C.getSpanToPxX(context,spanX),C.getSpanToPxY(context,spanY)));
                        }
                        updateWidget(hostView,spanX,spanY);
                    }
                }
            }
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    @Override
    public boolean onWidgetTouchEvent(View v,MotionEvent m,
            CheckLongPressHelper widgetLongPressHelper,String pkgName){
        //Log.e("onWidgetTouchEvent","v: "+v);
        switch(m.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP
                moveGap = C.dpToPx(context,5);

                if(widgetLongPressHelper!=null){
                    widgetLongPressHelper.postCheckForLongPress();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int gapX = (int)(STARTING_RAW_X-m.getRawX());
                int gapY = (int)(STARTING_RAW_Y-m.getRawY());

                //if any movement, cancel long press
                if(gapX>moveGap || gapX<(-moveGap)){
                    if(widgetLongPressHelper!=null){
                        widgetLongPressHelper.cancelLongPress();
                        return true;
                    }
                }
                if(gapY>moveGap || gapY<(-moveGap)){
                    if(widgetLongPressHelper!=null){
                        widgetLongPressHelper.cancelLongPress();
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if(widgetLongPressHelper!=null){
                    if(widgetLongPressHelper.hasPerformedLongPress()){
                        return true;
                    }
                    widgetLongPressHelper.cancelLongPress();
                }
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private void onWidgetMove(MotionEvent m,View v){
        switch(m.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                context.sendBroadcast(new Intent(context.getPackageName()+C.SCROLLING_DISABLE));
                hasMoved = false;
                moveGap = C.dpToPx(context,5);

                STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP

                vLeft = (int)v.getX();
                vTop = (int)v.getY();

                //default size
                viewX = C.getPxToSpanX(context,true,vLeft);
                viewY = C.getPxToSpanY(context,true,vTop);
                viewSpanX = ((LockerWidgetLayout)v).getSpanX();
                viewSpanY = ((LockerWidgetLayout)v).getSpanY();
                break;
            case MotionEvent.ACTION_MOVE:
                int gapX = (int)(STARTING_RAW_X-m.getRawX());
                int gapY = (int)(STARTING_RAW_Y-m.getRawY());

                if(gapX>moveGap || gapX<(-moveGap)){
                    hasMoved = true;
                }
                if(gapY>moveGap || gapY<(-moveGap)){
                    hasMoved = true;
                }

                int newLeft = vLeft-gapX;
                int newTop = vTop-gapY;

                v.setX(newLeft);
                v.setY(newTop);

                newX = C.getPxToSpanX(context,true,(int)v.getX());
                newY = C.getPxToSpanY(context,true,(int)v.getY());

                if(viewX-newX!=0){
                    viewX = newX;
                    Log.e("onWidgetMove","ACTION_MOVE>viewX: "+viewX);
                }
                if(viewY-newY!=0){
                    viewY = newY;
                    Log.e("onWidgetMove","ACTION_MOVE>viewY: "+viewY);
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.e("onWidgetMove>ACTION_UP","xy: "+viewX+"/"+viewY);
                if(viewX<0){
                    viewX = 0;
                }
                if(viewY<0){
                    viewY = 0;
                }
                if(isSlotAvailable(v.getId(),viewX,viewY,((LockerWidgetLayout)v).getSpanX(),((LockerWidgetLayout)v).getSpanY())){
                    int finalX = C.getSpanToPxX(context,viewX);
                    int finalY = C.getSpanToPxY(context,viewY);
                    v.setX(finalX);
                    v.setY(finalY);
                    updateWidgetSize(intWidgetProfile,v.getId(),viewX,viewY,viewSpanX,viewSpanY);
                }else{
                    v.setX(vLeft);
                    v.setY(vTop);
                    Toast.makeText(context,context.getString(R.string.settings_widget_size_over),Toast.LENGTH_SHORT).show();
                }
                context.sendBroadcast(new Intent(context.getPackageName()+C.SCROLLING_ENABLE));
                if(!hasMoved){
                    context.sendBroadcast(new Intent(context.getPackageName()+C.REMOVE_RESIZE_VIEWS_ALL));
                }
                break;
        }
    }

    @SuppressWarnings("rawtypes")
    private void onWidgetResize(MotionEvent m,View v,int gravity){
        switch(m.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                context.sendBroadcast(new Intent(context.getPackageName()+C.SCROLLING_DISABLE));

                TOUCH_GAP_X = C.getSpanToPxX(context,1);
                TOUCH_GAP_Y = C.getSpanToPxY(context,1);

                STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP

                maxWidgetSpanX = C.getMaxWidgetSpanX(context);
                maxWidgetSpanY = C.getMaxWidgetSpanY(context);

                vLeft = (int)v.getX();
                vTop = (int)v.getY();
                vWidth = v.getWidth();
                vHeight = v.getHeight();

                //left/top point
                viewX = C.getPxToSpanX(context,true,vLeft);
                viewY = C.getPxToSpanY(context,true,vTop);

                //right/bottom point
                viewSpanX = ((LockerWidgetLayout)v).getSpanX();
                viewSpanY = ((LockerWidgetLayout)v).getSpanY();
                break;
            case MotionEvent.ACTION_MOVE:
                int gapX = (int)(STARTING_RAW_X-m.getRawX());
                int gapY = (int)(STARTING_RAW_Y-m.getRawY());
                int newLeft = vLeft-gapX;
                int newTop = vTop-gapY;
                int newW = vWidth-gapX;
                int newH = vHeight-gapY;

                if(gravity==C.WIDGET_LEFT){
                    boolean isMoveLeftAllowed = newLeft>0;
                    boolean isMoveRightAllowed = (vLeft+vWidth)-newLeft>TOUCH_GAP_X;
                    if(isMoveLeftAllowed && isMoveRightAllowed){
                        v.setX(newLeft);
                        v.setLayoutParams(new RelativeLayout.LayoutParams(vLeft+vWidth-newLeft,vHeight));

                        newX = C.getPxToSpanX(context,true,(int)v.getX());
                        newSpanX = C.getPxToSpanX(context,false,v.getWidth());
                        if(viewSpanX-newSpanX!=0){
                            viewX = newX;
                            viewSpanX = newSpanX;
                            Log.e("onWidgetResize","ACTION_MOVE>viewX: "+viewX+"/"+viewSpanX);
                        }
                    }
                }else if(gravity==C.WIDGET_TOP){
                    boolean isMoveTopAllowed = newTop>0;
                    boolean isMoveBottomAllowed = (vTop+vHeight)-newTop>TOUCH_GAP_Y;
                    if(isMoveTopAllowed && isMoveBottomAllowed){
                        v.setY(newTop);
                        v.setLayoutParams(new RelativeLayout.LayoutParams(vWidth,vTop+vHeight-newTop));

                        newY = C.getPxToSpanY(context,true,(int)v.getY());
                        newSpanY = C.getPxToSpanY(context,false,v.getHeight());
                        if(viewSpanY-newSpanY!=0){
                            viewY = newY;
                            viewSpanY = newSpanY;
                            Log.e("onWidgetResize","ACTION_MOVE>viewY: "+viewY+"/"+viewSpanY);
                        }
                    }
                }else if(gravity==C.WIDGET_RIGHT){
                    boolean isMoveLeftAllowed = newW>TOUCH_GAP_X;
                    boolean isMoveRightAllowed = newW<(intLayoutWidth-vLeft);
                    if(isMoveLeftAllowed && isMoveRightAllowed){
                        v.setLayoutParams(new RelativeLayout.LayoutParams(newW,vHeight));

                        newSpanX = C.getPxToSpanX(context,false,v.getWidth());
                        if(viewSpanX-newSpanX!=0){
                            viewSpanX = newSpanX;
                            Log.e("onWidgetResize","ACTION_MOVE>viewSpanX: "+viewSpanX);
                        }
                    }
                }else if(gravity==C.WIDGET_BOTTOM){
                    boolean isMoveTopAllowed = newH>TOUCH_GAP_Y;
                    boolean isMoveBottomAllowed = newH<intLayoutHeight-vTop;
                    if(isMoveTopAllowed && isMoveBottomAllowed){
                        v.setLayoutParams(new RelativeLayout.LayoutParams(vWidth,newH));

                        newSpanY = C.getPxToSpanY(context,false,v.getHeight());
                        if(viewSpanY-newSpanY!=0){
                            viewSpanY = newSpanY;
                            Log.e("AonWidgetResize","ACTION_MOVE>viewSpanY: "+viewSpanY);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                if(viewSpanX<1){
                    viewSpanX = 0;
                }else if(viewSpanX>maxWidgetSpanX){
                    viewSpanX = maxWidgetSpanX;
                }
                if(viewSpanY<1){
                    viewSpanY = 1;
                }else if(viewSpanY>maxWidgetSpanY){
                    viewSpanY = maxWidgetSpanY;
                }

                if(isSlotAvailable(v.getId(),viewX,viewY,viewSpanX,viewSpanY)){
                    int finalX = C.getSpanToPxX(context,viewX);
                    int finalY = C.getSpanToPxY(context,viewY);
                    int finalW = (intLayoutWidth/maxWidgetSpanX)*viewSpanX;//c.getLayoutWidgetSpanSize(context,newSpanX);
                    int finalH = C.getSpanToPxY(context,viewSpanY);
                    v.setX(finalX);
                    v.setY(finalY);
                    v.setLayoutParams(new RelativeLayout.LayoutParams(finalW,finalH));
                    ((LockerWidgetLayout)v).setSpanX(viewSpanX);
                    ((LockerWidgetLayout)v).setSpanY(viewSpanY);
                    updateWidgetSize(intWidgetProfile,v.getId(),viewX,viewY,viewSpanX,viewSpanY);

                    if(v instanceof LockerAppWidgetHostView){//apps widget id > 0
                        updateWidget((LockerAppWidgetHostView)v,viewSpanX,viewSpanY);
                    }
                }else{
                    v.setX(vLeft);
                    v.setY(vTop);
                    v.setLayoutParams(new RelativeLayout.LayoutParams(vWidth,vHeight));
                    Toast.makeText(context,context.getString(R.string.settings_widget_size_over),Toast.LENGTH_SHORT).show();
                }

                context.sendBroadcast(new Intent(context.getPackageName()+C.SCROLLING_ENABLE));
                break;
        }
    }

    private AvailableSlot getAvailableSlot(int spanX,int spanY){
        int maxSpanX = C.getMaxWidgetSpanX(context);
        int maxSpanY = C.getMaxWidgetSpanY(context);

        final ArrayList<InfoWidget> widgetList = getWidgetList(intWidgetProfile);
        if(widgetList!=null && widgetList.size()>0){
            for(int y = 0; y<maxSpanY; y++){
                for(int x = 0; x<maxSpanX; x++){
                    if(isSlotAvailable(AppWidgetManager.INVALID_APPWIDGET_ID,x,y,spanX,spanY)){
                        return new AvailableSlot(x,y);
                    }
                }
            }
        }else{
            return new AvailableSlot(0,0);
        }
        Log.e("getAvailableSlot","no more slot");
        return new AvailableSlot(SLOT_FULL,SLOT_FULL);//no more slot
    }

    private boolean isSlotAvailable(int widgetId,int x,int y,int spanX,int spanY){
        int maxSpanX = C.getMaxWidgetSpanX(context);
        int maxSpanY = C.getMaxWidgetSpanY(context);

        int endWidthX = x+spanX;
        int endWidthY = y+spanY;

        if(endWidthX>maxSpanX){//x size over
            //Log.e("isWidgetSizeAllowed","x size over");
            return false;
        }
        if(endWidthY>maxSpanY){//y size over
            //Log.e("isWidgetSizeAllowed","y size over");
            return false;
        }

        //Log.e("isWidgetSizeAllowed","checking============="+
        //x+"/"+y+"/"+
        //endWidthX+"/"+endWidthY);
        final ArrayList<InfoWidget> widgetList = getWidgetList(intWidgetProfile);
        for(int iX = x; iX<endWidthX; iX++){
            for(int iY = y; iY<endWidthY; iY++){
                for(InfoWidget item : widgetList){
                    int endX = item.getWidgetX()+item.getWidgetSpanX();
                    int endY = item.getWidgetY()+item.getWidgetSpanY();
                    boolean isWithinX = item.getWidgetX()<=iX && iX<endX;
                    boolean isWithinY = item.getWidgetY()<=iY && iY<endY;

                    if(isWithinX && isWithinY){
                        boolean isDifferentId = widgetId!=item.getWidgetId();
                        boolean isInvalidId = widgetId==AppWidgetManager.INVALID_APPWIDGET_ID;
                        if(isDifferentId || isInvalidId){
                            //Log.e("isWidgetSizeAllowed 1","clashed with another widget");
                            //Log.e("isWidgetSizeAllowed 2","x: "+
                            //item.getWidgetX()+"<"+iX+"/"+isWithinX+">"+endY+" /y:  "+
                            //item.getWidgetY()+"<"+iY+"/"+isWithinY+">"+endY);
                            return false;
                        }
                    }
                }
            }
        }
        Log.e("isWidgetSizeAllowed","final check ok");
        return true;
    }

    private Intent getSettingsIntent(int id){
        switch(id){
            case C.CLOCK_ANALOG_CENTER:
                return new Intent(context,SettingsClockAnalog.class);
            case C.CLOCK_DIGITAL_CENTER:
                return new Intent(context,SettingsClockDigital.class);
            case C.CLOCK_OWNER_MSG:
                return new Intent(context,SettingsPersonalMsg.class);
            case C.CLOCK_EVENT_LIST:
                return new Intent(context,SettingsCalendar.class);
            case C.CLOCK_RSS:
                return new Intent(context,SettingsRSS.class);
        }
        return null;
    }

    public interface WidgetSetupListener{
        void onWidgetSetupFinished(int result,int widgetId);

        void onWidgetDeleteAll();
    }

    private class AvailableSlot{
        int x = 0;
        int y = 0;

        public AvailableSlot(int x,int y){
            super();
            this.x = x;
            this.y = y;
        }

        public int getX(){
            return x;
        }

        public int getY(){
            return y;
        }
    }
}