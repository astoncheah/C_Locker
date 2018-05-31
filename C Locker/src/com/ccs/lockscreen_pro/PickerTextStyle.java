package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ccs.lockscreen.R;

public class PickerTextStyle extends Activity{
    private String[] str = new String[]{"Default","txtStyle01.ttf","txtStyle02.ttf","txtStyle03.ttf","txtStyle04.ttf","txtStyle05.ttf","txtStyle06.ttf","txtStyle07.ttf","txtStyle08.ttf","txtStyle09.ttf","txtStyle10.ttf","txtStyle11.ttf","txtStyle12.ttf","txtStyle13.ttf","txtStyle14.ttf","txtStyle15.ttf","txtStyle16.ttf","txtStyle17.ttf","txtStyle18.ttf","txtStyle19.ttf","txtStyle20.ttf","txtStyle21.ttf","txtStyle22.ttf","txtStyle23.ttf","txtStyle24.ttf","txtStyle25.ttf","txtStyle26.ttf"};
    private TextView[] txt = new TextView[str.length];

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_text_style);

        txt[0] = (TextView)findViewById(R.id.txt_fontStyle00);
        txt[1] = (TextView)findViewById(R.id.txt_fontStyle01);
        txt[2] = (TextView)findViewById(R.id.txt_fontStyle02);
        txt[3] = (TextView)findViewById(R.id.txt_fontStyle03);
        txt[4] = (TextView)findViewById(R.id.txt_fontStyle04);
        txt[5] = (TextView)findViewById(R.id.txt_fontStyle05);
        txt[6] = (TextView)findViewById(R.id.txt_fontStyle06);
        txt[7] = (TextView)findViewById(R.id.txt_fontStyle07);
        txt[8] = (TextView)findViewById(R.id.txt_fontStyle08);
        txt[9] = (TextView)findViewById(R.id.txt_fontStyle09);
        txt[10] = (TextView)findViewById(R.id.txt_fontStyle10);
        txt[11] = (TextView)findViewById(R.id.txt_fontStyle11);
        txt[12] = (TextView)findViewById(R.id.txt_fontStyle12);
        txt[13] = (TextView)findViewById(R.id.txt_fontStyle13);
        txt[14] = (TextView)findViewById(R.id.txt_fontStyle14);
        txt[15] = (TextView)findViewById(R.id.txt_fontStyle15);
        txt[16] = (TextView)findViewById(R.id.txt_fontStyle16);
        txt[17] = (TextView)findViewById(R.id.txt_fontStyle17);
        txt[18] = (TextView)findViewById(R.id.txt_fontStyle18);
        txt[19] = (TextView)findViewById(R.id.txt_fontStyle19);
        txt[20] = (TextView)findViewById(R.id.txt_fontStyle20);
        txt[21] = (TextView)findViewById(R.id.txt_fontStyle21);
        txt[22] = (TextView)findViewById(R.id.txt_fontStyle22);
        txt[23] = (TextView)findViewById(R.id.txt_fontStyle23);
        txt[24] = (TextView)findViewById(R.id.txt_fontStyle24);
        txt[25] = (TextView)findViewById(R.id.txt_fontStyle25);
        txt[26] = (TextView)findViewById(R.id.txt_fontStyle26);

        for(int i = 0; i<txt.length; i++){
            buttonImage(txt[i],str[i]);
        }
    }

    @Override
    protected void onDestroy(){
        txt = null;
        super.onDestroy();
    }

    private void buttonImage(TextView v,final String code){
        if(code.equals(str[0])){
            v.setTypeface(Typeface.DEFAULT);
        }else{
            v.setTypeface(Typeface.createFromAsset(getAssets(),code));
        }
        v.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setTextStyle(code);
            }
        });
    }

    private void setTextStyle(String str){
        Intent i = new Intent();
        i.putExtra("strTextStyleLocker",str);
        setResult(Activity.RESULT_OK,i);
        finish();
    }
}
