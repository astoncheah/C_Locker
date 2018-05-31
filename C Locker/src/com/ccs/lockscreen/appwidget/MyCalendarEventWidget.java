package com.ccs.lockscreen.appwidget;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.InfoCalendarEvent;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.C.WidgetOnTouchListener;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MyCalendarEventWidget extends LinearLayout{
    private Context context;
    private ArrayList<InfoCalendarEvent> calendarList = new ArrayList<InfoCalendarEvent>();
    private boolean isCldAccountSelected, enableScroll;
    private int intAddEventDays;
    private String strCalendarAccountFilter, strColorTodayText, strColorOtherdayText;
    private WidgetOnTouchListener cb;

    public MyCalendarEventWidget(Context context,WidgetOnTouchListener cb){
        super(context);
        this.context = context;
        this.cb = cb;
        if(cb==null){
            enableScroll = false;
        }else{
            enableScroll = true;
        }

        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        isCldAccountSelected = prefs.getBoolean("isCldAccountSelected",false);
        intAddEventDays = prefs.getInt("intAddEventDay",30);
        strCalendarAccountFilter = prefs.getString("strCalendarAccount","");
        strColorTodayText = prefs.getString(P.STR_COLOR_EVENT_TODAY_TEXT,"FFFF00");
        strColorOtherdayText = prefs.getString(P.STR_COLOR_EVENT_OTHER_DAY_TEXT,"FFFFFF");

        setGravity(Gravity.CENTER);
        setPadding(C.dpToPx(context,40),C.dpToPx(context,5),C.dpToPx(context,40),C.dpToPx(context,5));
    }

    @Override
    protected void onAttachedToWindow(){
        super.onAttachedToWindow();
        new LoadEvents(context).execute();
    }

    @Override
    protected void onDetachedFromWindow(){
        super.onDetachedFromWindow();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent m){
        if(cb!=null){
            return cb.onWidgetTouchEvent(this,m,null,null);
        }
        return super.onTouchEvent(m);
    }

    private class LoadEvents extends AsyncTask<Void,Void,ArrayList<InfoCalendarEvent>>{
        private Context context;

        public LoadEvents(Context context){
            this.context = context;
        }

        @Override
        protected ArrayList<InfoCalendarEvent> doInBackground(Void... arg0){
            try{
                return loadEvents(loadEventCursor());
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<InfoCalendarEvent> tempCldList){
            try{
                if(tempCldList!=null && tempCldList.size()>0){
                    calendarList = tempCldList;
                    final EventAdapter eventAdapter = new EventAdapter(context,R.layout.list_widget_events,calendarList);
                    final ListView listView = new ListView(context);
                    listView.setClickable(enableScroll);
                    listView.setEnabled(enableScroll);
                    listView.setAdapter(eventAdapter);
                    listView.setOnItemClickListener(new OnItemClickListener(){
                        @Override
                        public void onItemClick(AdapterView<?> arg,View v,int position,long id){
                            try{
                                final InfoCalendarEvent cldEvent = calendarList.get(position);
                                final int eventID = cldEvent.getCldEventId();

                                final Intent i = new Intent(context.getPackageName()+C.PAGER_CLICK_NOTICE);
                                i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_EVENT);
                                i.putExtra(C.EVENT_ID,eventID);
                                context.sendBroadcast(i);
                            }catch(Exception e){
                                new MyCLocker(context).saveErrorLog(null,e);
                            }
                        }
                    });
                    MyCalendarEventWidget.this.addView(listView);
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }

        private ArrayList<InfoCalendarEvent> loadEvents(Cursor cursor){
            final int ACCOUNT_ID = 0, EVENT_ID = 1, TITLE = 2, DTSTART = 3, DTEND = 4,
                    LOCATION = 5, ALLDAY = 6;//TIMEZONE = 7;
            ArrayList<InfoCalendarEvent> tempCldList = new ArrayList<InfoCalendarEvent>();
            try{
                if(cursor!=null && cursor.getCount()>0){
                    Calendar cldNow = Calendar.getInstance();//-TimeZone.getDefault().getRawOffset()
                    Calendar cldBegin = Calendar.getInstance();
                    Calendar cldEnd = Calendar.getInstance();
                    cldBegin = setTo12am(cldBegin);
                    cldEnd = setTo12am(cldEnd);
                    cldEnd.add(Calendar.DAY_OF_YEAR,intAddEventDays);

                    cursor.moveToFirst();
                    do{
                        if(cursor.getString(ALLDAY).equals("1")){
                            if(tzOffSetAdd(cursor.getLong(DTSTART))<=cldEnd.getTimeInMillis() && tzOffSetMinus(cursor.getLong(DTEND))>=cldBegin.getTimeInMillis()){
                                tempCldList.add(new InfoCalendarEvent(cursor.getInt(ACCOUNT_ID),cursor.getInt(EVENT_ID),cursor.getString(TITLE),tzOffSetAdd(cursor.getLong(DTSTART)),tzOffSetMinus(cursor.getLong(DTEND)),cursor.getString(LOCATION),cursor.getString(ALLDAY)));
                            }
                        }else if(cursor.getLong(DTEND)>=cldNow.getTimeInMillis()){
                            if(cursor.getLong(DTSTART)<=cldEnd.getTimeInMillis() && cursor.getLong(DTEND)>=cldBegin.getTimeInMillis()){
                                tempCldList.add(new InfoCalendarEvent(cursor.getInt(ACCOUNT_ID),cursor.getInt(EVENT_ID),cursor.getString(TITLE),cursor.getLong(DTSTART),cursor.getLong(DTEND),cursor.getString(LOCATION),cursor.getString(ALLDAY)));
                            }
                        }
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return tempCldList;
        }

        private Cursor loadEventCursor(){
            try{
                Calendar cld = Calendar.getInstance();
                cld = setTo12am(cld);
                long longFilterStart = cld.getTimeInMillis()-(DateUtils.DAY_IN_MILLIS*90);
                long longFilterEnd = cld.getTimeInMillis()+(DateUtils.DAY_IN_MILLIS*intAddEventDays);

                String[] projection = new String[]{Instances.CALENDAR_ID,Instances.EVENT_ID,Instances.TITLE,Instances.BEGIN,Instances.END,Instances.EVENT_LOCATION,Instances.ALL_DAY,Instances.EVENT_TIMEZONE};
                String selection = "(";
                String[] arg = strCalendarAccountFilter.split(",");
                for(int i = 0; i<arg.length; i++){
                    if(i<(arg.length-1)){
                        selection += Instances.CALENDAR_ID+"=? OR ";
                    }else{
                        selection += Instances.CALENDAR_ID+"=?)";
                    }
                }
                String sort = Instances.BEGIN+" ASC";//"startDay ASC, startMinute ASC"

                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder,longFilterStart);
                ContentUris.appendId(builder,longFilterEnd);
                Uri eventsUri = builder.build();

                if(isCldAccountSelected){
                    return context.getContentResolver().query(eventsUri,projection,selection,arg,sort);
                }else{
                    return context.getContentResolver().query(eventsUri,projection,null,null,sort);
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }
        }

        private Calendar setTo12am(Calendar cld){
            cld = Calendar.getInstance();
            cld.add(Calendar.HOUR,-cld.get(Calendar.HOUR_OF_DAY));
            cld.add(Calendar.MINUTE,-(cld.get(Calendar.MINUTE)+1));
            return cld;
        }

        private long tzOffSetMinus(long longDate){
            //long val = DateUtils.MINUTE_IN_MILLIS * 2;
            long val = DateUtils.HOUR_IN_MILLIS*12; // testing
            return longDate-TimeZone.getDefault().getRawOffset()-val;
        }

        private long tzOffSetAdd(long longDate){
            long val = DateUtils.MINUTE_IN_MILLIS*2;
            return (longDate-TimeZone.getDefault().getRawOffset())+val;
        }
    }

    private class EventAdapter extends ArrayAdapter<InfoCalendarEvent>{
        private ViewHolder holder;
        private int layoutId;
        private ArrayList<InfoCalendarEvent> list = null;
        private InfoCalendarEvent cldEvent;

        private EventAdapter(Context context,int layoutId,ArrayList<InfoCalendarEvent> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            cldEvent = list.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtDateStart = (TextView)v.findViewById(R.id.txt_cld_date);
                viewHolder.txtLocation = (TextView)v.findViewById(R.id.txt_cld_location);
                viewHolder.txtTitle = (TextView)v.findViewById(R.id.txt_cld_title);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                if(isTodayEvent(cldEvent)){
                    holder.txtDateStart.setTextColor(Color.parseColor("#"+strColorTodayText));
                    holder.txtDateStart.setText(setDate(cldEvent));

                    holder.txtTitle.setTextColor(Color.parseColor("#"+strColorTodayText));
                    holder.txtTitle.setText(cutString(cldEvent.getCldTitle(),30));

                    if(cldEvent.getCldLocation()==null){
                        holder.txtLocation.setVisibility(View.GONE);
                    }else if(cldEvent.getCldLocation().equals("")){
                        holder.txtLocation.setVisibility(View.GONE);
                    }else{
                        holder.txtLocation.setVisibility(View.VISIBLE);
                        holder.txtLocation.setTextColor(Color.parseColor("#"+strColorTodayText));
                        holder.txtLocation.setText(cldEvent.getCldLocation());
                    }
                }else{
                    holder.txtDateStart.setTextColor(Color.parseColor("#"+strColorOtherdayText));
                    holder.txtDateStart.setText(setDate(cldEvent));

                    holder.txtTitle.setTextColor(Color.parseColor("#"+strColorOtherdayText));
                    holder.txtTitle.setText(cutString(cldEvent.getCldTitle(),30));

                    if(cldEvent.getCldLocation()==null || cldEvent.getCldLocation().equals("")){
                        holder.txtLocation.setVisibility(View.GONE);
                    }else{
                        holder.txtLocation.setVisibility(View.VISIBLE);
                        holder.txtLocation.setTextColor(Color.parseColor("#"+strColorOtherdayText));
                        holder.txtLocation.setText(cldEvent.getCldLocation());
                    }
                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return (inflater.inflate(layoutId,parent,false));
        }

        private boolean isTodayEvent(final InfoCalendarEvent cldEvent){
            Calendar cldNow = Calendar.getInstance();
            Calendar cldEventStart = Calendar.getInstance();
            Calendar cldEventEnd = Calendar.getInstance();
            cldNow.getTimeInMillis();
            cldEventStart.setTimeInMillis(cldEvent.getCldDateStart());
            cldEventEnd.setTimeInMillis(cldEvent.getCldDateEnd());

            long today = cldNow.get(Calendar.DAY_OF_YEAR);
            long eventDayStart = cldEventStart.get(Calendar.DAY_OF_YEAR);
            long eventDayEnd = cldEventEnd.get(Calendar.DAY_OF_YEAR);
            if(today<=intAddEventDays){
                today += 365;
            }
            if(eventDayStart<=intAddEventDays){
                eventDayStart += 365;
            }
            if(eventDayEnd<=intAddEventDays){
                eventDayEnd += 365;
            }

            if(eventDayStart<=today && eventDayEnd>=today){
                return true;
            }else{
                return false;
            }
        }

        private String setDate(final InfoCalendarEvent cldEvent){
            final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG,Locale.getDefault());
            final DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());
            String strDateStart = getDate(cldEvent.getCldDateStart(),df);
            String strDateEnd = getDate(cldEvent.getCldDateEnd(),df);
            String strTimeStart = tf.format(new Date(cldEvent.getCldDateStart())).toString();
            String strTimeEnd = tf.format(new Date(cldEvent.getCldDateEnd())).toString();

            Calendar cldEventStart = Calendar.getInstance();
            Calendar cldEventEnd = Calendar.getInstance();
            cldEventStart.setTimeInMillis(cldEvent.getCldDateStart());
            cldEventEnd.setTimeInMillis(cldEvent.getCldDateEnd());
            int start = cldEventStart.get(Calendar.DAY_OF_YEAR);
            int end = cldEventEnd.get(Calendar.DAY_OF_YEAR);

            if(cldEvent.getCldAllDayEvent().equals("1")){
                if(start!=end){
                    strDateStart = strDateStart+" - "+strDateEnd;
                }
                //new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,
                //"MyCalendarEventWidget: "+TimeZone.getDefault()+"/"+
                //TimeZone.getDefault().getRawOffset()+"/"+
                //strDateStart+" ("+strTimeStart+" - "+strTimeEnd+")");
                return strDateStart;
            }else{
                if(start!=end){
                    strDateStart = strDateStart+" - "+strDateEnd;
                }
                return strDateStart+" ("+strTimeStart+" - "+strTimeEnd+")";
            }
        }

        private String cutString(String str,int strLength){
            if(str.length()>strLength){
                return str.substring(0,strLength)+"..";
            }else{
                return str;
            }
        }

        private String getDate(final long date,final DateFormat df){
            final SimpleDateFormat dayFm = new SimpleDateFormat("EEE",Locale.getDefault());
            final String day = dayFm.format(new Date(date));

            int today;
            Calendar cld = Calendar.getInstance();
            today = cld.get(Calendar.DAY_OF_YEAR);
            cld.setTimeInMillis(date);

            if(today==cld.get(Calendar.DAY_OF_YEAR)){
                return day+" "+context.getString(R.string.bottom_bar_today);
            }else{
                return day+" "+df.format(new Date(date)).toString();
            }
        }

        private class ViewHolder{
            private TextView txtDateStart;
            private TextView txtTitle;
            private TextView txtLocation;
        }
    }
}