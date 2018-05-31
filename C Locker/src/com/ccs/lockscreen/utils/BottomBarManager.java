package com.ccs.lockscreen.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.provider.CallLog.Calls;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils.TruncateAt;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.DataRSS;
import com.ccs.lockscreen.data.InfoCalendarEvent;
import com.ccs.lockscreen.data.InfoGmail;
import com.ccs.lockscreen.data.InfoRSS;
import com.ccs.lockscreen.fragments.FragmentCallLogs;
import com.ccs.lockscreen.fragments.FragmentEvents;
import com.ccs.lockscreen.fragments.FragmentGmail;
import com.ccs.lockscreen.fragments.FragmentRSS;
import com.ccs.lockscreen.fragments.FragmentSMS;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen_pro.Locker;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class BottomBarManager implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int DISPLAY_BATTERY = 1, DISPLAY_RSS = 2, DISPLAY_NETWORK = 3;
    //Loader fields
    private static final int LOADER_ID_GMAIL = 0, LOADER_ID_SMS = 1, LOADER_ID_CALLLOG = 2, LOADER_ID_EVENTS = 3;
    //
    private static final int BOTTOM_ANI_SPEED = 200;
    private static final String COLOR_GREY = "#444444";
    private static final String COLOR_CLEAR = "#00000000";
    //public fields
    public View viewRunSettings;
    public ImageView imgBottomBarArrow;
    public TextView txtBtty;
    //classes
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    private Locker activity;
    private MyCLocker mLocker;
    private SaveData mSaveData = null;
    private Handler handler = new Handler();
    private LoaderManager loaderManager;
    //views
    private RelativeLayout lytBatteryBar;
    private View viewBtty;
    private String[] viewPagerTitle;
    private TabPageIndicator indicator;
    private LinearLayout lytPager;
    private boolean disableBottomBarManager = false, cBoxEnableBottomBar, isSetVolumeActive, cBoxEnableGmail, cBoxEnableSms, cBoxEnableMissedCall, cBoxEnableCalendar, cBoxEnableRSS, cBoxShowArrowUpIcon, cBoxShowBatteryLevelBar, isBottomBarShow, isCldAccountSelected, isSMSNoticeSupported;
    private boolean
            //RSS
            isAniLeft = true, isAniTop = false, isGmailLoaded = false, isSMSLoaded = false, isMissCallLoaded = false, isEventLoaded = false;
    private int intDisplayWidth, intDisplayHeight, infoBarText, intAddEventDays, intRssDisplayType, intRssDisplayLine;
    private int gmailCount = 0, smsCount = 0, missCallCount = 0, eventCount = 0;
    private int bttyLevel = 0, status = 0;
    private String gmailAccount;
    private String strCalendarAccountFilter, strTextStyleLocker, strColorBottomBarBttyLvl;
    private String strRssAll, strRssTitle, strRss1, strRss2, strRss3, strRss4, strRss5;
    private Runnable runRssDisplay = new Runnable(){
        @Override
        public void run(){
            try{
                switch(intRssDisplayLine){
                    case 0:
                        if(intRssDisplayType==0){
                            setBottomBarTransition(isAniLeft,isAniTop,strRssTitle);
                            isAniTop = true;
                            isAniLeft = false;
                        }else{
                            setBottomBarTransition(isAniLeft,false,strRssTitle);
                            isAniTop = false;
                            isAniLeft = true;
                        }
                        intRssDisplayLine = 1;
                        break;
                    case 1:
                        if(intRssDisplayType==0){
                            setBottomBarTransition(false,true,strRss1);
                        }else{
                            setBottomBarTransition(true,false,strRss1);
                        }
                        intRssDisplayLine = 2;
                        break;
                    case 2:
                        if(intRssDisplayType==0){
                            setBottomBarTransition(false,true,strRss2);
                        }else{
                            setBottomBarTransition(true,false,strRss2);
                        }
                        intRssDisplayLine = 3;
                        break;
                    case 3:
                        if(intRssDisplayType==0){
                            setBottomBarTransition(false,true,strRss3);
                        }else{
                            setBottomBarTransition(true,false,strRss3);
                        }
                        intRssDisplayLine = 4;
                        break;
                    case 4:
                        if(intRssDisplayType==0){
                            setBottomBarTransition(false,true,strRss4);
                        }else{
                            setBottomBarTransition(true,false,strRss4);
                        }
                        intRssDisplayLine = 5;
                        break;
                    case 5:
                        if(intRssDisplayType==0){
                            setBottomBarTransition(false,true,strRss5);
                        }else{
                            setBottomBarTransition(true,false,strRss5);
                        }
                        intRssDisplayLine = 0;
                        break;
                }
                handler.postDelayed(runRssDisplay,3000);
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };
    private Runnable runDelayBattery = new Runnable(){
        @Override
        public void run(){
            setBottomBarVisibility();
            isSetVolumeActive = false;
            setBottomBarText(false);
        }
    };
    //
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
                    if(bttyLevel!=intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1) || status!=intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1)){

                        bttyLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
                        setBottomBarText(true);
                    }
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };

    public BottomBarManager(Locker activity){
        this.context = activity.getBaseContext();
        this.activity = activity;
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        editor = prefs.edit();
        mLocker = new MyCLocker(context);
        mSaveData = new SaveData(context);
        //bottombar
        lytPager = (LinearLayout)activity.findViewById(R.id.lytPager);//Linear Layout
        lytBatteryBar = (RelativeLayout)activity.findViewById(R.id.relativelyt_btty);//Relative Layout
        viewBtty = activity.findViewById(R.id.lyt_btty);//View
        viewRunSettings = activity.findViewById(R.id.viewRunSettings);//View
        imgBottomBarArrow = (ImageView)activity.findViewById(R.id.imgBottomBarArrow);
        txtBtty = (TextView)activity.findViewById(R.id.txt_btty);
        //load settings
        loaderManager = activity.getSupportLoaderManager();
        loadPrefsBottomBar();
        loadBottomBarPages();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        this.context.registerReceiver(mReceiver,intentFilter);
    }

    private void loadPrefsBottomBar(){
        intDisplayWidth = C.getRealDisplaySize(context).x;
        intDisplayHeight = C.getRealDisplaySize(context).y;
        cBoxEnableBottomBar = prefs.getBoolean("cBoxEnableBottomBar",false);
        //BottomBar
        cBoxEnableGmail = GmailContract.isGmailSupported(context) && prefs.getBoolean("cBoxEnableGmail",false);
        cBoxEnableSms = prefs.getBoolean("cBoxEnableSms",true);
        cBoxEnableMissedCall = prefs.getBoolean("cBoxEnableMissedCall",true);
        //calendar events
        cBoxEnableCalendar = prefs.getBoolean("cBoxEnableCalendar",false);    //BottomBar
        isCldAccountSelected = prefs.getBoolean("isCldAccountSelected",false);
        intAddEventDays = prefs.getInt("intAddEventDay",30);
        strCalendarAccountFilter = prefs.getString("strCalendarAccount","");
        //rss
        cBoxEnableRSS = prefs.getBoolean("cBoxEnableRSS",false); //BottomBar
        boolean isInfoBarTextChanged = prefs.getBoolean("isInfoBarTextChanged",false);
        if(cBoxEnableRSS && !isInfoBarTextChanged){
            infoBarText = DISPLAY_RSS;
        }else{
            infoBarText = prefs.getInt("infoBarText",DISPLAY_BATTERY);
        }
        intRssDisplayType = prefs.getInt("intRssDisplayType",0);
        isSMSNoticeSupported = prefs.getBoolean("isSMSNoticeSupported",false);
        cBoxShowArrowUpIcon = prefs.getBoolean("cBoxShowArrowUpIcon",true);
        cBoxShowBatteryLevelBar = prefs.getBoolean("cBoxShowBatteryLevelBar",false);
        strColorBottomBarBttyLvl = prefs.getString(P.STR_COLOR_BATTERY_BAR,"888888");
        strTextStyleLocker = prefs.getString("strTextStyleLocker","txtStyle03_3.ttf");
        txtBtty.setTypeface(typeFace1(),typeFace2());
    }

    private void loadBottomBarPages(){
        if(cBoxEnableBottomBar){
            //load number of pages 1st
            final ArrayList<String> list = new ArrayList<>();
            if(cBoxEnableGmail){
                list.add(context.getString(R.string.viewpager_gmail));
            }
            if(cBoxEnableSms && isSMSNoticeSupported){
                list.add(context.getString(R.string.viewpager_sms));
            }
            if(cBoxEnableMissedCall){
                list.add(context.getString(R.string.viewpager_call_log));
            }
            if(cBoxEnableCalendar){
                list.add(context.getString(R.string.viewpager_events));
            }
            if(cBoxEnableRSS){
                list.add(context.getString(R.string.viewpager_rss));
            }
            if(list.size()>0){
                viewPagerTitle = list.toArray(new String[list.size()]);
            }
            //then only load the data
            if(cBoxEnableGmail){
                LoadGmailAccount();
            }else{
                isGmailLoaded = true;
            }
            if(cBoxEnableSms && isSMSNoticeSupported){
                loaderManager.restartLoader(LOADER_ID_SMS,null,this);
            }else{
                isSMSLoaded = true;
            }
            if(cBoxEnableMissedCall){
                loaderManager.restartLoader(LOADER_ID_CALLLOG,null,this);
            }else{
                isMissCallLoaded = true;
            }
            if(cBoxEnableCalendar){
                loaderManager.restartLoader(LOADER_ID_EVENTS,null,this);
            }else{
                isEventLoaded = true;
            }
        }
    }

    private Typeface typeFace1(){
        try{
            if(strTextStyleLocker.equals("Default")){
                return Typeface.DEFAULT;
            }
            if(strTextStyleLocker.equals("txtStyle03_1.ttf")){
                return Typeface.createFromAsset(context.getAssets(),"txtStyle03.ttf");
            }else{
                return Typeface.createFromAsset(context.getAssets(),strTextStyleLocker);
            }
        }catch(Exception e){
            return Typeface.DEFAULT;
        }
    }

    private int typeFace2(){
        if(strTextStyleLocker.equals("txtStyle03.ttf")){
            return Typeface.BOLD;
        }else{
            return Typeface.NORMAL;
        }
    }

    private void LoadGmailAccount(){
        // Get the account list, and pick the first one
        final String ACCOUNT_TYPE_GOOGLE = "com.google";
        final String[] FEATURES_MAIL = {"service_mail"};
        AccountManager.get(context).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE,FEATURES_MAIL,new AccountManagerCallback<Account[]>(){
            @Override
            public void run(AccountManagerFuture<Account[]> future){
                Account[] accounts = null;
                try{
                    accounts = future.getResult();
                }catch(OperationCanceledException | IOException | AuthenticatorException oce){
                    mLocker.saveErrorLog(null,oce);
                }
                onAccountResults(accounts);
            }
        },null);
    }

    private void onAccountResults(Account[] accounts){
        if(accounts!=null && accounts.length>0){
            // Pick the first one, and display a list of labels
            gmailAccount = accounts[0].name;
            final Bundle args = new Bundle();
            args.putString("account",gmailAccount);
            loaderManager.restartLoader(LOADER_ID_GMAIL,args,this);
        }
    }

    //Loader
    @Override
    public Loader<Cursor> onCreateLoader(int id,Bundle args){
        try{
            switch(id){
                case LOADER_ID_GMAIL:
                    return loadGmailCursor(args);
                case LOADER_ID_SMS:
                    return loadSMSCursor();
                case LOADER_ID_CALLLOG:
                    return loadCallLogCursor();
                case LOADER_ID_EVENTS:
                    return loadEventCursor();
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,Cursor cursor){
        try{
            switch(loader.getId()){
                case LOADER_ID_GMAIL:
                    new LoadGmail(context).execute();
                    break;
                case LOADER_ID_SMS:
                    if(cursor!=null){
                        smsCount = cursor.getCount();
                    }
                    isSMSLoaded = true;
                    if(isGmailLoaded && isSMSLoaded && isMissCallLoaded && isEventLoaded){
                        runViewPager();
                    }
                    break;
                case LOADER_ID_CALLLOG:
                    if(cursor!=null){
                        missCallCount = cursor.getCount();
                    }
                    isMissCallLoaded = true;
                    if(isGmailLoaded && isSMSLoaded && isMissCallLoaded && isEventLoaded){
                        runViewPager();
                    }
                    break;
                case LOADER_ID_EVENTS:
                    eventCount = 0;
                    new LoadEvents(context).execute();
                    break;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0){
    }

    public final void runViewPager(){
        if(disableBottomBarManager){
            return;
        }
        try{
            if(missCallCount>0){
                loadViewpager(getViewPageNo(context.getString(R.string.viewpager_call_log)),true);
            }else if(smsCount>0){
                loadViewpager(getViewPageNo(context.getString(R.string.viewpager_sms)),true);
            }else if(gmailCount>0){
                loadViewpager(getViewPageNo(context.getString(R.string.viewpager_gmail)),true);
            }else if(eventCount>0){
                loadViewpager(getViewPageNo(context.getString(R.string.viewpager_events)),true);
            }else{
                if(viewPagerTitle!=null){
                    loadViewpager(0,true);
                }
            }
            onReceiveNotice();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void loadViewpager(final int page,boolean isNew){
        if(page!=-1){
            final FragmentPagerAdapter adapter = new BottomBarAdapter(activity.getSupportFragmentManager());
            if(isNew){
                final ViewPager pager = (ViewPager)activity.findViewById(R.id.pager);
                if(indicator==null){
                    indicator = (TabPageIndicator)activity.findViewById(R.id.indicator);
                    indicator.setBackgroundColor(COLOR_CLEAR);
                    indicator.setTextHeight(28);
                }
                pager.setCurrentItem(page);
                pager.setAdapter(adapter);
                indicator.setViewPager(pager,page,context,typeFace1());
            }else{
                adapter.notifyDataSetChanged();
            }
        }
    }

    private int getViewPageNo(final String pageName){
        int pageNo = 0;
        if(viewPagerTitle!=null){
            for(String item : viewPagerTitle){
                if(item.equals(pageName)){
                    return pageNo;
                }
                pageNo++;
            }
        }
        return -1;
    }

    private void onReceiveNotice(){
        if(indicator!=null){
            final boolean cBoxEnableBottomBarNotice = prefs.getBoolean("cBoxEnableBottomBarNotice",true);
            if(cBoxEnableGmail){
                if(gmailCount>0){
                    indicator.changeTitle(R.string.viewpager_gmail,"("+gmailCount+")");
                    if(cBoxEnableBottomBar && cBoxEnableBottomBarNotice){
                        if(!indicator.isShown()){
                            indicator.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    indicator.changeTitle(R.string.viewpager_gmail,"");
                }
            }
            if(cBoxEnableSms){
                if(smsCount>0){
                    indicator.changeTitle(R.string.viewpager_sms,"("+smsCount+")");
                    if(cBoxEnableBottomBar && cBoxEnableBottomBarNotice){
                        if(!indicator.isShown()){
                            indicator.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    indicator.changeTitle(R.string.viewpager_sms,"");
                }
            }
            if(cBoxEnableMissedCall){
                if(missCallCount>0){
                    indicator.changeTitle(R.string.viewpager_call_log,"("+missCallCount+")");
                    if(cBoxEnableBottomBar && cBoxEnableBottomBarNotice){
                        if(!indicator.isShown()){
                            indicator.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    indicator.changeTitle(R.string.viewpager_call_log,"");
                }
            }
            if(cBoxEnableCalendar){
                if(eventCount>0){
                    indicator.changeTitle(R.string.viewpager_events,"(!)");
                    if(cBoxEnableBottomBar && cBoxEnableBottomBarNotice){
                        if(!indicator.isShown()){
                            indicator.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    indicator.changeTitle(R.string.viewpager_events,"");
                }
            }
            if(cBoxEnableRSS){
                if(mSaveData.isRssUpdated()){
                    indicator.changeTitle(R.string.viewpager_rss,"(!)");
                    if(indicator.getCurrentPage()!=getViewPageNo(context.getString(R.string.viewpager_call_log)) &&
                            indicator.getCurrentPage()!=getViewPageNo(context.getString(R.string.viewpager_sms)) &&
                            indicator.getCurrentPage()!=getViewPageNo(context.getString(R.string.viewpager_gmail))){
                        indicator.setCurrentItem(getViewPageNo(context.getString(R.string.viewpager_rss)));
                    }
                    if(cBoxEnableBottomBar && cBoxEnableBottomBarNotice){
                        if(!indicator.isShown()){
                            indicator.setVisibility(View.VISIBLE);
                        }
                    }
                }else{
                    indicator.changeTitle(R.string.viewpager_rss,"");
                }
            }
        }
    }

    private CursorLoader loadGmailCursor(Bundle args){
        try{
            final String account = args.getString("account");
            final Uri labelsUri = GmailContract.Labels.getLabelsUri(account);
            return new CursorLoader(context,labelsUri,null,null,null,null);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
            return null;
        }
    }

    private CursorLoader loadSMSCursor(){
        try{
            if(isSMSUriValid()){
                //final Uri uri = Uri.parse("content://sms/inbox");
                //final Uri uri = Uri.parse("content://mms/inbox");
                final Uri uri = Uri.parse(C.SMS_URI);
                final String[] projection = new String[]{"*"};
                final String selection = ("read"+"=?");
                final String[] arg = new String[]{"0"};//"1"=read,"0"=unread
                final String sort = "date DESC";//"startDay ASC, startMinute ASC"

                return new CursorLoader(context,uri,projection,selection,arg,sort);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return null;
    }

    private CursorLoader loadCallLogCursor(){
        try{
            final String[] projection = new String[]{Calls.NUMBER,Calls.DATE,Calls.IS_READ};
            final String selection = (Calls.TYPE+"=? AND "+Calls.IS_READ+"=?");
            final String[] arg = new String[]{Calls.MISSED_TYPE+"","0"};
            return new CursorLoader(context,Calls.CONTENT_URI,projection,selection,arg,null);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return null;
    }

    private CursorLoader loadEventCursor(){
        try{
            Calendar cld = Calendar.getInstance();
            cld = setTo12am();
            final long longFilterStart = cld.getTimeInMillis()-(DateUtils.DAY_IN_MILLIS*90);
            final long longFilterEnd = cld.getTimeInMillis()+(DateUtils.DAY_IN_MILLIS*intAddEventDays);

            final String[] projection = new String[]{Instances.CALENDAR_ID,Instances.EVENT_ID,Instances.TITLE,Instances.BEGIN,Instances.END,Instances.EVENT_LOCATION,Instances.ALL_DAY,Instances.EVENT_TIMEZONE};
            String selection = "(";
            final String[] arg = strCalendarAccountFilter.split(",");
            for(int i = 0; i<arg.length; i++){
                if(i<(arg.length-1)){
                    selection += Instances.CALENDAR_ID+"=? OR ";
                }else{
                    selection += Instances.CALENDAR_ID+"=?)";
                }
            }
            String sort = Instances.BEGIN+" ASC";//"startDay ASC, startMinute ASC"

            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder,longFilterStart);
            ContentUris.appendId(builder,longFilterEnd);
            Uri eventsUri = builder.build();

            if(isCldAccountSelected){
                return new CursorLoader(context,eventsUri,projection,selection,arg,sort);
            }else{
                return new CursorLoader(context,eventsUri,projection,null,null,sort);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return null;
    }

    private boolean isSMSUriValid(){
        try{
            Cursor cursor = null;
            try{
                final Uri uri = Uri.parse(C.SMS_URI);
                final String[] projection = new String[]{"*"};
                final String selection = ("read"+"=?");
                final String[] arg = new String[]{"0"};//"1"=read,"0"=unread
                final String sort = "date DESC";//"startDay ASC, startMinute ASC"

                cursor = context.getContentResolver().query(uri,projection,selection,arg,sort);
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return true;
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return false;
    }

    private Calendar setTo12am(){
        Calendar cld = Calendar.getInstance();
        cld.add(Calendar.HOUR,-cld.get(Calendar.HOUR_OF_DAY));
        cld.add(Calendar.MINUTE,-(cld.get(Calendar.MINUTE)+1));
        return cld;
    }

    private boolean isTodayEvent(final InfoCalendarEvent cldEvent){
        final Calendar cldNow = Calendar.getInstance();
        final Calendar cldEventStart = Calendar.getInstance();
        final Calendar cldEventEnd = Calendar.getInstance();
        cldNow.getTimeInMillis();
        cldEventStart.setTimeInMillis(cldEvent.getCldDateStart());
        cldEventEnd.setTimeInMillis(cldEvent.getCldDateEnd());

        long today = cldNow.get(Calendar.DAY_OF_YEAR);
        long eventDayStart = cldEventStart.get(Calendar.DAY_OF_YEAR);
        long eventDayEnd = cldEventEnd.get(Calendar.DAY_OF_YEAR);
        if(today<=intAddEventDays){
            today += 365;
        }
        if(eventDayStart<=intAddEventDays){
            eventDayStart += 365;
        }
        if(eventDayEnd<=intAddEventDays){
            eventDayEnd += 365;
        }
        return eventDayStart<=today && eventDayEnd>=today;
    }

    //
    public final void closeAll(){
        try{
            closeRSS();
            disableBottomBar();
            context.unregisterReceiver(mReceiver);//dont put in disableBottomBar();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    public final void closeRSS(){
        handler.removeCallbacks(runRssDisplay);
    }

    public final void disableBottomBar(){
        try{
            loaderManager.destroyLoader(LOADER_ID_SMS);
            loaderManager.destroyLoader(LOADER_ID_CALLLOG);
            loaderManager.destroyLoader(LOADER_ID_EVENTS);
            loaderManager.destroyLoader(LOADER_ID_GMAIL);
            hideAllBottomBarVisibility();
            disableBottomBarManager = true;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public final void hideAllBottomBarVisibility(){
        lytBatteryBar.setVisibility(View.GONE);
        imgBottomBarArrow.setVisibility(View.GONE);
        if(indicator!=null){
            indicator.setVisibility(View.GONE);
        }
    }

    public final void loadRssData(){
        if(disableBottomBarManager){
            return;
        }
        try{
            if(cBoxEnableRSS){
                final DataRSS db = new DataRSS(context);
                if(db!=null){
                    if(db.getRSS(1)!=null){
                        InfoRSS rss1 = db.getRSS(1);
                        InfoRSS rss2 = db.getRSS(2);
                        InfoRSS rss3 = db.getRSS(3);
                        InfoRSS rss4 = db.getRSS(4);
                        InfoRSS rss5 = db.getRSS(5);
                        db.close();

                        if(intRssDisplayType==0 || intRssDisplayType==1){
                            strRssTitle = rss1.getRssSource();
                            strRss1 = cutString("(1)"+rss1.getRssTitle(),38);
                            strRss2 = cutString("(2)"+rss2.getRssTitle(),38);
                            strRss3 = cutString("(3)"+rss3.getRssTitle(),38);
                            strRss4 = cutString("(4)"+rss4.getRssTitle(),38);
                            strRss5 = cutString("(5)"+rss5.getRssTitle(),38);
                        }else{
                            strRssAll = rss1.getRssSource()+": (1)"+
                                    rss1.getRssTitle()+" (2)"+
                                    rss2.getRssTitle()+" (3)"+
                                    rss3.getRssTitle()+" (4)"+
                                    rss4.getRssTitle()+" (5)"+
                                    rss5.getRssTitle();
                        }
                    }else{
                        infoBarText = prefs.getInt("infoBarText",DISPLAY_BATTERY);
                    }
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private String cutString(String str,int strLength){
        if(str.length()>strLength){
            return str.substring(0,strLength)+"...";
        }else{
            return str;
        }
    }

    public final void setBottomTouch(int direction){
        if(disableBottomBarManager){
            return;
        }
        if(direction==C.BOTTOM_TOUCH_TAP){
            if(isBottomBarShow){
                setBottomBarHide();
                return;
            }
            if(indicator!=null){
                if(indicator.isShown()){
                    //indicator.setVisibility(View.GONE);
                    setBottomBarShow(false);
                }else{
                    indicator.setVisibility(View.VISIBLE);
                }
            }
        }else if(direction==C.BOTTOM_TOUCH_TOP){
            setBottomBarShow(false);
        }else if(direction==C.BOTTOM_TOUCH_LEFT){
            setBottomSwipe(true);
        }else if(direction==C.BOTTOM_TOUCH_RIGHT){
            setBottomSwipe(false);
        }
    }

    public final void setBottomBarHide(){
        if(disableBottomBarManager){
            return;
        }
        if(indicator!=null){
            indicator.setVisibility(View.GONE);
            final View parent = (View)indicator.getParent();
            if(parent!=null){
                parent.setBackgroundColor(Color.parseColor(COLOR_CLEAR));
            }
        }
        if(!cBoxEnableBottomBar){
            return;
        }
        if(!isBottomBarShow){
            return;
        }

        isBottomBarShow = false;
        activity.callWindowAlert(true);
        if(indicator!=null){
            if(imgBottomBarArrow!=null){
                imgBottomBarArrow.setRotation(0);
            }
            View view = imgBottomBarArrow;
            if(!cBoxShowArrowUpIcon){
                view = viewRunSettings;
            }

            view.setBackground(null);
            final AnimationBottomBar ani = new AnimationBottomBar(lytPager,lytPager.getHeight(),0,false);
            ani.setDuration(BOTTOM_ANI_SPEED);
            ani.setAnimationListener(new AnimationListener(){
                @Override
                public void onAnimationEnd(Animation arg0){
                    if(infoBarText==DISPLAY_RSS){
                        if(intRssDisplayType==0 || intRssDisplayType==1){
                            setRssSlideDisplay(true);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation arg0){
                }

                @Override
                public void onAnimationStart(Animation arg0){
                }
            });
            lytPager.startAnimation(ani);
        }
    }

    private void setBottomBarShow(final boolean isFullTop){
        if(!cBoxEnableBottomBar){
            return;
        }
        if(isBottomBarShow){
            return;
        }

        if(!C.isAndroid(Build.VERSION_CODES.KITKAT) && cBoxEnableBottomBar){
            activity.callWindowAlert(false);
        }
        int targetHeight;
        isBottomBarShow = true;
        if(indicator!=null){
            if(imgBottomBarArrow!=null){
                imgBottomBarArrow.setRotation(180);
            }
            if(isFullTop){
                targetHeight = (int)(intDisplayHeight*0.75);
            }else{
                targetHeight = (intDisplayHeight/2);
            }
            indicator.setVisibility(View.INVISIBLE);//animation bug, dont delete this
            indicator.setVisibility(View.VISIBLE);//animation bug, dont delete this
            final View parent = (View)indicator.getParent();
            if(parent!=null){
                parent.setBackgroundColor(Color.parseColor(COLOR_GREY));
            }

            AnimationBottomBar ani = new AnimationBottomBar(lytPager,lytPager.getHeight(),targetHeight,true);
            ani.setDuration(BOTTOM_ANI_SPEED);
            ani.setAnimationListener(new AnimationListener(){
                @Override
                public void onAnimationStart(Animation arg0){
                }                @Override
                public void onAnimationEnd(Animation arg0){
                    if(infoBarText==DISPLAY_RSS){
                        if(intRssDisplayType==0 || intRssDisplayType==1){
                            setRssSlideDisplay(true);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation arg0){
                }


            });
            lytPager.startAnimation(ani);
        }
    }

    private void setBottomSwipe(final boolean isLeft){
        if(isLeft){
            infoBarText = getInforbarText(true,infoBarText);
            editor.putBoolean("isInfoBarTextChanged",true);//disable auto showing rss
            editor.putInt("infoBarText",infoBarText);
            editor.commit();

            isAniTop = false;
            isAniLeft = true;
            setBottomBarText(false);
        }else{
            infoBarText = getInforbarText(false,infoBarText);
            editor.putBoolean("isInfoBarTextChanged",true);//disable auto showing rss
            editor.putInt("infoBarText",infoBarText);
            editor.commit();

            isAniTop = false;
            isAniLeft = false;
            setBottomBarText(false);
        }
    }

    public final void setRssSlideDisplay(final boolean startTimer){
        if(disableBottomBarManager){
            return;
        }
        try{
            if(startTimer){
                intRssDisplayLine = 0;
                handler.removeCallbacks(runRssDisplay);
                handler.post(runRssDisplay);
            }else{
                handler.removeCallbacks(runRssDisplay);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private int getInforbarText(final boolean isReverse,int text){
        if(isReverse){
            if(text==DISPLAY_BATTERY){
                return DISPLAY_NETWORK;
            }else if(text==DISPLAY_RSS){
                return DISPLAY_BATTERY;
            }else{
                if(cBoxEnableRSS){
                    return DISPLAY_RSS;
                }else{
                    return DISPLAY_BATTERY;
                }
            }
        }else{
            if(text==DISPLAY_BATTERY){
                if(cBoxEnableRSS){
                    return DISPLAY_RSS;
                }else{
                    return DISPLAY_NETWORK;
                }
            }else if(text==DISPLAY_RSS){
                return DISPLAY_NETWORK;
            }else{
                return DISPLAY_BATTERY;
            }
        }
    }

    public final void setBottomBarText(boolean isBatteryUpdate){
        if(isSetVolumeActive){
            return;
        }
        if(disableBottomBarManager){
            return;
        }
        if(!cBoxShowArrowUpIcon){
            if(cBoxShowBatteryLevelBar){
                if(bttyLevel!=-5){//-5 = not battery update
                    viewBtty.setLayoutParams(new RelativeLayout.LayoutParams((bttyLevel*intDisplayWidth)/100,LayoutParams.MATCH_PARENT));
                    lytBatteryBar.setBackground(ContextCompat.getDrawable(context,R.drawable.shapebtty_bg));
                    if(bttyLevel<=15){
                        viewBtty.setBackground(C.setRectangleColor(C.REC_COLOR_BTTY_BAR,"ff0000",false));
                    }else{
                        viewBtty.setBackground(C.setRectangleColor(C.REC_COLOR_BTTY_BAR,strColorBottomBarBttyLvl,false));
                    }
                }
            }else{
                lytBatteryBar.setBackground(null);
                viewBtty.setBackground(null);
            }
            if(infoBarText==DISPLAY_BATTERY){
                setRssSlideDisplay(false);
                if(status==BatteryManager.BATTERY_PLUGGED_USB){
                    setBottomBarTransition(isAniLeft,false,context.getString(R.string.battery)+": "+bttyLevel+"%("+
                            context.getString(R.string.usb_charging)+"...)");
                }else if(status==BatteryManager.BATTERY_PLUGGED_AC){
                    setBottomBarTransition(isAniLeft,false,context.getString(R.string.battery)+": "+bttyLevel+"%("+
                            context.getString(R.string.ac_charging)+"...)");
                }else{
                    setBottomBarTransition(isAniLeft,false,context.getString(R.string.battery)+": "+bttyLevel+"%");
                }
            }else{
                if(isBatteryUpdate){
                    setRssSlideDisplay(false);
                    if(status==BatteryManager.BATTERY_PLUGGED_USB){
                        setBottomBarTransition(isAniLeft,false,context.getString(R.string.battery)+": "+bttyLevel+"%("+
                                context.getString(R.string.usb_charging)+"...)");
                    }else if(status==BatteryManager.BATTERY_PLUGGED_AC){
                        setBottomBarTransition(isAniLeft,false,context.getString(R.string.battery)+": "+bttyLevel+"%("+
                                context.getString(R.string.ac_charging)+"...)");
                    }else{
                        setBottomBarTransition(isAniLeft,false,context.getString(R.string.battery)+": "+bttyLevel+"%");
                    }
                    handler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            if(infoBarText==DISPLAY_RSS){
                                if(intRssDisplayType==0 || intRssDisplayType==1){
                                    setRssSlideDisplay(true);
                                }else{
                                    setBottomBarTransition(isAniLeft,false,strRssAll);
                                    txtBtty.setSelected(true);
                                    txtBtty.setEllipsize(TruncateAt.MARQUEE);
                                    txtBtty.setSingleLine(true);
                                }
                            }else if(infoBarText==DISPLAY_NETWORK){
                                setRssSlideDisplay(false);
                                TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                                String carrierName = manager.getNetworkOperatorName();
                                setBottomBarTransition(isAniLeft,false,carrierName);
                            }
                        }
                    },2000);
                }else{
                    if(infoBarText==DISPLAY_RSS){
                        if(intRssDisplayType==0 || intRssDisplayType==1){
                            setRssSlideDisplay(true);
                        }else{
                            setBottomBarTransition(isAniLeft,false,strRssAll);
                            txtBtty.setSelected(true);
                            txtBtty.setEllipsize(TruncateAt.MARQUEE);
                            txtBtty.setSingleLine(true);
                        }
                    }else if(infoBarText==DISPLAY_NETWORK){
                        setRssSlideDisplay(false);
                        final TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                        String carrierName = manager.getNetworkOperatorName();
                        if(carrierName!=null && carrierName.equals("")){
                            carrierName = "No Network";
                        }
                        setBottomBarTransition(isAniLeft,false,carrierName);
                    }
                }
            }
        }
    }

    public final void setBottomBarTransition(final boolean isSlideLeft,final boolean isSlideTop,
            final String info){
        if(disableBottomBarManager){
            return;
        }
        if(txtBtty.getText().toString().equals(info)){
            return;
        }
        final int top = -100;
        final int bottom = 100;
        final int left = -1000;
        final int Right = 1000;

        ObjectAnimator outX;
        if(isSlideLeft){
            outX = ObjectAnimator.ofFloat(txtBtty,"x",txtBtty.getLeft(),left).setDuration(200);
        }else if(isSlideTop){
            outX = ObjectAnimator.ofFloat(txtBtty,"y",0,bottom).setDuration(200);
        }else{
            outX = ObjectAnimator.ofFloat(txtBtty,"x",txtBtty.getLeft(),Right).setDuration(200);
        }
        final AnimatorSet animSet = new AnimatorSet();
        animSet.play(outX);
        animSet.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator arg0){
            }

            @Override
            public void onAnimationEnd(Animator arg0){
                txtBtty.setText(info);
                txtBtty.setVisibility(View.VISIBLE);
                txtBtty.requestLayout();//reset the layout, else it may not be accurate

                ObjectAnimator inX;
                if(isSlideLeft){
                    inX = ObjectAnimator.ofFloat(txtBtty,"x",Right,0).setDuration(200);
                }else if(isSlideTop){
                    inX = ObjectAnimator.ofFloat(txtBtty,"y",top,0).setDuration(200);
                }else{
                    inX = ObjectAnimator.ofFloat(txtBtty,"x",left,0).setDuration(200);
                }
                animSet.removeAllListeners();
                animSet.play(inX);
                animSet.start();
            }

            @Override
            public void onAnimationCancel(Animator arg0){
            }

            @Override
            public void onAnimationRepeat(Animator arg0){
            }
        });
        animSet.start();
    }

    //set bottom bar volume level
    public final void setVolume(final int shortcutId){
        isSetVolumeActive = true;
        setDelayBattery(false);
        final AudioManager aMgr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        Integer currentVolume = aMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        final Integer maxVolume = aMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if(shortcutId==DataAppsSelection.VOLUME_UP){
            currentVolume++;
            aMgr.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
            if(currentVolume>maxVolume){
                currentVolume = maxVolume;
            }
            txtBtty.setText("Volume UP ("+currentVolume+")");
        }else{
            currentVolume--;
            aMgr.setStreamVolume(AudioManager.STREAM_MUSIC,currentVolume,0);
            if(currentVolume==-1){
                currentVolume = 0;
            }
            txtBtty.setText("Volume DOWN ("+currentVolume+")");
        }
        final Float f = (currentVolume.floatValue()/maxVolume.floatValue())*100;
        setBottomBarVisibility();
        viewBtty.setLayoutParams(new RelativeLayout.LayoutParams((f.intValue()*intDisplayWidth)/100,LayoutParams.MATCH_PARENT));
        viewBtty.setBackground(C.setRectangleColor(C.REC_COLOR_BTTY_BAR,strColorBottomBarBttyLvl,false));
        setDelayBattery(true);
    }

    private void setDelayBattery(final boolean bln){
        handler.removeCallbacks(runDelayBattery);
        if(bln){
            handler.postDelayed(runDelayBattery,5000);
        }
    }

    public final void setBottomBarVisibility(){
        if(disableBottomBarManager){
            return;
        }
        if(cBoxShowArrowUpIcon){
            if(cBoxEnableBottomBar){
                lytBatteryBar.setVisibility(View.GONE);
                imgBottomBarArrow.setVisibility(View.VISIBLE);
            }
        }else{
            if(!cBoxEnableBottomBar){
                infoBarText = prefs.getInt("infoBarText",DISPLAY_BATTERY);
            }
            lytBatteryBar.setVisibility(View.VISIBLE);
            imgBottomBarArrow.setVisibility(View.GONE);
        }
    }

    private class BottomBarAdapter extends FragmentPagerAdapter implements IconPagerAdapter{
        private int pageCount;

        private BottomBarAdapter(FragmentManager fm){
            super(fm);
            this.pageCount = viewPagerTitle.length;
        }

        @Override
        public Fragment getItem(int position){
            if(viewPagerTitle[position].equals(context.getString(R.string.viewpager_gmail))){
                return FragmentGmail.newInstance(gmailAccount);
            }else if(viewPagerTitle[position].equals(context.getString(R.string.viewpager_sms))){
                return FragmentSMS.newInstance(null);
            }else if(viewPagerTitle[position].equals(context.getString(R.string.viewpager_call_log))){
                return FragmentCallLogs.newInstance(null);
            }else if(viewPagerTitle[position].equals(context.getString(R.string.viewpager_events))){
                return FragmentEvents.newInstance(null);
            }else if(viewPagerTitle[position].equals(context.getString(R.string.viewpager_rss))){
                return FragmentRSS.newInstance();
            }
            return null;
        }

        @Override
        public int getCount(){
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return viewPagerTitle[position%viewPagerTitle.length];
        }

        @Override
        public int getIconResId(int index){
            return icons()[index];
        }

        private int[] icons(){
            switch(pageCount){
                case 3:
                    //return new int[]{0,0,R.drawable.icon_rss_small,0,0};
                case 4:
                    //return new int[]{0,0,0,R.drawable.icon_rss_small,0};
                case 5:
                    //return new int[]{0,0,0,0,R.drawable.icon_rss_small};
            }
            return new int[]{0,0,0,0,0};
        }
    }

    private class LoadEvents extends AsyncTask<Void,Void,ArrayList<InfoCalendarEvent>>{
        private Context context;

        private LoadEvents(Context context){
            this.context = context;
        }

        @Override
        protected ArrayList<InfoCalendarEvent> doInBackground(Void... args){
            try{
                return loadEvents(loadEventCursor());
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return null;
        }        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        private ArrayList<InfoCalendarEvent> loadEvents(final Cursor cursor){
            final int ACCOUNT_ID = 0, EVENT_ID = 1, TITLE = 2, DTSTART = 3, DTEND = 4,
                    LOCATION = 5, ALLDAY = 6;//TIMEZONE = 7;
            final ArrayList<InfoCalendarEvent> tempCldList = new ArrayList<>();
            try{
                if(cursor!=null && cursor.getCount()>0){
                    final Calendar cldNow = Calendar.getInstance();//-TimeZone.getDefault().getRawOffset()
                    Calendar cldBegin = setTo12am();
                    Calendar cldEnd = setTo12am();
                    cldEnd.add(Calendar.DAY_OF_YEAR,intAddEventDays);

                    cursor.moveToFirst();
                    do{
                        if(cursor.getString(ALLDAY).equals("1")){
                            if(tzOffSetAdd(cursor.getLong(DTSTART))<=cldEnd.getTimeInMillis() && tzOffSetMinus(cursor.getLong(DTEND))>=cldBegin.getTimeInMillis()){
                                tempCldList.add(new InfoCalendarEvent(cursor.getInt(ACCOUNT_ID),cursor.getInt(EVENT_ID),cursor.getString(TITLE),tzOffSetAdd(cursor.getLong(DTSTART)),tzOffSetMinus(cursor.getLong(DTEND)),cursor.getString(LOCATION),cursor.getString(ALLDAY)));
                            }
                        }else if(cursor.getLong(DTEND)>=cldNow.getTimeInMillis()){
                            if(cursor.getLong(DTSTART)<=cldEnd.getTimeInMillis() && cursor.getLong(DTEND)>=cldBegin.getTimeInMillis()){
                                tempCldList.add(new InfoCalendarEvent(cursor.getInt(ACCOUNT_ID),cursor.getInt(EVENT_ID),cursor.getString(TITLE),cursor.getLong(DTSTART),cursor.getLong(DTEND),cursor.getString(LOCATION),cursor.getString(ALLDAY)));
                            }
                        }
                        /*
                        mLocker.writeToFile(
								"ACCOUNT_ID{"+
								cursor.getInt(ACCOUNT_ID)+"}EVENT_ID{"+
								cursor.getInt(EVENT_ID)+"}TITLE{"+
								cursor.getString(TITLE)+"}DTSTART{"+
								cursor.getLong(DTSTART)+"}DTEND{"+
								cursor.getLong(DTEND)+"}LOCATION{"+
								cursor.getString(LOCATION)+"}ALLDAY{"+
								cursor.getString(ALLDAY)+"}");*/
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return tempCldList;
        }

        private Cursor loadEventCursor(){
            try{
                Calendar cld = setTo12am();
                final long longFilterStart = cld.getTimeInMillis()-(DateUtils.DAY_IN_MILLIS*90);
                final long longFilterEnd = cld.getTimeInMillis()+(DateUtils.DAY_IN_MILLIS*intAddEventDays);

                final String[] projection = new String[]{Instances.CALENDAR_ID,Instances.EVENT_ID,Instances.TITLE,Instances.BEGIN,Instances.END,Instances.EVENT_LOCATION,Instances.ALL_DAY,Instances.EVENT_TIMEZONE};
                String selection = "(";
                final String[] arg = strCalendarAccountFilter.split(",");
                for(int i = 0; i<arg.length; i++){
                    if(i<(arg.length-1)){
                        selection += Instances.CALENDAR_ID+"=? OR ";
                    }else{
                        selection += Instances.CALENDAR_ID+"=?)";
                    }
                }
                final String sort = Instances.BEGIN+" ASC";//"startDay ASC, startMinute ASC"

                final Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder,longFilterStart);
                ContentUris.appendId(builder,longFilterEnd);
                final Uri eventsUri = builder.build();

                if(isCldAccountSelected){
                    return context.getContentResolver().query(eventsUri,projection,selection,arg,sort);
                }else{
                    return context.getContentResolver().query(eventsUri,projection,null,null,sort);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return null;
        }        @Override
        protected void onPostExecute(ArrayList<InfoCalendarEvent> tempCldList){
            try{
                isEventLoaded = true;
                if(tempCldList!=null && tempCldList.size()>0){
                    InfoCalendarEvent cldEvent = tempCldList.get(0);
                    if(isTodayEvent(cldEvent)){
                        eventCount = 1;
                    }
                }
                if(isGmailLoaded && isSMSLoaded && isMissCallLoaded && isEventLoaded){
                    runViewPager();
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private long tzOffSetMinus(long longDate){
            final long val = DateUtils.MINUTE_IN_MILLIS*2;
            return longDate-TimeZone.getDefault().getRawOffset()-val;
        }

        private long tzOffSetAdd(long longDate){
            final long val = DateUtils.MINUTE_IN_MILLIS*2;
            return (longDate-TimeZone.getDefault().getRawOffset())+val;
        }
    }

    private class LoadGmail extends AsyncTask<Void,Void,ArrayList<InfoGmail>>{
        private Context context;

        private LoadGmail(Context context){
            this.context = context;
        }

        @Override
        protected ArrayList<InfoGmail> doInBackground(Void... args){
            try{
                return loadUnreadGmail(loadGmailCursor());
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<InfoGmail> tempGmailList){
            try{
                isGmailLoaded = true;
                if(isGmailLoaded && isSMSLoaded && isMissCallLoaded && isEventLoaded){
                    runViewPager();
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }

        private Cursor loadGmailCursor(){
            try{
                final Uri labelsUri = GmailContract.Labels.getLabelsUri(gmailAccount);
                return context.getContentResolver().query(labelsUri,null,null,null,null);
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
                return null;
            }
        }

        private ArrayList<InfoGmail> loadUnreadGmail(final Cursor cursor){
            try{
                if(cursor!=null && cursor.getCount()>0){
                    final String filter1 = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX;
                    final String filter2 = "^sq_ig_i";
                    //^sq_if_i... = Primary,Social,Promotion,Updates,Forums inbox

                    final int CANONICAL_NAME = cursor.getColumnIndexOrThrow(GmailContract.Labels.CANONICAL_NAME);
                    final int UNREAD = cursor.getColumnIndexOrThrow(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS);

                    gmailCount = 0;
                    cursor.moveToFirst();
                    do{
                        if(cursor.getString(CANONICAL_NAME).equals(filter1) || cursor.getString(CANONICAL_NAME).startsWith(filter2)){
                            if(!cursor.getString(UNREAD).equals("0")){
                                gmailCount += Integer.parseInt(cursor.getString(UNREAD));
                            }
                        }
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return null;
        }
    }
}