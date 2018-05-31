package com.ccs.lockscreen.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.ccs.lockscreen.myclocker.C;

public class MySideShortcutLayout extends ScrollView{
    public LinearLayout lytMain;
    public ImageView imgLock, imgLockBg, imgApps01, imgApps02, imgApps03, imgApps04, imgApps05, imgApps06, imgApps07, imgApps08;
    private Context context;
    private int size;

    public MySideShortcutLayout(Context context){
        super(context);
        this.context = context;
        size = C.dpToPx(context,C.ICON_HEIGHT);

        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
        //pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        this.setLayoutParams(pr);
        //setBackgroundColor(getResources().getColor(R.color.windowBackgroundColor));
        this.addView(lytMain());
        //setLayoutTransition(new LayoutTransition());

        imgApps01 = imgNoticeIcon();
        imgApps02 = imgNoticeIcon();
        imgApps03 = imgNoticeIcon();
        imgApps04 = imgNoticeIcon();
        imgApps05 = imgNoticeIcon();
        imgApps06 = imgNoticeIcon();
        imgApps07 = imgNoticeIcon();
        imgApps08 = imgNoticeIcon();

        lytMain.addView(imgApps01);
        lytMain.addView(imgApps02);
        lytMain.addView(imgApps03);
        lytMain.addView(imgApps04);
        lytMain.addView(imgApps05);
        lytMain.addView(imgApps06);
        lytMain.addView(imgApps07);
        lytMain.addView(imgApps08);
    }

    private LinearLayout lytMain(){
        lytMain = new LinearLayout(context);
        lytMain.setOrientation(LinearLayout.VERTICAL);
        lytMain.setGravity(Gravity.CENTER);
        lytMain.setVisibility(View.GONE);
        //lytMain.setBackgroundColor(getResources().getColor(R.color.windowBackgroundColor));
        lytMain.setBackgroundColor(0);
        //lytMain.setLayoutTransition(new LayoutTransition());
        return lytMain;
    }

    private ImageView imgNoticeIcon(){
        int pad = C.dpToPx(context,6);
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(size,size);
        ImageView img = new ImageView(context);
        img.setLayoutParams(pr);
        img.setPadding(pad,pad,pad,pad);
        img.setAlpha(AnimationLock.ICON_ALPHA);
        return img;
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        imgApps01.setImageDrawable(null);
        imgApps02.setImageDrawable(null);
        imgApps03.setImageDrawable(null);
        imgApps04.setImageDrawable(null);
        imgApps05.setImageDrawable(null);
        imgApps06.setImageDrawable(null);
        imgApps07.setImageDrawable(null);
        imgApps08.setImageDrawable(null);
    }

    public final void setLayoutTop(){
        try{
            int shownIconCount = 0;
            View v;
            for(int i = 0; i<lytMain.getChildCount(); i++){
                v = lytMain.getChildAt(i);
                if(v.getContentDescription()!=null){
                    shownIconCount++;
                }
            }
            int top = 0;
            int h = C.getRealDisplaySize(context).y;
            int gap = h-(size*shownIconCount);
            if(gap>0){
                top = (gap/2);
            }
            final FrameLayout.LayoutParams pr = new FrameLayout.LayoutParams(size,LayoutParams.MATCH_PARENT);
            lytMain.setLayoutParams(pr);
            lytMain.setPadding(0,top,0,top);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public final void hideView(){
        if(lytMain.isShown()){
            setLayoutTransition(new LayoutTransition());
            lytMain.setVisibility(View.GONE);
        }
    }

    public final void showView(boolean left,int screenWidth){
        setLayoutTransition(null);
        int start, end;
        if(left){
            start = 0-size;
            end = 0;
        }else{
            start = screenWidth;
            end = screenWidth-size;
        }
        final ObjectAnimator outX1 = ObjectAnimator.ofFloat(this,"x",start,end).setDuration(300);
        final AnimatorSet animSet = new AnimatorSet();
        animSet.play(outX1);
        animSet.addListener(new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator arg0){
                lytMain.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator arg0){
                requestLayout();//reset the layout, else it may not be accurate
            }

            @Override
            public void onAnimationCancel(Animator arg0){
            }

            @Override
            public void onAnimationRepeat(Animator arg0){
            }
        });
        animSet.start();
    }
}