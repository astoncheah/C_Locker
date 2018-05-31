package com.ccs.lockscreen.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;

public class MyRingUnlock extends RelativeLayout{
    public LinearLayout lytLock, lytShortcut01, lytShortcut02, lytShortcut03, lytShortcut04, lytShortcut05, lytShortcut06, lytShortcut07, lytShortcut08;
    public ImageView imgLock, imgLockBg, imgApps01, imgApps02, imgApps03, imgApps04, imgApps05, imgApps06, imgApps07, imgApps08;

    private Context context;
    private SharedPreferences prefs;
    private int intLockStyle, intLockAnimationType;
    private boolean cBoxShowShortcutIcons, cBoxLockLayoutAnimation;
    private String strColorLockAnimation;
    private Runnable runLockAnimation = new Runnable(){
        @Override
        public void run(){
            try{
                if(intLockAnimationType<=C.BLINKING_TYPE_3){
                    imgLockBg.setImageDrawable(setRoundColor(intLockAnimationType,strColorLockAnimation,250));
                    final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imgLockBg,"alpha",0.1f,1).setDuration(1000);
                    final ObjectAnimator y = ObjectAnimator.ofFloat(imgLockBg,"scaleY",0.3f,1).setDuration(1000);
                    final ObjectAnimator x = ObjectAnimator.ofFloat(imgLockBg,"scaleX",0.3f,1).setDuration(1000);
                    final AnimatorSet animSet = new AnimatorSet();
                    animSet.playTogether(fadeIn,y,x);
                    animSet.addListener(new Animator.AnimatorListener(){
                        @Override
                        public void onAnimationStart(Animator arg0){
                        }                        @Override
                        public void onAnimationCancel(Animator arg0){
                        }

                        @Override
                        public void onAnimationEnd(Animator arg0){
                            imgLockBg.setImageDrawable(null);
                        }

                        @Override
                        public void onAnimationRepeat(Animator arg0){
                        }


                    });
                    animSet.start();
                }else if(intLockAnimationType==C.BLINKING_TYPE_4){
                    final ImageView img1 = new ImageView(context);
                    final ImageView img2 = new ImageView(context);
                    addView(img1,imgLockBg.getLayoutParams());
                    addView(img2,imgLockBg.getLayoutParams());

                    imgLockBg.setImageDrawable(setRoundColor(intLockAnimationType,strColorLockAnimation,250));
                    img1.setImageDrawable(setRoundColor(intLockAnimationType,strColorLockAnimation,250));
                    img2.setImageDrawable(setRoundColor(intLockAnimationType,strColorLockAnimation,250));

                    final ObjectAnimator rotate1 = ObjectAnimator.ofFloat(imgLockBg,"rotation",0,360).setDuration(1000);
                    final ObjectAnimator rotate2 = ObjectAnimator.ofFloat(img1,"rotation",360,0).setDuration(1000);
                    final ObjectAnimator rotate3 = ObjectAnimator.ofFloat(img2,"rotation",0,360).setDuration(1000);

                    final ObjectAnimator fadeIn1 = ObjectAnimator.ofFloat(imgLockBg,"alpha",0.1f,1).setDuration(1000);
                    final ObjectAnimator fadeIn2 = ObjectAnimator.ofFloat(img1,"alpha",0.1f,1).setDuration(1000);
                    final ObjectAnimator fadeIn3 = ObjectAnimator.ofFloat(img2,"alpha",0.1f,1).setDuration(1000);

                    final ObjectAnimator y1 = ObjectAnimator.ofFloat(imgLockBg,"scaleY",0.1f,0.8f).setDuration(1000);
                    final ObjectAnimator x1 = ObjectAnimator.ofFloat(imgLockBg,"scaleX",0.1f,0.8f).setDuration(1000);

                    final ObjectAnimator y2 = ObjectAnimator.ofFloat(img1,"scaleY",0.2f,0.9f).setDuration(1000);
                    final ObjectAnimator x2 = ObjectAnimator.ofFloat(img1,"scaleX",0.2f,0.9f).setDuration(1000);

                    final ObjectAnimator y3 = ObjectAnimator.ofFloat(img2,"scaleY",0.3f,1).setDuration(1000);
                    final ObjectAnimator x3 = ObjectAnimator.ofFloat(img2,"scaleX",0.3f,1).setDuration(1000);
                    final AnimatorSet animSet = new AnimatorSet();
                    animSet.playTogether(rotate1,rotate2,rotate3,fadeIn1,fadeIn2,fadeIn3,y1,x1,y2,x2,y3,x3);
                    animSet.addListener(new Animator.AnimatorListener(){
                        @Override
                        public void onAnimationCancel(Animator arg0){
                        }

                        @Override
                        public void onAnimationEnd(Animator arg0){
                            imgLockBg.setImageDrawable(null);
                            removeView(img1);
                            removeView(img2);
                        }

                        @Override
                        public void onAnimationRepeat(Animator arg0){
                        }

                        @Override
                        public void onAnimationStart(Animator arg0){
                        }
                    });
                    animSet.start();
                }else{
                    imgLockBg.setRotation(-90);
                    imgLockBg.setImageDrawable(setRoundColor(intLockAnimationType,strColorLockAnimation,250));
                    final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imgLockBg,"alpha",0,0.5f,0.8f,1).setDuration(1000);
                    final ObjectAnimator r = ObjectAnimator.ofFloat(imgLockBg,"rotation",270).setDuration(2000);
                    final AnimatorSet animSet = new AnimatorSet();
                    animSet.playTogether(fadeIn,r);
                    animSet.addListener(new Animator.AnimatorListener(){
                        @Override
                        public void onAnimationCancel(Animator arg0){
                        }

                        @Override
                        public void onAnimationEnd(Animator arg0){
                            imgLockBg.setImageDrawable(null);
                        }

                        @Override
                        public void onAnimationRepeat(Animator arg0){
                        }

                        @Override
                        public void onAnimationStart(Animator arg0){
                        }
                    });
                    animSet.start();
                }
                postDelayed(runLockAnimation,3000);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    public MyRingUnlock(Context context,int intLockStyle){
        this(context,-1,intLockStyle);
    }

    public MyRingUnlock(Context context,int intDisplayHeight,int intLockStyle){
        super(context);
        this.context = context;
        this.intLockStyle = intLockStyle;

        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        loadSettings();

        int lytId;
        if(intLockStyle==1){
            lytId = R.layout.locker_unlock_center;
        }else if(intLockStyle==2){
            lytId = R.layout.locker_unlock_bottom_1;
        }else{
            lytId = R.layout.locker_unlock_bottom_2;
        }

        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(lytId,this,true);

        lytLock = (LinearLayout)findViewById(R.id.lyt_lock);
        lytShortcut01 = (LinearLayout)findViewById(R.id.lyt_apps01);
        lytShortcut02 = (LinearLayout)findViewById(R.id.lyt_apps02);
        lytShortcut03 = (LinearLayout)findViewById(R.id.lyt_apps03);
        lytShortcut04 = (LinearLayout)findViewById(R.id.lyt_apps04);
        lytShortcut05 = (LinearLayout)findViewById(R.id.lyt_apps05);
        lytShortcut06 = (LinearLayout)findViewById(R.id.lyt_apps06);
        lytShortcut07 = (LinearLayout)findViewById(R.id.lyt_apps07);
        lytShortcut08 = (LinearLayout)findViewById(R.id.lyt_apps08);

        imgLock = (ImageView)findViewById(R.id.img_lock);
        imgLockBg = (ImageView)findViewById(R.id.img_lockBackground);
        imgApps01 = (ImageView)findViewById(R.id.img_apps01);
        imgApps02 = (ImageView)findViewById(R.id.img_apps02);
        imgApps03 = (ImageView)findViewById(R.id.img_apps03);
        imgApps04 = (ImageView)findViewById(R.id.img_apps04);
        imgApps05 = (ImageView)findViewById(R.id.img_apps05);
        imgApps06 = (ImageView)findViewById(R.id.img_apps06);
        imgApps07 = (ImageView)findViewById(R.id.img_apps07);
        imgApps08 = (ImageView)findViewById(R.id.img_apps08);

        imgLock.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps01.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps02.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps03.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps04.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps05.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps06.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps07.setAlpha(AnimationLock.ICON_ALPHA);
        imgApps08.setAlpha(AnimationLock.ICON_ALPHA);

        if(cBoxShowShortcutIcons || intDisplayHeight==-1){
            imgApps01.setVisibility(View.VISIBLE);
            imgApps02.setVisibility(View.VISIBLE);
            imgApps03.setVisibility(View.VISIBLE);
            imgApps04.setVisibility(View.VISIBLE);
            imgApps05.setVisibility(View.VISIBLE);
            imgApps06.setVisibility(View.VISIBLE);
            imgApps07.setVisibility(View.VISIBLE);
            imgApps08.setVisibility(View.VISIBLE);
        }

        if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
            if(intDisplayHeight!=-1){
                float lytTop;
                //final int addTop = C.getStatusBarHeight(context);
                final int addTop = 25;//statusbar = 25dp
                if(intLockStyle==1){
                    lytTop = (float)(intDisplayHeight-C.dpToPx(context,340-addTop));//(displayHeight*0.45);// overall height 285/290dp
                }else if(intLockStyle==2){
                    lytTop = (float)(intDisplayHeight-C.dpToPx(context,270-addTop));//(displayHeight*0.55);// overall height 295/200dp
                }else{
                    lytTop = (float)(intDisplayHeight-C.dpToPx(context,255-addTop));//(displayHeight*0.6); // overall height 255/210dp
                }
                setY(lytTop);
            }
        }else{
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            setLayoutParams(params);
        }
        //this.setVisibility(View.INVISIBLE);
    }

    public final void loadSettings(){
        cBoxShowShortcutIcons = prefs.getBoolean("cBoxShowShortcutIcons",false);
        cBoxLockLayoutAnimation = prefs.getBoolean("cBoxLockLayoutAnimation",false);
        intLockAnimationType = prefs.getInt("intLockAnimationType",C.BLINKING_TYPE_4);
        strColorLockAnimation = prefs.getString(P.STR_COLOR_LOCK_ANIMATION,"ffffff");
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        setLockAnimation(true);
    }

    @Override
    protected void onDetachedFromWindow(){
        setLockAnimation(false);
        imgLock.setImageDrawable(null);
        imgLockBg.setImageDrawable(null);
        imgApps01.setImageDrawable(null);
        imgApps02.setImageDrawable(null);
        imgApps03.setImageDrawable(null);
        imgApps04.setImageDrawable(null);
        imgApps05.setImageDrawable(null);
        imgApps06.setImageDrawable(null);
        imgApps07.setImageDrawable(null);
        imgApps08.setImageDrawable(null);
        super.onDetachedFromWindow();
    }

    public final void setLockAnimation(final boolean startTimer){
        setLockAnimation(startTimer,cBoxLockLayoutAnimation);
    }

    public final void setLockAnimation(final boolean startTimer,boolean cBoxLockLayoutAnimation){
        try{
            if(startTimer){
                removeCallbacks(runLockAnimation);
                if(cBoxLockLayoutAnimation){
                    postDelayed(runLockAnimation,3000);
                }
            }else{
                removeCallbacks(runLockAnimation);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView,int visibility){
        super.onVisibilityChanged(changedView,visibility);
    }

    public final void setFlashLightIcon(int val){
        if(!cBoxShowShortcutIcons){
            imgApps02.setVisibility(val);
        }
    }

    public final void setShortcutImageVisibility(final boolean show,boolean enableFlashLight){
        if(!cBoxShowShortcutIcons){
            if(show){
                imgApps01.setVisibility(View.VISIBLE);
                imgApps02.setVisibility(View.VISIBLE);
                imgApps03.setVisibility(View.VISIBLE);
                imgApps04.setVisibility(View.VISIBLE);
                imgApps05.setVisibility(View.VISIBLE);
                imgApps06.setVisibility(View.VISIBLE);
                imgApps07.setVisibility(View.VISIBLE);
                imgApps08.setVisibility(View.VISIBLE);

                AnimationLock ani = new AnimationLock();
                if(intLockStyle==C.CENTER_RING_UNLOCK){
                    ani.setAnimationIconCenter(imgApps01);
                    ani.setAnimationIconCenter(imgApps02);
                    ani.setAnimationIconCenter(imgApps03);
                    ani.setAnimationIconCenter(imgApps04);
                    ani.setAnimationIconCenter(imgApps05);
                    ani.setAnimationIconCenter(imgApps06);
                    ani.setAnimationIconCenter(imgApps07);
                    ani.setAnimationIconCenter(imgApps08);
                }else if(intLockStyle==C.BOTTOM_TRIANGLE_UNLOCK){
                    ani.setAnimationIconBottom1(imgApps01);
                    ani.setAnimationIconBottom1(imgApps02);
                    ani.setAnimationIconBottom1(imgApps03);
                    ani.setAnimationIconBottom1(imgApps04);
                    ani.setAnimationIconBottom1(imgApps05);
                    ani.setAnimationIconBottom1(imgApps06);
                    ani.setAnimationIconBottom1(imgApps07);
                    ani.setAnimationIconBottom1(imgApps08);
                }else if(intLockStyle==C.BOTTOM_RING_UNLOCK){
                    ani.setAnimationIconBottom2(imgApps01);
                    ani.setAnimationIconBottom2(imgApps02);
                    ani.setAnimationIconBottom2(imgApps03);
                    ani.setAnimationIconBottom2(imgApps04);
                    ani.setAnimationIconBottom2(imgApps05);
                    ani.setAnimationIconBottom2(imgApps06);
                    ani.setAnimationIconBottom2(imgApps07);
                    ani.setAnimationIconBottom2(imgApps08);
                }
            }else{
                imgApps01.setVisibility(View.INVISIBLE);
                if(!enableFlashLight){
                    imgApps02.setVisibility(View.INVISIBLE);
                }
                imgApps03.setVisibility(View.INVISIBLE);
                imgApps04.setVisibility(View.INVISIBLE);
                imgApps05.setVisibility(View.INVISIBLE);
                imgApps06.setVisibility(View.INVISIBLE);
                imgApps07.setVisibility(View.INVISIBLE);
                imgApps08.setVisibility(View.INVISIBLE);
            }
        }
    }

    private Drawable setRoundColor(int type,String strColor,int size){
        try{
            GradientDrawable gd = null;
            if(type==C.BLINKING_TYPE_1){
                gd = new GradientDrawable();
                gd.setShape(GradientDrawable.OVAL);
                gd.setStroke(2,Color.parseColor("#"+strColor));
            }else if(type==C.BLINKING_TYPE_2){
                int color01 = Color.parseColor("#"+strColor);
                int color02 = Color.parseColor("#88"+strColor);
                int color03 = Color.parseColor("#55"+strColor);
                int color04 = Color.parseColor("#22"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,0});
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                gd.setGradientRadius(size);
            }else if(type==C.BLINKING_TYPE_3){
                int color01 = Color.parseColor("#00"+strColor);
                int color02 = Color.parseColor("#11"+strColor);
                int color03 = Color.parseColor("#33"+strColor);
                int color04 = Color.parseColor("#"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color01,color02,color03,color04,0});
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                gd.setGradientRadius(size);
            }else if(type==C.BLINKING_TYPE_4){
                int color01 = Color.parseColor("#"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{0,0});//transparent
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
                gd.setGradientRadius(size);
                gd.setStroke(8,color01,20,40);
            }else{
                int color01 = Color.parseColor("#"+strColor);
                int color02 = Color.parseColor("#22"+strColor);

                gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,color02,color02,color02,color01});
                gd.setShape(GradientDrawable.OVAL);
                gd.setGradientType(GradientDrawable.SWEEP_GRADIENT);
                gd.setGradientRadius(size);
            }
            return gd;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}