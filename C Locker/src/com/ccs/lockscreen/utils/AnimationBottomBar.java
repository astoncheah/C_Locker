package com.ccs.lockscreen.utils;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class AnimationBottomBar extends Animation{
    private View v;
    private int currentHeight, targetHeight;
    private boolean aniUp;

    public AnimationBottomBar(View v,int currentHeight,int targetHeight,boolean aniUp){
        this.v = v;
        this.currentHeight = currentHeight;
        this.targetHeight = targetHeight;
        this.aniUp = aniUp;
    }

    @Override
    protected void applyTransformation(float interpolatedTime,Transformation t){
        int newHeight;
        if(aniUp){
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
