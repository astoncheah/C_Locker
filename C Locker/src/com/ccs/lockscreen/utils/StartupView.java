package com.ccs.lockscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;

public class StartupView extends LinearLayout{
    public StartupView(Context context){
        super(context);

        int size = C.dpToPx(context,50);
        int imgPad = C.dpToPx(context,2);
        int lytPad = C.dpToPx(context,32);

        ImageView img = new ImageView(context);
        if(context.getPackageName().equals(C.PKG_NAME_PRO)){
            img.setImageResource(R.drawable.c_lockscreen_icon_small_pro);
        }else{
            img.setImageResource(R.drawable.c_lockscreen_icon_small);
        }

        img.setLayoutParams(new LinearLayout.LayoutParams(size,size));
        img.setPadding(imgPad,imgPad,imgPad,imgPad);

        TextView txt = new TextView(context);
        txt.setText(R.string.introduction);
        txt.setGravity(Gravity.CENTER);

        LinearLayout lyt1 = new LinearLayout(context);
        lyt1.setGravity(Gravity.CENTER);
        lyt1.setOrientation(VERTICAL);
        lyt1.addView(img);
        lyt1.addView(txt);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        this.setPadding(lytPad,lytPad,lytPad,lytPad);
        this.setBackgroundColor(Color.BLACK);
        this.setGravity(Gravity.CENTER);
        this.setOrientation(VERTICAL);
        this.addView(lyt1);
        this.addView(progressBar);
        this.setOnTouchListener(new OnTouchListener(){
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v,MotionEvent event){
                //set true to block all touch events
                return true;
            }
        });
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();

    }

    @Override
    protected void onDetachedFromWindow(){
        try{
            this.removeAllViews();
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDetachedFromWindow();
    }
}
