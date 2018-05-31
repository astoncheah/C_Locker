package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;

public class SettingsBottombar extends BaseActivity{
    private static final int PRO_ENABLE_EVENTS = 1, PRO_ENABLE_EVENTS_SETTIGNS = 2, PRO_RSS = 3, PRO_RSS_LIST = 4, PRO_RSS_SETTINGS = 5, SELECTED_CLOCK_TEXT_COLOR = 7, SELECTED_BTTY_LEVEL_COLOR = 8, SELECTED_BOTTOM_BAR_TAB_COLOR = 9;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private CheckBox cBoxEnableBottomBar, cBoxEnableBottomBarNotice, cBoxEnableGmail, cBoxEnableSms, cBoxEnableMissedCall, cBoxEnableCalendar, cBoxEnableRSS;
    private RadioButton cBoxShowArrowUpIcon, cBoxShowBatteryLevelBar, cBoxShowTransparentBar;
    private LinearLayout lytEnableBottomBarDrag, lytBottomBarNotice, lytSetNoticeTextColor, lytSetBottomBarTabColor;
    private Button btnTestSMS;
    private TextView txtSetNoticeTextColor, txtSetBottomBarTabColor;
    private ImageView imgEventsSettings, imgRssList, imgRssSettings;
    private View viewBttyLevel;
    private String errorMsg, strColorBottomBarBttyLvl, strColorBottomBarNoticeText, strColorBottomBarTab;
    private MyAlertDialog myAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_bottom_bar);
        setBasicBackKeyAction();
        try{
            myAlertDialog = new MyAlertDialog(this);
            prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            lytEnableBottomBarDrag = (LinearLayout)findViewById(R.id.lytEnableBottomBarDrag);
            lytBottomBarNotice = (LinearLayout)findViewById(R.id.lytBottomBarNotice);
            lytSetNoticeTextColor = (LinearLayout)findViewById(R.id.lytSetNoticeTextColor);
            lytSetBottomBarTabColor = (LinearLayout)findViewById(R.id.lytSetBottomBarTabColor);

            cBoxEnableBottomBar = (CheckBox)findViewById(R.id.cBoxEnableBottomBarDrag);
            cBoxEnableBottomBarNotice = (CheckBox)findViewById(R.id.cBoxEnableBottomBarPopup);

            cBoxEnableGmail = (CheckBox)findViewById(R.id.cBoxEnableGmail);
            cBoxEnableSms = (CheckBox)findViewById(R.id.cBoxEnableSms);
            cBoxEnableMissedCall = (CheckBox)findViewById(R.id.cBoxEnableMissedCall);
            cBoxEnableCalendar = (CheckBox)findViewById(R.id.cBoxEnableEvents);
            cBoxEnableRSS = (CheckBox)findViewById(R.id.cBoxEnableRss);

            imgEventsSettings = (ImageView)findViewById(R.id.imgEnableEventsSettings);
            imgRssList = (ImageView)findViewById(R.id.imgRssList);
            imgRssSettings = (ImageView)findViewById(R.id.imgEnableRssSettings);

            btnTestSMS = (Button)findViewById(R.id.btnTestSMS);
            txtSetNoticeTextColor = (TextView)findViewById(R.id.txtSetNoticeTextColor);
            txtSetBottomBarTabColor = (TextView)findViewById(R.id.txtSetBottomBarTabColor);

            cBoxShowArrowUpIcon = (RadioButton)findViewById(R.id.rBtnShowArrowUpIcon);
            cBoxShowBatteryLevelBar = (RadioButton)findViewById(R.id.rBtnShowBatteryLevelBar);
            cBoxShowTransparentBar = (RadioButton)findViewById(R.id.rBtnShowBottomTransparentBar);

            viewBttyLevel = (View)findViewById(R.id.viewBttyLevelBar);

            onClickFunction();
            loadSettings();
            setLayoutVisibility();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
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
        return R.layout.settings_bottombar;
    }

    private void onClickFunction(){
        //bottom bar
        cBoxShowArrowUpIcon.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                //cBoxShowArrowUpIcon.setChecked(false);
                cBoxShowBatteryLevelBar.setChecked(false);
                cBoxShowTransparentBar.setChecked(false);
            }
        });
        cBoxShowBatteryLevelBar.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxShowArrowUpIcon.setChecked(false);
                //cBoxShowBatteryLevelBar.setChecked(false);
                cBoxShowTransparentBar.setChecked(false);
                Intent i = new Intent(SettingsBottombar.this,PickerColor.class);
                startActivityForResult(i,SELECTED_BTTY_LEVEL_COLOR);
            }
        });
        cBoxShowTransparentBar.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxShowArrowUpIcon.setChecked(false);
                cBoxShowBatteryLevelBar.setChecked(false);
                //cBoxFullyHideBottomBar.setChecked(false);
            }
        });
        viewBttyLevel.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxShowBatteryLevelBar.performClick();
            }
        });
        //bottom bar notices
        lytEnableBottomBarDrag.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                cBoxEnableBottomBar.performClick();
            }
        });
        cBoxEnableBottomBar.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                setLayoutVisibility();
            }
        });
        cBoxEnableCalendar.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEnableCalendar.isChecked()){
                    launchProSettings(PRO_ENABLE_EVENTS);
                }
            }
        });
        imgEventsSettings.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                launchProSettings(PRO_ENABLE_EVENTS_SETTIGNS);
            }
        });
        cBoxEnableRSS.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEnableRSS.isChecked()){
                    launchProSettings(PRO_RSS);
                }
            }
        });
        imgRssList.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                launchProSettings(PRO_RSS_LIST);
            }
        });
        imgRssSettings.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                launchProSettings(PRO_RSS_SETTINGS);
            }
        });
        btnTestSMS.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                if(isSMSUriValid()){
                    Toast.makeText(SettingsBottombar.this,getString(R.string.bottom_bar_test_sms_supported),Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(SettingsBottombar.this,getString(R.string.bottom_bar_test_sms_not_supported),Toast.LENGTH_LONG).show();
                    getAppHandler().sentLogs("SMS Notice Not Supported Information",errorMsg,true);
                }
            }
        });
        lytSetNoticeTextColor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsBottombar.this,PickerColor.class);
                startActivityForResult(i,SELECTED_CLOCK_TEXT_COLOR);
            }
        });
        lytSetBottomBarTabColor.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                Intent i = new Intent(SettingsBottombar.this,PickerColor.class);
                startActivityForResult(i,SELECTED_BOTTOM_BAR_TAB_COLOR);
            }
        });
    }

    private void loadSettings(){
        cBoxEnableBottomBar.setChecked(prefs.getBoolean("cBoxEnableBottomBar",false));
        cBoxEnableBottomBarNotice.setChecked(prefs.getBoolean("cBoxEnableBottomBarNotice",true));

        cBoxEnableGmail.setChecked(prefs.getBoolean("cBoxEnableGmail",true));
        cBoxEnableSms.setChecked(prefs.getBoolean("cBoxEnableSms",true));
        cBoxEnableMissedCall.setChecked(prefs.getBoolean("cBoxEnableMissedCall",true));
        cBoxEnableCalendar.setChecked(prefs.getBoolean("cBoxEnableCalendar",true));
        cBoxEnableRSS.setChecked(prefs.getBoolean("cBoxEnableRSS",true));

        cBoxShowArrowUpIcon.setChecked(prefs.getBoolean("cBoxShowArrowUpIcon",true));
        cBoxShowBatteryLevelBar.setChecked(prefs.getBoolean("cBoxShowBatteryLevelBar",false));
        cBoxShowTransparentBar.setChecked(prefs.getBoolean("cBoxShowTransparentBar",false));

        strColorBottomBarBttyLvl = prefs.getString(P.STR_COLOR_BATTERY_BAR,"888888");
        setBackGroundDrawable(true,viewBttyLevel,setRectangleColor(strColorBottomBarBttyLvl));
        strColorBottomBarNoticeText = prefs.getString(P.STR_COLOR_BOTTOMBAR_TEXT,"ffff00");
        txtSetNoticeTextColor.setTextColor(Color.parseColor("#"+strColorBottomBarNoticeText));
        strColorBottomBarTab = prefs.getString(P.STR_COLOR_BOTTOMBAR_TAB,"ffffff");
        txtSetBottomBarTabColor.setTextColor(Color.parseColor("#"+strColorBottomBarTab));
    }

    private void setLayoutVisibility(){
        if(cBoxEnableBottomBar.isChecked()){
            lytBottomBarNotice.setVisibility(View.VISIBLE);
        }else{
            lytBottomBarNotice.setVisibility(View.GONE);
        }
    }

    private void launchProSettings(int cBox){
        try{
            Intent i;
            switch(cBox){
                case PRO_ENABLE_EVENTS:
                    cBoxEnableCalendar.setChecked(true);
                    break;
                case PRO_ENABLE_EVENTS_SETTIGNS:
                    i = new Intent(this,SettingsCalendar.class);
                    startActivity(i);
                    break;
                case PRO_RSS:
                    cBoxEnableRSS.setChecked(true);
                    break;
                case PRO_RSS_LIST:
                    i = new Intent(this,ListRSS.class);
                    startActivity(i);
                    break;
                case PRO_RSS_SETTINGS:
                    i = new Intent(this,SettingsRSS.class);
                    startActivity(i);
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private boolean isSMSUriValid(){
        try{
            Cursor cursor = null;
            try{
                final Uri uri = Uri.parse(C.SMS_URI);
                final String[] projection = new String[]{"*"};
                final String selection = ("read"+"=?");
                final String[] arg = new String[]{"0"};//"1"=read,"0"=unread
                final String sort = "date DESC";//"startDay ASC, startMinute ASC"

                cursor = getContentResolver().query(uri,projection,selection,arg,sort);
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return true;
        }catch(IllegalArgumentException e){
            errorMsg = getStackTracetoString(e.getStackTrace());
            return false;
        }catch(IllegalStateException e){
            errorMsg = getStackTracetoString(e.getStackTrace());
            return false;
        }catch(NullPointerException e){
            errorMsg = getStackTracetoString(e.getStackTrace());
            return false;
        }catch(Exception e){
            errorMsg = getStackTracetoString(e.getStackTrace());
            return false;
        }
    }

    private void setBackGroundDrawable(final boolean bln,final View v,final Drawable d){
        if(bln){
            v.setBackground(d);
        }else{
            v.setBackground(null);
        }
    }

    private Drawable setRectangleColor(String strColor){
        GradientDrawable gd = null;
        try{
            int color01 = Color.parseColor("#"+strColor);
            int color02 = Color.parseColor("#cc"+strColor);
            int color03 = Color.parseColor("#dd"+strColor);
            gd = new GradientDrawable(Orientation.BOTTOM_TOP,new int[]{color02,color01,color03});
            gd.setShape(GradientDrawable.RECTANGLE);
        }catch(Exception e){
            saveErrorLogs(null,e);
            return null;
        }
        return gd;
    }

    private String getStackTracetoString(StackTraceElement[] stackTraceElements){
        if(stackTraceElements==null){
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(StackTraceElement element : stackTraceElements){
            stringBuilder.append(element.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case SELECTED_CLOCK_TEXT_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strColorBottomBarNoticeText = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffff00");
                        txtSetNoticeTextColor.setTextColor(Color.parseColor("#"+strColorBottomBarNoticeText));
                        myAlertDialog.colorTheme(strColorBottomBarNoticeText);
                    }
                    break;
                case SELECTED_BOTTOM_BAR_TAB_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strColorBottomBarTab = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"ffffff");
                        txtSetBottomBarTabColor.setTextColor(Color.parseColor("#"+strColorBottomBarTab));
                        myAlertDialog.colorTheme(strColorBottomBarTab);
                    }
                    break;
                case SELECTED_BTTY_LEVEL_COLOR:
                    if(resultCode==Activity.RESULT_OK){
                        strColorBottomBarBttyLvl = data.getExtras().getString(P.STR_COLOR_LOCKER_MAIN_TEXT,"888888");
                        setBackGroundDrawable(true,viewBttyLevel,setRectangleColor(strColorBottomBarBttyLvl));
                        myAlertDialog.colorTheme(strColorBottomBarBttyLvl);
                    }
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void saveSettings(){
        editor.putBoolean("cBoxEnableBottomBar",cBoxEnableBottomBar.isChecked());
        editor.putBoolean("cBoxEnableBottomBarNotice",cBoxEnableBottomBarNotice.isChecked());

        editor.putBoolean("cBoxEnableGmail",cBoxEnableGmail.isChecked());
        editor.putBoolean("cBoxEnableSms",cBoxEnableSms.isChecked());
        editor.putBoolean("cBoxEnableMissedCall",cBoxEnableMissedCall.isChecked());
        editor.putBoolean("cBoxEnableCalendar",cBoxEnableCalendar.isChecked());
        editor.putBoolean("cBoxEnableRSS",cBoxEnableRSS.isChecked());

        editor.putBoolean("cBoxShowArrowUpIcon",cBoxShowArrowUpIcon.isChecked());
        editor.putBoolean("cBoxShowBatteryLevelBar",cBoxShowBatteryLevelBar.isChecked());
        editor.putBoolean("cBoxShowTransparentBar",cBoxShowTransparentBar.isChecked());

        editor.putString(P.STR_COLOR_BOTTOMBAR_TEXT,strColorBottomBarNoticeText);
        editor.putString(P.STR_COLOR_BOTTOMBAR_TAB,strColorBottomBarTab);
        editor.putString(P.STR_COLOR_BATTERY_BAR,strColorBottomBarBttyLvl);

        editor.commit();
    }
}