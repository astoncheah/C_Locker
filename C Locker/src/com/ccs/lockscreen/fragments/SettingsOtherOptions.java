package com.ccs.lockscreen.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.DeviceAdminHandler;
import com.ccs.lockscreen_pro.ListAppsSelector;

import java.util.ArrayList;
import java.util.List;

public class SettingsOtherOptions extends Fragment{
    private static final int UNLOCK_SOUND = 1, LOCK_SOUND = 2, DEVICE_ADMIN_SET = 5, SELECTED_RINGTONE = 7;
    private Activity context;
    private Fragment fragment;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private LinearLayout
        lytBlockWidgetClick, lytReturnLS, lytUnlockToHome, lytNoLockShortcut, lytInactiveScreenOff, lytEnableProximitySensor,
        lytHomeKeyBypassMsg, lytHideEmergencyCall, lytHideForgotPin, lytHideNetworkCarrier, lytBackKeyUnlock, lytEnableKeyboardTyping,
        lytLockSound, lytUnlockSound, lytVibration, lytVolumeRockerScreenOff;
    private CheckBox
        cBoxReturnLS, cBoxUnlockToHome, cBoxNoLockShortcut, cBoxHomeKeyBypassMsg, cBoxHideEmergencyCall,
        cBoxHideForgotPin, cBoxHideNetworkCarrier, cBoxBackKeyUnlock, cBoxEnableKeyboardTyping, cBoxLockSound, cBoxUnlockSound,
        cBoxEnableVibration, cBoxInactiveScreenOff, cBoxEnableProximitySensor, cBoxVolumeControlScreenOff;
    private RadioButton cBoxServicePriorityNone, cBoxServicePriorityMid, cBoxServicePriorityHigh, cBoxMusicKeyMapType1, cBoxMusicKeyMapType2;
    private Spinner spnDelayLock, spnScreenTimeOut, spnScreenSystemTimeOut;
    private int intRingtone, lockSoundType, unlockSoundType;
    private String strRingtoneUri1, strRingtoneUri2;
    private DeviceAdminHandler deviceAdminHandler;

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case SELECTED_RINGTONE:
                    if(resultCode==Activity.RESULT_OK){
                        Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                        if(uri!=null){
                            if(intRingtone==LOCK_SOUND){
                                lockSoundType = C.SOUND_TYPE_CUSTOM_RINGTONE;
                                strRingtoneUri1 = uri.toString();
                            }else{
                                unlockSoundType = C.SOUND_TYPE_CUSTOM_RINGTONE;
                                strRingtoneUri2 = uri.toString();
                            }
                        }
                    }else{
                        if(intRingtone==LOCK_SOUND){
                            cBoxLockSound.setChecked(false);
                        }else{
                            cBoxUnlockSound.setChecked(false);
                        }
                    }
                    break;
                case DEVICE_ADMIN_SET:
                    if(resultCode==Activity.RESULT_OK){
                        //do nothing
                    }else{
                        cBoxInactiveScreenOff.setChecked(false);
                        cBoxEnableProximitySensor.setChecked(false);
                    }
                    break;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        fragment = this;
        final View view = inflater.inflate(R.layout.settings_other_options,container,false);

        lytBlockWidgetClick = view.findViewById(R.id.lytBlockWidgetClick);
        lytReturnLS = view.findViewById(R.id.lytReturnLS);
        lytUnlockToHome = view.findViewById(R.id.lytUnlockToHome);
        lytNoLockShortcut = view.findViewById(R.id.lytNoLockShortcut);
        lytInactiveScreenOff = view.findViewById(R.id.lytInactiveScreenOff);
        lytEnableProximitySensor = view.findViewById(R.id.lytEnableProximitySensor);
        lytHomeKeyBypassMsg = view.findViewById(R.id.lytHomeKeyBypassMsg);
        lytHideEmergencyCall = view.findViewById(R.id.lytHideEmergencyCall);
        lytHideForgotPin = view.findViewById(R.id.lytHideForgotPin);
        lytHideNetworkCarrier = view.findViewById(R.id.lytHideNetworkCarrier);
        lytBackKeyUnlock = view.findViewById(R.id.lytBackKeyUnlock);
        lytEnableKeyboardTyping = view.findViewById(R.id.lytEnableKeyboardTyping);
        lytLockSound = view.findViewById(R.id.lytLockSound);
        lytUnlockSound = view.findViewById(R.id.lytUnlockSound);
        lytVibration = view.findViewById(R.id.lytVibration);
        lytVolumeRockerScreenOff = view.findViewById(R.id.lytVolumeRockerScreenOff);

        spnDelayLock = view.findViewById(R.id.spnDelayLock);
        spnScreenTimeOut = view.findViewById(R.id.spinnerLockerTimeout);
        spnScreenSystemTimeOut = view.findViewById(R.id.spinnerScreenTimeoutSystem);

        cBoxReturnLS = view.findViewById(R.id.cBoxReturnLS);
        cBoxUnlockToHome = view.findViewById(R.id.cBoxUnlockToHome);
        cBoxNoLockShortcut = view.findViewById(R.id.cBoxNoLockShortcut);

        cBoxInactiveScreenOff = view.findViewById(R.id.cBoxInactiveScreenOff);
        cBoxEnableProximitySensor = view.findViewById(R.id.cBoxEnableProximitySensor);
        cBoxHomeKeyBypassMsg = view.findViewById(R.id.cBoxHomeKeyBypassMsg);
        cBoxHideEmergencyCall = view.findViewById(R.id.cBoxHideEmergencyCall);
        cBoxHideForgotPin = view.findViewById(R.id.cBoxHideForgotPin);
        cBoxHideNetworkCarrier = view.findViewById(R.id.cBoxHideNetworkCarrier);
        cBoxBackKeyUnlock = view.findViewById(R.id.cBoxBackKeyUnlock);
        cBoxEnableKeyboardTyping = view.findViewById(R.id.cBoxEnableKeyboardTyping);
        cBoxLockSound = view.findViewById(R.id.cBoxLockSound);
        cBoxUnlockSound = view.findViewById(R.id.cBoxUnlockSound);
        cBoxEnableVibration = view.findViewById(R.id.cBoxVibration);

        cBoxServicePriorityNone = view.findViewById(R.id.cBoxServicePriorityNone);
        cBoxServicePriorityMid = view.findViewById(R.id.cBoxServicePriorityMid);
        cBoxServicePriorityHigh = view.findViewById(R.id.cBoxServicePriorityHigh);

        cBoxVolumeControlScreenOff = view.findViewById(R.id.cBoxVolumeRockerScreenOff);
        cBoxMusicKeyMapType1 = view.findViewById(R.id.rBtnMusicKeyMap1);
        cBoxMusicKeyMapType2 = view.findViewById(R.id.rBtnMusicKeyMap2);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try{
            prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            deviceAdminHandler = new DeviceAdminHandler(context);
            spinnerFunction();
            onClickFunction();
            loadSettings();
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        //System.gc();
        super.onDestroy();
    }

    private void saveSettings(){
        int intDL = spnDelayLock.getSelectedItemPosition();
        editor.putInt("spnDelayLock",intDL);
        editor.putInt("intDelayLock",delayLock(intDL));

        int intSTO = spnScreenTimeOut.getSelectedItemPosition();
        editor.putInt("spinnerScreenTimeOut",intSTO);
        editor.putInt("intScreenTimeOut",setScreenTimeOut(intSTO));

        int intSSTO = spnScreenSystemTimeOut.getSelectedItemPosition();
        editor.putInt("spinnerScreenSystemTimeOut",intSSTO);
        editor.putInt("intScreenSystemTimeOut",setScreenTimeOut(intSSTO));

        editor.putBoolean("cBoxReturnLS",cBoxReturnLS.isChecked());
        editor.putBoolean("cBoxUnlockToHome",cBoxUnlockToHome.isChecked());
        editor.putBoolean("cBoxNoLockShortcut",cBoxNoLockShortcut.isChecked());

        editor.putBoolean("cBoxInactiveScreenOff",cBoxInactiveScreenOff.isChecked());
        editor.putBoolean("cBoxEnableProximitySensor",cBoxEnableProximitySensor.isChecked());

        editor.putBoolean("cBoxHomeKeyBypassMsg",cBoxHomeKeyBypassMsg.isChecked());
        editor.putBoolean("cBoxHideEmergencyCall",cBoxHideEmergencyCall.isChecked());
        editor.putBoolean("cBoxHideForgotPin",cBoxHideForgotPin.isChecked());
        editor.putBoolean("cBoxHideNetworkCarrier",cBoxHideNetworkCarrier.isChecked());
        editor.putBoolean("cBoxBackKeyUnlock",cBoxBackKeyUnlock.isChecked());
        editor.putBoolean("cBoxEnableKeyboardTyping",cBoxEnableKeyboardTyping.isChecked());
        editor.putBoolean("cBoxLockSound",cBoxLockSound.isChecked());
        editor.putBoolean("cBoxUnlockSound",cBoxUnlockSound.isChecked());
        editor.putBoolean("cBoxEnableVibration",cBoxEnableVibration.isChecked());
        editor.putBoolean("cBoxServicePriorityNone",cBoxServicePriorityNone.isChecked());
        editor.putBoolean("cBoxServicePriorityMid",cBoxServicePriorityMid.isChecked());
        editor.putBoolean("cBoxServicePriorityHigh",cBoxServicePriorityHigh.isChecked());

        editor.putBoolean("cBoxVolumeControlScreenOff",cBoxVolumeControlScreenOff.isChecked());
        editor.putBoolean("cBoxMusicKeyMapType1",cBoxMusicKeyMapType1.isChecked());
        editor.putBoolean("cBoxMusicKeyMapType2",cBoxMusicKeyMapType2.isChecked());

        editor.putInt("lockSoundType",lockSoundType);
        editor.putInt("unlockSoundType",unlockSoundType);

        editor.putString("strRingtoneUri1",strRingtoneUri1);
        editor.putString("strRingtoneUri2",strRingtoneUri2);
        editor.commit();
    }

    private int delayLock(int val){
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

    private int setScreenTimeOut(int intSTO){
        int no = 60000;
        switch(intSTO){
            case 0:
                no = C.FOLLOW_SYSTEM_TIMEOUT;
                break;
            case 1:
                no = 5000;
                break;
            case 2:
                no = 10000;
                break;
            case 3:
                no = 15000;//15sec
                break;
            case 4:
                no = 30000;
                break;
            case 5:
                no = 1000*60;//1min
                break;
            case 6:
                no = 1000*60*2;
                break;
            case 7:
                no = 1000*60*5;
                break;
            case 8:
                no = 1000*60*10;//10/min
                break;
            case 9:
                no = 1000*60*15;//15/min
                break;
            case 10:
                no = 1000*60*30;//30/min
                break;
            case 11:
                no = 1000*60*60;//1hr
                break;
            case 12:
                no = 1000*60*60*2;
                break;
            case 13:
                no = 1000*60*60*3;
                break;
        }
        return no;
    }

    private void spinnerFunction(){
        //spinnerScreenTimeOut
        List<String> list1 = new ArrayList<>();
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
        spnDelayLock.setAdapter(adt1);

        List<String> list2 = new ArrayList<>();
        list2.add(getString(R.string.follow_system_timeout));
        list2.add("5 "+getString(R.string.secs));
        list2.add("10 "+getString(R.string.secs));
        list2.add("15 "+getString(R.string.secs));
        list2.add("30 "+getString(R.string.secs));
        list2.add("1 "+getString(R.string.min));
        list2.add("2 "+getString(R.string.mins));
        list2.add("5 "+getString(R.string.mins));
        list2.add("10 "+getString(R.string.mins));
        list2.add("15 "+getString(R.string.mins));
        list2.add("30 "+getString(R.string.mins));
        list2.add("1 "+getString(R.string.hr));
        list2.add("2 "+getString(R.string.hrs));
        list2.add("3 "+getString(R.string.hrs));
        final ArrayAdapter<String> adt2 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,list2);
        adt2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnScreenTimeOut.setAdapter(adt2);
        spnScreenSystemTimeOut.setAdapter(adt2);
    }

    private void onClickFunction(){
        lytBlockWidgetClick.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(context,ListAppsSelector.class);
                i.putExtra(C.LIST_APPS_ID,DataAppsSelection.APP_TYPE_WIDGET_BLOCK);
                startActivity(i);
            }
        });
        lytReturnLS.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxReturnLS.performClick();
            }
        });
        lytUnlockToHome.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxUnlockToHome.performClick();
            }
        });
        lytNoLockShortcut.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxNoLockShortcut.performClick();
            }
        });
        lytInactiveScreenOff.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxInactiveScreenOff.performClick();
            }
        });
        lytEnableProximitySensor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxEnableProximitySensor.performClick();
            }
        });
        lytHomeKeyBypassMsg.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxHomeKeyBypassMsg.performClick();
            }
        });
        lytHideEmergencyCall.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxHideEmergencyCall.performClick();
            }
        });
        lytHideForgotPin.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxHideForgotPin.performClick();
            }
        });
        lytHideNetworkCarrier.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxHideNetworkCarrier.performClick();
            }
        });
        lytBackKeyUnlock.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxBackKeyUnlock.performClick();
            }
        });
        lytEnableKeyboardTyping.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxEnableKeyboardTyping.performClick();
            }
        });
        lytLockSound.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxLockSound.performClick();
            }
        });
        lytUnlockSound.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxUnlockSound.performClick();
            }
        });
        lytVibration.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxEnableVibration.performClick();
            }
        });
        lytVolumeRockerScreenOff.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxVolumeControlScreenOff.performClick();
            }
        });

        cBoxLockSound.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxLockSound.isChecked()){
                    try{
                        dialogUnlockSound(LOCK_SOUND);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        cBoxUnlockSound.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxUnlockSound.isChecked()){
                    try{
                        dialogUnlockSound(UNLOCK_SOUND);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        cBoxServicePriorityNone.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                //cBoxServicePriorityNone.setChecked(false);
                cBoxServicePriorityMid.setChecked(false);
                cBoxServicePriorityHigh.setChecked(false);
            }
        });
        cBoxServicePriorityMid.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxServicePriorityNone.setChecked(false);
                //cBoxServicePriorityMid.setChecked(false);
                cBoxServicePriorityHigh.setChecked(false);
            }
        });
        cBoxServicePriorityHigh.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxServicePriorityNone.setChecked(false);
                cBoxServicePriorityMid.setChecked(false);
                //cBoxServicePriorityHigh.setChecked(false);
            }
        });
        cBoxInactiveScreenOff.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxInactiveScreenOff.isChecked()){
                    if(!deviceAdminHandler.isDeviceAdminActivated()){
                        deviceAdminHandler.launchDeviceAdmin(context,DEVICE_ADMIN_SET);
                    }
                }
            }
        });
        cBoxEnableProximitySensor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEnableProximitySensor.isChecked()){
                    if(!deviceAdminHandler.isDeviceAdminActivated()){
                        deviceAdminHandler.launchDeviceAdmin(context,DEVICE_ADMIN_SET);
                    }
                }
            }
        });
        cBoxMusicKeyMapType1.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxMusicKeyMapType2.setChecked(false);
            }
        });
        cBoxMusicKeyMapType2.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxMusicKeyMapType1.setChecked(false);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void loadSettings(){
        spnDelayLock.setSelection(prefs.getInt("spnDelayLock",0));
        spnScreenTimeOut.setSelection(prefs.getInt("spinnerScreenTimeOut",6));
        spnScreenSystemTimeOut.setSelection(prefs.getInt("spinnerScreenSystemTimeOut",0));

        cBoxReturnLS.setChecked(prefs.getBoolean("cBoxReturnLS",true));
        cBoxUnlockToHome.setChecked(prefs.getBoolean("cBoxUnlockToHome",false));
        cBoxNoLockShortcut.setChecked(prefs.getBoolean("cBoxNoLockShortcut",false));
        cBoxHomeKeyBypassMsg.setChecked(prefs.getBoolean("cBoxHomeKeyBypassMsg",true));
        cBoxHideEmergencyCall.setChecked(prefs.getBoolean("cBoxHideEmergencyCall",false));
        cBoxHideForgotPin.setChecked(prefs.getBoolean("cBoxHideForgotPin",false));
        cBoxHideNetworkCarrier.setChecked(prefs.getBoolean("cBoxHideNetworkCarrier",false));
        cBoxBackKeyUnlock.setChecked(prefs.getBoolean("cBoxBackKeyUnlock",false));
        cBoxEnableKeyboardTyping.setChecked(prefs.getBoolean("cBoxEnableKeyboardTyping",false));
        cBoxLockSound.setChecked(prefs.getBoolean("cBoxLockSound",false));
        cBoxUnlockSound.setChecked(prefs.getBoolean("cBoxUnlockSound",false));
        cBoxEnableVibration.setChecked(prefs.getBoolean("cBoxEnableVibration",true));
        cBoxServicePriorityNone.setChecked(prefs.getBoolean("cBoxServicePriorityNone",false));
        cBoxServicePriorityMid.setChecked(prefs.getBoolean("cBoxServicePriorityMid",true));
        cBoxServicePriorityHigh.setChecked(prefs.getBoolean("cBoxServicePriorityHigh",false));
        if(!deviceAdminHandler.isDeviceAdminActivated()){
            cBoxInactiveScreenOff.setChecked(false);
            cBoxEnableProximitySensor.setChecked(false);
        }else{
            cBoxInactiveScreenOff.setChecked(prefs.getBoolean("cBoxInactiveScreenOff",true));
            cBoxEnableProximitySensor.setChecked(prefs.getBoolean("cBoxEnableProximitySensor",true));
        }

        cBoxVolumeControlScreenOff.setChecked(prefs.getBoolean("cBoxVolumeControlScreenOff",false));
        cBoxMusicKeyMapType1.setChecked(prefs.getBoolean("cBoxMusicKeyMapType1",true));
        cBoxMusicKeyMapType2.setChecked(prefs.getBoolean("cBoxMusicKeyMapType2",false));

        lockSoundType = prefs.getInt("lockSoundType",C.SOUND_TYPE_CLICK);
        unlockSoundType = prefs.getInt("unlockSoundType",C.SOUND_TYPE_CLICK);

        strRingtoneUri1 = prefs.getString("strRingtoneUri1","");
        strRingtoneUri2 = prefs.getString("strRingtoneUri2","");

        int naviKeysH = C.getNaviKeyHeight(context);
        if(naviKeysH==0 || C.isAndroid(Build.VERSION_CODES.O)){
            lytEnableKeyboardTyping.setVisibility(View.GONE);
        }
    }

    private void dialogUnlockSound(int no){
        intRingtone = no;
        final String[] items = new String[]{getString(R.string.sound_type_1),getString(R.string.sound_type_2),getString(R.string.sound_system_ringtone),getString(R.string.select_from_file)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.select_dialog_item,items);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.ringtone_title));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                try{
                    if(item==0){
                        if(intRingtone==LOCK_SOUND){
                            lockSoundType = C.SOUND_TYPE_CLICK;
                        }else{
                            unlockSoundType = C.SOUND_TYPE_CLICK;
                        }
                        am.playSoundEffect(AudioManager.FX_KEY_CLICK,(float)1.5);
                    }else if(item==1){
                        if(intRingtone==LOCK_SOUND){
                            lockSoundType = C.SOUND_TYPE_PRESS;
                        }else{
                            unlockSoundType = C.SOUND_TYPE_PRESS;
                        }
                        am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD,(float)1.5);
                    }else if(item==2){
                        if(intRingtone==LOCK_SOUND){
                            lockSoundType = C.SOUND_TYPE_SYSTEM_RINGTONE;
                        }else{
                            unlockSoundType = C.SOUND_TYPE_SYSTEM_RINGTONE;
                        }
                        final Ringtone ringtone = RingtoneManager.getRingtone(context,RingtoneManager.getActualDefaultRingtoneUri(context,RingtoneManager.TYPE_NOTIFICATION));
                        ringtone.play();
                    }else if(item==3){
                        final AlertDialog.Builder b = new AlertDialog.Builder(context);
                        b.setMessage(getString(R.string.ringtone_notice));
                        b.setTitle(getString(R.string.dialog_title));
                        b.setPositiveButton(getString(android.R.string.ok),new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog,int id){
                                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,getString(R.string.ringtone_title));
                                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,false);
                                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,false);
                                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
                                fragment.startActivityForResult(intent,SELECTED_RINGTONE);
                            }
                        });
                        final AlertDialog alert = b.create();
                        alert.show();
                    }
                }catch(Exception e){
                    Toast.makeText(context,"Function not supported",Toast.LENGTH_LONG).show();
                    new MyCLocker(context).saveErrorLog(null,e);
                }
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                if(intRingtone==LOCK_SOUND){
                    cBoxLockSound.setChecked(false);
                }else{
                    cBoxUnlockSound.setChecked(false);
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}