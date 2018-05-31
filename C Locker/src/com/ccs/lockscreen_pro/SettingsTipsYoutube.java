package com.ccs.lockscreen_pro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

public class SettingsTipsYoutube extends BaseActivity{
    private TextView txt1, txt2, txt3, txt4, txt5, txt6, txt7, txt8;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.tips_tricks_youtube_0);
        setBasicBackKeyAction();

        final SharedPreferences.Editor editor = getSharedPreferences(C.PREFS_NAME,0).edit();
        editor.putBoolean("isTipsYoutubeWatched",true);
        editor.commit();

        try{
            txt1 = (TextView)this.findViewById(R.id.txtTips1);
            txt2 = (TextView)this.findViewById(R.id.txtTips2);
            txt3 = (TextView)this.findViewById(R.id.txtTips3);
            txt4 = (TextView)this.findViewById(R.id.txtTips4);
            txt5 = (TextView)this.findViewById(R.id.txtTips5);
            txt6 = (TextView)this.findViewById(R.id.txtTips6);
            txt7 = (TextView)this.findViewById(R.id.txtTips7);
            txt8 = (TextView)this.findViewById(R.id.txtTips8);

            onClick();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.tips_youtube;
    }

    private void onClick(){
        txt1.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=41t5bE9i1R0")));
            }
        });
        txt2.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=ZgqvXfFaeNo")));
            }
        });
        txt3.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=Qu6McPrQkQ8")));
            }
        });
        txt4.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=wrVmYh-YqzI")));
            }
        });
        txt5.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=kIZJuHuyiEk")));
            }
        });
        txt6.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=w0MD_Ii5Xmc")));
            }
        });
        txt7.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=Abqh-itWmVI")));
            }
        });
        txt8.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=05UOQ-O-WCg")));
            }
        });
    }
}
