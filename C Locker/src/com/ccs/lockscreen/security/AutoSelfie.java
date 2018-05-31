package com.ccs.lockscreen.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class AutoSelfie{
    private static Camera camera;
    public Context context;
    public MyCLocker mLocker;
    private int cameraId = 0;
    private String fileName;
    private File pictureFile;
    private PictureCallback mCall = new PictureCallback(){
        public void onPictureTaken(final byte[] data,Camera camera){
            //Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data,0,data.length);
            //display.setImageBitmap(bitmapPicture);
            //new Thread(){public void run(){
            try{
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie onPictureTaken callback received");
                saveFile(data);
                sendEmail();
                releaseCamera("AutoSelfie>PictureCallback successfully completed");
            }catch(Exception e){
                releaseCamera("AutoSelfie>PictureCallback error");
                mLocker.saveErrorLog("Exception",e);
            }
            //}}.start();
        }
    };

    public AutoSelfie(Context context){
        this.context = context;
        mLocker = new MyCLocker(context);
        createFile();
    }

    public void createFile(){
        String date = getDateFormat();
        fileName = getPhotoFileName(date);
        pictureFile = createPhotoFile(fileName);
    }

    private String getDateFormat(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_h:mm:ssa");
        return dateFormat.format(new Date());
    }

    private String getPhotoFileName(String date){
        return "SecuritySelfie_"+date+".jpg";
    }

    private File createPhotoFile(String fileName){
        return new File(getDir()+File.separator+fileName);
    }

    public static final String getDir(){
        //File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File pictureFileDir = new File(C.AUTO_SELFIE_DIR);
        if(!pictureFileDir.exists()){
            pictureFileDir.mkdirs();
        }
        return pictureFileDir.getPath();
    }

    public boolean isFrontFacingCameraSupported(){
        if(checkCameraHardware(context)){
            cameraId = findFrontFacingCamera();
            if(cameraId>=0){
                return true;
            }
        }
        return false;
    }

    private boolean checkCameraHardware(Context context){
        if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        }else{
            return false;
        }
    }

    private int findFrontFacingCamera(){
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for(int i = 0; i<numberOfCameras; i++){
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i,info);
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie>findFrontFacingCamera: "+info.facing+"/cameraId: "+i);
            if(info.facing==CameraInfo.CAMERA_FACING_FRONT){
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public boolean safeCameraOpen(){
        try{
            //releaseCamera();
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie>safeCameraOpen: "+camera+"\ncameraId: "+cameraId);
            if(camera!=null){
                return true;
            }
            camera = Camera.open(cameraId);
            return (camera!=null);
        }catch(Exception e){
            releaseCamera("AutoSelfie>safeCameraOpen error");
            mLocker.saveErrorLog("safeCameraOpen error, cameraId: "+cameraId,e);
        }
        return false;
    }

    public void releaseCamera(String log){
        if(camera!=null){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie releaseCamera: "+log);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void takePicture(){
        try{
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie>takePicture: "+camera+"\ncameraId: "+cameraId);
            SurfaceTexture dummy = new SurfaceTexture(1);
            //SurfaceView dummy1 = new SurfaceView(context);
            Camera.Parameters params = camera.getParameters();
            params.setJpegQuality(100);

            String iso = params.get("iso-values");
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie iso-values: "+iso);
            if(iso!=null && iso.contains("800")){
                params.set("iso","800");
            }

            int maxEx = (int)(params.getMaxExposureCompensation()*0.8f);
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie getMaxExposureCompensation: "+maxEx);
            params.setExposureCompensation(maxEx);

            camera.setParameters(params);
            camera.setPreviewTexture(dummy);
            //camera.setPreviewDisplay(dummy1.getHolder());
            camera.startPreview();
            camera.takePicture(null,null,mCall);
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie takePicture: done, wating for picture data callback now (SurfaceTexture)");
        }catch(Exception e){
            releaseCamera("AutoSelfie>takePicture error");
            mLocker.saveErrorLog(null,e);
        }
    }

    public void saveFile(byte[] data) throws IOException{
        //FileOutputStream fos = null;
        //FileOutputStream fos2 = null;
        try{
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();

            Bitmap bm = BitmapFactory.decodeFile(pictureFile.getPath());
            Bitmap rotatedBitmap = rotateBitmap(bm,270);
            FileOutputStream fos2 = new FileOutputStream(pictureFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos2);
            fos2.close();

            final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM,Locale.getDefault());
            final SimpleDateFormat tf = new SimpleDateFormat("h:mm:ssa");
            String dt = df.format(new Date());
            String time = tf.format(new Date());

            final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
            editor.putString("latestThiefSelfieFile",fileName);
            editor.putString("latestThiefSelfieTime"," "+dt+"\nat "+time);
            editor.putBoolean("isNewSecuritySelfieTaken",true);
            editor.commit();

            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"SecuritySelfie file saved at: "+pictureFile.getAbsolutePath());
        }finally{
            /*if (null != fos) {
                fos.close();
            }
            if (null != fos2) {
            	fos2.close();
            }//*/
        }
    }

    private Bitmap rotateBitmap(Bitmap source,float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),matrix,true);
    }

    public void sendEmail(){
        new GmailSender(context).sendEmail();
    }
} 
