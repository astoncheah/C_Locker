package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;

import com.ccs.lockscreen.R;

public class PickerDefaultIcons extends Activity{
    private View imageV01, imageV02, imageV03, imageV04, imageV05, imageV06, imageV07, imageV08, imageV09;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_icons);

        imageV01 = (View)findViewById(R.id.lyt_icon_picker_imageView01);
        imageV02 = (View)findViewById(R.id.lyt_icon_picker_imageView02);
        imageV03 = (View)findViewById(R.id.lyt_icon_picker_imageView03);
        imageV04 = (View)findViewById(R.id.lyt_icon_picker_imageView04);
        imageV05 = (View)findViewById(R.id.lyt_icon_picker_imageView05);
        imageV06 = (View)findViewById(R.id.lyt_icon_picker_imageView06);
        imageV07 = (View)findViewById(R.id.lyt_icon_picker_imageView07);
        imageV08 = (View)findViewById(R.id.lyt_icon_picker_imageView08);
        imageV09 = (View)findViewById(R.id.lyt_icon_picker_imageView09);

        imageV01.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_https_white_48dp);
            }
        });
        imageV02.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_lock_open_white_48dp);
            }
        });
        imageV03.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_flashlight_white_48dp);
            }
        });
        imageV04.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_camera_alt_white_48dp);
            }
        });
        imageV05.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_youtube_play_white_48dp);
            }
        });
        imageV06.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_earth_white_48dp);
            }
        });
        imageV07.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_location_on_white_48dp);
            }
        });
        imageV08.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_call_white_48dp);
            }
        });
        imageV09.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(R.drawable.ic_textsms_white_48dp);
            }
        });
    }

    private void setWidgetColor(int drawableId){
        final Bitmap bmIcon = ((BitmapDrawable)ContextCompat.getDrawable(getBaseContext(),drawableId)).getBitmap();
        Intent i = new Intent();
        i.putExtra("icon",getResizedBitmap(bmIcon,144,144));
        setResult(Activity.RESULT_OK,i);
        finish();
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
}
