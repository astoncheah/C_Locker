package com.ccs.lockscreen.security;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.WindowManager;

import com.ccs.lockscreen.myclocker.C;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AutoSelfie2 extends AutoSelfie{
    //Conversion from screen rotation to JPEG orientation.
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static{
        ORIENTATIONS.append(Surface.ROTATION_0,90);
        ORIENTATIONS.append(Surface.ROTATION_90,0);
        ORIENTATIONS.append(Surface.ROTATION_180,270);
        ORIENTATIONS.append(Surface.ROTATION_270,180);
    }

    private CameraDevice camera2;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private String cameraId;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder captureBuilder;
    private Handler backgroundHandler;
    private int attempts = 0;
    private boolean isImageSaved = false,isCameraProcessing = false;

    //#1 step = get camera
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback(){
        @Override
        public void onOpened(@NonNull CameraDevice camera){
            //Log.e("mStateCallback", "onOpened: "+camera);
            mCameraOpenCloseLock.release();
            camera2 = camera;
            try{
                setCameraBuilder();
            }catch(Exception e){
                releaseCamera("mStateCallback>onOpened: error");
                mLocker.saveErrorLog("Exception",e);
            }
        }
        @Override
        public void onDisconnected(@NonNull CameraDevice camera){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>mStateCallback>onDisconnected");
            releaseCamera(camera,"mStateCallback>onDisconnected");
        }
        @Override
        public void onError(@NonNull CameraDevice camera,int error){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>mStateCallback>onError: "+error);
            releaseCamera(camera,"mStateCallback>onError");
        }

    };
    //#2 step = take picture
    private CameraCaptureSession.StateCallback captureStateCallback = new CameraCaptureSession.StateCallback(){
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session){
            if(camera2==null){
                return;
            }
            mSession = session;
            takePicture();
        }
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session){
            releaseCamera("onConfigureFailed");
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>captureStateCallback>onConfigureFailed");
        }
    };
    //#3 step = take picture result
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback(){
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,@NonNull CaptureRequest request,
            @NonNull TotalCaptureResult result){
            super.onCaptureCompleted(session,request,result);
            mSession = session;
            try{
                checkState(result);
            }catch(Exception e){
                releaseCamera("onCaptureCompleted: error");
                mLocker.saveErrorLog(null,e);
            }
        }
        private void checkState(CaptureResult result) throws CameraAccessException{
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if(aeState==null){// try take again
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>captureCallback>aeState == null");
            }else{
                int aeVal = aeState;
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>captureCallback>aeVal: "+aeVal);
                if(aeVal==CameraMetadata.CONTROL_AE_STATE_CONVERGED){
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>captureCallback>sendEmail1>attempts: "+attempts);
                    if(isImageSaved){
                        sendEmail();
                    }
                    mSession.stopRepeating();
                    releaseCamera("onCaptureCompleted 1");
                    return;
                }
            }

            if(attempts<5){//try five times and use the latest result
                attempts++;
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>captureCallback>retry>attempts: "+attempts);

                if(backgroundHandler!=null){
                    backgroundHandler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            takePicture();
                        }
                    },500);//<500 will get FC "java.lang.IllegalArgumentException: Surface had no valid native Surface"
                }else{
                    mSession.stopRepeating();
                    releaseCamera("backgroundHandler = null");
                }
            }else{
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>captureCallback>sendEmail>attempts: "+attempts);
                if(isImageSaved){
                    sendEmail();
                }
                mSession.stopRepeating();
                releaseCamera("onCaptureCompleted 2");
            }
        }
    };
    //#4 get camera image
    private ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener(){
        @Override
        public void onImageAvailable(ImageReader reader){
            Image image = null;
            try{
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                saveFile(bytes);
                isImageSaved = true;
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>readerListener: ImageSaved");
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }finally{
                if(image!=null){
                    image.close();
                }
            }
        }
    };

    public AutoSelfie2(Context context){
        super(context);
    }
    public boolean isFrontFacingCameraSupported(){
        if(checkCameraHardware(context)){
            cameraId = findFrontFacingCamera();
            //Log.e("cameraId","/"+cameraId);
            if(cameraId!=null){
                return true;
            }
        }
        return false;
    }
    private boolean checkCameraHardware(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
    private String findFrontFacingCamera(){
        try{
            CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
            for(final String cameraId : manager.getCameraIdList()){
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation==CameraCharacteristics.LENS_FACING_FRONT){//LENS_FACING_BACK,//LENS_FACING_FRONT
                    return cameraId;
                }
            }
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
        return null;
    }
    public boolean safeCameraOpen(){
        try{
            releaseCamera("safeCameraOpen");

            isCameraProcessing = true;
            CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
            if(!mCameraOpenCloseLock.tryAcquire(2500,TimeUnit.MILLISECONDS)){
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(cameraId,mStateCallback,null);
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        return false;
    }
    boolean isCameraProcessing(){
        return isCameraProcessing;
    }
    //
    @TargetApi(Build.VERSION_CODES.M)
    public void takePicture(){
        try{
            if(mSession!=null){
                mSession.capture(captureBuilder.build(),captureCallback,backgroundHandler);
                //mSession.setRepeatingRequest(captureBuilder.build(), captureCallback, backgroudHandler);
            }
        }catch(Exception e){
            releaseCamera("takePicture: error");
            mLocker.saveErrorLog(null,e);
        }
    }
    public void releaseCamera(String log){
        releaseCamera(camera2,log);
    }
    private void releaseCamera(CameraDevice camera,String log){
        if(mCameraOpenCloseLock!=null){
            mCameraOpenCloseLock.release();
        }
        if(camera!=null){
            camera.close();
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>releaseCamera2: "+log);
        }
        isCameraProcessing = false;
        attempts = 0;
    }
    private void setCameraBuilder() throws CameraAccessException{
        if(camera2==null){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>setCameraBuilder>camera2 is null, return");
            return;
        }

        CameraManager manager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(camera2.getId());

        //List<CaptureRequest.Key<?>> key = characteristics.getAvailableCaptureRequestKeys();
        //for(CaptureRequest.Key<?> item:key){
        //Log.e("CaptureRequest.Key","/"+item.getName());
        //}

        //List<Key<?>> key2 = characteristics.getKeys();
        //for(Key<?> item:key2){
        //Log.e("CaptureRequest.key2","/"+item.getName());
        //}

        Size[] jpegSizes = null;
        if(characteristics!=null){
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
        }
        int width = 640;
        int height = 480;
        if(jpegSizes!=null && jpegSizes.length>0){
            width = jpegSizes[0].getWidth();
            height = jpegSizes[0].getHeight();
        }
        if(height>1100){//more than 1080x1920
            width = 1920;
            height = 1080;
        }
        ImageReader reader = ImageReader.newInstance(width,height,ImageFormat.JPEG,1);
        List<Surface> outputSurfaces = new ArrayList<>(2);
        outputSurfaces.add(reader.getSurface());
        //outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

        //camera settings
        captureBuilder = camera2.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(reader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);
        //captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);//manually control AE

        //CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED = 0
        //CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL = 1
        //CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY = 2

        int lev = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>HARDWARE_LEVEL: "+lev);

        // Orientation
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        captureBuilder.set(CaptureRequest.JPEG_ORIENTATION,ORIENTATIONS.get(rotation));

        HandlerThread mHandlerThread = new HandlerThread("CameraPicture");
        mHandlerThread.start();

        backgroundHandler = new Handler(mHandlerThread.getLooper());
        reader.setOnImageAvailableListener(readerListener,backgroundHandler);
        camera2.createCaptureSession(outputSurfaces,captureStateCallback,backgroundHandler);

        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"AutoSelfie2>createCaptureSession");
    }
}
