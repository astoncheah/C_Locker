package com.ccs.lockscreen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.util.Calendar;

public class ProfileHandler{
    public static final int ID_DEFAULT = 1;
    public static final int ID_MUSIC = 2;
    public static final int ID_LOCATION = 3;
    public static final int ID_TIME = 4;
    public static final int ID_WIFI = 5;
    public static final int ID_BLUETOOTH = 6;
    private Context context;
    private SharedPreferences prefs;
    //private SharedPreferences.Editor editor;
    private MyCLocker mLocker;
    private boolean cBoxProfilePinMusic, cBoxProfilePinLocation, cBoxProfilePinTime, cBoxProfilePinWifi, cBoxProfilePinBluetooth;
    private boolean cBoxProfileMusic, cBoxProfileLocation, cBoxProfileTime, cBoxProfileWifi, cBoxProfileBluetooth;
    private String profilePriority;
    private OnPriorityCheck cb;
    //public static final int TOTAL_PROFILES	= 5;//!!! change this if you add new profile TODO

    public ProfileHandler(Context context,OnPriorityCheck cb){
        this.context = context;
        this.cb = cb;
        mLocker = new MyCLocker(context);
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        //editor = context.getSharedPreferences(C.PREFS_NAME, 0).edit();
        cBoxProfilePinMusic = prefs.getBoolean("cBoxProfilePinMusic",true);
        cBoxProfilePinLocation = prefs.getBoolean("cBoxProfilePinLocation",true);
        cBoxProfilePinTime = prefs.getBoolean("cBoxProfilePinTime",true);
        cBoxProfilePinWifi = prefs.getBoolean("cBoxProfilePinWifi",true);
        cBoxProfilePinBluetooth = prefs.getBoolean("cBoxProfilePinBluetooth",true);

        cBoxProfileMusic = prefs.getBoolean("cBoxProfileMusic",false);
        cBoxProfileLocation = prefs.getBoolean("cBoxProfileLocation",false);
        cBoxProfileTime = prefs.getBoolean("cBoxProfileTime",false);
        cBoxProfileWifi = prefs.getBoolean("cBoxProfileWifi",false);
        cBoxProfileBluetooth = prefs.getBoolean("cBoxProfileBluetooth",false);

        profilePriority = prefs.getString("profilePriority",null);

        //mLocker.writeToFile("ProfileHandler: "+cBoxProfileMusic+"/"+cBoxProfileLocation+"/"+cBoxProfileTime+"/"+cBoxProfileWifi);
    }

    public final void runPriority(){
        if(profilePriority!=null){
            final String[] items = profilePriority.split(";");
            //mLocker.writeToFile(C.FILE_BACKUP_RECORD,"ProfileHandler>runPriority>items: "+profilePriority);
            //if(items.length==TOTAL_PROFILES){
            for(int i = 0; i<items.length; i++){
                final Integer id = Integer.parseInt(items[i].toString());
                switch(id){
                    case ID_MUSIC:
                        if(isProfileMusic()){
                            cb.onProfilePriorityCallBack(id);
                            return;
                        }
                        break;
                    case ID_LOCATION:
                        if(isProfileLocation()){
                            cb.onProfilePriorityCallBack(id);
                            return;
                        }
                        break;
                    case ID_TIME:
                        if(isProfileTime()){
                            cb.onProfilePriorityCallBack(id);
                            return;
                        }
                        break;
                    case ID_WIFI:
                        if(isProfileWifi()){
                            cb.onProfilePriorityCallBack(id);
                            return;
                        }
                        break;
                    case ID_BLUETOOTH:
                        if(isProfileBluetooth()){
                            cb.onProfilePriorityCallBack(id);
                            return;
                        }
                        break;
                }
            }
            //}
        }else{//default priority
            if(isProfileMusic()){
                cb.onProfilePriorityCallBack(ID_MUSIC);
                return;
            }
            if(isProfileLocation()){
                cb.onProfilePriorityCallBack(ID_LOCATION);
                return;
            }
            if(isProfileTime()){
                cb.onProfilePriorityCallBack(ID_TIME);
                return;
            }
            if(isProfileWifi()){
                cb.onProfilePriorityCallBack(ID_WIFI);
                return;
            }
            if(isProfileBluetooth()){
                cb.onProfilePriorityCallBack(ID_BLUETOOTH);
                return;
            }
        }
        cb.onProfilePriorityCallBack(ID_DEFAULT);
    }

    @SuppressWarnings("deprecation")
    private boolean isProfileMusic(){
        try{
            if(cBoxProfileMusic){
                final AudioManager aMgr = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                if(aMgr.isMusicActive() || aMgr.isWiredHeadsetOn()){
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"isProfileMusic: "+aMgr.isMusicActive()+"/"+aMgr.isWiredHeadsetOn());
                    return true;
                }
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return false;
    }

    private boolean isProfileLocation(){
        if(!cBoxProfileLocation){
            return false;
        }
        return prefs.getBoolean("isProfileLocationActive",false);
    }

    private boolean isProfileTime(){
        if(!cBoxProfileTime){
            return false;
        }
        if(!isTimeProfileDaySelected()){
            return false;
        }
        int timePickerStartHour = prefs.getInt("timePickerStartHour",0);
        int timePickerStartMin = prefs.getInt("timePickerStartMin",0);

        int timePickerEndHour = prefs.getInt("timePickerEndHour",0);
        int timePickerEndMin = prefs.getInt("timePickerEndMin",0);

        final Calendar c = Calendar.getInstance();
        int hr = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);

        int totalTimeStart = (timePickerStartHour*100)+timePickerStartMin;
        int totalTimeEnd = (timePickerEndHour*100)+timePickerEndMin;
        int totalTime = (hr*100)+min;

        //mLocker.writeToFile("Locker>isTimeProfileTimeSelected 0: "+totalTimeStart+"/"+totalTimeEnd+"/"+totalTime);
        if(totalTimeStart<totalTimeEnd){
            if(totalTime>=totalTimeStart && totalTime<=totalTimeEnd){
                return true;
            }
        }
        if(totalTimeStart>totalTimeEnd){
            if(totalTime>=totalTimeStart || totalTime<=totalTimeEnd){
                return true;
            }
        }
        return false;
    }

    private boolean isProfileWifi(){
        if(!cBoxProfileWifi){
            return false;
        }
        return prefs.getBoolean("isProfileWifiActive",false);
    }

    private boolean isProfileBluetooth(){
        if(!cBoxProfileBluetooth){
            return false;
        }
        return prefs.getBoolean("isProfileBluetoothActive",false);
    }

    //
    private boolean isTimeProfileDaySelected(){
        boolean cBoxMon = prefs.getBoolean("timeProfileMon",true);
        boolean cBoxTue = prefs.getBoolean("timeProfileTue",true);
        boolean cBoxWed = prefs.getBoolean("timeProfileWed",true);
        boolean cBoxThu = prefs.getBoolean("timeProfileThu",true);
        boolean cBoxFri = prefs.getBoolean("timeProfileFri",true);
        boolean cBoxSat = prefs.getBoolean("timeProfileSat",true);
        boolean cBoxSun = prefs.getBoolean("timeProfileSun",true);

        final Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);
        switch(day){
            case Calendar.MONDAY:
                if(cBoxMon){
                    return true;
                }
                break;
            case Calendar.TUESDAY:
                if(cBoxTue){
                    return true;
                }
                break;
            case Calendar.WEDNESDAY:
                if(cBoxWed){
                    return true;
                }
                break;
            case Calendar.THURSDAY:
                if(cBoxThu){
                    return true;
                }
                break;
            case Calendar.FRIDAY:
                if(cBoxFri){
                    return true;
                }
                break;
            case Calendar.SATURDAY:
                if(cBoxSat){
                    return true;
                }
                break;
            case Calendar.SUNDAY:
                if(cBoxSun){
                    return true;
                }
                break;
        }
        return false;
    }

    //
    public final boolean isProfilePinEnable(int profile){
        switch(profile){
            case C.PROFILE_MUSIC:
                return cBoxProfilePinMusic;
            case C.PROFILE_LOCATION:
                return cBoxProfilePinLocation;
            case C.PROFILE_TIME:
                return cBoxProfilePinTime;
            case C.PROFILE_WIFI:
                return cBoxProfilePinWifi;
            case C.PROFILE_BLUETOOTH:
                return cBoxProfilePinBluetooth;
        }
        return true;
    }

    public final boolean isProfileMusicEnabled(){
        return cBoxProfileMusic;
    }

    public final boolean isProfileLocationEnabled(){
        return cBoxProfileLocation;
    }

    public final boolean isProfileTimeEnabled(){
        return cBoxProfileTime;
    }

    public final boolean isProfileWifiEnabled(){
        return cBoxProfileWifi;
    }

    public final boolean isProfileBluetoothEnabled(){
        return cBoxProfileBluetooth;
    }

    public interface OnPriorityCheck{
        void onProfilePriorityCallBack(int id);
    }
}