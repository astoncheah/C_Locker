package com.ccs.lockscreen.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen.security.AutoSelfie;
import com.ccs.lockscreen.security.AutoSelfie2;
import com.ccs.lockscreen.utils.ComponentSetting;
import com.ccs.lockscreen.utils.DeviceAdminHandler;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;
import com.ccs.lockscreen_pro.ListAppsSelector;
import com.ccs.lockscreen_pro.ListInstalledApps;
import com.ccs.lockscreen_pro.SettingsHomeKey;
import com.ccs.lockscreen_pro.SettingsPasswordUnlock;
import com.ccs.lockscreen_pro.SettingsSecurityRoot;
import com.ccs.lockscreen_pro.SettingsSecuritySelfie;

import java.util.ArrayList;
import java.util.List;

public class SettingsSecurity extends Fragment implements OnDialogListener{
    public static final int HOME_BLOCK_SERVICE_ACCESS = 1, HOME_BLOCK_SET_LAUNCHER = 2;
    private static final int //request code
        //long press home
        SELECTED_SEARCH_LONG_PRESS = 1, CHECK_SEARCH_LONG_PRESS_DEFAULT_RESULT = 2, SELECTED_C_LOCKER_SEARCH_LONG_PRESS = 3, //system settings
        SELECTED_SYSTEM_SETTINGS = 4, CHECK_SYSTEM_SETTINGS_DEFAULT_RESULT = 5, SELECTED_C_LOCKER_SYSTEM_SETTINGS = 6, //
        UNLOCK_PIN_SET = 7, DEVICE_ADMIN_SET = 8, SERVICE_ACCESS_BLOCK_RECENTAPPS = 9,SERVICE_ACCESS_BLOCK_OTHER_APPS = 10;
    private static final int IS_DEFAULT_CLEARED = 1, IS_DEFAULT_OTHER = 2, IS_DEFAULT_C_LOCKER = 3;
    private Activity context;
    private Fragment fragment;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private SaveData mSaveData = null;
    private MyCLocker mLocker;
    private String
        //====== Long Press Dialogs
        DIALOG_SELECT_SEARCH_LONG_PRESS_APP = "", DIALOG_CLEAR_OTHER_SEARCH_LONG_PRESS_DEFAULT = "", DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT = "", //====== Settings Dialogs
        DIALOG_SELECT_SETTINGS_APP = "", DIALOG_CLEAR_OTHER_SETTINGS_DEFAULT = "", DIALOG_SELECT_SETTINGS_DEFAULT = "";
    private int securityType;
    private View lytPassUnlock, lytSecuritySelfie, lytSetDefaultLockerNone, lytBlockStatusbar, lytHidePinAnimation, lytActivateDeviceAdmin, lytDisableHomeButton, lytDisableSearchLongPress, lytBlockSystemSettings, lytBlockRecentApps, lytBlockNoneShortcutApps, lytAdvancedRoot;
    private CheckBox cBoxEnablePassUnlock, cBoxBlockStatusbar, cBoxHidePinAnimation, cBoxActivateDeviceAdmin, cBoxDisableSearchLongPress, cBoxBlockSystemSettings, cBoxBlockRecentApps, cBoxBlockNoneShortcutApps;
    private Spinner spnDelayPin;
    private EditText editTxtSmsUnlock;
    private AlertDialog alert;
    private ProgressDialog progressBar;
    private DeviceAdminHandler deviceAdminHandler;

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                //====== Long Press Dialogs
                case SELECTED_SEARCH_LONG_PRESS:
                    if(resultCode==Activity.RESULT_OK){
                        new ComponentSetting(context).setSearchLongPress(true,"SELECTED_SELECTED_SEARCH_LONG_PRESS_true");
                        if(getDefaultType(getSearchLongPressDefault(),"SELECTED_SEARCH_LONG_PRESS>IS_DEFAULT_CLEARED")==IS_DEFAULT_CLEARED){
                            dialog(DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT);
                        }else if(getDefaultType(getSearchLongPressDefault(),"SELECTED_SEARCH_LONG_PRESS>IS_DEFAULT_OTHER")==IS_DEFAULT_OTHER){
                            dialog(DIALOG_CLEAR_OTHER_SEARCH_LONG_PRESS_DEFAULT);
                        }
                    }else{
                        cBoxDisableSearchLongPress.setChecked(false);
                        new ComponentSetting(context).setSearchLongPress(false,"SELECTED_SEARCH_LONG_PRESS");
                    }
                    break;
                case CHECK_SEARCH_LONG_PRESS_DEFAULT_RESULT:
                    if(getDefaultType(getSearchLongPressDefault(),"CHECK_SEARCH_LONG_PRESS_DEFAULT_RESULT")==IS_DEFAULT_CLEARED){
                        dialog(DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT);
                    }else{
                        cBoxDisableSearchLongPress.setChecked(false);
                        new ComponentSetting(context).setSearchLongPress(false,"CHECK_SEARCH_LONG_PRESS_DEFAULT_RESULT");
                    }
                    break;
                case SELECTED_C_LOCKER_SEARCH_LONG_PRESS:
                    if(getDefaultType(getSearchLongPressDefault(),"SELECTED_C_LOCKER_SEARCH_LONG_PRESS")!=IS_DEFAULT_C_LOCKER){
                        cBoxDisableSearchLongPress.setChecked(false);
                        new ComponentSetting(context).setSearchLongPress(false,"SELECTED_C_LOCKER_SEARCH_LONG_PRESS");
                    }
                    break;
                //====== System Settings Dialogs
                case SELECTED_SYSTEM_SETTINGS:
                    if(resultCode==Activity.RESULT_OK){
                        new ComponentSetting(context).setSystemSettings(true,"SELECTED_SYSTEM_SETTINGS_true");
                        if(getDefaultType(getSystemSettingsDefault(),"SELECTED_SYSTEM_SETTINGS>IS_DEFAULT_CLEARED")==IS_DEFAULT_CLEARED){
                            dialog(DIALOG_SELECT_SETTINGS_DEFAULT);
                        }else if(getDefaultType(getSystemSettingsDefault(),"SELECTED_SYSTEM_SETTINGS>IS_DEFAULT_OTHER")==IS_DEFAULT_OTHER){
                            dialog(DIALOG_CLEAR_OTHER_SETTINGS_DEFAULT);
                        }
                    }else{
                        cBoxBlockSystemSettings.setChecked(false);
                        new ComponentSetting(context).setSystemSettings(false,"SELECTED_SEARCH_LONG_PRESS");
                    }
                    break;
                case CHECK_SYSTEM_SETTINGS_DEFAULT_RESULT:
                    if(getDefaultType(getSystemSettingsDefault(),"CHECK_SYSTEM_SETTINGS_DEFAULT_RESULT")==IS_DEFAULT_CLEARED){
                        dialog(DIALOG_SELECT_SETTINGS_DEFAULT);
                    }else{
                        cBoxBlockSystemSettings.setChecked(false);
                        new ComponentSetting(context).setSystemSettings(false,"CHECK_SYSTEM_SETTINGS_DEFAULT_RESULT");
                    }
                    break;
                case SELECTED_C_LOCKER_SYSTEM_SETTINGS:
                    if(getDefaultType(getSystemSettingsDefault(),"SELECTED_C_LOCKER_SYSTEM_SETTINGS")!=IS_DEFAULT_C_LOCKER){
                        cBoxBlockSystemSettings.setChecked(false);
                        new ComponentSetting(context).setSystemSettings(false,"SELECTED_C_LOCKER_SYSTEM_SETTINGS");
                    }
                    break;
                //
                case UNLOCK_PIN_SET:
                    if(resultCode==Activity.RESULT_OK){
                        final Bundle extras = data.getExtras();
                        this.securityType = extras.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_PIN);

                        cBoxEnablePassUnlock.setChecked(true);
                        final DataAppsSelection db = new DataAppsSelection(context);
                        final List<InfoAppsSelection> appsList = db.getApps(DataAppsSelection.APP_TYPE_SHORTCUT);
                        try{
                            for(InfoAppsSelection item : appsList){
                                if(item.getShortcutId()==DataAppsSelection.SHORTCUT_APP_01 && item.getShortcutAction()==DataAppsSelection.ACTION_DEFAULT){
                                    db.setProfile(item.getShortcutProfile());// dont miss context!!
                                    db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);

                                }else if(item.getShortcutAction()==DataAppsSelection.ACTION_UNLOCK){
                                    db.setProfile(item.getShortcutProfile());// dont miss context!!
                                    db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                                }
                            }
                        }catch(Exception e){
                            mLocker.saveErrorLog(null,e);
                        }finally{
                            Log.e("UNLOCK_PIN_SET","db.close()");
                            db.close();
                        }
                    }else{
                        clearSecurity();
                    }
                    break;
                case DEVICE_ADMIN_SET:
                    if(resultCode!=Activity.RESULT_OK){
                        cBoxActivateDeviceAdmin.setChecked(false);
                    }
                    break;
                case SERVICE_ACCESS_BLOCK_RECENTAPPS:
                    if(C.isAccessibilityEnabled(context)){
                        cBoxBlockRecentApps.setChecked(true);
                    }
                    break;
                case SERVICE_ACCESS_BLOCK_OTHER_APPS:
                    if(C.isAccessibilityEnabled(context)){
                        cBoxBlockNoneShortcutApps.setChecked(true);
                        Intent i = new Intent(context,ListAppsSelector.class);
                        i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                        startActivity(i);
                    }
                    /*boolean isPermission = C.isGetRunningTaskPermission(context);
                    if(isPermission){
                        cBoxBlockNoneShortcutApps.setChecked(true);
                        Intent i = new Intent(context,ListAppsSelector.class);
                        i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                        startActivity(i);
                    }*/
                    break;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        fragment = this;
        final View view = inflater.inflate(R.layout.settings_security,container,false);
        //
        DIALOG_SELECT_SEARCH_LONG_PRESS_APP = getString(R.string.select_long_press_app_desc);
        DIALOG_CLEAR_OTHER_SEARCH_LONG_PRESS_DEFAULT = getString(R.string.clear_launcher_default_desc)+"..";//must add ".." to avoid same strings
        DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT = getString(R.string.select_launcher_default_desc)+"..";//must add ".." to avoid same strings
        //
        DIALOG_SELECT_SETTINGS_APP = getString(R.string.block_settings_selection);
        DIALOG_CLEAR_OTHER_SETTINGS_DEFAULT = getString(R.string.clear_settings_default_desc);
        DIALOG_SELECT_SETTINGS_DEFAULT = getString(R.string.select_settings_default_desc);

        lytSecuritySelfie = view.findViewById(R.id.lytSecuritySelfie);
        lytPassUnlock = view.findViewById(R.id.lytPinUnlock);
        lytSetDefaultLockerNone = view.findViewById(R.id.lytSetDefaultLockerNone);
        lytBlockStatusbar = view.findViewById(R.id.lytBlockStatusbar);
        lytHidePinAnimation = view.findViewById(R.id.lytHidePinAnimation);
        lytActivateDeviceAdmin = view.findViewById(R.id.lytActivateDeviceAdmin);
        lytDisableHomeButton = view.findViewById(R.id.lytDisableHomeButton);
        lytDisableSearchLongPress = view.findViewById(R.id.lytDisableSearchLongPress);
        lytBlockSystemSettings = view.findViewById(R.id.lytBlockSystemSettings);
        lytBlockRecentApps = view.findViewById(R.id.lytBlockRecentApps);
        lytBlockNoneShortcutApps = view.findViewById(R.id.lytBlockNoneShortcutApps);
        lytAdvancedRoot = view.findViewById(R.id.lytAdvancedRoot);

        cBoxEnablePassUnlock = view.findViewById(R.id.cBoxEnablePasswordUnlock);
        cBoxBlockStatusbar = view.findViewById(R.id.cBoxBlockStatusbar);
        cBoxHidePinAnimation = view.findViewById(R.id.cBoxHidePinAnimation);
        cBoxActivateDeviceAdmin = view.findViewById(R.id.cBoxActivateDeviceAdmin);
        cBoxDisableSearchLongPress = view.findViewById(R.id.cBoxDisableSearchLongPress);
        cBoxBlockSystemSettings = view.findViewById(R.id.cBoxBlockSystemSettings);
        cBoxBlockRecentApps = view.findViewById(R.id.cBoxBlockRecentApps);
        cBoxBlockNoneShortcutApps = view.findViewById(R.id.cBoxBlockNoneShortcutApps);

        spnDelayPin = view.findViewById(R.id.spnDelayPin);
        editTxtSmsUnlock = view.findViewById(R.id.editTxtSmsUnlock);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try{
            prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            mSaveData = new SaveData(context);
            mLocker = new MyCLocker(context);

            deviceAdminHandler = new DeviceAdminHandler(context);

            cBoxFunction();
            layoutFunction();
            spinnerFunction();
            loadSettings();
            context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            refreshSettings();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public void onDestroy(){
        try{
            if(alert!=null && alert.isShowing()){
                alert.dismiss();
            }
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        if(mLocker!=null){
            mLocker.close();
        }
        super.onDestroy();
    }

    private void saveSettings(){
        //editor.putBoolean("cBoxDisableHomeButton", cBoxDisableHomeButton.isChecked());
        int intDL = spnDelayPin.getSelectedItemPosition();
        editor.putInt("spnDelayPin",intDL);
        editor.putInt("intDelayPin",delayPIN(intDL));

        editor.putBoolean("cBoxEnablePassUnlock",cBoxEnablePassUnlock.isChecked());
        editor.putBoolean("cBoxBlockStatusbar",cBoxBlockStatusbar.isChecked());
        editor.putBoolean("cBoxHidePinAnimation",cBoxHidePinAnimation.isChecked());
        editor.putBoolean("cBoxBlockRecentApps",cBoxBlockRecentApps.isChecked());
        editor.putBoolean("cBoxBlockNoneShortcutApps",cBoxBlockNoneShortcutApps.isChecked());
        editor.putString("emergencyUnlock",editTxtSmsUnlock.getText().toString());
        editor.putInt(C.SECURITY_TYPE,securityType);
        editor.commit();
    }

    private int delayPIN(int val){
        switch(val){
            case 1:
                return 5000;
            case 2:
                return 10000;
            case 3:
                return 15000;
            case 4:
                return 30000;
            case 5:
                return 60000;
            case 6:
                return 2*60*1000;
            case 7:
                return 5*60*1000;
            case 8:
                return 10*60*1000;
            case 9:
                return 15*60*1000;
            case 10:
                return 30*60*1000;
            default:
                return 0;
        }
    }

    private void cBoxFunction(){
        cBoxEnablePassUnlock.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEnablePassUnlock.isChecked()){
                    //dialogSetSecurity();
                    cBoxEnablePassUnlock.setChecked(false);    //in case any incoming activity causing error..
                    new MyAlertDialog(context).selectSecurityUnlockType(SettingsSecurity.this);
                }else{
                    clearSecurity();
                }
            }
        });
        cBoxActivateDeviceAdmin.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxActivateDeviceAdmin.isChecked()){
                    deviceAdminHandler.launchDeviceAdmin(context,DEVICE_ADMIN_SET);
                }else{
                    deviceAdminHandler.removeDeviceAdmin();
                    lytActivateDeviceAdmin.setBackgroundColor(Color.parseColor("#66ff0000"));
                }
            }
        });
        cBoxDisableSearchLongPress.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxDisableSearchLongPress.isChecked()){
                    dialog(DIALOG_SELECT_SEARCH_LONG_PRESS_APP);
                }else{
                    new ComponentSetting(context).setSearchLongPress(false,"cBoxDisableSearchLongPress");
                }
            }
        });
        cBoxBlockSystemSettings.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxBlockSystemSettings.isChecked()){
                    dialog(DIALOG_SELECT_SETTINGS_APP);
                }else{
                    new ComponentSetting(context).setSystemSettings(false,"cBoxBlockSystemSettings");
                }
            }
        });
        cBoxBlockRecentApps.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(C.isAccessibilityEnabled(context)){
                    //return;
                }else{
                    if(cBoxBlockRecentApps.isChecked()){
                        cBoxBlockRecentApps.setChecked(false);
                        openServiceAccess(SERVICE_ACCESS_BLOCK_RECENTAPPS);
                    }
                }

                /*if(cBoxBlockRecentApps.isChecked()){
                    if(!cBoxEnablePassUnlock.isChecked()){
                        cBoxBlockRecentApps.setChecked(false);
                        Toast.makeText(context,getString(R.string.required_pin),Toast.LENGTH_LONG).show();
                    }
                }*/
            }
        });
        cBoxBlockNoneShortcutApps.setOnClickListener(new OnClickListener(){
            @SuppressLint("InlinedApi")
            @Override
            public void onClick(View v){
                if(C.isAccessibilityEnabled(context)){
                    if(cBoxBlockNoneShortcutApps.isChecked()){
                        Intent i = new Intent(context,ListAppsSelector.class);
                        i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                        startActivity(i);
                    }
                    //return;
                }else{
                    if(cBoxBlockNoneShortcutApps.isChecked()){
                        cBoxBlockNoneShortcutApps.setChecked(false);
                        openServiceAccess(SERVICE_ACCESS_BLOCK_OTHER_APPS);
                    }
                }

                /*if(cBoxBlockNoneShortcutApps.isChecked()){
                    if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP_MR1)){
                        try{
                            boolean isPermission = C.isGetRunningTaskPermission(context);
                            if(isPermission){
                                i = new Intent(context,ListAppsSelector.class);
                                i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                                startActivity(i);
                            }else{
                                cBoxBlockNoneShortcutApps.setChecked(false);
                                i = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                fragment.startActivityForResult(i,ACTION_USAGE_ACCESS_SETTINGS);
                            }
                        }catch(Exception e){
                            cBoxBlockNoneShortcutApps.setChecked(false);
                            Toast.makeText(context,"feature not supported, please email the developer",Toast.LENGTH_LONG).show();
                            mLocker.saveErrorLog(null,e);
                        }
                    }else{
                        if(!cBoxEnablePassUnlock.isChecked()){
                            cBoxBlockNoneShortcutApps.setChecked(false);
                            Toast.makeText(context,getString(R.string.required_pin),Toast.LENGTH_LONG).show();
                        }else{/*//*//*
                            i = new Intent(context,ListAppsSelector.class);
                            i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_BLOCKED_APPS);
                            startActivity(i);
                        }
                    }
                }*/
            }
        });
    }

    private void layoutFunction(){
        lytSetDefaultLockerNone.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                try{
                    Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                    startActivity(intent);
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }
        });
        lytPassUnlock.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxEnablePassUnlock.performClick();
            }
        });
        lytSecuritySelfie.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(!cBoxEnablePassUnlock.isChecked()){
                    Toast.makeText(context,R.string.required_pin,Toast.LENGTH_LONG).show();
                    return;
                }
                if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                    if(!new AutoSelfie2(context).isFrontFacingCameraSupported()){
                        Toast.makeText(context,R.string.no_front_camera,Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    if(!new AutoSelfie(context).isFrontFacingCameraSupported()){
                        Toast.makeText(context,R.string.no_front_camera,Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Intent i = new Intent(context,SettingsSecuritySelfie.class);
                startActivity(i);
            }
        });
        lytBlockStatusbar.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxBlockStatusbar.performClick();
            }
        });
        lytHidePinAnimation.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxHidePinAnimation.performClick();
            }
        });
        lytActivateDeviceAdmin.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxActivateDeviceAdmin.performClick();
            }
        });
        lytDisableHomeButton.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(C.hasNaviKey(context)){
                    final Intent i = new Intent(context,SettingsHomeKey.class);
                    startActivity(i);
                }else{
                    new MyAlertDialog(context).selectBlockKey(new OnDialogListener(){
                        @Override
                        public void onDialogClickListener(int dialogType,int result){
                            switch(result){
                                case HOME_BLOCK_SERVICE_ACCESS:
                                    openServiceAccess(-1);
                                    break;
                                case HOME_BLOCK_SET_LAUNCHER:
                                    final Intent i = new Intent(context,SettingsHomeKey.class);
                                    startActivity(i);
                                    break;
                            }
                        }
                    });
                }
            }
        });
        lytDisableSearchLongPress.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxDisableSearchLongPress.performClick();
            }
        });
        lytBlockSystemSettings.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxBlockSystemSettings.performClick();
            }
        });
        lytBlockRecentApps.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxBlockRecentApps.performClick();
            }
        });
        lytBlockNoneShortcutApps.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxBlockNoneShortcutApps.performClick();
            }
        });
        lytAdvancedRoot.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(context,SettingsSecurityRoot.class);
                startActivity(i);
            }
        });
    }
    private void openServiceAccess(int requestCode){
        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
        fragment.startActivityForResult(intent,requestCode);

        new MyAlertDialog(context).showServiceDialog(R.string.access_desc_enable);
    }
    private void spinnerFunction(){
        //spinnerScreenTimeOut
        final List<String> list1 = new ArrayList<>();
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_now));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_5_sec));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_10_sec));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_15_sec));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_30_sec));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_1_min));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_2_min));
        list1.add(getString(R.string.other_option_shortcuts_delay_lock_5_min));
        list1.add(getString(R.string.delay_10_min));
        list1.add(getString(R.string.delay_15_min));
        list1.add(getString(R.string.delay_30_min));
        final ArrayAdapter<String> adt1 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,list1);
        adt1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnDelayPin.setAdapter(adt1);
    }

    private void loadSettings(){
        cBoxEnablePassUnlock.setChecked(prefs.getBoolean("cBoxEnablePassUnlock",false));
        cBoxBlockStatusbar.setChecked(prefs.getBoolean("cBoxBlockStatusbar",true));
        cBoxHidePinAnimation.setChecked(prefs.getBoolean("cBoxHidePinAnimation",false));

        spnDelayPin.setSelection(prefs.getInt("spnDelayPin",0));
        editTxtSmsUnlock.setText(prefs.getString("emergencyUnlock",""));
        securityType = prefs.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_NONE);

        refreshSettings();
        if(C.isAccessibilityEnabled(context)){
            cBoxBlockRecentApps.setChecked(prefs.getBoolean("cBoxBlockRecentApps",false));
            cBoxBlockNoneShortcutApps.setChecked(prefs.getBoolean("cBoxBlockNoneShortcutApps",false));
        }else{
            cBoxBlockRecentApps.setChecked(false);
            cBoxBlockNoneShortcutApps.setChecked(false);
        }
    }

    private void clearSecurity(){
        //cBoxBlockRecentApps.setChecked(false);
        //cBoxBlockNoneShortcutApps.setChecked(false);
        securityType = C.SECURITY_UNLOCK_NONE;
        startProgressbar();
        new Thread(){
            public void run(){
                int intLockStyle = prefs.getInt("intLockStyle",C.SIMPLE_UNLOCK);
                if(intLockStyle==C.FINGERPRINT_UNLOCK){
                    editor.putInt("intLockStyle",C.SIMPLE_UNLOCK);
                }
                //PIN
                editor.putInt("intPass2nd01",-1);
                editor.putInt("intPass2nd02",-1);
                editor.putInt("intPass2nd03",-1);
                editor.putInt("intPass2nd04",-1);
                editor.putInt("intPass2nd05",-1);
                editor.putInt("intPass2nd06",-1);
                editor.putInt("intPass2nd07",-1);
                editor.putInt("intPass2nd08",-1);
                //pattern
                editor.putString(C.PATTERN_LOCK,"");
                editor.commit();
                if(mSaveData.isLockerRunning()){
                    mSaveData.setKillLocker(true);
                }
                DataAppsSelection db = new DataAppsSelection(context);
                List<InfoAppsSelection> appsList = db.getApps(DataAppsSelection.APP_TYPE_SHORTCUT);
                Log.e("clearSecurity","db.start(): ");
                try{
                    for(InfoAppsSelection item : appsList){
                        Log.e("clearSecurity","db.item(): "+item.getId());
                        db.setProfile(C.PROFILE_DEFAULT);
                        db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        db.setProfile(C.PROFILE_MUSIC);
                        db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        db.setProfile(C.PROFILE_LOCATION);
                        db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        db.setProfile(C.PROFILE_TIME);
                        db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        db.setProfile(C.PROFILE_WIFI);
                        db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        db.setProfile(C.PROFILE_BLUETOOTH);
                        db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                    }
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }finally{
                    Log.e("clearSecurity","db.close()");
                    db.close();
                }
                if(progressBar!=null && progressBar.isShowing()){
                    progressBar.dismiss();
                }
            }
        }.start();
    }

    private void dialog(final String str){
        String strNo = getString(android.R.string.cancel);
        String strOk = getString(R.string.next);
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(str);
            if(str.equals(DIALOG_SELECT_SEARCH_LONG_PRESS_APP)){
                builder.setTitle(getString(R.string.select_long_press_app_title));
            }else if(str.equals(DIALOG_CLEAR_OTHER_SEARCH_LONG_PRESS_DEFAULT)){
                builder.setTitle(getString(R.string.clear_settings_default_title));
            }else if(str.equals(DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT)){
                builder.setTitle(getString(R.string.select_launcher_default_title));
                //====== Settings Dialogs
            }else if(str.equals(DIALOG_SELECT_SETTINGS_APP)){
                builder.setTitle(getString(R.string.select_long_press_app_title));
            }else if(str.equals(DIALOG_CLEAR_OTHER_SETTINGS_DEFAULT)){
                builder.setTitle(getString(R.string.clear_settings_default_title));
            }else if(str.equals(DIALOG_SELECT_SETTINGS_DEFAULT)){
                builder.setTitle(getString(R.string.select_launcher_default_title));
            }
            builder.setNegativeButton(strNo,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){
                    if(str.equals(DIALOG_SELECT_SEARCH_LONG_PRESS_APP) ||
                        str.equals(DIALOG_CLEAR_OTHER_SEARCH_LONG_PRESS_DEFAULT) ||
                        str.equals(DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT)){
                        cBoxDisableSearchLongPress.setChecked(false);
                        new ComponentSetting(context).setSearchLongPress(false,"dialog");
                    }else if(str.equals(DIALOG_SELECT_SETTINGS_APP) ||
                        str.equals(DIALOG_CLEAR_OTHER_SETTINGS_DEFAULT) ||
                        str.equals(DIALOG_SELECT_SETTINGS_DEFAULT)){
                        new ComponentSetting(context).setSystemSettings(false,"dialog");
                    }
                }
            });
            builder.setPositiveButton(strOk,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){
                    //====== Long Press
                    if(str.equals(DIALOG_SELECT_SEARCH_LONG_PRESS_APP)){
                        final Intent i = new Intent(context,ListInstalledApps.class);
                        i.putExtra(C.LIST_APPS_ID,C.LIST_SEARCH_LONG_PRESS);
                        fragment.startActivityForResult(i,SELECTED_SEARCH_LONG_PRESS);
                    }else if(str.equals(DIALOG_CLEAR_OTHER_SEARCH_LONG_PRESS_DEFAULT)){
                        runAppDetails(getSearchLongPressDefault(),CHECK_SEARCH_LONG_PRESS_DEFAULT_RESULT);
                    }else if(str.equals(DIALOG_SELECT_SEARCH_LONG_PRESS_APP_DEFAULT)){
                        Intent i = new Intent(Intent.ACTION_ASSIST);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivityForResult(i,SELECTED_C_LOCKER_SEARCH_LONG_PRESS);
                        //====== Settings Dialogs
                    }else if(str.equals(DIALOG_SELECT_SETTINGS_APP)){
                        final Intent i = new Intent(context,ListInstalledApps.class);
                        i.putExtra(C.LIST_APPS_ID,C.LIST_SETTINGS);
                        fragment.startActivityForResult(i,SELECTED_SYSTEM_SETTINGS);
                    }else if(str.equals(DIALOG_CLEAR_OTHER_SETTINGS_DEFAULT)){
                        runAppDetails(getSystemSettingsDefault(),CHECK_SYSTEM_SETTINGS_DEFAULT_RESULT);
                    }else if(str.equals(DIALOG_SELECT_SETTINGS_DEFAULT)){
                        Intent i = new Intent(Settings.ACTION_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        fragment.startActivityForResult(i,SELECTED_C_LOCKER_SYSTEM_SETTINGS);
                    }
                }
            });
            alert = builder.create();
            alert.show();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void refreshSettings(){
        if(C.isAccessibilityEnabled(context)){
            lytDisableHomeButton.setBackgroundColor(0);
            lytDisableSearchLongPress.setVisibility(View.GONE);
            lytBlockSystemSettings.setVisibility(View.GONE);
            lytAdvancedRoot.setVisibility(View.GONE);

            if(getDefaultType(getLauncherDefault(),"refreshSettings>setHomeLauncher")!=IS_DEFAULT_C_LOCKER){
                new ComponentSetting(context).setHomeLauncher(false,"");// important!!
            }
            new ComponentSetting(context).setSearchLongPress(false,"");// important!!
            new ComponentSetting(context).setSystemSettings(false,"");// important!!
        }else{
            if(C.isAndroid(Build.VERSION_CODES.M)){
                lytDisableSearchLongPress.setVisibility(View.GONE);
                lytBlockSystemSettings.setVisibility(View.GONE);
                lytAdvancedRoot.setVisibility(View.GONE);
            }else{
                lytDisableSearchLongPress.setVisibility(View.VISIBLE);
                lytBlockSystemSettings.setVisibility(View.VISIBLE);
                lytAdvancedRoot.setVisibility(View.VISIBLE);
            }

            if(getDefaultType(getLauncherDefault(),"refreshSettings>cBoxDisableHomeButton")==IS_DEFAULT_C_LOCKER){
                lytDisableHomeButton.setBackgroundColor(0);
            }else{
                new ComponentSetting(context).setHomeLauncher(false,"refreshSettings>setHomeLauncher");// important!!
                lytDisableHomeButton.setBackgroundColor(Color.parseColor("#66ff0000"));
            }
            if(getDefaultType(getSearchLongPressDefault(),"refreshSettings>cBoxDisableSearchLongPress")==IS_DEFAULT_C_LOCKER){
                cBoxDisableSearchLongPress.setChecked(true);
            }else{
                new ComponentSetting(context).setSearchLongPress(false,"refreshSettings>setSearchLongPress");// important!!
                cBoxDisableSearchLongPress.setChecked(false);
            }
            if(getDefaultType(getSystemSettingsDefault(),"refreshSettings>getSystemSettingsDefault")==IS_DEFAULT_C_LOCKER){
                cBoxBlockSystemSettings.setChecked(true);
            }else{
                new ComponentSetting(context).setSystemSettings(false,"refreshSettings>setSystemSettings");// important!!
                cBoxBlockSystemSettings.setChecked(false);
            }
        }
        if(deviceAdminHandler.isDeviceAdminActivated()){
            cBoxActivateDeviceAdmin.setChecked(true);
            lytActivateDeviceAdmin.setBackgroundColor(0);
        }else{
            cBoxActivateDeviceAdmin.setChecked(false);
            lytActivateDeviceAdmin.setBackgroundColor(Color.parseColor("#66ff0000"));
        }
    }

    private void startProgressbar(){
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(false);
        progressBar.setMessage(getString(R.string.configuring));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    private void runAppDetails(String pkgName,final int result){
        final Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:"+pkgName));
        fragment.startActivityForResult(i,result);
    }

    private String getSearchLongPressDefault(){
        final Intent i = new Intent(Intent.ACTION_ASSIST);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        return context.getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    private String getSystemSettingsDefault(){
        final Intent i = new Intent(Settings.ACTION_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        return context.getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    private int getDefaultType(String appDefault,String msg){
        mLocker.writeToFile("SettingsSecurity>getDefaultType: "+appDefault+"/"+msg);
        final String getMyPackageName = context.getPackageName();
        if(appDefault.equals("android") ||
            appDefault.equals("org.cyanogenmod.resolver") ||
            appDefault.equals("com.huawei.android.internal.app")){
            // SettingsSecurity,SettingsHomeKey,ServiceMain,Locker classes all need to change together!!!!!
            return IS_DEFAULT_CLEARED;
        }else if(appDefault.equals(getMyPackageName)){
            return IS_DEFAULT_C_LOCKER;
        }else{
            return IS_DEFAULT_OTHER;
        }
    }

    private String getLauncherDefault(){
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        return context.getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
    }

    @Override
    public void onDialogClickListener(int dialogType,int result){
        if(dialogType==MyAlertDialog.OK){
            int securityType;
            if(result==0){//pin
                securityType = C.SECURITY_UNLOCK_PIN;
            }else{//pattern
                securityType = C.SECURITY_UNLOCK_PATTERN;
            }
            final Intent i = new Intent(context,SettingsPasswordUnlock.class);
            i.putExtra(C.SECURITY_TYPE,securityType);
            fragment.startActivityForResult(i,UNLOCK_PIN_SET);
        }else{
            clearSecurity();
        }
    }
}