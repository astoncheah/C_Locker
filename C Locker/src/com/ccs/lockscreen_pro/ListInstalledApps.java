package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListInstalledApps extends BaseActivity{
    private static int appsId;
    private AppAdapter adapter = null;
    private SharedPreferences.Editor editor;
    private static ProgressDialog progressBar;
    private ListView list;
    private static List<String[]> mFilteredList = new ArrayList<>();
    private static List<ResolveInfo> resolveInfoList;
    private LoaderManager loaderManager;
    private int profile;
    private MyCLocker mLocker;


    @Override
    protected int getLayoutResource(){
        return R.layout.list_apps_main;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.list_apps_select);
        setBasicBackKeyAction();

        final Bundle extras = getIntent().getExtras();
        this.appsId = extras.getInt(C.LIST_APPS_ID);
        if(extras.containsKey(C.LIST_APPS_PROFILE)){
            this.profile = extras.getInt(C.LIST_APPS_PROFILE);
        }

        try{
            list = (ListView)findViewById(R.id.list_select_app);
            mLocker = new MyCLocker(this);
            loaderManager = getSupportLoaderManager();
            prepareLoading();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        Intent i = new Intent();
        i.putExtra(C.LIST_APPS_ID,appsId);
        setResult(Activity.RESULT_CANCELED,i);
        return super.onKeyDown(keyCode,event);
    }

    protected void onListItemClick(AdapterView<?> listView,View v,int position,long id){
        final ActivityInfo activity = resolveInfoList.get(position).activityInfo;
        final String[] str = mFilteredList.get(position);
        final String packageName = str[0];
        final String className = str[1];
        final String appName = str[2];
        final PackageManager pm = getPackageManager();
        Drawable d;
        try{
            d = activity.loadIcon(pm);
        }catch(Exception e){
            d = ContextCompat.getDrawable(getBaseContext(),R.drawable.s4_weather_error);
            mLocker.saveErrorLog(null,e);
        }
        final Bitmap appNameIcon;

        Bitmap bm;
        if(d instanceof BitmapDrawable){
            appNameIcon = ((BitmapDrawable)d).getBitmap();
        }else{
            appNameIcon = Bitmap.createBitmap(d.getIntrinsicWidth(),d.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(appNameIcon);
            d.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
            d.draw(canvas);
        }

        editor = getSharedPreferences(C.PREFS_NAME,0).edit();
        switch(appsId){
            case C.LIST_SEARCH_LONG_PRESS:
                if(packageName.contains(C.PKG_NAME_FREE)){
                    Toast.makeText(this,getString(R.string.select_long_press_app_desc),Toast.LENGTH_LONG).show();
                    return;
                }
                editor.putString(P.STR_SEARCH_LONG_PRESS_PKG_NAME,packageName);
                editor.putString(P.STR_SEARCH_LONG_PRESS_CLASS_NAME,className);
                editor.commit();
                break;
            case C.LIST_LAUNCHER:
                if(packageName.contains(C.PKG_NAME_FREE)){
                    Toast.makeText(this,getString(R.string.select_launcher_app_desc),Toast.LENGTH_LONG).show();
                    return;
                }
                editor.putString(P.STR_HOME_LAUNCHER_PKG_NAME,packageName);
                editor.putString(P.STR_HOME_LAUNCHER_CLASS_NAME,className);
                editor.commit();
                break;
            case C.LIST_LAUNCHER_RUN:
                if(packageName.contains(C.PKG_NAME_FREE)){
                    Toast.makeText(this,getString(R.string.select_launcher_app_desc),Toast.LENGTH_LONG).show();
                    return;
                }
                editor.putString(P.STR_HOME_LAUNCHER_PKG_NAME,packageName);
                editor.putString(P.STR_HOME_LAUNCHER_CLASS_NAME,className);
                editor.commit();

                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setClassName(packageName,className);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);

                finish();
                break;
            case C.LIST_SETTINGS:// no use
                if(packageName.contains(C.PKG_NAME_FREE)){
                    Toast.makeText(this,"Please select System Settings",Toast.LENGTH_LONG).show();
                    return;
                }
                editor.putString(P.STR_SETTINGS_PKG_NAME,packageName);
                editor.putString(P.STR_SETTINGS_CLASS_NAME,className);
                editor.commit();
                break;
            case DataAppsSelection.SHORTCUT_APP_01://SHORTCUT_APP_01
            case DataAppsSelection.SHORTCUT_APP_02://SHORTCUT_APP_02
            case DataAppsSelection.SHORTCUT_APP_03://SHORTCUT_APP_03
            case DataAppsSelection.SHORTCUT_APP_04://SHORTCUT_APP_04
            case DataAppsSelection.SHORTCUT_APP_05://SHORTCUT_APP_05
            case DataAppsSelection.SHORTCUT_APP_06://SHORTCUT_APP_06
            case DataAppsSelection.SHORTCUT_APP_07://SHORTCUT_APP_07
            case DataAppsSelection.SHORTCUT_APP_08://SHORTCUT_APP_08
            case DataAppsSelection.VOLUME_UP://VOLUME_UP
            case DataAppsSelection.VOLUME_DOWN://VOLUME_DOWN
            case DataAppsSelection.GESTURE_UP://GESTURE_UP
            case DataAppsSelection.GESTURE_DOWN://GESTURE_DOWN
            case DataAppsSelection.GESTURE_LEFT://GESTURE_LEFT
            case DataAppsSelection.GESTURE_RIGHT://GESTURE_RIGHT
            case DataAppsSelection.GESTURE_UP_2://
            case DataAppsSelection.GESTURE_DOWN_2://
            case DataAppsSelection.GESTURE_LEFT_2://
            case DataAppsSelection.GESTURE_RIGHT_2://
            case DataAppsSelection.DOUBLE_TAP1:
            case DataAppsSelection.DOUBLE_TAP2:
            case DataAppsSelection.TRIPLE_TAP1:
            case DataAppsSelection.TRIPLE_TAP2:
                saveShortcut(packageName,className,appName);
                break;
        }

        Intent i = new Intent();
        i.putExtra("appName",appName);
        i.putExtra(C.LIST_APPS_ID,appsId);
        i.putExtra("appNameIcon",getResizedBitmap(appNameIcon,144,144));//default 144
        setResult(Activity.RESULT_OK,i);
        finish();
    }

    @Override
    protected void onDestroy(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
            if(mFilteredList!=null){
                mFilteredList.clear();
            }
            if(resolveInfoList!=null){
                resolveInfoList.clear();
            }
            if(loaderManager!=null){
                loaderManager.destroyLoader(1);
            }
            adapter.clearObjects();
            mLocker.close();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        super.onDestroy();
    }

    private void prepareLoading(){
        loaderManager.restartLoader(1, null, new LoaderManager.LoaderCallbacks<Map<String,Drawable>>(){
            @Override
            public Loader<Map<String,Drawable>> onCreateLoader(int id, Bundle args) {
                try{
                    progressBar = new ProgressDialog(ListInstalledApps.this);
                    progressBar.setCancelable(false);
                    progressBar.setMessage(getString(R.string.collecting_all_apps_info)+"...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    //progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressBar.show();
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
                return new LoadIconsTask(ListInstalledApps.this);
            }

            @Override
            public void onLoadFinished(Loader<Map<String,Drawable>> loader, Map<String,Drawable> icons) {
                try{
                    progressBar.dismiss();
                    adapter = new AppAdapter(ListInstalledApps.this,R.layout.list_apps,mFilteredList,icons);
                    list.setAdapter(adapter);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> parent,View v,int position,long id){
                            onListItemClick(parent,v,position,id);
                        }
                    });
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }

            @Override
            public void onLoaderReset(Loader<Map<String,Drawable>> loader) {

            }
        }).forceLoad();
    }
    private void saveShortcut(String pkgName,String className,String appName){
        final InfoAppsSelection app = new InfoAppsSelection();
        app.setAppType(DataAppsSelection.APP_TYPE_SHORTCUT);
        app.setShortcutProfile(profile);
        app.setShortcutId(appsId);
        app.setShortcutAction(DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP);
        app.setAppPkg(pkgName);
        app.setAppClass(className);
        app.setAppName(appName);

        final DataAppsSelection db = new DataAppsSelection(this);
        final List<InfoAppsSelection> appsList = db.getShortcutApps(appsId,profile);
        if(appsList.isEmpty()){
            db.addApp(app);
        }else{
            db.updateApp(app);
        }
        db.close();
    }

    private Bitmap getResizedBitmap(Bitmap bm,int newHeight,int newWidth){
        final int width = bm.getWidth();
        final int height = bm.getHeight();
        final float scaleWidth = ((float)newWidth)/width;
        final float scaleHeight = ((float)newHeight)/height;
        final Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        return Bitmap.createBitmap(bm,0,0,width,height,matrix,false);
    }

    private class ViewHolder{
        public TextView label;
        public ImageView icon;
    }

    private class AppAdapter extends ArrayAdapter<String[]>{
        private ViewHolder holder;
        private int layoutId;
        private Map<String,Drawable> mIcons;

        private AppAdapter(Context context,int layoutId,List<String[]> apps,
                Map<String,Drawable> icons){
            super(context,layoutId,apps);
            this.layoutId = layoutId;
            this.mIcons = icons;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View view = null;
            final String[] str = mFilteredList.get(position);
            try{
                if(convertView==null){
                    view = newView(parent);
                    ViewHolder viewHolder = new ViewHolder();
                    viewHolder.label = view.findViewById(R.id.label);
                    viewHolder.icon = view.findViewById(R.id.icon);
                    view.setTag(viewHolder);
                }else{
                    view = convertView;
                }

                holder = (ViewHolder)view.getTag();
                holder.label.setText(str[2]);//app name

                if(mIcons==null){
                    holder.icon.setImageResource(R.drawable.s4_weather_error);
                }else{
                    holder.icon.setImageDrawable(mIcons.get("MyAppName"+position));
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return (view);
        }

        private View newView(ViewGroup parent){
            return (getLayoutInflater().inflate(layoutId,parent,false));
        }

        private void clearObjects(){
            if(mIcons!=null){
                mIcons.clear();
                mIcons = null;
            }
            clear();
        }
    }

    private static class LoadIconsTask extends AsyncTaskLoader<Map<String,Drawable>>{
        private PackageManager pm;
        private WeakReference<ListInstalledApps> mActivity;

        private LoadIconsTask(ListInstalledApps activity){
            super(activity);
            mActivity = new WeakReference<>(activity);
            this.pm = activity.getPackageManager();
        }

        @Override
        public Map<String,Drawable> loadInBackground(){
            final List<ResolveInfo> list = loadData();
            final Map<String,Drawable> icons = new HashMap<>();
            progressBar.setMax(list.size());
            for(int i = 0; i<list.size(); i++){
                try{
                    final ResolveInfo entry = list.get(i);
                    mFilteredList.add(new String[]{entry.activityInfo.packageName,entry.activityInfo.name,entry.loadLabel(pm).toString()});
                    //Log.e("packageName",entry.activityInfo.packageName+"/"+entry.activityInfo.name+"/"+entry.loadLabel(pm).toString());
                    icons.put("MyAppName"+i,entry.loadIcon(pm));//.getBiTmap
                }catch(OutOfMemoryError | Exception e){
                    icons.put("MyAppName"+i,ContextCompat.getDrawable(mActivity.get(),R.drawable.s4_weather_error));
                }
                this.onProgressUpdate(i);
            }
            return icons;
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

        private List<ResolveInfo> loadData(){
            Intent main;
            if(appsId==C.LIST_SEARCH_LONG_PRESS){
                main = new Intent(Intent.ACTION_ASSIST);
                main.addCategory(Intent.CATEGORY_DEFAULT);
            }else if(appsId==C.LIST_LAUNCHER || appsId==C.LIST_LAUNCHER_RUN){
                main = new Intent(Intent.ACTION_MAIN);
                main.addCategory(Intent.CATEGORY_HOME);
            }else if(appsId==C.LIST_SETTINGS){
                main = new Intent(Settings.ACTION_SETTINGS);
                main.addCategory(Intent.CATEGORY_DEFAULT);
            }else{
                main = new Intent(Intent.ACTION_MAIN);
                main.addCategory(Intent.CATEGORY_LAUNCHER);
            }
            try{
                resolveInfoList = pm.queryIntentActivities(main,0);//Log.e("info",resolveInfoList.size()+"");
                Collections.sort(resolveInfoList,new ResolveInfo.DisplayNameComparator(pm));
            }catch(Exception e){
                e.printStackTrace();
            }
            return resolveInfoList;
        }
    }
}