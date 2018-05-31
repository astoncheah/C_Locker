package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.BuildConfig;
import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.DataWidgets;
import com.ccs.lockscreen.data.InfoWidget;
import com.ccs.lockscreen.fragments.SettingsDisplay;
import com.ccs.lockscreen.fragments.SettingsNotices;
import com.ccs.lockscreen.fragments.SettingsOtherOptions;
import com.ccs.lockscreen.fragments.SettingsProfiles;
import com.ccs.lockscreen.fragments.SettingsSecurity;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.SaveData;
import com.ccs.lockscreen.utils.BackpupDrive;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.ComponentSetting;
import com.ccs.lockscreen.utils.DeviceAdminHandler;
import com.ccs.lockscreen.utils.GoogleDrive;
import com.ccs.lockscreen.utils.GoogleDrive.DriveListener;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import mcalls.mmspot.sdk.MMspot;
import mcalls.mmspot.sdk.MMspotPayment;
import mcalls.mmspot.sdk.MMspotRedemption;
import mcalls.mmspot.sdk.RedemptionCallBack;
import mcalls.mmspot.sdk.RegisterCallBack;

public class SettingsLockerMain extends BaseActivity{
    private static final int PROFILES = 0, VISUAL = 1, NOTICE = 2, SECURITY = 3, OTHER_OPTIONS = 4;
    private static final int NEW_GUIDE = 1;
    //dialog tips
    private static final int DIALOG_WIDGETS = 1, DIALOG_LAYOUT_HOME = 2, DIALOG_LAYOUT_NOTICE = 3, DIALOG_DEVICE_ADMIN = 4, DIALOG_FINGERPRINT = 5, DIALOG_WIDGET_CLICK = 6;
    private AdView adView;
    private LinearLayout layoutAds;
    private InterstitialAd mInterstitialAd;
    private boolean cBoxLockerOk;
    //=====
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private MyAlertDialog dialog;
    private Handler handler = new Handler();
    private DeviceAdminHandler deviceAdminHandler;
    private ProgressDialog progressBar;
    private ArrayList<FragmentNavItem> drawerNavItems;
    private DrawerLayout drawer;
    private BackpupDrive backpupDrive;
    private View lytEnableLocker;
    private CheckBox cBoxEnableLsService;
    private LinearLayout lytMainOptions, lytEnablelockerSub, lytLockerProfiles, lytUnlockingStyle, lytVisual, lytSecurity, lytNotification, lytOtherOptions;
    private TextView txtQuickGuide, txtTips, txtBackupRestore, txtEmailMe, txtAboutCLocker;
    private boolean isFirstLaunch, isSMSNoticeSupported;
    private AlertDialog alert;
    private Config config;
    private int currentSetting = PROFILES;
    private String[] titles;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(getPackageName()+C.RESTORE_COMPLETE)){
                    //loadSettings();
                }else if(action.equals(getPackageName()+C.PROMOTION_CHECK)){
                    if(progressBar!=null && progressBar.isShowing()){
                        progressBar.dismiss();
                    }
                    if(intent.hasExtra("promotionResult")){
                        int result = intent.getExtras().getInt("promotionResult");
                        switch(result){
                            case C.PROMOTION_TIME_OK:
                                cBoxLockerOk = true;

                                editor.putBoolean(C.C_LOCKER_OK,true);
                                editor.putBoolean(C.LICENCE_CHECK_TRY,false); //it's free promotion
                                editor.putBoolean(C.PROMOTION_APP,true);
                                editor.commit();

                                dialogPromotion(getString(R.string.promotion_day_desc3),getString(android.R.string.ok),false);
                                break;
                            case C.PROMOTION_TIME_NG:
                                cBoxLockerOk = false;

                                editor.putBoolean(C.C_LOCKER_OK,false);
                                editor.putBoolean(C.LICENCE_CHECK_TRY,false); //free version with features locked
                                editor.putBoolean(C.PROMOTION_APP,false);
                                editor.commit();

                                dialogPromotion(getString(R.string.promotion_day_desc4),getString(android.R.string.ok),false);
                                break;
                            case C.PROMOTION_TIME_ERROR:
                                cBoxLockerOk = false;

                                editor.putBoolean(C.C_LOCKER_OK,false);
                                editor.putBoolean(C.LICENCE_CHECK_TRY,false); //free version with features locked
                                editor.putBoolean(C.PROMOTION_APP,false);
                                editor.commit();

                                dialogPromotion(getString(R.string.promotion_day_desc5),getString(android.R.string.ok),false);
                                break;
                        }
                    }
                }
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
        }
    };
    //
    private int STARTING_RAW_X, STARTING_RAW_Y, moveGap;
    private boolean hasMoved;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        MMspot.setupMMspot("MULADEMO","3c6e0b8a9c15224a8228b9a98ca1531d",this,"EN");//CN

        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        loadReceiver(true);//load 1st
        dialog = new MyAlertDialog(this);

        if (BuildConfig.FLAVOR.equalsIgnoreCase("xda")) {
            checkLicense(true);// XDA version
            //Toast.makeText(this,"XDA version",Toast.LENGTH_LONG).show();
        }else{
            checkLicense(false);// google play version
            //Toast.makeText(this,BuildConfig.FLAVOR+" NON XDA version",Toast.LENGTH_LONG).show();
        }

        loadLockerProInstalledCheck();
        loadAds();
        checkNewVersion();
        checkPromotion();//load ad 1st before check promotion
        if(!checkNewCLLite()){
            promoteRating();
        }

        SaveData sd = new SaveData(this);
        if(sd.isSettingsCalledByLocker()){
            sd.setSettingsCalledByLocker(false);
        }else{
            sd.setLockerRunning(false);//to prevent c locker not launching
        }

        try{
            deviceAdminHandler = new DeviceAdminHandler(this);
            loadViewIds();
            loadDrawer();
            onClickFunction();

            selectDrawerItem(lytLockerProfiles,PROFILES,false);
            openFragmentSetting(PROFILES);

            isSMSNoticeSupported = isSMSUriValid();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        loadReceiver(false);
        closeAds();
        if(!cBoxEnableLsService.isChecked()){
            new ComponentSetting(this).setAllComponentSettings(false,"onClickFunction");
        }
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            if(alert!=null && alert.isShowing()){
                alert.dismiss();
            }
            if(config!=null){
                config.cancel(true);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        System.gc();
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_main;
    }

    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(getPackageName()+C.RESTORE_COMPLETE);
                intentFilter.addAction(getPackageName()+C.PROMOTION_CHECK);
                registerReceiver(mReceiver,intentFilter);
            }else{
                unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void closeAds(){
        try{
            if(adView!=null){
                adView.removeAllViews();
                adView.destroy();
                layoutAds.removeView(adView);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_locker_menu,menu);

        Drawable iconShare = ContextCompat.getDrawable(this,R.drawable.ic_share_white_48dp);
        Drawable iconRate = ContextCompat.getDrawable(this,R.drawable.ic_star_outline_white_48dp);

        MenuItem share = menu.findItem(R.id.itemShare);
        MenuItem rate = menu.findItem(R.id.itemRate);

        share.setIcon(C.getScaledMenuIcon(this,iconShare));
        share.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        rate.setIcon(C.getScaledMenuIcon(this,iconRate));
        rate.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        try{
            switch(item.getItemId()){
                case R.id.itemShare:
                    //id: 6011 3113 5857
                    //pass: 9178 0634
                    Log.e("MMspot/MMspotPayment","do MMspotPayment: ");
                    MMspotPayment mcall = new MMspotPayment();
                    mcall.mmspotPaymentInfo(this,"5","test1");
                    mcall.redemptionResult(new RedemptionCallBack() {
                        @Override
                        public void onSuccess(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8) {
                            Log.e("MMspot/redemptionResult","onSuccess: "
                                    +s+"/1)"
                                    +s1+"/2)"
                                    +s2+"/3)"
                                    +s3+"/4)"
                                    +s4+"/5)"
                                    +s5+"/6)"
                                    +s6+"/7)"
                                    +s7+"/8)"
                                    +s8);
                        }

                        @Override
                        public void onError(String s, String s1) {
                            Log.e("MMspot/redemptionResult","onError: "
                                    +s+"/"
                                    +s1+"/");
                        }

                        @Override
                        public void onOTP(String s) {
                            Log.e("MMspot/redemptionResult","onOTP: "
                                    +s+"/");
                        }
                    });
//                    final MMspotRedemption mcall = new MMspotRedemption();
//                    mcall.registerMMspotRedemptionMethod(this);
//                    //mcall.makeMMspotRedemption(this,"3c6e0b8a9c15224a8228b9a98ca1531d","10","test");
//                    mcall.registerResult(new RegisterCallBack() {
//                        @Override
//                        public void onRegisterSuccess(String s, String s1, String s2, String s3) {
//                            Log.e("MMspot","onRegisterSuccess: "
//                                    +s+"/"
//                                    +s1+"/"
//                                    +s2+"/"
//                                    +s3+"/");
//
//                            mcall.makeMMspotRedemption(SettingsLockerMain.this,"3c6e0b8a9c15224a8228b9a98ca1531d","10","test");
//                        }
//
//                        @Override
//                        public void onUnregisterSuccess(String s, String s1, String s2, String s3) {
//                            Log.e("MMspot","onUnregisterSuccess: "
//                                    +s+"/"
//                                    +s1+"/"
//                                    +s2+"/"
//                                    +s3+"/");
//                        }
//
//                        @Override
//                        public void onRedemptionSuccess(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7) {
//                            Log.e("MMspot","onRedemptionSuccess: "
//                                    +s+"/"
//                                    +s1+"/"
//                                    +s2+"/"
//                                    +s3+"/"
//                                    +s4+"/"
//                                    +s5+"/"
//                                    +s6+"/"
//                                    +s7+"/");
//                        }
//
//                        @Override
//                        public void onError(String s, String s1, String s2, String s3) {
//                            Log.e("MMspot","onError: "
//                                    +s+"/"
//                                    +s1+"/"
//                                    +s2+"/"
//                                    +s3+"/");
//                        }
//                    });
//                    dialog.alert(getString(R.string.share_friends),new DialogInterface.OnClickListener(){
//                        @Override
//                        public void onClick(DialogInterface dialog,int which){
//                            Intent i = new Intent();
//                            i.setAction(Intent.ACTION_SEND);
//                            i.setType("text/plain");
//                            i.putExtra(android.content.Intent.EXTRA_SUBJECT,"Sharing an Android App - C Locker");
//                            i.putExtra(Intent.EXTRA_TEXT,"Hey i just installed C Locker, check out this highly customizable app at Google Playstore: "+"\n\nhttps://play.google.com/store/apps/details?id=com.ccs.lockscreen&hl=en");
//                            startActivity(i);
//                        }
//                    });
                    break;
                case R.id.itemRate:
                    dialog.alert(getString(R.string.settings_rate_myapp_desc),new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog,int which){
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse("market://details?id="+getPackageName()));
                            startActivity(i);
                        }
                    });
                    break;
                case R.id.itemBackupRestore:
                    dialog.selectBackupRestore(new OnDialogListener(){
                        @Override
                        public void onDialogClickListener(int dialogType,int result){
                            switch(result){
                                case 0:
                                    handleExportBackup(false);
                                    break;
                                case 1:
                                    handleExportBackup(true);
                                    break;
                                case 2:
                                    handleImportBackup(false);
                                    break;
                                case 3:
                                    handleImportBackup(true);
                                    break;
                            }
                        }
                    });
                    break;
                case R.id.itemDelLog:
                    try{
                        getAppHandler().clearLogs();
                        getAppHandler().writeToFile(C.FILE_BACKUP_RECORD,"Old files deleted (User Cleared Logs)");
                    }catch(Exception e){
                        saveErrorLogs(null,e);
                    }
                    break;
                case android.R.id.home:
                    if(drawer!=null){
                        if(drawer.isDrawerOpen(Gravity.START)){
                            drawer.closeDrawer(Gravity.START);
                            //setActionBarIcon(R.drawable.ic_menu_white);
                        }else{
                            drawer.openDrawer(Gravity.START);
                            //setActionBarIcon(R.drawable.ic_arrow_back_white);
                        }
                    }
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        return true;
    }

    private void handleExportBackup(final boolean isDrive){
        startProgressbar();
        new Thread(){
            public void run(){
                if(isDrive){
                    boolean isSuccessful = getAppHandler().exportBackup(false);
                    if(!isSuccessful){
                        if(progressBar!=null && progressBar.isShowing()){
                            progressBar.dismiss();
                        }
                        return;
                    }
                    backpupDrive = new BackpupDrive(SettingsLockerMain.this);
                    backpupDrive.doTask(BackpupDrive.BACKUP,new DriveListener(){
                        @Override
                        public void onDriveResult(int result,String title,String details){
                            if(result==BackpupDrive.RESULT_ERROR){
                                saveLogs(details);
                            }
                            showToastLong(title);
                            backpupDrive.disconnect();
                            if(progressBar!=null && progressBar.isShowing()){
                                progressBar.dismiss();
                            }
                        }                        @Override
                        public void onDriveProgress(int totalSize,int progress,String msg){
                            if(progressBar!=null){
                                progressBar.setMessage(progress+"/"+totalSize+" ("+msg+")");
                                progressBar.show();
                            }
                        }


                    });
                }else{
                    getAppHandler().exportBackup(true);
                    if(progressBar!=null && progressBar.isShowing()){
                        progressBar.dismiss();
                    }
                }
            }
        }.start();
    }

    private void handleImportBackup(final boolean isDrive){
        startProgressbar();
        closeFragmentSetting(currentSetting);
        if(isDrive){
            new Thread(){
                public void run(){
                    backpupDrive = new BackpupDrive(SettingsLockerMain.this);
                    backpupDrive.doTask(BackpupDrive.RESTORE,new DriveListener(){
                        @Override
                        public void onDriveProgress(int totalSize,int progress,String msg){
                            if(progressBar!=null){
                                progressBar.setMessage(progress+"/"+totalSize+" ("+msg+")");
                                progressBar.show();
                            }
                        }

                        @Override
                        public void onDriveResult(int result,String title,String details){
                            if(result==BackpupDrive.RESULT_OK){
                                String dir = C.EXT_BACKUP_DIR_PRO;
                                if(getPackageName().equals(C.PKG_NAME_FREE)){
                                    dir = C.EXT_BACKUP_DIR_FREE;
                                }
                                File backupFile = new File(dir,C.FILE_PREFS);
                                if(backupFile.getAbsoluteFile().exists()){
                                    getAppHandler().importBackup(dir,false);
                                }
                            }else{
                                saveLogs(details);
                            }
                            showToastLong(title);
                            backpupDrive.disconnect();
                            if(progressBar!=null && progressBar.isShowing()){
                                progressBar.dismiss();
                            }
                            openFragmentSetting(currentSetting);
                        }
                    });
                }
            }.start();
        }else{
            final File backupFileFree = new File(C.EXT_BACKUP_DIR_FREE,C.FILE_PREFS);
            final File backupFilePro = new File(C.EXT_BACKUP_DIR_PRO,C.FILE_PREFS);
            if(backupFileFree.getAbsoluteFile().exists() && backupFilePro.getAbsoluteFile().exists()){
                dialog.selectRestoreFile(new OnDialogListener(){
                    @Override
                    public void onDialogClickListener(int dialogType,int item){
                        if(item==0){
                            startProgressbar();
                            new Thread(){
                                public void run(){
                                    getAppHandler().importBackup(C.EXT_BACKUP_DIR_FREE,true);
                                    if(progressBar!=null && progressBar.isShowing()){
                                        progressBar.dismiss();
                                    }
                                }
                            }.start();
                        }else if(item==1){
                            startProgressbar();
                            new Thread(){
                                public void run(){
                                    getAppHandler().importBackup(C.EXT_BACKUP_DIR_PRO,true);
                                    if(progressBar!=null && progressBar.isShowing()){
                                        progressBar.dismiss();
                                    }
                                }
                            }.start();
                        }
                    }
                });
            }else if(backupFileFree.getAbsoluteFile().exists()){
                startProgressbar();
                new Thread(){
                    public void run(){
                        getAppHandler().importBackup(C.EXT_BACKUP_DIR_FREE,true);
                        if(progressBar!=null && progressBar.isShowing()){
                            progressBar.dismiss();
                        }
                        openFragmentSetting(currentSetting);
                    }
                }.start();
            }else if(backupFilePro.getAbsoluteFile().exists()){
                startProgressbar();
                new Thread(){
                    public void run(){
                        getAppHandler().importBackup(C.EXT_BACKUP_DIR_PRO,true);
                        if(progressBar!=null && progressBar.isShowing()){
                            progressBar.dismiss();
                        }
                        openFragmentSetting(currentSetting);
                    }
                }.start();
            }
        }
    }

    private void startProgressbar(){
        if(progressBar!=null && progressBar.isShowing()){
            progressBar.dismiss();
        }
        progressBar = new ProgressDialog(SettingsLockerMain.this);
        progressBar.setCancelable(false);
        progressBar.setMessage(getString(R.string.configuring));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    private void closeFragmentSetting(int position){
        try{
            FragmentNavItem navItem = drawerNavItems.get(position);
            Fragment fragment = navItem.getFragmentClass().newInstance();
            fragment = getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
            if(fragment!=null){
                getSupportFragmentManager().beginTransaction().detach(fragment).commit();
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        try{
            switch(requestCode){
                case NEW_GUIDE:
                    //dialog.usage("showNewNoticeIntroduction1",R.drawable.notice_lollipop,T.NEW_FEATURE,T.NEW_NOTIFICATION,null);
                    break;
                case GoogleDrive.REQUEST_CODE_RESOLUTION:
                    if(resultCode==RESULT_OK && backpupDrive!=null){
                        startProgressbar();
                        backpupDrive.connect();
                    }
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    public void onBackPressed(){
        try{
            if(cBoxEnableLsService.isChecked()){
                super.onBackPressed();
            }else{
                finish();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
            startMainService(cBoxEnableLsService.isChecked());
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            loadSettings();
            loadLicenseCheck();
            loadConfig(cBoxEnableLsService.isChecked());
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void loadSettings(){
        cBoxEnableLsService.setChecked(prefs.getBoolean(P.BLN_ENABLE_SERVICE,false));
    }

    private void loadLicenseCheck(){
        if(!cBoxLockerOk && getPackageName().equals("com.ccs.lockscreen_pro")){
            Toast.makeText(this,"Unlicensed Software, please download C Locker from Google Play Store",Toast.LENGTH_LONG).show();
        }
    }

    private void loadConfig(final boolean bln){
        try{
            if(bln){
                lytMainOptions.setVisibility(View.VISIBLE);
                lytEnablelockerSub.setVisibility(View.VISIBLE);
                if(!isWidgetsSet() ||
                    !isHomeDefault() ||
                    !C.isNotificationEnabled(this) ||
                    !isDeviceAdminActive() ||
                    !isFingerPrintActive() ||
                    !isTipsYoutubeWatched()){

                    if(alert!=null && alert.isShowing()){
                        alert.dismiss();
                        dialogTips();
                    }
                }else if(alert!=null && alert.isShowing()){
                    alert.dismiss();
                }
                config = new Config();
                config.execute();
            }else{
                lytMainOptions.setVisibility(View.GONE);
                lytEnablelockerSub.setVisibility(View.GONE);
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private boolean isWidgetsSet(){
        final DataWidgets db = new DataWidgets(this);
        final ArrayList<InfoWidget> widgetList = (ArrayList<InfoWidget>)db.getAllWidgets();
        if(widgetList==null || widgetList.size()<1){
            db.close();
            return false;
        }
        db.close();
        return true;
    }

    private boolean isHomeDefault(){
        final Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        final String defaultHome = getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;
        final String getMyPackageName = getPackageName();

        return defaultHome.equals(getMyPackageName);
    }

    private boolean isDeviceAdminActive(){
        return deviceAdminHandler.isDeviceAdminActivated();
    }

    private boolean isFingerPrintActive(){
        final Spass spass = new Spass();
        try{
            spass.initialize(this);
            final boolean isFeatureEnabled = spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
            final boolean cBoxFingerprintUnlock = prefs.getBoolean("cBoxFingerprintUnlock",false);
            if(!isFeatureEnabled){
                return true;//no need to have notice
            }
            if(!cBoxFingerprintUnlock){
                return false;
            }
        }catch(SsdkUnsupportedException | UnsupportedOperationException ignored){

        }
        return true;
    }

    private boolean isTipsYoutubeWatched(){
        return prefs.getBoolean("isTipsYoutubeWatched",false);
    }

    private void dialogTips(){
        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final ScrollView scrollView = new ScrollView(this);
            final LinearLayout lyt = new LinearLayout(this);
            lyt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
            lyt.setPadding(C.dpToPx(this,10),C.dpToPx(this,10),C.dpToPx(this,10),C.dpToPx(this,10));
            lyt.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(lyt);

            if(!isWidgetsSet()){
                lyt.addView(addLine());
                lyt.addView(lytMain(DIALOG_WIDGETS));
                lyt.addView(addLine());
            }
            if(!isHomeDefault()){
                lyt.addView(addLine());
                lyt.addView(lytMain(DIALOG_LAYOUT_HOME));
                lyt.addView(addLine());
            }
            if(!C.isNotificationEnabled(this)){
                lyt.addView(addLine());
                lyt.addView(lytMain(DIALOG_LAYOUT_NOTICE));
                lyt.addView(addLine());
            }
            if(!isDeviceAdminActive()){
                lyt.addView(addLine());
                lyt.addView(lytMain(DIALOG_DEVICE_ADMIN));
                lyt.addView(addLine());
            }
            if(!isFingerPrintActive()){
                lyt.addView(addLine());
                lyt.addView(lytMain(DIALOG_FINGERPRINT));
                lyt.addView(addLine());
            }
            if(!isTipsYoutubeWatched()){
                lyt.addView(addLine());
                lyt.addView(lytMain(DIALOG_WIDGET_CLICK));
                lyt.addView(addLine());
            }
            builder.setTitle(getString(R.string.lock_screen_settings)+" "+getString(R.string.settings_widget_help));
            builder.setView(scrollView);
            builder.setPositiveButton(getString(R.string.later),null);
            alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private View addLine(){
        final View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1,1));
        v.setBackgroundColor(Color.parseColor("#888888"));
        return v;
    }

    private LinearLayout lytMain(int val){
        final LinearLayout lyt = new LinearLayout(this);
        lyt.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        lyt.setPadding(0,C.dpToPx(this,10),0,C.dpToPx(this,10));
        lyt.setOrientation(LinearLayout.HORIZONTAL);
        lyt.setGravity(Gravity.CENTER_VERTICAL);
        lyt.addView(lytSub(val));
        lyt.addView(addButton(val));
        return lyt;
    }

    private LinearLayout lytSub(int val){
        final LinearLayout lyt = new LinearLayout(this);
        lyt.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
        lyt.setOrientation(LinearLayout.VERTICAL);
        lyt.setGravity(Gravity.CENTER_VERTICAL);

        switch(val){
            case DIALOG_WIDGETS:
                lyt.addView(addTxtHead(getString(R.string.widget_profile)));
                lyt.addView(addTxtDesc(getString(R.string.settings_widget_desc)));
                break;
            case DIALOG_LAYOUT_HOME:
                lyt.addView(addTxtHead(getString(R.string.security_set_homekey_)));
                lyt.addView(addTxtDesc(getString(R.string.security_set_homekey_desc)));
                break;
            case DIALOG_LAYOUT_NOTICE:
                lyt.addView(addTxtHead(getString(R.string.bottom_bar_statusbar_notices)));
                lyt.addView(addTxtDesc(getString(R.string.bottom_bar_statusbar_notices_desc1)));
                break;
            case DIALOG_DEVICE_ADMIN:
                lyt.addView(addTxtHead(getString(R.string.device_admin)));
                lyt.addView(addTxtDesc(getString(R.string.device_admin_desc)));
                break;
            case DIALOG_FINGERPRINT:
                lyt.addView(addTxtHead(getString(R.string.firnger_print)));
                lyt.addView(addTxtDesc(getString(R.string.firnger_print_desc)));
                break;
            case DIALOG_WIDGET_CLICK:
                lyt.addView(addTxtHead(getString(R.string.tips_tricks)));
                lyt.addView(addTxtDesc(getString(R.string.tips_tricks_decs)));
                break;
        }
        return lyt;
    }

    private Button addButton(final int val){
        final Button btn = new Button(this);
        btn.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        btn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i;
                switch(val){
                    case DIALOG_WIDGETS:
                        selectDrawerItem(lytNotification,PROFILES,true);
                        i = new Intent(SettingsLockerMain.this,SettingsWidgets.class);
                        startActivity(i);
                        alert.dismiss();
                        break;
                    case DIALOG_LAYOUT_HOME:
                        selectDrawerItem(lytSecurity,SECURITY,true);
                        alert.dismiss();
                        break;
                    case DIALOG_LAYOUT_NOTICE:
                        selectDrawerItem(lytNotification,NOTICE,true);
                        alert.dismiss();
                        break;
                    case DIALOG_DEVICE_ADMIN:
                        selectDrawerItem(lytSecurity,SECURITY,true);
                        alert.dismiss();
                        break;
                    case DIALOG_FINGERPRINT:
                        selectDrawerItem(lytSecurity,SECURITY,true);
                        alert.dismiss();
                        break;
                    case DIALOG_WIDGET_CLICK:
                        i = new Intent(SettingsLockerMain.this,SettingsTipsYoutube.class);
                        startActivity(i);
                        alert.dismiss();
                        break;
                }
            }
        });
        switch(val){
            case DIALOG_WIDGETS:
                btn.setText(getString(R.string.set));
                break;
            case DIALOG_LAYOUT_HOME:
                btn.setText(getString(R.string.set));
                break;
            case DIALOG_LAYOUT_NOTICE:
                btn.setText(getString(R.string.set));
                break;
            case DIALOG_DEVICE_ADMIN:
                btn.setText(getString(R.string.set));
                break;
            case DIALOG_FINGERPRINT:
                btn.setText(getString(R.string.set));
                break;
            case DIALOG_WIDGET_CLICK:
                btn.setText(getString(R.string.watch));
                break;
        }
        return btn;
    }

    private TextView addTxtHead(String str){
        final TextView txt = new TextView(this);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        txt.setText(str);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        return txt;
    }

    private TextView addTxtDesc(String str){
        final TextView txt = new TextView(this);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
        txt.setText(str);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        return txt;
    }

    //drawer
    private void selectDrawerItem(View v,int position,boolean closeDrawer){
        lytLockerProfiles.setBackgroundColor(0);
        lytVisual.setBackgroundColor(0);
        lytNotification.setBackgroundColor(0);
        lytSecurity.setBackgroundColor(0);
        lytOtherOptions.setBackgroundColor(0);

        v.setBackgroundColor(Color.parseColor("#88888888"));
        setToolbarTitle(titles[position]);
        if(currentSetting!=position){
            currentSetting = position;
            lytMainOptions.removeAllViews();
        }
        if(closeDrawer){
            if(drawer!=null){
                drawer.closeDrawer(Gravity.START);
            }else{
                setToolbarTitle(titles[currentSetting]);
                if(lytMainOptions.getChildCount()==0){
                    openFragmentSetting(currentSetting);
                }
            }
        }
    }

    private void openFragmentSetting(int position){
        try{
            showInterstitialAd();

            FragmentNavItem navItem = drawerNavItems.get(position);
            Fragment fragment = navItem.getFragmentClass().newInstance();
            Bundle args = navItem.getFragmentArgs();
            if(args!=null){
                fragment.setArguments(args);
            }
            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                // (.add) will not save setting as opPause() is not called
                //.add(R.id.lytMainOptions,fragment,fragment.getClass().getSimpleName())
                .replace(R.id.lytMainOptions,fragment,fragment.getClass().getSimpleName()).commit();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void showInterstitialAd(){
        if(mInterstitialAd!=null && mInterstitialAd.isLoaded()){
            mInterstitialAd.show();
        }
    }

    private void saveSettings(){
        editor.putBoolean(P.BLN_ENABLE_SERVICE,cBoxEnableLsService.isChecked());
        editor.putInt("intDefaultScreenTimeOutVal",getScreenTimeOutValue());
        editor.putBoolean("isSMSNoticeSupported",isSMSNoticeSupported);
        editor.commit();
    }

    private void startMainService(boolean bln){
        try{
            if(bln){
                startService(new Intent(getBaseContext(),ServiceMain.class));
            }else{
                stopService(new Intent(getBaseContext(),ServiceMain.class));
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private int getScreenTimeOutValue(){
        try{
            return Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT);
        }catch(SettingNotFoundException e){
            saveErrorLogs(null,e);
            return 60000;
        }
    }

    private void loadAds(){
        try{
            if(!cBoxLockerOk){
                boolean isFisttimeAds = prefs.getBoolean("isFisttimeAds",true);
                boolean isBannerAds = prefs.getBoolean("isBannerAds",true);
                AdRequest adRequest = new AdRequest.Builder().build();
                if(isFisttimeAds){
                    Log.e("loadAds","isFisttimeAds");
                    adView = new AdView(this);
                    adView.setAdSize(AdSize.BANNER);
                    adView.setAdUnitId(C.ADMOB_ID_BANNER);

                    layoutAds = (LinearLayout)findViewById(R.id.lytSettingsMainAds);
                    layoutAds.addView(adView);

                    adView.loadAd(adRequest);

                    editor.putBoolean("isFisttimeAds",false);
                    editor.commit();
                }else if(isBannerAds){
                    Log.e("loadAds","isBannerAds");
                    adView = new AdView(this);
                    adView.setAdSize(AdSize.BANNER);
                    adView.setAdUnitId(C.ADMOB_ID_BANNER);

                    layoutAds = (LinearLayout)findViewById(R.id.lytSettingsMainAds);
                    layoutAds.addView(adView);

                    adView.loadAd(adRequest);

                    editor.putBoolean("isBannerAds",false);
                    editor.commit();
                }else{
                    Log.e("loadAds","mInterstitialAd");
                    mInterstitialAd = new InterstitialAd(this);
                    mInterstitialAd.setAdUnitId(C.ADMOB_ID_INTERS);
                    mInterstitialAd.setAdListener(new AdListener(){
                        @Override
                        public void onAdClosed(){
                            mInterstitialAd = null;
                        }
                    });
                    mInterstitialAd.loadAd(adRequest);
                    //new Handler().postDelayed(new Runnable(){@Override public void run(){
                    //showInterstitialAd();
                    //}},2000);
                    editor.putBoolean("isBannerAds",true);
                    editor.commit();
                }
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void checkLicense(boolean enableFullFeatures){
        if(enableFullFeatures){
            cBoxLockerOk = true;

            editor.putBoolean(C.C_LOCKER_OK,true);
            editor.putBoolean(C.LICENCE_CHECK_TRY,false); //it's free promotion
            editor.putBoolean(C.PROMOTION_APP,true);
            editor.commit();
        }else{
            cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);//Google Play
        }
        handler.post(new Runnable(){
            @Override
            public void run(){
                final boolean isPromotionApp = prefs.getBoolean(C.PROMOTION_APP,false);
                if(isPromotionApp){
                    return;
                }
                getAppHandler().checkLicense();
            }
        });
    }

    private void checkPromotion(){
        handler.post(new Runnable(){
            @Override
            public void run(){
                final boolean isPromotionApp = prefs.getBoolean(C.PROMOTION_APP,false);
                if(isPromotionApp){
                    return;
                }
                if(getAppHandler().isAppPromotion()){
                    dialogPromotion(getString(R.string.promotion_day_desc2),getString(R.string.activate),true);
                }
            }
        });
    }

    private void checkNewVersion(){
        String version;
        try{
            version = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        }catch(NameNotFoundException e){
            version = "";
            saveErrorLogs(null,e);
        }

        boolean isNewVersion = prefs.getBoolean("isNewVersion"+version,true);
        if(isNewVersion){
            try{
                //show quick guide
                startActivityForResult(new Intent(this,SettingsTips.class),NEW_GUIDE);
                //delete old logs
                getAppHandler().clearLogs();
                getAppHandler().writeToFile(C.FILE_BACKUP_RECORD,"Old files deleted (isNewVersion)");
                editor.putBoolean("isNewVersion"+version,false);
                editor.commit();
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
        }
    }

    private boolean checkNewCLLite(){
        if(this.getPackageName().equals(C.PKG_NAME_PRO)){
            return false;
        }
        final String checkNewCLLite = "checkNewCLLiteNoMore";//"checkNewCLLite";
        if(!prefs.getBoolean(checkNewCLLite,false)){
            return false;//user does not wish to show msg if true
        }
        long longInstalledDate;
        long date = System.currentTimeMillis();
        try{
            longInstalledDate = getPackageManager().getPackageInfo(getPackageName(),0).firstInstallTime;

            long day = 1000*60*60*24;
            if(date-longInstalledDate<day){
                return false;
            }
        }catch(NameNotFoundException e){
            e.printStackTrace();
        }

        String title = "New C Locker Lite";
        String msg = "New C Locker Lite is available in playstore now, "+"it's designed to be a Simple, Smart and Secure Locker.\n\n"+"Try it from playstore now?";
        dialog.usage(checkNewCLLite,R.drawable.cll_feature_img,title,msg,new OnDialogListener(){
            @Override
            public void onDialogClickListener(int dialogType,int result){
                if(result==MyAlertDialog.OK){
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("market://details?id="+"com.ccs.lockscreen_lite"));
                    startActivity(i);
                }
            }
        });
        return true;
    }

    private boolean promoteRating(){
        final String promoteRating = "promoteRating";
        if(prefs.getBoolean(promoteRating,false)){
            return false;//user does not wish to show msg if true
        }
        long date = System.currentTimeMillis();
        try{
            final long longLastUpDate = getPackageManager().getPackageInfo(getPackageName(),0).lastUpdateTime;

            long day = 1000*60*60*24*2;// 3 days
            if(date-longLastUpDate<day){
                return false;
            }
        }catch(NameNotFoundException e){
            e.printStackTrace();
        }

        String title = "Please Rate Me";
        String msg = getString(R.string.settings_rate_myapp_desc)+
            "\n\nYour support is a very important motivation for me keep C Locker development moving.."+
            "\n\nThank you very much!";
        dialog.usage(promoteRating,0,title,msg,new OnDialogListener(){
            @Override
            public void onDialogClickListener(int dialogType,int result){
                if(result==MyAlertDialog.OK){
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("market://details?id="+getPackageName()));
                    startActivity(i);
                }
            }
        });
        return true;
    }

    private void loadViewIds(){
        //DIALOG_LOCKSCREEN = getString(R.string.ls_notice);
        drawer = (DrawerLayout)findViewById(R.id.drawerSettingsLockerMain);

        lytEnableLocker = findViewById(R.id.lytEnablelockerMain);
        lytMainOptions = (LinearLayout)findViewById(R.id.lytMainOptions);
        lytEnablelockerSub = (LinearLayout)findViewById(R.id.lytEnablelockerSub);

        lytLockerProfiles = (LinearLayout)findViewById(R.id.lytLockerProfiles);
        lytUnlockingStyle = (LinearLayout)findViewById(R.id.lytUnlockingStyle);
        lytVisual = (LinearLayout)findViewById(R.id.lytVisual);
        lytSecurity = (LinearLayout)findViewById(R.id.lytSecurity);
        lytNotification = (LinearLayout)findViewById(R.id.lytNotification);
        lytOtherOptions = (LinearLayout)findViewById(R.id.lytOtherOptions);

        txtQuickGuide = (TextView)findViewById(R.id.txtQuickGuide);
        txtTips = (TextView)findViewById(R.id.txtTips);
        txtBackupRestore = (TextView)findViewById(R.id.txtBackupRestore);
        txtEmailMe = (TextView)findViewById(R.id.txtEmailMe);
        txtAboutCLocker = (TextView)findViewById(R.id.txtAboutCLocker);

        cBoxEnableLsService = (CheckBox)findViewById(R.id.cBoxEnablelockScreen);
    }

    private void loadDrawer(){
        if(drawer!=null){
            drawer.setDrawerShadow(R.drawable.drawer_shadow,Gravity.START);
            drawer.openDrawer(Gravity.START);

            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,drawer,getToolbar(),R.string.app_name,R.string.app_name){
                public void onDrawerOpened(View drawerView){
                    super.onDrawerOpened(drawerView);
                    setToolbarTitle(R.string.lock_screen_settings);
                }

                public void onDrawerClosed(View view){
                    super.onDrawerClosed(view);
                    setToolbarTitle(titles[currentSetting]);
                    if(lytMainOptions.getChildCount()==0){
                        openFragmentSetting(currentSetting);
                    }
                }
            };
            drawer.addDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }else{
            ActionBar actionBar = getSupportActionBar();
            if(actionBar!=null){
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }

        titles = new String[]{getString(R.string.Locker_Profiles),getString(R.string.settings_main_visual),getString(R.string.notice_settings),getString(R.string.security),getString(R.string.other_options)};

        drawerNavItems = new ArrayList<>();
        addNavItem(titles[PROFILES],SettingsProfiles.class);
        addNavItem(titles[VISUAL],SettingsDisplay.class);
        addNavItem(titles[NOTICE],SettingsNotices.class);
        addNavItem(titles[SECURITY],SettingsSecurity.class);
        addNavItem(titles[OTHER_OPTIONS],SettingsOtherOptions.class);
    }

    private void loadLockerProInstalledCheck(){
        handler.post(new Runnable(){
            @Override
            public void run(){
                try{
                    if(getPackageName().equals(C.PKG_NAME_PRO)){
                        if(isAppInstalled(C.PKG_NAME_FREE)){
                            Intent i = new Intent(C.DISABLE_LOCKER_SEND);
                            sendBroadcast(i);
                        }
                    }else{
                        if(isAppInstalled(C.PKG_NAME_PRO)){
                            getAppHandler().exportBackup(true);//export backup before closing

                            final Intent i = new Intent(Intent.ACTION_MAIN);
                            i.setClassName(C.PKG_NAME_PRO,C.PKG_NAME_PRO+".SettingsLockerMain");
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);

                            cBoxEnableLsService.setChecked(false);
                            finish();
                        }
                    }
                }catch(Exception e){
                    saveErrorLogs(null,e);
                }
            }
        });
    }

    private boolean isAppInstalled(String appPkg){
        try{
            final PackageManager pm = getPackageManager();
            final Intent it = new Intent(Intent.ACTION_MAIN);
            it.addCategory(Intent.CATEGORY_LAUNCHER);
            pm.queryIntentActivities(it,0);
            final List<ResolveInfo> list = pm.queryIntentActivities(it,0);

            for(int i = 0; i<list.size(); i++){
                final ResolveInfo app = list.get(i);
                if(app.activityInfo.packageName.equals(appPkg)){
                    getAppHandler().writeToFile(C.FILE_BACKUP_RECORD,"SettingsLockerMain>isAppInstalled: "+appPkg);
                    return true;
                }
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClickFunction(){
        cBoxEnableLsService.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEnableLsService.isChecked()){
                    loadConfig(true);
                }else{
                    loadConfig(false);
                }
            }
        });
        lytEnableLocker.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                cBoxEnableLsService.performClick();
            }
        });

        lytLockerProfiles.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event){
                return onlytTouch(event,v,PROFILES);
            }
        });
        lytUnlockingStyle.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(SettingsLockerMain.this,SettingsUnlockSelection.class));
                if(currentSetting==SECURITY){
                    selectDrawerItem(lytLockerProfiles,PROFILES,true);//set to locker profile if it's in security profile
                }
            }
        });
        lytVisual.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event){
                return onlytTouch(event,v,VISUAL);
            }
        });
        lytNotification.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event){
                return onlytTouch(event,v,NOTICE);
            }
        });
        lytSecurity.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event){
                return onlytTouch(event,v,SECURITY);
            }
        });
        lytOtherOptions.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event){
                return onlytTouch(event,v,OTHER_OPTIONS);
            }
        });

        txtQuickGuide.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(SettingsLockerMain.this,SettingsTips.class));
            }
        });
        txtTips.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(!isWidgetsSet() ||
                    !isHomeDefault() ||
                    !C.isNotificationEnabled(SettingsLockerMain.this) ||
                    !isDeviceAdminActive() ||
                    !isFingerPrintActive() ||
                    !isTipsYoutubeWatched()){

                    dialogTips();
                    if(!cBoxEnableLsService.isChecked()){
                        cBoxEnableLsService.performClick();
                    }
                }
            }
        });
        txtBackupRestore.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                dialog.selectBackupRestore(new OnDialogListener(){
                    @Override
                    public void onDialogClickListener(int dialogType,int result){
                        switch(result){
                            case 0:
                                handleExportBackup(false);
                                break;
                            case 1:
                                handleExportBackup(true);
                                break;
                            case 2:
                                handleImportBackup(false);
                                break;
                            case 3:
                                handleImportBackup(true);
                                break;
                        }
                    }
                });
            }
        });
        txtEmailMe.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                getAppHandler().sentLogs("C Locker Log Information","",true);
            }
        });
        txtAboutCLocker.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Intent i = new Intent(SettingsLockerMain.this,SettingsAbout.class);
                startActivity(i);
            }
        });
    }

    private boolean onlytTouch(MotionEvent m,View v,int position){
        switch(m.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                hasMoved = false;
                moveGap = C.dpToPx(this,10);

                STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP
                break;
            case MotionEvent.ACTION_MOVE:
                int gapX = (int)(STARTING_RAW_X-m.getRawX());
                int gapY = (int)(STARTING_RAW_Y-m.getRawY());

                if(gapX>moveGap || gapX<(-moveGap)){
                    hasMoved = true;
                    return false;
                }
                if(gapY>moveGap || gapY<(-moveGap)){
                    hasMoved = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(hasMoved){
                    return false;
                }
                selectDrawerItem(v,position,true);
                break;
        }
        return true;
    }

    private boolean isSMSUriValid(){
        try{
            Cursor cursor = null;
            try{
                final Uri uri = Uri.parse(C.SMS_URI);
                final String[] projection = new String[]{"*"};
                final String selection = ("read"+"=?");
                final String[] arg = new String[]{"0"};//"1"=read,"0"=unread
                final String sort = "date DESC";//"startDay ASC, startMinute ASC"

                cursor = getContentResolver().query(uri,projection,selection,arg,sort);
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return true;
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        return false;
    }

    //dialog promotion
    private void dialogPromotion(final String msg,final String button,final boolean activate){
        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.promotion_day_desc1));
            builder.setMessage(msg);
            builder.setPositiveButton(button,new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which){
                    if(activate){
                        startProgressbar();
                        getAppHandler().activateAppPromotion();
                    }
                }
            });
            builder.setCancelable(false);
            alert = builder.create();
            alert.show();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void addNavItem(String title,Class<? extends Fragment> fragmentClass){
        drawerNavItems.add(new FragmentNavItem(title,fragmentClass));
    }

    private class Config extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... arg0){
            isFirstLaunch = prefs.getBoolean("isFirstLaunch",true);
            saveImage(C.ICON_LOCK,R.drawable.ic_https_white_48dp);
            saveImage(C.ICON_SHORTCUT_01,R.drawable.ic_lock_open_white_48dp);
            saveImage(C.ICON_SHORTCUT_02,R.drawable.ic_flashlight_white_48dp);
            saveImage(C.ICON_SHORTCUT_03,R.drawable.ic_camera_alt_white_48dp);
            saveImage(C.ICON_SHORTCUT_04,R.drawable.ic_youtube_play_white_48dp);
            saveImage(C.ICON_SHORTCUT_05,R.drawable.ic_earth_white_48dp);
            saveImage(C.ICON_SHORTCUT_06,R.drawable.ic_location_on_white_48dp);
            saveImage(C.ICON_SHORTCUT_07,R.drawable.ic_call_white_48dp);
            saveImage(C.ICON_SHORTCUT_08,R.drawable.ic_textsms_white_48dp);

            addDefaultwidget();
            resetNoticeApps();
            return null;
        }

        @Override
        protected void onPreExecute(){
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
            progressBar = new ProgressDialog(SettingsLockerMain.this);
            progressBar.setCancelable(true);
            progressBar.setMessage(getString(R.string.configuring));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
        }

        @Override
        protected void onPostExecute(Void resultOK){
            checkBackupSetting();
            editor.putBoolean("isFirstLaunch",false);
            editor.commit();
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }

        private void checkBackupSetting(){
            if(isFirstLaunch){
                final File backupFileFree = new File(C.EXT_BACKUP_DIR_FREE,C.FILE_PREFS);
                final File backupFilePro = new File(C.EXT_BACKUP_DIR_PRO,C.FILE_PREFS);
                if(backupFileFree.getAbsoluteFile().exists() || backupFilePro.getAbsoluteFile().exists()){
                    dialog.foundBackup(new OnDialogListener(){
                        @Override
                        public void onDialogClickListener(int dialogType,int result){
                            handleImportBackup(false);
                        }
                    });
                }
            }
        }

        private void saveImage(final String imgName,final int drawableIcon){
            try{
                //int w = C.WIDGET_PROFILE_LOCATION;
                final File oldIcon = new File(C.EXT_STORAGE_DIR,imgName);
                File newIcon;
                if(imgName.equals(C.ICON_LOCK)){
                    newIcon = new File(C.EXT_STORAGE_DIR,imgName);
                    if(!newIcon.getParentFile().exists()){
                        newIcon.getParentFile().mkdir();
                    }
                    if(!newIcon.getAbsoluteFile().exists() || isFirstLaunch){
                        final Bitmap bmIcon = ((BitmapDrawable)ContextCompat.getDrawable(getBaseContext(),drawableIcon)).getBitmap();
                        final OutputStream outStream = new FileOutputStream(newIcon);
                        bmIcon.compress(Bitmap.CompressFormat.PNG,100,outStream);
                        outStream.flush();
                        outStream.close();
                    }
                }else{
                    for(int i = C.PROFILE_DEFAULT; i<=C.PROFILE_BLUETOOTH; i++){
                        newIcon = new File(C.EXT_STORAGE_DIR,imgName+i);
                        if(!newIcon.getParentFile().exists()){
                            newIcon.getParentFile().mkdir();
                        }
                        if(oldIcon.getAbsoluteFile().exists() && i==C.PROFILE_DEFAULT){
                            oldIcon.renameTo(newIcon.getAbsoluteFile());
                        }else{
                            if(!newIcon.getAbsoluteFile().exists() || isFirstLaunch){
                                final Bitmap bmIcon = ((BitmapDrawable)ContextCompat.getDrawable(getBaseContext(),drawableIcon)).getBitmap();
                                final OutputStream outStream = new FileOutputStream(newIcon);
                                bmIcon.compress(Bitmap.CompressFormat.PNG,100,outStream);
                                outStream.flush();
                                outStream.close();
                            }
                        }
                    }
                }
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
        }

        private void addDefaultwidget(){
            if(isFirstLaunch){
                final InfoWidget widgets = new InfoWidget(-1,//primary id not using
                    C.PROFILE_DEFAULT,C.CLOCK_DIGITAL_CENTER,DataWidgets.WIDGET_TYPE_DEFAULT,getPackageName(),//no use
                    getPackageName(),0,0,0,C.getMaxWidgetSpanX(SettingsLockerMain.this),1,0,0);

                final DataWidgets dbWidgets = new DataWidgets(SettingsLockerMain.this);
                dbWidgets.addWidget(widgets);
                dbWidgets.close();
            }
        }

        private void resetNoticeApps(){//delete all notice apps selection > version code: 470
            DataAppsSelection db = new DataAppsSelection(SettingsLockerMain.this);
            try{
                db.checkShortcutData();//double check empty date
                if(prefs.getBoolean("resetSelectedNoticeApps",true)){
                    db.deleteAllApps(DataAppsSelection.APP_TYPE_BLOCKED_NOTICE_APPS);
                    editor.putBoolean("resetSelectedNoticeApps",false);
                    editor.commit();
                }
            }catch(Exception e){
                saveErrorLogs(null,e);
            }finally{
                Log.e("resetNoticeApps","db.close()");
                db.close();
            }
        }
    }

    private class FragmentNavItem{
        private Class<? extends Fragment> fragmentClass;
        //private String title;
        private Bundle fragmentArgs;

        FragmentNavItem(String title,Class<? extends Fragment> fragmentClass){
            this(title,fragmentClass,null);
        }

        FragmentNavItem(String title,Class<? extends Fragment> fragmentClass,Bundle args){
            this.fragmentClass = fragmentClass;
            this.fragmentArgs = args;
            //this.title = title;
        }

        Class<? extends Fragment> getFragmentClass(){
            return fragmentClass;
        }

        Bundle getFragmentArgs(){
            return fragmentArgs;
        }
    }
}