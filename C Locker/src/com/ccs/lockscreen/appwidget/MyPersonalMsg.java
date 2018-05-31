package com.ccs.lockscreen.appwidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;

public class MyPersonalMsg extends LinearLayout{
    private static final int TEXT_MIN_SIZE = 15;
    private SharedPreferences prefs;
    private Context context;
    private Typeface mTypeface;
    private LinearLayout main;
    private TextView txtOwnerMsg;
    private int intTextColor, intTextStyle, seekBarTextSizePersonalMsg;
    private String strTextStyle;

    public MyPersonalMsg(final Context context,final boolean isEnabled){
        super(context);
        this.context = context;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.locker_personal_info,this,true);

        main = (LinearLayout)v.findViewById(R.id.lytOwnerMsg);
        txtOwnerMsg = (TextView)v.findViewById(R.id.txtOwnerMsg);
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        loadSettings();
        loadTextSettings();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
    }

    private void loadSettings(){
        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        intTextColor = Color.parseColor("#"+prefs.getString("strColorLockerMainText","ffffff"));
        strTextStyle = prefs.getString("strTextStyleLocker","txtStyle03.ttf");
        seekBarTextSizePersonalMsg = prefs.getInt("seekBarTextSizePersonalMsg",3);

        final String msg = prefs.getString("txtOwnerMsg","");
        txtOwnerMsg.setText(msg);
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

    public final void setParam(int w,int h){
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(w,h);
        main.setLayoutParams(pr);
    }
}
