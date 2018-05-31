package com.ccs.lockscreen.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.WallpaperHandler;
import com.ccs.lockscreen_pro.SettingsWidgets;

public class SettingsDisplay extends Fragment{
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private Activity context;
    private LinearLayout lytDisplayFullScreen, lytSetPortrait, lytCtrTvAnmation;
    private CheckBox cBoxDisplayFullScreen, cBoxPortrait, cBoxCtrTvAnmation;
    private RadioButton cBoxWallpaperHome, cBoxWallpaperPicture, rBtnUnlockAnimDefault, rBtnUnlockAnimZoomOut, rBtnUnlockAnimZoomIn, rBtnUnlockAnimRotation;
    private SeekBar seekBarWallpaperDimLevel;
    private ProgressDialog progressBar;
    private int setWallpaperType;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        final View view = inflater.inflate(R.layout.settings_display,container,false);

        lytDisplayFullScreen = (LinearLayout)view.findViewById(R.id.lytDisplayFullScreen);
        lytSetPortrait = (LinearLayout)view.findViewById(R.id.lytSetPortrait);
        lytCtrTvAnmation = (LinearLayout)view.findViewById(R.id.lytCtrTvAnmation);

        cBoxDisplayFullScreen = (CheckBox)view.findViewById(R.id.cBoxDisplayFullScreen);
        cBoxPortrait = (CheckBox)view.findViewById(R.id.cBoxSetPortrait);
        cBoxCtrTvAnmation = (CheckBox)view.findViewById(R.id.cBoxCtrTvAnmation);

        cBoxWallpaperHome = (RadioButton)view.findViewById(R.id.rBtn_setWallpaperHome);
        cBoxWallpaperPicture = (RadioButton)view.findViewById(R.id.rBtn_setWallpaperFromPicture);

        seekBarWallpaperDimLevel = (SeekBar)view.findViewById(R.id.seekBarWallpaperDimLevel);

        rBtnUnlockAnimDefault = (RadioButton)view.findViewById(R.id.rBtnUnlockAnimDefault);
        rBtnUnlockAnimZoomOut = (RadioButton)view.findViewById(R.id.rBtnUnlockAnimZoomOut);
        rBtnUnlockAnimZoomIn = (RadioButton)view.findViewById(R.id.rBtnUnlockAnimZoomIn);
        rBtnUnlockAnimRotation = (RadioButton)view.findViewById(R.id.rBtnUnlockAnimRotation);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        try{
            prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            onClickFunction();
            loadSettings();
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    @Override
    public void onDestroy(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void saveSettings(){
        editor.putBoolean("cBoxDisplayFullScreen",cBoxDisplayFullScreen.isChecked());
        editor.putBoolean("cBoxPortrait",cBoxPortrait.isChecked());
        editor.putBoolean("cBoxCtrTvAnmation",cBoxCtrTvAnmation.isChecked());

        editor.putBoolean("cBoxWallpaperHome",cBoxWallpaperHome.isChecked());
        editor.putBoolean("cBoxWallpaperPicture",cBoxWallpaperPicture.isChecked());

        editor.putInt("setWallpaperType",setWallpaperType);
        editor.putInt("seekBarWallpaperDimLevel",seekBarWallpaperDimLevel.getProgress());

        editor.putBoolean("rBtnUnlockAnimDefault",rBtnUnlockAnimDefault.isChecked());
        editor.putBoolean("rBtnUnlockAnimZoomOut",rBtnUnlockAnimZoomOut.isChecked());
        editor.putBoolean("rBtnUnlockAnimZoomIn",rBtnUnlockAnimZoomIn.isChecked());
        editor.putBoolean("rBtnUnlockAnimRotation",rBtnUnlockAnimRotation.isChecked());

        editor.commit();
    }

    private void onClickFunction(){
        lytDisplayFullScreen.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxDisplayFullScreen.performClick();
            }
        });
        lytSetPortrait.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxPortrait.performClick();
            }
        });
        lytCtrTvAnmation.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxCtrTvAnmation.performClick();
            }
        });
        cBoxWallpaperHome.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setWallpaperType = C.WALLPAPER_HOME;
                cBoxWallpaperPicture.setChecked(false);

                WallpaperHandler wallpaperHandler = new WallpaperHandler(context,SettingsDisplay.this);
                wallpaperHandler.deleteWallpaper(C.WALL_PAPER_01);
                wallpaperHandler.deleteWallpaper(C.WALL_PAPER_02);
                wallpaperHandler.deleteWallpaper(C.WALL_PAPER_03);
                wallpaperHandler.deleteWallpaper(C.WALL_PAPER_04);
                wallpaperHandler.deleteWallpaper(C.WALL_PAPER_05);
                wallpaperHandler.deleteWallpaper(C.WALL_PAPER_06);
            }
        });
        cBoxWallpaperPicture.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setWallpaperType = C.WALLPAPER_CUSTOM;
                cBoxWallpaperHome.setChecked(false);

                Intent i = new Intent(context,SettingsWidgets.class);
                startActivityForResult(i,1);
            }
        });
        rBtnUnlockAnimDefault.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                //rBtnUnlockAnimDefault.setChecked(false);
                rBtnUnlockAnimZoomOut.setChecked(false);
                rBtnUnlockAnimZoomIn.setChecked(false);
                rBtnUnlockAnimRotation.setChecked(false);
            }
        });
        rBtnUnlockAnimZoomOut.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                rBtnUnlockAnimDefault.setChecked(false);
                //rBtnUnlockAnimZoomOut.setChecked(false);
                rBtnUnlockAnimZoomIn.setChecked(false);
                rBtnUnlockAnimRotation.setChecked(false);
            }
        });
        rBtnUnlockAnimZoomIn.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                rBtnUnlockAnimDefault.setChecked(false);
                rBtnUnlockAnimZoomOut.setChecked(false);
                //rBtnUnlockAnimZoomIn.setChecked(false);
                rBtnUnlockAnimRotation.setChecked(false);
            }
        });
        rBtnUnlockAnimRotation.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                rBtnUnlockAnimDefault.setChecked(false);
                rBtnUnlockAnimZoomOut.setChecked(false);
                rBtnUnlockAnimZoomIn.setChecked(false);
                //rBtnUnlockAnimRotation.setChecked(false);
            }
        });
    }

    private void loadSettings(){
        cBoxDisplayFullScreen.setChecked(prefs.getBoolean("cBoxDisplayFullScreen",false));
        cBoxPortrait.setChecked(prefs.getBoolean("cBoxPortrait",false));
        cBoxCtrTvAnmation.setChecked(prefs.getBoolean("cBoxCtrTvAnmation",false));

        cBoxWallpaperHome.setChecked(prefs.getBoolean("cBoxWallpaperHome",true));
        cBoxWallpaperPicture.setChecked(prefs.getBoolean("cBoxWallpaperPicture",false));

        setWallpaperType = prefs.getInt("setWallpaperType",C.WALLPAPER_HOME);
        seekBarWallpaperDimLevel.setProgress(prefs.getInt("seekBarWallpaperDimLevel",3));

        rBtnUnlockAnimDefault.setChecked(prefs.getBoolean("rBtnUnlockAnimDefault",true));
        rBtnUnlockAnimZoomOut.setChecked(prefs.getBoolean("rBtnUnlockAnimZoomOut",false));
        rBtnUnlockAnimZoomIn.setChecked(prefs.getBoolean("rBtnUnlockAnimZoomIn",false));
        rBtnUnlockAnimRotation.setChecked(prefs.getBoolean("rBtnUnlockAnimRotation",false));
    }
}