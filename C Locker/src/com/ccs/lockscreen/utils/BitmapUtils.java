package com.ccs.lockscreen.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.ccs.lockscreen.myclocker.C;

public class BitmapUtils{
    private Context context;

    public BitmapUtils(Context context){
        this.context = context;
    }

    public final Bitmap getRoundedCornerBitmap(Bitmap bitmap,int size){
        try{
            final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Config.ARGB_8888);
            final Canvas canvas = new Canvas(output);
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
            final RectF rectF = new RectF(rect);
            final float roundPx = C.dpToPx(context,size);//default 12
            paint.setAntiAlias(true);
            canvas.drawARGB(0,0,0,0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF,roundPx,roundPx,paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap,rect,rect,paint);
            return output;
        }catch(OutOfMemoryError e){
            e.printStackTrace();
        }
        return bitmap;
    }
}
