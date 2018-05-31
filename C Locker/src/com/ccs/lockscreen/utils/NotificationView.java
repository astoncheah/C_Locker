package com.ccs.lockscreen.utils;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.appwidget.LockerAppWidgetHostView;
import com.ccs.lockscreen.appwidget.LockerWidgetLayout;
import com.ccs.lockscreen.appwidget.MyWidgetHelper;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.T;
import com.ccs.lockscreen_pro.ServiceNotification;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationView{
    public static final int REMOVE_NOTICE = 1;
    public static final int OPEN_NOTICE = 2;
    public static final int ON_NEW_UPDATE = 1;
    public static final int ON_RESUME = 2;
    public static final int ON_TOUCH_DOWN = 3;
    public static final int ON_KEY_BACK = 4;
    public static final int ON_DRAG_ENDED = 5;
    public static final int ON_LAUNCH_ACTION = 6;
    private static final int CLOCK_ID = 100;
    private static final int COUNTER_ID = 200;
    private static final int TITLE_ID = 300;
    private static final int TIME_ID = 400;
    private static final int IMG_ID = 500;
    private static final int CONTENT_ID = 600;
    private static final int ACTION_ID = 700;
    private static final int EX_VIEW_ID = 2000;
    private static final int NOTICE_ICON_HEIGHT = 50;
    private static final int MINI_ICON_SIZE = 15;
    private static final int MINI_ICON_MARG = 3;
    private static final int PADDING_BG_IMG = 5;
    private static final int TEXT_TITLE = 16;
    private static final int TEXT_CONTENT = 13;
    private static final int ANIMATION_TIME = 250;
    private static final int MIN_TXT_LINE = 1;
    private static final int MAX_TXT_LINE = 10;
    private static final float CROP_SENDER = 0.8F;
    private static final float CROP_APP = 0.65F;//0.68 to fit play store
    private Context context;
    private MyViewGroupCallBack callBack;
    //private ArrayList<InfoAppsSelection> appNoticeList;
    private boolean cBoxNoticeStyleLollipop, isNoticeLayoutLeft, isNoticeLayoutBgEmpty, cBoxShowNoticeContent, cBoxShowSenderPicture;
    private float STARTING_X, STARTING_Y, MOVE_X, MOVE_Y;
    private boolean isSwipeNoticeUp = false, isSwipeNoticeDown = false, isSwipeNoticeLeft = false, isSwipeNoticeRight = false, isRemoveNotice = false, isOpenNotice = false;
    private String noticePkgName, noticeTag;
    private long noticeImgId, lastUpdateTime;
    private int intLockStyle, screenWidth, screenHeight, noticeViewWidth, noticeViewHeight, MAX_VIEW, COLLAPSED_HEIGHT = -1, EXPANDED_HEIGHT = -1, MOVE_GAP;
    private LayoutParams defaultParams;
    private LinearLayout actionNoticeView,directInputView;

    public NotificationView(Context context,MyViewGroupCallBack callBack){
        this.context = context;
        this.callBack = callBack;
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);

        cBoxNoticeStyleLollipop = prefs.getBoolean("cBoxNoticeStyleLollipop",true);
        isNoticeLayoutLeft = prefs.getBoolean("cBoxNoticeLayoutLeft",true);
        isNoticeLayoutBgEmpty = prefs.getBoolean("cBoxNoticeLayoutBgEmpty",true);
        cBoxShowNoticeContent = prefs.getBoolean("cBoxShowNoticeContent",true);
        cBoxShowSenderPicture = prefs.getBoolean("cBoxShowSenderPicture",true);
        intLockStyle = prefs.getInt("intLockStyle",C.SIMPLE_UNLOCK);//1 = center, 2&3 = bottom
        //cBoxShowNoticeCount = prefs.getBoolean("cBoxShowNoticeCount",true);

        screenWidth = C.getRealDisplaySize(context).x;
        screenHeight = C.getRealDisplaySize(context).y;
        MOVE_GAP = C.dpToPx(context,MINI_ICON_SIZE);
    }
    public final String getNoticeTitle(String title,String pkg){
        return getNoticeTitle(title,pkg,false);
    }
    public final String getNoticeContent(String content){
        if(content==null || content.equals("") || !cBoxShowNoticeContent){
            return "";
        }
        return content;
    }
    public final String getNoticePostTime(long postTime,String pkgName){
        try{
            long now = System.currentTimeMillis();
            long gapTime = now-postTime;
            String pkg = getNoticeTitle(null,pkgName,true);
            String getDay = getDay(postTime);
            String today = context.getString(R.string.bottom_bar_today);

            if(isSecondsAgo(gapTime,60)){
                gapTime = gapTime/1000;
                if(gapTime==0){
                    //return getTime(postTime)+" - "+gapTime+" sec ago - "+pkg;
                    return getDay+" • now • "+pkg;
                }else if(gapTime<2){
                    //return getTime(postTime)+" - "+gapTime+" sec ago - "+pkg;
                    return getDay+" • "+gapTime+" sec ago • "+pkg;
                }else{
                    return getDay+" • "+gapTime+" secs ago • "+pkg;
                }
            }else if(isMinutesAgo(gapTime,60)){
                gapTime = gapTime/1000/60;
                if(gapTime<2){
                    return getDay+" • "+gapTime+" min ago • "+pkg;
                }else{
                    return getDay+" • "+gapTime+" mins ago • "+pkg;
                }
            }else if(isHoursAgo(gapTime,12)){
                gapTime = gapTime/1000/60/60;
                if(gapTime<2){
                    return getDay+" • "+gapTime+" hr ago • "+pkg;
                }else{
                    return getDay+" • "+gapTime+" hrs ago • "+pkg;
                }
            }else if(isToday(postTime)){
                return getDay+" • "+today+" • "+getTime(postTime)+" • "+pkg;
            }
            return getDay+getDate(postTime)+" • "+pkg;
        }catch(final Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public final String getNoticeTitle(String title,String pkg,boolean isPostTime){
        try{
            //Log.e("getNoticeTitle",""+title+"/"+pkg);
            if(isPostTime){
                final PackageManager pm = context.getPackageManager();
                final ApplicationInfo applicationInfo = pm.getApplicationInfo(pkg,0);
                return (String)pm.getApplicationLabel(applicationInfo);
            }
            if(title==null || title.equals("") || !cBoxShowNoticeContent){
                final PackageManager pm = context.getPackageManager();
                final ApplicationInfo applicationInfo = pm.getApplicationInfo(pkg,0);
                final String appName = (String)pm.getApplicationLabel(applicationInfo);
                if(cBoxNoticeStyleLollipop){
                    return T.SWIPE_TO_OPEN+" "+appName;
                }else{
                    return context.getString(R.string.notice_tap_to_open)+" "+appName;
                }
            }
        }catch(final NameNotFoundException e){
            if(isPostTime){
                return "";
            }
            if(!cBoxShowNoticeContent){
                if(cBoxNoticeStyleLollipop){
                    return T.SWIPE_TO_OPEN+" ";
                }else{
                    return context.getString(R.string.notice_tap_to_open)+" ";
                }
            }
        }
        return title;
    }
    private String getDay(final long date){
        final SimpleDateFormat dayFm = new SimpleDateFormat("EEE",Locale.getDefault());
        final String day = dayFm.format(new Date(date));
        Calendar cld = Calendar.getInstance();
        cld.setTimeInMillis(date);
        return day;
    }
    private boolean isSecondsAgo(long gapTime,int val){
        long VAL = val*1000;
        return gapTime<VAL;
    }
    private boolean isMinutesAgo(long gapTime,int val){
        long VAL = val*60000;
        return gapTime<VAL;
    }
    private boolean isHoursAgo(long gapTime,int val){
        long VAL = val*60000*60;
        return gapTime<VAL;
    }
    private boolean isToday(final long date){
        final int today;
        Calendar cld = Calendar.getInstance();
        today = cld.get(Calendar.DAY_OF_YEAR);
        cld.setTimeInMillis(date);

        return today==cld.get(Calendar.DAY_OF_YEAR);
    }
    private String getTime(final long time){
        final DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());//SHORT
        return tf.format(new Date(time));
    }
    private String getDate(final long date){
        //final SimpleDateFormat dayFm = new SimpleDateFormat("EEE",Locale.getDefault());
        //final String day = dayFm.format(new Date(date));
        final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
        return df.format(new Date(date));//+"\n("+day+")";

    }
    public final LinearLayout lytSideNotice(){
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(-2,-2);
        pr.addRule(getLayoutPosition(isNoticeLayoutLeft));
        pr.addRule(RelativeLayout.CENTER_VERTICAL);
        //pr.setMargins(0,C.dpToPx(this,10),0,C.dpToPx(this,20));

        final LinearLayout lytNotices = new LinearLayout(context);
        lytNotices.setLayoutParams(pr);
        lytNotices.setGravity(Gravity.CENTER);
        //lytNotices.setVerticalScrollBarEnabled(true);
        lytNotices.setOrientation(LinearLayout.VERTICAL);
        lytNotices.setVisibility(View.INVISIBLE);
        lytNotices.setPadding(C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5),C.dpToPx(context,5));
        if(!isNoticeLayoutBgEmpty){
            if(isNoticeLayoutLeft){
                lytNotices.setBackgroundResource(R.drawable.shape_notice_bg_left);
            }else{
                lytNotices.setBackgroundResource(R.drawable.shape_notice_bg_right);
            }
        }
        return lytNotices;
    }
    private int getLayoutPosition(final boolean isLeft){
        if(isLeft){//in reverse
            return RelativeLayout.ALIGN_PARENT_RIGHT;
        }else{
            return RelativeLayout.ALIGN_PARENT_LEFT;
        }
    }
    public final LinearLayout lytNoticeDetail(){
        final RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        param.addRule(RelativeLayout.CENTER_VERTICAL);
        param.setMargins(C.dpToPx(context,getLayoutMarginWidth(isNoticeLayoutLeft,true)),0,C.dpToPx(context,getLayoutMarginWidth(isNoticeLayoutLeft,false)),0);

        final LinearLayout lytNoticePost = new LinearLayout(context);
        lytNoticePost.setLayoutParams(param);
        lytNoticePost.setPadding(C.dpToPx(context,10),C.dpToPx(context,20),C.dpToPx(context,10),C.dpToPx(context,20));
        lytNoticePost.setOrientation(LinearLayout.VERTICAL);
        lytNoticePost.setGravity(Gravity.CENTER);
        lytNoticePost.setVisibility(View.GONE);
        lytNoticePost.addView(txtNoticePostTime());
        lytNoticePost.addView(txtNoticePostTitle());
        lytNoticePost.addView(txtNoticePostContent());
        callBack.addDragListener(lytNoticePost);
        return lytNoticePost;
    }
    private int getLayoutMarginWidth(boolean isNoticeLayoutLeft,final boolean isLeft){
        if(isNoticeLayoutLeft){
            if(isLeft){
                return 70;
            }else{
                return 15;
            }
        }else{
            if(isLeft){
                return 15;
            }else{
                return 70;
            }
        }
    }
    //notice details
    private TextView txtNoticePostTime(){
        final TextView txt = new TextView(context);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,TEXT_CONTENT);
        txt.setGravity(Gravity.CENTER);
        txt.setEllipsize(TruncateAt.END);
        txt.setMaxLines(1);
        txt.setAlpha(0.5f);
        return txt;
    }
    private TextView txtNoticePostTitle(){
        final TextView txt = new TextView(context);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        txt.setPadding(0,C.dpToPx(context,2),0,0);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,TEXT_TITLE);
        txt.setGravity(Gravity.CENTER);
        txt.setEllipsize(TruncateAt.END);
        txt.setMaxLines(1);
        return txt;
    }
    private TextView txtNoticePostContent(){
        final TextView txt = new TextView(context);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));
        txt.setPadding(0,C.dpToPx(context,2),0,0);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,TEXT_CONTENT);
        txt.setGravity(Gravity.CENTER);
        txt.setEllipsize(TruncateAt.END);
        txt.setMaxLines(2);
        return txt;
    }
    //lollipop view
    public final void loadLollipopNoticeView(MyWidgetHelper myWidgetHelper,
        final RelativeLayout lytWidgets,ArrayList<InfoAppsSelection> appNoticeList,int state,long updateTime){
        if(lytWidgets!=null){
            //Log.e("loadLollipopNoticeView","/"+state);
            hideKeyboard();
            lytWidgets.setLayoutTransition(null);

            final boolean isNewNotice = lastUpdateTime!=updateTime;
            final int maxSpanX = C.getMaxWidgetSpanX(context);
            final int padding = C.dpToPx(context,NOTICE_ICON_HEIGHT)/5;
            final int viewHeight = LayoutParams.WRAP_CONTENT;
            int count = 1;
            int extraViewCount = 1;
            int lastId = CLOCK_ID;
            boolean isEmptyNotice = true;

            MAX_VIEW = getMaxNoticeSpanY();
            noticeViewWidth = C.getSpanToPxX(context,maxSpanX);

            switch(state){
                case ON_NEW_UPDATE:
                case ON_DRAG_ENDED:
                case ON_LAUNCH_ACTION:
                    try{
                        final View clock = ((RelativeLayout)lytWidgets.getChildAt(0)).getChildAt(0);
                        if(clock!=null && clock.getId()==CLOCK_ID){
                            isEmptyNotice = false;
                        }
                    }catch(Exception ignored){
                    }
                    break;
                case ON_RESUME:
                    if(!isNewNotice){
                        RelativeLayout renderView = (RelativeLayout)lytWidgets.getChildAt(0);
                        if(renderView!=null){
                            renderView.setLayoutTransition(new LayoutTransition());
                            refreshTime(renderView);
                            return;
                        }
                    }
                    break;
                case ON_TOUCH_DOWN:
                case ON_KEY_BACK:
                    if(!isNewNotice){
                        final View clock = ((RelativeLayout)lytWidgets.getChildAt(0)).getChildAt(0);
                        if(clock!=null && clock.getId()==CLOCK_ID){
                            RelativeLayout renderView = (RelativeLayout)lytWidgets.getChildAt(0);
                            if(renderView!=null){
                                //Log.e("resetToDefaultview","resetToDefaultview"+"/"+isFirstPage);
                                renderView.setLayoutTransition(new LayoutTransition());
                                resetTxtExpanded(renderView);
                                refreshTime(renderView);
                                return;
                            }
                        }
                    }
                    break;
            }

            final RelativeLayout.LayoutParams shadowPr = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            final RelativeLayout renderView = new RelativeLayout(context);
            renderView.setLayoutParams(shadowPr);
            renderView.setPadding(0,0,0,padding*3);
            //renderView.setBackgroundColor(Color.RED);//testing
            renderView.setOnTouchListener(new OnTouchListener(){//prevent triggering locker ontouch action
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View v,MotionEvent m){
                    return true;
                }
            });//*/

            lytWidgets.removeAllViews();
            lytWidgets.addView(renderView);

            //add clock widget at top
            final View widgetclock = lytWidgetView(myWidgetHelper);
            if(isEmptyNotice){
                widgetclock.setVisibility(View.GONE);
            }
            renderView.addView(widgetclock,widgetclock.getLayoutParams());

            lytWidgets.setLayoutTransition(new LayoutTransition());
            for(int i = 0; i<appNoticeList.size(); i++){
                final InfoAppsSelection item = appNoticeList.get(i);
                final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(noticeViewWidth,viewHeight);
                pr.setMargins(padding,padding/2,padding,0);
                pr.addRule(RelativeLayout.BELOW,lastId);

                final RelativeLayout mainView = new RelativeLayout(context);
                final RelativeLayout noticeContent = lytNoticeInfoContent(item,i,false);

                mainView.setId(CLOCK_ID+count);
                mainView.setContentDescription(item.getAppKey());
                mainView.setLayoutParams(pr);
                mainView.setPadding(padding,padding,padding,padding);
                mainView.setClickable(true);
                mainView.setLayoutTransition(new LayoutTransition());
                mainView.addView(noticeContent);
                mainView.setOnTouchListener(new OnTouchListener(){
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v,MotionEvent m){
                        onNoticeTouch(v,m,item);
                        return false;
                    }
                });//*/
                mainView.setVisibility(View.GONE);

                C.setCustomBackgroundRipple(context,mainView);

                if(count==MAX_VIEW*extraViewCount){
                    final RelativeLayout extraView = mainExtraView(extraViewCount,lastId,appNoticeList,MAX_VIEW);
                    extraView.setOnTouchListener(new OnTouchListener(){
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouch(View v,MotionEvent m){
                            onNoticeTouch(v,m,item);
                            return false;
                        }
                    });//*/
                    extraView.setVisibility(View.GONE);
                    lastId = extraView.getId();
                    renderView.addView(extraView);
                    extraViewCount++;

                    pr.addRule(RelativeLayout.BELOW,lastId);
                    mainView.setLayoutParams(pr);
                }

                lastId = mainView.getId();
                renderView.addView(mainView);
                count++;
            }

            if(isNewNotice){
                lastUpdateTime = updateTime;
            }
            renderView.setLayoutTransition(new LayoutTransition());
            resetToDefaultview(renderView);
            refreshTime(renderView);
        }
    }
    @SuppressWarnings("rawtypes")
    private View lytWidgetView(MyWidgetHelper myWidgetHelper){
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final int intAppWidgetId = prefs.getInt("lytLollipopStyleWidget",AppWidgetManager.INVALID_APPWIDGET_ID);
        final LockerAppWidgetHostView hostView = myWidgetHelper.getHostViewSimple(intAppWidgetId,null);
        if(hostView!=null){
            hostView.setId(CLOCK_ID);
            if(!isScreenPortrait() && MAX_VIEW<3){
                final RelativeLayout.LayoutParams pr = (LayoutParams)hostView.getLayoutParams();
                pr.height = C.getSpanToPxY(context,1);
                hostView.setLayoutParams(pr);
            }
            return hostView;
        }
        //add default clock if no custom widget found
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(noticeViewWidth,C.getDefaultSpanToPxY(context,1));
        final LockerWidgetLayout mView = new LockerWidgetLayout<>(context,C.CLOCK_DIGITAL_CENTER,null);
        mView.setId(CLOCK_ID);
        mView.setX(0);
        mView.setY(0);
        mView.setSpanX(C.getMaxWidgetSpanX(context));
        mView.setSpanY(1);
        mView.setLayoutParams(pr);
        return mView;
    }
    private RelativeLayout lytNoticeInfoContent(InfoAppsSelection item,int index,boolean addCallBack){
        final RelativeLayout rl = new RelativeLayout(context);
        try{
            final long postTime = item.getAppPostTime();
            final String pkgName = item.getAppPkg();

            String title = item.getAppSubInfo();
            String content = item.getAppSubInfoContent();

            title = getNoticeTitle(title,pkgName);
            content = getNoticeContent(content);

            final int size = (int)(C.dpToPx(context,C.ICON_HEIGHT)*1.1);
            final RelativeLayout imgContentIcon = lytRelativeIcon(item,index,size,addCallBack,false);
            final TextView txtCounter = txtPostCountL(index,item.getAppPostCount());
            final TextView txtTitle = txtTitle(imgContentIcon,txtCounter,index,title,content);
            final TextView txtPostTime = new PostTimeTextView(context,txtTitle.getId(),txtTitle.getId(),index,postTime,pkgName);
            final TextView txtContent = txtContent(txtTitle.getId(),txtPostTime.getId(),index,content,false);
            final LinearLayout actionNoticeLayout = actionNoticeLayout(txtContent.getId(),index);
            final LinearLayout directInputLayout = directInputLayout(actionNoticeLayout.getId(),item);

            final LayoutParams pr = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
            pr.addRule(RelativeLayout.ALIGN_PARENT_TOP);

            rl.setId(getNoticeContentImgId(index));
            rl.setLayoutParams(pr);
            rl.addView(txtCounter);
            rl.addView(imgContentIcon);
            rl.addView(txtTitle);
            rl.addView(txtPostTime);
            rl.addView(txtContent);
            rl.addView(actionNoticeLayout);
            rl.addView(directInputLayout);

            if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
                rl.setElevation(4 * context.getResources().getDisplayMetrics().density);
            }

            addActionTitles(actionNoticeLayout,item);
        }catch(Exception e){
            Toast.makeText(context,"notification error",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return rl;
    }
    private TextView txtPostCountL(final int index,int postCount){
        final int size = C.dpToPx(context,MINI_ICON_SIZE);
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        if(postCount>9){
            pr = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,size);
        }
        pr.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        pr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        final TextView txt = new TextView(context);
        txt.setId(getNoticeContentImgId(index)+COUNTER_ID);
        txt.setLayoutParams(pr);
        txt.setText(postCount+"");
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
        txt.setTextColor(Color.WHITE);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        txt.setBackgroundResource(R.drawable.shape_notice_count);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(C.dpToPx(context,2),0,C.dpToPx(context,2),0);
        if(postCount==0){
            txt.setVisibility(View.INVISIBLE);
        }
        return txt;
    }
    private TextView txtTitle(View img,View counter,int index,String title,String content){
        int lytSize = LayoutParams.WRAP_CONTENT;
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(lytSize,lytSize);
        pr.addRule(RelativeLayout.RIGHT_OF,img.getId());
        pr.addRule(RelativeLayout.LEFT_OF,counter.getId());
        if(content==null){
            pr.addRule(RelativeLayout.CENTER_VERTICAL);
        }
        //int color = context.getResources().getColor(R.color.colorTextTitle);

        TextView v = new TextView(context);
        v.setId(getNoticeContentImgId(index)+TITLE_ID);
        v.setLayoutParams(pr);
        v.setEllipsize(TruncateAt.END);
        v.setMaxLines(1);
        v.setTextColor(Color.BLACK);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        //v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        //v.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        v.setText(title);
        v.setPadding(C.dpToPx(context,5),0,C.dpToPx(context,5),0);
        return v;
    }
    private TextView txtContent(int leftId,int belowId,int index,String content,boolean allAppNotice){
        if(content==null){
            content = "";
        }
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        pr.addRule(RelativeLayout.ALIGN_LEFT,leftId);
        pr.addRule(RelativeLayout.BELOW,belowId);

        int color = ContextCompat.getColor(context,R.color.colorTextDesc);
        TextView v = new TextView(context);
        v.setLayoutParams(pr);
        v.setId(getNoticeContentImgId(index)+CONTENT_ID);
        v.setEllipsize(TruncateAt.END);
        if(allAppNotice){
            v.setMaxLines(6);
        }else{
            v.setMaxLines(1);
        }
        v.setText(content);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        v.setTextColor(color);
        v.setPadding(C.dpToPx(context,5),0,C.dpToPx(context,5),0);
        return v;
    }
    private LinearLayout actionNoticeLayout(int belowId,int index){
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        pr.addRule(RelativeLayout.BELOW,belowId);
        //pr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        LinearLayout v = new LinearLayout(context);
        v.setLayoutParams(pr);
        v.setId(getNoticeContentImgId(index)+ACTION_ID);
        v.setOrientation(LinearLayout.HORIZONTAL);
        v.setVisibility(View.GONE);
        return v;
    }
    private LinearLayout directInputLayout(int belowId,final InfoAppsSelection item){
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        pr.addRule(RelativeLayout.BELOW,belowId);

        final String inputLabel = item.getAllowDirectInput();
        int pad = C.dpToPx(context,8);
        LinearLayout v = new LinearLayout(context);
        v.setPadding(pad,0,pad,0);
        v.setLayoutParams(pr);
        v.setGravity(Gravity.CENTER);
        v.setOrientation(LinearLayout.HORIZONTAL);
        v.setVisibility(View.GONE);

        if(inputLabel!=null){
            final EditText editText = new EditText(context);
            editText.setPadding(pad,pad,pad,pad);
            editText.setTextColor(Color.DKGRAY);
            editText.setHint("..........");
            //editText.setBackgroundColor(Color.LTGRAY);

            TextView textView = new TextView(context);
            textView.setPadding(pad,pad,pad,pad);
            textView.setText(inputLabel);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            textView.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    //editText.clearFocus();
                    //callBack.onLollipopNoticeTouchEvent(MotionEvent.ACTION_OUTSIDE,-1,item.getAppPkg(),inputLabel,noticeImgId);
                    hideKeyboard();

                    Intent i = new Intent(context.getPackageName()+C.NOTICE_LISTENER_SERVICE);
                    i.putExtra(C.NOTICE_COMMAND,C.RUN_ACTION_PENDING_INTENT);
                    i.putExtra(C.NOTICE_APP_PKG_NAME,item.getAppPkg());
                    i.putExtra(C.NOTICE_APP_PKG_ID,item.getAppPostTime());
                    i.putExtra(C.ACTION_PENDING_INTENT_TITLE,inputLabel);
                    i.putExtra(C.ACTION_PENDING_INTENT_MSG,editText.getText().toString());
                    context.sendBroadcast(i);
                }
            });

            if(inputLabel.length()<10){
                v.addView(editText,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
                v.addView(textView,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            }else{
                textView.setMaxLines(1);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                v.addView(editText,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,3));
                v.addView(textView,new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
            }
        }
        return v;
    }
    private void hideKeyboard(){
        View view = ((Activity)context).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void addActionTitles(LinearLayout lyt,InfoAppsSelection item){
        final ArrayList<String> actionTitles = item.getActionTitles();
        final ArrayList<Integer> actionIcons = item.getActionIcons();
        final String pkg = item.getAppPkg();
        final Long noticeImgId = item.getAppPostTime();

        //Log.e("addActionTitles",pkg+"/"+actionTitles+"/"+actionIcons);

        if(actionTitles==null && actionIcons==null){
            return;
        }
        if(actionTitles!=null){
            for(int i = 0; i<actionTitles.size(); i++){
                String title = actionTitles.get(i);
                int resId = actionIcons.get(i);
                if(title!=null){
                    View v = actionIcon(pkg,title,resId,noticeImgId);
                    if(v!=null){
                        lyt.addView(v);
                    }
                    lyt.addView(actionTitle(pkg,title,resId,noticeImgId));
                }
            }
        }else if(actionIcons!=null){
            for(int i = 0; i<actionIcons.size(); i++){
                Integer resId = actionIcons.get(i);
                View v = actionIcon(pkg,"",resId,noticeImgId);//dont know what to put title
                if(v!=null){
                    lyt.addView(v);
                }
            }
        }
    }
    private TextView actionTitle(final String pkg,final String text,int resId,final long noticeImgId){
        LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        if(resId==R.drawable.ic_not_interested_white_48dp){
            pr.weight = 1;
        }

        final int pad = C.dpToPx(context,10);

        TextView txt = new TextView(context);
        txt.setLayoutParams(pr);
        txt.setText(text);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(pad,0,pad,0);
        txt.setTextColor(Color.BLACK);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        if(resId==R.drawable.ic_not_interested_white_48dp){
            return txt;
        }
        txt.setBackground(ContextCompat.getDrawable(context,R.drawable.layout_selector));
        txt.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                callBack.onLollipopNoticeTouchEvent(MotionEvent.ACTION_OUTSIDE,-1,pkg,text,noticeImgId);
            }
        });
        return txt;
    }
    @SuppressWarnings("deprecation")
    private ImageView actionIcon(final String pkg,final String text,final int resId,final long noticeImgId){
        try{
            int pad = C.dpToPx(context,2);
            int size = getExpandTitleHeight();
            final LinearLayout.LayoutParams pr = new LinearLayout.LayoutParams(size,size);
            Resources appRes = context.getPackageManager().getResourcesForApplication(pkg);

            ImageView img = new ImageView(context);
            img.setLayoutParams(pr);
            img.setContentDescription(text);
            img.setPadding(pad,pad,pad,pad);
            img.setBackgroundResource(R.drawable.shape_round_grey);
            img.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    callBack.onLollipopNoticeTouchEvent(MotionEvent.ACTION_OUTSIDE,resId,pkg,text,noticeImgId);
                }
            });
            if(resId==R.drawable.ic_not_interested_white_48dp){
                img.setImageResource(resId);
            }else{
                img.setImageDrawable(appRes.getDrawable(resId));
            }
            return img;
        }catch(Exception ignored){
        }
        return null;
    }
    private RelativeLayout mainExtraView(int extraViewCount,int lastId,ArrayList<InfoAppsSelection> extraNotice,int MAX_VIEW){
        final int viewHeight = LayoutParams.WRAP_CONTENT;
        final int padding = C.dpToPx(context,NOTICE_ICON_HEIGHT)/5;
        final int MARGIN = 4;
        final int size = padding*MARGIN;
        final RelativeLayout extraView = new RelativeLayout(context);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(noticeViewWidth,viewHeight);
        pr.setMargins(padding,padding/2,padding,0);
        pr.addRule(RelativeLayout.BELOW,lastId);

        final LinearLayout lyt = lytExtraView(extraNotice,size,MAX_VIEW*extraViewCount);
        final TextView txt = txtPostCountL(-1,lyt.getChildCount());
        final ImageView img = imgArrowView();

        extraView.setId(EX_VIEW_ID+extraViewCount);
        extraView.setLayoutParams(pr);
        extraView.setPadding(padding,padding,padding,padding);
        extraView.setClickable(true);
        extraView.addView(lyt);
        extraView.addView(txt);
        extraView.addView(img);

        C.setCustomBackgroundRipple(context,extraView);
        return extraView;
    }
    private LinearLayout lytExtraView(ArrayList<InfoAppsSelection> extraNotice,int size,int MAX_VIEW){
        final LinearLayout v = new LinearLayout(context);
        for(int i = MAX_VIEW-1; i<extraNotice.size(); i++){
            v.addView(imgNoticeIcon(-1,extraNotice.get(i).getAppPkg(),(int)(size*0.8),"",false));
        }
        return v;
    }
    private ImageView imgArrowView(){
        final int size = C.dpToPx(context,MINI_ICON_SIZE);
        final float defaultY = (float)(size*1.2);
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        //pr.setMargins(0,C.dpToPx(context,2),-C.dpToPx(context,2),0);
        //pr.addRule(RelativeLayout.BELOW,id); cant use this due to animation
        pr.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        ImageView v = new ImageView(context);
        v.setY(defaultY);
        v.setLayoutParams(pr);
        v.setImageResource(R.drawable.ic_arrow_up);
        arrowAnimation(v,defaultY+(size/2));
        return v;
    }
    private void onNoticeTouch(final View v,MotionEvent m,InfoAppsSelection item){
        final int size = C.dpToPx(context,NOTICE_ICON_HEIGHT)/5;
        switch(m.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                hideKeyboard();

                STARTING_X = (int)m.getRawX();
                STARTING_Y = (int)m.getRawY();

                isSwipeNoticeLeft = false;
                isSwipeNoticeRight = false;
                isSwipeNoticeDown = false;
                isSwipeNoticeUp = false;
                isRemoveNotice = false;
                isOpenNotice = false;

                noticeViewHeight = v.getHeight();
                defaultParams = (LayoutParams)v.getLayoutParams();

                actionNoticeView = getActionNoticeView(v);
                directInputView = getDirectInputView(v);

                //Log.e("actionNoticeView",actionNoticeView+"/"+directInputView);
                if(v.getId()<EX_VIEW_ID){
                    if(COLLAPSED_HEIGHT==-1){//only need the 1st time value
                        COLLAPSED_HEIGHT = v.getHeight();
                    }
                    EXPANDED_HEIGHT = getExpandHeight(item.getAppSubInfoContent());
                }
                if(item!=null){
                    noticePkgName = item.getAppPkg();
                    noticeTag = item.getAppTag();
                    noticeImgId = item.getAppPostTime();
                }

                callBack.onLollipopNoticeTouchEvent(MotionEvent.ACTION_DOWN,-1,null,null,-1);
                break;
            case MotionEvent.ACTION_MOVE:
                MOVE_X = m.getRawX()-STARTING_X;
                MOVE_Y = m.getRawY()-STARTING_Y;

                if(MOVE_X>MOVE_GAP){
                    if(!isSwipeNoticeUp && !isSwipeNoticeDown){
                        isSwipeNoticeRight = true;
                        float mv = MOVE_X-MOVE_GAP+size;
                        float a = (float)(1-(mv/noticeViewWidth*0.75));
                        v.setX(mv);
                        v.setAlpha(a+0.2f);

                        if(MOVE_X>getSwipeWidth()){
                            isOpenNotice = true;
                        }
                    }
                }else if(MOVE_X<-MOVE_GAP){
                    if(!isSwipeNoticeUp && !isSwipeNoticeDown){
                        isSwipeNoticeLeft = true;
                        float mv = MOVE_X+MOVE_GAP+size;
                        float a = (float)(1-(mv/-(noticeViewWidth*0.75)));
                        v.setX(mv);
                        v.setAlpha(a+0.2f);

                        if(MOVE_X<-getSwipeWidth()){
                            isRemoveNotice = true;
                        }
                    }
                }
                if(MOVE_Y>MOVE_GAP*2){
                    if(!isSwipeNoticeLeft && !isSwipeNoticeRight){
                        isSwipeNoticeDown = true;
                        if(v.getId()>=EX_VIEW_ID){
                            //v.setY(STARTING_Y + MOVE_Y);
                        }else{
                            if(actionNoticeView!=null && !actionNoticeView.isShown()){
                                actionNoticeView.setVisibility(View.VISIBLE);
                            }
                            if(directInputView!=null && !directInputView.isShown()){
                                directInputView.setVisibility(View.VISIBLE);
                            }
                            expandTextView(v,MAX_TXT_LINE);
                            final int h = (int)(noticeViewHeight+MOVE_Y-MOVE_GAP*2);
                            final int max = EXPANDED_HEIGHT+C.dpToPx(context,30);
                            //if(h<(screenHeight*0.4)){
                            if(h<max){
                                v.getLayoutParams().height = h;
                            }
                        }
                    }
                }else if(MOVE_Y<-MOVE_GAP*2){
                    if(!isSwipeNoticeLeft && !isSwipeNoticeRight){
                        isSwipeNoticeUp = true;
                        if(v.getId()>=EX_VIEW_ID){
                            //v.setY(STARTING_Y + MOVE_Y);
                        }else{
                            int h = (int)(noticeViewHeight+MOVE_Y+MOVE_GAP*2);
                            if(h>150){
                                defaultParams.height = h;
                                v.setLayoutParams(defaultParams);
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                //Log.e("onNoticeTouch","/ACTION_UP");
                if(isRemoveNotice){
                    if(v.getId()<EX_VIEW_ID){
                        resetViewAnimation(v,-screenWidth,0,0,REMOVE_NOTICE);
                        break;
                    }
                }else if(isOpenNotice){
                    if(v.getId()<EX_VIEW_ID){
                        resetViewAnimation(v,screenWidth,0,0,OPEN_NOTICE);
                        break;
                    }
                }else if(isSwipeNoticeUp){
                    int id = v.getId();
                    if(id>=EX_VIEW_ID){
                        final RelativeLayout renderView = (RelativeLayout)v.getParent();
                        //resetTxtExpanded(renderView);
                        handleNoticeVisibility(id,true,renderView);
                    }else{
                        if(actionNoticeView!=null){
                            actionNoticeView.setVisibility(View.GONE);
                        }
                        if(directInputView!=null){
                            directInputView.setVisibility(View.GONE);
                        }
                        expandAnimation(v,false,COLLAPSED_HEIGHT,EXPANDED_HEIGHT);
                    }
                }else if(isSwipeNoticeDown){
                    int id = v.getId();
                    if(id>=EX_VIEW_ID){
                        final RelativeLayout renderView = (RelativeLayout)v.getParent();
                        if(id==EX_VIEW_ID+1){//first page extra view
                            resetToDefaultview(renderView);
                        }else{
                            handleNoticeVisibility(id,false,renderView);
                        }
                    }else{
                        if(actionNoticeView!=null){
                            actionNoticeView.setVisibility(View.VISIBLE);
                        }
                        if(directInputView!=null){
                            directInputView.setVisibility(View.VISIBLE);
                        }
                        expandTextView(v,MAX_TXT_LINE);
                        expandAnimation(v,true,COLLAPSED_HEIGHT,EXPANDED_HEIGHT);
                        //break;
                    }
                }
                resetViewAnimation(v,size,STARTING_Y-MOVE_Y,1,-1);
                actionNoticeView = null;
                directInputView = null;
                break;
        }
    }

    //side icon
    public final ImageView imgNotice(final int id,final int drawableId){
        final LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(C.dpToPx(context,50),C.dpToPx(context,50));
        final ImageView img = new ImageView(context);
        img.setLayoutParams(param);
        img.setId(id);
        img.setImageResource(drawableId);
        img.setPadding(C.dpToPx(context,8),C.dpToPx(context,8),C.dpToPx(context,8),C.dpToPx(context,8));
        callBack.addTouchListener(img);
        callBack.addDragListener(img);
        return img;
    }
    public final RelativeLayout lytRelativeIcon(InfoAppsSelection item,int index,int size,boolean addCallBack,boolean addPostCount){
        try{
            final String pkgName = item.getAppPkg();
            final long imgId = item.getAppPostTime();
            final int postCount = item.getAppPostCount();

            final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
            final RelativeLayout rl = new RelativeLayout(context);
            final String strIcon = getIconDir(pkgName,imgId);

            rl.setId(getNoticeContentImgId(index)+IMG_ID);
            rl.setLayoutParams(pr);
            rl.setContentDescription(pkgName);
            rl.addView(imgRingBackground(size));
            rl.addView(imgNoticeIcon(index,pkgName,-1,strIcon,addCallBack));
            if(addPostCount && postCount>1){
                rl.addView(txtPostCount(postCount));
            }

            final File fileIcon = new File(strIcon);
            if(fileIcon.exists() && cBoxShowSenderPicture){
                rl.addView(imgMiniRingBackground());
                rl.addView(imgMiniIcon(pkgName));
            }
            return rl;
        }catch(NameNotFoundException e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
        return null;
    }
    private ImageView imgRingBackground(int size){
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        final ImageView img = new ImageView(context);
        //final File fileIcon = new File(strIcon);

        //if(fileIcon.isFile()){
        Bitmap bmIcon = getRoundIconBackground(size);
        img.setImageBitmap(handleImage(bmIcon));
        //}
        img.setLayoutParams(pr);
        int padding = C.dpToPx(context,PADDING_BG_IMG);
        img.setPadding(padding,padding,padding,padding);
        return img;
    }
    private ImageView imgNoticeIcon(int currentNoticeIndex,String pkgName,int size,String strIcon,boolean addCallBack){
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);

        ImageView img = new ImageView(context);
        img.setId(getNoticeContentImgId(currentNoticeIndex));
        img.setLayoutParams(pr);
        img.setContentDescription(pkgName);
        setNoticeIcon(img,pkgName,strIcon);

        if(addCallBack){
            callBack.addTouchListener(img);
            callBack.addDragListener(img);
        }
        return img;
    }
    private TextView txtPostCount(int postCount){
        final int size = C.dpToPx(context,MINI_ICON_SIZE);
        final int margin = C.dpToPx(context,MINI_ICON_MARG);
        RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        if(postCount>9){
            pr = new RelativeLayout.LayoutParams(-2,size);
        }
        pr.setMargins(margin,margin,margin,0);
        pr.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        pr.addRule(getLayoutPosition(!isNoticeLayoutLeft));

        final TextView txt = new TextView(context);
        txt.setLayoutParams(pr);
        txt.setText(postCount+"");
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
        txt.setTextColor(Color.WHITE);
        txt.setTypeface(Typeface.DEFAULT_BOLD);
        //txt.setBackgroundColor(Color.RED);
        txt.setPadding(C.dpToPx(context,2),0,C.dpToPx(context,2),0);
        txt.setBackgroundResource(R.drawable.shape_notice_count);
        txt.setGravity(Gravity.CENTER);
        return txt;
    }
    private ImageView imgMiniRingBackground(){
        final int size = C.dpToPx(context,MINI_ICON_SIZE);
        final int margin = C.dpToPx(context,MINI_ICON_MARG);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        pr.setMargins(margin,margin,margin,margin);
        pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        pr.addRule(getLayoutPosition(!isNoticeLayoutLeft));

        final ImageView img = new ImageView(context);
        Bitmap bmIcon = getRoundIconBackground(size);
        img.setImageBitmap(handleImage(bmIcon));
        img.setLayoutParams(pr);
        return img;
    }
    private ImageView imgMiniIcon(final String pkgName) throws NameNotFoundException{
        final int size = C.dpToPx(context,MINI_ICON_SIZE);
        final int margin = C.dpToPx(context,MINI_ICON_MARG);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        pr.setMargins(margin,margin,margin,margin);
        pr.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        pr.addRule(getLayoutPosition(!isNoticeLayoutLeft));

        final PackageManager pm = context.getPackageManager();
        final Drawable d = pm.getApplicationIcon(pkgName);
        final ImageView img = new ImageView(context);
        img.setLayoutParams(pr);
        img.setImageBitmap(handleImage(cropAppImage(d,CROP_APP)));
        //img.setImageDrawable(drawableIcon);
        return img;
    }
    //tools
    private void setNoticeIcon(ImageView img,String pkgName,String strIcon){
        try{
            final PackageManager pm = context.getPackageManager();
            final File fileIcon = new File(strIcon);
            final int ROUND_MARGIN = 2;
            int padding;
            if(fileIcon.exists() && cBoxShowSenderPicture){
                padding = C.dpToPx(context,PADDING_BG_IMG);
                padding = padding+C.dpToPx(context,ROUND_MARGIN);

                final Bitmap b = BitmapFactory.decodeFile(strIcon);
                img.setImageBitmap(handleImage(cropAppImage(b,CROP_SENDER)));
                //Log.e("setNoticeIcon 1 ",b.getHeight()+"/"+b.getWidth());
            }else{
                padding = C.dpToPx(context,PADDING_BG_IMG);
                padding = padding+C.dpToPx(context,ROUND_MARGIN);

                //final Drawable d = pm.getApplicationInfo(pkgName,0).loadIcon(pm);
                final Drawable d = pm.getApplicationIcon(pkgName);
                img.setImageBitmap(handleImage(cropAppImage(d,CROP_APP)));
            }
            img.setPadding(padding,padding,padding,padding);
        }catch(NameNotFoundException e){
            e.printStackTrace();
        }
    }
    private int getNoticeContentImgId(final int currentNoticeIndex){
        return currentNoticeIndex+C.NOTICE_IMG_ID;
    }
    private Bitmap getRoundIconBackground(int size){
        try{
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap bmp = Bitmap.createBitmap(size,size,conf); // this creates a MUTABLE bitmap
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            return bmp;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private Bitmap cropAppImage(Drawable bmIcon,float cropSize){
        Bitmap bm;
        if(bmIcon instanceof BitmapDrawable){
            bm = ((BitmapDrawable)bmIcon).getBitmap();
        }else{
            bm = Bitmap.createBitmap(bmIcon.getIntrinsicWidth(),bmIcon.getIntrinsicHeight(),Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            bmIcon.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
            bmIcon.draw(canvas);
        }
        return cropAppImage(bm,cropSize);
    }
    private Bitmap cropAppImage(Bitmap bmIcon,float cropSize){
        //Log.e("cropAppImage 1",bmIcon.getWidth()+"/"+bmIcon.getHeight());
        if(bmIcon==null){
            return null;
        }
        if(bmIcon.getWidth()>192 && bmIcon.getHeight()>192){
            bmIcon = Bitmap.createScaledBitmap(bmIcon,192,192,true);
        }
        int w = (int)(bmIcon.getWidth()*cropSize);
        int h = (int)(bmIcon.getHeight()*cropSize);
        int startX = (bmIcon.getWidth()-w)/2;
        int startY = (bmIcon.getHeight()-h)/2;
        //Log.e("cropAppImage 2",resizedbitmap.getWidth()+"/"+resizedbitmap.getHeight());
        return Bitmap.createBitmap(bmIcon,startX,startY,w,h);
    }
    private Bitmap handleImage(Bitmap bmIcon){
        if(bmIcon==null){
            return null;
        }
        return new BitmapUtils(context).getRoundedCornerBitmap(bmIcon,C.ICON_HEIGHT);
    }
    private String getIconDir(String pkgName,long postTime){
        return C.EXT_STORAGE_DIR+"/"+pkgName+"_"+postTime+ServiceNotification.IMAGE_FORMAT;
    }
    private int getSwipeWidth(){
        return noticeViewWidth/2;
    }
    private int getExpandTitleHeight(){
        final float WIDTH = 0.7f;
        TextView txt1 = new TextView(context);
        txt1.setText("1 title");
        txt1.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(noticeViewWidth*WIDTH),View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        txt1.measure(widthMeasureSpec,heightMeasureSpec);
        return txt1.getMeasuredHeight();
    }
    private int getExpandDirectReplyViewHeight(){
        int pad = C.dpToPx(context,8);
        final EditText editText = new EditText(context);
        editText.setText("1 title");
        editText.setPadding(pad,pad,pad,pad);

        final float WIDTH = 0.7f;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(noticeViewWidth*WIDTH),View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        editText.measure(widthMeasureSpec,heightMeasureSpec);
        return editText.getMeasuredHeight();
    }
    private int getExpandHeight(String content){
        int EXPANDED_HEIGHT;
        int widthMeasureSpec;
        int heightMeasureSpec;
        final float WIDTH = 0.7f;
        final int padding = C.dpToPx(context,NOTICE_ICON_HEIGHT)/5;
        //Log.e("NotificationView","getExpandHeight 0: "+padding*2);

        int titleHeight = getExpandTitleHeight();
        EXPANDED_HEIGHT = titleHeight;

        TextView txt2 = new TextView(context);
        txt2.setText("2nd post time");
        txt2.setTextSize(TypedValue.COMPLEX_UNIT_SP,11);
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(noticeViewWidth*WIDTH),View.MeasureSpec.AT_MOST);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        txt2.measure(widthMeasureSpec,heightMeasureSpec);
        EXPANDED_HEIGHT = txt2.getMeasuredHeight()+EXPANDED_HEIGHT;
        //Log.e("NotificationView","getExpandHeight 2: "+EXPANDED_HEIGHT);

        TextView txt3 = new TextView(context);
        txt3.setText(content);
        txt3.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        txt3.setMaxLines(MAX_TXT_LINE);
        widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(noticeViewWidth*WIDTH),View.MeasureSpec.AT_MOST);
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        txt3.measure(widthMeasureSpec,heightMeasureSpec);
        EXPANDED_HEIGHT = txt3.getMeasuredHeight()+EXPANDED_HEIGHT;

        if(actionNoticeView!=null){
            EXPANDED_HEIGHT = titleHeight+EXPANDED_HEIGHT;//action view height
        }
        if(directInputView!=null){
            EXPANDED_HEIGHT = getExpandDirectReplyViewHeight()+EXPANDED_HEIGHT;//direct view height
        }
        return (padding*2)+EXPANDED_HEIGHT;
    }
    private int getContentHeight(String str){
        final float WIDTH = 0.7f;
        final TextView txt = new TextView(context);
        txt.setText(str);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
        txt.setMaxLines(MAX_TXT_LINE);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int)(noticeViewWidth*WIDTH),View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        txt.measure(widthMeasureSpec,heightMeasureSpec);
        return txt.getMeasuredHeight();
    }
    private void expandTextView(View v,int line){
        try{
            if(v.getId()==CLOCK_ID){
                return;
            }
            if(v.getId()>=EX_VIEW_ID){
                return;
            }
            RelativeLayout contentV = (RelativeLayout)((RelativeLayout)v).getChildAt(0);
            if(contentV!=null && contentV.getChildCount()>=5){
                TextView txt = (TextView)contentV.getChildAt(4);
                int val1 = getContentHeight("single line");
                int val2 = getContentHeight(txt.getText().toString());
                //Log.e("expandTextView","getContentHeight: "+val1+"/"+val2);
                if(val1==val2 && line==MAX_TXT_LINE){//no extra text so no need to expand view
                    txt.setMaxLines(MIN_TXT_LINE);
                }else{
                    txt.setMaxLines(line);
                }
            }
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }
    private void resetToDefaultview(final RelativeLayout renderView){
        int visible = View.GONE;
        for(int i = 0; i<renderView.getChildCount(); i++){
            try{
                View mainView = renderView.getChildAt(i);
                if(mainView.getId()==CLOCK_ID){
                    visible = View.VISIBLE;
                    mainView.setVisibility(visible);
                }else if(mainView.getId()==EX_VIEW_ID+1){
                    handleImgRotation(mainView,false);
                    mainView.setVisibility(visible);
                    visible = View.GONE;
                }else{
                    mainView.setVisibility(visible);
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }
    }
    private void resetTxtExpanded(final RelativeLayout renderView){
        boolean hasResetTxtExpanded = false;
        for(int i = 0; i<renderView.getChildCount(); i++){
            try{
                View mainView = renderView.getChildAt(i);
                if(mainView instanceof RelativeLayout){
                    RelativeLayout rv = (RelativeLayout)mainView;
                    if(mainView.isShown()){
                        boolean isActionShown = resetActionNotice(rv,false);
                        boolean isDirectInputShown = resetDirectInputNotice(rv,false);
                        for(int i1 = 0; i1<rv.getChildCount(); i1++){
                            View contentView = rv.getChildAt(i1);
                            if(contentView instanceof RelativeLayout){
                                View v = ((RelativeLayout)contentView).getChildAt(4);//content
                                if(v instanceof TextView){
                                    int expandH;
                                    TextView txt = (TextView)v;
                                    if(txt.getLineCount()>1 || isActionShown || isDirectInputShown){//content text is expanded
                                        expandH = getExpandHeight(txt.getText().toString());
                                        if(isActionShown){
                                            expandH = expandH+getExpandTitleHeight();
                                        }
                                        if(isDirectInputShown){
                                            expandH = expandH+getExpandDirectReplyViewHeight();
                                        }

                                        rv.getLayoutParams().height = expandH;
                                        expandAnimation(rv,false,COLLAPSED_HEIGHT,expandH);
                                        hasResetTxtExpanded = true;
                                    }
                                }
                            }
                        }
                    }else{
                        if(COLLAPSED_HEIGHT>0){
                            if(rv.getId()!=CLOCK_ID){
                                if(rv.getId()<EX_VIEW_ID){
                                    resetActionNotice(rv,true);
                                    resetDirectInputNotice(rv,true);
                                    expandTextView(rv,MIN_TXT_LINE);
                                    rv.getLayoutParams().height = COLLAPSED_HEIGHT;
                                    rv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                                    rv.requestLayout();
                                }
                            }
                        }
                    }
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }
        if(!hasResetTxtExpanded){
            resetToDefaultview(renderView);
        }
    }
    private boolean resetActionNotice(final RelativeLayout mainView,boolean force){
        View child1 = mainView.getChildAt(mainView.getChildCount()-1);
        if(child1 instanceof RelativeLayout){
            View child2 = ((RelativeLayout)child1).getChildAt(((RelativeLayout)child1).getChildCount()-2);//-2 = actionNoticeLayout
            if(child2 instanceof LinearLayout){
                if(force){
                    child2.setVisibility(View.GONE);
                    if(((LinearLayout)child2).getChildCount()>0){
                        return true;
                    }
                }
                if(child2.isShown()){
                    child2.setVisibility(View.GONE);
                    if(((LinearLayout)child2).getChildCount()>0){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean resetDirectInputNotice(final RelativeLayout mainView,boolean force){
        View child1 = mainView.getChildAt(mainView.getChildCount()-1);
        if(child1 instanceof RelativeLayout){
            View child2 = ((RelativeLayout)child1).getChildAt(((RelativeLayout)child1).getChildCount()-1);//-1 = directInputLayout
            if(child2 instanceof LinearLayout){
                if(force){
                    child2.setVisibility(View.GONE);
                    if(((LinearLayout)child2).getChildCount()>0){
                        return true;
                    }
                }
                if(child2.isShown()){
                    child2.setVisibility(View.GONE);
                    if(((LinearLayout)child2).getChildCount()>0){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private void refreshTime(final RelativeLayout renderView){
        for(int i = 0; i<renderView.getChildCount(); i++){
            View mainView = renderView.getChildAt(i);
            if(mainView instanceof RelativeLayout){
                RelativeLayout rv = (RelativeLayout)mainView;
                for(int i1 = 0; i1<rv.getChildCount(); i1++){
                    View contentView = rv.getChildAt(i1);
                    if(contentView instanceof RelativeLayout){
                        View v = ((RelativeLayout)contentView).getChildAt(3);//content
                        if(v instanceof PostTimeTextView){
                            ((PostTimeTextView)v).refreshTime();
                        }
                    }
                }
            }
        }
    }
    private void resetViewAnimation(final View v,float x,float y,float a,final int action){
        try{
            final ObjectAnimator xVal = ObjectAnimator.ofFloat(v,"x",v.getX(),x).setDuration(ANIMATION_TIME);
            //final ObjectAnimator yVal = ObjectAnimator.ofFloat(v,"y",v.getY(),y).setDuration(200);
            final ObjectAnimator aVal = ObjectAnimator.ofFloat(v,"alpha",v.getAlpha(),a).setDuration(ANIMATION_TIME);
            final AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(xVal,aVal);
            animSet.addListener(new Animator.AnimatorListener(){
                @Override
                public void onAnimationStart(Animator animation){
                }

                @Override
                public void onAnimationEnd(Animator animation){
                    if(action==OPEN_NOTICE){
                        callBack.onLollipopNoticeTouchEvent(MotionEvent.ACTION_UP,OPEN_NOTICE,noticePkgName,noticeTag,noticeImgId);
                        resetViewAnimation(v,C.dpToPx(context,NOTICE_ICON_HEIGHT)/5,STARTING_Y-MOVE_Y,1,-1);
                    }else if(action==REMOVE_NOTICE){
                        callBack.onLollipopNoticeTouchEvent(MotionEvent.ACTION_UP,REMOVE_NOTICE,noticePkgName,noticeTag,noticeImgId);
                    }
                    //v.invalidate();
                    //v.requestLayout();
                    //Log.e("resetViewAnimation","onAnimationEnd");
                }

                @Override
                public void onAnimationCancel(Animator arg0){
                }

                @Override
                public void onAnimationRepeat(Animator arg0){
                }
            });
            animSet.start(); //must add listener 1st
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void setNoticeExpandVisibility(View v,boolean isExpand,final int expH){
        float maxH = screenHeight*0.2f;
        if(!isScreenPortrait()){
            maxH = screenHeight*0.25f;
        }
        if(isExpand && expH<maxH){
            return;
        }
        RelativeLayout renderView = (RelativeLayout)v.getParent();
        if(isExpand){
            for(int i = 0; i<renderView.getChildCount(); i++){
                View mainV = renderView.getChildAt(i);
                if(mainV.getId()!=v.getId()){
                    mainV.setVisibility(View.GONE);
                }
            }
        }else{
            boolean isViewId = false;
            for(int i = (renderView.getChildCount()-1); i>0; i--){
                View mainV = renderView.getChildAt(i);
                if(mainV.getId()==v.getId()){
                    isViewId = true;
                }
                if(isViewId){
                    if(mainV.getId()>=EX_VIEW_ID){
                        handleNoticeVisibility(mainV.getId(),true,renderView);
                        return;
                    }
                }
            }
            //if no extra view found
            resetToDefaultview(renderView);
        }
    }
    private void handleNoticeVisibility(int id,boolean up,RelativeLayout renderView){
        int minId = id;
        int maxId = id+1;
        if(!up){//swipe down
            minId = id-1;
            maxId = id;
        }
        int visible = View.GONE;
        for(int i = 0; i<renderView.getChildCount(); i++){
            View mainV = renderView.getChildAt(i);//==mainView
            if(mainV instanceof RelativeLayout){
                if(mainV.getId()==minId){
                    visible = View.VISIBLE;
                    handleImgRotation(mainV,true);
                    mainV.setVisibility(visible);
                }else if(mainV.getId()==maxId){
                    handleImgRotation(mainV,false);
                    mainV.setVisibility(visible);
                    visible = View.GONE;
                }else{
                    mainV.setVisibility(visible);
                }
            }else{
                mainV.setVisibility(visible);
            }
        }
    }
    private void handleImgRotation(View rv,boolean up){
        if(up){
            View img = ((RelativeLayout)rv).getChildAt(2);
            if(img instanceof ImageView){
                img.setRotation(180);
            }
        }else{
            View img = ((RelativeLayout)rv).getChildAt(2);
            if(img instanceof ImageView){
                img.setRotation(0);
            }
        }
    }
    private void arrowAnimation(final View v,final float defaultY){
        /*try{
            final ObjectAnimator xVal = ObjectAnimator.ofFloat(v,"y",defaultY*1.2f,defaultY,defaultY*1.2f).setDuration(ANIMATION_TIME*4);
			//final ObjectAnimator aVal = ObjectAnimator.ofFloat(v,"alpha",1,0).setDuration(ANIMATION_TIME);
			final AnimatorSet animSet = new AnimatorSet();
		    animSet.play(xVal);
			animSet.addListener(new Animator.AnimatorListener(){
				@Override public void onAnimationCancel(Animator arg0){}
				@Override public void onAnimationRepeat(Animator arg0){}
				@Override public void onAnimationStart(Animator animation) {}
				@Override public void onAnimationEnd(Animator animation) {
					handler.postDelayed(new Runnable(){
						@Override
						public void run() {
							if(v!=null){
								arrowAnimation(v,defaultY);
							}
						}
					},1000);
				}
			});
			//animSet.start(); //must add listener 1st
		}catch(Exception e){
			e.printStackTrace();
		}//*/
    }
    private void expandAnimation(final View v,final boolean isExpand,final int colH,final int expH){
        final int gapC = v.getLayoutParams().height-colH;
        final int gapE = v.getLayoutParams().height-expH;
        final int viewH = v.getLayoutParams().height;
        //Log.e("expandAnimation",log+"/"+colH+"/"+expH+"/"+viewH);

        if(viewH<0){
            return;
        }
        Animation a = new Animation(){
            @Override
            protected void applyTransformation(float interpolatedTime,Transformation t){
                if(interpolatedTime<1.0f){
                    if(isExpand){
                        int h = (int)(gapE*interpolatedTime);
                        v.getLayoutParams().height = viewH-h;
                        //Log.e("NotificationView","applyTransformation 1: "+v.getLayoutParams().height+"/"+expH);
                    }else{
                        int h = (int)(gapC*interpolatedTime);
                        v.getLayoutParams().height = viewH-h;
                        //Log.e("NotificationView","applyTransformation 2: "+v.getLayoutParams().height+"/"+expH);
                    }
                }else{
                    if(!isExpand){
                        expandTextView(v,MIN_TXT_LINE);
                    }
                    setNoticeExpandVisibility(v,isExpand,expH);
                    v.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    //Log.e("applyTransformation","applyTransformation end/");
                }
                //v.invalidate();
                v.requestLayout();
            }
        };
        a.setDuration(ANIMATION_TIME); // in ms
        v.startAnimation(a);
    }
    private LinearLayout getActionNoticeView(View v){
        if(v instanceof RelativeLayout){
            RelativeLayout rl = (RelativeLayout)v;//==mainView
            View child1 = rl.getChildAt(rl.getChildCount()-1);//==noticeContent
            if(child1 instanceof RelativeLayout){
                View child2 = ((RelativeLayout)child1).getChildAt(((RelativeLayout)child1).getChildCount()-2);//-2 = actionNoticeLayout
                if(child2 instanceof LinearLayout){
                    if(((LinearLayout)child2).getChildCount()>0){
                        return (LinearLayout)child2;
                    }
                }
            }
        }
        return null;
    }
    private LinearLayout getDirectInputView(View v){
        if(v instanceof RelativeLayout){
            RelativeLayout rl = (RelativeLayout)v;//==mainView
            View child1 = rl.getChildAt(rl.getChildCount()-1);//==noticeContent
            if(child1 instanceof RelativeLayout){
                View child2 = ((RelativeLayout)child1).getChildAt(((RelativeLayout)child1).getChildCount()-1);//-1 = directInputLayout
                if(child2 instanceof LinearLayout){
                    if(((LinearLayout)child2).getChildCount()>0){
                        return (LinearLayout)child2;
                    }
                }
            }
        }
        return null;
    }
    private int getMaxNoticeSpanY(){
        final int perDenSize = C.dpToPx(context,C.DEFAULT_DEN_DP_Y);
        int intDisplayHeight = C.getRealDisplaySize(context).y;
        int maxsize = intDisplayHeight/perDenSize;
        if(isScreenPortrait()){
            if(intLockStyle==C.SIMPLE_UNLOCK ||
                intLockStyle==C.GESTURE_UNLOCK ||
                intLockStyle==C.FINGERPRINT_UNLOCK){
                maxsize = (int)(maxsize*0.8f);
            }else{
                maxsize = (int)(maxsize*0.6f);
            }
        }else{
            maxsize = (int)(maxsize*0.8f);
        }
        //Log.e("getMaxWidgetSpan1",intDisplayHeight+"/"+perDenSize+"/"+maxsize+"");
        return maxsize;
    }
    private boolean isScreenPortrait(){
        return context.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT;
    }
    public interface MyViewGroupCallBack{
        void addTouchListener(final View v);

        void addDragListener(final View v);

        void onLollipopNoticeTouchEvent(int touchAction,int result,String noticePkgName,
            String noticeTag,long noticeImgId);
    }
    private class PostTimeTextView extends TextView{
        private long postTime;
        private String pkgName;

        @SuppressLint("RtlHardcoded")
        public PostTimeTextView(Context context,int leftId,int belowId,int index,long postTime,
            String pkgName){
            super(context);

            int color = ContextCompat.getColor(context,R.color.colorTextDesc);
            final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            pr.addRule(RelativeLayout.ALIGN_LEFT,leftId);
            pr.addRule(RelativeLayout.BELOW,belowId);

            this.postTime = postTime;
            this.pkgName = pkgName;

            this.setId(getNoticeContentImgId(index)+TIME_ID);
            this.setLayoutParams(pr);
            this.setEllipsize(TruncateAt.END);
            //v.setText(strTime);
            this.setGravity(Gravity.RIGHT);
            this.setAlpha(0.5f);
            this.setTextSize(TypedValue.COMPLEX_UNIT_SP,11);
            this.setTextColor(color);
            this.setPadding(C.dpToPx(context,5),0,C.dpToPx(context,5),0);
            refreshTime();
        }

        public void refreshTime(){
            final String time = getNoticePostTime(postTime,pkgName);
            this.setText(time);
        }
    }
}