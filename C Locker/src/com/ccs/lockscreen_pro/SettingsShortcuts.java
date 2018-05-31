package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.myclocker.T;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.CropOption;
import com.ccs.lockscreen.utils.CropOptionAdapter;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyRingUnlock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SettingsShortcuts extends BaseActivity{
    private static final int SELECTED_ICON_1 = 1, SELECTED_ICON_2 = 2, CROP_PICTURE = 3, SELECTED_SHORTCUT = 4, SELECTED_LOCK_ANIMATION_COLOR = 5, SELECTED_ICON_ON_PRESS_COLOR = 6, SELECTED_FAVORITE_SHORTCUT_1 = 7, SELECTED_FAVORITE_SHORTCUT_2 = 8;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private CheckBox cBoxShowShortcutIcons, cBoxLockLayoutAnimation, cBoxIconBgOnPressEffect;
    private RadioButton cBoxLockAnimationType1, cBoxLockAnimationType2, cBoxLockAnimationType3, cBoxLockAnimationType4, cBoxLockAnimationType5, cBoxIconBgOnPressType1, cBoxIconBgOnPressType2, cBoxIconBgOnPressType3, cBoxIconBgOnPressType4;
    private AlertDialog.Builder builder;
    private AlertDialog alert;
    private boolean cBoxHideCenterLockIcon;
    private int intIconBgOnPressType, customIconNo, temporaryShortcutId = -1;
    private String strColorIconBgOnPress;
    private LinearLayout lytMain, lytShowAppsImageAllTime;
    private MyRingUnlock lytMyRingUnlock;
    private DataAppsSelection shortcutData;
    private int profile = C.PROFILE_DEFAULT;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_shortcut_layout);
        setBasicBackKeyAction();

        final Bundle extras = getIntent().getExtras();
        int intLockStyle = extras.getInt("intLockStyle");
        if(extras.containsKey(C.LIST_APPS_PROFILE)){
            this.profile = extras.getInt(C.LIST_APPS_PROFILE);
        }

        myAlertDialog = new MyAlertDialog(this);
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            //intDisplayHeight = metrics.heightPixels;
            shortcutData = new DataAppsSelection(this);
            shortcutData.setProfile(profile);

            lytMain = (LinearLayout)findViewById(R.id.lytSettingsShortcutMain);
            lytShowAppsImageAllTime = (LinearLayout)findViewById(R.id.lytShowAppsImageAllTime);

            cBoxShowShortcutIcons = (CheckBox)findViewById(R.id.cboxShowAppsImageAllTime);
            cBoxLockLayoutAnimation = (CheckBox)findViewById(R.id.cBoxLockLayoutAnimation);
            cBoxIconBgOnPressEffect = (CheckBox)findViewById(R.id.cBoxIconBgOnPressEffect);

            cBoxLockAnimationType1 = (RadioButton)findViewById(R.id.cBoxLockAnimationType1);
            cBoxLockAnimationType2 = (RadioButton)findViewById(R.id.cBoxLockAnimationType2);
            cBoxLockAnimationType3 = (RadioButton)findViewById(R.id.cBoxLockAnimationType3);
            cBoxLockAnimationType4 = (RadioButton)findViewById(R.id.cBoxLockAnimationType4);
            cBoxLockAnimationType5 = (RadioButton)findViewById(R.id.cBoxLockAnimationType5);

            cBoxIconBgOnPressType1 = (RadioButton)findViewById(R.id.cBoxIconBgOnPressType1);
            cBoxIconBgOnPressType2 = (RadioButton)findViewById(R.id.cBoxIconBgOnPressType2);
            cBoxIconBgOnPressType3 = (RadioButton)findViewById(R.id.cBoxIconBgOnPressType3);
            cBoxIconBgOnPressType4 = (RadioButton)findViewById(R.id.cBoxIconBgOnPressType4);

            loadSettings();
            loadLockLayout(intLockStyle);
            setIconBgOnPress();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            saveSettings();
            if(shortcutData!=null){
                shortcutData.close();
                shortcutData = null;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        try{
            if(myAlertDialog!=null){
                myAlertDialog.close();
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_shortcut;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_shortcut_layout_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.itemResetShortcutLayout:
                resetAllShortcut();
                break;
            default:
                break;
        }
        return true;
    }

    private void resetAllShortcut(){
        lytMyRingUnlock.imgLock.setImageResource(R.drawable.ic_https_white_48dp);
        lytMyRingUnlock.imgApps01.setImageResource(R.drawable.ic_lock_open_white_48dp);
        lytMyRingUnlock.imgApps02.setImageResource(R.drawable.ic_flashlight_white_48dp);
        lytMyRingUnlock.imgApps03.setImageResource(R.drawable.ic_camera_alt_white_48dp);
        lytMyRingUnlock.imgApps04.setImageResource(R.drawable.ic_youtube_play_white_48dp);
        lytMyRingUnlock.imgApps05.setImageResource(R.drawable.ic_earth_white_48dp);
        lytMyRingUnlock.imgApps06.setImageResource(R.drawable.ic_location_on_white_48dp);
        lytMyRingUnlock.imgApps07.setImageResource(R.drawable.ic_call_white_48dp);
        lytMyRingUnlock.imgApps08.setImageResource(R.drawable.ic_textsms_white_48dp);

        resetShortcutData(DataAppsSelection.SHORTCUT_APP_01);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_02);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_03);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_04);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_05);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_06);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_07);
        resetShortcutData(DataAppsSelection.SHORTCUT_APP_08);
    }

    private void resetShortcutData(int shortcutId){
        final List<InfoAppsSelection> appsList = shortcutData.getShortcutApps(shortcutId,profile);
        if(appsList.isEmpty()){
            final InfoAppsSelection app = new InfoAppsSelection(shortcutId);
            app.setAppType(DataAppsSelection.APP_TYPE_SHORTCUT);
            shortcutData.addApp(app);
        }else{
            shortcutData.resetShortcut(shortcutId,profile);
        }
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
                                setShortcutIcon(customIconNo,bm);
                                return;
                            }
                        }catch(Exception e){
                            saveErrorLogs(null,e);
                        }
                    }
                    Toast.makeText(this,"Selection errors",Toast.LENGTH_LONG).show();
                    break;
                case SELECTED_ICON_2:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            cropImage(data,144,144);
                            return;
                        }catch(Exception e){
                            saveErrorLogs(null,e);
                        }
                    }
                    Toast.makeText(this,"Selection errors",Toast.LENGTH_LONG).show();
                    break;
                case CROP_PICTURE:
                    if(resultCode==Activity.RESULT_OK){
                        Bundle extras = data.getExtras();
                        if(extras!=null){
                            Bitmap bm = extras.getParcelable("data");
                            setShortcutIcon(customIconNo,bm);
                            return;
                        }
                    }
                    Toast.makeText(this,"Selection errors",Toast.LENGTH_LONG).show();
                    break;
                case SELECTED_SHORTCUT:
                    if(resultCode==Activity.RESULT_OK){
                        int id = data.getExtras().getInt(C.LIST_APPS_ID);
                        Bitmap bm = (Bitmap)data.getExtras().get("appNameIcon");
                        setShortcutIcon(id,bm);
                    }
                    break;
                case SELECTED_LOCK_ANIMATION_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        String strColorLockAnimation = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");
                        editor.putString(P.STR_COLOR_LOCK_ANIMATION,strColorLockAnimation);
                        editor.commit();
                        lytMyRingUnlock.loadSettings();
                        lytMyRingUnlock.setLockAnimation(cBoxLockLayoutAnimation.isChecked(),true);
                        myAlertDialog.colorTheme(strColorLockAnimation);
                    }
                    break;
                case SELECTED_ICON_ON_PRESS_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strColorIconBgOnPress = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");
                        setIconBgOnPress();
                        myAlertDialog.colorTheme(strColorIconBgOnPress);
                    }else{
                        setIconBgOnPress();
                    }
                    break;
                case SELECTED_FAVORITE_SHORTCUT_1:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            //Intent.URI_INTENT_SCHEME = no use
                            startActivityForResult(Intent.parseUri(data.toUri(0),0),SELECTED_FAVORITE_SHORTCUT_2);
                            break;
                        }catch(Exception e){
                            saveErrorLogs(null,e);
                        }
                    }
                    Toast.makeText(this,"Shortcut Setting error",Toast.LENGTH_LONG).show();
                    break;
                case SELECTED_FAVORITE_SHORTCUT_2:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            if(data.hasExtra(Intent.EXTRA_SHORTCUT_INTENT) && data.hasExtra(Intent.EXTRA_SHORTCUT_NAME)){

                                Intent i = (Intent)data.getExtras().get(Intent.EXTRA_SHORTCUT_INTENT);
                                String name = data.getExtras().getString(Intent.EXTRA_SHORTCUT_NAME);
                                shortcutData.updateFavoriteShortcut(temporaryShortcutId,i.toUri(0).toString(),name);

                                if(data.hasExtra(Intent.EXTRA_SHORTCUT_ICON)){
                                    Bitmap bm = (Bitmap)data.getExtras().get(Intent.EXTRA_SHORTCUT_ICON);
                                    setShortcutIcon(temporaryShortcutId,getResizedBitmap(bm,124,124));
                                }else if(data.hasExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)){
                                    PackageManager packageManager = getPackageManager();
                                    ShortcutIconResource iconResource = (ShortcutIconResource)data.getExtras().get(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                                    Resources res = packageManager.getResourcesForApplication(iconResource.packageName);
                                    Drawable icon = res.getDrawable(res.getIdentifier(iconResource.resourceName,null,null));
                                    Bitmap bm = ((BitmapDrawable)icon).getBitmap();
                                    setShortcutIcon(temporaryShortcutId,getResizedBitmap(bm,124,124));
                                }
                            }
                            break;
                        }catch(Exception e){
                            saveErrorLogs(null,e);
                        }
                    }
                    Toast.makeText(this,"Shortcut Setting error",Toast.LENGTH_LONG).show();
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            //saveSettings();
            //finish();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void setShortcutIcon(int no,Bitmap bm){
        //bm = getResizedBitmap(bm,124,124);//default 144
        switch(no){
            case 0:
                lytMyRingUnlock.imgLock.setImageBitmap(bm);
                break;
            case 1:
                lytMyRingUnlock.imgApps01.setImageBitmap(bm);
                break;
            case 2:
                lytMyRingUnlock.imgApps02.setImageBitmap(bm);
                break;
            case 3:
                lytMyRingUnlock.imgApps03.setImageBitmap(bm);
                break;
            case 4:
                lytMyRingUnlock.imgApps04.setImageBitmap(bm);
                break;
            case 5:
                lytMyRingUnlock.imgApps05.setImageBitmap(bm);
                break;
            case 6:
                lytMyRingUnlock.imgApps06.setImageBitmap(bm);
                break;
            case 7:
                lytMyRingUnlock.imgApps07.setImageBitmap(bm);
                break;
            case 8:
                lytMyRingUnlock.imgApps08.setImageBitmap(bm);
                break;
        }
    }

    private void cropImage(Intent data,final int imageW,final int imageH){
        final Uri selectedImage = data.getData();
        final Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        final List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent,0);
        int cropNumber = list.size();
        if(cropNumber==0){
            Toast.makeText(this,"Selection errors",Toast.LENGTH_SHORT).show();
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
                            saveLogs("SettingsShortcutList>cropImage>isGooglePlusCropAvailable: "+
                                    res.activityInfo.packageName+"/"+res.activityInfo.name);
                            return;
                        }
                    }
                }else{
                    final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
                    for(ResolveInfo res : list){
                        final CropOption co = new CropOption();

                        co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                        co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                        co.appIntent = new Intent(intent);
                        co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
                        cropOptions.add(co);

                    }
                    final CropOptionAdapter adapter = new CropOptionAdapter(this,cropOptions);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsShortcuts.this);
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
                                getContentResolver().delete(selectedImage,null,null);
                            }
                        }
                    });
                    alert = builder.create();
                    alert.show();
                }
            }
        }
    }

    private void setIconBgOnPress(){
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut01,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut02,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut03,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut04,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut05,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut06,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut07,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
        setBackGroundDrawable(cBoxIconBgOnPressEffect.isChecked(),lytMyRingUnlock.lytShortcut08,setRoundColor(intIconBgOnPressType,strColorIconBgOnPress,110));
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

    private boolean isGooglePlusCropAvailable(List<ResolveInfo> list){
        for(ResolveInfo res : list){
            if(res.activityInfo.packageName.equals("com.google.android.apps.plus")){
                return true;
            }
        }
        return false;
    }

    private void setBackGroundDrawable(final boolean bln,final View v,final Drawable d){
        if(bln){
            v.setBackground(d);
        }else{
            v.setBackground(null);
        }
    }

    private Drawable setRoundColor(int type,String strColor,int size){
        try{
            GradientDrawable gd = null;
            if(type==C.BLINKING_TYPE_1){
                gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setStroke(2,Color.parseColor("#"+strColor));
            }else if(type==C.BLINKING_TYPE_2){
                int color01 = Color.parseColor("#"+strColor);
                int color02 = Color.parseColor("#88"+strColor);
                int color03 = Color.parseColor("#55"+strColor);
                int color04 = Color.parseColor("#22"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,0});
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                gd.setGradientRadius(size);
            }else if(type==C.BLINKING_TYPE_3){
                int color01 = Color.parseColor("#00"+strColor);
                int color02 = Color.parseColor("#11"+strColor);
                int color03 = Color.parseColor("#33"+strColor);
                int color04 = Color.parseColor("#"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,0});
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                gd.setGradientRadius(size);
            }else if(type==C.BLINKING_TYPE_4){
                int color01 = Color.parseColor("#"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{0,0});//transparent
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                gd.setGradientRadius(size);
                gd.setStroke(8,color01,20,40);
            }else{
                int color01 = Color.parseColor("#"+strColor);
                int color02 = Color.parseColor("#22"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,color02,color02,color02,color01});
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.SWEEP_GRADIENT);
                gd.setGradientRadius(size);
            }
            return gd;
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        return null;
    }

    private void loadSettings(){
        cBoxHideCenterLockIcon = prefs.getBoolean("cBoxHideCenterLockIcon",false);
        cBoxShowShortcutIcons.setChecked(prefs.getBoolean("cBoxShowShortcutIcons",false));

        cBoxLockLayoutAnimation.setChecked(prefs.getBoolean("cBoxLockLayoutAnimation",false));
        cBoxIconBgOnPressEffect.setChecked(prefs.getBoolean("cBoxIconBgOnPressEffect",false));

        cBoxLockAnimationType1.setChecked(prefs.getBoolean("cBoxLockAnimationType1",false));
        cBoxLockAnimationType2.setChecked(prefs.getBoolean("cBoxLockAnimationType2",false));
        cBoxLockAnimationType3.setChecked(prefs.getBoolean("cBoxLockAnimationType3",false));
        cBoxLockAnimationType4.setChecked(prefs.getBoolean("cBoxLockAnimationType4",true));
        cBoxLockAnimationType5.setChecked(prefs.getBoolean("cBoxLockAnimationType5",false));

        cBoxIconBgOnPressType1.setChecked(prefs.getBoolean("cBoxIconBgOnPressType1",false));
        cBoxIconBgOnPressType2.setChecked(prefs.getBoolean("cBoxIconBgOnPressType2",true));
        cBoxIconBgOnPressType3.setChecked(prefs.getBoolean("cBoxIconBgOnPressType3",false));
        cBoxIconBgOnPressType4.setChecked(prefs.getBoolean("cBoxIconBgOnPressType4",false));

        intIconBgOnPressType = prefs.getInt("intIconBgOnPressType",C.BLINKING_TYPE_2);
        strColorIconBgOnPress = prefs.getString(P.STR_COLOR_ICON_BG_ON_PRESS,"ffffff");
    }

    private void loadLockLayout(int intLockStyle){
        lytMyRingUnlock = new MyRingUnlock(this,intLockStyle);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lytMyRingUnlock.setLayoutParams(params);
        lytMyRingUnlock.setVisibility(View.VISIBLE);
        lytMain.addView(lytMyRingUnlock,(1));
        loadImage();
        setOnClick(true);
        lytMyRingUnlock.setLockAnimation(cBoxLockLayoutAnimation.isChecked(),true);
    }

    private void loadImage(){
        loadImage(lytMyRingUnlock.imgLock,C.ICON_LOCK);
        loadImage(lytMyRingUnlock.imgApps01,C.ICON_SHORTCUT_01+1);
        loadImage(lytMyRingUnlock.imgApps02,C.ICON_SHORTCUT_02+1);
        loadImage(lytMyRingUnlock.imgApps03,C.ICON_SHORTCUT_03+1);
        loadImage(lytMyRingUnlock.imgApps04,C.ICON_SHORTCUT_04+1);
        loadImage(lytMyRingUnlock.imgApps05,C.ICON_SHORTCUT_05+1);
        loadImage(lytMyRingUnlock.imgApps06,C.ICON_SHORTCUT_06+1);
        loadImage(lytMyRingUnlock.imgApps07,C.ICON_SHORTCUT_07+1);
        loadImage(lytMyRingUnlock.imgApps08,C.ICON_SHORTCUT_08+1);
    }

    private void loadImage(ImageView v,String imgName){
        try{
            String icon = C.EXT_STORAGE_DIR+imgName;
            final Bitmap bmIcon = BitmapFactory.decodeFile(icon);
            v.setImageBitmap(bmIcon);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void saveSettings(){
        try{
            editor.putBoolean("cBoxHideCenterLockIcon",cBoxHideCenterLockIcon);
            editor.putBoolean("cBoxShowShortcutIcons",cBoxShowShortcutIcons.isChecked());

            editor.putBoolean("cBoxLockLayoutAnimation",cBoxLockLayoutAnimation.isChecked());
            editor.putBoolean("cBoxIconBgOnPressEffect",cBoxIconBgOnPressEffect.isChecked());

            editor.putBoolean("cBoxLockAnimationType1",cBoxLockAnimationType1.isChecked());
            editor.putBoolean("cBoxLockAnimationType2",cBoxLockAnimationType2.isChecked());
            editor.putBoolean("cBoxLockAnimationType3",cBoxLockAnimationType3.isChecked());
            editor.putBoolean("cBoxLockAnimationType4",cBoxLockAnimationType4.isChecked());
            editor.putBoolean("cBoxLockAnimationType5",cBoxLockAnimationType5.isChecked());

            editor.putBoolean("cBoxIconBgOnPressType1",cBoxIconBgOnPressType1.isChecked());
            editor.putBoolean("cBoxIconBgOnPressType2",cBoxIconBgOnPressType2.isChecked());
            editor.putBoolean("cBoxIconBgOnPressType3",cBoxIconBgOnPressType3.isChecked());
            editor.putBoolean("cBoxIconBgOnPressType4",cBoxIconBgOnPressType4.isChecked());

            editor.putInt("intIconBgOnPressType",intIconBgOnPressType);
            editor.putString(P.STR_COLOR_ICON_BG_ON_PRESS,strColorIconBgOnPress);
            editor.commit();

            if(!shortcutData.isUnlockSet(profile)){
                resetShortcutData(DataAppsSelection.SHORTCUT_APP_01);
                lytMyRingUnlock.imgApps01.setImageResource(R.drawable.ic_lock_open_white_48dp);

                Toast.makeText(this,"No Unlock Action found in "+
                        getString(R.string.fragment_locker_profile)+
                        ", Top icon reset to Unlock Action",Toast.LENGTH_LONG).show();
            }

            saveImage(lytMyRingUnlock.imgLock,C.ICON_LOCK);
            saveImage(lytMyRingUnlock.imgApps01,C.ICON_SHORTCUT_01+1);
            saveImage(lytMyRingUnlock.imgApps02,C.ICON_SHORTCUT_02+1);
            saveImage(lytMyRingUnlock.imgApps03,C.ICON_SHORTCUT_03+1);
            saveImage(lytMyRingUnlock.imgApps04,C.ICON_SHORTCUT_04+1);
            saveImage(lytMyRingUnlock.imgApps05,C.ICON_SHORTCUT_05+1);
            saveImage(lytMyRingUnlock.imgApps06,C.ICON_SHORTCUT_06+1);
            saveImage(lytMyRingUnlock.imgApps07,C.ICON_SHORTCUT_07+1);
            saveImage(lytMyRingUnlock.imgApps08,C.ICON_SHORTCUT_08+1);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void saveImage(ImageView v,String imgName){
        try{
            final Bitmap bmIcon = ((BitmapDrawable)v.getDrawable()).getBitmap();
            final File fileIcon = new File(C.EXT_STORAGE_DIR,imgName);
            if(!fileIcon.getParentFile().exists()){
                fileIcon.getParentFile().mkdir();
            }
            final OutputStream outStream = new FileOutputStream(fileIcon);
            bmIcon.compress(Bitmap.CompressFormat.PNG,100,outStream);
            outStream.flush();
            outStream.close();
        }catch(FileNotFoundException e){
            saveErrorLogs(null,e);
        }catch(IOException e){
            saveErrorLogs(null,e);
        }catch(Exception e){
            saveErrorLogs(null,e);
            resetAllShortcut();
            saveSettings();
        }
    }

    private void setOnClick(boolean bln){
        if(bln){
            lytShowAppsImageAllTime.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    cBoxShowShortcutIcons.performClick();
                }
            });

            cBoxLockLayoutAnimation.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    lytMyRingUnlock.setLockAnimation(cBoxLockLayoutAnimation.isChecked(),true);
                }
            });
            cBoxIconBgOnPressEffect.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    setIconBgOnPress();
                }
            });

            cBoxLockAnimationType1.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    //cBoxLockAnimationType1.setChecked(false);
                    cBoxLockAnimationType2.setChecked(false);
                    cBoxLockAnimationType3.setChecked(false);
                    cBoxLockAnimationType4.setChecked(false);
                    cBoxLockAnimationType5.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_LOCK_ANIMATION_COLOR);

                    editor.putInt("intLockAnimationType",C.BLINKING_TYPE_1);
                    editor.commit();
                }
            });
            cBoxLockAnimationType2.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxLockAnimationType1.setChecked(false);
                    //cBoxLockAnimationType2.setChecked(false);
                    cBoxLockAnimationType3.setChecked(false);
                    cBoxLockAnimationType4.setChecked(false);
                    cBoxLockAnimationType5.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_LOCK_ANIMATION_COLOR);

                    editor.putInt("intLockAnimationType",C.BLINKING_TYPE_2);
                    editor.commit();
                }
            });
            cBoxLockAnimationType3.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxLockAnimationType1.setChecked(false);
                    cBoxLockAnimationType2.setChecked(false);
                    //cBoxLockAnimationType3.setChecked(false);
                    cBoxLockAnimationType4.setChecked(false);
                    cBoxLockAnimationType5.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_LOCK_ANIMATION_COLOR);

                    editor.putInt("intLockAnimationType",C.BLINKING_TYPE_3);
                    editor.commit();
                }
            });
            cBoxLockAnimationType4.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxLockAnimationType1.setChecked(false);
                    cBoxLockAnimationType2.setChecked(false);
                    cBoxLockAnimationType3.setChecked(false);
                    //cBoxLockAnimationType4.setChecked(false);
                    cBoxLockAnimationType5.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_LOCK_ANIMATION_COLOR);

                    editor.putInt("intLockAnimationType",C.BLINKING_TYPE_4);
                    editor.commit();
                }
            });
            cBoxLockAnimationType5.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxLockAnimationType1.setChecked(false);
                    cBoxLockAnimationType2.setChecked(false);
                    cBoxLockAnimationType3.setChecked(false);
                    cBoxLockAnimationType4.setChecked(false);
                    //cBoxLockAnimationType5.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_LOCK_ANIMATION_COLOR);

                    editor.putInt("intLockAnimationType",C.BLINKING_TYPE_5);
                    editor.commit();
                }
            });

            cBoxIconBgOnPressType1.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    //cBoxIconBgOnPressType1.setChecked(false);
                    cBoxIconBgOnPressType2.setChecked(false);
                    cBoxIconBgOnPressType3.setChecked(false);
                    cBoxIconBgOnPressType4.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_ICON_ON_PRESS_COLOR);

                    intIconBgOnPressType = C.BLINKING_TYPE_1;
                }
            });
            cBoxIconBgOnPressType2.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxIconBgOnPressType1.setChecked(false);
                    //cBoxIconBgOnPressType2.setChecked(false);
                    cBoxIconBgOnPressType3.setChecked(false);
                    cBoxIconBgOnPressType4.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_ICON_ON_PRESS_COLOR);

                    intIconBgOnPressType = C.BLINKING_TYPE_2;
                }
            });
            cBoxIconBgOnPressType3.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxIconBgOnPressType1.setChecked(false);
                    cBoxIconBgOnPressType2.setChecked(false);
                    //cBoxIconBgOnPressType3.setChecked(false);
                    cBoxIconBgOnPressType4.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_ICON_ON_PRESS_COLOR);

                    intIconBgOnPressType = C.BLINKING_TYPE_3;
                }
            });
            cBoxIconBgOnPressType4.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0){
                    cBoxIconBgOnPressType1.setChecked(false);
                    cBoxIconBgOnPressType2.setChecked(false);
                    cBoxIconBgOnPressType3.setChecked(false);
                    //cBoxIconBgOnPressType4.setChecked(false);

                    Intent i = new Intent(SettingsShortcuts.this,PickerColor.class);
                    startActivityForResult(i,SELECTED_ICON_ON_PRESS_COLOR);

                    intIconBgOnPressType = C.BLINKING_TYPE_4;
                }
            });

            lytMyRingUnlock.imgLock.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 0;
                    dialogShortcutAppIcon();
                }
            });
            lytMyRingUnlock.imgApps01.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 1;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_01);
                }
            });
            lytMyRingUnlock.imgApps02.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 2;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_02);
                }
            });
            lytMyRingUnlock.imgApps03.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 3;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_03);
                }
            });
            lytMyRingUnlock.imgApps04.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 4;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_04);
                }
            });
            lytMyRingUnlock.imgApps05.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 5;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_05);
                }
            });
            lytMyRingUnlock.imgApps06.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 6;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_06);
                }
            });
            lytMyRingUnlock.imgApps07.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 7;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_07);
                }
            });
            lytMyRingUnlock.imgApps08.setOnClickListener(new OnClickListener(){
                public void onClick(View v){
                    customIconNo = 8;
                    dialogShortcutApp(DataAppsSelection.SHORTCUT_APP_08);
                }
            });
        }else{
            cBoxLockLayoutAnimation.setOnClickListener(null);
            cBoxIconBgOnPressEffect.setOnClickListener(null);

            cBoxLockAnimationType1.setOnClickListener(null);
            cBoxLockAnimationType2.setOnClickListener(null);
            cBoxLockAnimationType3.setOnClickListener(null);
            cBoxLockAnimationType4.setOnClickListener(null);
            cBoxLockAnimationType5.setOnClickListener(null);

            cBoxIconBgOnPressType1.setOnClickListener(null);
            cBoxIconBgOnPressType2.setOnClickListener(null);
            cBoxIconBgOnPressType3.setOnClickListener(null);
            cBoxIconBgOnPressType4.setOnClickListener(null);

            lytMyRingUnlock.imgApps01.setOnClickListener(null);
            lytMyRingUnlock.imgApps02.setOnClickListener(null);
            lytMyRingUnlock.imgApps03.setOnClickListener(null);
            lytMyRingUnlock.imgApps04.setOnClickListener(null);
            lytMyRingUnlock.imgApps05.setOnClickListener(null);
            lytMyRingUnlock.imgApps06.setOnClickListener(null);
            lytMyRingUnlock.imgApps07.setOnClickListener(null);
            lytMyRingUnlock.imgApps08.setOnClickListener(null);
        }
    }

    private void iconAction1(){
        try{
            //final String ACTION_PICK_ICON1 = "org.adw.launcher.THEMES"; //ADW
            final String ACTION_PICK_ICON2 = "org.adw.launcher.icons.ACTION_PICK_ICON"; //ADW
            //final String ACTION_PICK_ICON3 = "com.gau.go.launcherex.theme";	//GO

            //final String CAT_PICK_ICON1 = "com.anddoes.launcher.THEME"; //APEX
            //final String CAT_PICK_ICON1 = "com.teslacoilsw.launcher.THEME"; //NOVA
            //final String CAT_PICK_ICON1 = "com.fede.launcher.THEME_ICONPACK"; //LAUNCHER PRO

            //final Intent i = new Intent(this,PickerDefaultIcons.class);
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
            saveErrorLogs(null,e);
        }
    }

    private void iconAction2(){
        try{
            final Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent,SELECTED_ICON_2);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void dialogShortcutAppIcon(){
        final String[] items = new String[]{getString(R.string.settings_shortcut_icon_app),getString(R.string.settings_shortcut_icon_gallery),getString(R.string.hide_icon)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.shortcut_app_title));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                if(item==0){
                    cBoxHideCenterLockIcon = false;
                    iconAction1();
                }else if(item==1){
                    cBoxHideCenterLockIcon = false;
                    iconAction2();
                }else if(item==2){
                    cBoxHideCenterLockIcon = true;
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    private void dialogShortcutApp(final int shortcutNo){
        final String[] items = new String[]{getString(R.string.launch_app),T.SETTINGS_SHORTCUTS,getString(R.string.set_as_unlock),getString(R.string.hide_icon),getString(R.string.settings_shortcut_icon_app),getString(R.string.settings_shortcut_icon_gallery)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.shortcut_app_title));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                if(item==0){
                    final Intent i = new Intent(SettingsShortcuts.this,ListInstalledApps.class);
                    i.putExtra(C.LIST_APPS_ID,shortcutNo);
                    i.putExtra(C.LIST_APPS_PROFILE,profile);
                    startActivityForResult(i,SELECTED_SHORTCUT);
                }else if(item==1){
                    temporaryShortcutId = shortcutNo;
                    Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                    pickIntent.putExtra(Intent.EXTRA_INTENT,new Intent(Intent.ACTION_CREATE_SHORTCUT));
                    startActivityForResult(pickIntent,SELECTED_FAVORITE_SHORTCUT_1);
                }else if(item==2){
                    setShortcutAction(shortcutNo,DataAppsSelection.ACTION_UNLOCK);
                    switch(shortcutNo){
                        case 1:
                            lytMyRingUnlock.imgApps01.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 2:
                            lytMyRingUnlock.imgApps02.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 3:
                            lytMyRingUnlock.imgApps03.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 4:
                            lytMyRingUnlock.imgApps04.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 5:
                            lytMyRingUnlock.imgApps05.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 6:
                            lytMyRingUnlock.imgApps06.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 7:
                            lytMyRingUnlock.imgApps07.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                        case 8:
                            lytMyRingUnlock.imgApps08.setImageResource(R.drawable.ic_lock_open_white_48dp);
                            break;
                    }
                }else if(item==3){
                    setShortcutAction(shortcutNo,DataAppsSelection.ACTION_SHORTCUT_HIDE);
                    switch(shortcutNo){
                        case 1:
                            lytMyRingUnlock.imgApps01.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 2:
                            lytMyRingUnlock.imgApps02.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 3:
                            lytMyRingUnlock.imgApps03.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 4:
                            lytMyRingUnlock.imgApps04.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 5:
                            lytMyRingUnlock.imgApps05.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 6:
                            lytMyRingUnlock.imgApps06.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 7:
                            lytMyRingUnlock.imgApps07.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                        case 8:
                            lytMyRingUnlock.imgApps08.setImageResource(R.drawable.ic_mode_edit_white_48dp);
                            break;
                    }
                }else if(item==4){
                    iconAction1();
                }else if(item==5){
                    iconAction2();
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    private void setShortcutAction(final int shortcutId,int action){
        shortcutData.updateShortcutInfo(shortcutId,DataAppsSelection.KEY_SHORTCUT_ACTION,action);
    }
}