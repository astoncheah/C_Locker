package com.ccs.lockscreen.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
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
import com.ccs.lockscreen.data.InfoCallLog;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FragmentCallLogs extends Fragment{
    private static final String CALLLOG_LIST = "callLogsList";
    private static final String CALLLOG_LIST_SAVED = "callLogsListSaved";
    private ListView listView;
    private ArrayList<InfoCallLog> callLogsList = new ArrayList<InfoCallLog>();
    private ProgressBar progressBar;
    private boolean cBoxShowNoticeContent;

    public static FragmentCallLogs newInstance(ArrayList<InfoCallLog> callLogsList){
        FragmentCallLogs f = new FragmentCallLogs();
        Bundle b = new Bundle();
        b.putParcelableArrayList(CALLLOG_LIST,callLogsList);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.list_fragment,container,false);
        listView = (ListView)rootView.findViewById(R.id.listNotifications);
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        final SharedPreferences prefs = getActivity().getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        cBoxShowNoticeContent = prefs.getBoolean("cBoxShowNoticeContent",true);

        Bundle b = getArguments();
        if((b!=null) && b.containsKey(CALLLOG_LIST)){
            callLogsList = b.getParcelableArrayList(CALLLOG_LIST);
        }else if((savedInstanceState!=null) && savedInstanceState.containsKey(CALLLOG_LIST_SAVED)){
            callLogsList = savedInstanceState.getParcelableArrayList(CALLLOG_LIST_SAVED);
        }

        setCldOnClick(true);
        new LoadCallLogs(getActivity()).execute();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList(CALLLOG_LIST_SAVED,callLogsList);
        super.onSaveInstanceState(outState);
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

    private void setCldOnClick(final boolean bln){
        if(bln){
            listView.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg,View v,int position,long id){
                    InfoCallLog callLogList = callLogsList.get(position);
                    try{
                        Intent i = new Intent(getActivity().getPackageName()+C.PAGER_CLICK_NOTICE);
                        i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_CALLLOG);
                        i.putExtra(C.CONTACT_ID,getContactId(callLogList.getCallNo()));
                        i.putExtra(C.PHONE_NO,callLogList.getCallNo());
                        getActivity().sendBroadcast(i);
                    }catch(Exception e){
                        new MyCLocker(getActivity()).saveErrorLog(null,e);
                    }
                }
            });
        }
    }

    private String getContactId(String number){
        String contactId = null;
        try{
            Uri uri = Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,Uri.encode(number));
            String[] projection = new String[]{Contacts._ID};
            Cursor cursor = getActivity().getContentResolver().query(uri,projection,null,null,null);
            try{
                if(cursor!=null && cursor.getCount()>0){
                    cursor.moveToNext();
                    contactId = cursor.getString(cursor.getColumnIndex(Contacts._ID));
                    //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            if(contactId==null){
                return null;
            }else{
                return contactId;
            }
        }catch(Exception e){
            new MyCLocker(getActivity()).saveErrorLog(null,e);
            return null;
        }
    }

    private class LoadCallLogs extends AsyncTask<Void,Void,ArrayList<InfoCallLog>>{
        private Context context;

        public LoadCallLogs(Context context){
            this.context = context;
        }

        @Override
        protected ArrayList<InfoCallLog> doInBackground(Void... args){
            try{
                return loadCallLogs(loadCallLogCursor());
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
        protected void onPostExecute(ArrayList<InfoCallLog> tempCallLogsList){
            try{
                if(tempCallLogsList!=null && tempCallLogsList.size()>0){
                    callLogsList = tempCallLogsList;
                    final CallLogsAdapter callLogsAdapter = new CallLogsAdapter(context,R.layout.list_fragment_calllog,callLogsList);
                    listView.setAdapter(callLogsAdapter);
                }
                progressBar.setVisibility(View.GONE);
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }

        private ArrayList<InfoCallLog> loadCallLogs(Cursor cursor){
            ArrayList<InfoCallLog> tempCallLogsList = new ArrayList<InfoCallLog>();
            try{
                if(cursor!=null && cursor.getCount()>0){
                    final int PHONE_NO = 0;
                    final int DATE = 1;
                    final int IS_READ = 2;
                    //final int IS_NEW = 3;

                    cursor.moveToFirst();
                    do{
                        if(cursor.getString(PHONE_NO)!=null){
                            tempCallLogsList.add(new InfoCallLog(cursor.getString(PHONE_NO),getContactDisplayNameByNumber(cursor.getString(PHONE_NO)),getDate(cursor.getLong(DATE))+", "+getTime(cursor.getLong(DATE)),cursor.getString(IS_READ),cursor.getLong(DATE)));
                        }
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return tempCallLogsList;
        }

        private Cursor loadCallLogCursor(){
            try{
                Calendar cld = Calendar.getInstance();
                long longFilterStart = cld.getTimeInMillis()-(DateUtils.DAY_IN_MILLIS*7);

                String[] projection = new String[]{Calls.NUMBER,Calls.DATE,Calls.IS_READ};
                String selection = (Calls.TYPE+"=? AND date >=?");
                String[] arg = new String[]{Calls.MISSED_TYPE+"",longFilterStart+""};
                String sort = Calls.DATE+" DESC";//"startDay ASC, startMinute ASC"
                return context.getContentResolver().query(Calls.CONTENT_URI,projection,selection,arg,sort);
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }
        }

        private String getContactDisplayNameByNumber(String number){
            String name = number;
            boolean hasName = false;
            try{
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
                String[] str = new String[]{BaseColumns._ID,ContactsContract.PhoneLookup.DISPLAY_NAME};

                Cursor cursor = context.getContentResolver().query(uri,str,null,null,null);
                try{
                    if(cursor!=null && cursor.getCount()>0){
                        cursor.moveToNext();
                        name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                        hasName = true;
                        //String contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
                    }
                }finally{
                    if(cursor!=null){
                        cursor.close();
                    }
                }
                if(name==null){
                    return getString(R.string.unknown_number);
                }else{
                    if(hasName){
                        return name+"("+number+")";
                    }
                    return name;
                }
            }catch(Exception e){
                if(name==null){
                    return getString(R.string.unknown_number);
                }else{
                    return name;
                }
            }
        }

        private String getDate(long date){
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
            int today;
            Calendar cld = Calendar.getInstance();
            today = cld.get(Calendar.DAY_OF_YEAR);
            cld.setTimeInMillis(date);

            if(today==cld.get(Calendar.DAY_OF_YEAR)){
                return context.getString(R.string.bottom_bar_today);
            }else{
                return df.format(new Date(date)).toString();
            }
        }

        private String getTime(long time){
            //SimpleDateFormat tf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());
            return tf.format(new Date(time)).toString();
        }
    }

    private class CallLogsAdapter extends ArrayAdapter<InfoCallLog>{
        private ViewHolder holder;
        private int layoutId;
        private ArrayList<InfoCallLog> list = null;
        private InfoCallLog callLogList;

        private CallLogsAdapter(Context context,int layoutId,ArrayList<InfoCallLog> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            callLogList = list.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtCallNo = (TextView)v.findViewById(R.id.txtCallName);
                viewHolder.txtCallStatus = (TextView)v.findViewById(R.id.txtCallStatusNew);
                viewHolder.txtCallDate = (TextView)v.findViewById(R.id.txtCallDate);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                holder.txtCallDate.setText(callLogList.getCallDateStr());
                if(cBoxShowNoticeContent){
                    holder.txtCallNo.setText(callLogList.getCallName());
                }else{
                    holder.txtCallNo.setText(getActivity().getString(R.string.miss_call));
                }
                if(callLogList.getIsRead().equals("0")){
                    holder.txtCallStatus.setText(getActivity().getString(R.string.bottom_bar_new_notice)+"!");
                }else{
                    holder.txtCallStatus.setText("");
                }
                if(getDate(callLogList.getCallDate()).equals(getActivity().getString(R.string.bottom_bar_today))){
                    //holder.txtCallDate.setTextColor(Color.YELLOW);
                }else{
                    //holder.txtCallDate.setTextColor(Color.LTGRAY);
                }
            }catch(Exception e){
                new MyCLocker(getActivity()).saveErrorLog(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            return (getActivity().getLayoutInflater().inflate(layoutId,parent,false));
        }

        private String getDate(long date){
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
            int today;
            Calendar cld = Calendar.getInstance();
            today = cld.get(Calendar.DAY_OF_YEAR);
            cld.setTimeInMillis(date);

            if(today==cld.get(Calendar.DAY_OF_YEAR)){
                return getActivity().getString(R.string.bottom_bar_today);
            }else{
                return df.format(new Date(date)).toString();
            }
        }

        private class ViewHolder{
            private TextView txtCallNo;
            private TextView txtCallStatus;
            private TextView txtCallDate;
        }
    }
}