package com.ccs.lockscreen.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.InfoGmail;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.utils.GmailContract;

import java.util.ArrayList;

public final class FragmentGmail extends Fragment{
    private static final String GMAIL_ACCOUNT = "gmailAccount";
    private static final String GMAIL_LIST_SAVED = "gmailListSaved";
    private ListView listView;
    private TextView txtGmailNotSupport;
    private ProgressBar progressBar;
    private ArrayList<InfoGmail> gmailList = new ArrayList<>();
    private String gmailAccount;

    public static FragmentGmail newInstance(String gmailAccount){
        FragmentGmail f = new FragmentGmail();
        Bundle b = new Bundle();
        b.putString(GMAIL_ACCOUNT,gmailAccount);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.list_fragment,container,false);
        listView = (ListView)rootView.findViewById(R.id.listNotifications);
        txtGmailNotSupport = (TextView)rootView.findViewById(R.id.txtEmptyFragmentListMsg);
        progressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        Bundle b = getArguments();
        if((b!=null) && b.containsKey(GMAIL_ACCOUNT)){
            gmailAccount = b.getString(GMAIL_ACCOUNT);
        }
        if((savedInstanceState!=null) && savedInstanceState.containsKey(GMAIL_LIST_SAVED)){
            gmailList = savedInstanceState.getParcelableArrayList(GMAIL_LIST_SAVED);
        }
        if(GmailContract.isGmailSupported(getActivity())){
            new LoadGmail(getActivity()).execute();
        }else{
            txtGmailNotSupport.setVisibility(View.VISIBLE);
            txtGmailNotSupport.setText("(Gmail)"+getActivity().getString(R.string.bottom_bar_notice_not_supported_assist));
        }
        setCldOnClick(true);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelableArrayList(GMAIL_LIST_SAVED,gmailList);
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
                    try{
                        final Intent i = new Intent(getActivity().getPackageName()+C.PAGER_CLICK_NOTICE);
                        i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_NOTICE);
                        getActivity().sendBroadcast(i);
                    }catch(Exception e){
                        new MyCLocker(getActivity()).saveErrorLog(null,e);
                        Toast.makeText(getActivity(),"No Gmail app found",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            txtGmailNotSupport.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    new MyCLocker(getActivity()).sentLogs("Gmail Notice Not Supported Information","",true);
                }
            });
        }
    }

    private class LoadGmail extends AsyncTask<Void,Void,ArrayList<InfoGmail>>{
        private Context context;

        private LoadGmail(Context context){
            this.context = context;
        }

        @Override
        protected ArrayList<InfoGmail> doInBackground(Void... args){
            try{
                return loadUnreadGmail(loadGmailCursor());
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
        protected void onPostExecute(ArrayList<InfoGmail> tempGmailList){
            try{
                if(tempGmailList!=null && tempGmailList.size()>0){
                    gmailList = tempGmailList;
                    final GmailAdapter gmailAdapter = new GmailAdapter(context,R.layout.list_fragment_gmail,gmailList);
                    listView.setAdapter(gmailAdapter);
                }
                progressBar.setVisibility(View.GONE);
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }

        private ArrayList<InfoGmail> loadUnreadGmail(Cursor cursor){
            ArrayList<InfoGmail> tempGmailList = new ArrayList<InfoGmail>();
            try{
                if(cursor!=null && cursor.getCount()>0){
                    final String filter1 = GmailContract.Labels.LabelCanonicalNames.CANONICAL_NAME_INBOX;
                    final String filter2 = "^sq_ig_i";
                    //^sq_if_i... = Primary,Social,Promotion,Updates,Forums inbox

                    final int CANONICAL_NAME = cursor.getColumnIndexOrThrow(GmailContract.Labels.CANONICAL_NAME);
                    final int NAME = cursor.getColumnIndexOrThrow(GmailContract.Labels.NAME);
                    final int READ = cursor.getColumnIndexOrThrow(GmailContract.Labels.NUM_CONVERSATIONS);
                    final int UNREAD = cursor.getColumnIndexOrThrow(GmailContract.Labels.NUM_UNREAD_CONVERSATIONS);

                    cursor.moveToFirst();
                    do{
                        if(cursor.getString(CANONICAL_NAME).equals(filter1) || cursor.getString(CANONICAL_NAME).startsWith(filter2)){
                            tempGmailList.add(new InfoGmail(cursor.getString(NAME),cursor.getString(READ),cursor.getString(UNREAD)));
                        }
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            return tempGmailList;
        }

        private Cursor loadGmailCursor(){
            try{
                final Uri labelsUri = GmailContract.Labels.getLabelsUri(gmailAccount);
                return context.getContentResolver().query(labelsUri,null,null,null,null);
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
                return null;
            }
        }
    }

    private class GmailAdapter extends ArrayAdapter<InfoGmail>{
        private ViewHolder holder;
        private int layoutId;
        private ArrayList<InfoGmail> list = null;
        private InfoGmail gmailList;

        private GmailAdapter(Context context,int layoutId,ArrayList<InfoGmail> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;
        }

        @NonNull
        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            gmailList = list.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtGmailBox = (TextView)v.findViewById(R.id.txtGmailBox);
                viewHolder.txtGmailStatus = (TextView)v.findViewById(R.id.txtGmailStatusNew);
                viewHolder.txtUnread = (TextView)v.findViewById(R.id.txtGmailUnread);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                holder.txtGmailBox.setText(gmailList.getMailBox()+": "+gmailList.getRead());
                holder.txtUnread.setText(getActivity().getString(R.string.bottom_bar_unread)+": "+gmailList.getUnread());
                if(!gmailList.getUnread().equals("0")){
                    holder.txtGmailStatus.setText(getActivity().getString(R.string.bottom_bar_new_notice)+"!");
                }else{
                    holder.txtGmailStatus.setText("");
                }
            }catch(Exception e){
                new MyCLocker(getActivity()).saveErrorLog(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            return (getActivity().getLayoutInflater().inflate(layoutId,parent,false));
        }

        private class ViewHolder{
            private TextView txtGmailBox;
            private TextView txtGmailStatus;
            private TextView txtUnread;
        }
    }
}