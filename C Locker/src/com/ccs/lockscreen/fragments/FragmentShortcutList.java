package com.ccs.lockscreen.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.T;
import com.ccs.lockscreen.utils.CropOption;
import com.ccs.lockscreen.utils.CropOptionAdapter;
import com.ccs.lockscreen.utils.DeviceAdminHandler;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;
import com.ccs.lockscreen_pro.ListInstalledApps;
import com.ccs.lockscreen_pro.SettingsPasswordUnlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FragmentShortcutList extends Fragment{
    private static final String PAGE_NO = "pageNo";
    private static final int SELECTED_ICON_1 = 1, SELECTED_ICON_2 = 2, CROP_PICTURE = 3, SELECTED_SHORTCUT = 4, PRO_PIN_UNLOCK = 5, PRO_PIN_ALL = 6, PICK_CONTACT_SMS = 8, PICK_CONTACT_CALL = 9, DEVICE_ADMIN_SET = 10, FAVORITE_SHORTCUT_1 = 11, FAVORITE_SHORTCUT_2 = 12, IMG_ID = 1000, TXT_ID = 2000, CBOX_ID = 3000;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;
    private View mainView;
    //private ProgressBar progressBarView;
    private LinearLayout lytShortcutSub;
    private AlertDialog.Builder builder;
    private AlertDialog alert;
    private DataAppsSelection shortcutData;
    private int profile, temporaryShortcutId;
    private ProgressDialog progressBar;
    private Handler handler = new Handler();
    private MyCLocker mLocker;
    private MyAlertDialog myAlertDialog;
    private DeviceAdminHandler deviceAdminHandler;
    //
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(context.getPackageName()+C.RESET_ALL_PROFILE)){
                    resetAllShortcut(true);
                }else if(action.equals(context.getPackageName()+C.SET_UNLOCK_PIN)){
                    setUnlockPin();
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };

    public static FragmentShortcutList newInstance(int noPage){
        FragmentShortcutList f = new FragmentShortcutList();
        Bundle b = new Bundle();
        b.putInt(PAGE_NO,noPage);
        f.setArguments(b);
        return f;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case SELECTED_ICON_1:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            Bundle extras = data.getExtras();
                            if(extras!=null){
                                Bitmap bm = extras.getParcelable("icon");
                                setShortcutIcon(temporaryShortcutId,bm);
                                return;
                            }
                        }catch(Exception e){
                            mLocker.saveErrorLog(null,e);
                        }
                    }
                    Toast.makeText(context,"Selection errors",Toast.LENGTH_LONG).show();
                    break;
                case SELECTED_ICON_2:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            cropImage(data,144,144);
                            return;
                        }catch(Exception e){
                            mLocker.saveErrorLog(null,e);
                        }
                    }
                    Toast.makeText(context,"Selection errors",Toast.LENGTH_LONG).show();
                    break;
                case CROP_PICTURE:
                    if(resultCode==Activity.RESULT_OK){
                        Bundle extras = data.getExtras();
                        if(extras!=null){
                            Bitmap bm = extras.getParcelable("data");
                            setShortcutIcon(temporaryShortcutId,bm);
                            return;
                        }
                    }
                    Toast.makeText(context,"Selection errors",Toast.LENGTH_LONG).show();
                    break;
                case SELECTED_SHORTCUT:
                    if(resultCode==Activity.RESULT_OK){
                        int id = data.getExtras().getInt(C.LIST_APPS_ID);
                        String str = data.getExtras().getString("appName");
                        Bitmap bm = (Bitmap)data.getExtras().get("appNameIcon");

                        setText(id,0,str);
                        setShortcutIcon(id,bm);
                    }
                    break;
                case PRO_PIN_UNLOCK:
                    if(resultCode==Activity.RESULT_OK){
                        final Bundle extras = data.getExtras();
                        int securityType = extras.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_PIN);

                        editor.putBoolean("cBoxEnablePassUnlock",true);
                        editor.putInt(C.SECURITY_TYPE,securityType);
                        editor.commit();
                        setCBox(temporaryShortcutId,true);
                        shortcutData.updateShortcutInfo(temporaryShortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);

                        Intent i = new Intent(context.getPackageName()+C.SET_UNLOCK_PIN);
                        context.sendBroadcast(i);
                    }else{
                        setCBox(temporaryShortcutId,false);
                        shortcutData.updateShortcutInfo(temporaryShortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                    }
                    break;
                case PRO_PIN_ALL:
                    if(resultCode==Activity.RESULT_OK){
                        final Bundle extras = data.getExtras();
                        int securityType = extras.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_PIN);

                        editor.putBoolean("cBoxEnablePassUnlock",true);
                        editor.putInt(C.SECURITY_TYPE,securityType);
                        editor.commit();
                        setAllPin(true,true);
                    }else{
                        clearSecurity();
                        ;
                        setAllPin(true,false);
                    }
                    break;
                case PICK_CONTACT_SMS:
                    if(resultCode==Activity.RESULT_OK){
                        if(data!=null){
                            Uri contactData = data.getData();
                            setContactNo(contactData,true);
                        }
                    }
                    break;
                case PICK_CONTACT_CALL:
                    if(resultCode==Activity.RESULT_OK){
                        if(data!=null){
                            Uri contactData = data.getData();
                            setContactNo(contactData,false);
                        }
                    }
                    break;
                case DEVICE_ADMIN_SET:
                    if(resultCode==Activity.RESULT_OK){
                        setShortcutAction(temporaryShortcutId,DataAppsSelection.ACTION_OFF_SCREEN);
                        setText(temporaryShortcutId,R.string.turn_off_screen,"");
                        setShortcutIcon(temporaryShortcutId,null);
                    }
                    break;
                case FAVORITE_SHORTCUT_1:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            //Intent.URI_INTENT_SCHEME = no use
                            startActivityForResult(Intent.parseUri(data.toUri(0),0),FAVORITE_SHORTCUT_2);
                            break;
                        }catch(Exception e){
                            mLocker.saveErrorLog(null,e);
                        }
                    }
                    Toast.makeText(context,"Shortcut Setting error",Toast.LENGTH_LONG).show();
                    break;
                case FAVORITE_SHORTCUT_2:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            if(data.hasExtra(Intent.EXTRA_SHORTCUT_INTENT) && data.hasExtra(Intent.EXTRA_SHORTCUT_NAME)){

                                Intent i = (Intent)data.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT);
                                String name = data.getExtras().getString(Intent.EXTRA_SHORTCUT_NAME);

                                setText(temporaryShortcutId,0,name);
                                shortcutData.updateFavoriteShortcut(temporaryShortcutId,i.toUri(0).toString(),name);
                                if(data.hasExtra(Intent.EXTRA_SHORTCUT_ICON)){
                                    Bitmap bm = (Bitmap)data.getExtras().get(Intent.EXTRA_SHORTCUT_ICON);
                                    setShortcutIcon(temporaryShortcutId,getResizedBitmap(bm,124,124));
                                }else if(data.hasExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)){
                                    PackageManager packageManager = context.getPackageManager();
                                    ShortcutIconResource iconResource = (ShortcutIconResource)data.getExtras().get(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                                    Resources res = packageManager.getResourcesForApplication(iconResource.packageName);
                                    Drawable icon = res.getDrawable(res.getIdentifier(iconResource.resourceName,null,null));
                                    Bitmap bm = ((BitmapDrawable)icon).getBitmap();
                                    setShortcutIcon(temporaryShortcutId,getResizedBitmap(bm,124,124));
                                }
                            }
                            break;
                        }catch(Exception e){
                            mLocker.saveErrorLog(null,e);
                        }
                    }
                    Toast.makeText(context,"Shortcut Setting error",Toast.LENGTH_LONG).show();
                    break;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        mainView = inflater.inflate(R.layout.settings_shortcut_list,container,false);
        //((LinearLayout)mainView).addView(progressBar());
        setHasOptionsMenu(true);
        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        final Bundle b = getArguments();
        if(b!=null && b.containsKey(PAGE_NO)){
            profile = b.getInt(PAGE_NO);
        }
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        editor = prefs.edit();

        mLocker = new MyCLocker(context);
        myAlertDialog = new MyAlertDialog(context);
        shortcutData = new DataAppsSelection(context);
        shortcutData.setProfile(profile);

        deviceAdminHandler = new DeviceAdminHandler(context);

        loadReceiver(true);
        try{
            handler.post(new Runnable(){
                @Override
                public void run(){
                    final LinearLayout lyt = (LinearLayout)mainView.findViewById(R.id.lytShortcutScroll);
                    //lyt.setVisibility(View.GONE);
                    //lock title
                    lyt.addView(lytTitle(DataAppsSelection.SHORTCUT_TYPE_LOCK_ICON));
                    lyt.addView(line(C.dpToPx(context,2),"#33B5E5"));
                    //lock shortcut
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_01));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_02));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_03));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_04));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_05));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_06));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_07));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.SHORTCUT_APP_08));//top
                    //volume key title
                    lyt.addView(lytTitle(DataAppsSelection.SHORTCUT_TYPE_VOLUME_KEY));
                    lyt.addView(line(C.dpToPx(context,2),"#33B5E5"));
                    //volume key shortcut
                    lyt.addView(lytShortcutMain(DataAppsSelection.VOLUME_UP));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.VOLUME_DOWN));//top
                    //gesture title
                    lyt.addView(lytTitle(DataAppsSelection.SHORTCUT_TYPE_GESTURE_SINGLE));
                    lyt.addView(line(C.dpToPx(context,2),"#33B5E5"));
                    //gesture shortcut
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_UP));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_DOWN));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_LEFT));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_RIGHT));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_UP_2));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_DOWN_2));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_LEFT_2));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.GESTURE_RIGHT_2));//top
                    //tap title
                    lyt.addView(lytTitle(DataAppsSelection.SHORTCUT_TYPE_TAPPING));
                    lyt.addView(line(C.dpToPx(context,2),"#33B5E5"));
                    //tap shortcut
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.DOUBLE_TAP1));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.DOUBLE_TAP2));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.TRIPLE_TAP1));//top
                    lyt.addView(line(C.dpToPx(context,1),"#555555"));
                    lyt.addView(lytShortcutMain(DataAppsSelection.TRIPLE_TAP2));//top
                }
            });
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public void onPause(){
        try{
            saveSettings();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        super.onPause();
    }

    @Override
    public void onDestroy(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
            if(alert!=null && alert.isShowing()){
                alert.dismiss();
            }
            if(shortcutData!=null){
                shortcutData.close();
            }
            if(myAlertDialog!=null){
                myAlertDialog.close();
            }
            loadReceiver(false);
        }catch(Exception e){
            //mLocker.saveErrorLog(null,e);// no need this to prevent FC
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        final boolean cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);
        Intent i;
        switch(item.getItemId()){
            case R.id.itemSelectAllPin:
                setAllPin(cBoxLockerOk,true);
                return true;
            case R.id.itemDeselectAllPin:
                setAllPin(cBoxLockerOk,false);
                return true;
            case R.id.itemCopyFromLockerProfile:
                if(profile!=C.PROFILE_DEFAULT){
                    copyShortcuts(C.PROFILE_DEFAULT);
                }
                return true;
            case R.id.itemCopyFromMusicProfile:
                if(profile!=C.PROFILE_MUSIC){
                    copyShortcuts(C.PROFILE_MUSIC);
                }
                return true;
            case R.id.itemCopyFromLocationProfile:
                if(profile!=C.PROFILE_LOCATION){
                    copyShortcuts(C.PROFILE_LOCATION);
                }
                return true;
            case R.id.itemCopyFromTimeProfile:
                if(profile!=C.PROFILE_TIME){
                    copyShortcuts(C.PROFILE_TIME);
                }
                return true;
            case R.id.itemCopyFromWifiProfile:
                if(profile!=C.PROFILE_WIFI){
                    copyShortcuts(C.PROFILE_WIFI);
                }
                return true;
            case R.id.itemCopyFromBluetoothProfile:
                if(profile!=C.PROFILE_BLUETOOTH){
                    copyShortcuts(C.PROFILE_BLUETOOTH);
                }
                return true;
            case R.id.itemResetShortcuts:
                resetAllShortcut(false);
                return true;
            case R.id.itemResetAllProfileShortcuts:
                i = new Intent(context.getPackageName()+C.RESET_ALL_PROFILE);
                context.sendBroadcast(i);
                return true;
        }
        return false;
    }

    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(context.getPackageName()+C.RESET_ALL_PROFILE);
                intentFilter.addAction(context.getPackageName()+C.SET_UNLOCK_PIN);
                context.registerReceiver(mReceiver,intentFilter);
            }else{
                context.unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void setText(final int shortcutId,final int strId,final String contact){
        handler.post(new Runnable(){
            @Override
            public void run(){
                try{
                    String str = "";
                    if(strId!=0){
                        str = getString(strId);
                    }
                    final TextView txt = (TextView)mainView.findViewById(shortcutId+TXT_ID);
                    txt.setText(str+contact);
                    saveShortcutText(shortcutId);
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }
        });
    }

    private void setShortcutIcon(final int shortcutId,final int imgId){
        handler.post(new Runnable(){
            @Override
            public void run(){
                try{
                    final ImageView img = (ImageView)mainView.findViewById(shortcutId+IMG_ID);
                    if(imgId==0){
                        img.setImageDrawable(null);
                    }else{
                        img.setImageResource(imgId);
                    }
                    saveImage(shortcutId);
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }
        });
    }

    private void setShortcutIcon(final int shortcutId,final Bitmap bm){
        //bm = getResizedBitmap(bm,124,124);//default 144
        final ImageView img = (ImageView)mainView.findViewById(shortcutId+IMG_ID);
        img.setImageBitmap(bm);
        saveImage(shortcutId);
    }

    private void setShortcutAction(final int shortcutId,int action){
        shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_ACTION,action);
    }

    private void setUnlockPin(){
        startProgressbar();
        new Thread(){
            public void run(){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    mLocker.saveErrorLog(null,e);
                }
                final List<InfoAppsSelection> appsList = shortcutData.getApps(DataAppsSelection.APP_TYPE_SHORTCUT);
                for(InfoAppsSelection item : appsList){
                    if(item.getShortcutProfile()==profile){
                        if(item.getShortcutId()==DataAppsSelection.SHORTCUT_APP_01 && item.getShortcutAction()==DataAppsSelection.ACTION_DEFAULT){
                            setCBox(item.getShortcutId(),true);
                            shortcutData.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }else if(item.getShortcutAction()==DataAppsSelection.ACTION_UNLOCK){
                            setCBox(item.getShortcutId(),true);
                            shortcutData.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }
                    }
                }
                if(progressBar!=null && progressBar.isShowing()){
                    progressBar.dismiss();
                }
            }
        }.start();
    }

    private void startProgressbar(){
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(false);
        progressBar.setMessage(getString(R.string.configuring));
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();
    }

    private void setCBox(final int shortcutId,final boolean check){
        handler.post(new Runnable(){
            @Override
            public void run(){
                try{
                    final CheckBox cBox = (CheckBox)mainView.findViewById(shortcutId+CBOX_ID);
                    cBox.setChecked(check);
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }
        });
    }

    private void setContactNo(final Uri contactData,final boolean isSMS){
        try{
            final String id = contactData.getLastPathSegment();
            final Cursor phoneCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.
                    Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" = ?",new String[]{id},null);
            final ArrayList<String> phonesList = new ArrayList<String>();
            while(phoneCur.moveToNext()){
                // context would allow you get several phone addresses
                // if the phone addresses were stored in an array
                final String phone = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
                phonesList.add(phone);
            }
            phoneCur.close();
            if(phonesList.size()==0){
                Toast.makeText(context,getString(R.string.gesture_no_contact),Toast.LENGTH_LONG).show();
                setText(temporaryShortcutId,R.string.no_action,"");
            }else if(phonesList.size()==1){
                final String contactName = getContactDisplayNameByNumber(phonesList.get(0));
                shortcutData.updateShortcutInfo(temporaryShortcutId,DataAppsSelection.KEY_OTHER_INFO,phonesList.get(0));
                if(isSMS){
                    setShortcutAction(temporaryShortcutId,DataAppsSelection.ACTION_DIRECT_SMS);
                    setText(temporaryShortcutId,R.string.gesture_action_sms," ("+contactName+")");
                    setShortcutIcon(temporaryShortcutId,null);
                }else{
                    setShortcutAction(temporaryShortcutId,DataAppsSelection.ACTION_DIRECT_CALL);
                    setText(temporaryShortcutId,R.string.gesture_action_call," ("+contactName+")");
                    setShortcutIcon(temporaryShortcutId,null);
                    new MyAlertDialog(context).selectDirectCall(null);
                }
            }else{
                //if more than one contact
                final String[] phonesArr = new String[phonesList.size()];
                for(int i = 0; i<phonesList.size(); i++){
                    phonesArr[i] = phonesList.get(i);
                }
                final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Choose Phone No");
                dialog.setItems(phonesArr,new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog,int item){
                        final String contactName = getContactDisplayNameByNumber(phonesArr[item]);
                        shortcutData.updateShortcutInfo(temporaryShortcutId,DataAppsSelection.KEY_OTHER_INFO,phonesArr[item]);
                        if(isSMS){
                            setShortcutAction(temporaryShortcutId,DataAppsSelection.ACTION_DIRECT_SMS);
                            setText(temporaryShortcutId,R.string.gesture_action_sms," ("+contactName+")");
                            setShortcutIcon(temporaryShortcutId,null);
                        }else{
                            setShortcutAction(temporaryShortcutId,DataAppsSelection.ACTION_DIRECT_CALL);
                            setText(temporaryShortcutId,R.string.gesture_action_call," ("+contactName+")");
                            setShortcutIcon(temporaryShortcutId,null);
                            new MyAlertDialog(context).selectDirectCall(null);
                        }
                    }
                }).create();
                dialog.show();
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private String getContactDisplayNameByNumber(String number){
        String name = number;
        try{
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
            String[] str = new String[]{BaseColumns._ID,ContactsContract.PhoneLookup.DISPLAY_NAME};

            Cursor cursor = context.getContentResolver().query(uri,str,null,null,null);
            try{
                if(cursor!=null && cursor.getCount()>0){
                    cursor.moveToNext();
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            if(name==null){
                return getString(R.string.unknown_number);
            }else{
                return name;
            }
        }catch(Exception e){
            if(name==null){
                return getString(R.string.unknown_number);
            }else{
                return name;
            }
        }
    }

    private void saveSettings(){
        try{
            handler.post(new Runnable(){
                @Override
                public void run(){
                    if(!shortcutData.isUnlockSet(profile)){
                        setShortcutAction(DataAppsSelection.SHORTCUT_APP_01,DataAppsSelection.ACTION_UNLOCK);
                        setText(DataAppsSelection.SHORTCUT_APP_01,R.string.unlock,"");
                        setShortcutIcon(DataAppsSelection.SHORTCUT_APP_01,R.drawable.ic_lock_open_white_48dp);

                        if(isPinActivated()){
                            setCBox(DataAppsSelection.SHORTCUT_APP_01,true);
                            shortcutData.updateShortcutInfo(DataAppsSelection.SHORTCUT_APP_01,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }
                        Toast.makeText(context,"No Unlock Action found in "+getProfile()+
                                ", Top icon reset to Unlock Action",Toast.LENGTH_LONG).show();
                    }
                    final List<InfoAppsSelection> appsList = shortcutData.getApps(DataAppsSelection.APP_TYPE_SHORTCUT,profile);
                    for(InfoAppsSelection item : appsList){
                        saveShortcutText(item.getShortcutId());
                    }
                }
            });
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void saveImage(int shortcutId){
        final ImageView img = (ImageView)mainView.findViewById(shortcutId+IMG_ID);
        try{
            if(img!=null && img.getDrawable()!=null){
                final Bitmap bmIcon = ((BitmapDrawable)img.getDrawable()).getBitmap();
                final File fileIcon = new File(C.EXT_STORAGE_DIR,getIcon(shortcutId,profile));
                if(bmIcon!=null){
                    if(!fileIcon.getParentFile().exists()){
                        fileIcon.getParentFile().mkdir();
                    }
                    final OutputStream outStream = new FileOutputStream(fileIcon);
                    bmIcon.compress(Bitmap.CompressFormat.PNG,100,outStream);
                    outStream.flush();
                    outStream.close();
                }
            }
        }catch(FileNotFoundException e){
            mLocker.saveErrorLog(null,e);
        }catch(IOException e){
            mLocker.saveErrorLog(null,e);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm,int newHeight,int newWidth){
        final int width = bm.getWidth();
        final int height = bm.getHeight();
        final float scaleWidth = ((float)newWidth)/width;
        final float scaleHeight = ((float)newHeight)/height;
        final Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        return Bitmap.createBitmap(bm,0,0,width,height,matrix,false);
    }

    private void saveShortcutText(int shortcutId){
        try{
            final TextView txt = (TextView)mainView.findViewById(shortcutId+TXT_ID);
            editor.putString(getTextPref(shortcutId,profile),txt.getText().toString());
            editor.commit();
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void setAllPin(final boolean cBoxLockerOk,final boolean select){
        handler.post(new Runnable(){
            @Override
            public void run(){
                final List<InfoAppsSelection> appsList = shortcutData.getApps(DataAppsSelection.APP_TYPE_SHORTCUT,profile);
                if(select){
                    if(cBoxLockerOk){
                        if(isPinActivated()){
                            for(InfoAppsSelection item : appsList){
                                setCBox(item.getShortcutId(),true);
                                shortcutData.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                            }
                        }else{
                            new MyAlertDialog(context).selectSecurityUnlockType(new OnDialogListener(){
                                @Override
                                public void onDialogClickListener(int dialogType,int result){
                                    if(dialogType==MyAlertDialog.OK){
                                        int securityType;
                                        if(result==0){//pin
                                            securityType = C.SECURITY_UNLOCK_PIN;
                                        }else{//pattern
                                            securityType = C.SECURITY_UNLOCK_PATTERN;
                                        }
                                        final Intent i = new Intent(context,SettingsPasswordUnlock.class);
                                        i.putExtra(C.SECURITY_TYPE,securityType);
                                        startActivityForResult(i,PRO_PIN_ALL);
                                    }else{
                                        clearSecurity();
                                    }
                                }
                            });
                        }
                    }else{
                        myAlertDialog.upgradeLocker();
                    }
                }else{
                    for(InfoAppsSelection item : appsList){
                        setCBox(item.getShortcutId(),false);
                        shortcutData.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                    }
                    clearSecurity();
                }
            }
        });
    }

    private void copyShortcuts(final int newProfile){
        startProgressbar();
        new Thread(){
            public void run(){
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    mLocker.saveErrorLog(null,e);
                }
                try{
                    final List<InfoAppsSelection> appsList = shortcutData.getApps(DataAppsSelection.APP_TYPE_SHORTCUT,newProfile);
                    ImageView img;
                    int id;
                    for(InfoAppsSelection item : appsList){
                        item.setShortcutProfile(profile);
                        shortcutData.updateApp(item);// update to current profile
                        id = item.getShortcutId();
                        if(id>DataAppsSelection.SHORTCUT_APP_08){
                            if(item.getShortcutAction()==DataAppsSelection.ACTION_SHORTCUT_LAUNCH_APP || item.getShortcutAction()==DataAppsSelection.ACTION_UNLOCK){
                                img = (ImageView)mainView.findViewById(id+IMG_ID);
                                loadImage(img,getIcon(id,newProfile));
                            }else{
                                img = (ImageView)mainView.findViewById(id+IMG_ID);
                                loadImage(img,"");
                            }
                        }else{
                            img = (ImageView)mainView.findViewById(id+IMG_ID);
                            loadImage(img,getIcon(id,newProfile));
                        }
                        setText(id,0,loadShortcutText(id,newProfile));
                        if(item.getShortcutPIN()==DataAppsSelection.PIN_REQUIRED){
                            setCBox(id,true);
                            shortcutData.updateShortcutInfo(id,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }else{
                            setCBox(id,false);
                            shortcutData.updateShortcutInfo(id,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        }
                    }
                    for(InfoAppsSelection item : appsList){
                        saveImage(item.getShortcutId());
                    }
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
                if(progressBar!=null && progressBar.isShowing()){
                    progressBar.dismiss();
                }
            }
        }.start();
    }

    private void resetAllShortcut(final boolean resetAllProfile){
        startProgressbar();
        new Thread(){
            public void run(){// dont create any "new Handler()" inside, else it will crash!
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    mLocker.saveErrorLog(null,e);
                }
                try{
                    final List<InfoAppsSelection> appsList = shortcutData.getApps(DataAppsSelection.APP_TYPE_SHORTCUT,profile);
                    for(InfoAppsSelection item : appsList){
                        setShortcutIcon(item.getShortcutId(),getDefaultIcon(item.getShortcutId()));
                        setText(item.getShortcutId(),0,getDescSub(item.getShortcutId()));
                        setCBox(item.getShortcutId(),false);
                    }
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
                try{
                    if(resetAllProfile){
                        if(profile==C.PROFILE_DEFAULT){//only one profile need update, prevent data crashing
                            shortcutData.deleteAllApps(DataAppsSelection.APP_TYPE_SHORTCUT);
                            shortcutData.checkShortcutData();
                        }
                    }else{
                        shortcutData.deleteAllApps(DataAppsSelection.APP_TYPE_SHORTCUT,profile);
                        shortcutData.checkShortcutData();
                    }
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
                if(progressBar!=null && progressBar.isShowing()){
                    progressBar.dismiss();
                }
            }
        }.start();
    }

    private void dialogShortcutApp(final int shortcutId){
        final String[] items = new String[]{getString(R.string.launch_app),T.SETTINGS_SHORTCUTS,getString(R.string.set_as_unlock),getString(R.string.hide_icon),getString(R.string.settings_shortcut_icon_app),getString(R.string.settings_shortcut_icon_gallery)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.shortcut_app_title));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int selection){
                switch(selection){
                    case 0:
                        final Intent i = new Intent(context,ListInstalledApps.class);
                        i.putExtra(C.LIST_APPS_ID,shortcutId);
                        i.putExtra(C.LIST_APPS_PROFILE,profile);
                        startActivityForResult(i,SELECTED_SHORTCUT);
                        break;
                    case 1:
                        temporaryShortcutId = shortcutId;
                        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                        pickIntent.putExtra(Intent.EXTRA_INTENT,new Intent(Intent.ACTION_CREATE_SHORTCUT));
                        startActivityForResult(pickIntent,FAVORITE_SHORTCUT_1);
                        break;
                    case 2:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_UNLOCK);
                        setText(shortcutId,R.string.unlock,"");
                        setShortcutIcon(shortcutId,R.drawable.ic_lock_open_white_48dp);

                        if(isPinActivated()){
                            setCBox(shortcutId,true);
                            shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }
                        break;
                    case 3:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_SHORTCUT_HIDE);
                        setText(shortcutId,R.string.no_action,"");
                        setShortcutIcon(shortcutId,R.drawable.ic_mode_edit_white_48dp);
                        break;
                    case 4:
                        iconAction1();
                        break;
                    case 5:
                        iconAction2();
                        break;
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    private void dialogVolume(final int shortcutId){
        final String[] items = new String[]{getString(R.string.launch_app),T.SETTINGS_SHORTCUTS,getString(R.string.sound_mode),getString(R.string.shortcut_volume_key_default),getString(R.string.no_action)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.set_volume_rocker_selection));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int selection){
                switch(selection){
                    case 0:
                        final Intent i = new Intent(context,ListInstalledApps.class);
                        i.putExtra(C.LIST_APPS_ID,shortcutId);
                        i.putExtra(C.LIST_APPS_PROFILE,profile);
                        startActivityForResult(i,SELECTED_SHORTCUT);
                        break;
                    case 1:
                        temporaryShortcutId = shortcutId;
                        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                        pickIntent.putExtra(Intent.EXTRA_INTENT,new Intent(Intent.ACTION_CREATE_SHORTCUT));
                        startActivityForResult(pickIntent,FAVORITE_SHORTCUT_1);
                        break;
                    case 2:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_SOUND_MODE);
                        setText(shortcutId,R.string.sound_mode,"");
                        setShortcutIcon(shortcutId,null);
                        break;
                    case 3:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_DEFAULT);
                        setText(shortcutId,R.string.shortcut_volume_key_default,"");
                        setShortcutIcon(shortcutId,null);
                        break;
                    case 4:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_TYPE_DO_NOTHING);
                        setText(shortcutId,R.string.no_action,"");
                        setShortcutIcon(shortcutId,null);
                        break;
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    private void dialogGesture(final int shortcutId){
        String[] items;
        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
            items = new String[]{getString(R.string.launch_app),T.SETTINGS_SHORTCUTS,getString(R.string.gesture_action_sms_desc),getString(R.string.gesture_action_call_desc),getString(R.string.turn_off_screen),getString(R.string.sound_mode),getString(R.string.unlock),getString(R.string.no_action)};
        }else{
            items = new String[]{getString(R.string.launch_app),T.SETTINGS_SHORTCUTS,getString(R.string.gesture_action_sms_desc),getString(R.string.gesture_action_call_desc),getString(R.string.turn_off_screen),getString(R.string.sound_mode),getString(R.string.drop_statusbar), //not supported 5.0
                    getString(R.string.unlock),getString(R.string.no_action)};
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.gesture_selection));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int selection){
                Intent i;
                if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                    switch(selection){
                        case 0:
                            i = new Intent(context,ListInstalledApps.class);
                            i.putExtra(C.LIST_APPS_ID,shortcutId);
                            i.putExtra(C.LIST_APPS_PROFILE,profile);
                            startActivityForResult(i,SELECTED_SHORTCUT);
                            break;
                        case 1:
                            temporaryShortcutId = shortcutId;
                            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                            pickIntent.putExtra(Intent.EXTRA_INTENT,new Intent(Intent.ACTION_CREATE_SHORTCUT));
                            startActivityForResult(pickIntent,FAVORITE_SHORTCUT_1);
                            break;
                        case 2:
                            i = new Intent(Intent.ACTION_PICK);
                            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            startActivityForResult(i,PICK_CONTACT_SMS);
                            break;
                        case 3:
                            i = new Intent(Intent.ACTION_PICK);
                            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            startActivityForResult(i,PICK_CONTACT_CALL);
                            break;
                        case 4:
                            if(!deviceAdminHandler.isDeviceAdminActivated()){
                                deviceAdminHandler.launchDeviceAdmin(getActivity(),DEVICE_ADMIN_SET);
                            }else{
                                setShortcutAction(shortcutId,DataAppsSelection.ACTION_OFF_SCREEN);
                                setText(shortcutId,R.string.turn_off_screen,"");
                                setShortcutIcon(shortcutId,null);
                            }
                            break;
                        case 5:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_SOUND_MODE);
                            setText(shortcutId,R.string.sound_mode,"");
                            setShortcutIcon(shortcutId,null);
                            break;
                        case 6:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_UNLOCK);
                            setText(shortcutId,R.string.unlock,"");
                            setShortcutIcon(shortcutId,R.drawable.ic_lock_open_white_48dp);

                            if(isPinActivated()){
                                setCBox(shortcutId,true);
                                shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                            }
                            break;
                        case 7:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_TYPE_DO_NOTHING);
                            setText(shortcutId,R.string.no_action,"");
                            setShortcutIcon(shortcutId,null);
                            break;
                    }
                }else{
                    switch(selection){
                        case 0:
                            i = new Intent(context,ListInstalledApps.class);
                            i.putExtra(C.LIST_APPS_ID,shortcutId);
                            i.putExtra(C.LIST_APPS_PROFILE,profile);
                            startActivityForResult(i,SELECTED_SHORTCUT);
                            break;
                        case 1:
                            temporaryShortcutId = shortcutId;
                            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                            pickIntent.putExtra(Intent.EXTRA_INTENT,new Intent(Intent.ACTION_CREATE_SHORTCUT));
                            startActivityForResult(pickIntent,FAVORITE_SHORTCUT_1);
                            break;
                        case 2:
                            i = new Intent(Intent.ACTION_PICK);
                            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            startActivityForResult(i,PICK_CONTACT_SMS);
                            break;
                        case 3:
                            i = new Intent(Intent.ACTION_PICK);
                            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                            startActivityForResult(i,PICK_CONTACT_CALL);
                            break;
                        case 4:
                            if(!deviceAdminHandler.isDeviceAdminActivated()){
                                deviceAdminHandler.launchDeviceAdmin(getActivity(),DEVICE_ADMIN_SET);
                            }else{
                                setShortcutAction(shortcutId,DataAppsSelection.ACTION_OFF_SCREEN);
                                setText(shortcutId,R.string.turn_off_screen,"");
                                setShortcutIcon(shortcutId,null);
                            }
                            break;
                        case 5:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_SOUND_MODE);
                            setText(shortcutId,R.string.sound_mode,"");
                            setShortcutIcon(shortcutId,null);
                            break;
                        case 6:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_EXPAND_STATUS_BAR);
                            setText(shortcutId,R.string.drop_statusbar,"");
                            setShortcutIcon(shortcutId,null);
                            break;
                        case 7:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_UNLOCK);
                            setText(shortcutId,R.string.unlock,"");
                            setShortcutIcon(shortcutId,R.drawable.ic_lock_open_white_48dp);

                            if(isPinActivated()){
                                setCBox(shortcutId,true);
                                shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                            }
                            break;
                        case 8:
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_TYPE_DO_NOTHING);
                            setText(shortcutId,R.string.no_action,"");
                            setShortcutIcon(shortcutId,null);
                            break;
                    }
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    private void dialogTap(final int shortcutId){
        final String[] items = new String[]{getString(R.string.launch_app),T.SETTINGS_SHORTCUTS,getString(R.string.gesture_action_sms_desc),getString(R.string.gesture_action_call_desc),getString(R.string.turn_off_screen),getString(R.string.sound_mode),getString(R.string.unlock),getString(R.string.no_action)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.gesture_selection));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int selection){
                Intent i;
                switch(selection){
                    case 0:
                        i = new Intent(context,ListInstalledApps.class);
                        i.putExtra(C.LIST_APPS_ID,shortcutId);
                        i.putExtra(C.LIST_APPS_PROFILE,profile);
                        startActivityForResult(i,SELECTED_SHORTCUT);
                        break;
                    case 1:
                        temporaryShortcutId = shortcutId;
                        Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                        pickIntent.putExtra(Intent.EXTRA_INTENT,new Intent(Intent.ACTION_CREATE_SHORTCUT));
                        startActivityForResult(pickIntent,FAVORITE_SHORTCUT_1);
                        break;
                    case 2:
                        i = new Intent(Intent.ACTION_PICK);
                        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        startActivityForResult(i,PICK_CONTACT_SMS);
                        break;
                    case 3:
                        i = new Intent(Intent.ACTION_PICK);
                        i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        startActivityForResult(i,PICK_CONTACT_CALL);
                        break;
                    case 4:
                        if(!deviceAdminHandler.isDeviceAdminActivated()){
                            deviceAdminHandler.launchDeviceAdmin(getActivity(),DEVICE_ADMIN_SET);
                        }else{
                            setShortcutAction(shortcutId,DataAppsSelection.ACTION_OFF_SCREEN);
                            setText(shortcutId,R.string.turn_off_screen,"");
                            setShortcutIcon(shortcutId,null);
                        }
                        break;
                    case 5:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_SOUND_MODE);
                        setText(shortcutId,R.string.sound_mode,"");
                        setShortcutIcon(shortcutId,null);
                        break;
                    case 6:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_UNLOCK);
                        setText(shortcutId,R.string.unlock,"");
                        setShortcutIcon(shortcutId,R.drawable.ic_lock_open_white_48dp);

                        if(isPinActivated()){
                            setCBox(shortcutId,true);
                            shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }
                        break;
                    case 7:
                        setShortcutAction(shortcutId,DataAppsSelection.ACTION_TYPE_DO_NOTHING);
                        setText(shortcutId,R.string.no_action,"");
                        setShortcutIcon(shortcutId,null);
                        break;
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    private void cropImage(Intent data,final int imageW,final int imageH){
        final Uri selectedImage = data.getData();
        final Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,0);
        int cropNumber = list.size();
        if(cropNumber==0){
            Toast.makeText(context,"Selection errors",Toast.LENGTH_SHORT).show();
            return;
        }else{
            intent.setData(selectedImage);
            intent.putExtra("outputX",imageW);
            intent.putExtra("outputY",imageH);
            intent.putExtra("aspectX",1);
            intent.putExtra("aspectY",1);
            intent.putExtra("crop",true);
            intent.putExtra("scale",true);
            intent.putExtra("return-data",true);

            if(cropNumber==1){
                final Intent i = new Intent(intent);
                final ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
                startActivityForResult(i,CROP_PICTURE);
            }else{
                if(isGooglePlusCropAvailable(list)){
                    for(ResolveInfo res : list){
                        if(res.activityInfo.packageName.equals("com.google.android.apps.plus")){
                            Intent i = new Intent(intent);
                            i.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
                            startActivityForResult(i,CROP_PICTURE);
                            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"SettingsShortcutList>cropImage>isGooglePlusCropAvailable: "+
                                    res.activityInfo.packageName+"/"+res.activityInfo.name);
                            return;
                        }
                    }
                }else{
                    final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
                    for(ResolveInfo res : list){
                        final CropOption co = new CropOption();

                        co.title = context.getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                        co.icon = context.getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                        co.appIntent = new Intent(intent);
                        co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
                        cropOptions.add(co);

                    }
                    final CropOptionAdapter adapter = new CropOptionAdapter(context,cropOptions);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose Crop App");
                    builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int item){
                            startActivityForResult(cropOptions.get(item).appIntent,CROP_PICTURE);
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener(){
                        @Override
                        public void onCancel(DialogInterface dialog){
                            if(selectedImage!=null){
                                context.getContentResolver().delete(selectedImage,null,null);
                            }
                        }
                    });
                    alert = builder.create();
                    alert.show();
                }
            }
        }
    }

    private boolean isGooglePlusCropAvailable(List<ResolveInfo> list){
        for(ResolveInfo res : list){
            if(res.activityInfo.packageName.equals("com.google.android.apps.plus")){
                return true;
            }
        }
        return false;
    }

    private void iconAction1(){
        try{
            //final String ACTION_PICK_ICON1 = "org.adw.launcher.THEMES"; //ADW
            final String ACTION_PICK_ICON2 = "org.adw.launcher.icons.ACTION_PICK_ICON"; //ADW
            //final String ACTION_PICK_ICON3 = "com.gau.go.launcherex.theme";	//GO

            //final String CAT_PICK_ICON1 = "com.anddoes.launcher.THEME"; //APEX
            //final String CAT_PICK_ICON1 = "com.teslacoilsw.launcher.THEME"; //NOVA
            //final String CAT_PICK_ICON1 = "com.fede.launcher.THEME_ICONPACK"; //LAUNCHER PRO

            //final Intent i = new Intent(context,PickerDefaultIcons.class);
            final Intent i = new Intent();
            //i.setAction(ACTION_PICK_ICON1);
            i.setAction(ACTION_PICK_ICON2);
            //i.setAction(ACTION_PICK_ICON3);
            //i.addCategory(CAT_PICK_ICON1);
            //i.addCategory(CAT_PICK_ICON2);
            //i.addCategory(CAT_PICK_ICON3);
            //i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(i,SELECTED_ICON_1);
            //startActivityIfNeeded(i, SELECTED_ICON_1);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void iconAction2(){
        try{
            final Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,SELECTED_ICON_2);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    //load layout
    /*
    private ProgressBar progressBar(){
		final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(-1,-1);
		pr.setMargins(C.dpToPx(context,100),C.dpToPx(context,100),C.dpToPx(context,100),C.dpToPx(context,100));
		progressBarView = new ProgressBar(context,null,android.R.attr.progressBarStyleLarge);
		progressBarView.setLayoutParams(pr);
		progressBarView.setIndeterminateDrawable(context.getResources().getDrawable(R.drawable.progress));
		return progressBarView;
	}*/
    private LinearLayout lytTitle(int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(-1,-2);
        final LinearLayout lyt = new LinearLayout(context);
        lyt.setLayoutParams(pr);
        lyt.setOrientation(LinearLayout.HORIZONTAL);
        lyt.addView(txtTitle(getTitle(shortcutId)));
        lyt.addView(txtTitlePIN());
        return lyt;
    }

    private LinearLayout lytShortcutMain(int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(-1,-2);
        pr.setMargins(C.dpToPx(context,5),0,C.dpToPx(context,5),0);
        final LinearLayout lyt = new LinearLayout(context);
        lyt.setLayoutParams(pr);
        lyt.setOrientation(LinearLayout.HORIZONTAL);
        lyt.setGravity(Gravity.LEFT | Gravity.CENTER);
        lyt.addView(lytShortcutSub(shortcutId));
        lyt.addView(cBox(shortcutId));
        return lyt;
    }

    private LinearLayout lytShortcutSub(final int shortcutId){
        final boolean cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(0,-2,1);
        lytShortcutSub = new LinearLayout(context);
        lytShortcutSub.setId(shortcutId);
        lytShortcutSub.setLayoutParams(pr);
        lytShortcutSub.setOrientation(LinearLayout.HORIZONTAL);
        lytShortcutSub.setGravity(Gravity.CENTER);
        lytShortcutSub.setClickable(true);
        lytShortcutSub.setBackgroundResource(R.drawable.layout_selector);
        lytShortcutSub.addView(lytShortcutText(shortcutId));
        lytShortcutSub.addView(img(shortcutId));
        lytShortcutSub.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                switch(v.getId()){
                    case DataAppsSelection.SHORTCUT_APP_01:
                    case DataAppsSelection.SHORTCUT_APP_02:
                    case DataAppsSelection.SHORTCUT_APP_03:
                    case DataAppsSelection.SHORTCUT_APP_04:
                    case DataAppsSelection.SHORTCUT_APP_05:
                    case DataAppsSelection.SHORTCUT_APP_06:
                    case DataAppsSelection.SHORTCUT_APP_07:
                    case DataAppsSelection.SHORTCUT_APP_08:
                        temporaryShortcutId = shortcutId;
                        dialogShortcutApp(shortcutId);
                        break;
                    case DataAppsSelection.VOLUME_UP:
                    case DataAppsSelection.VOLUME_DOWN:
                        dialogVolume(shortcutId);
                        break;
                    case DataAppsSelection.GESTURE_UP:
                    case DataAppsSelection.GESTURE_DOWN:
                    case DataAppsSelection.GESTURE_LEFT:
                    case DataAppsSelection.GESTURE_RIGHT:
                    case DataAppsSelection.GESTURE_UP_2:
                    case DataAppsSelection.GESTURE_DOWN_2:
                    case DataAppsSelection.GESTURE_LEFT_2:
                    case DataAppsSelection.GESTURE_RIGHT_2:
                        temporaryShortcutId = shortcutId;
                        if(cBoxLockerOk){
                            dialogGesture(shortcutId);
                        }else{
                            myAlertDialog.upgradeLocker();
                        }
                        break;
                    case DataAppsSelection.DOUBLE_TAP1:
                    case DataAppsSelection.DOUBLE_TAP2:
                    case DataAppsSelection.TRIPLE_TAP1:
                    case DataAppsSelection.TRIPLE_TAP2:
                        temporaryShortcutId = shortcutId;
                        if(cBoxLockerOk){
                            dialogTap(shortcutId);
                        }else{
                            myAlertDialog.upgradeLocker();
                        }
                        break;
                }
            }
        });
        return lytShortcutSub;
    }

    private LinearLayout lytShortcutText(final int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(0,-2,1);
        LinearLayout lyt = new LinearLayout(context);
        lyt.setLayoutParams(pr);
        lyt.setOrientation(LinearLayout.VERTICAL);
        lyt.setGravity(Gravity.LEFT | Gravity.CENTER);
        lyt.addView(txtDesc(shortcutId));
        lyt.addView(txtDescSub(shortcutId));
        return lyt;
    }

    private View line(int h,String color){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(-1,h);
        final View v = new View(context);
        v.setLayoutParams(pr);
        v.setBackgroundColor(Color.parseColor(color));
        return v;
    }

    private TextView txtTitle(String strLabel){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(0,-2,1);
        final TextView txt = new TextView(context);
        txt.setLayoutParams(pr);
        txt.setPadding(C.dpToPx(context,2),C.dpToPx(context,5),C.dpToPx(context,2),C.dpToPx(context,5));
        txt.setText(strLabel);
        txt.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return txt;
    }

    private TextView txtTitlePIN(){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(C.dpToPx(context,50),-2);
        pr.setMargins(0,0,C.dpToPx(context,5),0);
        final TextView txt = new TextView(context);
        txt.setLayoutParams(pr);
        txt.setGravity(Gravity.CENTER);
        txt.setText("PIN");
        txt.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return txt;
    }

    private TextView txtDesc(int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(-2,-2);
        final TextView txt = new TextView(context);
        txt.setLayoutParams(pr);
        txt.setText(getDesc(shortcutId));
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        txt.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return txt;
    }

    private TextView txtDescSub(int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(-2,-2);
        final TextView txt = new TextView(context);
        txt.setId(shortcutId+TXT_ID);
        txt.setLayoutParams(pr);
        txt.setText(loadShortcutText(shortcutId,profile));
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        return txt;
    }

    private String loadShortcutText(int shortcutId,int profile){
        return prefs.getString(getTextPref(shortcutId,profile),"");
    }

    private ImageView img(final int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(C.dpToPx(context,50),C.dpToPx(context,50));
        final ImageView imgIcon = new ImageView(context);
        imgIcon.setId(shortcutId+IMG_ID);
        imgIcon.setLayoutParams(pr);
        imgIcon.setPadding(C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5));
        int action = shortcutData.getShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_ACTION,profile);
        if(shortcutId>DataAppsSelection.SHORTCUT_APP_08){
            if(action==DataAppsSelection.ACTION_DEFAULT ||
                    action==DataAppsSelection.ACTION_DIRECT_CALL ||
                    action==DataAppsSelection.ACTION_DIRECT_SMS ||
                    action==DataAppsSelection.ACTION_OFF_SCREEN ||
                    action==DataAppsSelection.ACTION_EXPAND_STATUS_BAR ||
                    action==DataAppsSelection.ACTION_TYPE_DO_NOTHING){
                return imgIcon;
            }
        }
        handler.post(new Runnable(){
            @Override
            public void run(){
                //Log.e("img","img:"+getIcon(shortcutId));
                loadImage(imgIcon,getIcon(shortcutId,profile));
            }
        });
        return imgIcon;
    }

    private void loadImage(final ImageView v,final String imgName){
        handler.post(new Runnable(){
            @Override
            public void run(){
                try{
                    if(imgName.equals("")){
                        v.setImageBitmap(null);
                        ;
                    }
                    String icon = C.EXT_STORAGE_DIR+imgName;
                    final File fileIcon = new File(icon);
                    if(fileIcon.exists()){
                        final Bitmap bmIcon = BitmapFactory.decodeFile(icon);
                        v.setImageBitmap(bmIcon);
                    }
                }catch(Exception e){
                    mLocker.saveErrorLog(null,e);
                }
            }
        });
    }

    private CheckBox cBox(final int shortcutId){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(C.dpToPx(context,50),C.dpToPx(context,50));
        final CheckBox cBox = new CheckBox(context);
        pr.setMargins(C.dpToPx(context,20),0,C.dpToPx(context,-10),0);
        cBox.setId(shortcutId+CBOX_ID);
        cBox.setLayoutParams(pr);
        cBox.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                temporaryShortcutId = shortcutId;
                final boolean cBoxLockerOk = prefs.getBoolean(C.C_LOCKER_OK,true);
                final boolean isChecked = ((CheckBox)v).isChecked();

                if(!isChecked){
                    int action = shortcutData.getShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_ACTION,profile);
                    if(shortcutId==DataAppsSelection.SHORTCUT_APP_01 && //if it's unlock action, force to set true and return
                            action==DataAppsSelection.ACTION_DEFAULT){
                        ((CheckBox)v).setChecked(true);
                        return;
                    }else if(action==DataAppsSelection.ACTION_UNLOCK){//if it's unlock action, force to set true and return
                        ((CheckBox)v).setChecked(true);
                        return;
                    }
                }
                if(cBoxLockerOk){
                    launchProSettings(isChecked,shortcutId,PRO_PIN_UNLOCK);
                }else{
                    if(isPinAllowed() || !isChecked){
                        launchProSettings(isChecked,shortcutId,PRO_PIN_UNLOCK);
                    }else{
                        ((CheckBox)v).setChecked(false);
                        myAlertDialog.upgradeLocker();
                        Toast.makeText(context,getString(R.string.pin_not_allow),Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        int pin = shortcutData.getShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,profile);
        if(pin==DataAppsSelection.PIN_REQUIRED){
            cBox.setChecked(true);
        }else{
            cBox.setChecked(false);
        }
        return cBox;
    }

    private String getTitle(int shortcutId){
        switch(shortcutId){
            case DataAppsSelection.SHORTCUT_TYPE_LOCK_ICON:
                return getString(R.string.shortcut_lock_circle);
            case DataAppsSelection.SHORTCUT_TYPE_VOLUME_KEY:
                return getString(R.string.shortcut_volume_key);
            case DataAppsSelection.SHORTCUT_TYPE_GESTURE_SINGLE:
                return getString(R.string.shortcut_gesture);
            case DataAppsSelection.SHORTCUT_TYPE_GESTURE_DOUBLE:
                return getString(R.string.shortcut_gesture);
            case DataAppsSelection.SHORTCUT_TYPE_TAPPING:
                return getString(R.string.tapping_shortcut);
        }
        return "";
    }

    private String getDesc(int shortcutId){
        switch(shortcutId){
            case DataAppsSelection.SHORTCUT_APP_01:
                return getString(R.string.shortcut_lock_1)+": ";
            case DataAppsSelection.SHORTCUT_APP_02:
                return getString(R.string.shortcut_lock_2)+": ";
            case DataAppsSelection.SHORTCUT_APP_03:
                return getString(R.string.shortcut_lock_3)+": ";
            case DataAppsSelection.SHORTCUT_APP_04:
                return getString(R.string.shortcut_lock_4)+": ";
            case DataAppsSelection.SHORTCUT_APP_05:
                return getString(R.string.shortcut_lock_5)+": ";
            case DataAppsSelection.SHORTCUT_APP_06:
                return getString(R.string.shortcut_lock_6)+": ";
            case DataAppsSelection.SHORTCUT_APP_07:
                return getString(R.string.shortcut_lock_7)+": ";
            case DataAppsSelection.SHORTCUT_APP_08:
                return getString(R.string.shortcut_lock_8)+": ";
            case DataAppsSelection.VOLUME_UP:
                return getString(R.string.shortcut_volume_key_up)+": ";
            case DataAppsSelection.VOLUME_DOWN:
                return getString(R.string.shortcut_volume_key_down)+": ";
            case DataAppsSelection.GESTURE_UP:
                return getString(R.string.shortcut_gesture1_up)+": ";
            case DataAppsSelection.GESTURE_DOWN:
                return getString(R.string.shortcut_gesture1_down)+": ";
            case DataAppsSelection.GESTURE_LEFT:
                return getString(R.string.shortcut_gesture1_left)+": ";
            case DataAppsSelection.GESTURE_RIGHT:
                return getString(R.string.shortcut_gesture1_right)+": ";
            case DataAppsSelection.GESTURE_UP_2:
                return getString(R.string.shortcut_gesture2_up)+": ";
            case DataAppsSelection.GESTURE_DOWN_2:
                return getString(R.string.shortcut_gesture2_down)+": ";
            case DataAppsSelection.GESTURE_LEFT_2:
                return getString(R.string.shortcut_gesture2_left)+": ";
            case DataAppsSelection.GESTURE_RIGHT_2:
                return getString(R.string.shortcut_gesture2_right)+": ";
            case DataAppsSelection.DOUBLE_TAP1:
                return getString(R.string.double_tab1)+": ";
            case DataAppsSelection.DOUBLE_TAP2:
                return getString(R.string.double_tab2)+": ";
            case DataAppsSelection.TRIPLE_TAP1:
                return getString(R.string.triple_tab1)+": ";
            case DataAppsSelection.TRIPLE_TAP2:
                return getString(R.string.triple_tab2)+": ";
        }
        return "";
    }

    private String getDescSub(int shortcutId){
        switch(shortcutId){
            case DataAppsSelection.SHORTCUT_APP_01:
                return getString(R.string.unlock);
            case DataAppsSelection.SHORTCUT_APP_02:
                return getString(R.string.flash);
            case DataAppsSelection.SHORTCUT_APP_03:
                return getString(R.string.camera);
            case DataAppsSelection.SHORTCUT_APP_04:
                return getString(R.string.youtube);
            case DataAppsSelection.SHORTCUT_APP_05:
                return getString(R.string.internet);
            case DataAppsSelection.SHORTCUT_APP_06:
                return getString(R.string.map);
            case DataAppsSelection.SHORTCUT_APP_07:
                return getString(R.string.call);
            case DataAppsSelection.SHORTCUT_APP_08:
                return getString(R.string.sms);
            case DataAppsSelection.VOLUME_UP:
            case DataAppsSelection.VOLUME_DOWN:
                return getString(R.string.shortcut_volume_key_default);
            case DataAppsSelection.GESTURE_UP:
            case DataAppsSelection.GESTURE_DOWN:
            case DataAppsSelection.GESTURE_LEFT:
            case DataAppsSelection.GESTURE_RIGHT:
            case DataAppsSelection.GESTURE_UP_2:
            case DataAppsSelection.GESTURE_DOWN_2:
            case DataAppsSelection.GESTURE_LEFT_2:
            case DataAppsSelection.GESTURE_RIGHT_2:
            case DataAppsSelection.DOUBLE_TAP1:
            case DataAppsSelection.DOUBLE_TAP2:
            case DataAppsSelection.TRIPLE_TAP1:
            case DataAppsSelection.TRIPLE_TAP2:
                return getString(R.string.no_action);
        }
        return "";
    }

    private String getIcon(int shortcutId,int profile){
        switch(shortcutId){
            case DataAppsSelection.SHORTCUT_APP_01:
                return C.ICON_SHORTCUT_01+profile;
            case DataAppsSelection.SHORTCUT_APP_02:
                return C.ICON_SHORTCUT_02+profile;
            case DataAppsSelection.SHORTCUT_APP_03:
                return C.ICON_SHORTCUT_03+profile;
            case DataAppsSelection.SHORTCUT_APP_04:
                return C.ICON_SHORTCUT_04+profile;
            case DataAppsSelection.SHORTCUT_APP_05:
                return C.ICON_SHORTCUT_05+profile;
            case DataAppsSelection.SHORTCUT_APP_06:
                return C.ICON_SHORTCUT_06+profile;
            case DataAppsSelection.SHORTCUT_APP_07:
                return C.ICON_SHORTCUT_07+profile;
            case DataAppsSelection.SHORTCUT_APP_08:
                return C.ICON_SHORTCUT_08+profile;
            case DataAppsSelection.VOLUME_UP:
                return C.ICON_VOLUME_UP+profile;
            case DataAppsSelection.VOLUME_DOWN:
                return C.ICON_VOLUME_DOWN+profile;
            case DataAppsSelection.GESTURE_UP:
                return C.ICON_GESTURE_UP+profile;
            case DataAppsSelection.GESTURE_DOWN:
                return C.ICON_GESTURE_DOWN+profile;
            case DataAppsSelection.GESTURE_LEFT:
                return C.ICON_GESTURE_LEFT+profile;
            case DataAppsSelection.GESTURE_RIGHT:
                return C.ICON_GESTURE_RIGHT+profile;
            case DataAppsSelection.GESTURE_UP_2:
                return C.ICON_GESTURE_UP_2+profile;
            case DataAppsSelection.GESTURE_DOWN_2:
                return C.ICON_GESTURE_DOWN_2+profile;
            case DataAppsSelection.GESTURE_LEFT_2:
                return C.ICON_GESTURE_LEFT_2+profile;
            case DataAppsSelection.GESTURE_RIGHT_2:
                return C.ICON_GESTURE_RIGHT_2+profile;
            case DataAppsSelection.DOUBLE_TAP1:
                return C.ICON_DOUBLE_TAP1+profile;
            case DataAppsSelection.DOUBLE_TAP2:
                return C.ICON_DOUBLE_TAP2+profile;
            case DataAppsSelection.TRIPLE_TAP1:
                return C.ICON_TRIPLE_TAP1+profile;
            case DataAppsSelection.TRIPLE_TAP2:
                return C.ICON_TRIPLE_TAP2+profile;
        }
        return null;
    }

    private String getTextPref(int shortcutId,int profile){
        switch(shortcutId){
            case DataAppsSelection.SHORTCUT_APP_01:
                return "SHORTCUT_APP_01"+profile;
            case DataAppsSelection.SHORTCUT_APP_02:
                return "SHORTCUT_APP_02"+profile;
            case DataAppsSelection.SHORTCUT_APP_03:
                return "SHORTCUT_APP_03"+profile;
            case DataAppsSelection.SHORTCUT_APP_04:
                return "SHORTCUT_APP_04"+profile;
            case DataAppsSelection.SHORTCUT_APP_05:
                return "SHORTCUT_APP_05"+profile;
            case DataAppsSelection.SHORTCUT_APP_06:
                return "SHORTCUT_APP_06"+profile;
            case DataAppsSelection.SHORTCUT_APP_07:
                return "SHORTCUT_APP_07"+profile;
            case DataAppsSelection.SHORTCUT_APP_08:
                return "SHORTCUT_APP_08"+profile;
            case DataAppsSelection.VOLUME_UP:
                return "btnVolumeUpText"+profile;
            case DataAppsSelection.VOLUME_DOWN:
                return "btnVolumeDownText"+profile;
            case DataAppsSelection.GESTURE_UP:
                return "btnGesUpText"+profile;
            case DataAppsSelection.GESTURE_DOWN:
                return "btnGesDownText"+profile;
            case DataAppsSelection.GESTURE_LEFT:
                return "btnGesLeftText"+profile;
            case DataAppsSelection.GESTURE_RIGHT:
                return "btnGesRightText"+profile;
            case DataAppsSelection.GESTURE_UP_2:
                return "btnGesUpText_2"+profile;
            case DataAppsSelection.GESTURE_DOWN_2:
                return "btnGesDownText_2"+profile;
            case DataAppsSelection.GESTURE_LEFT_2:
                return "btnGesLeftText_2"+profile;
            case DataAppsSelection.GESTURE_RIGHT_2:
                return "btnGesRightText_2"+profile;
            case DataAppsSelection.DOUBLE_TAP1:
                return "btnDoubleTapText_1"+profile;
            case DataAppsSelection.DOUBLE_TAP2:
                return "btnDoubleTapText_2"+profile;
            case DataAppsSelection.TRIPLE_TAP1:
                return "btnTripleTapText_1"+profile;
            case DataAppsSelection.TRIPLE_TAP2:
                return "btnTripleTapText_2"+profile;
        }
        return "";
    }

    private String getProfile(){
        switch(profile){
            case C.PROFILE_DEFAULT:
                return getString(R.string.fragment_locker_profile);
            case C.PROFILE_MUSIC:
                return getString(R.string.fragment_music_profile);
            case C.PROFILE_LOCATION:
                return getString(R.string.fragment_location_profile);
            case C.PROFILE_TIME:
                return getString(R.string.fragment_time_profile);
        }
        return getString(R.string.fragment_wifi_profile);
    }

    private int getDefaultIcon(int shortcutId){
        switch(shortcutId){
            case DataAppsSelection.SHORTCUT_APP_01:
                return R.drawable.ic_lock_open_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_02:
                return R.drawable.ic_flashlight_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_03:
                return R.drawable.ic_camera_alt_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_04:
                return R.drawable.ic_youtube_play_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_05:
                return R.drawable.ic_earth_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_06:
                return R.drawable.ic_location_on_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_07:
                return R.drawable.ic_call_white_48dp;
            case DataAppsSelection.SHORTCUT_APP_08:
                return R.drawable.ic_textsms_white_48dp;
        }
        return 0;
    }

    private void launchProSettings(boolean isChecked,final int shortcutId,int intPRO){
        try{
            switch(intPRO){
                case PRO_PIN_UNLOCK:
                    final boolean isPinSet = isPinActivated();
                    if(isChecked){
                        if(isPinSet){
                            shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }else{
                            new MyAlertDialog(context).selectSecurityUnlockType(new OnDialogListener(){
                                @Override
                                public void onDialogClickListener(int dialogType,int result){
                                    if(dialogType==MyAlertDialog.OK){
                                        int securityType;
                                        if(result==0){//pin
                                            securityType = C.SECURITY_UNLOCK_PIN;
                                        }else{//pattern
                                            securityType = C.SECURITY_UNLOCK_PATTERN;
                                        }
                                        final Intent i = new Intent(context,SettingsPasswordUnlock.class);
                                        i.putExtra(C.SECURITY_TYPE,securityType);
                                        startActivityForResult(i,PRO_PIN_UNLOCK);
                                    }else{
                                        clearSecurity();
                                    }
                                }
                            });
                        }
                    }else{
                        shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_NOT_REQUIRED);
                        if(isPinSet){
                            //nothing
                        }else{
                            clearSecurity();
                        }
                    }
                    break;
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private boolean isPinAllowed(){
        final List<InfoAppsSelection> appsList = shortcutData.getApps(DataAppsSelection.APP_TYPE_SHORTCUT,profile);
        for(InfoAppsSelection item : appsList){
            if(item.getShortcutId()==DataAppsSelection.SHORTCUT_APP_01 && item.getShortcutAction()==DataAppsSelection.ACTION_DEFAULT){
                //skip this
            }else if(item.getShortcutAction()!=DataAppsSelection.ACTION_UNLOCK){
                if(item.getShortcutPIN()==DataAppsSelection.PIN_REQUIRED){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPinActivated(){
        final int securityType = prefs.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_NONE);
        final boolean cBoxEnablePassUnlock = prefs.getBoolean("cBoxEnablePassUnlock",false);
        if(securityType!=C.SECURITY_UNLOCK_NONE && cBoxEnablePassUnlock){
            return true;
        }
        return false;
    }

    private void clearSecurity(){
        editor.putBoolean("cBoxEnablePassUnlock",false);
        editor.putBoolean("cBoxBlockStatusbar",false);
        editor.putBoolean("cBoxBlockRecentApps",false);
        editor.putInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_NONE);
        //PIN
        editor.putInt("intPass2nd01",-1);
        editor.putInt("intPass2nd02",-1);
        editor.putInt("intPass2nd03",-1);
        editor.putInt("intPass2nd04",-1);
        editor.putInt("intPass2nd05",-1);
        editor.putInt("intPass2nd06",-1);
        editor.putInt("intPass2nd07",-1);
        editor.putInt("intPass2nd08",-1);
        //pattern
        editor.putString(C.PATTERN_LOCK,"");
        editor.commit();
    }
}