package com.ccs.lockscreen_pro;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.ProfileHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SettingsProfilePriority extends BaseActivity{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private LinearLayout lytProfileMain, lytProfileMusic, lytProfileLocation, lytProfileTime, lytProfileWifi, lytProfileBluetooth;
    private Button btnProfileMusic, btnProfileLocation, btnProfileTime, btnProfileWifi, btnProfileBluetooth;
    private String profilePriority;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.profile_prority);
        setBasicBackKeyAction();
        try{
            prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            lytProfileMain = (LinearLayout)findViewById(R.id.lytProfileMain);
            lytProfileMusic = (LinearLayout)findViewById(R.id.lytProfileMusic);
            lytProfileLocation = (LinearLayout)findViewById(R.id.lytProfileLocation);
            lytProfileTime = (LinearLayout)findViewById(R.id.lytProfileTime);
            lytProfileWifi = (LinearLayout)findViewById(R.id.lytProfileWifi);
            lytProfileBluetooth = (LinearLayout)findViewById(R.id.lytProfileBluetooth);

            lytProfileMusic.setId(ProfileHandler.ID_MUSIC);
            lytProfileLocation.setId(ProfileHandler.ID_LOCATION);
            lytProfileTime.setId(ProfileHandler.ID_TIME);
            lytProfileWifi.setId(ProfileHandler.ID_WIFI);
            lytProfileBluetooth.setId(ProfileHandler.ID_BLUETOOTH);

            btnProfileMusic = (Button)findViewById(R.id.btnProfileMusic);
            btnProfileLocation = (Button)findViewById(R.id.btnProfileLocation);
            btnProfileTime = (Button)findViewById(R.id.btnProfileTime);
            btnProfileWifi = (Button)findViewById(R.id.btnProfileWifi);
            btnProfileBluetooth = (Button)findViewById(R.id.btnProfileBluetooth);

            loadSettings();
            onClick();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_profiles_priority;
    }

    private void loadSettings(){
        profilePriority = prefs.getString("profilePriority",null);
        if(profilePriority!=null){
            final String[] items = profilePriority.split(";");
            //if(items.length==ProfileHandler.TOTAL_PROFILES){ TODO
            for(int i = 0; i<items.length; i++){
                final Integer id = Integer.parseInt(items[i].toString());
                final View v = getLayoutView(id);
                if(v==null){
                    break;
                }
                if(i==0){
                    lytProfileMain.removeAllViews();
                }
                lytProfileMain.addView(v);
            }
            //}
        }
        final LayoutTransition layoutTransition = new LayoutTransition();
        lytProfileMain.setLayoutTransition(layoutTransition);
        setNumber();
    }

    private void onClick(){
        btnProfileMusic.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setViewPosition(lytProfileMusic);
            }
        });
        btnProfileLocation.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setViewPosition(lytProfileLocation);
            }
        });
        btnProfileTime.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setViewPosition(lytProfileTime);
            }
        });
        btnProfileWifi.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setViewPosition(lytProfileWifi);
            }
        });
        btnProfileBluetooth.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setViewPosition(lytProfileBluetooth);
            }
        });
    }

    private View getLayoutView(int id){
        switch(id){
            case ProfileHandler.ID_MUSIC:
                return lytProfileMusic;
            case ProfileHandler.ID_LOCATION:
                return lytProfileLocation;
            case ProfileHandler.ID_TIME:
                return lytProfileTime;
            case ProfileHandler.ID_WIFI:
                return lytProfileWifi;
            case ProfileHandler.ID_BLUETOOTH:
                return lytProfileBluetooth;
        }
        return null;
    }

    private void setNumber(){
        for(int i = 0; i<lytProfileMain.getChildCount(); i++){
            if(lytProfileMain.getChildAt(i) instanceof LinearLayout){
                final LinearLayout child1 = (LinearLayout)lytProfileMain.getChildAt(i);
                final LinearLayout child2 = (LinearLayout)child1.getChildAt(0);
                final TextView txt = (TextView)child2.getChildAt(0);
                txt.setText((i+1)+". ");
            }
        }
    }

    private void setViewPosition(View v){
        int position = getViewPosition(v.getId());
        if(position>0){
            lytProfileMain.removeView(v);
            lytProfileMain.addView(v,(position-1));
        }
        setNumber();
    }

    private int getViewPosition(int id){
        for(int i = 0; i<lytProfileMain.getChildCount(); i++){
            final View v = lytProfileMain.getChildAt(i);
            if(v instanceof LinearLayout){
                if(v.getId()==id){
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void onPause(){
        super.onPause();
        saveSettings();
    }

    private void saveSettings(){
        final List<Integer> listViewId = new ArrayList<Integer>();
        for(int i = 0; i<lytProfileMain.getChildCount(); i++){
            final View v = lytProfileMain.getChildAt(i);
            if(v instanceof LinearLayout){
                listViewId.add(v.getId());
            }
        }
        final StringBuilder strbul = new StringBuilder();
        final Iterator<Integer> iter = listViewId.iterator();
        while(iter.hasNext()){
            strbul.append(iter.next());
            if(iter.hasNext()){
                strbul.append(";");
            }
        }
        editor.putString("profilePriority",strbul.toString());
        editor.commit();
    }
}
