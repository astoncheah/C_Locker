package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.fragments.FragmentWidgetProfile;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyViewPager;
import com.viewpagerindicator.TabPageIndicator;

public class SettingsWidgets extends BaseActivity{
    private MyViewPager pager;
    private AlertDialog alert;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals(context.getPackageName()+C.SCROLLING_ENABLE)){
                pager.setScrollingEnabled(true);
            }else if(action.equals(context.getPackageName()+C.SCROLLING_DISABLE)){
                pager.setScrollingEnabled(false);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.widget_profile);
        setBasicBackKeyAction();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        addContentView(addLayout(),new LinearLayout.LayoutParams(-1,-1));//-1 = LayoutParams.MATCH_PARENT
        dialogTips();
    }

    @Override
    public void onDestroy(){
        try{
            if(alert!=null && alert.isShowing()){
                alert.dismiss();
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return 0;
    }

    @SuppressLint("InflateParams")
    private LinearLayout addLayout(){
        final LinearLayout lytMain = new LinearLayout(this);
        lytMain.setOrientation(LinearLayout.VERTICAL);

        final FragmentPagerAdapter adapter = new ProfileAdapter(getSupportFragmentManager());
        pager = new MyViewPager(this);
        pager.setId(R.id.action0);//ramdom id to prevent error.
        pager.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
        //pager.setCurrentItem(0);//default 2nd page then change to 1st page onresume
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(adapter);

        int color = ContextCompat.getColor(this,R.color.colorPrimary);//color same as toolbar
        final TabPageIndicator indicator = new TabPageIndicator(this);
        indicator.setTextHeight(30);
        indicator.setTextSize(16);
        indicator.setViewPager(pager,0,this,Typeface.DEFAULT);
        indicator.setBackgroundColor(color);
        indicator.setWhiteTab();

        //final Toolbar toolbar = getToolbar();
        //lytMain.addView(toolbar);
        lytMain.addView(indicator);
        lytMain.addView(pager);
        return lytMain;
    }

    private void dialogTips(){
        try{
            new MyAlertDialog(this).tips(getString(R.string.settings_widget_dialog)+" "+getString(R.string.settings_widget_help),getString(R.string.widget_settings_tips));
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            unregisterReceiver(mReceiver);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(getPackageName()+C.SCROLLING_ENABLE);
            filter.addAction(getPackageName()+C.SCROLLING_DISABLE);
            registerReceiver(mReceiver,filter);

            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    //pager.setCurrentItem(0);
                }
            },1000);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_widgets_menu,menu);

        MenuItem item = menu.getItem(0);
        Drawable icon = ContextCompat.getDrawable(this,R.drawable.ic_info_outline_white_24dp);
        item.setIcon(C.getScaledMenuIcon(this,icon));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.itemWidgetHelp:
                dialogTips();
                break;
        }
        return true;
    }

    //class
    private class ProfileAdapter extends FragmentPagerAdapter{
        private int pageCount = viewPagerTitle().length;

        public ProfileAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            switch(position){
                case 0:
                    return FragmentWidgetProfile.newInstance(C.PROFILE_DEFAULT);
                case 1:
                    return FragmentWidgetProfile.newInstance(C.PROFILE_MUSIC);
                case 2:
                    return FragmentWidgetProfile.newInstance(C.PROFILE_LOCATION);
                case 3:
                    return FragmentWidgetProfile.newInstance(C.PROFILE_TIME);
                case 4:
                    return FragmentWidgetProfile.newInstance(C.PROFILE_WIFI);
                default:
                    return FragmentWidgetProfile.newInstance(C.PROFILE_BLUETOOTH);
            }
        }

        @Override
        public int getCount(){
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position){
            return viewPagerTitle()[position%viewPagerTitle().length];
        }

        private String[] viewPagerTitle(){
            return new String[]{getString(R.string.fragment_locker_profile),getString(R.string.fragment_music_profile),getString(R.string.fragment_location_profile),getString(R.string.fragment_time_profile),getString(R.string.fragment_wifi_profile),getString(R.string.fragment_bluetooth_profile)};
        }
    }
}