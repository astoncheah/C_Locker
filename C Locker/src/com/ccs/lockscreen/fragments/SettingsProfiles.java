package com.ccs.lockscreen.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen_pro.ListSavedBluetooth;
import com.ccs.lockscreen_pro.ListSavedWifi;
import com.ccs.lockscreen_pro.SettingsAddLocation;
import com.ccs.lockscreen_pro.SettingsProfilePin;
import com.ccs.lockscreen_pro.SettingsProfilePriority;
import com.ccs.lockscreen_pro.SettingsShortcutList;
import com.ccs.lockscreen_pro.SettingsWidgets;

public class SettingsProfiles extends Fragment{
    private Activity context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private LinearLayout lytWidgetProfiles, lytShortcutProfiles, lytProfileMusic, lytProfileLocation, lytProfileTime, lytProfileWifi, lytProfileBluetooth, lytProfilePriority, lytProfilePin, lytProfileSmartUnlock;
    private CheckBox cBoxProfileDefault, cBoxProfileMusic, cBoxProfileLocation, cBoxProfileTime, cBoxProfileWifi, cBoxProfileBluetooth;
    private ImageView imgMap, imgTime, imgWifi, imgBluetooth;
    private ProgressDialog progressBar;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        final View view = inflater.inflate(R.layout.settings_profiles,container,false);

        lytWidgetProfiles = (LinearLayout)view.findViewById(R.id.lytWidgetProfiles);
        lytShortcutProfiles = (LinearLayout)view.findViewById(R.id.lytShortcutProfiles);
        lytProfileMusic = (LinearLayout)view.findViewById(R.id.lytProfileMusic);
        lytProfileLocation = (LinearLayout)view.findViewById(R.id.lytProfileLocation);
        lytProfileTime = (LinearLayout)view.findViewById(R.id.lytProfileTime);
        lytProfileWifi = (LinearLayout)view.findViewById(R.id.lytProfileWifi);
        lytProfileBluetooth = (LinearLayout)view.findViewById(R.id.lytProfileBluetooth);
        lytProfilePriority = (LinearLayout)view.findViewById(R.id.lytProfilePriority);
        lytProfilePin = (LinearLayout)view.findViewById(R.id.lytProfilePin);
        lytProfileSmartUnlock = (LinearLayout)view.findViewById(R.id.lytProfileSmartUnlock);

        cBoxProfileDefault = (CheckBox)view.findViewById(R.id.cBoxProfileDefault);
        cBoxProfileMusic = (CheckBox)view.findViewById(R.id.cBoxProfileMusic);
        cBoxProfileLocation = (CheckBox)view.findViewById(R.id.cBoxProfileLocation);
        cBoxProfileTime = (CheckBox)view.findViewById(R.id.cBoxProfileTime);
        cBoxProfileWifi = (CheckBox)view.findViewById(R.id.cBoxProfileWifi);
        cBoxProfileBluetooth = (CheckBox)view.findViewById(R.id.cBoxProfileBluetooth);

        imgMap = (ImageView)view.findViewById(R.id.imgMap);
        imgTime = (ImageView)view.findViewById(R.id.imgTime);
        imgWifi = (ImageView)view.findViewById(R.id.imgWifi);
        imgBluetooth = (ImageView)view.findViewById(R.id.imgBluetooth);

        cBoxProfileDefault.setChecked(true);
        cBoxProfileDefault.setEnabled(false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try{
            prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            myAlertDialog = new MyAlertDialog(context);
            onClickFunction();
            loadSettings();
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        cBoxProfileLocation.setChecked(prefs.getBoolean("cBoxProfileLocation",false));
    }

    @Override
    public void onPause(){
        super.onPause();
        saveSettings();
    }

    @Override
    public void onDestroy(){
        try{
            myAlertDialog.close();
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void saveSettings(){
        editor.putBoolean("cBoxProfileMusic",cBoxProfileMusic.isChecked());
        editor.putBoolean("cBoxProfileLocation",cBoxProfileLocation.isChecked());
        editor.putBoolean("cBoxProfileTime",cBoxProfileTime.isChecked());
        editor.putBoolean("cBoxProfileWifi",cBoxProfileWifi.isChecked());
        editor.putBoolean("cBoxProfileBluetooth",cBoxProfileBluetooth.isChecked());
        editor.commit();
    }

    private void onClickFunction(){
        lytWidgetProfiles.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(context,SettingsWidgets.class);
                startActivity(i);
            }
        });
        lytShortcutProfiles.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(context,SettingsShortcutList.class);
                startActivity(i);
            }
        });
        lytProfileMusic.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxProfileMusic.performClick();
            }
        });
        lytProfileLocation.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxProfileLocation.performClick();
            }
        });
        lytProfileTime.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxProfileTime.performClick();
            }
        });
        lytProfileWifi.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxProfileWifi.performClick();
            }
        });
        lytProfileBluetooth.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxProfileBluetooth.performClick();
            }
        });
        lytProfilePriority.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(context,SettingsProfilePriority.class);
                startActivity(i);
            }
        });
        lytProfilePin.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(context,SettingsProfilePin.class);
                i.putExtra(C.PROFILE_TYPE,C.PROFILE_PIN_SETTINGS);
                startActivity(i);
            }
        });
        lytProfileSmartUnlock.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(context,SettingsProfilePin.class);
                i.putExtra(C.PROFILE_TYPE,C.PROFILE_SMART_UNLOCK_SETTINGS);
                startActivity(i);
            }
        });

        cBoxProfileLocation.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxProfileLocation.isChecked()){
                    imgMap.performClick();
                }
            }
        });
        cBoxProfileTime.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxProfileTime.isChecked()){
                    imgTime.performClick();
                }
            }
        });
        cBoxProfileWifi.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxProfileWifi.isChecked()){
                    imgWifi.performClick();
                }
            }
        });
        cBoxProfileBluetooth.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxProfileBluetooth.isChecked()){
                    imgBluetooth.performClick();
                }
            }
        });

        imgMap.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(context,SettingsAddLocation.class);
                startActivity(i);
            }
        });
        imgTime.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                myAlertDialog.timePicker();
            }
        });
        imgWifi.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                final WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager.isWifiEnabled()){
                    Intent i = new Intent(context,ListSavedWifi.class);
                    startActivity(i);
                }else{
                    myAlertDialog.simpleMsg(context.getString(R.string.enable_wifi));
                    if(prefs.getString("strTrustedWifi","").equals("")){
                        cBoxProfileWifi.setChecked(false);
                    }
                }
            }
        });
        imgBluetooth.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()){
                    Intent i = new Intent(context,ListSavedBluetooth.class);
                    startActivity(i);
                }else{
                    myAlertDialog.simpleMsg(context.getString(R.string.enable_bluetooth));
                    if(prefs.getString("strTrustedBluetooth","").equals("")){
                        cBoxProfileBluetooth.setChecked(false);
                    }
                }
            }
        });
    }

    private void loadSettings(){
        cBoxProfileMusic.setChecked(prefs.getBoolean("cBoxProfileMusic",false));
        cBoxProfileTime.setChecked(prefs.getBoolean("cBoxProfileTime",false));
        cBoxProfileWifi.setChecked(prefs.getBoolean("cBoxProfileWifi",false));
        cBoxProfileBluetooth.setChecked(prefs.getBoolean("cBoxProfileBluetooth",false));
    }
}