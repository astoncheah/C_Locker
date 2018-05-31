package com.ccs.lockscreen.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class LocationNameHandler extends AsyncTask<LatLng,Void,Boolean>{
    private Context context;
    private MyCLocker mCLocker = null;
    private LocationNameCallBack callback = null;
    private String streetName, cityName, countryName;

    public LocationNameHandler(Context context,LocationNameCallBack callback){
        this.context = context;
        this.callback = callback;
        mCLocker = new MyCLocker(context);
    }

    @Override
    protected Boolean doInBackground(LatLng... arg){
        try{
            getCityName(arg[0]);
            return true;
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }
        return false;
    }

    @Override
    protected void onPreExecute(){
        if(!C.isInternetConnected(context)){
            callback.onGetLocationNameFinished(false,streetName,cityName,countryName);
            this.cancel(true);
        }
    }

    @Override
    protected void onPostExecute(Boolean resultOK){
        callback.onGetLocationNameFinished(resultOK,streetName,cityName,countryName);
    }

    private void getCityName(LatLng position) throws Exception{
        String strStreename = null;
        String strCityName = null;
        String strCountryName = null;
        if(Geocoder.isPresent()){
            try{
                Geocoder gcd = new Geocoder(context,Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(position.latitude,position.longitude,1);

                if(addresses.size()>0){
                    strStreename = addresses.get(0).getSubLocality();
                    strCityName = addresses.get(0).getLocality();
                    strCountryName = addresses.get(0).getCountryName();
                }
            }catch(IOException e){
                mCLocker.saveErrorLog(null,e);
            }catch(Exception e){
                mCLocker.saveErrorLog(null,e);
                // after a while, Geocoder start to throw "Service not availalbe" exception. really weird since it was working before (same device, same Android version etc..
            }
        }
        if(strStreename!=null){ // i.e., Geocoder succeed
            streetName = strStreename;
        }
        if(strCityName!=null){
            cityName = strCityName;
            countryName = strCountryName;
        }else{ // i.e., Geocoder failed{
            fetchCityNameUsingGoogleMap(position);
        }
        //Log.e("getCityName",streetName+"/"+cityName+"/"+countryName);
    }

    private void fetchCityNameUsingGoogleMap(LatLng position) throws Exception{
        //penang location
        //http://maps.googleapis.com/maps/api/geocode/json?latlng= 5.3864805, 100.3881482 &sensor=false
        String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng="+
                position.latitude+","+
                position.longitude+"&sensor=false";
        StringBuilder urlResult = null;
        HttpURLConnection conn = null;
        try{
            URL url = new URL(googleMapUrl);
            conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            urlResult = new StringBuilder();
            String line;
            while((line = reader.readLine())!=null){
                urlResult.append(line);
            }
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }finally{
            conn.disconnect();
        }
        JSONObject googleMapResponse = new JSONObject(urlResult.toString());

        // many nested loops.. not great -> use expression instead
        // loop among all results
        JSONArray results = (JSONArray)googleMapResponse.get("results");
        for(int i = 0; i<results.length(); i++){
            // loop among all addresses within this result
            JSONObject result = results.getJSONObject(i);
            if(result.has("address_components")){
                JSONArray addressComponents = result.getJSONArray("address_components");
                // loop among all address component to find a 'locality' or 'sublocality'
                for(int j = 0; j<addressComponents.length(); j++){
                    JSONObject addressComponent = addressComponents.getJSONObject(j);
                    if(result.has("types")){
                        JSONArray types = addressComponent.getJSONArray("types");
                        // search for locality and sublocality
                        String strStreename = null;
                        String strCityName = null;
                        String strCountryName = null;
                        for(int k = 0; k<types.length(); k++){
                            if("sublocality".equals(types.getString(k)) && strCityName==null){
                                if(addressComponent.has("long_name")){
                                    strStreename = addressComponent.getString("long_name");
                                }else if(addressComponent.has("short_name")){
                                    strStreename = addressComponent.getString("short_name");
                                }
                            }
                            if("locality".equals(types.getString(k)) && strCityName==null){
                                if(addressComponent.has("long_name")){
                                    strCityName = addressComponent.getString("long_name");
                                }else if(addressComponent.has("short_name")){
                                    strCityName = addressComponent.getString("short_name");
                                }
                            }
                            if("country".equals(types.getString(k)) && strCountryName==null){
                                if(addressComponent.has("long_name")){
                                    strCountryName = addressComponent.getString("long_name");
                                }else if(addressComponent.has("short_name")){
                                    strCountryName = addressComponent.getString("short_name");
                                }
                            }
                        }
                        if(strStreename!=null){
                            streetName = strCityName;
                        }
                        if(strCityName!=null){
                            cityName = strCityName;
                        }
                        if(strCountryName!=null){
                            countryName = strCountryName;
                        }
                    }
                }
            }
        }
        if(cityName!=null){
            if(countryName!=null){
                mCLocker.saveErrorLog("WeatherUpdate>MainUpdate>fetchCityNameUsingGoogleMap: "+
                        cityName+"/"+countryName,null);
            }else{
                mCLocker.saveErrorLog("WeatherUpdate>MainUpdate>fetchCityNameUsingGoogleMap: "+
                        cityName+"/null",null);
            }
        }else{
            mCLocker.saveErrorLog("WeatherUpdate>MainUpdate>fetchCityNameUsingGoogleMap: city not found!",null);
        }
        //Log.e("fetchCityNameUsingGoogleMap",streetName+"/"+cityName+"/"+countryName);
    }

    public interface LocationNameCallBack{
        void onGetLocationNameFinished(Boolean resultOK,String streetName,String cityName,
                String countryName);
    }
}
