package com.ccs.lockscreen.utils;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;

import com.ccs.lockscreen.myclocker.SaveData;

public class DeviceAdmin extends DeviceAdminReceiver{
    @Override
    public void onEnabled(Context context,Intent intent){
    }

    @Override
    public CharSequence onDisableRequested(Context context,Intent intent){
        return super.onDisableRequested(context,intent);
    }

    @Override
    public void onDisabled(Context context,Intent intent){
    }

    @Override
    public void onPasswordChanged(Context context,Intent intent){
        SaveData mSaveData = new SaveData(context);
        mSaveData.setPasswordChanged(true);
    }

    @Override
    public void onPasswordSucceeded(Context context,Intent intent){
    }
}
