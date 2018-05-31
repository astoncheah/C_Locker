package com.ccs.lockscreen.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.InfoSms;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FragmentSMS extends Fragment{
    private static final String SMS_LIST = "unreadSMSList";
    private static final String SMS_LIST_SAVED = "unreadSMSListSaved";
    private ListView listView;
    private TextView txtSmsNotSupport;
    private ArrayList<InfoSms> unreadSMSList = new ArrayList<InfoSms>();
    private ProgressBar progressBar;
    private boolean cBoxShowNoticeContent, isSMSNoticeSupported;
    private String errorMsg;

    public static FragmentSMS newInstance(ArrayList<InfoSms> unreadSMSList){
        FragmentSMS f = new FragmentSMS();
        Bundle b = new Bundle();
        b.putParcelableArrayList(SMS_LIST,unreadSMSList);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.list_fragment,container,false);
        listView = (ListView)rootView.findViewById(R.id.listNotifications);
        txtSmsNotSupport = (TextView)rootView.findViewById(R.id.txtEmptyFragmentListMsg);
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);

        SharedPreferences prefs = getActivity().getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        cBoxShowNoticeContent = prefs.getBoolean("cBoxShowNoticeContent",true);
        isSMSNoticeSupported = prefs.getBoolean("isSMSNoticeSupported",false);

        Bundle b = getArguments();
        if((b!=null) && b.containsKey(SMS_LIST)){
            unreadSMSList = b.getParcelableArrayList(SMS_LIST);
        }else if((savedInstanceState!=null) && savedInstanceState.containsKey(SMS_LIST_SAVED)){
            unreadSMSList = savedInstanceState.getParcelableArrayList(SMS_LIST_SAVED);
        }

        setCldOnClick(true);
        if(isSMSNoticeSupported){
            new LoadUnreadSMS(getActivity()).execute();
        }else{
            listView.setVisibility(View.GONE);
            txtSmsNotSupport.setVisibility(View.VISIBLE);
            txtSmsNotSupport.setText("(SMS)"+getActivity().getString(R.string.bottom_bar_notice_not_supported_assist));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList(SMS_LIST_SAVED,unreadSMSList);
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
                    InfoSms smsList = unreadSMSList.get(position);
                    try{
                        Intent i = new Intent(getActivity().getPackageName()+C.PAGER_CLICK_NOTICE);
                        i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_SMS);
                        i.putExtra(C.SMS_ID,"smsto:"+smsList.getSmsNo());
                        getActivity().sendBroadcast(i);
                    }catch(Exception e){
                        new MyCLocker(getActivity()).saveErrorLog(null,e);
                        e.printStackTrace();
                    }
                }
            });
            txtSmsNotSupport.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    isSMSUriValid();
                    new MyCLocker(getActivity()).sentLogs("SMS Notice Not Supported Information",errorMsg,true);
                }
            });
        }
    }

    private boolean isSMSUriValid(){
        try{
            Cursor cursor = null;
            try{
                final Uri uri = Uri.parse(C.SMS_URI);
                final String[] projection = new String[]{"*"};
                final String selection = ("read"+"=?");
                final String[] arg = new String[]{"0"};//"1"=read,"0"=unread
                final String sort = "date DESC";//"startDay ASC, startMinute ASC"

                cursor = getActivity().getContentResolver().query(uri,projection,selection,arg,sort);
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return true;
        }catch(IllegalArgumentException e){
            new MyCLocker(getActivity()).saveErrorLog(null,e);
            return false;
        }catch(IllegalStateException e){
            new MyCLocker(getActivity()).saveErrorLog(null,e);
            return false;
        }catch(NullPointerException e){
            new MyCLocker(getActivity()).saveErrorLog(null,e);
            return false;
        }catch(Exception e){
            new MyCLocker(getActivity()).saveErrorLog(null,e);
            return false;
        }
    }

    /*
        if(cursor!=null&&cursor.getCount()>0){
            cursor.moveToFirst();
            do{
                for(int i=0;i<cursor.getColumnCount();i++){
                    try{
                        Log.e("UNREAD_SMS","("+i+")"+cursor.getColumnName(i)+": "+cursor.getString(i));
                    }catch(Exception e){
                        Log.e("UNREAD_SMS","Exception("+i+")"+cursor.getColumnName(i));
                    }
                }
            }while(cursor.moveToNext());
        }

        final Uri uri = Uri.parse("content://mms-sms/conversations?simple=true");
        (0)_id: 14
        (1)date: 1396315857005
        (2)message_count: 16
        (3)recipient_ids: 14
        (4)snippet: I35353
        (5)snippet_cs: 0
        (6)read: 1
        (7)type: 0
        (8)error: 0
        (9)has_attachment: 0
        (10)unread_count: 0
        (11)alert_expired: 1
        (12)reply_all: -1
        (13)group_snippet: null
        (14)message_type: 0
        (15)display_recipient_ids: 14
        (16)translate_mode: off
        (17)secret_mode: 0

        final Uri uri = Uri.parse("content://sms/inbox");
        int phoneNumber = cursorSMS.getColumnIndex("address");
        int content = cursorSMS.getColumnIndexOrThrow("body");
        int date = cursorSMS.getColumnIndexOrThrow("date");

        (0)_id: 21
        (1)thread_id: 4
        (2)address: +60124275598
        (3)person: null
        (4)date: 1394419876372
        (5)date_sent: 1394419876000
        (6)protocol: 0
        (7)read: 1
        (8)status: -1
        (9)type: 1
        (10)reply_path_present: 0
        (11)subject: null
        (12)body: Lunch time is 11.45am. U want 12pm also can.
        (13)service_center: +60120000015
        (14)locked: 0
        (15)error_code: 0
        (16)seen: 1
        (17)deletable: 0
        (18)hidden: 0
        (19)group_id: null
        (20)group_type: null
        (21)delivery_date: null
        (22)app_id: 0
        (23)msg_id: 0
        (24)callback_number: null
        (25)reserved: 0
        (26)pri: 0
        (27)teleservice_id: 0
        (28)link_url: null
        (29)svc_cmd: 0
        (30)svc_cmd_content: null
        (31)roam_pending: 0

        final Uri uri = Uri.parse("content://mms/inbox");
        onLoadFinished(0): _id
        onLoadFinished(1): thread_id
        onLoadFinished(2): date
        onLoadFinished(3): date_sent
        onLoadFinished(4): msg_box
        onLoadFinished(5): read
        onLoadFinished(6): m_id
        onLoadFinished(7): sub
        onLoadFinished(8): sub_cs
        onLoadFinished(9): ct_t
        onLoadFinished(10): ct_l
        onLoadFinished(11): exp
        onLoadFinished(12): m_cls
        onLoadFinished(13): m_type
        onLoadFinished(14): v
        onLoadFinished(15): m_size
        onLoadFinished(16): pri
        onLoadFinished(17): rr
        onLoadFinished(18): rpt_a
        onLoadFinished(19): resp_st
        onLoadFinished(20): st
        onLoadFinished(21): tr_id
        onLoadFinished(22): retr_st
        onLoadFinished(23): retr_txt
        onLoadFinished(24): retr_txt_cs
        onLoadFinished(25): read_status
        onLoadFinished(26): ct_cls
        onLoadFinished(27): resp_txt
        onLoadFinished(28): d_tm
        onLoadFinished(29): d_rpt
        onLoadFinished(30): locked
        onLoadFinished(31): seen
        onLoadFinished(32): deletable
        onLoadFinished(33): hidden
        onLoadFinished(34): app_id
        onLoadFinished(35): msg_id
        onLoadFinished(36): callback_set
        onLoadFinished(37): reserved
        onLoadFinished(38): text_only
    */
    private class LoadUnreadSMS extends AsyncTask<Void,Void,ArrayList<InfoSms>>{
        private Context context;

        public LoadUnreadSMS(Context context){
            this.context = context;
        }

        @Override
        protected ArrayList<InfoSms> doInBackground(Void... args){
            try{
                return loadUnreadSMS(loadSMSCursor());
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(ArrayList<InfoSms> tempUnreadSMSList){
            try{
                if(tempUnreadSMSList!=null && tempUnreadSMSList.size()>0){
                    unreadSMSList = tempUnreadSMSList;
                    final SmsAdapter UnreadSMSAdapter = new SmsAdapter(context,R.layout.list_fragment_sms,unreadSMSList);
                    listView.setAdapter(UnreadSMSAdapter);
                }else if(errorMsg!=null){
                    listView.setVisibility(View.GONE);
                    txtSmsNotSupport.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
                e.printStackTrace();
            }
        }

        private ArrayList<InfoSms> loadUnreadSMS(Cursor cursor){
            ArrayList<InfoSms> tempUnreadSMSList = new ArrayList<InfoSms>();
            try{
                if(cursor!=null && cursor.getCount()>0){
                    try{
                        int threadId = cursor.getColumnIndexOrThrow("_id");
                        int content = cursor.getColumnIndexOrThrow("snippet");
                        int date = cursor.getColumnIndexOrThrow("date");
                        int isRead = cursor.getColumnIndexOrThrow("read");

                        cursor.moveToFirst();
                        do{
                            if(getPhoneNumber(cursor.getString(threadId))!=null){
                                tempUnreadSMSList.add(new InfoSms(getPhoneNumber(cursor.getString(threadId)),getContactDisplayNameByNumber(getPhoneNumber(cursor.getString(threadId))),getDate(cursor.getLong(date))+", "+getTime(cursor.getLong(date)),cursor.getString(content),cursor.getString(isRead),cursor.getLong(date)));
                            }
                        }while(cursor.moveToNext());
                    }catch(Exception e){
                        errorMsg = "Exception";
                        new MyCLocker(context).saveErrorLog(null,e);
                        if(cursor!=null && cursor.getCount()>0){
                            cursor.moveToFirst();
                            do{
                                for(int i = 0; i<cursor.getColumnCount(); i++){
                                    try{
                                        new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SMS_ERROR_CURSOR:("+i+"){"+
                                                cursor.getColumnName(i)+"}: {"+
                                                cursor.getString(i)+"}");
                                    }catch(Exception e1){
                                        new MyCLocker(context).writeToFile(C.FILE_BACKUP_RECORD,"SMS_ERROR(Exception)_CURSOR:("+i+"){"+
                                                cursor.getColumnName(i)+"}");
                                    }
                                }
                            }while(cursor.moveToNext());
                        }
                        return null;
                    }
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return tempUnreadSMSList;
        }

        private Cursor loadSMSCursor(){
            try{
                Calendar cld = Calendar.getInstance();
                long longFilterStart = cld.getTimeInMillis()-(DateUtils.DAY_IN_MILLIS*7);

                //final Uri uri = Uri.parse("content://sms/inbox");
                //final Uri uri = Uri.parse("content://mms/inbox");
                final Uri uri = Uri.parse(C.SMS_URI);
                String[] projection = new String[]{"*"};
                //String selection = ("read"+ "=?");
                //String[] arg = new String[]{"0"};//"1"=read,"0"=unread
                String selection = ("date >=?");
                String[] arg = new String[]{longFilterStart+""};
                String sort = "date DESC";//"startDay ASC, startMinute ASC"
                return context.getContentResolver().query(uri,projection,selection,arg,sort);
            }catch(IllegalArgumentException e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }catch(IllegalStateException e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }catch(NullPointerException e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }
        }

        private String getPhoneNumber(String id){
            try{
                Cursor c = null;
                try{
                    //String threadId = TextBasedSmsColumns.THREAD_ID;// Kitkat+

                    final Uri uri = Uri.parse("content://sms/inbox");
                    String[] projection = new String[]{"thread_id","address"};
                    String selection = ("thread_id"+"=?");
                    String[] arg = new String[]{id};
                    c = context.getContentResolver().query(uri,projection,selection,arg,null);

                    c.moveToFirst();
                    if(c.getString(c.getColumnIndex("address"))!=null){
                        //Log.e("getPhoneNumber",c.getString(c.getColumnIndex("thread_id"))+"//"+id);
                        return c.getString(c.getColumnIndex("address"));
                    }
                }finally{
                    if(c!=null){
                        c.close();
                        c = null;
                    }
                }
            }catch(Exception e){
                //new MyCLocker(context).writeToFile(
                //ServiceMain.EXT_BACKUP_DIR,ServiceMain.FILE_BACKUP_RECORD,
                //"FragmentSMS>LoadUnreadSMS>getPhoneNumber>error id: "+ id);
            }
            return null;
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

        private String getDate(final long date){
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

        private String getTime(final long time){
            //SimpleDateFormat tf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT,Locale.getDefault());
            return tf.format(new Date(time)).toString();
        }
    }

    private class SmsAdapter extends ArrayAdapter<InfoSms>{
        private ViewHolder holder;
        private int layoutId;
        private ArrayList<InfoSms> list = null;
        private InfoSms smsList;

        private SmsAdapter(Context context,int layoutId,ArrayList<InfoSms> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            smsList = list.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtSmsName = (TextView)v.findViewById(R.id.txtSmsName);
                viewHolder.txtSmsStatus = (TextView)v.findViewById(R.id.txtSmsStatusNew);
                viewHolder.txtSmsDate = (TextView)v.findViewById(R.id.txtSmsDate);
                viewHolder.txtSmsContent = (TextView)v.findViewById(R.id.txtSmsContent);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                holder.txtSmsDate.setText(smsList.getSmsDateStr());
                if(cBoxShowNoticeContent){
                    holder.txtSmsName.setText(smsList.getSmsName());
                    holder.txtSmsContent.setText(smsList.getSmsContent());
                }else{
                    holder.txtSmsName.setText(getActivity().getString(R.string.sms));
                }
                if(smsList.getUnReadCount().equals("0")){
                    holder.txtSmsStatus.setText(getActivity().getString(R.string.bottom_bar_new_notice)+"!");
                }else{
                    holder.txtSmsStatus.setText("");
                }
                if(getDate(smsList.getSmsDate()).equals(getActivity().getString(R.string.bottom_bar_today))){
                    //holder.txtSmsDate.setTextColor(Color.YELLOW);
                }else{
                    //holder.txtSmsDate.setTextColor(Color.LTGRAY);
                }
            }catch(Exception e){
                new MyCLocker(getActivity()).saveErrorLog(null,e);
                e.printStackTrace();
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            return (getActivity().getLayoutInflater().inflate(layoutId,parent,false));
        }

        private String getDate(final long date){
            final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
            final int today;
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
            private TextView txtSmsName;
            private TextView txtSmsStatus;
            private TextView txtSmsDate;
            private TextView txtSmsContent;
        }
    }
}