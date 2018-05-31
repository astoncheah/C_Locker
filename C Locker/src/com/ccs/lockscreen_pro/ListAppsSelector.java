package com.ccs.lockscreen_pro;

import android.app.ProgressDialog;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.MyWidgetHelper;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListAppsSelector extends BaseActivity{
    //=====
    private SharedPreferences.Editor editor;
    private NoticeAdapter adapter = null;
    private LoaderManager loaderManager;
    private static DataAppsSelection db;
    private static List<InfoAppsSelection> appSelectionList = new ArrayList<>();
    private ListView list;
    private Button btnCheckAll, btnUncheckAll;
    private static ProgressDialog progressBar;
    private static int listType;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.list_apps_select);
        setBasicBackKeyAction();

        final Bundle extras = getIntent().getExtras();
        listType = extras.getInt(C.LIST_APPS_ID);

        try{
            myAlertDialog = new MyAlertDialog(this);

            list = (ListView)findViewById(R.id.listSelector);
            btnCheckAll = (Button)findViewById(R.id.btnCalendarCheckAll);
            btnUncheckAll = (Button)findViewById(R.id.btnCalendarUncheckAll);

            onClickFunction();

            loaderManager = getSupportLoaderManager();
            db = new DataAppsSelection(this);
            prepareLoading();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            saveSettings();
            saveBlockApps();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
            if(db!=null){
                db.close();
                db = null;
            }
            if(myAlertDialog!=null){
                myAlertDialog.close();
            }
            if(appSelectionList!=null){
                appSelectionList.clear();
            }
            if(loaderManager!=null){
                loaderManager.destroyLoader(1);
            }
            //adapter.clearObjects();
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.list_cld_account_main;
    }

    private void prepareLoading(){
        loaderManager.restartLoader(1, null, new LoaderManager.LoaderCallbacks<Map<String,Drawable>>(){
            @Override
            public Loader<Map<String,Drawable>> onCreateLoader(int id, Bundle args) {
                try{
                    progressBar = new ProgressDialog(ListAppsSelector.this);
                    progressBar.setCancelable(false);
                    progressBar.setMessage(getString(R.string.collecting_all_apps_info)+"...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    //progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressBar.show();
                }catch(Exception e){
                    saveErrorLogs(null,e);
                }
                return new LoadCustomAppListTask(ListAppsSelector.this);
            }

            @Override
            public void onLoadFinished(Loader<Map<String,Drawable>> loader, Map<String,Drawable> icons) {
                progressBar.dismiss();
                adapter = new NoticeAdapter(ListAppsSelector.this,R.layout.list_notice_apps,appSelectionList,icons);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                        //NOTHING
                    }
                });
            }

            @Override
            public void onLoaderReset(Loader<Map<String,Drawable>> loader) {

            }
        }).forceLoad();
    }
    private void saveSettings(){
        if(appSelectionList!=null){
            db.deleteAllApps(listType);
            for(int i = 0; i<appSelectionList.size(); i++){
                final InfoAppsSelection item = appSelectionList.get(i);
                if(item.isSelected()){
                    final InfoAppsSelection app = new InfoAppsSelection();
                    app.setAppType(listType);
                    app.setAppPkg(item.getAppPkg());
                    app.setAppClass(item.getAppClass());
                    app.setAppName(item.getAppName());
                    db.addApp(app);
                }
            }
        }
    }

    private void saveBlockApps(){
        if(listType==DataAppsSelection.APP_TYPE_BLOCKED_APPS){
            editor = getSharedPreferences(C.PREFS_NAME,0).edit();
            final List<InfoAppsSelection> list = db.getApps(listType);
            if(list!=null && list.size()>0){
                editor.putBoolean("cBoxBlockNoneShortcutApps",true);
                editor.commit();
                return;
            }
            editor.putBoolean("cBoxBlockNoneShortcutApps",false);
            editor.commit();
        }
    }

    private void onClickFunction(){
        btnCheckAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                for(int pos = 0; pos<appSelectionList.size(); pos++){
                    appSelectionList.get(pos).setSelected(true);
                }
                adapter.notifyDataSetChanged();
            }
        });
        btnUncheckAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                for(int pos = 0; pos<appSelectionList.size(); pos++){
                    appSelectionList.get(pos).setSelected(false);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private static class ViewHolder{
        private ImageView imgNoticeApp;
        private TextView txtNoticeAppName;
        private CheckBox cBoxEnableNoticeApp;
    }

    private class NoticeAdapter extends ArrayAdapter<InfoAppsSelection>{
        private ViewHolder holder;
        private int layoutId;
        private Map<String,Drawable> mIcons;

        NoticeAdapter(Context context,int layoutId,List<InfoAppsSelection> apps,
                Map<String,Drawable> icons){
            super(context,layoutId,apps);
            this.layoutId = layoutId;
            this.mIcons = icons;
        }

        @NonNull
        @Override
        public View getView(final int position,View convertView,@NonNull ViewGroup parent){
            try{
                if(convertView==null){
                    convertView = newView(parent);
                    final ViewHolder viewHolder = new ViewHolder();
                    viewHolder.imgNoticeApp = convertView.findViewById(R.id.imgNoticeApp);
                    viewHolder.txtNoticeAppName = convertView.findViewById(R.id.txtNoticeAppName);
                    viewHolder.cBoxEnableNoticeApp = convertView.findViewById(R.id.cBoxEnableNoticeApp);

                    viewHolder.txtNoticeAppName.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v){
                            viewHolder.cBoxEnableNoticeApp.performClick();
                        }
                    });
                    viewHolder.cBoxEnableNoticeApp.setOnClickListener(new OnClickListener(){
                        @Override
                        public void onClick(View v){
                            InfoAppsSelection element = (InfoAppsSelection)viewHolder.cBoxEnableNoticeApp.getTag();
                            element.setSelected(viewHolder.cBoxEnableNoticeApp.isChecked());
                        }
                    });
                    convertView.setTag(viewHolder);
                    viewHolder.cBoxEnableNoticeApp.setTag(appSelectionList.get(position));
                }

                holder = (ViewHolder)convertView.getTag();
                holder.txtNoticeAppName.setText(appSelectionList.get(position).getAppName());//app name
                holder.cBoxEnableNoticeApp.setChecked(appSelectionList.get(position).isSelected());
                holder.cBoxEnableNoticeApp.setTag(appSelectionList.get(position));
                if(mIcons==null){
                    holder.imgNoticeApp.setImageResource(R.drawable.s4_weather_error);
                }else{
                    holder.imgNoticeApp.setImageDrawable(mIcons.get("appIcon"+position));
                }
                //Log.e("getView",position+"/"+cldList.get(position).getId()+"/"+cldList.get(position).isSelected());
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
            return convertView;
        }

        private View newView(ViewGroup parent){
            return (getLayoutInflater().inflate(layoutId,parent,false));
        }
    }

    private static class LoadCustomAppListTask extends AsyncTaskLoader<Map<String,Drawable>>{
        private ListAppsSelector activity;
        private PackageManager pm;
        private WeakReference<ListAppsSelector> mActivity;

        private LoadCustomAppListTask(ListAppsSelector activity){
            super(activity);
            this.activity = activity;
            mActivity = new WeakReference<>(activity);
            this.pm = activity.getPackageManager();
        }

        @Override
        public Map<String,Drawable> loadInBackground(){
            final Map<String,Drawable> mapApplist = new HashMap<>();
            if(listType==DataAppsSelection.APP_TYPE_BLOCKED_NOTICE_APPS){//blacklist notice apps
                final List<ResolveInfo> list = getInstalledApps();
                progressBar.setMax(list.size());
                int iconIndex = 0;
                for(int i = 0; i<list.size(); i++){
                    try{
                        final ResolveInfo entry = list.get(i);
                        final InfoAppsSelection app = new InfoAppsSelection();
                        app.setAppType(listType);
                        app.setAppPkg(entry.activityInfo.packageName);
                        app.setAppClass(entry.activityInfo.applicationInfo.className);
                        app.setAppName(entry.activityInfo.loadLabel(pm).toString());
                        appSelectionList.add(app);

                        mapApplist.put("appIcon"+iconIndex,entry.loadIcon(pm));
                    }catch(OutOfMemoryError | Exception e){
                        mapApplist.put("appIcon"+iconIndex,ContextCompat.getDrawable(mActivity.get(),R.drawable.s4_weather_error));
                    }
                    this.onProgressUpdate(i);
                    iconIndex++;
                }
                List<InfoAppsSelection> blockSystemNoticeApps = getBlockedSystemNoticeApps(list);
                for(int i = 0; i<blockSystemNoticeApps.size(); i++){
                    try{
                        final InfoAppsSelection entry = blockSystemNoticeApps.get(i);
                        final InfoAppsSelection app = new InfoAppsSelection();
                        app.setAppType(listType);
                        app.setAppPkg(entry.getAppPkg());
                        app.setAppClass(entry.getAppClass());
                        app.setAppName("(System)"+getAppName(entry.getAppPkg()));
                        appSelectionList.add(app);

                        mapApplist.put("appIcon"+iconIndex,getIcon(entry.getAppPkg()));
                    }catch(OutOfMemoryError | Exception e){
                        mapApplist.put("appIcon"+iconIndex,ContextCompat.getDrawable(mActivity.get(),R.drawable.s4_weather_error));
                    }
                    this.onProgressUpdate(i);
                    iconIndex++;
                }
            }else if(listType==DataAppsSelection.APP_TYPE_WIDGET_BLOCK){//blacklist notice apps
                List<AppWidgetProviderInfo> listWidget = new MyWidgetHelper(activity).getInstalledProviders();
                progressBar.setMax(listWidget.size());
                for(int i = 0; i<listWidget.size(); i++){
                    try{
                        final AppWidgetProviderInfo widget = listWidget.get(i);
                        final InfoAppsSelection app = new InfoAppsSelection();
                        app.setAppType(listType);
                        app.setAppPkg(widget.provider.getPackageName());
                        app.setAppClass(widget.provider.getClassName());//no use
                        app.setAppName(widget.label);//no use
                        appSelectionList.add(app);

                        mapApplist.put("appIcon"+i,getIcon(widget.provider.getPackageName()));
                    }catch(OutOfMemoryError | Exception e){
                        mapApplist.put("appIcon"+i,ContextCompat.getDrawable(mActivity.get(),R.drawable.s4_weather_error));
                    }
                    this.onProgressUpdate(i);
                }
            }else{
                final List<ResolveInfo> list = getInstalledApps();
                progressBar.setMax(list.size());
                for(int i = 0; i<list.size(); i++){
                    try{
                        final ResolveInfo entry = list.get(i);
                        final InfoAppsSelection app = new InfoAppsSelection();
                        app.setAppType(listType);
                        app.setAppPkg(entry.activityInfo.packageName);
                        app.setAppClass(entry.activityInfo.applicationInfo.className);
                        app.setAppName(entry.activityInfo.loadLabel(pm).toString());
                        appSelectionList.add(app);

                        mapApplist.put("appIcon"+i,entry.loadIcon(pm));
                        //mapApplist.put("appIcon"+i,getResizedDrawable(((BitmapDrawable)entry.loadIcon(pm)).getBitmap(),72,72));
                    }catch(OutOfMemoryError | Exception e){
                        mapApplist.put("appIcon"+i,ContextCompat.getDrawable(mActivity.get(),R.drawable.s4_weather_error));
                    }
                    this.onProgressUpdate(i);
                }
            }
            loadCheckList();
            return mapApplist;
        }
        private void onProgressUpdate(final int values){
            if (mActivity.get() != null) {
                mActivity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.get().progressBar.setProgress(values);
                    }
                });
            }
        }

        private List<ResolveInfo> getInstalledApps(){
            try{
                final Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
                final List<ResolveInfo> appList = pm.queryIntentActivities(i,0);
                Collections.sort(appList,new ResolveInfo.DisplayNameComparator(pm));
                return appList;
            }catch(Exception e){
                //saveErrorLogs(null,e);
            }
            return null;
        }

        private List<InfoAppsSelection> getBlockedSystemNoticeApps(List<ResolveInfo> list){
            final List<InfoAppsSelection> saveApps = db.getApps(listType);
            final List<InfoAppsSelection> blockSystemNoticeApps = new ArrayList<>();
            for(InfoAppsSelection item : saveApps){
                try{
                    if(isSystemApp(list,item.getAppPkg())){
                        blockSystemNoticeApps.add(item);
                    }
                }catch(Exception e){
                    //saveErrorLogs(null,e);
                }
            }
            return blockSystemNoticeApps;
        }

        private String getAppName(String pkg){
            try{
                final ApplicationInfo applicationInfo = pm.getApplicationInfo(pkg,0);
                return (String)pm.getApplicationLabel(applicationInfo);
            }catch(final NameNotFoundException e){
                e.printStackTrace();
            }
            return pkg;
        }

        private Drawable getIcon(String pkg){
            try{
                return pm.getApplicationIcon(pkg);
            }catch(final NameNotFoundException e){
                e.printStackTrace();
            }
            return null;
        }

        private void loadCheckList(){
            try{
                if(listType==DataAppsSelection.APP_TYPE_BLOCKED_NOTICE_APPS){//block app
                    final List<InfoAppsSelection> saveApps = db.getApps(listType);
                    for(InfoAppsSelection app : saveApps){
                        for(InfoAppsSelection item : appSelectionList){
                            try{
                                boolean isSamePkg = item.getAppPkg().equals(app.getAppPkg());
                                if(isSamePkg){
                                    item.setSelected(true);
                                }
                            }catch(Exception e){
                                //saveErrorLogs(null,e);
                            }
                        }
                    }
                }else{
                    final List<InfoAppsSelection> saveApps = db.getApps(listType);
                    for(InfoAppsSelection app : saveApps){
                        for(InfoAppsSelection item : appSelectionList){
                            try{
                                boolean isSamePkg = item.getAppPkg().equals(app.getAppPkg());
                                boolean isSameAppName = item.getAppName().equals(app.getAppName());
                                if(isSamePkg && isSameAppName){
                                    //Log.e("loadCheckList",item.getAppName()+"/"+item.getAppPkg());
                                    item.setSelected(true);
                                }
                            }catch(Exception e){
                                //saveErrorLogs(null,e);
                            }
                        }
                    }
                }
            }catch(Exception e){
                //saveErrorLogs(null,e);
            }
        }

        private boolean isSystemApp(List<ResolveInfo> list,String pkg){
            for(ResolveInfo item : list){
                if(item.activityInfo.packageName.equals(pkg)){
                    return false;
                }
            }
            return true;
        }
    }
}