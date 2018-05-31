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

public final class FragmentUnlockSelection extends Fragment{
    private static final String PAGE_NO = "pageNo";
    private Context context;
    private LinearLayout lytMain;

    public static FragmentUnlockSelection newInstance(int noPage){
        FragmentUnlockSelection f = new FragmentUnlockSelection();
        Bundle b = new Bundle();
        b.putInt(PAGE_NO,noPage);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        context = getActivity();
        lytMain = lytMain();
        return lytMain;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Bundle b = getArguments();
        if(b!=null && b.containsKey(PAGE_NO)){
            int pageNo = b.getInt(PAGE_NO);
            try{
                String str;
                switch(pageNo){
                    case 0:
                        str = "1)"+context.getString(R.string.lolipop_unlock_desc1)+"\n2)"+getString(R.string.lolipop_unlock_desc2)+"\n3)"+context.getString(R.string.lolipop_unlock_desc3)+"\n4)"+context.getString(R.string.lolipop_unlock_desc4);
                        lytMain.addView(textTitle(R.string.lolipop_unlock));
                        lytMain.addView(textDesc(str,R.drawable.unlock_samsung));
                        lytMain.addView(img(R.drawable.unlock_samsung));
                        break;
                    case 1:
                        str = "1)"+context.getString(R.string.gesture_unlock_desc1)+"\n2)"+context.getString(R.string.lolipop_unlock_desc4);
                        lytMain.addView(textTitle(R.string.gesture_unlock));
                        lytMain.addView(textDesc(str,R.drawable.unlock_gesture));
                        lytMain.addView(img(R.drawable.unlock_gesture));
                        break;
                    case 2:
                        str = "1)"+context.getString(R.string.fingerprint_unlock_desc1)+"\n2)"+context.getString(R.string.fingerprint_unlock_desc2)+"\n3)"+context.getString(R.string.fingerprint_unlock_desc3);
                        lytMain.addView(textTitle(R.string.fingerprint_unlock));
                        lytMain.addView(textDesc(str,R.drawable.unlock_fingerprint));
                        lytMain.addView(img(R.drawable.unlock_fingerprint));
                        break;
                    case 3:
                        str = context.getString(R.string.center_ring_desc);
                        lytMain.addView(textTitle(R.string.center_ring));
                        lytMain.addView(textDesc(str,R.drawable.unlock_center_ring));
                        lytMain.addView(img(R.drawable.unlock_center_ring));
                        break;
                    case 4:
                        str = context.getString(R.string.center_ring_desc);
                        lytMain.addView(textTitle(R.string.bottom_ring));
                        lytMain.addView(textDesc(str,R.drawable.unlock_bottom_ring));
                        lytMain.addView(img(R.drawable.unlock_bottom_ring));
                        break;
                    case 5:
                        str = context.getString(R.string.center_ring_desc);
                        lytMain.addView(textTitle(R.string.bottom_triagle));
                        lytMain.addView(textDesc(str,R.drawable.unlock_bottom_triangle));
                        lytMain.addView(img(R.drawable.unlock_bottom_triangle));
                        break;
                }
            }catch(OutOfMemoryError e){
                //new MyCLocker(context).saveErrorLog(null,e);
                e.printStackTrace();
                Toast.makeText(context,"Not enough memory",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy(){
        for(int i = 0; i<lytMain.getChildCount(); i++){
            View v = lytMain.getChildAt(i);
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

    private TextView textTitle(int title){
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

    private TextView textDesc(String desc,int imgRes){
        return textDesc(desc,null,imgRes);
    }

    private ImageView img(int res){
        final ImageView v = new ImageView(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        v.setImageResource(res);
        v.setScaleType(ScaleType.FIT_CENTER);
        v.setAdjustViewBounds(true);
        //v.setBackgroundColor(Color.RED);
        return v;
    }

    private TextView textDesc(String desc,String color,int imgRes){
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
        //v.setBackgroundResource(imgRes);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        return v;
    }

    private LinearLayout lytMain(){
        final LinearLayout v = new LinearLayout(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        v.setOrientation(LinearLayout.VERTICAL);
        v.setBackgroundColor(Color.WHITE);
        return v;
    }
}