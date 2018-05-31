package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.fragments.FragmentTips;
import com.ccs.lockscreen.utils.BaseActivity;
import com.viewpagerindicator.LinePageIndicator;

public class SettingsTips extends BaseActivity{
    private static final int PAGES = 9;
    private FragmentStatePagerAdapter adapter;
    private ViewPager pager;
    private LinePageIndicator indicator;
    private TextView btnLeft, btnRight;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name)+" "+getString(R.string.quick_guide));
        setBasicBackKeyAction();
        try{
            //getWindow().setFlags(
            //WindowManager.LayoutParams.FLAG_FULLSCREEN,
            //WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //getSupportActionBar().hide();

            btnLeft = (TextView)findViewById(R.id.btnLeft);
            btnRight = (TextView)findViewById(R.id.btnRight);

            btnLeft.setTextColor(Color.BLACK);
            btnRight.setTextColor(Color.BLACK);

            btnLeft.setBackgroundColor(Color.WHITE);
            btnRight.setBackgroundColor(Color.WHITE);

            btnLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            btnRight.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

            btnLeft.setText(R.string.skip);
            btnRight.setText(R.string.start);

            adapter = new LineFragmentAdapter(getSupportFragmentManager());

            pager = (ViewPager)findViewById(R.id.pagerTips);
            //pager.setOffscreenPageLimit(1);
            pager.setAdapter(adapter);

            indicator = (LinePageIndicator)findViewById(R.id.indicatorTips);
            indicator.setViewPager(pager,this,null);
            indicator.setClickable(false);
            indicator.setBackgroundColor(Color.WHITE);
            onClickFunction();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy(){
        System.gc();
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.tips;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClickFunction(){
        pager.addOnPageChangeListener(new OnPageChangeListener(){
            @Override
            public void onPageScrolled(int arg0,float arg1,int arg2){
            }

            @Override
            public void onPageSelected(int page){
                indicator.setCurrentItem(page);
                if(page==0){
                    btnLeft.setText(R.string.skip);
                    btnRight.setText(R.string.start);
                }else if(page==(PAGES-1)){
                    btnRight.setText(R.string.lets_start);
                }else{
                    btnLeft.setText(R.string.previous);
                    btnRight.setText(R.string.next);
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0){
            }
        });
        btnLeft.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent m){
                switch(m.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        btnLeft.setBackgroundColor(Color.GRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnLeft.setBackgroundColor(Color.WHITE);
                        if(pager.getCurrentItem()>0){
                            pager.setCurrentItem(pager.getCurrentItem()-1);
                        }else{
                            finish();
                        }
                        break;
                }
                return true;
            }
        });
        btnRight.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent m){
                switch(m.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        btnRight.setBackgroundColor(Color.GRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnRight.setBackgroundColor(Color.WHITE);
                        if(pager.getCurrentItem()<(PAGES-1)){
                            pager.setCurrentItem(pager.getCurrentItem()+1);
                        }else{
                            finish();
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed(){
    }

    private class LineFragmentAdapter extends FragmentStatePagerAdapter{
        public LineFragmentAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            return FragmentTips.newInstance(position);
        }

        @Override
        public int getCount(){
            return PAGES;
        }
        /*@Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        	final FragmentTips fragment = (FragmentTips) object;
        	final View view = (View) fragment.getView();
        	final LinearLayout lytTips = (LinearLayout)view.findViewById(R.id.lytTips);                
    		for(int i=0;i<lytTips.getChildCount();i++){
    			View v = lytTips.getChildAt(i);
    			if(v instanceof ImageView){
    			    Drawable drawable = ((ImageView) v).getDrawable();
    			    if(drawable instanceof BitmapDrawable) {
    			        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
    			        if(bitmapDrawable != null) {
    			        	//Log.e("LineFragmentAdapter","destroyItem>getDrawable>pageNo: "+position);
    			            Bitmap bitmap = bitmapDrawable.getBitmap();
    			            if(bitmap != null && !bitmap.isRecycled()) bitmap.recycle();    
    			        }
    			    }
    			}
    		}
            //((ViewPager)container).removeView(view);
        }*/
    }
}
