package com.ccs.lockscreen_pro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

import java.util.List;

public class SettingsAddLocation extends BaseActivity{
    public static final String LOCATION_ID = "LOCATION_ID";
    public static final String LOCATION_LAT = "LOCATION_LAT";
    public static final String LOCATION_LON = "LOCATION_LON";
    public static final String LOCATION_CITY_NAME = "LOCATION_CITY_NAME";
    public static final String LOCATION_DISTANCE_COVERAGE = "LOCATION_DISTANCE_COVERAGE";
    public static final int LOCATION_INVALID_ID = -99;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private LinearLayout lytProfileMain;
    private DataAppsSelection shortcutData;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.select_location);
        setBasicBackKeyAction();
        try{
            prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            editor = prefs.edit();

            lytProfileMain = (LinearLayout)findViewById(R.id.lytProfileMain);
            shortcutData = new DataAppsSelection(this);
            loadSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        if(shortcutData!=null){
            shortcutData.close();
            shortcutData = null;
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_profiles_add_location;
    }

    private void loadSettings(){
        //import old settings if not yet imported
        final boolean isOldSavedLocationImported = prefs.getBoolean("isOldSavedLocationImported",false);
        if(!isOldSavedLocationImported){
            final String lat = prefs.getString("locationProfileLat","0");
            final String lon = prefs.getString("locationProfileLon","0");
            final String cityName = prefs.getString("locationProfileCityName",this.getString(R.string.map_location_null));
            final int profileCoverage = prefs.getInt("intLocationDistanceCover",0);

            //AppPkg = lat
            //SppClass = lon
            //AppName = city name
            //ShortcutAction = distance coverage
            if(!lat.equals("0") && !lon.equals("0")){
                final InfoAppsSelection app = new InfoAppsSelection();
                app.setAppType(DataAppsSelection.APP_TYPE_SAVED_LOCATION);
                app.setAppPkg(lat);
                app.setAppClass(lon);
                app.setAppName(cityName);
                app.setShortcutAction(profileCoverage);
                shortcutData.addApp(app);
            }
            editor.putBoolean("isOldSavedLocationImported",true);
            editor.commit();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        saveSettings();
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            loadViews();
        }catch(Exception e){

        }
    }

    private void saveSettings(){
        editor.putBoolean("cBoxProfileLocation",lytProfileMain.getChildCount()>1);
        editor.commit();
    }

    private void loadViews(){
        lytProfileMain.removeAllViews();
        final List<InfoAppsSelection> list = shortcutData.getApps(DataAppsSelection.APP_TYPE_SAVED_LOCATION);
        int count = 0;
        for(InfoAppsSelection item : list){
            if(item!=null){
                count++;
                lytProfileMain.addView(lytLocation(item,count));
                //new MyCLocker(SettingsAddLocation.this).writeToFile(C.FILE_BACKUP_RECORD,
                //"SettingsAddLocation>loadViews: "+item.getAppName()+"/"+item.getShortcutAction());
            }
        }
        lytProfileMain.addView(lytLocation());
    }

    //new
    private LinearLayout lytLocation(){
        return lytLocation(null,-1);
    }

    private LinearLayout lytLocation(final InfoAppsSelection item,int no){
        final Drawable d = ContextCompat.getDrawable(getBaseContext(),R.drawable.layout_selector);
        final int pad = C.dpToPx(this,16);
        final LinearLayout lyt = new LinearLayout(this);
        lyt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        lyt.setPadding(0,C.dpToPx(this,10),0,C.dpToPx(this,10));
        lyt.setOrientation(LinearLayout.HORIZONTAL);
        lyt.setGravity(Gravity.CENTER_VERTICAL);
        lyt.setPadding(pad,pad,pad,pad);
        lyt.setBackground(d);

        if(item==null){//new location
            lyt.addView(addTxtHead(getString(R.string.add_location)));
            lyt.addView(img(R.drawable.ic_add_white_48dp,LOCATION_INVALID_ID));
            lyt.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    //AppPkg = lat
                    //SppClass = lon
                    //AppName = city name
                    //ShortcutAction = distance coverage
                    Intent i = new Intent(SettingsAddLocation.this,SettingsMap.class);
                    i.putExtra(LOCATION_ID,LOCATION_INVALID_ID);
                    i.putExtra(LOCATION_LAT,"0");
                    i.putExtra(LOCATION_LON,"0");
                    i.putExtra(LOCATION_CITY_NAME,getString(R.string.map_location_null));
                    i.putExtra(LOCATION_DISTANCE_COVERAGE,200);//default 200
                    startActivity(i);
                }
            });
        }else{
            lyt.addView(addTxtHead(no+". "+item.getAppName()));
            lyt.addView(img(R.drawable.ic_delete_white_48dp,item.getId()));
            lyt.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    //AppPkg = lat
                    //SppClass = lon
                    //AppName = city name
                    //ShortcutAction = distance coverage
                    Intent i = new Intent(SettingsAddLocation.this,SettingsMap.class);
                    i.putExtra(LOCATION_ID,item.getId());
                    i.putExtra(LOCATION_LAT,item.getAppPkg());
                    i.putExtra(LOCATION_LON,item.getAppClass());
                    i.putExtra(LOCATION_CITY_NAME,item.getAppName());
                    i.putExtra(LOCATION_DISTANCE_COVERAGE,item.getShortcutAction());//default 200
                    startActivity(i);
                    //new MyCLocker(SettingsAddLocation.this).writeToFile(C.FILE_BACKUP_RECORD,
                    //"SettingsAddLocation>lytLocation: "+item.getAppName()+"/"+item.getShortcutAction());
                }
            });
        }
        return lyt;
    }

    private TextView addTxtHead(String str){
        final TextView txt = new TextView(this);
        txt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2,1));
        txt.setText(str);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        //txt.setTypeface(Typeface.DEFAULT_BOLD);
        return txt;
    }

    private ImageView img(int resid,final int locationId){
        //final Drawable d = this.getResources().getDrawable(R.drawable.layout_selector);
        final int size = C.dpToPx(this,(int)(C.ICON_HEIGHT*0.8f));
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        final ImageView img = new ImageView(this);
        img.setLayoutParams(pr);
        img.setBackgroundResource(resid);
        //img.setBackground(d);

        if(resid==R.drawable.ic_delete_white_48dp){
            img.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    shortcutData.deleteLocation(locationId);
                    loadViews();
                }
            });
        }
        return img;
    }
}
