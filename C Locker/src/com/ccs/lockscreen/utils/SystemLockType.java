package com.ccs.lockscreen.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;

public class SystemLockType{
    public final static int SOMETHING_ELSE = 0;
    public final static int NONE_OR_SLIDER = 1;
    public final static int FACE_WITH_PATTERN = 3;
    public final static int FACE_WITH_PIN = 4;
    public final static int FACE_WITH_SOMETHING_ELSE = 9;
    public final static int PATTERN = 10;
    public final static int PIN = 11;
    public final static int PASSWORD_ALPHABETIC = 12;
    public final static int PASSWORD_ALPHANUMERIC = 13;
    private static String PASSWORD_TYPE_KEY = "lockscreen.password_type";

    @SuppressWarnings("deprecation")
    public static int getCurrent(ContentResolver contentResolver){
        long mode = android.provider.Settings.Secure.getLong(contentResolver,PASSWORD_TYPE_KEY,DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
        if(mode==DevicePolicyManager.PASSWORD_QUALITY_SOMETHING){
            if(android.provider.Settings.Secure.getInt(contentResolver,Settings.Secure.LOCK_PATTERN_ENABLED,0)==1){
                return SystemLockType.PATTERN;
            }else{
                return SystemLockType.NONE_OR_SLIDER;
            }
        }else if(mode==DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK){
            String dataDirPath = Environment.getDataDirectory().getAbsolutePath();
            if(nonEmptyFileExists(dataDirPath+"/system/gesture.key")){
                return SystemLockType.FACE_WITH_PATTERN;
            }else if(nonEmptyFileExists(dataDirPath+"/system/password.key")){
                return SystemLockType.FACE_WITH_PIN;
            }else{
                return FACE_WITH_SOMETHING_ELSE;
            }
        }else if(mode==DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC){
            return SystemLockType.PASSWORD_ALPHANUMERIC;
        }else if(mode==DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC){
            return SystemLockType.PASSWORD_ALPHABETIC;
        }else if(mode==DevicePolicyManager.PASSWORD_QUALITY_NUMERIC){
            return SystemLockType.PIN;
        }else{
            return SystemLockType.SOMETHING_ELSE;
        }
    }

    private static boolean nonEmptyFileExists(String filename){
        File file = new File(filename);
        return file.exists() && file.length()>0;
    }
}
