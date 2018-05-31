package com.ccs.lockscreen.myclocker;

import android.content.Context;
import android.content.SharedPreferences;

public class SaveData{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SaveData(Context context){
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        editor = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE).edit();
    }

    public boolean isOnReboot(){
        return prefs.getBoolean("isOnReboot",false);
    }

    public void setOnReboot(boolean bln){
        editor.putBoolean("isOnReboot",bln);
        editor.commit();
    }

    public boolean isLockerRunning(){
        return prefs.getBoolean("isLockerRunning",false);
    }

    public void setLockerRunning(boolean bln){
        editor.putBoolean("isLockerRunning",bln);
        editor.commit();
    }

    public boolean isSettingsCalledByLocker(){
        return prefs.getBoolean("isSettingsCalledByLocker",false);
    }

    public void setSettingsCalledByLocker(boolean bln){
        editor.putBoolean("isSettingsCalledByLocker",bln);
        editor.commit();
    }

    public boolean isProfileWifiActive(){
        return prefs.getBoolean("isProfileWifiActive",false);
    }

    public void setProfileWifiActive(boolean bln){
        editor.putBoolean("isProfileWifiActive",bln);
        editor.commit();
    }

    public boolean isProfileBluetoothActive(){
        return prefs.getBoolean("isProfileBluetoothActive",false);
    }

    public void setProfileBluetoothActive(boolean bln){
        editor.putBoolean("isProfileBluetoothActive",bln);
        editor.commit();
    }

    public boolean isProfileLocationActive(){
        return prefs.getBoolean("isProfileLocationActive",false);
    }

    public void setProfileLocationActive(boolean bln){
        editor.putBoolean("isProfileLocationActive",bln);
        editor.commit();
    }

    public boolean isPasswordChanged(){
        return prefs.getBoolean("isPasswordChanged",false);
    }

    public void setPasswordChanged(boolean bln){
        editor.putBoolean("isPasswordChanged",bln);
        editor.commit();
    }

    public boolean isKillLocker(){
        return prefs.getBoolean("isKillLocker",false);
    }

    public void setKillLocker(boolean bln){
        editor.putBoolean("isKillLocker",bln);
        editor.commit();
    }

    public boolean isRssUpdated(){
        return prefs.getBoolean("isRssUpdated",false);
    }

    public void setRssUpdated(boolean bln){
        editor.putBoolean("isRssUpdated",bln);
        editor.commit();
    }

    public boolean isTurnScreenOff(){
        return prefs.getBoolean("isTurnScreenOff",false);
    }

    public void setTurnScreenOff(boolean bln){
        editor.putBoolean("isTurnScreenOff",bln);
        editor.commit();
    }

    public boolean isHeadsetConnected(){
        return prefs.getBoolean("isHeadsetConnected",false);
    }

    public void setHeadsetConnected(boolean bln){
        editor.putBoolean("isHeadsetConnected",bln);
        editor.commit();
    }

    public int getScreenTimeOutVal(){
        return prefs.getInt("screenTimeOutVal",C.DEFAULT_SCREEN_TIMEOUT);
    }

    public void setScreenTimeOutVal(int val){
        editor.putInt("screenTimeOutVal",val);
        editor.commit();
    }

    public int getScreenBrightness(){
        return prefs.getInt("intSystemBrightnessVal",100);
    }

    public void setScreenBrightness(int val){
        editor.putInt("intSystemBrightnessVal",val);
        editor.commit();
    }

    public int getScreenTimeoutType(){
        return prefs.getInt("intScreenTimeoutType",C.TYPE_SYSTEM_SCREEN_TIMEOUT);
    }

    public void setScreenTimeoutType(int val){
        editor.putInt("intScreenTimeoutType",val);
        editor.commit();
    }

    public int getScreenTimeoutNotice(){
        return prefs.getInt("intScreenTimeoutNotice",2000);
    }

    public void setScreenTimeoutNotice(int val){
        editor.putInt("intScreenTimeoutNotice",val);
        editor.commit();
    }

    public long getLockerActivationTime(){
        return prefs.getLong("intLockerActivationTime",-1);
    }

    public void setLockerActivationTime(long val){
        editor.putLong("intLockerActivationTime",val);
        editor.commit();
    }
}