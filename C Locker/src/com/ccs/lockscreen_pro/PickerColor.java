package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.P;

public class PickerColor extends Activity{
    private View imageV01, imageV02, imageV03, imageV04, imageV05, imageV06, imageV07, imageV08, imageV09, imageV10, imageV11, imageV12, imageV13, imageV14, imageV15, imageV16, imageV17, imageV18, imageV19, imageV20, imageV21, imageV22, imageV23, imageV24;
    private String
            //white, black
            strCode01 = "ffffff", strCode02 = "000000", strCode03 = "b2b2b2", strCode04 = "888888", //yellow
            strCode05 = "ffff00", strCode06 = "ffbb33", strCode07 = "ff8800", //red
            strCode08 = "ff0000", strCode09 = "ff4444", strCode10 = "cc0000", //blue
            strCode11 = "00ffff", strCode12 = "33b5e5", strCode13 = "0386dd", strCode14 = "0099cc", strCode15 = "6666ff", strCode16 = "0000ff", //pink
            strCode17 = "ed08a3", strCode18 = "ff00ff", strCode19 = "aa66cc", strCode20 = "9933cc", strCode21 = "8000ff", //green
            strCode22 = "00ff00", strCode23 = "99cc00", strCode24 = "669900";
    private EditText eTxt;
    private Button btnSaveColor;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_color);

        imageV01 = (View)findViewById(R.id.lyt_color_picker_imageView01);
        imageV02 = (View)findViewById(R.id.lyt_color_picker_imageView02);
        imageV03 = (View)findViewById(R.id.lyt_color_picker_imageView03);
        imageV04 = (View)findViewById(R.id.lyt_color_picker_imageView04);
        imageV05 = (View)findViewById(R.id.lyt_color_picker_imageView05);
        imageV06 = (View)findViewById(R.id.lyt_color_picker_imageView06);
        imageV07 = (View)findViewById(R.id.lyt_color_picker_imageView07);
        imageV08 = (View)findViewById(R.id.lyt_color_picker_imageView08);
        imageV09 = (View)findViewById(R.id.lyt_color_picker_imageView09);
        imageV10 = (View)findViewById(R.id.lyt_color_picker_imageView10);
        imageV11 = (View)findViewById(R.id.lyt_color_picker_imageView11);
        imageV12 = (View)findViewById(R.id.lyt_color_picker_imageView12);
        imageV13 = (View)findViewById(R.id.lyt_color_picker_imageView13);
        imageV14 = (View)findViewById(R.id.lyt_color_picker_imageView14);
        imageV15 = (View)findViewById(R.id.lyt_color_picker_imageView15);
        imageV16 = (View)findViewById(R.id.lyt_color_picker_imageView16);
        imageV17 = (View)findViewById(R.id.lyt_color_picker_imageView17);
        imageV18 = (View)findViewById(R.id.lyt_color_picker_imageView18);
        imageV19 = (View)findViewById(R.id.lyt_color_picker_imageView19);
        imageV20 = (View)findViewById(R.id.lyt_color_picker_imageView20);
        imageV21 = (View)findViewById(R.id.lyt_color_picker_imageView21);
        imageV22 = (View)findViewById(R.id.lyt_color_picker_imageView22);
        imageV23 = (View)findViewById(R.id.lyt_color_picker_imageView23);
        imageV24 = (View)findViewById(R.id.lyt_color_picker_imageView24);

        eTxt = (EditText)findViewById(R.id.editTxtColorCodes);
        btnSaveColor = (Button)findViewById(R.id.btnSaveColor);

        buttonImageV01();
        buttonImageV02();
        buttonImageV03();
        buttonImageV04();
        buttonImageV05();
        buttonImageV06();
        buttonImageV07();
        buttonImageV08();
        buttonImageV09();
        buttonImageV10();
        buttonImageV11();
        buttonImageV12();
        buttonImageV13();
        buttonImageV14();
        buttonImageV15();
        buttonImageV16();
        buttonImageV17();
        buttonImageV18();
        buttonImageV19();
        buttonImageV20();
        buttonImageV21();
        buttonImageV22();
        buttonImageV23();
        buttonImageV24();
        buttonCustomColor();
    }

    private void buttonImageV01(){
        imageV01.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode01);
            }
        });
    }

    private void buttonImageV02(){
        imageV02.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode02);
            }
        });
    }

    private void buttonImageV03(){
        imageV03.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode03);
            }
        });
    }

    private void buttonImageV04(){
        imageV04.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode04);
            }
        });
    }

    private void buttonImageV05(){
        imageV05.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode05);
            }
        });
    }

    private void buttonImageV06(){
        imageV06.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode06);
            }
        });
    }

    private void buttonImageV07(){
        imageV07.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode07);
            }
        });
    }

    private void buttonImageV08(){
        imageV08.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode08);
            }
        });
    }

    private void buttonImageV09(){
        imageV09.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode09);
            }
        });
    }

    private void buttonImageV10(){
        imageV10.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode10);
            }
        });
    }

    private void buttonImageV11(){
        imageV11.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode11);
            }
        });
    }

    private void buttonImageV12(){
        imageV12.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode12);
            }
        });
    }

    private void buttonImageV13(){
        imageV13.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode13);
            }
        });
    }

    private void buttonImageV14(){
        imageV14.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode14);
            }
        });
    }

    private void buttonImageV15(){
        imageV15.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode15);
            }
        });
    }

    private void buttonImageV16(){
        imageV16.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode16);
            }
        });
    }

    private void buttonImageV17(){
        imageV17.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode17);
            }
        });
    }

    private void buttonImageV18(){
        imageV18.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode18);
            }
        });
    }

    private void buttonImageV19(){
        imageV19.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode19);
            }
        });
    }

    private void buttonImageV20(){
        imageV20.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode20);
            }
        });
    }

    private void buttonImageV21(){
        imageV21.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode21);
            }
        });
    }

    private void buttonImageV22(){
        imageV22.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode22);
            }
        });
    }

    private void buttonImageV23(){
        imageV23.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode23);
            }
        });
    }

    private void buttonImageV24(){
        imageV24.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                setWidgetColor(strCode24);
            }
        });
    }

    private void buttonCustomColor(){
        btnSaveColor.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                try{
                    String str = eTxt.getText().toString();
                    if(str.length()==6){
                        Color.parseColor("#"+str);
                        setWidgetColor(str);
                        return;
                    }
                }catch(Exception e){
                }
                Toast.makeText(PickerColor.this,getString(R.string.picker_color_custom_color_wrong_text),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setWidgetColor(String str){
        Intent i = new Intent();
        i.putExtra(P.STR_COLOR_LOCKER_MAIN_TEXT,str);
        setResult(Activity.RESULT_OK,i);
        finish();
    }
}
