package com.ccs.lockscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.myclocker.C;

public class MySimpleUnlock{
    public static final int MAIN_HEIGHT = 150;
    private static final int IMG_SIZE = 80;
    public LinearLayout lytCall, lytUnlock, lytCam;
    private ImageView imgCall, imgUnlock, imgCam;
    private int STARTING_RAW_X, STARTING_RAW_Y, MOVE_GAP;
    private int iconCall = R.drawable.ic_call_grey_48dp;
    private int iconUnlock = R.drawable.ic_call_grey_48dp;
    private int iconCam = R.drawable.ic_call_grey_48dp;
    private int iconSize, iconMargin;
    private boolean hasMoved;
    private MySimpleUnlockCallBack callback;

    public MySimpleUnlock(Context context,RelativeLayout lytMainActivity,MySimpleUnlockCallBack callback){
        //this.context = context;
        this.callback = callback;
        iconSize = C.dpToPx(context,MAIN_HEIGHT);
        iconMargin = C.dpToPx(context,MAIN_HEIGHT/4);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean("isSimpleWhiteIcons",true)){
            iconCall = R.drawable.ic_call_white_48dp;
            iconUnlock = R.drawable.ic_https_white_48dp;
            iconCam = R.drawable.ic_camera_alt_white_48dp;
        }

        imgCall = img(context,iconCall);
        imgUnlock = img(context,iconUnlock);
        imgCam = img(context,iconCam);

        if(C.isScreenPortrait(context)){
            lytCall = lyt(context,RelativeLayout.ALIGN_PARENT_LEFT);
            lytUnlock = lyt(context,RelativeLayout.CENTER_HORIZONTAL);
            lytCam = lyt(context,RelativeLayout.ALIGN_PARENT_RIGHT);
        }else{
            lytCall = lyt(context,RelativeLayout.ALIGN_PARENT_TOP);
            lytUnlock = lyt(context,RelativeLayout.CENTER_VERTICAL);
            lytCam = lyt(context,RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        lytCall.addView(imgCall);
        lytUnlock.addView(imgUnlock);
        lytCam.addView(imgCam);

        if(lytCall.getParent()==null){
            lytMainActivity.addView(lytCall);
        }
        if(lytUnlock.getParent()==null){
            lytMainActivity.addView(lytUnlock);
        }
        if(lytCam.getParent()==null){
            lytMainActivity.addView(lytCam);
        }
    }

    private ImageView img(final Context context,int res){
        final int size = C.dpToPx(context,IMG_SIZE);
        final int pad = C.dpToPx(context,26);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        final ImageView v = new ImageView(context);
        v.setImageResource(res);
        v.setPadding(pad,pad,pad,pad);
        v.setLayoutParams(pr);
        v.setAlpha(0.8f);
        //v.setBackgroundColor(Color.YELLOW);
        C.setBackgroundRipple(context,v);
        v.setOnTouchListener(new OnTouchListener(){
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v,MotionEvent m){
                switch(m.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        STARTING_RAW_X = (int)m.getRawX();//DISPLAY LEFT
                        STARTING_RAW_Y = (int)m.getRawY();//DISPLAY TOP
                        MOVE_GAP = C.dpToPx(context,(int)(MAIN_HEIGHT*0.5));
                        hasMoved = false;
                        callback.callLockerAction(MotionEvent.ACTION_DOWN);

                        if(v==imgCall){
                            imgCall.setImageResource(R.drawable.ic_call_black_48dp);
                            lytCall.setBackground(ContextCompat.getDrawable(context,R.drawable.shape_round_white_large));
                        }else if(v==imgUnlock){
                            imgUnlock.setImageResource(R.drawable.ic_https_black_48dp);
                            lytUnlock.setBackground(ContextCompat.getDrawable(context,R.drawable.shape_round_white_large));
                        }else if(v==imgCam){
                            imgCam.setImageResource(R.drawable.ic_camera_alt_black_48dp);
                            lytCam.setBackground(ContextCompat.getDrawable(context,R.drawable.shape_round_white_large));
                        }
                    case MotionEvent.ACTION_MOVE:
                        int gapX = (int)(STARTING_RAW_X-m.getRawX());
                        int gapY = (int)(STARTING_RAW_Y-m.getRawY());
                        if(gapX>MOVE_GAP || gapX<(-MOVE_GAP)){
                            if(!hasMoved){
                                hasMoved = true;
                                if(v==imgCall){
                                    callback.callLockerAction(DataAppsSelection.TRIGGER_CALL);
                                }else if(v==imgUnlock){
                                    callback.callLockerAction(-1);
                                }else if(v==imgCam){
                                    callback.callLockerAction(DataAppsSelection.TRIGGER_CAM);
                                }
                            }
                        }
                        if(gapY>MOVE_GAP || gapY<(-MOVE_GAP)){
                            if(!hasMoved){
                                hasMoved = true;
                                if(v==imgCall){
                                    callback.callLockerAction(DataAppsSelection.TRIGGER_CALL);
                                }else if(v==imgUnlock){
                                    callback.callLockerAction(-1);
                                }else if(v==imgCam){
                                    callback.callLockerAction(DataAppsSelection.TRIGGER_CAM);
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(v==imgCall){
                            imgCall.setImageResource(iconCall);
                        }else if(v==imgUnlock){
                            imgUnlock.setImageResource(iconUnlock);
                        }else if(v==imgCam){
                            imgCam.setImageResource(iconCam);
                        }
                        lytCall.setBackground(null);
                        lytUnlock.setBackground(null);
                        lytCam.setBackground(null);
                        break;
                }
                return true;
            }
        });
        return v;
    }

    private LinearLayout lyt(final Context context,int position){
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(iconSize,iconSize);
        if(C.isScreenPortrait(context)){
            if(position==RelativeLayout.ALIGN_PARENT_LEFT){
                pr.setMargins(-iconMargin,0,0,-iconMargin);
                //pr.setMargins(-mar, 0, 0, 0);
            }else if(position==RelativeLayout.ALIGN_PARENT_RIGHT){
                pr.setMargins(0,0,-iconMargin,-iconMargin);
                //pr.setMargins(0, 0, -mar, 0);
            }else{
                pr.setMargins(0,0,0,-iconMargin);
                //pr.setMargins(0, 0, 0, 0);
            }
            pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            pr.addRule(position);
        }else{
            pr.setMargins(0,0,-iconMargin,0);
            pr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            pr.addRule(position);
        }

        final LinearLayout v = new LinearLayout(context);
        v.setLayoutParams(pr);
        v.setBackground(ContextCompat.getDrawable(context,R.drawable.shape_ring_selector_null));
        //v.setBackgroundColor(Color.BLUE);
        v.setGravity(Gravity.CENTER);

        C.setBackgroundRipple(context,v);
        return v;
    }

    public void resetImages(){
        if(imgUnlock!=null){
            imgCall.setImageResource(iconCall);
            imgUnlock.setImageResource(iconUnlock);
            imgCam.setImageResource(iconCam);
            lytCall.setBackground(null);
            lytUnlock.setBackground(null);
            lytCam.setBackground(null);
        }
    }

    public interface MySimpleUnlockCallBack{
        void callLockerAction(int actionType);
    }
}