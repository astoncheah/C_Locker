package com.ccs.lockscreen.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SaveAnalogClockImage extends AsyncTask<String,Integer,Boolean>{
    private Context context;
    private ProgressDialog progressBar;
    private String progressMsg = "";
    private int imgType, intClockSize;

    public SaveAnalogClockImage(Context context,int imgType){//1 = clock, 2 = bottom bar tab
        super();
        this.context = context;
        this.imgType = imgType;

        int intDisplayHeight = C.getRealDisplaySize(context).y;
        intClockSize = (int)(intDisplayHeight*0.15);
    }

    @Override
    protected Boolean doInBackground(String... str){
        try{
            String colorDial = str[0];
            File file = null;
            OutputStream outStream = null;
            if(imgType==1){
                createClock(colorDial,file,outStream);
            }else if(imgType==2){
                createTab(colorDial,file,outStream);
            }else{
                createClock(colorDial,file,outStream);
                createTab(colorDial,file,outStream);
            }
            return true;
        }catch(FileNotFoundException e){
            new MyCLocker(context).saveErrorLog(null,e);
        }catch(IOException e){
            new MyCLocker(context).saveErrorLog(null,e);
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return false;
    }

    @Override
    protected void onPreExecute(){
        progressBar = new ProgressDialog(context);
        progressBar.setCancelable(false);
        progressBar.setMessage(progressMsg+"...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBar.setProgress(0);
        progressBar.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values){
        progressBar.setMessage(progressMsg);
        progressBar.setProgress(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(){
        try{
            if(progressBar!=null && progressBar.isShowing()){
                progressBar.dismiss();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onCancelled();
    }

    private void createClock(String colorDial,File file,OutputStream outStream) throws IOException{
        progressMsg = "Creating.."+C.CLOCK_DIAL+"..(5)";
        final Bitmap bmpClockDial = setColor(R.drawable.a_analog_clock_dial_big_blk,colorDial,0,intClockSize,intClockSize);
        progressMsg = "Creating.."+C.CLOCK_DIAL+"..(4)";
        final Bitmap bmpClockDial_30 = setColor(R.drawable.a_analog_clock_dial_small_blk,colorDial,30,intClockSize,intClockSize);
        progressMsg = "Creating.."+C.CLOCK_DIAL+"..(3)";
        final Bitmap bmpClockDial_60 = setColor(R.drawable.a_analog_clock_dial_small_blk,colorDial,60,intClockSize,intClockSize);

        //Log.e("info",bmpClockDial.getHeight()+"/"+bmpClockDial_30.getHeight());

        final float fLeft = (bmpClockDial_30.getWidth()-bmpClockDial.getWidth())/2;
        final float fTop = (bmpClockDial_30.getHeight()-bmpClockDial.getHeight())/2;

        Canvas canvas = new Canvas(bmpClockDial);
        canvas.drawBitmap(bmpClockDial_30,-fLeft,-fTop,null);
        canvas.drawBitmap(bmpClockDial_60,-fLeft,-fTop,null);

        file = new File(C.EXT_STORAGE_DIR,C.CLOCK_DIAL);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdir();
        }
        outStream = new FileOutputStream(file);
        bmpClockDial.compress(Bitmap.CompressFormat.PNG,100,outStream);
        outStream.flush();

        progressMsg = "Creating.."+C.CLOCK_HOUR+"..(2)";
        final Bitmap bmpClockHrs = setColor(R.drawable.a_analog_clock_hour_blk,colorDial,0,intClockSize,intClockSize);
        file = new File(C.EXT_STORAGE_DIR,C.CLOCK_HOUR);
        outStream = new FileOutputStream(file);
        bmpClockHrs.compress(Bitmap.CompressFormat.PNG,100,outStream);
        outStream.flush();

        progressMsg = "Creating.."+C.CLOCK_MIN+"..(1)";
        final Bitmap bmpClockMin = setColor(R.drawable.a_analog_clock_minute_blk,colorDial,0,intClockSize,intClockSize);
        file = new File(C.EXT_STORAGE_DIR,C.CLOCK_MIN);
        outStream = new FileOutputStream(file);
        bmpClockMin.compress(Bitmap.CompressFormat.PNG,100,outStream);
        outStream.flush();

        bmpClockDial.recycle();
        bmpClockDial_30.recycle();
        bmpClockDial_60.recycle();
        bmpClockHrs.recycle();
        bmpClockMin.recycle();

        outStream.close();
    }

    private void createTab(String colorDial,File file,OutputStream outStream) throws IOException{
        progressMsg = "Creating.."+C.TAB_SELECTED+"..";
        final Bitmap bmpTapSelectedBg = setColor(R.drawable.tab_selected_white,colorDial,0,6,24);
        file = new File(C.EXT_STORAGE_DIR,C.TAB_SELECTED);
        outStream = new FileOutputStream(file);
        bmpTapSelectedBg.compress(Bitmap.CompressFormat.PNG,100,outStream);
        outStream.flush();

        bmpTapSelectedBg.recycle();

        outStream.close();
    }

    private Bitmap setColor(int id,String color,float rotation,int mWidth,int mHeight){
        final Bitmap oldBmp = ((BitmapDrawable)ContextCompat.getDrawable(context,id)).getBitmap();
        final Bitmap newBmp = oldBmp.copy(Bitmap.Config.ARGB_8888,true);
        //Log.e("info",oldBmp.getHeight()+"/"+newBmp.getHeight());

        final int width = newBmp.getWidth();
        final int height = newBmp.getHeight();
        progressBar.setMax(width);

        //Canvas canvas = new Canvas(newBmp);
        //Paint p = new Paint();
        //p.setColor(Color.parseColor("#"+color));

        //int[]allpixels = new int[width * height];
        //Log.e("setColor","started"); 
        for(int x = 0; x<width; x++){
            for(int y = 0; y<height; y++){
                if(newBmp.getPixel(x,y)!=Color.parseColor("#00000000")){
                    //canvas.drawPoint(x,y,p);
                    //Log.e("color",x+"/"+y);
                    newBmp.setPixel(x,y,Color.parseColor("#"+color));
                }
            }
            //if(allpixels[x] != Color.parseColor("#00000000")){
            //     allpixels[x] = Color.parseColor("#"+color);
            //}
            this.publishProgress(x);
        }
        //Log.e("setColor","ended");
        //newBmp.setPixels(allpixels,0,width,0,0,width,height);

        final float scaledX = (float)mWidth/width;//default 450
        final float scaledY = (float)mHeight/height;

        final Matrix matrix = new Matrix();
        matrix.setScale(scaledX,scaledY);
        matrix.postRotate(rotation);

        return Bitmap.createBitmap(newBmp,0,0,width,height,matrix,true);
    }
}