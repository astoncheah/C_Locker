package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;

public class SettingsPersonalMsg extends BaseActivity{
    private static final int SELECTED_CLOCK_TEXT_TYPE = 5, SELECTED_CLOCK_TEXT_COLOR = 6;
    private static final int TEXT_MIN_SIZE = 15;
    //=====
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private LinearLayout lytTextTypePersonalMsg, lytTextColorPersonalMsg;
    private TextView txtTextTypePersonalMsg, txtTextColorPersonalMsg;
    private SeekBar seekBarTextSizePersonalMsg;
    private EditText editTxtPersonalMsg;
    private String strTextStylePersonalMsg, strTextColorPersonalMsg;
    private int intTextStyle;
    private Typeface mTypeface;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_personal_msg);
        setBasicBackKeyAction();

        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            lytTextTypePersonalMsg = (LinearLayout)findViewById(R.id.lytTextTypePersonalMsg);
            lytTextColorPersonalMsg = (LinearLayout)findViewById(R.id.lytTextColorPersonalMsg);

            txtTextTypePersonalMsg = (TextView)findViewById(R.id.txtTextTypePersonalMsg);
            txtTextColorPersonalMsg = (TextView)findViewById(R.id.txtTextColorPersonalMsg);

            seekBarTextSizePersonalMsg = (SeekBar)findViewById(R.id.seekBarTextSizePersonalMsg);
            editTxtPersonalMsg = (EditText)findViewById(R.id.editTxtPersonalMsg);

            onClickFunction();
            loadSettings();
            loadTextSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            saveSettings();
            Intent i = new Intent(getPackageName()+C.UPDATE_CONFIGURE);
            sendBroadcast(i);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        //System.gc();
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_personal_msg;
    }

    private void saveSettings(){
        editor.putInt("seekBarTextSizePersonalMsg",seekBarTextSizePersonalMsg.getProgress());
        editor.putString("strTextStyleLocker",strTextStylePersonalMsg);
        editor.putString("strColorLockerMainText",strTextColorPersonalMsg);
        editor.putString("txtOwnerMsg",editTxtPersonalMsg.getText().toString());
        editor.commit();
    }

    private void onClickFunction(){
        lytTextTypePersonalMsg.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsPersonalMsg.this,PickerTextStyle.class);
                startActivityForResult(i,SELECTED_CLOCK_TEXT_TYPE);
            }
        });
        lytTextColorPersonalMsg.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsPersonalMsg.this,PickerColor.class);
                startActivityForResult(i,SELECTED_CLOCK_TEXT_COLOR);
            }
        });
        seekBarTextSizePersonalMsg.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){
                editTxtPersonalMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP,TEXT_MIN_SIZE+progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){
            }
        });
    }

    private void loadSettings(){
        strTextStylePersonalMsg = prefs.getString("strTextStyleLocker","txtStyle03.ttf");
        strTextColorPersonalMsg = prefs.getString("strColorLockerMainText","ffffff");
        seekBarTextSizePersonalMsg.setProgress(prefs.getInt("seekBarTextSizePersonalMsg",3));

        final String msg = prefs.getString("txtOwnerMsg","");
        editTxtPersonalMsg.setText(msg);
    }

    private void loadTextSettings(){
        try{
            if(strTextStylePersonalMsg.equals("Default")){
                mTypeface = Typeface.DEFAULT;
            }else{
                mTypeface = Typeface.createFromAsset(getAssets(),strTextStylePersonalMsg);
            }
        }catch(Exception e){
            mTypeface = Typeface.DEFAULT;
        }
        if(strTextStylePersonalMsg.equals("txtStyle03.ttf")){
            intTextStyle = Typeface.BOLD;
        }else{
            intTextStyle = Typeface.NORMAL;
        }
        editTxtPersonalMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP,TEXT_MIN_SIZE+seekBarTextSizePersonalMsg.getProgress());
        loadFontStyle();
        loadFontColor();
    }

    private void loadFontStyle(){
        txtTextTypePersonalMsg.setTypeface(mTypeface,intTextStyle);
        editTxtPersonalMsg.setTypeface(mTypeface,intTextStyle);
    }

    private void loadFontColor(){
        txtTextColorPersonalMsg.setTextColor(Color.parseColor("#"+strTextColorPersonalMsg));
        editTxtPersonalMsg.setTextColor(Color.parseColor("#"+strTextColorPersonalMsg));
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case SELECTED_CLOCK_TEXT_TYPE:
                    if(resultCode==Activity.RESULT_OK){
                        strTextStylePersonalMsg = data.getExtras().getString("strTextStyleLocker","txtStyle03.ttf");//must use this "STR_COLOR_LOCKER_MAIN_TEXT"
                        loadTextSettings();
                    }
                    break;
                case SELECTED_CLOCK_TEXT_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strTextColorPersonalMsg = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");//must use this "strTextStyleLocker"
                        loadTextSettings();
                    }
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }
}