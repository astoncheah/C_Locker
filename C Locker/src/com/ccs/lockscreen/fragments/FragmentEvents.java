package com.ccs.lockscreen.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Instances;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.InfoCalendarEvent;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class FragmentEvents extends Fragment{
    private static final String CLD_LIST = "cldList";
    private static final String CLD_LIST_SAVED = "cldListSaved";
    private SharedPreferences prefs;
    private View rootView;
    private ListView listView;
    private ProgressBar progressBar;
    private int intAddEventDays;
    private ArrayList<InfoCalendarEvent> calendarList = new ArrayList<InfoCalendarEvent>();
    private boolean cBoxEnableCalendar, isCldAccountSelected;
    private String strCalendarAccountFilter, strColorTodayText;

    public static FragmentEvents newInstance(ArrayList<InfoCalendarEvent> calendarList){
        FragmentEvents f = new FragmentEvents();
        Bundle b = new Bundle();
        b.putParcelableArrayList(CLD_LIST,calendarList);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        rootView = inflater.inflate(R.layout.list_fragment,container,false);
        listView = (ListView)rootView.findViewById(R.id.listNotifications);
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        prefs = getActivity().getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);

        Bundle b = getArguments();
        if((b!=null) && b.containsKey(CLD_LIST)){
            calendarList = b.getParcelableArrayList(CLD_LIST);
        }else if((savedInstanceState!=null) && savedInstanceState.containsKey(CLD_LIST_SAVED)){
            calendarList = savedInstanceState.getParcelableArrayList(CLD_LIST_SAVED);
        }

        loadSettings();
        setCldOnClick(cBoxEnableCalendar);
        new LoadEvents(getActivity()).execute();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle b){
        b.putParcelableArrayList(CLD_LIST_SAVED,calendarList);
        super.onSaveInstanceState(b);
    }

    /*
    @Override public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            //throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            //throw new RuntimeException(e);
        }
    }
    */
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void loadSettings(){
        cBoxEnableCalendar = prefs.getBoolean("cBoxEnableCalendar",false);
        isCldAccountSelected = prefs.getBoolean("isCldAccountSelected",false);
        intAddEventDays = prefs.getInt("intAddEventDay",30);
        strCalendarAccountFilter = prefs.getString("strCalendarAccount","");
        strColorTodayText = prefs.getString("strColorTodayText","FFFF00");
    }

    private void setCldOnClick(final boolean bln){
        if(bln){
            listView.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg,View v,int position,long id){
                    try{
                        InfoCalendarEvent cldEvent = calendarList.get(position);
                        int eventID = cldEvent.getCldEventId();

                        Intent i = new Intent(getActivity().getPackageName()+C.PAGER_CLICK_NOTICE);
                        i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_EVENT);
                        i.putExtra(C.EVENT_ID,eventID);
                        getActivity().sendBroadcast(i);
                    }catch(Exception e){
                        new MyCLocker(getActivity()).saveErrorLog(null,e);
                    }
                }
            });
        }
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
            try{
                progressBar.setVisibility(View.VISIBLE);
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }

        @Override
        protected void onPostExecute(ArrayList<InfoCalendarEvent> tempCldList){
            try{
                if(cBoxEnableCalendar){
                    if(tempCldList!=null && tempCldList.size()>0){
                        calendarList = tempCldList;
                        final EventAdapter eventAdapter = new EventAdapter(context,R.layout.list_fragment_events,calendarList);
                        listView.setAdapter(eventAdapter);
                    }
                }
                progressBar.setVisibility(View.GONE);
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
        private int colorTitle, colorDesc;

        private EventAdapter(Context context,int layoutId,ArrayList<InfoCalendarEvent> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;

            colorTitle = ContextCompat.getColor(getActivity(),R.color.colorTextTitle);
            colorDesc = ContextCompat.getColor(getActivity(),R.color.colorTextDesc);
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
                    holder.txtTitle.setText(cldEvent.getCldTitle());

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
                    holder.txtDateStart.setTextColor(colorDesc);
                    holder.txtDateStart.setText(setDate(cldEvent));

                    holder.txtTitle.setTextColor(colorTitle);
                    holder.txtTitle.setText(cldEvent.getCldTitle());

                    if(cldEvent.getCldLocation()==null || cldEvent.getCldLocation().equals("")){
                        holder.txtLocation.setVisibility(View.GONE);
                    }else{
                        holder.txtLocation.setVisibility(View.VISIBLE);
                        holder.txtLocation.setTextColor(colorDesc);
                        holder.txtLocation.setText(cldEvent.getCldLocation());
                    }
                }
            }catch(Exception e){
                new MyCLocker(getActivity()).saveErrorLog(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            return (getActivity().getLayoutInflater().inflate(layoutId,parent,false));
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
            final DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);
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
                //new MyCLocker(getActivity()).writeToFile(C.FILE_BACKUP_RECORD,
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

        private String getDate(final long date,final DateFormat df){
            final SimpleDateFormat dayFm = new SimpleDateFormat("EEE",Locale.getDefault());
            final String day = dayFm.format(new Date(date));

            int today;
            Calendar cld = Calendar.getInstance();
            today = cld.get(Calendar.DAY_OF_YEAR);
            cld.setTimeInMillis(date);

            if(today==cld.get(Calendar.DAY_OF_YEAR)){
                return day+" "+getActivity().getString(R.string.bottom_bar_today);
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