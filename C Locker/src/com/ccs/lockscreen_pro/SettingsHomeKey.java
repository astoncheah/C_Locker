package com.ccs.lockscreen_pro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.ComponentSetting;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;

import java.util.List;

public class SettingsHomeKey extends BaseActivity{
    private static final int IS_DEFAULT_CLEARED = 1, IS_DEFAULT_OTHER = 2, IS_DEFAULT_C_LOCKER = 3;
    private static SettingsHomeKey instance = null;
    private View lytClearHomeDefault, lytSetCLockerAsHome, lytSelectHomeLauncher;
    private CheckBox cBoxClearHomeDefault, cBoxSetCLockerAsHome;
    private ImageView imgSelectHomeLauncher;

    public static boolean isSettingsHomeKey(){
        return instance!=null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.security_set_homekey_);
        setBasicBackKeyAction();
        //Log.e("SettingsHomeKey>onCreate: ",""+instance);
        try{
            lytClearHomeDefault = this.findViewById(R.id.lytClearHomeDefault);
            lytSetCLockerAsHome = this.findViewById(R.id.lytSetCLockerAsHome);
            lytSelectHomeLauncher = this.findViewById(R.id.lytSelectHomeLauncher);

            cBoxClearHomeDefault = (CheckBox)this.findViewById(R.id.cBoxClearHomeDefault);
            cBoxSetCLockerAsHome = (CheckBox)this.findViewById(R.id.cBoxSetCLockerAsHome);

            imgSelectHomeLauncher = (ImageView)this.findViewById(R.id.imgSelectHomeLauncher);
            onClick();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        instance = null;
        //Log.e("SettingsHomeKey>onDestroy: ",""+instance);
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_home_key;
    }

    private void onClick(){
        lytSelectHomeLauncher.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                final Intent i = new Intent(SettingsHomeKey.this,ListInstalledApps.class);
                i.putExtra(C.LIST_APPS_ID,C.LIST_LAUNCHER);
                startActivity(i);
            }
        });
        lytClearHomeDefault.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxClearHomeDefault.performClick();
            }
        });
        lytSetCLockerAsHome.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxSetCLockerAsHome.performClick();
            }
        });
        //
        cBoxClearHomeDefault.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                int defaultType = getDefaultType(C.getHomeLauncher(SettingsHomeKey.this),"cBoxClearHomeDefault");
                if(defaultType==IS_DEFAULT_OTHER){
                    int count = getLauncherCount();
                    if(count>1){
                        final Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:"+C.getHomeLauncher(SettingsHomeKey.this)));
                        startActivity(i);
                    }else{//only one launcher, so no need to clear and go step 3
                        new MyAlertDialog(SettingsHomeKey.this).simpleMsg(getString(R.string.clear_other_home_launcher_desc1));
                        cBoxClearHomeDefault.setChecked(true);
                    }
                }else if(defaultType==IS_DEFAULT_CLEARED){
                    new MyAlertDialog(SettingsHomeKey.this).simpleMsg(getString(R.string.clear_other_home_launcher_desc1));
                    cBoxClearHomeDefault.setChecked(true);
                }else if(defaultType==IS_DEFAULT_C_LOCKER){
                    new MyAlertDialog(SettingsHomeKey.this).simpleMsg(getString(R.string.clear_other_home_launcher_desc3),new OnDialogListener(){
                        @Override
                        public void onDialogClickListener(int dialogType,int result){
                            if(result==MyAlertDialog.OK){
                                final Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.addCategory(Intent.CATEGORY_DEFAULT);
                                i.setData(Uri.parse("package:"+C.getHomeLauncher(SettingsHomeKey.this)));
                                startActivity(i);
                            }
                        }
                    });
                    cBoxClearHomeDefault.setChecked(true);
                }
            }
        });
        cBoxSetCLockerAsHome.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(cBoxSetCLockerAsHome.isChecked()){
                    int defaultType = getDefaultType(C.getHomeLauncher(SettingsHomeKey.this),"cBoxSetCLockerAsHome");
                    int count = getLauncherCount();
                    if(defaultType==IS_DEFAULT_C_LOCKER){
                        return;
                    }
                    if(defaultType==IS_DEFAULT_OTHER){
                        if(count>1){//if only one launcher, skip this
                            new MyAlertDialog(SettingsHomeKey.this).simpleMsg(getString(R.string.set_clocker_as_home_desc1));
                            cBoxSetCLockerAsHome.setChecked(false);
                            return;
                        }
                    }
                    new ComponentSetting(SettingsHomeKey.this).setHomeLauncher(true,"SettingsHomeKey>onClick: true");

                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_HOME);
                    startActivity(i);
                }else{
                    new ComponentSetting(SettingsHomeKey.this).setHomeLauncher(false,"SettingsHomeKey>onClick: false");
                }
            }
        });
    }

    private int getDefaultType(String appDefault,String msg){
        saveLogs("SettingsHomeKey>getDefaultType: "+appDefault+"/"+msg);
        final String getMyPackageName = getPackageName();
        if(appDefault.equals("android") ||
            appDefault.equals("org.cyanogenmod.resolver") ||
            appDefault.equals("com.huawei.android.launcher") ||
            appDefault.equals("com.huawei.android.internal.app")){
            // SettingsSecurity,SettingsHomeKey,ServiceMain,Locker classes all need to change together!!!!!
            return IS_DEFAULT_CLEARED;
        }else if(appDefault.equals(getMyPackageName)){
            return IS_DEFAULT_C_LOCKER;
        }else{
            return IS_DEFAULT_OTHER;
        }
    }

    private int getLauncherCount(){
        final Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> list = this.getPackageManager().queryIntentActivities(main,0);

        int count = 0;
        for(ResolveInfo item: list){
            if(!item.activityInfo.packageName.equalsIgnoreCase("com.android.settings")){
                count++;
            }
            saveLogs("SettingsHomeKey>getLauncherCount: "+item.activityInfo.packageName+"/"+count);
        }
        //return list.size();
        return count;
    }

    @Override
    public void onResume(){
        super.onResume();
        instance = this;
        //Log.e("SettingsHomeKey>onResume: ",""+instance);
        loadSettings();
    }

    private void loadSettings(){
        int defaultType = getDefaultType(C.getHomeLauncher(this),"loadSettings");
        int count = getLauncherCount();
        if(defaultType==IS_DEFAULT_C_LOCKER){
            cBoxClearHomeDefault.setChecked(true);
            cBoxSetCLockerAsHome.setChecked(true);
        }else if(defaultType==IS_DEFAULT_CLEARED){
            cBoxClearHomeDefault.setChecked(true);
            cBoxSetCLockerAsHome.setChecked(false);
        }else if(defaultType==IS_DEFAULT_OTHER){
            if(count>1){
                cBoxClearHomeDefault.setChecked(false);
                cBoxSetCLockerAsHome.setChecked(false);
            }else{
                cBoxClearHomeDefault.setChecked(true);
                cBoxSetCLockerAsHome.setChecked(false);
            }
        }
        imgSelectHomeLauncher.setImageDrawable(getIcon());
    }

    private Drawable getIcon(){
        final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final String pkgName = prefs.getString(P.STR_HOME_LAUNCHER_PKG_NAME,"");
        try{
            return this.getPackageManager().getApplicationIcon(pkgName);
        }catch(NameNotFoundException e){
            e.printStackTrace();
        }
        return null;
    }
}
