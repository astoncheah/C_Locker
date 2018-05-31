package com.ccs.lockscreen_pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

public class SettingsAbout extends BaseActivity{
    private View lytEmailMeLogs, lytTipsAndTricks, lytGooglePlusBeta, lytXDAThread, lytTranslation, lytPermissions, lytTasker;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_about);
        setBasicBackKeyAction();
        try{
            lytEmailMeLogs = findViewById(R.id.lytEmailMeLogs);
            lytTipsAndTricks = findViewById(R.id.lytTipsAndtricks);
            lytGooglePlusBeta = findViewById(R.id.lytGooglePlusBeta);
            lytXDAThread = findViewById(R.id.lytXDAThread);
            lytTranslation = findViewById(R.id.lytTranslation);
            lytPermissions = findViewById(R.id.lytPermissions);
            lytTasker = findViewById(R.id.lytTasker);

            final SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
            boolean cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);
            String version;
            try{
                version = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
            }catch(NameNotFoundException e){
                version = "";
                saveErrorLogs(null,e);
            }
            final TextView txtAppVersion = (TextView)findViewById(R.id.txtAppVersion);
            if(cBoxLockerOk){
                txtAppVersion.setText("App Version (Pro): "+version);
            }else{
                txtAppVersion.setText("App Version: "+version);
            }
            onClick();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_about;
    }

    private void onClick(){
        lytEmailMeLogs.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                getAppHandler().sentLogs("C Locker Log Information","",true);
            }
        });
        lytTipsAndTricks.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(SettingsAbout.this,SettingsTipsYoutube.class);
                startActivity(i);
            }
        });
        lytGooglePlusBeta.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://plus.google.com/communities/117122431743448418924")));
            }
        });
        lytXDAThread.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://forum.xda-developers.com/showthread.php?t=2182431")));
            }
        });
        lytTranslation.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.dropbox.com/sh/giu3sjefkwniokj/k0Qz8nQ7hA")));
            }
        });
        lytPermissions.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.dropbox.com/s/aavmz9c7rv8nlhj/C%20Locker%20Permissions.pdf")));
            }
        });
        lytTasker.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(SettingsAbout.this,SettingsTasker.class);
                startActivity(i);
            }
        });
    }
}
