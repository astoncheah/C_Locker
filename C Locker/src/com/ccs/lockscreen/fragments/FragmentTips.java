package com.ccs.lockscreen.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;

public final class FragmentTips extends Fragment{
    private static final String PAGE_NO = "pageNo";
    private static final String CHANGE_LOGS = "C Locker Change Logs: ";
    private static final String CHANGE_LOGS_DETAIL = "8.1.0:"+
            "\n-Added: Smart Unlock for Wifi, Bluetooth and Location profiles in [Locker Profiles>Smart Unlock] settings"+

            "\n\n8.0.5:"+
            "\n-Added: Widget Grid Settings in [Widget Profiles] Settings"+
            "\n-Added: Backup/Restore settings to/from Google Drive option"+
            "\n-Added: Enable Service Accessibility option to Block Home, Recent Apps Keys"+
            "\n-Added: Widget can now be put in full screen"+
            "\n-updated: : wallpaper to be set in widget profiles settings"+
            "\n-added: block navikey on locker"+
            "\n-improved overall performance and ram usage";
    private Context context;
    private LinearLayout lytTips;
    private int pageNo = 0;

    public static FragmentTips newInstance(int noPage){
        FragmentTips f = new FragmentTips();
        Bundle b = new Bundle();
        b.putInt(PAGE_NO,noPage);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();

        final View v = inflater.inflate(R.layout.tips_sub,container,false);
        v.setBackgroundColor(Color.WHITE);
        lytTips = (LinearLayout)v.findViewById(R.id.lytTips);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        if(b!=null && b.containsKey(PAGE_NO)){
            pageNo = b.getInt(PAGE_NO);
            //Log.e("FragmentTips","newInstance>onActivityCreated: "+pageNo);
            try{
                switch(pageNo){
                    case 0:
                        lytTips.addView(img(R.drawable.qg_00));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page1_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page1_2)));
                        lytTips.addView(textTitle(CHANGE_LOGS));
                        lytTips.addView(textDesc(CHANGE_LOGS_DETAIL));
                        break;
                    case 1:
                        lytTips.addView(img(R.drawable.qg_01));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page2)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page2_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page2_2)));
                        break;
                    case 2:
                        lytTips.addView(img(R.drawable.qg_02_a));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page3_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page3_1)));
                        //
                        lytTips.addView(img(R.drawable.qg_02_b));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page3_2)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page3_2)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page3_3)));
                        //
                        lytTips.addView(img(R.drawable.qg_02_c));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page3_3)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page3_4)));
                        break;
                    case 3:
                        lytTips.addView(img(R.drawable.qg_03));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page4_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page4_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page4_2),"#2196F3"));
                        break;
                    case 4:
                        lytTips.addView(img(R.drawable.qg_04));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page5_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page5_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page5_2)));
                        break;
                    case 5:
                        lytTips.addView(img(R.drawable.qg_05));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page6_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page6_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page6_2)));
                        break;
                    case 6:
                        lytTips.addView(img(R.drawable.qg_06));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page7_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page7_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page7_2)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page7_3)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page7_4)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page7_5),"#2196F3"));
                        break;
                    case 7:
                        lytTips.addView(img(R.drawable.qg_07));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page8_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page8_1)));
                        break;
                    case 8:
                        lytTips.addView(img(R.drawable.qg_08));
                        lytTips.addView(textTitle(context.getString(R.string.q_title_page9_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page9_1)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page9_2)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page9_3)));
                        lytTips.addView(textDesc(context.getString(R.string.q_desc_page9_4)));
                        break;
                } 
                /*lytTips.postDelayed(new Runnable(){
                    @Override
					public void run() {
						int w = C.pxToDp(context,lytTips.getChildAt(0).getWidth());
						int h = C.pxToDp(context,lytTips.getChildAt(0).getHeight());
						Toast.makeText(context,
								"w: "+w+"/"+
								"h: "+h,Toast.LENGTH_LONG).show();
					}             
                },1000);*/
            }catch(OutOfMemoryError e){
                //new MyCLocker(context).saveErrorLog(null,e);
                e.printStackTrace();
                Toast.makeText(context,"Not enough memory",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy(){
        for(int i = 0; i<lytTips.getChildCount(); i++){
            View v = lytTips.getChildAt(i);
            if(v instanceof ImageView){
                ((ImageView)v).setImageDrawable(null);
                /*Drawable drawable = ((ImageView) v).getDrawable();
                if(drawable instanceof BitmapDrawable) {
			        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			        if(bitmapDrawable != null) {
			        	//Log.e("FragmentTips","destroyItem>getDrawable>pageNo: "+pageNo);
			            Bitmap bitmap = bitmapDrawable.getBitmap();
			            if(bitmap != null && !bitmap.isRecycled()) bitmap.recycle();    
			        }
			    }*/
            }
        }
        super.onDestroy();
    }

    private ImageView img(int res){
        final ImageView v = new ImageView(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        v.setImageResource(res);
        v.setScaleType(ScaleType.FIT_CENTER);
        v.setAdjustViewBounds(true);
        //v.setBackgroundColor(Color.RED);
        return v;
    }

    private TextView textTitle(String title){
        final int mar = C.dpToPx(context,16);
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        pr.setMargins(mar,(mar/2),mar,(mar/2));

        final TextView v = new TextView(context);
        v.setLayoutParams(pr);
        v.setText(title);
        v.setTextColor(Color.BLACK);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
        return v;
    }

    private TextView textDesc(String desc){
        return textDesc(desc,null);
    }

    private TextView textDesc(String desc,String color){
        final int mar = C.dpToPx(context,16);
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        pr.setMargins(mar,(mar/2),mar,(mar/2));

        final TextView v = new TextView(context);
        v.setLayoutParams(pr);
        v.setText(desc);
        if(color==null){
            v.setTextColor(Color.parseColor("#888888"));
        }else{
            v.setTextColor(Color.parseColor(color));
        }
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        return v;
    }
}