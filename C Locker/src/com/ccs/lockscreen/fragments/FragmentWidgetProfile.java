package com.ccs.lockscreen.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.MyWidgetHelper;
import com.ccs.lockscreen.appwidget.MyWidgetHelper.WidgetSetupListener;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.WallpaperHandler;
import com.ccs.lockscreen.utils.WallpaperHandler.WallpaperHandlerListener;
import com.ccs.lockscreen_pro.ServiceMain;

import java.io.File;

public final class FragmentWidgetProfile extends Fragment implements WidgetSetupListener,
        WallpaperHandlerListener{
    private static final String PAGE_NO = "pageNo";
    private Context context;
    private MyCLocker mLocker;
    private RelativeLayout lytWidgets;
    private MyWidgetHelper myWidgetHelper;
    private WallpaperHandler wallpaperHandler;
    private int intWidgetProfile = C.PROFILE_DEFAULT;
    private int STARTING_RAW_X, STARTING_RAW_Y;
    private int moveGap;
    private boolean hasMoved;
    //
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(final Context context,final Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(context.getPackageName()+C.UPDATE_CONFIGURE)){
                    refreshLayout(false);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };

    public static FragmentWidgetProfile newInstance(int noPage){
        FragmentWidgetProfile f = new FragmentWidgetProfile();
        Bundle b = new Bundle();
        b.putInt(PAGE_NO,noPage);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case WallpaperHandler.TAKE_PICTURE_GALLERY:
                    if(resultCode==Activity.RESULT_OK){
                        try{
                            Uri selectedImage = data.getData();
                            new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>onActivityResult>selectedImage: "+selectedImage);
                            wallpaperHandler.dialogCropImage(selectedImage);
                        }catch(Exception e){
                            Toast.makeText(context,getString(R.string.wallpaper_not_supported),Toast.LENGTH_LONG).show();
                            handleWallpaperError();
                            new MyCLocker(context).saveErrorLog(null,e);
                        }
                    }else{
                        Toast.makeText(context,getString(R.string.wallpaper_cancelled),Toast.LENGTH_LONG).show();
                        handleWallpaperError();
                    }
                    break;
                case WallpaperHandler.CROP_PICTURE:
                    if(resultCode==Activity.RESULT_OK){
                        handleWallpaperSuccess();
                    }else{
                        handleWallpaperError();
                    }
                    break;
                default:
                    myWidgetHelper.handleActivityResult(requestCode,resultCode,data);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        lytWidgets = new RelativeLayout(context);

        final int disPlayH = C.getRealDisplaySize(context).y;
        final int statusbarH = C.getStatusBarHeight(context)*2;//*2 = viewpager size
        final int naviKeysH = C.getNaviKeyHeight(context);

        final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE).edit();
        editor.putInt(ServiceMain.USABLE_DISPLAY_HEIGHT,disPlayH-statusbarH-naviKeysH);
        editor.commit();
        return lytWidgets;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        if(b!=null && b.containsKey(PAGE_NO)){
            intWidgetProfile = b.getInt(PAGE_NO);
            mLocker = new MyCLocker(context);
            myWidgetHelper = new MyWidgetHelper(context,this,this);
            wallpaperHandler = new WallpaperHandler(context,this);

            try{
                ViewPager.LayoutParams pr = new ViewPager.LayoutParams();
                pr.width = -1;
                pr.height = myWidgetHelper.getLayoutHeight();
                //lytWidgets.setLayoutParams(new ViewPager.LayoutParams(-1,myWidgetHelper.getLayoutHeight()));
                lytWidgets.setLayoutParams(pr);
                loadReceiver(true);
                refreshLayout(false);
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    }

    @Override
    public void onDestroy(){
        try{
            if(myWidgetHelper!=null){
                myWidgetHelper.closeAlertDialog();
                myWidgetHelper.closeData();
            }
            loadReceiver(false);
        }catch(Exception e){
            //mLocker.saveErrorLog(null,e);// no need this to prevent FC
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private void loadReceiver(final boolean enable){
        try{
            if(enable){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(context.getPackageName()+C.UPDATE_CONFIGURE);
                context.registerReceiver(mReceiver,intentFilter);
            }else{
                context.unregisterReceiver(mReceiver);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }

    private void refreshLayout(boolean isDeleteAll){
        if(isDeleteAll){
            wallpaperHandler.deleteWallpaper(WallpaperHandler.getWallpaperNo(intWidgetProfile));
        }
        lytWidgets.removeAllViews();
        lytWidgets.addView(addBackgroundView());
        myWidgetHelper.loadWidgets(intWidgetProfile,lytWidgets,null,isDeleteAll);
    }

    private ImageView addBackgroundView(){
        final ImageView img = new ImageView(context);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(-1,-1);
        pr.addRule(RelativeLayout.CENTER_IN_PARENT);

        img.setLayoutParams(pr);
        img.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent m){
                switch(m.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        hasMoved = false;
                        moveGap = C.dpToPx(context,5);

                        STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                        STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int gapX = (int)(STARTING_RAW_X-m.getRawX());
                        int gapY = (int)(STARTING_RAW_Y-m.getRawY());

                        if(gapX>moveGap || gapX<(-moveGap)){
                            hasMoved = true;
                        }
                        if(gapY>moveGap || gapY<(-moveGap)){
                            hasMoved = true;
                        }
                        break;
                }
                return false;
            }
        });
        img.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                if(!hasMoved){
                    myWidgetHelper.emptyLayoutLongPressAction(intWidgetProfile,lytWidgets,wallpaperHandler);
                }
                return true;
            }
        });
        img.setAlpha(0.5f);
        img.setScaleType(ScaleType.FIT_XY);
        setWallpaper(img);
        return img;
    }

    private void setWallpaper(ImageView img){
        try{
            String file = WallpaperHandler.getWallpaperNo(intWidgetProfile);
            if(isFileAvailable(file)){
                final Bitmap bm = BitmapFactory.decodeFile(C.EXT_STORAGE_DIR+file);
                img.setImageBitmap(bm);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }catch(OutOfMemoryError e){
            mLocker.saveErrorLog(e.toString(),null);
        }
    }

    private boolean isFileAvailable(String dir){
        final File file = new File(C.EXT_STORAGE_DIR+dir);
        if(file.getAbsoluteFile().exists()){
            return true;
        }
        return false;
    }

    @Override
    public void handleWallpaperSuccess(){
        refreshLayout(false);

        SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
        editor.putBoolean("cBoxWallpaperHome",false);
        editor.putBoolean("cBoxWallpaperPicture",true);
        editor.putBoolean("cBoxWallpaperSettingSuccess",true);
        editor.putInt("setWallpaperType",C.WALLPAPER_CUSTOM);
        editor.commit();
    }

    @Override
    public void handleWallpaperError(){
        Toast.makeText(context,getString(R.string.wallpaper_crop_error),Toast.LENGTH_LONG).show();

        SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
        editor.putBoolean("cBoxWallpaperSettingSuccess",false);
        editor.commit();
    }

    @Override
    public void onWidgetSetupFinished(int result,int widgetId){
        if(result==MyWidgetHelper.WIDGET_SETUP_SUCCESSFUL){
            refreshLayout(false);
        }
    }

    @Override
    public void onWidgetDeleteAll(){
        refreshLayout(true);
    }
}