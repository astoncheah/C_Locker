/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.viewpagerindicator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;

import java.io.File;

public class TabPageIndicator extends HorizontalScrollView implements PageIndicator{
    private static final CharSequence EMPTY_TITLE = "";
    private SharedPreferences prefs;
    private Runnable mTabSelector;
    private LinearLayout mTabLayout;
    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mMaxTabWidth;
    private int mSelectedTabIndex, textSize = 14, textHeight = LayoutParams.WRAP_CONTENT;
    private OnTabReselectedListener mTabReselectedListener;
    private Context context;
    //private Typeface mTypeface;
    private String strColorBottomBarNoticeText;
    private boolean isWhiteTab = false;
    private OnClickListener mTabClickListener = new OnClickListener(){
        public void onClick(View view){
            TabView tabView = (TabView)view;
            final int oldSelected = mViewPager.getCurrentItem();
            final int newSelected = tabView.getIndex();
            mViewPager.setCurrentItem(newSelected);
            if(oldSelected==newSelected && mTabReselectedListener!=null){
                mTabReselectedListener.onTabReselected(newSelected);
            }
            if(oldSelected==newSelected){
                runApps(tabView.getId());
            }
        }
    };

    public TabPageIndicator(Context context){
        this(context,null);
    }

    public TabPageIndicator(Context context,AttributeSet attrs){
        super(context,attrs);
        setHorizontalScrollBarEnabled(false);
        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

        mTabLayout = new LinearLayout(context);
        //mTabLayout.setBackgroundResource(R.drawable.vpi__tab_unselected_holo);
        //setBackGroundDrawable(mTabLayout,getDrawable(context,C.TAB_UNSELECTED,R.drawable.vpi__tab_unselected_holo));
        addView(mTabLayout,new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
    }

    //
    @Override
    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final boolean lockedExpanded = widthMode==MeasureSpec.EXACTLY;
        setFillViewport(lockedExpanded);

        final int childCount = mTabLayout.getChildCount();
        if(childCount>1 && (widthMode==MeasureSpec.EXACTLY || widthMode==MeasureSpec.AT_MOST)){
            if(childCount>2){
                mMaxTabWidth = (int)(MeasureSpec.getSize(widthMeasureSpec)*0.4f);
            }else{
                mMaxTabWidth = MeasureSpec.getSize(widthMeasureSpec)/2;
            }
        }else{
            mMaxTabWidth = -1;
        }

        final int oldWidth = getMeasuredWidth();
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        final int newWidth = getMeasuredWidth();

        if(lockedExpanded && oldWidth!=newWidth){
            // Recenter the tab display if we're at a new (scrollable) size.
            setCurrentItem(mSelectedTabIndex);
        }
    }

    private void animateToTab(final int position){
        final View tabView = mTabLayout.getChildAt(position);
        if(mTabSelector!=null){
            removeCallbacks(mTabSelector);
        }
        mTabSelector = new Runnable(){
            public void run(){
                final int scrollPos = tabView.getLeft()-(getWidth()-tabView.getWidth())/2;
                smoothScrollTo(scrollPos,0);
                mTabSelector = null;
            }
        };
        post(mTabSelector);
    }

    private void setBackGroundDrawable(final View v,final Drawable d){
        if(isWhiteTab){
            if(d!=null){
                ((TextView)v).setTextColor(Color.WHITE);
                v.setBackground(ContextCompat.getDrawable(context,R.drawable.tab_selected_white));
            }else{
                ((TextView)v).setTextColor(Color.parseColor("#cccccccc"));
                v.setBackground(d);
            }
        }else{
            if(d!=null){
                ((TextView)v).setTextColor(Color.parseColor("#"+strColorBottomBarNoticeText));
            }else{
                ((TextView)v).setTextColor(Color.parseColor("#cccccccc"));
            }
            v.setBackground(d);
        }
    }

    private Drawable getDrawable(Context context,String pngName,int idDrawable){
        try{
            Bitmap bm = null;
            final File file = new File(C.EXT_STORAGE_DIR,pngName);
            ;
            if(file.getAbsoluteFile().exists()){
                bm = BitmapFactory.decodeFile(file.getPath());
                return new BitmapDrawable(context.getResources(),bm);
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return ContextCompat.getDrawable(context,idDrawable);
    }

    public void setTextPadding(TabView tabView){
        tabView.setPadding(C.dpToPx(context,12),C.dpToPx(context,5),C.dpToPx(context,12),C.dpToPx(context,5));// tabView.setPadding(50,5,50,20);
    }

    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        if(mTabSelector!=null){
            // Re-post the selector we saved
            post(mTabSelector);
        }
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if(mTabSelector!=null){
            removeCallbacks(mTabSelector);
        }
    }

    @Override
    public void onPageScrolled(int arg0,float arg1,int arg2){
        if(mListener!=null){
            mListener.onPageScrolled(arg0,arg1,arg2);
        }
    }

    @Override
    public void onPageSelected(int pageIndex){
        setCurrentItem(pageIndex);
        if(mListener!=null){
            mListener.onPageSelected(pageIndex);
        }
    }

    //
    @Override
    public void onPageScrollStateChanged(int arg0){
        if(mListener!=null){
            mListener.onPageScrollStateChanged(arg0);
        }
    }

    @Override
    public void setViewPager(ViewPager view,Context context,Typeface mTypeface){
        this.context = context;
        //this.mTypeface = mTypeface;

        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        strColorBottomBarNoticeText = prefs.getString(P.STR_COLOR_BOTTOMBAR_TEXT,"ffff00");

        if(mViewPager==view){
            return;
        }
        if(mViewPager!=null){
            mViewPager.addOnPageChangeListener(null);
        }
        final PagerAdapter adapter = view.getAdapter();
        if(adapter==null){
            throw new IllegalStateException("ViewPager does not have adapter instance. TabPageIndicator(line: 189)");
        }
        mViewPager = view;
        view.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    //
    @Override
    public void setViewPager(ViewPager view,int initialPosition,Context context,Typeface mTypeface){
        setViewPager(view,context,mTypeface);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int pageIndex){
        if(mViewPager==null){
            throw new IllegalStateException("ViewPager has not been bound. TabPageIndicator(line: 198)");
        }
        mSelectedTabIndex = pageIndex;
        mViewPager.setCurrentItem(pageIndex);

        final int tabCount = mTabLayout.getChildCount();
        for(int i = 0; i<tabCount; i++){
            final TabView tabView = (TabView)mTabLayout.getChildAt(i);
            final boolean isSelected = (i==pageIndex);
            tabView.setSelected(isSelected);
            if(isSelected){
                animateToTab(pageIndex);
                setBackGroundDrawable(tabView,getDrawable(context,C.TAB_SELECTED,R.drawable.tab_selected_white));
            }else{
                setBackGroundDrawable(tabView,null);
            }
            setTextPadding(tabView);
        }
    }

    public void notifyDataSetChanged(){
        mTabLayout.removeAllViews();
        PagerAdapter adapter = mViewPager.getAdapter();
        IconPagerAdapter iconAdapter = null;
        if(adapter instanceof IconPagerAdapter){
            iconAdapter = (IconPagerAdapter)adapter;
        }
        final int count = adapter.getCount();
        for(int i = 0; i<count; i++){
            CharSequence title = adapter.getPageTitle(i);
            if(title==null){
                title = EMPTY_TITLE;
            }
            int iconResId = 0;
            if(iconAdapter!=null){
                iconResId = iconAdapter.getIconResId(i);
            }
            addTab(i,title,iconResId);
        }
        if(mSelectedTabIndex>count){
            mSelectedTabIndex = count-1;
        }
        setCurrentItem(mSelectedTabIndex);
        requestLayout();
    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener){
        mListener = listener;
    }

    public void setBackgroundColor(String color){
        setBackgroundColor(Color.parseColor(color));
    }

    public void setTextSize(int size){
        textSize = size;
    }

    public void setTextHeight(int h){
        textHeight = h;
    }

    public void setWhiteTab(){
        isWhiteTab = true;
    }

    //
    public int getCurrentPage(){
        return mSelectedTabIndex;
    }

    public void changeTitle(int id,String notice){
        final int tabCount = mTabLayout.getChildCount();
        for(int i = 0; i<tabCount; i++){
            final TabView tabView = (TabView)mTabLayout.getChildAt(i);
            final boolean isIdMatched = (tabView.getId()==id);
            if(isIdMatched){
                tabView.setText(context.getString(id)+notice);
                if(!notice.equals("")){
                    if(id==R.string.viewpager_call_log){
                        tabView.setTextColor(Color.RED);
                    }else{
                        tabView.setTextColor(Color.parseColor("#"+strColorBottomBarNoticeText));
                    }
                }else{
                    tabView.setTextColor(Color.parseColor("#cccccccc"));
                }
                setTextPadding(tabView);
            }
        }
    }

    private int getIdIndex(CharSequence text){
        if(text.toString().equals(context.getString(R.string.viewpager_gmail))){
            return R.string.viewpager_gmail;
        }else if(text.toString().equals(context.getString(R.string.viewpager_sms))){
            return R.string.viewpager_sms;
        }else if(text.toString().equals(context.getString(R.string.viewpager_call_log))){
            return R.string.viewpager_call_log;
        }else if(text.toString().equals(context.getString(R.string.viewpager_events))){
            return R.string.viewpager_events;
        }else if(text.toString().equals(context.getString(R.string.viewpager_rss))){
            return R.string.viewpager_rss;
        }else{
            return 0;
        }
    }

    private void runApps(int id){
        if(id==R.string.viewpager_events){
            try{
                Intent i = new Intent(context.getPackageName()+C.PAGER_TAB_NOTICE);
                i.putExtra(C.TAB_ACTION,C.LAUNCH_CALENDAR);
                context.sendBroadcast(i);
            }catch(Exception e){
                Toast.makeText(context,"Calendar Launch Error",Toast.LENGTH_SHORT).show();
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }else if(id==R.string.viewpager_gmail){
            try{
                Intent i = new Intent(context.getPackageName()+C.PAGER_TAB_NOTICE);
                i.putExtra(C.TAB_ACTION,C.LAUNCH_GMAIL);
                context.sendBroadcast(i);
            }catch(Exception e){
                Toast.makeText(context,"Gmail Launch Error",Toast.LENGTH_SHORT).show();
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }else if(id==R.string.viewpager_call_log){
            try{
                Intent i = new Intent(context.getPackageName()+C.PAGER_TAB_NOTICE);
                i.putExtra(C.TAB_ACTION,C.LAUNCH_CALLLOG);
                context.sendBroadcast(i);
            }catch(Exception e){
                Toast.makeText(context,"Call Log Launch Error",Toast.LENGTH_SHORT).show();
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }else if(id==R.string.viewpager_sms){
            try{
                Intent i = new Intent(context.getPackageName()+C.PAGER_TAB_NOTICE);
                i.putExtra(C.TAB_ACTION,C.LAUNCH_SMS);
                context.sendBroadcast(i);
            }catch(Exception e){
                Toast.makeText(context,"SMS Launch Error",Toast.LENGTH_SHORT).show();
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }else if(id==R.string.viewpager_rss){
            try{
                Intent i = new Intent(context.getPackageName()+C.PAGER_TAB_NOTICE);
                i.putExtra(C.TAB_ACTION,C.LAUNCH_RSS);
                context.sendBroadcast(i);
            }catch(Exception e){
                Toast.makeText(context,"RSS Launch Error",Toast.LENGTH_SHORT).show();
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }
    }

    private void addTab(int index,CharSequence text,int iconResId){
        final TabView tabView = new TabView(getContext());
        tabView.mIndex = index;
        tabView.setId(getIdIndex(text));
        tabView.setFocusable(true);
        tabView.setOnClickListener(mTabClickListener);
        tabView.setText(text);
        tabView.setTextColor(Color.WHITE);
        //tabView.setTypeface(mTypeface,Typeface.BOLD);
        tabView.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
        tabView.setGravity(Gravity.CENTER_HORIZONTAL);
        setTextPadding(tabView);
        if(iconResId!=0){
            tabView.setCompoundDrawablesWithIntrinsicBounds(iconResId,0,0,0);
        }

        final int mar = C.dpToPx(context,5);
        int height = LayoutParams.WRAP_CONTENT;
        if(textHeight!=LayoutParams.WRAP_CONTENT){
            height = C.dpToPx(context,textHeight);
        }
        final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,height,1);//dont put "0" (width)
        pr.setMargins(mar,0,mar,0);
        mTabLayout.addView(tabView,pr);
    }

    public void setOnTabReselectedListener(OnTabReselectedListener listener){
        mTabReselectedListener = listener;
    }

    public interface OnTabReselectedListener{
        /**
         * Callback when the selected tab has been reselected.
         *
         * @param position Position of the current center item.
         */
        void onTabReselected(int position);
    }

    //
    private class TabView extends TextView{
        private int mIndex;

        public TabView(Context context){
            super(context);
        }

        @Override
        public void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            // Re-measure if we went beyond our maximum size.
            if(mMaxTabWidth>0 && getMeasuredWidth()>mMaxTabWidth){
                super.onMeasure(MeasureSpec.makeMeasureSpec(mMaxTabWidth,MeasureSpec.EXACTLY),heightMeasureSpec);
            }
        }

        public int getIndex(){
            return mIndex;
        }
    }
}