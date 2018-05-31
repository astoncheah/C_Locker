package com.ccs.lockscreen.appwidget;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataRSS;
import com.ccs.lockscreen.data.DownloadRSS;
import com.ccs.lockscreen.data.InfoRSS;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.util.ArrayList;

public class MyRssWidget extends LinearLayout{
    private Context context;
    private ArrayList<InfoRSS> rssList;
    private boolean enableScroll;
    private WidgetOnTouchListener cb;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(context.getPackageName()+C.UPDATE_RSS)){
                    loadData();
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }
    };

    public MyRssWidget(final Context context,WidgetOnTouchListener cb){
        super(context);
        this.context = context;
        this.cb = cb;
        if(cb==null){
            enableScroll = false;
        }else{
            enableScroll = true;
        }

        setGravity(Gravity.CENTER);
        setPadding(C.dpToPx(context,40),C.dpToPx(context,5),C.dpToPx(context,40),C.dpToPx(context,5));
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        loadReceiver(true);
        loadData();
    }

    @Override
    protected void onDetachedFromWindow(){
        loadReceiver(false);
        super.onDetachedFromWindow();
    }

    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(context.getPackageName()+C.UPDATE_RSS);
                context.registerReceiver(mReceiver,intentFilter);
            }else{
                context.unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    private void loadData(){
        DataRSS db = null;
        try{
            db = new DataRSS(context);
            rssList = (ArrayList<InfoRSS>)db.getAllRSS();

            if(rssList!=null && rssList.size()>0){
                final RSSAdapter adapter = new RSSAdapter(context,R.layout.list_widget_rss,rssList);
                final ListView listView = new ListView(context);
                listView.setClickable(enableScroll);
                listView.setEnabled(enableScroll);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> arg,View v,int position,long id){
                        try{
                            final InfoRSS rss = rssList.get(position);
                            final Intent i = new Intent(context.getPackageName()+C.PAGER_CLICK_NOTICE);
                            i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_RSS);
                            i.putExtra(C.RSS_LINK,rss.getRssLink());
                            context.sendBroadcast(i);
                        }catch(Exception e){
                            new MyCLocker(context).saveErrorLog(null,e);
                        }
                    }
                });
                MyRssWidget.this.removeAllViews();
                MyRssWidget.this.addView(listView);
            }else{
                if(isInternetConnected()){
                    new DownloadRSS(context,true).execute();
                }else{
                    Toast.makeText(context,"Internet connection errors!",Toast.LENGTH_LONG).show();
                }
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        if(db!=null){
            db.close();
            db = null;
        }
    }

    private boolean isInternetConnected(){
        try{
            final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(netInfo!=null && netInfo.isConnected()){
                return true;
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent m){
        if(cb!=null){
            return cb.onWidgetTouchEvent(this,m,null,null);
        }
        return super.onTouchEvent(m);
    }

    private class RSSAdapter extends ArrayAdapter<InfoRSS>{
        private ViewHolder holder;
        private int layoutId;
        private InfoRSS rss;
        private ArrayList<InfoRSS> list;

        private RSSAdapter(Context context,int layoutId,ArrayList<InfoRSS> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            rss = list.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtTitle = (TextView)v.findViewById(R.id.txt_rss_title);
                viewHolder.txtPost = (TextView)v.findViewById(R.id.txt_rss_postno);
                viewHolder.txtPubDate = (TextView)v.findViewById(R.id.txt_rss_pubdate);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                holder.txtPubDate.setText(cutString(rss.getRssPubDate(),29));
                holder.txtPost.setText("#"+(position+1));
                holder.txtTitle.setText(cutString(rss.getRssTitle(),30));
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return (inflater.inflate(layoutId,parent,false));
        }

        private String cutString(String str,int strLength){
            if(str.length()>strLength){
                return str.substring(0,strLength)+"..";
            }else{
                return str;
            }
        }

        private class ViewHolder{
            private TextView txtTitle;
            private TextView txtPost;
            private TextView txtPubDate;
        }
    }
}
