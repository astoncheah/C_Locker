package com.ccs.lockscreen.security;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.security.LockPatternView.Cell;

import java.util.List;

public class SecurityUnlockView extends RelativeLayout implements LockPinView.OnPinListener,
    LockPatternView.OnPatternListener{
    private static final int ANI_TIMER = 250;
    private static final int TEXT_MIN_SIZE = 15;
    //=====
    private static boolean instance = false;
    private Context context;
    private MyCLocker mLocker;
    private AutoSelfie autoSelfie;
    private AutoSelfie2 as2;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private OnSecurityUnlockListener callback;
    private LinearLayout mainView;
    private LinearLayout lytOwnerMsg, lytSecurityType;
    private View vPassWord01, vPassWord02, vPassWord03, vPassWord04, vPassWord05, vPassWord06, vPassWord07, vPassWord08;
    private EditText eTxtOwnerMsg;
    private RadioButton cBoxPinDigit4, cBoxPinDigit6, cBoxPinDigit8;
    private TextView txtOwnerMsg, txtOwnerMsgDesc, txtPassWordDesc, txtNetWorkProvider, btnForgotPin, btnEmergencyCall;
    private boolean isSettingMode, relaunchLocker;
    private Typeface mTypeface;
    private int securityType, intTextColor, intTextStyle, seekBarTextSizePersonalMsg;
    private String strTextStyle;

    private BroadcastReceiver mainReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            final String action = intent.getAction();
            try{
                if(action.equals(context.getPackageName()+C.FINISH_PIN)){
                    finish(false,false);
                }
            }catch(Exception e){
                mLocker.saveErrorLog(null,e);
            }
        }
    };
    public SecurityUnlockView(final Context context,boolean isSettingMode,OnSecurityUnlockListener callback,int securityType){
        super(context);
        this.context = context;
        this.isSettingMode = isSettingMode;
        this.relaunchLocker = false;
        this.callback = callback;
        this.securityType = securityType;
        mLocker = new MyCLocker(context);
        loadViews();
        rBtnFunction();
        loadSettings();
        //setLayoutTransition(new LayoutTransition());
    }
    public SecurityUnlockView(final Context context,int securityType,boolean relaunchLocker){
        super(context);
        this.context = context;
        this.isSettingMode = false;
        this.relaunchLocker = relaunchLocker;
        this.callback = null;
        this.securityType = securityType;
        mLocker = new MyCLocker(context);
        loadViews();
        rBtnFunction();
        loadSettings();
        //setLayoutTransition(new LayoutTransition());
    }
    public static boolean isPINRunning(){
        return instance;
    }
    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        instance = true;
        loadReceiver(true);
        if(isSettingMode){
            mainView.setVisibility(View.VISIBLE);
            return;
        }

        ViewTreeObserver layoutObserver = getViewTreeObserver();
        layoutObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout(){
                ViewTreeObserver obs = getViewTreeObserver();
                obs.removeOnGlobalLayoutListener(this);

                int intLockStyle = prefs.getInt("intLockStyle",C.SIMPLE_UNLOCK);//1 = center, 2&3 = bottom

                if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                    int direction;
                    if(intLockStyle==C.FINGERPRINT_UNLOCK){
                        direction = Gravity.LEFT;//left - right
                    }else if(intLockStyle==C.GESTURE_UNLOCK){
                        int intGestureUnlockDirection = prefs.getInt("intGestureUnlockDirection",DataAppsSelection.GESTURE_UP);
                        switch(intGestureUnlockDirection){
                            default://up
                                direction = Gravity.BOTTOM;//bottom - up
                                break;
                            case DataAppsSelection.GESTURE_DOWN:
                                direction = Gravity.TOP;//up - bottom
                                break;
                            case DataAppsSelection.GESTURE_LEFT:
                                direction = Gravity.RIGHT;//right - left
                                break;
                            case DataAppsSelection.GESTURE_RIGHT:
                                direction = Gravity.LEFT;//left - right
                                break;
                        }
                    }else{
                        direction = Gravity.BOTTOM;//bottom - up
                    }
                    Slide slide = new Slide();
                    slide.setSlideEdge(direction);
                    slide.addListener(new Transition.TransitionListener(){
                        @Override
                        public void onTransitionStart(Transition transition){
                        }
                        @Override
                        public void onTransitionEnd(Transition transition){
                            if(relaunchLocker){
                                context.sendBroadcast(new Intent(context.getPackageName()+C.LAUNCH_LOCKER));
                            }
                        }
                        @Override
                        public void onTransitionCancel(Transition transition){
                        }
                        @Override
                        public void onTransitionPause(Transition transition){
                        }
                        @Override
                        public void onTransitionResume(Transition transition){
                        }
                    });

                    TransitionManager.beginDelayedTransition(mainView, slide);
                    mainView.setVisibility(View.VISIBLE);
                }else{
                    ObjectAnimator xIn;
                    if(intLockStyle==C.FINGERPRINT_UNLOCK){
                        xIn = ObjectAnimator.ofFloat(mainView,"x",-getWidth(),0).setDuration(ANI_TIMER);//left - right
                    }else if(intLockStyle==C.GESTURE_UNLOCK){
                        int intGestureUnlockDirection = prefs.getInt("intGestureUnlockDirection",DataAppsSelection.GESTURE_UP);
                        switch(intGestureUnlockDirection){
                            default://up
                                xIn = ObjectAnimator.ofFloat(mainView,"y",getHeight(),0).setDuration(ANI_TIMER);//bottom - up
                                break;
                            case DataAppsSelection.GESTURE_DOWN:
                                xIn = ObjectAnimator.ofFloat(mainView,"y",-getHeight(),0).setDuration(ANI_TIMER);//up - bottom
                                break;
                            case DataAppsSelection.GESTURE_LEFT:
                                xIn = ObjectAnimator.ofFloat(mainView,"x",getWidth(),0).setDuration(ANI_TIMER);//right - left
                                break;
                            case DataAppsSelection.GESTURE_RIGHT:
                                xIn = ObjectAnimator.ofFloat(mainView,"x",-getWidth(),0).setDuration(ANI_TIMER);//left - right
                                break;
                        }
                    }else{
                        xIn = ObjectAnimator.ofFloat(mainView,"y",getHeight(),0).setDuration(ANI_TIMER);//bottom - up
                    }
                    final AnimatorSet animSet = new AnimatorSet();
                    animSet.play(xIn);
                    animSet.addListener(new Animator.AnimatorListener(){
                        @Override
                        public void onAnimationStart(Animator arg0){
                            mainView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animator arg0){
                            if(relaunchLocker){
                                context.sendBroadcast(new Intent(context.getPackageName()+C.LAUNCH_LOCKER));
                            }
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
        });
    }
    @Override
    protected void onDetachedFromWindow(){
        instance = false;
        loadReceiver(false);
        if(isSettingMode){
            saveSettings();
        }
        if(autoSelfie!=null){
            autoSelfie.releaseCamera("SecurityUnlockView>onDetachedFromWindow");
        }
        super.onDetachedFromWindow();
    }
    private void saveSettings(){
        editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
        editor.putString("txtOwnerMsg",eTxtOwnerMsg.getText().toString());

        editor.putBoolean("cBoxPinDigit4",cBoxPinDigit4.isChecked());
        editor.putBoolean("cBoxPinDigit6",cBoxPinDigit6.isChecked());
        editor.putBoolean("cBoxPinDigit8",cBoxPinDigit8.isChecked());

        editor.commit();
    }
    private void loadReceiver(final boolean on){
        try{
            if(on){
                final IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(context.getPackageName()+C.FINISH_PIN);
                context.registerReceiver(mainReceiver,intentFilter);
            }else{
                context.unregisterReceiver(mainReceiver);
            }
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
    }
    @Override
    public void onSettingFinish(final int result){
        if(callback!=null){
            callback.onSettingResult(result,securityType);
        }
    }
    @Override
    public void onFinish(boolean unlock,boolean isEmergencyCall){
        finish(unlock,isEmergencyCall);
    }
    @Override
    public int onAttemptsFailed(int attempts){
        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"onAttemptsFailed: "+attempts);
        boolean cBoxSecuritySelfie = prefs.getBoolean("cBoxSecuritySelfie",false);
        int securitySelfieAttempts = prefs.getInt("securitySelfieAttempts",3);
        if(cBoxSecuritySelfie){
            if(attempts<securitySelfieAttempts){
                attempts++;
            }else{
                attempts = 0;
                post(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                                if(as2==null){
                                    as2 = new AutoSelfie2(context);
                                }
                                if(as2.isCameraProcessing()){
                                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"onAttemptsFailed>as2.isCameraProcessing(): true");
                                    return;
                                }
                                if(as2.isFrontFacingCameraSupported()){
                                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"onAttemptsFailed>start taking camera picture");
                                    as2.safeCameraOpen();
                                }
                            }else{
                                autoSelfie = new AutoSelfie(context);
                                if(autoSelfie.isFrontFacingCameraSupported()){
                                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"onAttemptsFailed>as.isFrontFacingCameraSupported()");
                                    boolean isCameraOpen = autoSelfie.safeCameraOpen();
                                    if(isCameraOpen){
                                        autoSelfie.takePicture();
                                    }
                                }
                            }
                        }catch(Exception e){
                            mLocker.saveErrorLog(null,e);
                        }
                    }
                });
            }
        }

        boolean cBoxHideForgotPin = prefs.getBoolean("cBoxHideForgotPin",false);
        if(!cBoxHideForgotPin){
            if(!btnForgotPin.isShown()){
                btnForgotPin.setVisibility(View.VISIBLE);
            }
        }

        return attempts;
    }
    //PIN
    @Override
    public void onSetPassword(int intPinIndex){
        switch(intPinIndex){
            case 0:
                vPassWord01.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 1:
                vPassWord02.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 2:
                vPassWord03.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 3:
                vPassWord04.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 4:
                vPassWord05.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 5:
                vPassWord06.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 6:
                vPassWord07.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
            case 7:
                vPassWord08.setBackgroundResource(R.drawable.shape_ring_white_small);
                break;
        }
    }
    @Override
    public void onClearTexts(){
        vPassWord01.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord02.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord03.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord04.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord05.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord06.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord07.setBackgroundResource(R.drawable.shape_round_white_small);
        vPassWord08.setBackgroundResource(R.drawable.shape_round_white_small);
    }
    private void finish(final boolean unlock,final boolean isEmergencyCall){
        if(unlock){
            context.sendBroadcast(new Intent(context.getPackageName()+C.PIN_UNLOCK));
            closePIN();//must put the last
            return;
        }
        if(isEmergencyCall){
            context.sendBroadcast(new Intent(context.getPackageName()+C.EMERGENCY_CALL));
        }
        context.sendBroadcast(new Intent(context.getPackageName()+C.SHOW_VIEW));
        closePIN();//must put the last
    }
    private void closePIN(){
        if(!C.isAndroid(Build.VERSION_CODES.O)){
            WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.removeView(this);
        }
        context.sendBroadcast(new Intent(context.getPackageName()+C.CLOSE_PIN));
    }
    //pattern
    @Override
    public void onPatternStart(){
    }
    @Override
    public void onPatternCleared(){
    }
    @Override
    public void onPatternCellAdded(List<Cell> pattern){
    }
    @Override
    public void onPatternDetected(List<Cell> pattern){
    }
    //
    private void loadViews(){
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = (LinearLayout)inflater.inflate(R.layout.security_unlock,this,false);
        mainView.setVisibility(View.INVISIBLE);
        mainView.setBackgroundColor(Color.parseColor("#99000000"));
        this.addView(mainView);

        cBoxPinDigit4 = findViewById(R.id.cBoxPinDigit4);
        cBoxPinDigit6 = findViewById(R.id.cBoxPinDigit6);
        cBoxPinDigit8 = findViewById(R.id.cBoxPinDigit8);

        txtPassWordDesc = findViewById(R.id.txtPasswordDesc);
        vPassWord01 = findViewById(R.id.viewPassword_1);
        vPassWord02 = findViewById(R.id.viewPassword_2);
        vPassWord03 = findViewById(R.id.viewPassword_3);
        vPassWord04 = findViewById(R.id.viewPassword_4);
        vPassWord05 = findViewById(R.id.viewPassword_5);
        vPassWord06 = findViewById(R.id.viewPassword_6);
        vPassWord07 = findViewById(R.id.viewPassword_7);
        vPassWord08 = findViewById(R.id.viewPassword_8);

        lytSecurityType = findViewById(R.id.lytSecurityType);

        if(isSettingMode){
            eTxtOwnerMsg = findViewById(R.id.editTxtOwnerMsg);
            txtOwnerMsgDesc = findViewById(R.id.txtOwnerMsgDesc);
        }else{
            lytOwnerMsg = findViewById(R.id.lytOwnerMsg);
            txtOwnerMsg = findViewById(R.id.txtOwnerMsg);
            txtNetWorkProvider = findViewById(R.id.txtNetworkProvider);
            btnForgotPin = findViewById(R.id.btnForgotPin);
            btnEmergencyCall = findViewById(R.id.btnEmergencyCall);

            LinearLayout v = (LinearLayout)btnEmergencyCall.getParent();
            v.setLayoutTransition(new LayoutTransition());
        }
    }
    private void loadSettings(){
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        boolean cBoxHidePinAnimation = prefs.getBoolean("cBoxHidePinAnimation",false);
        boolean cBoxHideEmergencyCall = prefs.getBoolean("cBoxHideEmergencyCall",false);
        boolean cBoxHideNetworkCarrier = prefs.getBoolean("cBoxHideNetworkCarrier",false);

        if(securityType==C.SECURITY_UNLOCK_PIN){
            final LockPinView pinView = new LockPinView(context,this,txtPassWordDesc,isSettingMode);
            lytSecurityType.addView(pinView);
            if(!isSettingMode){
                pinView.setInStealthMode(cBoxHidePinAnimation);
            }
            txtPassWordDesc.setText(R.string.type_new_password);
        }else if(securityType==C.SECURITY_UNLOCK_PATTERN){
            final LockPatternView patternView = new LockPatternView(context,this,txtPassWordDesc,isSettingMode);
            patternView.setTactileFeedbackEnabled(false);

            lytSecurityType.addView(patternView);
            if(isSettingMode){
                Button btn = new Button(context);
                btn.setText(R.string.clear);
                btn.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        patternView.reset();
                    }
                });
                lytSecurityType.addView(btn);
            }else{
                patternView.setInStealthMode(cBoxHidePinAnimation);
                addBackKey();
            }
            txtPassWordDesc.setText(R.string.enter_pattern);
        }else{
            finish(true,false);//no security
        }

        cBoxPinDigit4.setChecked(prefs.getBoolean("cBoxPinDigit4",true));
        cBoxPinDigit6.setChecked(prefs.getBoolean("cBoxPinDigit6",false));
        cBoxPinDigit8.setChecked(prefs.getBoolean("cBoxPinDigit8",false));

        if(isSettingMode){
            eTxtOwnerMsg.setText(prefs.getString("txtOwnerMsg",""));

            txtOwnerMsgDesc.setVisibility(View.VISIBLE);
            eTxtOwnerMsg.setVisibility(View.VISIBLE);

            if(securityType==C.SECURITY_UNLOCK_PATTERN){
                cBoxPinDigit4.setVisibility(View.GONE);
                cBoxPinDigit6.setVisibility(View.GONE);
                cBoxPinDigit8.setVisibility(View.GONE);
            }
        }else{
            cBoxPinDigit4.setVisibility(View.GONE);
            cBoxPinDigit6.setVisibility(View.GONE);
            cBoxPinDigit8.setVisibility(View.GONE);


            if(!cBoxHideEmergencyCall){
                btnEmergencyCall.setVisibility(View.VISIBLE);
            }

            final TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            txtNetWorkProvider.setText(manager.getNetworkOperatorName());
            if(!cBoxHideNetworkCarrier){
                txtNetWorkProvider.setVisibility(View.VISIBLE);
            }else{
                txtNetWorkProvider.setVisibility(View.INVISIBLE);
            }

            lytOwnerMsg.setVisibility(View.VISIBLE);
            final String msg = prefs.getString("txtOwnerMsg","");
            if(!msg.equals("")){
                intTextColor = Color.parseColor("#"+prefs.getString("strTextColorPersonalMsg","ffffff"));
                strTextStyle = prefs.getString("strTextStylePersonalMsg","txtStyle03.ttf");
                seekBarTextSizePersonalMsg = prefs.getInt("seekBarTextSizePersonalMsg",3);
                txtOwnerMsg.setText(msg);
                loadTextSettings();
            }
        }

        if(cBoxPinDigit4.isChecked()){
            vPassWord05.setVisibility(View.GONE);
            vPassWord06.setVisibility(View.GONE);
            vPassWord07.setVisibility(View.GONE);
            vPassWord08.setVisibility(View.GONE);
        }else if(cBoxPinDigit6.isChecked()){
            vPassWord07.setVisibility(View.GONE);
            vPassWord08.setVisibility(View.GONE);
        }
    }
    private void loadTextSettings(){
        try{
            if(strTextStyle.equals("Default")){
                mTypeface = Typeface.DEFAULT;
            }else{
                mTypeface = Typeface.createFromAsset(context.getAssets(),strTextStyle);
            }
        }catch(Exception e){
            mTypeface = Typeface.DEFAULT;
        }
        if(strTextStyle.equals("txtStyle03.ttf")){
            intTextStyle = Typeface.BOLD;
        }else{
            intTextStyle = Typeface.NORMAL;
        }
        txtOwnerMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP,TEXT_MIN_SIZE+seekBarTextSizePersonalMsg);
        loadFontStyle();
        loadFontColor();
    }
    private void loadFontStyle(){
        txtOwnerMsg.setTypeface(mTypeface,intTextStyle);
    }
    private void loadFontColor(){
        txtOwnerMsg.setTextColor(intTextColor);
    }
    private void launchEmergencyCall(){
        Toast toast = Toast.makeText(context,context.getString(R.string.try_ec_no),Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();

        finish(false,true);
    }
    private void rBtnFunction(){
        if(isSettingMode){
            cBoxPinDigit4.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    //cBoxPinDigit4.setChecked(false);
                    cBoxPinDigit6.setChecked(false);
                    cBoxPinDigit8.setChecked(false);

                    vPassWord05.setVisibility(View.GONE);
                    vPassWord06.setVisibility(View.GONE);
                    vPassWord07.setVisibility(View.GONE);
                    vPassWord08.setVisibility(View.GONE);

                    saveSettings();
                }
            });
            cBoxPinDigit6.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    cBoxPinDigit4.setChecked(false);
                    //cBoxPinDigit6.setChecked(false);
                    cBoxPinDigit8.setChecked(false);

                    vPassWord05.setVisibility(View.VISIBLE);
                    vPassWord06.setVisibility(View.VISIBLE);
                    vPassWord07.setVisibility(View.GONE);
                    vPassWord08.setVisibility(View.GONE);

                    saveSettings();
                }
            });
            cBoxPinDigit8.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    cBoxPinDigit4.setChecked(false);
                    cBoxPinDigit6.setChecked(false);
                    //cBoxPinDigit8.setChecked(false);

                    vPassWord05.setVisibility(View.VISIBLE);
                    vPassWord06.setVisibility(View.VISIBLE);
                    vPassWord07.setVisibility(View.VISIBLE);
                    vPassWord08.setVisibility(View.VISIBLE);

                    saveSettings();
                }
            });
        }else{
            btnForgotPin.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    context.sendBroadcast(new Intent(context.getPackageName()+C.VERIFY_GOOGLE_PIN));
                    closePIN();//must put the last
                }
            });
            btnEmergencyCall.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    try{
                        launchEmergencyCall();
                    }catch(Exception e){
                        mLocker.saveErrorLog(null,e);
                    }
                }
            });
        }
    }
    private void addBackKey(){
        final int side = C.dpToPx(context,50);
        final int pad = (int)(side*0.2f);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(side,side);
        pr.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        ImageView img = new ImageView(context);
        img.setLayoutParams(pr);
        img.setImageResource(R.drawable.ic_arrow_back_192);
        img.setPadding(pad,pad,pad,pad);
        img.setAlpha(0.7f);
        img.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                finish(false,false);
            }
        });
        this.addView(img);
    }
    public interface OnSecurityUnlockListener{
        void onSettingResult(final int result,int securityType);
    }
}