package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.MyWidgetHelper;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListWidgetApps extends BaseActivity{
    private AppAdapter adapter = null;
    private ProgressDialog progressBar;
    private MyWidgetHelper mWidgetHelper;
    private List<String[]> mFilteredList;
    private List<AppWidgetProviderInfo> listwidget;
    private ListView list;
    private int widgetType;

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
        widgetType = extras.getInt(C.LIST_APP_WIDGET_TYPE);

        try{
            mFilteredList = new ArrayList<>();
            mWidgetHelper = new MyWidgetHelper(this);
            list = (ListView)findViewById(R.id.list_select_app);
            listwidget = mWidgetHelper.getInstalledProviders();
            new LoadIconsTask(this).execute();
        }catch(Exception e){
            new MyCLocker(this).saveErrorLog(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        Intent i = new Intent();
        setResult(Activity.RESULT_CANCELED,i);
        return super.onKeyDown(keyCode,event);
    }

    protected void onListItemClick(int position){
        final String[] str = mFilteredList.get(position);
        final String pkgName = str[1];
        final String className = str[2];
        int appWidgetId = mWidgetHelper.allocateAppWidgetId();
        final Intent i = new Intent();
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        i.putExtra("strGetWidgetPkgName",pkgName);
        i.putExtra("strGetWidgetClassName",className);
        setResult(Activity.RESULT_OK,i);
        finish();
    }

    @Override
    protected void onDestroy(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
            mWidgetHelper.closeAll();
            adapter.clearObjects();
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
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

        @NonNull
        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View view = null;
            final String[] str = mFilteredList.get(position);
            try{
                if(convertView==null){
                    view = newView(parent);
                    ViewHolder viewHolder = new ViewHolder();
                    viewHolder.label = (TextView)view.findViewById(R.id.label);
                    viewHolder.icon = (ImageView)view.findViewById(R.id.icon);
                    view.setTag(viewHolder);
                }else{
                    view = convertView;
                }

                holder = (ViewHolder)view.getTag();
                holder.label.setText(str[3]);//app name

                if(mIcons==null){
                    holder.icon.setImageResource(R.drawable.s4_weather_error);
                }else{
                    holder.icon.setImageDrawable(mIcons.get("MyAppName"+position));
                }
            }catch(Exception e){
                new MyCLocker(ListWidgetApps.this).saveErrorLog(null,e);
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

    private class LoadIconsTask extends AsyncTask<Void,Integer,Map<String,Drawable>>{
        private Context context;
        private PackageManager pm;

        private LoadIconsTask(Context context){
            this.context = context;
            this.pm = getPackageManager();
        }

        @SuppressWarnings("deprecation")
        @SuppressLint("NewApi")
        @Override
        protected Map<String,Drawable> doInBackground(Void... apps){
            final Map<String,Drawable> icons = new HashMap<>();
            progressBar.setMax(listwidget.size());
            AppWidgetProviderInfo widget;
            int iconIndex = 0;
            for(int i = 0; i<listwidget.size(); i++){
                try{
                    widget = listwidget.get(i);
                    if(widgetType==C.LIST_ALL_APPS_WIDGETS){
                        icons.put("MyAppName"+iconIndex,pm.getApplicationIcon(widget.provider.getPackageName()));
                        String label;
                        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                            label = widget.loadLabel(pm);
                        }else{
                            label = widget.label;
                        }
                        mFilteredList.add(new String[]{String.valueOf(i),widget.provider.getPackageName(),widget.provider.getClassName(),label});
                        iconIndex++;
                    }
                    this.publishProgress(i);
                }catch(OutOfMemoryError e){
                    icons.put("MyAppName"+i,getResources().getDrawable(R.drawable.s4_weather_error));
                }catch(NameNotFoundException e){
                    new MyCLocker(ListWidgetApps.this).saveErrorLog(null,e);
                }
            }
            return icons;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            try{
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage(getString(R.string.collecting_all_apps_info)+"...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                //progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();

                Collections.sort(listwidget,new Comparator<AppWidgetProviderInfo>(){
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public int compare(AppWidgetProviderInfo arg0,AppWidgetProviderInfo arg1){
                        String comA;
                        String comB;
                        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                            comA = arg0.loadLabel(pm);
                            comB = arg1.loadLabel(pm);
                        }else{
                            comA = arg0.label;
                            comB = arg1.label;
                        }
                        return comA.compareTo(comB);
                    }
                });
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }

        @Override
        protected void onPostExecute(Map<String,Drawable> icons){
            try{
                progressBar.dismiss();
                adapter = new AppAdapter(context,R.layout.list_apps,mFilteredList,icons);
                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent,View v,int position,long id){
                        onListItemClick(position);
                    }
                });
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values){
            progressBar.setProgress(values[0]);
            super.onProgressUpdate(values);
        }
    }
}