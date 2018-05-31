package com.ccs.lockscreen_pro;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SettingsSecurityRoot extends BaseActivity{
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private View lytSetSystemApp, lytHideStatusbarClock, lytHideStatusbarIcon, lytHideStatusbarAlert, lytHideNavigationKeys;
    private CheckBox cBoxSetSystemApp, cBoxHideStatusbarClock, cBoxHideStatusbarIcon, cBoxHideStatusbarAlert, cBoxHideNavigationKeys;
    private TextView txtSetSystemAppTick, txtSetSystemAppTick2, txtSetSystemAppUntick;
    private ProgressDialog progressBar;
    private AlertDialog alert;
    private String currentDir, appName;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_root);
        setBasicBackKeyAction();

        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();
        try{
            lytSetSystemApp = (View)findViewById(R.id.lytSetSystemApp);
            lytHideStatusbarClock = (View)findViewById(R.id.lytHideStatusbarClock);
            lytHideStatusbarIcon = (View)findViewById(R.id.lytHideStatusbarIcon);
            lytHideStatusbarAlert = (View)findViewById(R.id.lytHideStatusbarAlert);
            lytHideNavigationKeys = (View)findViewById(R.id.lytHideNavigationKeys);

            txtSetSystemAppTick = (TextView)findViewById(R.id.txtSetSystemAppTick);
            txtSetSystemAppTick2 = (TextView)findViewById(R.id.txtSetSystemAppTick2);
            txtSetSystemAppUntick = (TextView)findViewById(R.id.txtSetSystemAppUntick);

            cBoxSetSystemApp = (CheckBox)findViewById(R.id.cBoxSetSystemApp);
            cBoxHideStatusbarClock = (CheckBox)findViewById(R.id.cBoxHideStatusbarClock);
            cBoxHideStatusbarIcon = (CheckBox)findViewById(R.id.cBoxHideStatusbarIcon);
            cBoxHideStatusbarAlert = (CheckBox)findViewById(R.id.cBoxHideStatusbarAlert);
            cBoxHideNavigationKeys = (CheckBox)findViewById(R.id.cBoxHideNavigationKeys);

            onClickFunction();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        try{
            if(alert!=null && alert.isShowing()){
                alert.dismiss();
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_security_root;
    }

    private void onClickFunction(){
        lytSetSystemApp.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxSetSystemApp.performClick();
            }
        });
        lytHideStatusbarClock.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxHideStatusbarClock.performClick();
            }
        });
        lytHideStatusbarIcon.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxHideStatusbarIcon.performClick();
            }
        });
        lytHideStatusbarAlert.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxHideStatusbarAlert.performClick();
            }
        });
        lytHideNavigationKeys.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxHideNavigationKeys.performClick();
            }
        });
        cBoxSetSystemApp.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                dialogMoveApp();
            }
        });
        cBoxHideStatusbarClock.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(cBoxHideStatusbarClock.isChecked() && !currentDir.contains("/system/")){
                    cBoxHideStatusbarClock.setChecked(false);
                    Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_note),Toast.LENGTH_LONG).show();
                }
            }
        });
        cBoxHideStatusbarIcon.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(cBoxHideStatusbarIcon.isChecked() && !currentDir.contains("/system/")){
                    cBoxHideStatusbarIcon.setChecked(false);
                    Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_note),Toast.LENGTH_LONG).show();
                }
            }
        });
        cBoxHideStatusbarAlert.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(cBoxHideStatusbarAlert.isChecked() && !currentDir.contains("/system/")){
                    cBoxHideStatusbarAlert.setChecked(false);
                    Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_note),Toast.LENGTH_LONG).show();
                }
            }
        });
        cBoxHideNavigationKeys.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(cBoxHideNavigationKeys.isChecked() && !currentDir.contains("/system/")){
                    cBoxHideNavigationKeys.setChecked(false);
                    Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_note),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void dialogMoveApp(){
        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LinearLayout lyt = new LinearLayout(this);
            lyt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
            lyt.setPadding(0,C.dpToPx(this,10),0,C.dpToPx(this,10));
            lyt.setOrientation(LinearLayout.VERTICAL);
            lyt.addView(addTxt("1) "+getString(R.string.settings_root_dialog_1),Color.WHITE));
            lyt.addView(addTxt("2) "+getString(R.string.settings_root_dialog_2),Color.WHITE));
            lyt.addView(addTxt("3) "+getString(R.string.settings_root_dialog_3),Color.YELLOW));
            lyt.addView(addTxt("4) "+getString(R.string.settings_root_dialog_4),Color.WHITE));
            builder.setTitle(getString(R.string.settings_root_move_to_system_steps));
            builder.setView(lyt);
            builder.setOnCancelListener(new OnCancelListener(){
                @Override
                public void onCancel(DialogInterface dialog){
                    onResume();
                }
            });
            builder.setPositiveButton(getString(R.string.next),new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which){
                    getAppHandler().exportBackup(false);
                    new MoveApp().execute();
                }
            });
            alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private TextView addTxt(String str,int color){
        final TextView txt = new TextView(this);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        txt.setPadding(C.dpToPx(this,15),C.dpToPx(this,2),C.dpToPx(this,15),C.dpToPx(this,2));
        txt.setText(str);
        txt.setTextColor(color);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
        //txt.setBackgroundColor(Color.parseColor("#66000000"));
        return txt;
    }

    private boolean isSystemApp(){
        try{
            final ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),0);
            currentDir = appInfo.sourceDir;
            appName = appInfo.packageName+".apk";
            if(currentDir!=null && currentDir.contains("/system/")){
                return true;
            }
        }catch(NameNotFoundException e){
            saveErrorLogs(null,e);
        }
        return false;
    }

    private void loadSettings(){
        cBoxHideStatusbarClock.setChecked(prefs.getBoolean("cBoxHideStatusbarClock",false));
        cBoxHideStatusbarIcon.setChecked(prefs.getBoolean("cBoxHideStatusbarIcon",false));
        cBoxHideStatusbarAlert.setChecked(prefs.getBoolean("cBoxHideStatusbarAlert",false));
        cBoxHideNavigationKeys.setChecked(prefs.getBoolean("cBoxHideNavigationKeys",false));
    }

    @Override
    protected void onPause(){
        saveSettings();
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(isSystemApp()){
            txtSetSystemAppTick.setVisibility(View.GONE);
            txtSetSystemAppTick2.setVisibility(View.GONE);
            txtSetSystemAppUntick.setVisibility(View.VISIBLE);
            cBoxSetSystemApp.setChecked(true);

            loadSettings();
        }else{
            txtSetSystemAppTick.setVisibility(View.VISIBLE);
            txtSetSystemAppTick2.setVisibility(View.VISIBLE);
            txtSetSystemAppUntick.setVisibility(View.GONE);

            lytSetSystemApp.setBackgroundColor(Color.parseColor("#66ff0000"));
            cBoxSetSystemApp.setChecked(false);
            cBoxHideStatusbarClock.setChecked(false);
            cBoxHideStatusbarIcon.setChecked(false);
            cBoxHideStatusbarAlert.setChecked(false);
            cBoxHideNavigationKeys.setChecked(false);
        }
    }

    private void saveSettings(){
        if(isSystemApp()){
            editor.putBoolean("cBoxHideStatusbarClock",cBoxHideStatusbarClock.isChecked());
            editor.putBoolean("cBoxHideStatusbarIcon",cBoxHideStatusbarIcon.isChecked());
            editor.putBoolean("cBoxHideStatusbarAlert",cBoxHideStatusbarAlert.isChecked());
            editor.putBoolean("cBoxHideNavigationKeys",cBoxHideNavigationKeys.isChecked());
            editor.commit();
        }
    }

    private class MoveApp extends AsyncTask<Void,String,Integer>{
        @Override
        protected Integer doInBackground(Void... arg0){
            int result = 3;//failed
            if(canRunRootCommands()){
                String toDataDir = "/data/app/"+appName;
                String toSystemDir = "/system/app/"+appName;
                String toSystemPrivDir = "/system/priv-app/"+appName;

                publishProgress(getString(R.string.settings_root_dialog_2));
                if(currentDir.contains("/system/")){
                    result = moveToDataApk(currentDir,toDataDir);
                }else{
                    if(isPrivAppAvailable()){
                        result = moveToSystemApk(currentDir,toSystemPrivDir);
                    }else{
                        result = moveToSystemApk(currentDir,toSystemDir);
                    }
                }
            }else{
                result = 2;
            }
            return result;
        }

        @Override
        protected void onPreExecute(){
            progressBar = new ProgressDialog(SettingsSecurityRoot.this);
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.settings_root_dialog_1));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }

        @Override
        protected void onPostExecute(Integer val){
            progressBar.dismiss();
            if(val==1){
                Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_successful),Toast.LENGTH_LONG).show();
                android.os.Process.killProcess(android.os.Process.myPid());
            }else if(val==2){
                cBoxSetSystemApp.setChecked(false);
                Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_denied),Toast.LENGTH_LONG).show();
            }else{
                cBoxSetSystemApp.setChecked(false);
                Toast.makeText(SettingsSecurityRoot.this,getString(R.string.settings_root_failed),Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values){
            progressBar.setMessage(values[0]);
            super.onProgressUpdate(values);
        }

        private boolean canRunRootCommands(){
            boolean retval = false;
            Process suProcess;
            try{
                suProcess = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
                BufferedReader osRes = new BufferedReader(new InputStreamReader(suProcess.getInputStream()));

                if(null!=os && null!=osRes){
                    // Getting the id of the current user to check if this is root
                    os.writeBytes("id\n");
                    os.flush();

                    String currUid = osRes.readLine();
                    boolean exitSu;
                    if(null==currUid){
                        retval = false;
                        exitSu = false;
                        saveLogs("Can't get root access or denied by user");
                        //Log.w("ROOT", "Can't get root access or denied by user");
                    }else if(true==currUid.contains("uid=0")){
                        retval = true;
                        exitSu = true;
                        saveLogs("Root access granted: "+currUid);
                        //Log.w("ROOT", "Root access granted");
                    }else{
                        retval = false;
                        exitSu = true;
                        saveLogs("Root access rejected: "+currUid);
                        //Log.w("ROOT", "Root access rejected: " + currUid);
                    }

                    if(exitSu){
                        os.writeBytes("exit\n");
                        os.flush();
                    }
                }
            }catch(Exception e){
                retval = false;
                //Log.w("ROOT", "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
                saveErrorLogs(null,e);
            }
            return retval;
        }

        private int moveToDataApk(String from,String to){//root access
            try{
                saveLogs("moveToDataApk: "+from+" > "+to);

                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                out.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n");
                out.writeBytes("cat "+from+" > "+to+"\n");    //move apk
                out.writeBytes("rm "+from+"\n");// remove old apk
                out.writeBytes("chmod 644 "+to+"\n");//update permissions
                out.writeBytes("mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system\n");
                //
                publishProgress(getString(R.string.settings_root_dialog_5));
                //
                out.writeBytes("pm install "+to+"\n");//re-install apk
                out.writeBytes("rm "+to+"\n");// remove copied apk, to prevent double apk
                out.writeBytes("reboot\n");
                out.writeBytes("exit\n");
                out.flush();
                out.close();

                process.waitFor();

                if(!isDirAvalaible(from)){
                    return 1;
                }
            }catch(IOException e){
                saveErrorLogs(null,e);
            }catch(InterruptedException e){
                saveErrorLogs(null,e);
            }
            return 3;//fail to move apk
        }

        private boolean isPrivAppAvailable(){
            final File fileFrom = new File("/system");
            if(fileFrom.isDirectory()){
                final String[] children = fileFrom.list();
                for(String item : children){
                    //mLocker.writeToFile(C.EXT_BACKUP_DIR,C.FILE_BACKUP_RECORD,item);
                    if(item.equals("priv-app")){
                        return true;
                    }
                }
            }
            return false;
        }

        private int moveToSystemApk(String from,String to){//root access
            try{
                saveLogs("moveToSystemApk: "+from+" > "+to);

                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream out = new DataOutputStream(process.getOutputStream());

                out.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system\n");
                out.writeBytes("cat "+from+" > "+to+"\n");
                out.writeBytes("rm "+from+"\n");
                out.writeBytes("chmod 644 "+to+"\n");
                out.writeBytes("mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system\n");
                out.writeBytes("reboot\n");
                out.writeBytes("exit\n");
                out.flush();
                out.close();

                process.waitFor();

                if(!isDirAvalaible(from)){
                    return 1;
                }
            }catch(IOException e){
                saveErrorLogs(null,e);
            }catch(InterruptedException e){
                saveErrorLogs(null,e);
            }
            return 3;//fail to move apk
        }

        private boolean isDirAvalaible(String dir){
            final File fileFrom = new File(dir);
            if(fileFrom.isFile()){
                return true;
            }
            return false;
        }
    }
}