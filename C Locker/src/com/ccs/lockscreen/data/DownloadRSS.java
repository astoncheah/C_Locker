package com.ccs.lockscreen.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.SaveData;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class DownloadRSS extends AsyncTask<Void,Void,Boolean>{
    private Context context;
    private ProgressDialog progressBar;
    private MyCLocker myCLocker;
    private boolean showProgressBar;

    public DownloadRSS(Context context,boolean showProgressBar){
        this.context = context;
        this.showProgressBar = showProgressBar;
        myCLocker = new MyCLocker(context);
    }

    @Override
    protected Boolean doInBackground(Void... arg0){
        try{
            return readStream();
        }catch(Exception e){
            myCLocker.saveErrorLog(null,e);
            return false;
        }
    }

    @Override
    protected void onPreExecute(){
        if(showProgressBar){
            try{
                progressBar = new ProgressDialog(context);
                progressBar.setCancelable(false);
                progressBar.setMessage(context.getString(R.string.rss_updating)+"...");
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.show();
            }catch(Exception e){
                myCLocker.saveErrorLog(null,e);
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean resultOK){
        try{
            if(progressBar!=null){
                progressBar.dismiss();
            }
        }catch(Exception e){
            myCLocker.saveErrorLog(null,e);
        }

        if(resultOK){
            DateFormat ltf = new SimpleDateFormat("h:mma"+", "+"EEE, d MMM yy",Locale.getDefault());
            String currentTime = ltf.format(new Date());
            String str = context.getString(R.string.last_update_successful_at)+": "+currentTime;

            new SaveData(context).setRssUpdated(true);
            final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
            editor.putString("LastUpdateDate",str);
            editor.commit();

            Intent i = new Intent(context.getPackageName()+C.UPDATE_RSS);
            context.sendBroadcast(i);

            showToastMsg(context,context.getString(R.string.rss_updated));
        }else{
            showToastMsg(context,context.getString(R.string.network_error));
        }
    }

    private void showToastMsg(Context context,String str){
        SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        boolean cBoxShowRSSUpdateNotice = prefs.getBoolean("cBoxShowRSSUpdateNotice",true);
        if(cBoxShowRSSUpdateNotice){
            Toast.makeText(context,str,Toast.LENGTH_LONG).show();
        }
    }

    private boolean readStream(){
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        boolean rBtnCustomRSS = prefs.getBoolean("rBtnCustomRSS",false);
        String str = "";
        if(rBtnCustomRSS){
            str = prefs.getString("getRSSCustomLink","http://rss.cnn.com/rss/edition.rss");
        }else{
            str = prefs.getString("getRSSLink","http://rss.cnn.com/rss/edition.rss");
        }
        RSSReader reader = new RSSReader();
        DataRSS db = new DataRSS(context);

        RSSFeed feed = null;
        try{
            feed = reader.load(str);

            String rssSource = feed.toString();
            List<RSSItem> list = feed.getItems();
            db.deleteAllRSS();
            for(RSSItem rssItem : list){
                db.addRSS(new InfoRSS(rssSource,rssItem.getTitle(),rssItem.getPubDate(),rssItem.getDescription(),rssItem.getContent(),rssItem.getCategories().toString(),rssItem.getLink().toString()));
            }
            if(db!=null){
                db.close();
                db = null;
            }
            return true;
        }catch(RSSReaderException e){
            myCLocker.saveErrorLog(null,e);
        }
        if(db!=null){
            db.close();
            db = null;
        }
        return false;
    }
}
