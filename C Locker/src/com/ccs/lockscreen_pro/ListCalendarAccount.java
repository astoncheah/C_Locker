package com.ccs.lockscreen_pro;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.InfoCalendarAccount;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class ListCalendarAccount extends BaseActivity{
    //=====
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private CalendarAdapter adapter = null;
    private List<InfoCalendarAccount> cldList = null;
    private List<String> cldSavedlist;
    private boolean isCldAccountSelected;
    private ListView list;
    private Button btnCheckAll, btnUncheckAll;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.calendar_account);
        setBasicBackKeyAction();
        try{
            list = (ListView)findViewById(R.id.listSelector);
            btnCheckAll = (Button)findViewById(R.id.btnCalendarCheckAll);
            btnUncheckAll = (Button)findViewById(R.id.btnCalendarUncheckAll);

            loadSettings();
            buttonFunction();

            final int _ID = 0, ACCOUNT_NAME = 1, CALENDAR_DISPLAY_NAME = 2;
            cldList = new ArrayList<InfoCalendarAccount>();

            Uri uri = Calendars.CONTENT_URI;
            String[] projection = new String[]{Calendars._ID,
                    //Calendars.OWNER_ACCOUNT,
                    Calendars.ACCOUNT_NAME,Calendars.CALENDAR_DISPLAY_NAME};
            String sort = Calendars.ACCOUNT_NAME+" ASC,"+
                    Calendars.CALENDAR_DISPLAY_NAME+" ASC";//"startDay ASC, startMinute ASC"
            Cursor cursor = getContentResolver().query(uri,projection,null,null,sort);
            try{
                if(cursor!=null && cursor.getCount()>0){
                    cursor.moveToFirst();
                    do{
                        cldList.add(new InfoCalendarAccount(cursor.getString(_ID),cursor.getString(ACCOUNT_NAME),cursor.getString(CALENDAR_DISPLAY_NAME)));
                    }while(cursor.moveToNext());
                }
            }finally{
                if(cursor!=null){
                    cursor.close();
                }
            }
            for(int i = 0; i<cldSavedlist.size(); i++){
                for(int pos = 0; pos<cldList.size(); pos++){
                    if(cldSavedlist.get(i).equals(cldList.get(pos).getId())){
                        cldList.get(pos).setSelected(true);
                        //Log.e("setSelected: ",cldList.get(pos).getId());
                    }
                }
            }
            loadData();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            adapter.clearObjects();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.list_cld_account_main;
    }

    private void loadSettings(){
        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        isCldAccountSelected = prefs.getBoolean("isCldAccountSelected",false);
        String str = prefs.getString("strCalendarAccount","");
        String[] items = str.split(",");
        cldSavedlist = new ArrayList<String>();
        for(int i = 0; i<items.length; i++){
            cldSavedlist.add(items[i].toString());
        }
        //Log.e("load",str);
    }

    private void buttonFunction(){
        btnCheckAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                for(int pos = 0; pos<cldList.size(); pos++){
                    cldList.get(pos).setSelected(true);
                }
                loadData();
            }
        });
        btnUncheckAll.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                for(int pos = 0; pos<cldList.size(); pos++){
                    cldList.get(pos).setSelected(false);
                }
                loadData();
            }
        });
    }

    private void loadData(){
        try{
            adapter = new CalendarAdapter(this,R.layout.list_cld_account,cldList);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent,View view,int position,long id){
                    //NOTHING
                }
            });
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void saveSettings(){
        editor = getSharedPreferences(C.PREFS_NAME,0).edit();
        StringBuilder csvList = new StringBuilder();
        for(int i = 0; i<cldList.size(); i++){
            if(cldList.get(i).isSelected()){
                csvList.append(cldList.get(i).getId());
                csvList.append(",");
            }
        }
        if(csvList.length()==0){
            isCldAccountSelected = false;
        }
        editor.putBoolean("isCldAccountSelected",isCldAccountSelected);
        editor.putString("strCalendarAccount",csvList.toString());
        editor.commit();
        //Log.e("save",csvList.toString());
    }

    private class ViewHolder{
        private TextView txtCldAccountName, txtCldAccountEmail;
        private CheckBox cBoxCldAccount;
    }

    private class CalendarAdapter extends ArrayAdapter<InfoCalendarAccount>{
        private ViewHolder holder;
        private int layoutId;

        public CalendarAdapter(Context context,int layoutId,List<InfoCalendarAccount> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
        }

        @Override
        public View getView(final int position,View convertView,ViewGroup parent){
            View view = null;
            if(convertView==null){
                view = newView(parent);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtCldAccountEmail = (TextView)view.findViewById(R.id.txt_cld_account_email);
                viewHolder.txtCldAccountName = (TextView)view.findViewById(R.id.txt_cld_account_name);
                viewHolder.cBoxCldAccount = (CheckBox)view.findViewById(R.id.cBox_cld_account_name);
                viewHolder.cBoxCldAccount.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View v){
                        InfoCalendarAccount element = (InfoCalendarAccount)viewHolder.cBoxCldAccount.getTag();
                        element.setSelected(viewHolder.cBoxCldAccount.isChecked());
                        isCldAccountSelected = true;
                        //Log.e("onCheckedChanged",position+"/"+cldList.get(position).getId()+"/"+cldList.get(position).isSelected());
                    }
                });
                view.setTag(viewHolder);
                viewHolder.cBoxCldAccount.setTag(cldList.get(position));
            }else{
                view = convertView;
                ((ViewHolder)view.getTag()).cBoxCldAccount.setTag(cldList.get(position));
            }
            try{
                holder = (ViewHolder)view.getTag();
                holder.txtCldAccountEmail.setText(cldList.get(position).getEmail());
                holder.txtCldAccountName.setText(cldList.get(position).getName());
                holder.cBoxCldAccount.setChecked(cldList.get(position).isSelected());
                //Log.e("getView",position+"/"+cldList.get(position).getId()+"/"+cldList.get(position).isSelected());
            }catch(Exception e){
                saveErrorLogs(null,e);
            }
            return (view);
        }

        private View newView(ViewGroup parent){
            return (getLayoutInflater().inflate(layoutId,parent,false));
        }

        /*private boolean setCheckBox(int position){
            for(int i=0;i<cldSavedlist.size();i++){
                if(cldSavedlist.get(i).equals(cldList.get(position).getId())){
                    return true;
                }
            }
            return false;
        }*/
        private void clearObjects(){
            clear();
        }
    }
}
