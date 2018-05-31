package com.ccs.lockscreen.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import com.ccs.lockscreen_pro.CaptureHome;
import com.ccs.lockscreen_pro.CaptureSearchLongPress;
import com.ccs.lockscreen_pro.CaptureSystemSettings;

public class ComponentSetting{
    private Context context;

    public ComponentSetting(Context context){
        this.context = context;
    }

    public final void setAllComponentSettings(boolean enable,String msg){
        if(!enable){
            final DevicePolicyManager securityManager = (DevicePolicyManager)context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            final ComponentName deviceAdminClass = new ComponentName(context,DeviceAdmin.class);
            if(securityManager.isAdminActive(deviceAdminClass)){
                securityManager.removeActiveAdmin(deviceAdminClass);
            }
        }
        setHomeLauncher(enable,msg);
        setSearchLongPress(enable,msg);
        setSystemSettings(enable,msg);
    }

    public final void setHomeLauncher(boolean bln1,String msg){
        //mLocker.writeToFile("SettingsLockerMain>setHomeLauncher: "+bln1+"/"+msg);
        if(!bln1){
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context,CaptureHome.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        }else{
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context,CaptureHome.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
        }
    }

    public final void setSearchLongPress(boolean bln1,String msg){
        //mLocker.writeToFile("SettingsSecurity>setSearchLongPress: "+bln1+"/"+msg);
        if(!bln1){
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context,CaptureSearchLongPress.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        }else{
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context,CaptureSearchLongPress.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
        }
    }

    public final void setSystemSettings(boolean bln1,String msg){
        //mLocker.writeToFile("SettingsSecurity>setSearchLongPress: "+bln1+"/"+msg);
        if(!bln1){
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context,CaptureSystemSettings.class),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        }else{
            context.getPackageManager().setComponentEnabledSetting(new ComponentName(context,CaptureSystemSettings.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
        }
    }
}
