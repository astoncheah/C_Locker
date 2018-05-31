package com.ccs.lockscreen.utils;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class AnimationCalendarList extends Animation{
    private View v;
    private int currentHeight, targetHeight;
    private boolean aniOpen;

    public AnimationCalendarList(View v,int targetHeight,boolean aniOpen){
        this.v = v;
        if(aniOpen){
            this.currentHeight = 0;
            this.targetHeight = targetHeight;
        }else{
            this.currentHeight = targetHeight;
            this.targetHeight = 0;
        }
        this.aniOpen = aniOpen;
    }

    @Override
    protected void applyTransformation(float interpolatedTime,Transformation t){
        int newHeight;
        if(aniOpen){
            int gap = targetHeight-currentHeight;
            newHeight = (int)(currentHeight+(gap*interpolatedTime));
        }else{
            newHeight = (int)(currentHeight-(currentHeight*interpolatedTime));
        }
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,newHeight));
        //v.getLayoutParams().height = newHeight;
        //v.requestLayout();
        //Log.e("Animation","applyTransformation: "+newHeight+"/"+targetHeight+"/"+interpolatedTime);
    }
}
