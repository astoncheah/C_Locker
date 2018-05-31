package com.ccs.lockscreen_pro;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.LocationNameHandler;
import com.ccs.lockscreen.utils.LocationNameHandler.LocationNameCallBack;
import com.ccs.lockscreen.utils.MyLocationManager;
import com.ccs.lockscreen.utils.MyLocationManager.LocationCallBack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class SettingsMap extends BaseActivity implements LocationCallBack, CancelableCallback,
        LocationNameCallBack{
    //private SharedPreferences prefs;
    //private SharedPreferences.Editor editor;
    private GoogleMap map;
    private Spinner spnLocationDistanceCover;
    private TextView txtName;
    private Circle circle;
    private LatLng latLng;
    private boolean locationButtonClicked = false;
    private boolean isGpsEnabled = true;
    private boolean isLocationSelected = false;
    private int locationId;
    private String lat = "0", lon = "0", cityName;
    private Marker marker1, marker2;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        //no long support since android 21v - requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); //must be called before adding contentview
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_location);
        setBasicBackKeyAction();

        spnLocationDistanceCover = (Spinner)findViewById(R.id.spinLocationDistanceCover);
        txtName = (TextView)findViewById(R.id.txtCurrentLocProfileName);
        try{
            spinnerFunction();
            loadSettings(getIntent().getExtras());
            setUpMapIfOk();
        }catch(Exception e){
            showToastMsg("SettingsMap>onCreate error, please contact the app Developer");
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_map;
    }

    @Override
    protected void onPause(){
        super.onPause();
        saveSettings(marker2);
    }

    @Override
    protected void onResume(){
        super.onResume();
        try{
            setUpMapIfOk();
        }catch(Exception e){
            showToastMsg("SettingsMap>onResume error, please contact the app Developer");
            saveErrorLogs(null,e);
        }
    }

    private void setUpMapIfOk(){
        if(map==null){
            //map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            ((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback(){
                @Override
                public void onMapReady(GoogleMap gMap){
                    if(gMap!=null){
                        map = gMap;
                        setUpMap();
                    }
                }
            });
        }
    }

    private void showToastMsg(String str){
        Toast.makeText(this,str,Toast.LENGTH_LONG).show();
    }

    private void setUpMap(){
        //map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(new OnMapClickListener(){
            @Override
            public void onMapClick(LatLng arg){
                latLng = new LatLng(arg.latitude,arg.longitude);
                final int dis = spnLocationDistanceCover.getSelectedItemPosition();
                final int size = convertDistance(dis);
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,convertZoom(size)));//min 2.0, max 21.0
                setUpMarker(latLng);
                setUpCircle(latLng,size);
                getLocationName();
            }
        });
        map.setOnCameraChangeListener(new OnCameraChangeListener(){
            @Override
            public void onCameraChange(CameraPosition cam){
                //no long support since android 21v - setProgressBarIndeterminateVisibility(false);
                if(locationButtonClicked){
                    locationButtonClicked = false;
                    Log.e("SettingsMap","setOnCameraChangeListener: "+cam.target.latitude+"/"+cam.target.longitude);
                    latLng = new LatLng(cam.target.latitude,cam.target.longitude);
                    final int dis = spnLocationDistanceCover.getSelectedItemPosition();
                    final int size = convertDistance(dis);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,convertZoom(size)));//min 2.0, max 21.0
                    setUpMarker(latLng);
                    setUpCircle(latLng,size);
                    getLocationName();
                }
            }
        });
        map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick(){
                //no long support since android 21v - setProgressBarIndeterminateVisibility(true);
                locationButtonClicked = true;
                return false;
            }
        });
        map.setOnMarkerClickListener(new OnMarkerClickListener(){
            @Override
            public boolean onMarkerClick(Marker m){
                isLocationSelected = true;
                txtName.setText(m.getSnippet());
                SettingsMap.this.marker2 = m;

                //editor.putBoolean("cBoxProfileLocation",true);
                //editor.putString("locationProfileLat",m.getPosition().latitude+"");
                //editor.putString("locationProfileLon",m.getPosition().longitude+"");
                //editor.putString("locationProfileCityName",m.getSnippet());
                //editor.commit();
                return true;
            }
        });
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker m){
                isLocationSelected = true;
                txtName.setText(m.getSnippet());
                SettingsMap.this.marker2 = m;

                //editor.putBoolean("cBoxProfileLocation",true);
                //editor.putString("locationProfileLat",m.getPosition().latitude+"");
                //editor.putString("locationProfileLon",m.getPosition().longitude+"");
                //editor.putString("locationProfileCityName",m.getSnippet());
                //editor.commit();
            }
        });

        //no long support since android 21v - setProgressBarIndeterminateVisibility(true);
        if(locationId==SettingsAddLocation.LOCATION_INVALID_ID){//load current location
            final MyLocationManager mLocation = new MyLocationManager(this,this);
            final Location loc = mLocation.runLocationChecker();
            if(loc!=null){
                latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
                final int dis = spnLocationDistanceCover.getSelectedItemPosition();
                final int size = convertDistance(dis);

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,convertZoom(size)));
                setUpMarker(latLng);
                setUpCircle(latLng,size);
            }
        }else{
            final Location loc = new Location("SettingsMap");
            loc.setLatitude(Double.parseDouble(lat));
            loc.setLongitude(Double.parseDouble(lon));
            if(loc!=null){
                latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
                final int dis = spnLocationDistanceCover.getSelectedItemPosition();
                final int size = convertDistance(dis);

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,convertZoom(size)));
                setUpMarker(latLng);
                setUpCircle(latLng,size);
                getLocationName();//name will not show if not add this
            }
        }
    }

    private int convertDistance(int dis){
        switch(dis){
            default://0
                return 50;
            case 1:
                return 100;
            case 2:
                return 200;
            case 3:
                return 500;
            case 4:
                return 1000;
            case 5:
                return 2000;
            case 6:
                return 5000;
        }
    }

    private float convertZoom(int meter){
        if(!C.isInternetConnected(this) || !isGpsEnabled){
            return 2;
        }
        switch(meter){
            default://50m
                return 18;
            case 100:
                return 17;
            case 200:
                return 16;
            case 500:
                return 15;
            case 1000:
                return 14;
            case 2000:
                return 13;
            case 5000:
                return 12;
        }
    }

    private void setUpMarker(LatLng latLng){
        if(marker1==null){
            marker1 = map.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.map_maker_title)));
        }else{
            marker1.setPosition(latLng);
        }
    }

    private void setUpCircle(LatLng latLng,int size){
        if(circle==null){
            circle = map.addCircle(cOption(latLng,size));
        }else{
            circle.setCenter(latLng);
            circle.setRadius(size);
        }
    }

    private void getLocationName(){
        try{
            //no long support since android 21v - setProgressBarIndeterminateVisibility(true);
            new LocationNameHandler(this,this).execute(latLng);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private CircleOptions cOption(LatLng latLng,int size){
        return new CircleOptions().center(latLng).radius(size).strokeWidth(0).strokeColor(0).fillColor(Color.parseColor("#55555555"));
    }

    private void saveSettings(Marker m){
        //AppPkg = lat
        //SppClass = lon
        //AppName = city name
        //ShortcutAction = distance coverage
        final DataAppsSelection shortcutData = new DataAppsSelection(this);
        if(isLocationSelected){
            if(m!=null){
                final int dis = spnLocationDistanceCover.getSelectedItemPosition();
                final InfoAppsSelection app = new InfoAppsSelection();
                app.setAppType(DataAppsSelection.APP_TYPE_SAVED_LOCATION);
                app.setAppPkg(m.getPosition().latitude+"");
                app.setAppClass(m.getPosition().longitude+"");
                app.setAppName(m.getSnippet());
                app.setShortcutAction(convertDistance(dis));
                if(locationId==SettingsAddLocation.LOCATION_INVALID_ID){//save new location
                    shortcutData.addApp(app);
                }else{//edit current saved location
                    app.setId(locationId);//important!
                    shortcutData.updateLocation(app);
                }
            }else if(!lat.equals("0")){
                if(locationId!=SettingsAddLocation.LOCATION_INVALID_ID){//edit current saved location
                    final int dis = spnLocationDistanceCover.getSelectedItemPosition();
                    final InfoAppsSelection app = new InfoAppsSelection();
                    app.setAppType(DataAppsSelection.APP_TYPE_SAVED_LOCATION);
                    app.setAppPkg(lat);
                    app.setAppClass(lon);
                    app.setAppName(cityName);
                    app.setShortcutAction(convertDistance(dis));
                    app.setId(locationId);//important!
                    shortcutData.updateLocation(app);
                }
            }
        }
        shortcutData.close();
        //editor.putInt("spnLocationDistanceCover",dis);
        //editor.putInt("intLocationDistanceCover", convertDistance(dis));
        //editor.commit();
    }

    @Override
    public void updateLocation(Location loc){
        if(isMapReady()){
            latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));//min 2.0, max 21.0
        }
    }

    @Override
    public void updateLocationFinal(final Location loc){
        if(isMapReady()){
            //no long support since android 21v - setProgressBarIndeterminateVisibility(false);

            latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
            final int dis = spnLocationDistanceCover.getSelectedItemPosition();
            final int size = convertDistance(dis);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,convertZoom(size)));//min 2.0, max 21.0
            setUpMarker(latLng);
            setUpCircle(latLng,size);
            onFinish();
        }
    }

    @Override
    public void updateLocationGpsError(){
        isGpsEnabled = false;
        if(isMapReady()){
            map.animateCamera(CameraUpdateFactory.zoomTo(2));
        }
    }

    @Override
    public void updateLocationError(String msg){
        showToastMsg(msg);
    }

    @Override
    public void onFinish(){
        getLocationName();
    }

    @Override
    public void onCancel(){
    }

    private boolean isMapReady(){
        if(map==null){
            Toast.makeText(this,"Map not ready",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onGetLocationNameFinished(Boolean resultOK,String streetName,String cityName,
            String countryName){
        if(resultOK){
            if(streetName!=null){
                marker1.setSnippet(streetName+", "+cityName);
            }else if(cityName!=null){
                marker1.setSnippet(cityName);
            }
            marker1.showInfoWindow();
        }else{
            showToastMsg(getString(R.string.map_location_error));
        }
        //no long support since android 21v - setProgressBarIndeterminateVisibility(false);
    }

    private void loadSettings(Bundle extras){
        locationId = extras.getInt(SettingsAddLocation.LOCATION_ID,SettingsAddLocation.LOCATION_INVALID_ID);
        lat = extras.getString(SettingsAddLocation.LOCATION_LAT,"0");
        lon = extras.getString(SettingsAddLocation.LOCATION_LON,"0");
        cityName = extras.getString(SettingsAddLocation.LOCATION_CITY_NAME,getString(R.string.map_location_null));
        int distanceCover = extras.getInt(SettingsAddLocation.LOCATION_DISTANCE_COVERAGE,200);

        spnLocationDistanceCover.setSelection(convertDistanceToSpin(distanceCover));//200m
        latLng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
        txtName.setText(cityName);
    }

    private void spinnerFunction(){
        final List<String> list1 = new ArrayList<String>();
        //list1.add("10 meters");
        list1.add(getString(R.string.map_location_distance_50m));
        list1.add(getString(R.string.map_location_distance_100m));
        list1.add(getString(R.string.map_location_distance_200m));
        list1.add(getString(R.string.map_location_distance_500m));
        list1.add(getString(R.string.map_location_distance_1km));
        list1.add(getString(R.string.map_location_distance_2km));
        list1.add(getString(R.string.map_location_distance_5km));
        final ArrayAdapter<String> adt1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list1);
        adt1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLocationDistanceCover.setAdapter(adt1);
        spnLocationDistanceCover.setOnItemSelectedListener(new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
                isLocationSelected = true;
                if(isMapReady()){
                    final int size = convertDistance(position);
                    map.animateCamera(CameraUpdateFactory.zoomTo(convertZoom(size)));
                    setUpCircle(latLng,size);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent){
            }
        });
    }

    private int convertDistanceToSpin(int dis){
        switch(dis){
            case 50:
                return 0;
            case 100:
                return 1;
            default://200
                return 2;
            case 500:
                return 3;
            case 1000:
                return 4;
            case 2000:
                return 5;
            case 5000:
                return 6;
        }
    }
}