package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.fragments.FragmentShortcutList;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.viewpagerindicator.TabPageIndicator;

public class SettingsShortcutList extends BaseActivity{
    private AlertDialog alert;
    private ImageView imgMap;
    private boolean isColorWhite;
    private ViewPager pager;
    private Runnable runBlink = new Runnable(){
        @Override
        public void run(){
            try{
                if(isColorWhite){
                    isColorWhite = false;
                    //viewTips.setTextColor(Color.WHITE);
                    imgMap.setBackgroundColor(Color.parseColor("#00000000"));
                }else{
                    isColorWhite = true;
                    //viewTips.setTextColor(Color.RED);
                    imgMap.setBackgroundColor(Color.parseColor("#66ff0000"));
                }
                imgMap.postDelayed(runBlink,1500);
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.shortcut_profile);
        setBasicBackKeyAction();
        addContentView(addLayout(),new LinearLayout.LayoutParams(-1,-1));//-1 = LayoutParams.MATCH_PARENT

        imgMap = new ImageView(this);
        imgMap.setLayoutParams(new LinearLayout.LayoutParams(C.dpToPx(this,64),-1));
        imgMap.setPadding(C.dpToPx(this,10),C.dpToPx(this,10),C.dpToPx(this,10),C.dpToPx(this,10));
        imgMap.setImageResource(R.drawable.ic_location_on_white_48dp);
        imgMap.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                if(!isLocationProfileEnabled()){
                    //Intent i = new Intent(SettingsShortcutList.this,SettingsProfile.class);
                    //startActivity(i);
                    //TODO
                }else{
                    Intent i = new Intent(SettingsShortcutList.this,SettingsMap.class);
                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onDestroy(){
        try{
            if(alert!=null && alert.isShowing()){
                alert.dismiss();
            }
        }catch(Exception e){
            //saveErrorLogs(null,e);// no need this to prevent FC
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
        pager = new ViewPager(this);
        pager.setId(R.id.action0);//ramdom id to prevent error.
        pager.setLayoutParams(new LinearLayout.LayoutParams(-1,-1));
        //pager.setCurrentItem(0);
        pager.setOffscreenPageLimit(4);
        pager.setAdapter(adapter);

        int color = ContextCompat.getColor(getBaseContext(),R.color.colorPrimary);//color same as toolbar
        final TabPageIndicator indicator = new TabPageIndicator(this);
        indicator.setTextHeight(30);
        indicator.setTextSize(16);
        indicator.setViewPager(pager,0,this,Typeface.DEFAULT);
        indicator.setBackgroundColor(color);
        indicator.setWhiteTab();

        final Toolbar toolbar = getToolbar();
        lytMain.addView(toolbar);
        lytMain.addView(indicator);
        lytMain.addView(pager);
        return lytMain;
    }

    private boolean isLocationProfileEnabled(){
        final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        return prefs.getBoolean("cBoxProfileLocation",false);
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            imgMap.removeCallbacks(runBlink);
            imgMap.setBackgroundColor(Color.parseColor("#00000000"));
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            if(!isLocationProfileEnabled()){
                imgMap.removeCallbacks(runBlink);
                imgMap.post(runBlink);

            }
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
        inflater.inflate(R.menu.settings_shortcut_list_menu,menu);

        MenuItem item = menu.getItem(0);//map
        item.setActionView(imgMap);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.itemShortcutListHelp:
                dialogTips();
                break;
            case R.id.itemSelectAllPin:
                return false;
            case R.id.itemDeselectAllPin:
                return false;
            case R.id.itemCopyFromLockerProfile:
                return false;
            case R.id.itemCopyFromMusicProfile:
                return false;
            case R.id.itemCopyFromLocationProfile:
                return false;
            case R.id.itemResetShortcuts:
                return false;
            case R.id.itemResetAllProfileShortcuts:
                return false;
        }
        return true;
    }

    private void dialogTips(){
        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LinearLayout lyt = new LinearLayout(this);
            lyt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
            lyt.setPadding(0,C.dpToPx(this,10),0,C.dpToPx(this,10));
            lyt.setOrientation(LinearLayout.VERTICAL);
            lyt.addView(addTxt(getString(R.string.shortcut_gesture3_horizotal)));
            lyt.addView(addTxt(getString(R.string.shortcut_gesture3_vertical)));
            builder.setTitle(getString(R.string.shortcut_profile)+" "+getString(R.string.settings_widget_help));
            builder.setView(lyt);
            alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private TextView addTxt(String str){
        final TextView txt = new TextView(this);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        txt.setPadding(C.dpToPx(this,15),C.dpToPx(this,10),C.dpToPx(this,15),C.dpToPx(this,10));
        txt.setText(str);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        //txt.setBackgroundColor(Color.parseColor("#66000000"));
        return txt;
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
                    return FragmentShortcutList.newInstance(C.PROFILE_DEFAULT);
                case 1:
                    return FragmentShortcutList.newInstance(C.PROFILE_MUSIC);
                case 2:
                    return FragmentShortcutList.newInstance(C.PROFILE_LOCATION);
                case 3:
                    return FragmentShortcutList.newInstance(C.PROFILE_TIME);
                case 4:
                    return FragmentShortcutList.newInstance(C.PROFILE_WIFI);
                default:
                    return FragmentShortcutList.newInstance(C.PROFILE_BLUETOOTH);
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