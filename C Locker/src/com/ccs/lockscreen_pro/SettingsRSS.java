package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DownloadRSS;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class SettingsRSS extends BaseActivity{
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private LinearLayout lytCustomRSS, lytPresetRSS;
    private RadioGroup rBtnGroupRSS, rBtnGroupRssDisplayType;
    private RadioButton rBtnCustomRSS, rBtnPresetRSS, rBtn01, rBtn02, rBtn03, rBtn04, rBtn05, rBtn06, rBtn07, rBtn08, rBtn09, rBtn10, rBtn11, rBtn12, rBtn13, rBtn14, rBtn15, rBtn16, rBtn17, rBtn18, rBtn19, rBtn20, rBtn21, rBtn22, rBtn23, rBtn24;
    private Spinner spnRefreshTimeRSS;
    private CheckBox cBoxShowRSSUpdateNotice;
    private EditText edText;
    private Button btnTestRSS;
    private int intSpnUpdateInterval;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_widget_rss_list);
        setBasicBackKeyAction();

        try{
            cBoxShowRSSUpdateNotice = (CheckBox)findViewById(R.id.cBox_showRSSRefreshNotice);
            rBtnGroupRssDisplayType = (RadioGroup)findViewById(R.id.radioGroupRssDisplayType);

            rBtnCustomRSS = (RadioButton)findViewById(R.id.rBtnCustomRSS);
            rBtnPresetRSS = (RadioButton)findViewById(R.id.rBtnPresetRSS);

            lytCustomRSS = (LinearLayout)findViewById(R.id.lytCustomRSS);
            lytPresetRSS = (LinearLayout)findViewById(R.id.lytPresetRSS);
            edText = (EditText)findViewById(R.id.edTextTestRSS);
            btnTestRSS = (Button)findViewById(R.id.btnTestRSS);
            loadspinner();
            loadRadioTexts();
            loadOnClick();
            loadSettings();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy(){
        try{
            Intent i = new Intent(getPackageName()+C.UPDATE_CONFIGURE);
            sendBroadcast(i);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_rss;
    }

    private void loadspinner(){
        spnRefreshTimeRSS = (Spinner)findViewById(R.id.spinner_timeFormat);
        List<String> list1 = new ArrayList<String>();
        list1.add(getString(R.string.never));
        list1.add(getString(R.string.every_1_hour));
        list1.add(getString(R.string.every_2_hour));
        list1.add(getString(R.string.every_3_hour));
        list1.add(getString(R.string.every_6_hour));
        list1.add(getString(R.string.every_12_hour));
        list1.add(getString(R.string.once_a_day));
        ArrayAdapter<String> dataAdapterFormatTime = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list1);
        dataAdapterFormatTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRefreshTimeRSS.setAdapter(dataAdapterFormatTime);
        spnRefreshTimeRSS.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0,View arg1,int arg2,long arg3){
                switch(spnRefreshTimeRSS.getSelectedItemPosition()){
                    case 0:
                        intSpnUpdateInterval = 0;
                        break;
                    case 1:
                        intSpnUpdateInterval = 1;
                        break;
                    case 2:
                        intSpnUpdateInterval = 2;
                        break;
                    case 3:
                        intSpnUpdateInterval = 3;
                        break;
                    case 4:
                        intSpnUpdateInterval = 6;
                        break;
                    case 5:
                        intSpnUpdateInterval = 12;
                        break;
                    case 6:
                        intSpnUpdateInterval = 24;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0){
            }
        });
    }

    private void loadRadioTexts(){
        rBtnGroupRSS = (RadioGroup)findViewById(R.id.radioGroupRSS);
        rBtn01 = (RadioButton)findViewById(R.id.radio01);
        rBtn02 = (RadioButton)findViewById(R.id.radio02);
        rBtn03 = (RadioButton)findViewById(R.id.radio03);
        rBtn04 = (RadioButton)findViewById(R.id.radio04);
        rBtn05 = (RadioButton)findViewById(R.id.radio05);
        rBtn06 = (RadioButton)findViewById(R.id.radio06);
        rBtn07 = (RadioButton)findViewById(R.id.radio07);
        rBtn08 = (RadioButton)findViewById(R.id.radio08);
        rBtn09 = (RadioButton)findViewById(R.id.radio09);
        rBtn10 = (RadioButton)findViewById(R.id.radio10);
        rBtn11 = (RadioButton)findViewById(R.id.radio11);
        rBtn12 = (RadioButton)findViewById(R.id.radio12);
        rBtn13 = (RadioButton)findViewById(R.id.radio13);
        rBtn14 = (RadioButton)findViewById(R.id.radio14);
        rBtn15 = (RadioButton)findViewById(R.id.radio15);
        rBtn16 = (RadioButton)findViewById(R.id.radio16);
        rBtn17 = (RadioButton)findViewById(R.id.radio17);
        rBtn18 = (RadioButton)findViewById(R.id.radio18);
        rBtn19 = (RadioButton)findViewById(R.id.radio19);
        rBtn20 = (RadioButton)findViewById(R.id.radio20);
        rBtn21 = (RadioButton)findViewById(R.id.radio21);
        rBtn22 = (RadioButton)findViewById(R.id.radio22);
        rBtn23 = (RadioButton)findViewById(R.id.radio23);
        rBtn24 = (RadioButton)findViewById(R.id.radio24);

        rBtn01.setText("Top Stories");
        rBtn02.setText("World");
        rBtn03.setText("U.S.");
        rBtn04.setText("Europe");
        rBtn05.setText("Asia");
        rBtn06.setText("Americas");
        rBtn07.setText("Africa");
        rBtn08.setText("Middle East");
        rBtn09.setText("Business");
        rBtn10.setText("Technology");
        rBtn11.setText("World Sport");
        rBtn12.setText("Entertainment");
        rBtn13.setText("Top Stories");
        rBtn14.setText("World");
        rBtn15.setText("US & Canada");
        rBtn16.setText("Europe");
        rBtn17.setText("Asia");
        rBtn18.setText("Latin America");
        rBtn19.setText("Africa");
        rBtn20.setText("Middle East");
        rBtn21.setText("Business");
        rBtn22.setText("Technology");
        rBtn23.setText("Sport");
        rBtn24.setText("Entertainment & Arts");
    }

    private void loadOnClick(){
        rBtnCustomRSS.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                rBtnPresetRSS.setChecked(false);
                lytCustomRSS.setVisibility(View.VISIBLE);
                lytPresetRSS.setVisibility(View.GONE);
            }
        });
        rBtnPresetRSS.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                rBtnCustomRSS.setChecked(false);
                lytCustomRSS.setVisibility(View.GONE);
                lytPresetRSS.setVisibility(View.VISIBLE);
                setResult(Activity.RESULT_OK);
            }
        });
        btnTestRSS.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(edText.length()!=0){
                    saveSettings();
                    new DownloadRSS(SettingsRSS.this,true).execute();
                }else{
                    Toast.makeText(SettingsRSS.this,getString(R.string.empty_rss_url),Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadSettings(){
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        spnRefreshTimeRSS.setSelection(prefs.getInt("spnRefreshTimeRSS",0));
        rBtnGroupRssDisplayType.check(prefs.getInt("rBtnGroupRssDisplayType",R.id.rBtn_rssDisplayFastTop));
        rBtnGroupRSS.check(prefs.getInt("rBtnGroupRSS",R.id.radio01));
        intSpnUpdateInterval = prefs.getInt("intSpnUpdateInterval",0);
        cBoxShowRSSUpdateNotice.setChecked(prefs.getBoolean("cBoxShowRSSUpdateNotice",true));
        rBtnCustomRSS.setChecked(prefs.getBoolean("rBtnCustomRSS",false));
        rBtnPresetRSS.setChecked(prefs.getBoolean("rBtnPresetRSS",true));
        edText.setText(prefs.getString("getRSSCustomLink",""));

        if(rBtnCustomRSS.isChecked()){
            lytPresetRSS.setVisibility(View.GONE);
        }else{
            lytCustomRSS.setVisibility(View.GONE);
        }
    }

    private void saveSettings(){
        editor = getSharedPreferences(C.PREFS_NAME,0).edit();
        editor.putInt("spnRefreshTimeRSS",spnRefreshTimeRSS.getSelectedItemPosition());
        editor.putInt("rBtnGroupRssDisplayType",rBtnGroupRssDisplayType.getCheckedRadioButtonId());
        editor.putInt("rBtnGroupRSS",rBtnGroupRSS.getCheckedRadioButtonId());
        editor.putInt("intRssDisplayType",getRssDisplayType(rBtnGroupRssDisplayType.getCheckedRadioButtonId()));
        editor.putString("getRSSLink",getRssLink(rBtnGroupRSS.getCheckedRadioButtonId()));
        editor.putBoolean("cBoxShowRSSUpdateNotice",cBoxShowRSSUpdateNotice.isChecked());
        editor.putBoolean("rBtnCustomRSS",rBtnCustomRSS.isChecked());
        editor.putBoolean("rBtnPresetRSS",rBtnPresetRSS.isChecked());
        editor.putString("getRSSCustomLink",edText.getText().toString());

        editor.putInt("intSpnUpdateInterval",intSpnUpdateInterval);
        editor.commit();
    }

    private int getRssDisplayType(int no){
        switch(no){
            case R.id.rBtn_rssDisplayFast:
                return 1;
            case R.id.rBtn_rssDisplaySlow:
                return 2;
            default:
                return 0;
        }
    }

    private String getRssLink(int no){
        switch(no){
            //CNN
            default:
                return "http://rss.cnn.com/rss/edition.rss";
            case R.id.radio02:
                return "http://rss.cnn.com/rss/edition_world.rss";
            case R.id.radio03:
                return "http://rss.cnn.com/rss/edition_us.rss";
            case R.id.radio04:
                return "http://rss.cnn.com/rss/edition_europe.rss";
            case R.id.radio05:
                return "http://rss.cnn.com/rss/edition_asia.rss";
            case R.id.radio06:
                return "http://rss.cnn.com/rss/edition_americas.rss";
            case R.id.radio07:
                return "http://rss.cnn.com/rss/edition_africa.rss";
            case R.id.radio08:
                return "http://rss.cnn.com/rss/edition_meast.rss";
            case R.id.radio09:
                return "http://rss.cnn.com/rss/edition_business.rss";
            case R.id.radio10:
                return "http://rss.cnn.com/rss/edition_technology.rss";
            case R.id.radio11:
                return "http://rss.cnn.com/rss/edition_sport.rss";
            case R.id.radio12:
                return "http://rss.cnn.com/rss/edition_entertainment.rss";
            //BBC
            case R.id.radio13:
                return "http://feeds.bbci.co.uk/news/rss.xml";
            case R.id.radio14:
                return "http://feeds.bbci.co.uk/news/world/rss.xml";
            case R.id.radio15:
                return "http://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml";
            case R.id.radio16:
                return "http://feeds.bbci.co.uk/news/world/europe/rss.xml";
            case R.id.radio17:
                return "http://feeds.bbci.co.uk/news/world/asia/rss.xml";
            case R.id.radio18:
                return "http://feeds.bbci.co.uk/news/world/latin_america/rss.xml";
            case R.id.radio19:
                return "http://feeds.bbci.co.uk/news/world/africa/rss.xml";
            case R.id.radio20:
                return "http://feeds.bbci.co.uk/news/world/middle_east/rss.xml";
            case R.id.radio21:
                return "http://feeds.bbci.co.uk/news/business/rss.xml";
            case R.id.radio22:
                return "http://feeds.bbci.co.uk/news/technology/rss.xml";
            case R.id.radio23:
                return "http://feeds.bbci.co.uk/sport/0/rss.xml?edition=uk";
            case R.id.radio24:
                return "http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml";
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}
