package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

public class SettingsProfilePin extends BaseActivity{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private LinearLayout lytProfileMusic, lytProfileLocation, lytProfileTime, lytProfileWifi, lytProfileBluetooth;
    private CheckBox cBoxProfilePinMusic, cBoxProfilePinLocation, cBoxProfilePinTime, cBoxProfilePinWifi, cBoxProfilePinBluetooth;
    private boolean isProfilePinSettings;

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.profile_pin);
        setBasicBackKeyAction();

        final Bundle b = getIntent().getExtras();
        if(b!=null){
            int extra = b.getInt(C.PROFILE_TYPE);
            if(extra==C.PROFILE_PIN_SETTINGS){
                isProfilePinSettings = true;
            }else if(extra==C.PROFILE_SMART_UNLOCK_SETTINGS){
                isProfilePinSettings = false;
            }
        }
        try{
            prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            lytProfileMusic = (LinearLayout)findViewById(R.id.lytProfileMusic);
            lytProfileLocation = (LinearLayout)findViewById(R.id.lytProfileLocation);
            lytProfileTime = (LinearLayout)findViewById(R.id.lytProfileTime);
            lytProfileWifi = (LinearLayout)findViewById(R.id.lytProfileWifi);
            lytProfileBluetooth = (LinearLayout)findViewById(R.id.lytProfileBluetooth);

            cBoxProfilePinMusic = (CheckBox)findViewById(R.id.cBoxProfilePinMusic);
            cBoxProfilePinLocation = (CheckBox)findViewById(R.id.cBoxProfilePinLocation);
            cBoxProfilePinTime = (CheckBox)findViewById(R.id.cBoxProfilePinTime);
            cBoxProfilePinWifi = (CheckBox)findViewById(R.id.cBoxProfilePinWifi);
            cBoxProfilePinBluetooth = (CheckBox)findViewById(R.id.cBoxProfilePinBluetooth);

            loadSettings();
            onClick();

            if(!isProfilePinSettings){
                lytProfileMusic.setVisibility(View.GONE);
                lytProfileTime.setVisibility(View.GONE);
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_profiles_pin;
    }

    private void loadSettings(){
        if(isProfilePinSettings){
            cBoxProfilePinMusic.setChecked(prefs.getBoolean("cBoxProfilePinMusic",true));
            cBoxProfilePinLocation.setChecked(prefs.getBoolean("cBoxProfilePinLocation",true));
            cBoxProfilePinTime.setChecked(prefs.getBoolean("cBoxProfilePinTime",true));
            cBoxProfilePinWifi.setChecked(prefs.getBoolean("cBoxProfilePinWifi",true));
            cBoxProfilePinBluetooth.setChecked(prefs.getBoolean("cBoxProfilePinBluetooth",true));
        }else{
            cBoxProfilePinMusic.setChecked(prefs.getBoolean("cBoxSmartMusicUnlock",false));
            cBoxProfilePinLocation.setChecked(prefs.getBoolean("cBoxSmartLocationUnlock",false));
            cBoxProfilePinTime.setChecked(prefs.getBoolean("cBoxSmartTimeUnlock",false));
            cBoxProfilePinWifi.setChecked(prefs.getBoolean("cBoxSmartWifiUnlock",false));
            cBoxProfilePinBluetooth.setChecked(prefs.getBoolean("cBoxSmartBluetoothUnlock",false));
        }
    }

    private void onClick(){
        lytProfileMusic.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxProfilePinMusic.performClick();
            }
        });
        lytProfileLocation.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxProfilePinLocation.performClick();
            }
        });
        lytProfileTime.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxProfilePinTime.performClick();
            }
        });
        lytProfileWifi.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxProfilePinWifi.performClick();
            }
        });
        lytProfileBluetooth.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxProfilePinBluetooth.performClick();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        saveSettings();
    }

    private void saveSettings(){
        if(isProfilePinSettings){
            editor.putBoolean("cBoxProfilePinMusic",cBoxProfilePinMusic.isChecked());
            editor.putBoolean("cBoxProfilePinLocation",cBoxProfilePinLocation.isChecked());
            editor.putBoolean("cBoxProfilePinTime",cBoxProfilePinTime.isChecked());
            editor.putBoolean("cBoxProfilePinWifi",cBoxProfilePinWifi.isChecked());
            editor.putBoolean("cBoxProfilePinBluetooth",cBoxProfilePinBluetooth.isChecked());
        }else{
            editor.putBoolean("cBoxSmartMusicUnlock",cBoxProfilePinMusic.isChecked());
            editor.putBoolean("cBoxSmartLocationUnlock",cBoxProfilePinLocation.isChecked());
            editor.putBoolean("cBoxSmartTimeUnlock",cBoxProfilePinTime.isChecked());
            editor.putBoolean("cBoxSmartWifiUnlock",cBoxProfilePinWifi.isChecked());
            editor.putBoolean("cBoxSmartBluetoothUnlock",cBoxProfilePinBluetooth.isChecked());
        }
        editor.commit();
    }
}
