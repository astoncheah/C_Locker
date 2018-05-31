package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;

import java.util.ArrayList;
import java.util.List;

public class SettingsClockDigital extends BaseActivity{
    private static final int SELECTED_CLOCK_TEXT_TYPE = 5, SELECTED_CLOCK_TEXT_COLOR = 6;
    //=====
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private LinearLayout lytTextType, lytTextColor, lytShowClockAm, lytShowClockAlarm;
    private TextView txtTextType, txtTextColor;
    private CheckBox cBoxShowClockAm, cBoxShowClockAlarm;
    private Spinner spinnerFormatTime;
    private String strColorLockerMainText, strTextStyleLocker;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_digital_clock);
        setBasicBackKeyAction();

        myAlertDialog = new MyAlertDialog(this);
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            lytTextType = (LinearLayout)findViewById(R.id.lytTextType);
            lytTextColor = (LinearLayout)findViewById(R.id.lytTextColor);
            lytShowClockAm = (LinearLayout)findViewById(R.id.lytShowClockAm);
            lytShowClockAlarm = (LinearLayout)findViewById(R.id.lytShowClockAlarm);

            txtTextType = (TextView)findViewById(R.id.txtTextType);
            txtTextColor = (TextView)findViewById(R.id.txtTextColor);

            cBoxShowClockAm = (CheckBox)findViewById(R.id.cBoxShowClockAm);
            cBoxShowClockAlarm = (CheckBox)findViewById(R.id.cBoxShowClockAlarm);

            spinnerFunction();
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
        return R.layout.settings_clock_digital;
    }

    private void spinnerFunction(){
        spinnerFormatTime = (Spinner)findViewById(R.id.spnTimeFormat);
        List<String> listFormatTime = new ArrayList<String>();
        listFormatTime.add("24hr: 00:00");
        listFormatTime.add("12hr: 00:00");
        listFormatTime.add("12hr: 0:00");
        ArrayAdapter<String> dataAdapterFormatTime = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,listFormatTime);
        dataAdapterFormatTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormatTime.setAdapter(dataAdapterFormatTime);
    }

    private void onClickFunction(){
        lytTextType.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsClockDigital.this,PickerTextStyle.class);
                startActivityForResult(i,SELECTED_CLOCK_TEXT_TYPE);
            }
        });
        lytTextColor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsClockDigital.this,PickerColor.class);
                startActivityForResult(i,SELECTED_CLOCK_TEXT_COLOR);
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
        spinnerFormatTime.setSelection(prefs.getInt("spinnerFormatTime",0));

        cBoxShowClockAm.setChecked(prefs.getBoolean("cBoxShowAM",false));
        cBoxShowClockAlarm.setChecked(prefs.getBoolean("cBoxShowAlarm",true));

        strTextStyleLocker = prefs.getString("strTextStyleLocker","txtStyle03.ttf");
        if(strTextStyleLocker.equals("Default")){
            txtTextType.setTypeface(Typeface.DEFAULT);
        }else{
            txtTextType.setTypeface(Typeface.createFromAsset(getAssets(),strTextStyleLocker));
        }

        strColorLockerMainText = prefs.getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");
        txtTextColor.setTextColor(Color.parseColor("#"+strColorLockerMainText));
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
                case SELECTED_CLOCK_TEXT_TYPE:
                    if(resultCode==Activity.RESULT_OK){
                        strTextStyleLocker = data.getExtras().getString("strTextStyleLocker","txtStyle03.ttf");
                        if(strTextStyleLocker.equals("Default")){
                            txtTextType.setTypeface(Typeface.DEFAULT);
                        }else{
                            txtTextType.setTypeface(Typeface.createFromAsset(getAssets(),strTextStyleLocker));
                        }
                    }
                    break;
                case SELECTED_CLOCK_TEXT_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strColorLockerMainText = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");
                        txtTextColor.setTextColor(Color.parseColor("#"+strColorLockerMainText));
                        myAlertDialog.colorTheme(strColorLockerMainText);
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
        int intFT = spinnerFormatTime.getSelectedItemPosition();
        editor.putInt("spinnerFormatTime",intFT);
        editor.putString("strTimeFormat",setTimeFormat(intFT));

        editor.putBoolean("cBoxShowAM",cBoxShowClockAm.isChecked());
        editor.putBoolean("cBoxShowAlarm",cBoxShowClockAlarm.isChecked());

        editor.putString(P.STR_COLOR_LOCKER_MAIN_TEXT,strColorLockerMainText);
        editor.putString("strTextStyleLocker",strTextStyleLocker);
        editor.commit();
    }

    private String setTimeFormat(int no){
        String strTimeFormat = "HH:mm";
        if(no==0){
            strTimeFormat = "HH:mm";
        }else if(no==1){
            strTimeFormat = "hh:mm";
        }else{
            strTimeFormat = "h:mm";
        }
        return strTimeFormat;
    }
}