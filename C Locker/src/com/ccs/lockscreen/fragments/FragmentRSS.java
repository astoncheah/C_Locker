package com.ccs.lockscreen.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataRSS;
import com.ccs.lockscreen.data.InfoRSS;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

import java.util.ArrayList;

public final class FragmentRSS extends Fragment{
    private ListView listView;
    private ArrayList<InfoRSS> rssList;
    private TextView txtEmptyRSS;
    private boolean cBoxEnableRSS;

    public static FragmentRSS newInstance(){
        FragmentRSS fragment = new FragmentRSS();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.list_fragment,container,false);
        listView = (ListView)rootView.findViewById(R.id.listNotifications);
        txtEmptyRSS = (TextView)rootView.findViewById(R.id.txtEmptyFragmentListMsg);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        final SharedPreferences prefs = getActivity().getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        cBoxEnableRSS = prefs.getBoolean("cBoxEnableRSS",false);
        setCldOnClick(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        loadData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        //outState.putParcelableArrayList(CALLLOG_LIST,rssList);
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

    private void loadData(){
        DataRSS db = null;
        try{
            if(cBoxEnableRSS){
                db = new DataRSS(getActivity());
                rssList = (ArrayList<InfoRSS>)db.getAllRSS();

                if(rssList!=null && rssList.size()>0){
                    final RSSAdapter adapter = new RSSAdapter(getActivity(),R.layout.list_fragment_rss,rssList);
                    listView.setAdapter(adapter);
                    listView.setVisibility(View.VISIBLE);
                    txtEmptyRSS.setVisibility(View.GONE);
                }else{
                    listView.setVisibility(View.GONE);
                    txtEmptyRSS.setVisibility(View.VISIBLE);
                    txtEmptyRSS.setText("Tap to refresh");
                }
            }
        }catch(Exception e){
            new MyCLocker(getActivity()).saveErrorLog(null,e);
        }
        if(db!=null){
            db.close();
            db = null;
        }
    }

    private void setCldOnClick(final boolean bln){
        if(bln){
            listView.setOnItemClickListener(new OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> arg,View v,int position,long id){
                    try{
                        InfoRSS rss = rssList.get(position);
                        Intent i = new Intent(getActivity().getPackageName()+C.PAGER_CLICK_NOTICE);
                        i.putExtra(C.SINGE_CLICK_ACTION,C.LAUNCH_SINGLE_RSS);
                        i.putExtra(C.RSS_LINK,rss.getRssLink());
                        getActivity().sendBroadcast(i);
                    }catch(Exception e){
                        new MyCLocker(getActivity()).saveErrorLog(null,e);
                    }
                }
            });
            txtEmptyRSS.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    try{
                        Intent i = new Intent(getActivity().getPackageName()+C.PAGER_TAB_NOTICE);
                        i.putExtra(C.TAB_ACTION,C.LAUNCH_RSS);
                        getActivity().sendBroadcast(i);
                    }catch(Exception e){
                        Toast.makeText(getActivity(),"RSS Launch Error",Toast.LENGTH_SHORT).show();
                        new MyCLocker(getActivity()).saveErrorLog(null,e);
                    }
                }
            });
        }
    }

    private class RSSAdapter extends ArrayAdapter<InfoRSS>{
        private ViewHolder holder;
        private int layoutId;
        private InfoRSS rss;
        private ArrayList<InfoRSS> list;

        private RSSAdapter(Context context,int layoutId,ArrayList<InfoRSS> list){
            super(context,layoutId,list);
            this.layoutId = layoutId;
            this.list = list;
        }

        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            View v = convertView;
            rss = list.get(position);
            if(v==null){
                v = newView(parent);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.txtTitle = (TextView)v.findViewById(R.id.txt_rss_title);
                viewHolder.txtPost = (TextView)v.findViewById(R.id.txt_rss_postno);
                viewHolder.txtPubDate = (TextView)v.findViewById(R.id.txt_rss_pubdate);
                viewHolder.txtDesc = (TextView)v.findViewById(R.id.txt_rss_desc);
                v.setTag(viewHolder);
            }
            try{
                holder = (ViewHolder)v.getTag();
                holder.txtTitle.setText(rss.getRssTitle());
                holder.txtPost.setText("#"+(position+1));
                holder.txtPubDate.setText("("+rss.getRssPubDate()+")");
                holder.txtDesc.setText(rss.getRssDesc());
                holder.txtDesc.setMaxLines(3);
                holder.txtDesc.setEllipsize(TruncateAt.END);
            }catch(Exception e){
                new MyCLocker(getActivity()).saveErrorLog(null,e);
            }
            return (v);
        }

        private View newView(ViewGroup parent){
            return (getActivity().getLayoutInflater().inflate(layoutId,parent,false));
        }

        private class ViewHolder{
            private TextView txtTitle;
            private TextView txtPost;
            private TextView txtPubDate;
            private TextView txtDesc;
        }
    }
}