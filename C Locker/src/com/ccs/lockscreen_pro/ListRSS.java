package com.ccs.lockscreen_pro;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataRSS;
import com.ccs.lockscreen.data.DownloadRSS;
import com.ccs.lockscreen.data.InfoRSS;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ListRSS extends BaseActivity{
    //=====
    private SharedPreferences prefs;
    private int RSS_SETTINGS = 1;
    private AppAdapter adapter = null;
    private DataRSS db;
    private ArrayList<InfoRSS> rssList = new ArrayList<InfoRSS>();
    private ProgressDialog progressBar;
    private ListView listRss;
    private TextView txtTitle, txtNextUpdate, txtLastUpdate;
    private int intSpnUpdateInterval;
    private String currentTime, nextUpdateDate;
    private DateFormat tf = new SimpleDateFormat("h:mma"+", "+"EEE, d MMM yy",Locale.getDefault());
    //
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(getPackageName()+C.UPDATE_RSS)){
                    loadData();
                    loadSettings();
                }
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_widget_rss_list);
        setBasicBackKeyAction();

        listRss = (ListView)findViewById(R.id.listRss);
        txtTitle = (TextView)findViewById(R.id.txt_rssTotalCount);
        txtNextUpdate = (TextView)findViewById(R.id.txt_rssNextUpdate);
        txtLastUpdate = (TextView)findViewById(R.id.txt_rssLastUpdate);

        try{
            loadReceiver(true);
            loadData();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(getPackageName()+C.UPDATE_RSS);
                registerReceiver(mReceiver,intentFilter);
            }else{
                unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void loadData(){
        try{
            db = new DataRSS(this);
            if(db.getRSS(1)==null){
                if(isInternetConnected()){
                    new DownloadRSS(this,true).execute();
                }else{
                    Toast.makeText(this,"Internet connection errors!",Toast.LENGTH_LONG).show();
                }
            }else{
                InfoRSS rss = db.getRSS(1);
                rssList = (ArrayList<InfoRSS>)db.getAllRSS();

                setTitle(rss.getRssSource());
                txtTitle.setText(getString(R.string.total_news)+": "+db.getRSSCount());

                adapter = new AppAdapter(this,R.layout.list_fragment_rss,rssList);
                listRss.setAdapter(adapter);
                listRss.setOnItemClickListener(new OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                        final InfoRSS rss = rssList.get(position);
                        final Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(rss.getRssLink()));
                        startActivity(i);
                    }
                });
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        if(db!=null){
            db.close();
            db = null;
        }
    }

    private boolean isInternetConnected(){
        try{
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(netInfo!=null && netInfo.isConnected()){
                return true;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        return false;
    }

    @Override
    protected void onDestroy(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
            adapter.clearObjects();
            loadReceiver(false);

            Intent i = new Intent(getPackageName()+".REQUEST_CONFIGURE");
            sendBroadcast(i);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        if(db!=null){
            db.close();
            db = null;
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.list_rss_main;
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadSettings();
    }

    private void loadSettings(){
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        intSpnUpdateInterval = prefs.getInt("intSpnUpdateInterval",0);
        if(intSpnUpdateInterval!=0){
            timeConvertion();
            nextUpdateDate = getString(R.string.next_update)+" "+currentTime;
        }else{
            nextUpdateDate = getString(R.string.next_update_never);
        }
        txtNextUpdate.setText(nextUpdateDate);
        txtLastUpdate.setText(prefs.getString("LastUpdateDate","").toString());
    }

    private void timeConvertion(){
        try{
            if(intSpnUpdateInterval!=0){
                Calendar cldRightNow = Calendar.getInstance();
                int hours = cldRightNow.get(Calendar.HOUR_OF_DAY),
                        min = cldRightNow.get(Calendar.MINUTE),
                        sec = cldRightNow.get(Calendar.SECOND),
                        updateHours = intSpnUpdateInterval-(hours%intSpnUpdateInterval);
                Calendar cldRSSUpdate = Calendar.getInstance();
                cldRSSUpdate.setTimeInMillis(System.currentTimeMillis());
                cldRSSUpdate.add(Calendar.HOUR_OF_DAY,updateHours);
                cldRSSUpdate.add(Calendar.MINUTE,-min);
                cldRSSUpdate.add(Calendar.SECOND,-sec);
                currentTime = tf.format(cldRSSUpdate.getTime());
            }
        }catch(Exception e){
            nextUpdateDate = getString(R.string.next_update_error);
            saveErrorLogs(null,e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_rss_menu,menu);
        //Set icon for the menu button
        Drawable icon1 = ContextCompat.getDrawable(getBaseContext(),R.drawable.ic_settings_white_48dp);
        Drawable icon2 = ContextCompat.getDrawable(getBaseContext(),R.drawable.ic_autorenew_white_48dp);

        menu.getItem(0).setIcon(getScaledIcon(icon1,72,72));
        menu.getItem(1).setIcon(getScaledIcon(icon2,72,72));
        return true;
    }

    private Drawable getScaledIcon(Drawable drawable,int dstWidth,int dstHeight){
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap,dstWidth,dstHeight,false);
        return new BitmapDrawable(getResources(),bitmapScaled);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        Intent i;
        switch(item.getItemId()){
            case R.id.item_rssRefresh:
                if(isInternetConnected()){
                    new DownloadRSS(this,true).execute();
                }else{
                    Toast.makeText(this,"Internet connection errors!",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.item_rssSettings:
                i = new Intent(this,SettingsRSS.class);
                startActivityForResult(i,RSS_SETTINGS);
                break;
            default:
                break;
        }
        return true;
    }

    private class ViewHolder{
        private TextView txtTitle;
        private TextView txtPost;
        private TextView txtPubDate;
        private TextView txtDesc;
    }

    private class AppAdapter extends ArrayAdapter<InfoRSS>{
        private ViewHolder holder;
        private int layoutId;

        public AppAdapter(Context context,int layoutId,List<InfoRSS> listRss){
            super(context,layoutId,listRss);
            this.layoutId = layoutId;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            InfoRSS rss = rssList.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtTitle = (TextView)v.findViewById(R.id.txt_rss_title);
                viewHolder.txtPost = (TextView)v.findViewById(R.id.txt_rss_postno);
                viewHolder.txtPubDate = (TextView)v.findViewById(R.id.txt_rss_pubdate);
                viewHolder.txtDesc = (TextView)v.findViewById(R.id.txt_rss_desc);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                holder.txtTitle.setText(rss.getRssTitle());
                holder.txtPost.setText("#"+(position+1));
                holder.txtPubDate.setText("("+rss.getRssPubDate()+")");
                holder.txtDesc.setText(rss.getRssDesc());
                holder.txtDesc.setMaxLines(3);
                holder.txtDesc.setEllipsize(TruncateAt.END);
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            return (getLayoutInflater().inflate(layoutId,parent,false));
        }

        private void clearObjects(){
            clear();
        }
    }
}