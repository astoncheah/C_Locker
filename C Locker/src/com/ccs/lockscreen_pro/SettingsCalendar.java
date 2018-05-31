package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingsCalendar extends BaseActivity{
    private static final int SELECTED_TODAY_TEXT_COLOR = 1, SELECTED_OTHER_DAY_TEXT_COLOR = 2;
    //=====
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private LinearLayout lytSetTodayTextColor, lytSetOtherdayTextColor, lytCldAccountList;
    private Spinner spinnerCalendarDays;
    private TextView txtSetTodayTextColor, txtSetOtherdayTextColor;
    private String strColorTodayText, strColorOtherdayText;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_calendar);
        setBasicBackKeyAction();

        try{
            lytSetTodayTextColor = (LinearLayout)findViewById(R.id.lytSetTodayTextColor);
            lytSetOtherdayTextColor = (LinearLayout)findViewById(R.id.lytSetOtherdayTextColor);
            lytCldAccountList = (LinearLayout)findViewById(R.id.lytCldAccountList);

            spinnerCalendarDays = (Spinner)findViewById(R.id.spn_calendarEvents);
            txtSetTodayTextColor = (TextView)findViewById(R.id.txtSetTodayTextColor);
            txtSetOtherdayTextColor = (TextView)findViewById(R.id.txtSetOtherdayTextColor);

            spinnerFunction();
            onClickFunction();
            loadSettings();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy(){
        Intent i = new Intent(getPackageName()+C.UPDATE_CONFIGURE);
        sendBroadcast(i);
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_calendar;
    }

    private void spinnerFunction(){
        List<String> listCalendarDays = new ArrayList<String>();
        listCalendarDays.add(getString(R.string.next_event_1days));
        listCalendarDays.add(getString(R.string.next_event_3days));
        listCalendarDays.add(getString(R.string.next_event_7days));
        listCalendarDays.add(getString(R.string.next_event_14days));
        listCalendarDays.add(getString(R.string.next_event_30days));
        listCalendarDays.add(getString(R.string.next_event_60days));
        listCalendarDays.add(getString(R.string.next_event_90days));
        ArrayAdapter<String> dataAdapterCalendarDays = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,listCalendarDays);
        dataAdapterCalendarDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCalendarDays.setAdapter(dataAdapterCalendarDays);
    }

    private void onClickFunction(){
        lytSetTodayTextColor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsCalendar.this,PickerColor.class);
                startActivityForResult(i,SELECTED_TODAY_TEXT_COLOR);
            }
        });
        lytSetOtherdayTextColor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsCalendar.this,PickerColor.class);
                startActivityForResult(i,SELECTED_OTHER_DAY_TEXT_COLOR);
            }
        });
        lytCldAccountList.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                startActivity(new Intent(SettingsCalendar.this,ListCalendarAccount.class));
            }
        });
    }

    private void loadSettings(){
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);

        spinnerCalendarDays.setSelection(prefs.getInt("spinnerCalendarDays",2));
        strColorTodayText = prefs.getString(P.STR_COLOR_EVENT_TODAY_TEXT,"FFFF00");
        strColorOtherdayText = prefs.getString(P.STR_COLOR_EVENT_OTHER_DAY_TEXT,"FFFFFF");
        txtSetTodayTextColor.setTextColor(Color.parseColor("#"+strColorTodayText));
        txtSetOtherdayTextColor.setTextColor(Color.parseColor("#"+strColorOtherdayText));
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            case SELECTED_TODAY_TEXT_COLOR:
                if(resultCode==Activity.RESULT_OK){
                    strColorTodayText = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"FFFF00");
                    txtSetTodayTextColor.setTextColor(Color.parseColor("#"+strColorTodayText));
                }
                break;
            case SELECTED_OTHER_DAY_TEXT_COLOR:
                if(resultCode==Activity.RESULT_OK){
                    strColorOtherdayText = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"FFFFFF");
                    txtSetOtherdayTextColor.setTextColor(Color.parseColor("#"+strColorOtherdayText));
                }
                break;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
            //finish();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void saveSettings(){
        editor = getSharedPreferences(C.PREFS_NAME,0).edit();

        editor.putInt("spinnerCalendarDays",spinnerCalendarDays.getSelectedItemPosition());
        editor.putInt("intAddEventDay",setNextCldDays(spinnerCalendarDays.getSelectedItemPosition()));
        editor.putBoolean("isCldListHiden",false);

        editor.putString(P.STR_COLOR_EVENT_TODAY_TEXT,strColorTodayText);
        editor.putString(P.STR_COLOR_EVENT_OTHER_DAY_TEXT,strColorOtherdayText);

        editor.commit();
    }

    private int setNextCldDays(int intVal){
        switch(intVal){
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 7;
            case 3:
                return 14;
            case 4:
                return 31;
            case 5:
                return 93;
            case 6:
                return 186;
        }
        return 1;
    }
}