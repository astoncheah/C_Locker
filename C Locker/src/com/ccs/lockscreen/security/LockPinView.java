package com.ccs.lockscreen.security;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;

public class LockPinView extends LinearLayout{
    //=====
    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private OnPinListener callback;
    private LinearLayout mainView;
    private TextView txtPassWordDesc;
    private boolean isSettingMode;
    private boolean isFirstTry = true;
    private TextView btnNo01, btnNo02, btnNo03, btnNo04, btnNo05, btnNo06, btnNo07, btnNo08, btnNo09, btnNo00, btnFunction01, btnFunction02;
    private int intPinIndex = 0, attempts = 0;
    private int //setting mode
            intPass1st01 = -1, intPass1st02 = -1, intPass1st03 = -1, intPass1st04 = -1, intPass1st05 = -1, intPass1st06 = -1, intPass1st07 = -1, intPass1st08 = -1, intPass2nd01 = -1, intPass2nd02 = -1, intPass2nd03 = -1, intPass2nd04 = -1, intPass2nd05 = -1, intPass2nd06 = -1, intPass2nd07 = -1, intPass2nd08 = -1;
    private int intPass01, intPass02, intPass03, intPass04, intPass05, intPass06, intPass07, intPass08, intCurrentPass01, intCurrentPass02, intCurrentPass03, intCurrentPass04, intCurrentPass05, intCurrentPass06, intCurrentPass07, intCurrentPass08;

    public LockPinView(final Context context,OnPinListener callback,final TextView txtPassWordDesc,
            boolean isSettingMode){
        super(context);
        this.context = context;
        this.callback = callback;
        this.txtPassWordDesc = txtPassWordDesc;
        this.isSettingMode = isSettingMode;
        loadViews();
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
    }

    private void loadViews(){
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mainView = (LinearLayout)inflater.inflate(R.layout.pin_unlock,this,false);
        addView(mainView);

        btnNo01 = (TextView)findViewById(R.id.btnNo_1);
        btnNo02 = (TextView)findViewById(R.id.btnNo_2);
        btnNo03 = (TextView)findViewById(R.id.btnNo_3);
        btnNo04 = (TextView)findViewById(R.id.btnNo_4);
        btnNo05 = (TextView)findViewById(R.id.btnNo_5);
        btnNo06 = (TextView)findViewById(R.id.btnNo_6);
        btnNo07 = (TextView)findViewById(R.id.btnNo_7);
        btnNo08 = (TextView)findViewById(R.id.btnNo_8);
        btnNo09 = (TextView)findViewById(R.id.btnNo_9);
        btnNo00 = (TextView)findViewById(R.id.btnNo_0);

        btnFunction01 = (TextView)findViewById(R.id.btnFunction_1);
        btnFunction02 = (TextView)findViewById(R.id.btnFunction_2);

        resetPin();
        rBtnFunction();
        loadSettings();
    }

    private void loadSettings(){
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);

        intPass01 = prefs.getInt("intPass2nd01",-1);
        intPass02 = prefs.getInt("intPass2nd02",-1);
        intPass03 = prefs.getInt("intPass2nd03",-1);
        intPass04 = prefs.getInt("intPass2nd04",-1);
        intPass05 = prefs.getInt("intPass2nd05",-1);
        intPass06 = prefs.getInt("intPass2nd06",-1);
        intPass07 = prefs.getInt("intPass2nd07",-1);
        intPass08 = prefs.getInt("intPass2nd08",-1);

        if(isSettingMode){
            btnFunction01.setText(R.string.clear);
            btnFunction02.setText(android.R.string.ok);
        }else{
            btnFunction01.setText(android.R.string.cancel);
            btnFunction02.setText(R.string.clear);
        }
    }

    private void saveSettings(boolean isPasswordSetCompleted){
        editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
        if(isPasswordSetCompleted){
            editor.putInt("intPass2nd01",intPass2nd01);
            editor.putInt("intPass2nd02",intPass2nd02);
            editor.putInt("intPass2nd03",intPass2nd03);
            editor.putInt("intPass2nd04",intPass2nd04);
            editor.putInt("intPass2nd05",intPass2nd05);
            editor.putInt("intPass2nd06",intPass2nd06);
            editor.putInt("intPass2nd07",intPass2nd07);
            editor.putInt("intPass2nd08",intPass2nd08);
            editor.putBoolean("cBoxReturnLS",true);
        }else{
            editor.putInt("intPass2nd01",-1);
            editor.putInt("intPass2nd02",-1);
            editor.putInt("intPass2nd03",-1);
            editor.putInt("intPass2nd04",-1);
            editor.putInt("intPass2nd05",-1);
            editor.putInt("intPass2nd06",-1);
            editor.putInt("intPass2nd07",-1);
            editor.putInt("intPass2nd08",-1);
        }
        editor.commit();
    }

    private void rBtnFunction(){
        btnNo01.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(1);
            }
        });
        btnNo02.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(2);
            }
        });
        btnNo03.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(3);
            }
        });
        btnNo04.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(4);
            }
        });
        btnNo05.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(5);
            }
        });
        btnNo06.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(6);
            }
        });
        btnNo07.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(7);
            }
        });
        btnNo08.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(8);
            }
        });
        btnNo09.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(9);
            }
        });
        btnNo00.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setPassword(0);
            }
        });
        btnFunction01.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(isSettingMode){
                    resetPin();
                    if(isFirstTry){
                        clear1stPassword();
                        txtPassWordDesc.setText(R.string.type_new_password);
                    }else{
                        clear2ndPassword();
                        txtPassWordDesc.setText(R.string.type_confirm_password);
                    }
                }else{
                    callback.onFinish(false,false);
                }
            }
        });
        btnFunction02.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(isSettingMode){
                    if(isFirstTry){
                        if(checkPassword()){
                            isFirstTry = false;
                            resetPin();
                            txtPassWordDesc.setText(R.string.type_confirm_password);
                        }else{
                            isFirstTry = true;
                        }
                    }else{
                        if(checkPassword()){
                            if(confirmPassword()){
                                saveSettings(true);
                                callback.onSettingFinish(Activity.RESULT_OK);
                            }else{
                                saveSettings(false);
                                isFirstTry = true;
                                resetPin();
                                clear1stPassword();
                                clear2ndPassword();
                                txtPassWordDesc.setText(R.string.type_new_password);
                            }
                        }
                    }
                }else{
                    resetPin();
                }
            }
        });
    }

    public void setInStealthMode(boolean inStealthMode){
        if(inStealthMode){
            btnNo01.setBackground(null);
            btnNo02.setBackground(null);
            btnNo03.setBackground(null);
            btnNo04.setBackground(null);
            btnNo05.setBackground(null);
            btnNo06.setBackground(null);
            btnNo07.setBackground(null);
            btnNo08.setBackground(null);
            btnNo09.setBackground(null);
            btnNo00.setBackground(null);
        }
    }

    private void setPassword(int val){
        boolean cBoxPinDigit4 = prefs.getBoolean("cBoxPinDigit4",true);
        boolean cBoxPinDigit6 = prefs.getBoolean("cBoxPinDigit6",false);
        //boolean cBoxPinDigit8 = prefs.getBoolean("cBoxPinDigit8", false);
        callback.onSetPassword(intPinIndex);

        switch(intPinIndex){
            case 0:
                intPinIndex = 1;
                if(isSettingMode){
                    isFirstTry(isFirstTry,1,val);
                }else{
                    intCurrentPass01 = val;
                }
                break;
            case 1:
                intPinIndex = 2;
                if(isSettingMode){
                    isFirstTry(isFirstTry,2,val);
                }else{
                    intCurrentPass02 = val;
                }
                break;
            case 2:
                intPinIndex = 3;
                if(isSettingMode){
                    isFirstTry(isFirstTry,3,val);
                }else{
                    intCurrentPass03 = val;
                }
                break;
            case 3:
                intPinIndex = 4;
                if(isSettingMode){
                    isFirstTry(isFirstTry,4,val);
                    if(cBoxPinDigit4){
                        return;
                    }
                }else{
                    intCurrentPass04 = val;
                    if(cBoxPinDigit4){
                        checkPassword();
                        return;
                    }
                }
                break;
            case 4:
                intPinIndex = 5;
                if(isSettingMode){
                    isFirstTry(isFirstTry,5,val);
                }else{
                    intCurrentPass05 = val;
                }
                break;
            case 5:
                intPinIndex = 6;
                if(isSettingMode){
                    isFirstTry(isFirstTry,6,val);
                    if(cBoxPinDigit6){
                        return;
                    }
                }else{
                    intCurrentPass06 = val;
                    if(cBoxPinDigit6){
                        checkPassword();
                        return;
                    }
                }
                break;
            case 6:
                intPinIndex = 7;
                if(isSettingMode){
                    isFirstTry(isFirstTry,7,val);
                }else{
                    intCurrentPass07 = val;
                }
                break;
            case 7:
                intPinIndex = 8;
                if(isSettingMode){
                    isFirstTry(isFirstTry,8,val);
                }else{
                    intCurrentPass08 = val;
                    checkPassword();
                }
                break;
        }
    }

    private void isFirstTry(boolean bln,int no,int val){
        boolean cBoxPinDigit4 = prefs.getBoolean("cBoxPinDigit4",true);
        boolean cBoxPinDigit6 = prefs.getBoolean("cBoxPinDigit6",false);
        //boolean cBoxPinDigit8 = prefs.getBoolean("cBoxPinDigit8", false);
        switch(no){
            case 1:
                if(bln){
                    intPass1st01 = val;
                }else{
                    intPass2nd01 = val;
                }
                break;
            case 2:
                if(bln){
                    intPass1st02 = val;
                }else{
                    intPass2nd02 = val;
                }
                break;
            case 3:
                if(bln){
                    intPass1st03 = val;
                }else{
                    intPass2nd03 = val;
                }
                break;
            case 4:
                if(bln){
                    intPass1st04 = val;
                }else{
                    intPass2nd04 = val;
                }
                break;
            case 5:
                if(cBoxPinDigit4){
                    return;
                }
                if(bln){
                    intPass1st05 = val;
                }else{
                    intPass2nd05 = val;
                }
                break;
            case 6:
                if(bln){
                    intPass1st06 = val;
                }else{
                    intPass2nd06 = val;
                }
                break;
            case 7:
                if(cBoxPinDigit6){
                    return;
                }
                if(bln){
                    intPass1st07 = val;
                }else{
                    intPass2nd07 = val;
                }
                break;
            case 8:
                if(bln){
                    intPass1st08 = val;
                }else{
                    intPass2nd08 = val;
                }
                break;
        }
    }

    private void resetPin(){
        intPinIndex = 0;

        intCurrentPass01 = -1;
        intCurrentPass02 = -1;
        intCurrentPass03 = -1;
        intCurrentPass04 = -1;
        intCurrentPass05 = -1;
        intCurrentPass06 = -1;
        intCurrentPass07 = -1;
        intCurrentPass08 = -1;

        callback.onClearTexts();
    }

    private void clear1stPassword(){
        intPass1st01 = -1;
        intPass1st02 = -1;
        intPass1st03 = -1;
        intPass1st04 = -1;
        intPass1st05 = -1;
        intPass1st06 = -1;
        intPass1st07 = -1;
        intPass1st08 = -1;
    }

    private void clear2ndPassword(){
        intPass2nd01 = -1;
        intPass2nd02 = -1;
        intPass2nd03 = -1;
        intPass2nd04 = -1;
        intPass2nd05 = -1;
        intPass2nd06 = -1;
        intPass2nd07 = -1;
        intPass2nd08 = -1;
    }

    private boolean checkPassword(){
        boolean cBoxPinDigit4 = prefs.getBoolean("cBoxPinDigit4",true);
        boolean cBoxPinDigit6 = prefs.getBoolean("cBoxPinDigit6",false);
        //boolean cBoxPinDigit8 = prefs.getBoolean("cBoxPinDigit8", false);
        if(isSettingMode){
            if(cBoxPinDigit4){
                if(intPinIndex==4){
                    return true;
                }
            }else if(cBoxPinDigit6){
                if(intPinIndex==6){
                    return true;
                }
            }else{
                if(intPinIndex==8){
                    return true;
                }
            }
            Toast t = Toast.makeText(context,R.string.password_not_complete,Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP,0,50);
            t.show();
        }else{
            if(cBoxPinDigit4){
                if(intCurrentPass01==intPass01 &&
                        intCurrentPass02==intPass02 &&
                        intCurrentPass03==intPass03 &&
                        intCurrentPass04==intPass04){

                    callback.onFinish(true,false);
                    return true;
                }
            }else if(cBoxPinDigit6){
                if(intCurrentPass01==intPass01 &&
                        intCurrentPass02==intPass02 &&
                        intCurrentPass03==intPass03 &&
                        intCurrentPass04==intPass04 &&
                        intCurrentPass05==intPass05 &&
                        intCurrentPass06==intPass06){

                    callback.onFinish(true,false);
                    return true;
                }
            }else{
                if(intCurrentPass01==intPass01 &&
                        intCurrentPass02==intPass02 &&
                        intCurrentPass03==intPass03 &&
                        intCurrentPass04==intPass04 &&
                        intCurrentPass05==intPass05 &&
                        intCurrentPass06==intPass06 &&
                        intCurrentPass07==intPass07 &&
                        intCurrentPass08==intPass08){

                    callback.onFinish(true,false);
                    return true;
                }
            }
            //attempt fails
            attempts = callback.onAttemptsFailed(attempts);
            resetPin();
            Toast t = Toast.makeText(context,R.string.password_not_matched,Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP,0,50);
            t.show();
        }
        performHapticFeedback();
        return false;
    }

    private boolean confirmPassword(){
        boolean cBoxPinDigit4 = prefs.getBoolean("cBoxPinDigit4",true);
        boolean cBoxPinDigit6 = prefs.getBoolean("cBoxPinDigit6",false);
        //boolean cBoxPinDigit8 = prefs.getBoolean("cBoxPinDigit8", false);
        if(cBoxPinDigit4){
            if(intPass1st01==intPass2nd01 &&
                    intPass1st02==intPass2nd02 &&
                    intPass1st03==intPass2nd03 &&
                    intPass1st04==intPass2nd04){
                return true;
            }
        }else if(cBoxPinDigit6){
            if(intPass1st01==intPass2nd01 &&
                    intPass1st02==intPass2nd02 &&
                    intPass1st03==intPass2nd03 &&
                    intPass1st04==intPass2nd04 &&
                    intPass1st05==intPass2nd05 &&
                    intPass1st06==intPass2nd06){
                return true;
            }
        }else{
            if(intPass1st01==intPass2nd01 &&
                    intPass1st02==intPass2nd02 &&
                    intPass1st03==intPass2nd03 &&
                    intPass1st04==intPass2nd04 &&
                    intPass1st05==intPass2nd05 &&
                    intPass1st06==intPass2nd06 &&
                    intPass1st07==intPass2nd07 &&
                    intPass1st08==intPass2nd08){
                return true;
            }
        }

        performHapticFeedback();
        Toast t = Toast.makeText(context,R.string.password_not_matched,Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP,0,50);
        t.show();
        return false;
    }

    private void performHapticFeedback(){
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
    }

    public interface OnPinListener{
        void onSettingFinish(final int result);

        void onFinish(final boolean unlock,final boolean isEmergencyCall);

        int onAttemptsFailed(int attempts);

        void onSetPassword(int val);

        void onClearTexts();
    }
}