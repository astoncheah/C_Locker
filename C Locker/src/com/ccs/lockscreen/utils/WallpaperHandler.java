package com.ccs.lockscreen.utils;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WallpaperHandler{
    public static final int TAKE_PICTURE_GALLERY = 998, CROP_PICTURE = 999;
    public static final String WALLPAPER_ERROR = "wallpaper_error.pg";
    private Context context;
    private Fragment fragment;
    private AlertDialog.Builder builder;
    private int cropNumber;
    private String wallpaperProfile = "";

    public WallpaperHandler(Context context,Fragment fragment){
        this.context = context;
        this.fragment = fragment;
    }

    public static final String getWallpaperNo(int profile){
        switch(profile){
            case C.PROFILE_DEFAULT:
                return C.WALL_PAPER_01;
            case C.PROFILE_MUSIC:
                return C.WALL_PAPER_02;
            case C.PROFILE_LOCATION:
                return C.WALL_PAPER_03;
            case C.PROFILE_TIME:
                return C.WALL_PAPER_04;
            case C.PROFILE_WIFI:
                return C.WALL_PAPER_05;
            case C.PROFILE_BLUETOOTH:
                return C.WALL_PAPER_06;
        }
        return "";
    }

    public final void pickPictures(String noWallpaper){
        wallpaperProfile = noWallpaper;

        final Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent,TAKE_PICTURE_GALLERY);
    }

    public final void deleteWallpaper(String dir){
        final File file = new File(C.EXT_STORAGE_DIR+dir);
        if(file.getAbsoluteFile().exists()){
            file.delete();
        }
    }

    public final void dialogCropImage(final Uri selectedImage){
        String[] items;
        if(C.isS8()){
            //S8 & S8+ screen = 1440 x 2960/1080 x 2220
            items = new String[]{
                "Portrait (720x1480)",
                "Portrait (1080x2220)",
                "Landscape (1480x720)",
                "Landscape (2220x1080)",
                context.getString(R.string.imageType_3)};
        }else{
            items = new String[]{
                context.getString(R.string.imageType_1),
                "Portrait (1080x1920)",
                context.getString(R.string.imageType_2),
                "Landscape (1920x1080)",
                context.getString(R.string.imageType_3)};
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.select_dialog_item,items);
        builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.selectImageType));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                if(C.isS8()){
                    if(item==0){
                        cropImage(selectedImage,720,1480,false);//Portrait
                    }else if(item==1){
                        cropImage(selectedImage,1080,2220,false);//Portrait
                    }else if(item==2){
                        cropImage(selectedImage,1480,720,false);//Landscape
                    }else if(item==3){
                        cropImage(selectedImage,2220,1080,false);//Landscape
                    }else if(item==4){
                        cropImage(selectedImage,900,900,true);//free size
                    }
                }else{
                    if(item==0){
                        cropImage(selectedImage,720,1280,false);//Portrait
                    }else if(item==1){
                        cropImage(selectedImage,1080,1920,false);//Portrait
                    }else if(item==2){
                        cropImage(selectedImage,1280,720,false);//Landscape
                    }else if(item==3){
                        cropImage(selectedImage,1920,1080,false);//Landscape
                    }else if(item==4){
                        cropImage(selectedImage,900,900,true);//free size
                    }
                }
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void cropImage(final Uri selectedImage,int imageW,int imageH,boolean bln){
        try{
            final Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setType("image/*");
            final List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,0);

            cropNumber = list.size();
            new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage>cropNumber: "+cropNumber);
            if(cropNumber==0){
                Toast.makeText(context,R.string.wallpaper_not_supported,Toast.LENGTH_SHORT).show();
                new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage: "+context.getString(R.string.wallpaper_not_supported));
                return;
            }else{
                intent.setData(selectedImage);
                if(!bln){
                    intent.putExtra("outputX",imageW);
                    intent.putExtra("outputY",imageH);
                    intent.putExtra("aspectX",imageW);
                    intent.putExtra("aspectY",imageH);
                }else{
                    intent.putExtra("outputX",imageW);
                    intent.putExtra("outputY",imageH);
                }
                //intent.putExtra("crop", true);
                //intent.putExtra("scale", true);
                //intent.putExtra("return-data", true);
                new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage>imageFile: "+C.EXT_STORAGE_DIR+"/"+wallpaperProfile);
                File imageFile = new File(C.EXT_STORAGE_DIR,wallpaperProfile);
                if(!imageFile.getParentFile().exists()){
                    imageFile.getParentFile().mkdir();
                }
                Uri imageFileUri = Uri.fromFile(imageFile);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,imageFileUri);

                if(cropNumber==1){
                    Intent i = new Intent(intent);
                    ResolveInfo res = list.get(0);
                    i.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
                    fragment.startActivityForResult(i,CROP_PICTURE);
                    new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage>CROP_PICTURE: "+
                            res.activityInfo.packageName+"/"+res.activityInfo.name);
                }else{
                    ComponentName cn = getRecommendedCrop(list,"com.sec.android.gallery3d.app.CropImage");
                    if(cn!=null){
                        Intent i = new Intent(intent);
                        i.setComponent(cn);
                        fragment.startActivityForResult(i,CROP_PICTURE);
                        new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage>getRecommendedCrop: "+cn);
                        return;
                    }
                    cn = getRecommendedCrop(list,"com.google.android.apps.photos.photoeditor.intents.EditActivity");
                    if(cn!=null){
                        Intent i = new Intent(intent);
                        i.setComponent(cn);
                        fragment.startActivityForResult(i,CROP_PICTURE);
                        new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage>getRecommendedCrop: "+cn);
                        return;
                    }
                    final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
                    for(ResolveInfo res : list){
                        final CropOption co = new CropOption();
                        co.title = context.getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                        co.icon = context.getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                        co.appIntent = new Intent(intent);
                        co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName,res.activityInfo.name));
                        cropOptions.add(co);

                        new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SettingsDisplay>cropImage>CropOption: "+
                                res.activityInfo.packageName+"/"+res.activityInfo.name);
                    }
                    CropOptionAdapter adapter = new CropOptionAdapter(context,cropOptions);
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose Crop App");
                    builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog,int item){
                            fragment.startActivityForResult(cropOptions.get(item).appIntent,CROP_PICTURE);
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
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        }catch(Exception e){
            Toast.makeText(context,R.string.wallpaper_crop_error,Toast.LENGTH_LONG).show();
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    private ComponentName getRecommendedCrop(List<ResolveInfo> list,String name){
        for(ResolveInfo res : list){
            //com.sec.android.gallery3d/com.sec.android.gallery3d.app.CropImage
            //com.google.android.apps.photos/com.google.android.apps.photos.photoeditor.intents.EditActivity
            if(res.activityInfo.name.equals(name)){
                return new ComponentName(res.activityInfo.packageName,res.activityInfo.name);
            }
        }
        return null;
    }

    public interface WallpaperHandlerListener{
        void handleWallpaperSuccess();

        void handleWallpaperError();
    }
}