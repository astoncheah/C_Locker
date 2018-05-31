package com.ccs.lockscreen.myclocker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.DataWidgets;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.data.InfoWidget;
import com.ccs.lockscreen.utils.SntpClient;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MyCLocker implements LicenseCheckerCallback{
    //TODO default val day = 0, year = 0, example EX_DAY = 200,EX_YEAR = 2014
    private static final int EX_DAY = 0, EX_YEAR = 0;
    //TODO free full unlocked features promotion 5/8/2014
    //private static final int PROMO_YEAR = 2014,PROMO_MONTH = 8,PROMO_DAY = 5;
    private static final int PROMO_YEAR = 2014, PROMO_MONTH = 9, PROMO_DAY = 27;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(C.DATE_FORMAT_LOG,Locale.getDefault());
    private static final String WIDGET = "Widget";
    private static final String APP_SELECTION = "AppSelection";
    // important!! dont change it!!
    private static final byte[] SALT = new byte[]{-46,65,30,-112,-103,-57,74,-64,51,118,-95,-45,77,-117,-33,-113,-111,32,-124,89};
    private static boolean IS_DEBUG_MODE = false;
    private static String LOCKER_PRO_PUBLIC_KEY = "KEY_IS_HIDDEN";
    private static String APP_SIGNATURE_KEY = "KEY_IS_HIDDEN";
    private static String Marcin_Krasicki = "cab8bbaa937860aa";//Polish Translator
    private static String martinus = "d446cb8005bfa2bc";//Dutch Translator
    private static String Wim_N = "a1af0f28628b6414";//Dutch Translator
    private static String gaich = "bc97a7f74640ad29";//Russian Translator
    private static String pakitos = "78ea903f1b016260";//Spanish Translator
    private static String Markus = "62350be7957fa8c7";//German Translator
    private static String Antoder10 = "ef71a4f94b2b7626";//Italian Translator
    private static String crosshair = "43b3419e6738af23";//Croatian Translator
    private static String Nikola_Stojanovic = "7730e6c95fb7ef34";//Serbian Translator
    private static String TwoBit_Gary = "be9701b76ba50ee3";//HomeKey bug for paid app
    private static String JingLin = "5009bf655396e406";//SJL
    private static String Andrea = "27c5fb2ec74265df";//Problem from google verification
    private static String Ilan = "73427b6320db5df";//Problem from google verification
    private static String Miles_Crissey = "7e9b6b7c2016b7f7";//tester
    private String timeLogs;
    private SharedPreferences.Editor editor;
    private Context context;
    private Handler handler;
    private String strPkgName;
    private InputStream inStream;
    private OutputStream outStream;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private int profile = -1, widgetId = -1, type = -1, index = -1, x = -1, y = -1, spanX = -1, spanY = -1, width = -1, height = -1;
    private int appType = -1, shortcutId = -1, shortcutProfile = C.PROFILE_DEFAULT, shortcutAction = DataAppsSelection.ACTION_DEFAULT;
    //shortcutPIN = DataAppsSelection.PIN_NOT_REQUIRED;
    private String widgetPkgName = "", widgetClassName = "", appPkgName = "0", appClassName = "0", appName = "0", appSubInfo = "0";

    public MyCLocker(Context context){
        try{
            handler = new Handler();
            this.context = context;
            this.strPkgName = context.getPackageName();
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    public final void saveErrorLog(String extraMsg,Exception e){
        try{
            if(e!=null){
                Log.e(C.LOG,extraMsg+" error");
                e.printStackTrace();
            }

            final File fileTo = new File(C.EXT_STORAGE_DIR,C.FILE_MY_LOG);
            if(!fileTo.getParentFile().exists()){
                fileTo.getParentFile().mkdir();
                if(!fileTo.exists()){
                    fileTo.createNewFile();
                }
            }
            final BufferedWriter buf = new BufferedWriter(new FileWriter(fileTo,true));
            if(extraMsg==null){
                buf.append("");
                buf.newLine();
            }else{
                timeLogs = DATE_FORMAT.format(new Date());
                buf.append(timeLogs).append("/").append(extraMsg);
                buf.newLine();
            }
            if(e!=null && e.getStackTrace()!=null){
                buf.append(e.toString());
                buf.newLine();
                buf.append(getStackTracetoString(e.getStackTrace()));
                buf.newLine();
                buf.newLine();
            }
            buf.close();
        }catch(Exception er){
            er.printStackTrace();
        }
    }

    private String getStackTracetoString(StackTraceElement[] stackTraceElements){
        if(stackTraceElements==null){
            return null;
        }
        timeLogs = DATE_FORMAT.format(new Date());
        final StringBuilder stringBuilder = new StringBuilder();
        for(StackTraceElement element : stackTraceElements){
            stringBuilder.append(timeLogs).append("/").append(element.toString()).append(getPkgName()).append("\n");
        }
        return stringBuilder.toString();
    }

    private String getPkgName(){
        if(strPkgName!=null && strPkgName.equals(C.PKG_NAME_PRO)){
            return "(Pro)";
        }else{
            return "";
        }
    }

    public final void checkLicense(){//use getApplicationContext to prevent leak memory
        try{
            editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
            if(context.getPackageName().equals(C.PKG_NAME_PRO)){
                final Calendar c = Calendar.getInstance();
                if(c.get(Calendar.DAY_OF_YEAR)>=EX_DAY && c.get(Calendar.YEAR)>=EX_YEAR){
                    //proceed license check
                }else if(c.get(Calendar.YEAR)>=(EX_YEAR+1)){
                    //proceed license check
                }else{
                    //within trial period
                    return;
                }
                if(!isDebugMode(context)){
                    if(!isLicensedSignature(context)){
                        editor.putBoolean(C.C_LOCKER_OK,false);
                        editor.putBoolean(C.LICENCE_CHECK_TRY,true);
                        editor.commit();
                        writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>checkLicense>isLicensedSignature: MODIFIED");
                        showMsg("Unlicensed Software, please download C Locker from Google Play Store");
                        return;
                    }
                }
                String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
                if(installer==null){// if the app is not installed from google play then proceed lucky patcher check
                    if(isLuckyPatchInstalled()){
                        editor.putBoolean(C.C_LOCKER_OK,false);
                        editor.putBoolean(C.LICENCE_CHECK_TRY,true);
                        editor.commit();
                        writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>checkLicense: Unlicensed software found: Lucky Patcher");
                        showMsg("Unlicensed software found: Lucky Patcher, please contact developer to unlock [Pro] features");
                    }
                }
                if(installer!=null && installer.equals("com.android.vending")){
                    editor.putBoolean(C.C_LOCKER_OK,true);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,false);
                    editor.commit();
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>checkLicense 1: LICENSED: "+getAndroidId(context));
                    return;
                }
                if(isFullFeaturesUser()){
                    editor.putBoolean(C.C_LOCKER_OK,true);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,false);
                    editor.commit();
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>checkLicense 2: LICENSED: "+getAndroidId(context));
                    return;
                }
                // Construct the LicenseCheckerCallback. The library calls this when done.
                mLicenseCheckerCallback = this;
                // Construct the LicenseChecker with a Policy.
                final AESObfuscator aesF = new AESObfuscator(SALT,context.getPackageName(),getAndroidId(context));
                final ServerManagedPolicy smp = new ServerManagedPolicy(context,aesF);
                mChecker = new LicenseChecker(context.getApplicationContext(),smp,LOCKER_PRO_PUBLIC_KEY);
                mChecker.checkAccess(mLicenseCheckerCallback);
            }else{
                if(isFullFeaturesUser()){
                    editor.putBoolean(C.C_LOCKER_OK,true);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,false);
                    editor.commit();
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>checkLicense2: LICENSED: "+getAndroidId(context));
                    return;
                }
                editor.putBoolean(C.C_LOCKER_OK,false);
                editor.putBoolean(C.LICENCE_CHECK_TRY,false);
                editor.commit();
            }
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    public final boolean isAppPromotion(){
        try{
            if(!context.getPackageName().equals(C.PKG_NAME_PRO)){
                final long systemTime = System.currentTimeMillis();
                final long longInstalledDate = context.getPackageManager().getPackageInfo(context.getPackageName(),0).firstInstallTime;
                if(isPromotionTime(systemTime) && isPromotionTime(longInstalledDate)){
                    return true;
                }
            }
        }catch(Exception e){
            saveErrorLog(null,e);
        }
        return false;
    }

    private boolean isPromotionTime(final long time){
        int year = getDate(time,Calendar.YEAR);
        int month = getDate(time,Calendar.MONTH);
        int day = getDate(time,Calendar.DAY_OF_MONTH);
        //Log.e("isPromotionTime","isPromotionTime: "+year+"/"+month+"/"+day);
        if(year==PROMO_YEAR){
            if(month==PROMO_MONTH){
                if(day==PROMO_DAY || day==(PROMO_DAY+1) || day==(PROMO_DAY-1)){
                    return true;
                }
            }
        }
        return false;
    }

    private int getDate(final long date,final int returnVal){
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        if(returnVal==Calendar.MONTH){
            return c.get(returnVal)+1;
        }
        return c.get(returnVal);
    }

    public final void activateAppPromotion(){
        new GetInternetTime().execute();
    }

    public final void close(){
        if(mChecker!=null){
            mChecker.onDestroy();
        }
    }

    private void showMsg(final String msg){
        handler.post(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isFullFeaturesUser(){
        return getAndroidId(context).equals(Marcin_Krasicki) || getAndroidId(context).equals(martinus) || getAndroidId(context).equals(Wim_N) || getAndroidId(context).equals(gaich) || getAndroidId(context).equals(pakitos) || getAndroidId(context).equals(Markus) || getAndroidId(context).equals(Antoder10) || getAndroidId(context).equals(crosshair) || getAndroidId(context).equals(Nikola_Stojanovic) || getAndroidId(context).equals(TwoBit_Gary) || getAndroidId(context).equals(JingLin) || getAndroidId(context).equals(Andrea) || getAndroidId(context).equals(Ilan) || getAndroidId(context).equals(Miles_Crissey);
    }

    private boolean isDebugMode(final Context context){
        int flag = context.getApplicationContext().getApplicationInfo().flags;
        //Log.e("isDebugMode",flag+"");
        int mode = ApplicationInfo.FLAG_DEBUGGABLE;
        int val = flag &= mode;
        //Log.e("isDebugMode",flag+"/"+mode+"/"+val);
        return val!=0;
    }

    private boolean isLicensedSignature(final Context context){
        try{
            Signature[] signatures = context.getPackageManager().getPackageInfo(context.getPackageName(),PackageManager.GET_SIGNATURES).signatures;
            //writeToFile(C.EXT_BACKUP_DIR,C.FILE_BACKUP_RECORD,"MyCLocker>checkLicense>isLicensedSignature: "+signatures[0].toCharsString());
            if(signatures[0].toCharsString().equals(APP_SIGNATURE_KEY)){
                //android.os.Process.killProcess(android.os.Process.myPid());
                return true;
            }
        }catch(NameNotFoundException ex){
            //android.os.Process.killProcess(android.os.Process.myPid());
        }
        return false;
    }

    private boolean isLuckyPatchInstalled(){
        final String pkg1 = new String(Base64.decode("Y29tLmRpbW9udmlkZW8ubHVja3lwYXRjaGVy",Base64.DEFAULT));
        final String pkg2 = new String(Base64.decode("Y29tLmNoZWxwdXMubGFja3lwYXRjaA==",Base64.DEFAULT));
        if(isPackageInstalled(pkg1)){
            return true;
        }
        return isPackageInstalled(pkg2);
    }

    private boolean isPackageInstalled(final String packageName){
        try{
            final ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName,0);
            return info!=null;
        }catch(NameNotFoundException e){
            //saveErrorLog(null,e);
        }catch(Exception e){
            //saveErrorLog(null,e);
        }
        return false;
    }

    @Override
    public void allow(int reason){
        try{
            switch(reason){
                case Policy.RETRY:
                    editor.putBoolean(C.C_LOCKER_OK,true);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,true);
                    editor.commit();
                    writeToFile("MyCLocker>allow: RETRY");
                    break;
                case Policy.LICENSED:
                    editor.putBoolean(C.C_LOCKER_OK,true);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,false);
                    editor.commit();
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>allow: LICENSED");
                    break;
            }
            close();
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    @Override
    public void dontAllow(int reason){
        try{
            switch(reason){
                case Policy.RETRY:
                    editor.putBoolean(C.C_LOCKER_OK,true);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,true);
                    editor.commit();
                    writeToFile("MyCLocker>dontAllow: RETRY");
                    break;
                case Policy.NOT_LICENSED:
                    editor.putBoolean(C.C_LOCKER_OK,false);
                    editor.putBoolean(C.LICENCE_CHECK_TRY,true);
                    editor.commit();
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>dontAllow: NOT_LICENSED");
                    showMsg("Unlicensed Software, please download C Locker from Google Play Store");
                    break;
            }
            close();
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    @Override
    public void applicationError(int errorCode){
        try{
            dontAllow(Policy.RETRY);
            writeToFile("MyCLocker>applicationError: "+errorCode);
            close();
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    //use getApplicationContext to prevent leak memory
    public final void clearLogs(){
        try{
            File filename = new File(C.EXT_STORAGE_DIR,C.FILE_MY_LOG);
            if(filename.getAbsoluteFile().exists()){
                filename.getAbsoluteFile().delete();
            }
            filename = new File(C.EXT_STORAGE_DIR,C.FILE_BACKUP_RECORD);
            if(filename.getAbsoluteFile().exists()){
                filename.getAbsoluteFile().delete();
            }
            filename = new File(getBackupDir()+C.FILE_BACKUP_RECORD);
            if(filename.getAbsoluteFile().exists()){
                filename.getAbsoluteFile().delete();
            }
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    private String getBackupDir(){
        if(strPkgName!=null && strPkgName.equals(C.PKG_NAME_PRO)){
            return C.EXT_BACKUP_DIR_PRO;
        }else{
            return C.EXT_BACKUP_DIR_FREE;
        }
    }

    public final void sentLogs(final String emailTitle,final String errorMsg,
            final boolean attachFiles){
        try{
            final String Su1 = "com.noshufou.android.su"; // ChainsDD
            final String Su2 = "eu.chainfire.supersu"; // Chainfire
            final String Su3 = "com.koushikdutta.superuser"; // Koush

            final long longInstalledDate = context.getPackageManager().getPackageInfo(context.getPackageName(),0).firstInstallTime;
            final long longLastUpDate = context.getPackageManager().getPackageInfo(context.getPackageName(),0).lastUpdateTime;
            final String installedDate = getDate(longInstalledDate);
            final String lastUpDate = getDate(longLastUpDate);

            final StringBuffer buf = new StringBuffer();
            buf.append("You can add additional message here:");
            buf.append("\n");
            buf.append("\n");
            buf.append("\n");
            buf.append("\nApp Package Name {"+context.getPackageName()+"}");
            buf.append("\nApp Version {"+context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName+"}");
            buf.append("\nVERSION.RELEASE {"+Build.VERSION.RELEASE+"}");
            buf.append("\nVERSION.INCREMENTAL {"+Build.VERSION.INCREMENTAL+"}");
            buf.append("\nVERSION.SDK_INT {"+Build.VERSION.SDK_INT+"}");
            buf.append("\nBRAND {"+Build.BRAND+"}");
            buf.append("\nDEVICE {"+Build.DEVICE+"}");
            buf.append("\nMANUFACTURER {"+Build.MANUFACTURER+"}");
            buf.append("\nMODEL {"+Build.MODEL+"}");
            buf.append("\nANDROID_ID {"+getAndroidId(context)+"}");
            buf.append("\nInstaller {"+context.getPackageManager().getInstallerPackageName(context.getPackageName())+"}");
            buf.append("\nDate {"+installedDate+"-"+lastUpDate+"}");
            buf.append("\nDiretory {"+context.getPackageManager().getApplicationInfo(context.getPackageName(),0).sourceDir+"}");
            buf.append("\nServiceAccess {"+C.isAccessibilityEnabled(context)+"}");
            buf.append("\nServiceNotification {"+C.isNotificationEnabled(context)+"}");
            buf.append("\nHomeLauncher {"+C.getHomeLauncher(context)+"}");
            buf.append("\nSu {"+isPackageInstalled(Su1)+"/"+isPackageInstalled(Su2)+"/"+isPackageInstalled(Su3)+"}");
            buf.append("\nErrorMsg {"+errorMsg+"}");

            final Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
            //i.setType("message/rfc822");
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_EMAIL,new String[]{"cheahchinseng@gmail.com"});
            i.putExtra(Intent.EXTRA_TEXT,buf.toString());
            i.putExtra(Intent.EXTRA_SUBJECT,emailTitle);

            File file;
            if(attachFiles){
                exportBackup(false);
                exportLogcat();

                final ArrayList<Uri> uris = new ArrayList<Uri>();
                file = new File(C.EXT_STORAGE_DIR+C.FILE_SYSTEM_LOG);
                if(file.exists()){
                    uris.add(Uri.fromFile(file));
                }
                file = new File(C.EXT_STORAGE_DIR+C.FILE_MY_LOG);
                if(file.exists()){
                    uris.add(Uri.fromFile(file));
                }
                file = new File(getBackupDir()+C.FILE_BACKUP_RECORD);
                if(file.exists()){
                    uris.add(Uri.fromFile(file));
                }
                file = new File(getBackupDir()+C.FILE_PREFS);
                if(file.exists()){
                    uris.add(Uri.fromFile(file));
                }
                i.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);
            }
            context.startActivity(Intent.createChooser(i,"Send Report to Dev.."));
        }catch(Exception e){
            saveErrorLog(null,e);
            showMsg("Email sending failed!");
        }
    }

    public final boolean exportBackup(boolean isMsgShow){
        boolean isSuccessful = false;
        try{
            File fileTo = new File(C.EXT_STORAGE_DIR);
            if(!fileTo.getParentFile().exists()){
                fileTo.getParentFile().mkdir();
            }

            fileTo = new File(getBackupDir(),C.FILE_PREFS);
            if(!fileTo.getParentFile().exists()){
                fileTo.getParentFile().mkdir();
            }
            fileTo.delete();
            fileTo.createNewFile();

            FileWriter writer = new FileWriter(fileTo);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8",null);
            serializer.text("\n");

            timeLogs = DATE_FORMAT.format(new Date());
            savePrefs(serializer,"Backup","Date",timeLogs);

            // export shared preference
            final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            final Map<String,?> items = prefs.getAll();
            for(String key : items.keySet()){//keys.entrySet()
                if(key!=null){
                    if(!key.equals(C.C_LOCKER_OK) &&
                            !key.equals(C.PROMOTION_APP) &&
                            !key.equals(P.BLN_ENABLE_SERVICE) &&
                            //!key.equals("cBoxEnablePassUnlock")&&
                            !key.equals("intPass2nd01") &&
                            !key.equals("intPass2nd02") &&
                            !key.equals("intPass2nd03") &&
                            !key.equals("intPass2nd04") &&
                            !key.equals("intPass2nd05") &&
                            !key.equals("intPass2nd06") &&
                            !key.equals("intPass2nd07") &&
                            !key.equals("intPass2nd08") &&
                            !key.equals(C.PATTERN_LOCK)){
                        if(items.get(key)!=null){
                            final Object obj = items.get(key);
                            final String cls = obj.getClass().getSimpleName();
                            final String val = obj.toString();
                            if(val!=null){
                                savePrefs(serializer,cls,key,val);
                            }
                        }
                    }
                }
            }
            // export widget data
            final DataWidgets widgetDb = new DataWidgets(context);
            final ArrayList<InfoWidget> widgetList = (ArrayList<InfoWidget>)widgetDb.getAllWidgets();
            if(widgetList!=null && widgetList.size()>0){
                for(InfoWidget key : widgetList){//keys.entrySet()
                    if(key.getWidgetProfile()>0){
                        savePrefs(serializer,WIDGET,"getId()",key.getId()+"");
                        savePrefs(serializer,WIDGET,"getWidgetProfile()",key.getWidgetProfile()+"");
                        savePrefs(serializer,WIDGET,"getWidgetId()",key.getWidgetId()+"");
                        savePrefs(serializer,WIDGET,"getWidgetType()",key.getWidgetType()+"");
                        savePrefs(serializer,WIDGET,"getWidgetPkgsName()",key.getWidgetPkgsName()+"");
                        savePrefs(serializer,WIDGET,"getWidgetClassName()",key.getWidgetClassName()+"");
                        savePrefs(serializer,WIDGET,"getWidgetIndex()",key.getWidgetIndex()+"");
                        savePrefs(serializer,WIDGET,"getWidgetX()",key.getWidgetX()+"");
                        savePrefs(serializer,WIDGET,"getWidgetY()",key.getWidgetY()+"");
                        savePrefs(serializer,WIDGET,"getWidgetSpanX()",key.getWidgetSpanX()+"");
                        savePrefs(serializer,WIDGET,"getWidgetSpanY()",key.getWidgetSpanY()+"");
                        savePrefs(serializer,WIDGET,"getWidgetMinWidth()",key.getWidgetMinWidth()+"");
                        savePrefs(serializer,WIDGET,"getWidgetMinHeight()",key.getWidgetMinHeight()+"");
                    }
                }
            }
            if(widgetDb!=null){
                widgetDb.close();
            }
            // app selections
            final DataAppsSelection appDb = new DataAppsSelection(context);
            final ArrayList<InfoAppsSelection> appList = (ArrayList<InfoAppsSelection>)appDb.getAllApps();
            if(appList!=null && appList.size()>0){
                for(InfoAppsSelection key : appList){//keys.entrySet()
                    if(key.getAppType()>0){
                        savePrefs(serializer,APP_SELECTION,"getId()",key.getId()+"");
                        savePrefs(serializer,APP_SELECTION,"getAppType()",key.getAppType()+"");
                        savePrefs(serializer,APP_SELECTION,"getShortcutProfile()",key.getShortcutProfile()+"");
                        savePrefs(serializer,APP_SELECTION,"getShortcutId()",key.getShortcutId()+"");
                        savePrefs(serializer,APP_SELECTION,"getShortcutAction()",key.getShortcutAction()+"");
                        savePrefs(serializer,APP_SELECTION,"getShortcutPIN()",key.getShortcutPIN()+"");
                        savePrefs(serializer,APP_SELECTION,"getAppPkg()",key.getAppPkg()+"");
                        savePrefs(serializer,APP_SELECTION,"getAppClass()",key.getAppClass()+"");
                        savePrefs(serializer,APP_SELECTION,"getAppName()",key.getAppName()+"");
                        savePrefs(serializer,APP_SELECTION,"getAppSubInfo()",key.getAppSubInfo()+"");
                    }
                }
            }
            if(appDb!=null){
                appDb.close();
            }
            serializer.endDocument();
            serializer.flush();
            writer.close();

            // export images
            copyDirectory(C.EXT_STORAGE_DIR,getBackupDir());

            if(isMsgShow){
                showMsg("Backup settings completed");
            }
            writeToFile(C.FILE_BACKUP_RECORD,"MyBackupData>exportBackup>Done");
            isSuccessful = true;
        }catch(Exception e){
            showMsg("Backup error");
            saveErrorLog(null,e);
        }
        return isSuccessful;
    }

    public final void importBackup(String dir,boolean isMsgShow){
        try{
            File fileFrom = new File(dir,C.FILE_PREFS);
            if(fileFrom.getAbsoluteFile().exists()){
                final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                final XmlPullParser xpp = factory.newPullParser();

                xpp.setInput(new InputStreamReader(new FileInputStream(fileFrom)));
                int eventType = xpp.getEventType();
                String tag = "";
                String attr = "";

                final String BLN = "Boolean";
                final String VAL = "Integer";
                final String STR = "String";
                final String FLT = "Float";
                final String LON = "Long";

                editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
                final DataWidgets widgetDb = new DataWidgets(context);
                final DataAppsSelection appDb = new DataAppsSelection(context);

                widgetDb.deleteAllWidgets();//clear all before import
                appDb.deleteAllApps();//clear all before import

                while(eventType!=XmlPullParser.END_DOCUMENT){
                    if(eventType==XmlPullParser.START_TAG){
                        tag = xpp.getName();
                        attr = xpp.getAttributeValue(0);
                    }else if(eventType==XmlPullParser.TEXT){
                        // importing shared preference
                        if(tag.equals(BLN)){
                            if(!attr.equals(C.C_LOCKER_OK) &&
                                !attr.equals(C.PROMOTION_APP) &&
                                !attr.equals(P.BLN_ENABLE_SERVICE) &&
                                !attr.equals("cBoxEnablePassUnlock") &&// dont load these.
                                !attr.equals("cBoxSecuritySelfie") &&
                                !attr.equals("cBoxEmailSelfie") &&
                                !attr.equals("cBoxBlockRecentApps") &&
                                !attr.equals("cBoxBlockNoneShortcutApps") &&
                                !attr.equals(P.STR_SERVICE_ACCESS_PKG_NAME) &&
                                !attr.equals(P.STR_SERVICE_ACCESS_CLASS_NAME)){
                                editor.putBoolean(attr,Boolean.parseBoolean(xpp.getText()));
                            }
                        }else if(tag.equals(VAL)){
                            editor.putInt(attr,Integer.parseInt(xpp.getText()));
                        }else if(tag.equals(STR)){
                            if(!attr.equals(P.STR_SERVICE_ACCESS_PKG_NAME) &&
                                !attr.equals(P.STR_SERVICE_ACCESS_CLASS_NAME)){
                                editor.putString(attr,xpp.getText());
                            }
                        }else if(tag.equals(FLT)){
                            editor.putFloat(attr,Float.parseFloat(xpp.getText()));
                        }else if(tag.equals(LON)){
                            editor.putLong(attr,Long.parseLong(xpp.getText()));
                        }else if(tag.equals(WIDGET)){
                            // importing widget data
                            importWidgetData(attr,xpp,widgetDb);
                        }else if(tag.equals(APP_SELECTION)){
                            // importing app data
                            importAppsData(attr,xpp,appDb);
                        }
                    }
                    eventType = xpp.next();
                }
                editor.commit();
                if(widgetDb!=null){
                    widgetDb.close();
                }
                if(appDb!=null){
                    appDb.close();
                }

                // import images
                copyDirectory(dir,C.EXT_STORAGE_DIR);

                if(isMsgShow){
                    showMsg("Restore settings completed");
                }
                writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>importBackup>Done");

                final Intent i = new Intent(context.getPackageName()+C.RESTORE_COMPLETE);
                context.sendBroadcast(i);
            }else{
                showMsg("No back up settings found");
                writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>importBackup>No back up settings found");
            }
        }catch(Exception e){
            showMsg("Restore error");
            saveErrorLog(null,e);
        }
    }

    private void importWidgetData(final String attr,final XmlPullParser xpp,
            final DataWidgets widgetDb){
        if(attr.equals("getWidgetProfile()")){
            profile = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetId()")){
            widgetId = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetType()")){
            type = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetPkgsName()")){
            widgetPkgName = xpp.getText();
        }else if(attr.equals("getWidgetClassName()")){
            widgetClassName = xpp.getText();
        }else if(attr.equals("getWidgetIndex()")){
            index = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetX()")){
            x = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetY()")){
            y = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetSpanX()")){
            spanX = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetSpanY()")){
            spanY = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetMinWidth()")){
            width = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getWidgetMinHeight()")){
            height = Integer.parseInt(xpp.getText());
            if(profile>0){
                InfoWidget widgets = new InfoWidget(-1,//primary id not using
                        profile,widgetId,type,widgetPkgName,widgetClassName,index,x,y,spanX,spanY,//x,y,span x,span y not using
                        width,height);
                widgetDb.addWidget(widgets);
            }
            profile = -1;
            widgetId = -1;
            type = -1;
            index = -1;
            x = -1;
            y = -1;
            spanX = -1;
            spanY = -1;
            width = -1;
            height = -1;
            widgetPkgName = "";
            widgetClassName = "";
        }
    }

    private void importAppsData(final String attr,final XmlPullParser xpp,
            final DataAppsSelection appDb){
        if(attr.equals("getAppType()")){
            appType = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getShortcutProfile()")){
            shortcutProfile = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getShortcutId()")){
            shortcutId = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getShortcutAction()")){
            shortcutAction = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getShortcutPIN()")){
            //shortcutPIN = Integer.parseInt(xpp.getText());
        }else if(attr.equals("getAppPkg()")){
            appPkgName = xpp.getText();
        }else if(attr.equals("getAppClass()")){
            appClassName = xpp.getText();
        }else if(attr.equals("getAppName()")){
            appName = xpp.getText();
        }else if(attr.equals("getAppSubInfo()")){
            appSubInfo = xpp.getText();
            if(appType>0){
                InfoAppsSelection apps = new InfoAppsSelection();
                apps.setAppType(appType);//primary id not using
                apps.setShortcutProfile(shortcutProfile);
                apps.setShortcutId(shortcutId);
                apps.setShortcutAction(shortcutAction);
                //apps.setShortcutPIN(shortcutPIN); dont restore PIN
                apps.setAppPkg(appPkgName);
                apps.setAppClass(appClassName);
                apps.setAppName(appName);
                apps.setAppSubInfo(appSubInfo);
                appDb.addApp(apps);
            }
            appType = -1;
            shortcutId = -1;
            shortcutAction = DataAppsSelection.ACTION_DEFAULT;
            //shortcutPIN		= DataAppsSelection.PIN_NOT_REQUIRED;
            appPkgName = "0";
            appClassName = "0";
            appName = "0";
            appSubInfo = "0";
        }
    }

    public final void writeToFile(String data){
        if(!IS_DEBUG_MODE){
            return;
        }
        writeToFile(C.FILE_BACKUP_RECORD,data);
    }

    public final void writeToFile(String fileName,String data){
        try{
            Log.e(C.LOG,data);

            File fileTo = new File(C.EXT_STORAGE_DIR);
            if(!fileTo.exists()){
                fileTo.mkdir();
            }
            fileTo = new File(getBackupDir(),fileName);
            if(!fileTo.getParentFile().exists()){
                fileTo.getParentFile().mkdir();
                if(!fileTo.exists()){
                    fileTo.createNewFile();
                }
            }

            timeLogs = DATE_FORMAT.format(new Date());
            final BufferedWriter buf = new BufferedWriter(new FileWriter(fileTo,true));
            buf.append(timeLogs+"/"+data+getPkgName());
            buf.newLine();
            buf.close();
        }catch(FileNotFoundException e){
            saveErrorLog(null,e);
        }catch(IOException e){
            saveErrorLog(null,e);
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    private void exportLogcat(){
        try{
            final File filename = new File(C.EXT_STORAGE_DIR+C.FILE_SYSTEM_LOG);
            if(!filename.getParentFile().exists()){
                filename.getParentFile().mkdir();
            }else{
                filename.getAbsoluteFile().delete();
            }
            filename.createNewFile();
            String[] cmd = new String[]{"logcat","-f",filename.getAbsolutePath(),"-v","time","-s",C.LOG+":W","System.err:W"};//"System.err:W"
            Runtime.getRuntime().exec(cmd);
        }catch(IOException e){
            saveErrorLog(null,e);
        }
    }

    private void savePrefs(final XmlSerializer serializer,final String tag,final String key,
            final String val){
        try{
            serializer.startTag("",tag);
            serializer.attribute("","name",key);
            serializer.text(val);
            serializer.endTag("",tag);
            serializer.text("\n");
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    private String getAndroidId(Context context){
        final String str;
        try{
            str = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
            if(str!=null){
                return str;
            }
        }catch(Exception e){
            saveErrorLog(null,e);
        }
        return "N/A";
    }

    private String getDate(final long date){
        return "("+getDate(date,Calendar.DAY_OF_MONTH)+"/"+
                getDate(date,Calendar.MONTH)+"/"+//january = 0, so must plus 1
                getDate(date,Calendar.YEAR)+")";
    }

    private void copyDirectory(String fromDir,String toDir){
        try{
            final File fileFrom = new File(fromDir);
            final File fileTo = new File(toDir);
            if(fileFrom.isDirectory()){
                final String[] children = fileFrom.list();
                for(int i = 0; i<children.length; i++){
                    if(!C.FILE_PREFS.contains(children[i]) &&            // dont copy prefs.xml
                            !C.FILE_BACKUP_RECORD.contains(children[i]) &&    // dont copy BackupRecord.txt
                            !C.FILE_MY_LOG.contains(children[i]) &&        // dont copy CLockerlog.txt
                            !C.FILE_SYSTEM_LOG.contains(children[i])){        // dont copy Systemlog.txt
                        //Log.e("info",children[i]);
                        copyFiles(new File(fileFrom,children[i]),new File(fileTo,children[i]));
                    }
                }
            }else{
                writeToFile(C.FILE_BACKUP_RECORD,"MyBackupData>copyFile: No file found");
            }

        }catch(FileNotFoundException e){
            saveErrorLog(null,e);
        }catch(IOException e){
            saveErrorLog(null,e);
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    private void copyFiles(final File fFrom,final File fTo) throws IOException{
        try{
            if(!fFrom.isDirectory()){
                if(!fTo.getParentFile().exists()){
                    fTo.getParentFile().mkdir();
                }
                inStream = new FileInputStream(fFrom);
                outStream = new FileOutputStream(fTo);

                byte[] buf = new byte[1024];
                int len;
                while((len = inStream.read(buf))>0){
                    outStream.write(buf,0,len);
                }
                inStream.close();
                outStream.close();
            }
        }catch(FileNotFoundException e){
            saveErrorLog(null,e);
        }catch(IOException e){
            saveErrorLog(null,e);
        }catch(Exception e){
            saveErrorLog(null,e);
        }
    }

    //run background
    private class GetInternetTime extends AsyncTask<Void,Void,Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            try{
                //if all below not working, goto: "http://tf.nist.gov/tf-cgi/servers.cgi"
                final SntpClient client = new SntpClient();
                final int TIME_OUT = 2000;
                if(client.requestTime("nist.time.nosc.us",TIME_OUT)){
                    //actual time now + (system time now - the requested time)
                    final long internetTime = client.getNtpTime()+SystemClock.elapsedRealtime()-client.getNtpTimeReference();
                    final Date d = new Date();
                    d.setTime(internetTime);
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>GetInternetTime>doInBackground>requestTime1: "+d.toString());

                    if(!isPromotionTime(internetTime)){
                        return C.PROMOTION_TIME_NG;
                    }
                    return C.PROMOTION_TIME_OK;
                }else if(client.requestTime("nist1-nj2.ustiming.org",TIME_OUT)){
                    //actual time now + (system time now - the requested time)
                    final long internetTime = client.getNtpTime()+SystemClock.elapsedRealtime()-client.getNtpTimeReference();
                    final Date d = new Date();
                    d.setTime(internetTime);
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>GetInternetTime>doInBackground>requestTime2: "+d.toString());

                    if(!isPromotionTime(internetTime)){
                        return C.PROMOTION_TIME_NG;
                    }
                    return C.PROMOTION_TIME_OK;
                }else if(client.requestTime("nist1-pa.ustiming.org",TIME_OUT)){
                    //actual time now + (system time now - the requested time)
                    final long internetTime = client.getNtpTime()+SystemClock.elapsedRealtime()-client.getNtpTimeReference();
                    final Date d = new Date();
                    d.setTime(internetTime);
                    writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>GetInternetTime>doInBackground>requestTime3: "+d.toString());

                    if(!isPromotionTime(internetTime)){
                        return C.PROMOTION_TIME_NG;
                    }
                    return C.PROMOTION_TIME_OK;
                }
            }catch(Exception e){
                saveErrorLog(null,e);
            }
            return C.PROMOTION_TIME_ERROR;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Integer result){
            try{
                editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
                switch(result){
                    case C.PROMOTION_TIME_OK:
                        writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>GetInternetTime>doInBackground>error: PROMOTION_TIME_OK");
                        break;
                    case C.PROMOTION_TIME_NG:
                        writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>GetInternetTime>doInBackground>error: PROMOTION_TIME_NG");
                        break;
                    case C.PROMOTION_TIME_ERROR:
                        writeToFile(C.FILE_BACKUP_RECORD,"MyCLocker>GetInternetTime>doInBackground>error: PROMOTION_TIME_ERROR");
                        break;
                }
                final Intent i = new Intent(context.getPackageName()+C.PROMOTION_CHECK);
                i.putExtra("promotionResult",result);
                context.sendBroadcast(i);
            }catch(Exception e){
                saveErrorLog(null,e);
            }
        }
    }
}