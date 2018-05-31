package com.ccs.lockscreen.utils;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.ccs.lockscreen.R;

public class DeviceAdminHandler{
    private DevicePolicyManager securityManager;
    private ComponentName deviceAdminClass;
    private Context context;

    public DeviceAdminHandler(Context context){
        securityManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdminClass = new ComponentName(context,DeviceAdmin.class);
        this.context = context;
    }

    public final void turnScreenOff(){
        if(isDeviceAdminActivated()){
            securityManager.lockNow();
        }
    }

    public final boolean isDeviceAdminActivated(){
        return securityManager.isAdminActive(deviceAdminClass);
    }

    public final void launchDeviceAdmin(Activity context,int requestCode){
        final Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,deviceAdminClass);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,context.getString(R.string.device_admin_desc));
        context.startActivityForResult(intent,requestCode);
    }

    public final void removeDeviceAdmin(){
        securityManager.removeActiveAdmin(deviceAdminClass);
    }

    public final void setStatusbarDisabled(boolean disable){
        if(isDeviceOwnerApp()){
            securityManager.setStatusBarDisabled(deviceAdminClass,disable);
        }
    }

    public final boolean isDeviceOwnerApp(){
        return securityManager.isDeviceOwnerApp(context.getPackageName());
    }
}