package com.ccs.lockscreen.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.ccs.lockscreen.R;

public class AnimationLock{
    public static final float ICON_ALPHA = 0.8f;

    public final void setAnimationIconCenter(final View v){
        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v,"alpha",0,ICON_ALPHA).setDuration(600);
        final AnimatorSet animSet = new AnimatorSet();
        switch(v.getId()){
            case R.id.img_apps01:
                final ObjectAnimator mover1 = ObjectAnimator.ofFloat(v,"translationY",300,0);
                animSet.playTogether(fadeIn,mover1);
                animSet.start();
                break;
            case R.id.img_apps02:
                final ObjectAnimator animX2 = ObjectAnimator.ofFloat(v,"translationY",300,0);
                final ObjectAnimator animY2 = ObjectAnimator.ofFloat(v,"translationX",-300,0);
                animSet.playTogether(fadeIn,animX2,animY2);
                animSet.start();
                break;
            case R.id.img_apps03:
                final ObjectAnimator mover3 = ObjectAnimator.ofFloat(v,"translationX",-300,0);
                animSet.playTogether(fadeIn,mover3);
                animSet.start();
                break;
            case R.id.img_apps04:
                final ObjectAnimator animX4 = ObjectAnimator.ofFloat(v,"translationY",-300,0);
                final ObjectAnimator animY4 = ObjectAnimator.ofFloat(v,"translationX",-300,0);
                animSet.playTogether(fadeIn,animX4,animY4);
                animSet.start();
                break;
            case R.id.img_apps05:
                final ObjectAnimator mover5 = ObjectAnimator.ofFloat(v,"translationY",-300,0);
                animSet.playTogether(fadeIn,mover5);
                animSet.start();
                break;
            case R.id.img_apps06:
                final ObjectAnimator animX6 = ObjectAnimator.ofFloat(v,"translationY",-300,0);
                final ObjectAnimator animY6 = ObjectAnimator.ofFloat(v,"translationX",300,0);
                animSet.playTogether(fadeIn,animX6,animY6);
                animSet.start();
                break;
            case R.id.img_apps07:
                final ObjectAnimator mover7 = ObjectAnimator.ofFloat(v,"translationX",300,0);
                animSet.playTogether(fadeIn,mover7);
                animSet.start();
                break;
            case R.id.img_apps08:
                final ObjectAnimator animX8 = ObjectAnimator.ofFloat(v,"translationY",300,0);
                final ObjectAnimator animY8 = ObjectAnimator.ofFloat(v,"translationX",300,0);
                animSet.playTogether(fadeIn,animX8,animY8);
                animSet.start();
                break;
        }
    }

    public final void setAnimationIconBottom1(final View v){
        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v,"alpha",0,ICON_ALPHA).setDuration(600);
        final AnimatorSet animSet = new AnimatorSet();
        switch(v.getId()){
            case R.id.img_apps01:
                final ObjectAnimator mover1 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,mover1);
                animSet.start();
                break;
            case R.id.img_apps02:
                final ObjectAnimator animX2 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,animX2);
                animSet.start();
                break;
            case R.id.img_apps03:
                final ObjectAnimator mover3 = ObjectAnimator.ofFloat(v,"translationX",-200,0);
                animSet.playTogether(fadeIn,mover3);
                animSet.start();
                break;
            case R.id.img_apps04:
                final ObjectAnimator animY4 = ObjectAnimator.ofFloat(v,"translationX",-100,0);
                animSet.playTogether(fadeIn,animY4);
                animSet.start();
                break;
            case R.id.img_apps05:
                final ObjectAnimator mover5 = ObjectAnimator.ofFloat(v,"translationY",200,0);
                animSet.playTogether(fadeIn,mover5);
                animSet.start();
                break;
            case R.id.img_apps06:
                final ObjectAnimator animY6 = ObjectAnimator.ofFloat(v,"translationX",100,0);
                animSet.playTogether(fadeIn,animY6);
                animSet.start();
                break;
            case R.id.img_apps07:
                final ObjectAnimator mover7 = ObjectAnimator.ofFloat(v,"translationX",200,0);
                animSet.playTogether(fadeIn,mover7);
                animSet.start();
                break;
            case R.id.img_apps08:
                final ObjectAnimator animX8 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,animX8);
                animSet.start();
                break;
        }
    }

    public final void setAnimationIconBottom2(final View v){
        final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v,"alpha",0,ICON_ALPHA).setDuration(600);
        final AnimatorSet animSet = new AnimatorSet();
        switch(v.getId()){
            case R.id.img_apps01:
                final ObjectAnimator mover1 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,mover1);
                animSet.start();
                break;
            case R.id.img_apps02:
                final ObjectAnimator animX2 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,animX2);
                animSet.start();
                break;
            case R.id.img_apps03:
                final ObjectAnimator mover3 = ObjectAnimator.ofFloat(v,"translationX",-100,0);
                animSet.playTogether(fadeIn,mover3);
                animSet.start();
                break;
            case R.id.img_apps04:
                final ObjectAnimator animY4 = ObjectAnimator.ofFloat(v,"translationX",-100,0);
                animSet.playTogether(fadeIn,animY4);
                animSet.start();
                break;
            case R.id.img_apps05:
                final ObjectAnimator mover5 = ObjectAnimator.ofFloat(v,"translationX",100,0);
                animSet.playTogether(fadeIn,mover5);
                animSet.start();
                break;
            case R.id.img_apps06:
                final ObjectAnimator animY6 = ObjectAnimator.ofFloat(v,"translationX",100,0);
                animSet.playTogether(fadeIn,animY6);
                animSet.start();
                break;
            case R.id.img_apps07:
                final ObjectAnimator mover7 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,mover7);
                animSet.start();
                break;
            case R.id.img_apps08:
                final ObjectAnimator animX8 = ObjectAnimator.ofFloat(v,"translationY",100,0);
                animSet.playTogether(fadeIn,animX8);
                animSet.start();
                break;
        }
    }
}
