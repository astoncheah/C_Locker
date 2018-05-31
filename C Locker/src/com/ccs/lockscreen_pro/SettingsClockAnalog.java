package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;

public class SettingsClockAnalog extends BaseActivity{
    private static final int SELECTED_ANALOG_CLOCK_COLOR = 7;
    //=====
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private LinearLayout lytAnalogClockColor, lytEnableAnalogClockUpdateInSecond, lytShowClockAm, lytShowClockAlarm;
    private TextView txtAnalogClockColor;
    private CheckBox cBoxEnableUpdateInSecond, cBoxShowClockAm, cBoxShowClockAlarm;
    private String strColorAnalogClock;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_analog_clock);
        setBasicBackKeyAction();

        myAlertDialog = new MyAlertDialog(this);
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            lytAnalogClockColor = (LinearLayout)findViewById(R.id.lytAnalogClockColor);
            lytEnableAnalogClockUpdateInSecond = (LinearLayout)findViewById(R.id.lytEnableAnalogClockUpdateInSecond);
            lytShowClockAm = (LinearLayout)findViewById(R.id.lytShowClockAm);
            lytShowClockAlarm = (LinearLayout)findViewById(R.id.lytShowClockAlarm);

            txtAnalogClockColor = (TextView)findViewById(R.id.txtAnalogClockColor);

            cBoxEnableUpdateInSecond = (CheckBox)findViewById(R.id.cBoxEnableAnalogClockUpdateInSecond);
            cBoxShowClockAm = (CheckBox)findViewById(R.id.cBoxShowClockAm);
            cBoxShowClockAlarm = (CheckBox)findViewById(R.id.cBoxShowClockAlarm);

            onClickFunction();
            loadSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            Intent i = new Intent(getPackageName()+C.UPDATE_CONFIGURE);
            sendBroadcast(i);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        try{
            if(myAlertDialog!=null){
                myAlertDialog.close();
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_clock_analog;
    }

    private void onClickFunction(){
        lytAnalogClockColor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsClockAnalog.this,PickerColor.class);
                startActivityForResult(i,SELECTED_ANALOG_CLOCK_COLOR);
            }
        });
        lytEnableAnalogClockUpdateInSecond.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                cBoxEnableUpdateInSecond.performClick();
            }
        });
        lytShowClockAm.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                cBoxShowClockAm.performClick();
            }
        });
        lytShowClockAlarm.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                cBoxShowClockAlarm.performClick();
            }
        });
    }

    private void loadSettings(){
        cBoxEnableUpdateInSecond.setChecked(prefs.getBoolean("cBoxEnableUpdateInSecond",false));
        cBoxShowClockAm.setChecked(prefs.getBoolean("cBoxShowAM",false));
        cBoxShowClockAlarm.setChecked(prefs.getBoolean("cBoxShowAlarm",true));

        strColorAnalogClock = prefs.getString(P.STR_COLOR_ANALOG_CLOCK,"ffffff");
        txtAnalogClockColor.setTextColor(Color.parseColor("#"+strColorAnalogClock));
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case SELECTED_ANALOG_CLOCK_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strColorAnalogClock = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");
                        txtAnalogClockColor.setTextColor(Color.parseColor("#"+strColorAnalogClock));
                        myAlertDialog.colorTheme(strColorAnalogClock);
                    }
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void saveSettings(){
        editor.putBoolean("cBoxEnableUpdateInSecond",cBoxEnableUpdateInSecond.isChecked());
        editor.putBoolean("cBoxShowAM",cBoxShowClockAm.isChecked());
        editor.putBoolean("cBoxShowAlarm",cBoxShowClockAlarm.isChecked());

        editor.putString(P.STR_COLOR_ANALOG_CLOCK,strColorAnalogClock);
        editor.commit();
    }
}