package com.ccs.lockscreen.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.MyCLocker;


public class MyLocationManager implements LocationListener{
    private static final int LOCATION_DETECTING_TIME = 10000;//previously 10sec
    private Handler handler = new Handler();
    private LocationManager locationManager;
    private Location lastLocation, newLocation;
    private Context context;
    private MyCLocker mCLocker = null;
    private LocationCallBack callback;
    private Runnable runUpdateTimer = new Runnable(){
        @Override
        public void run(){
            try{
                removeUpdate();
                if(lastLocation!=null){
                    callback.updateLocationFinal(getFinalLocation());
                }else if(newLocation!=null){
                    callback.updateLocationFinal(newLocation);
                }else{
                    callback.updateLocationError(context.getString(R.string.map_location_error));
                }
            }catch(Exception e){
                mCLocker.saveErrorLog(null,e);
            }
        }
    };

    public MyLocationManager(Context context,LocationCallBack callback){
        this.context = context;
        this.callback = callback;
        this.mCLocker = new MyCLocker(context);
    }

    public final Location runLocationChecker(){
        if(!isInternetConnected()){
            callback.updateLocationError(context.getString(R.string.network_error));
            return null;
        }
        if(!isLocationProviderEnabled(LocationManager.GPS_PROVIDER)){
            callback.updateLocationGpsError();
        }
        this.lastLocation = getLastLocation();
        runUpdateTimer(true);
        return lastLocation;
    }

    private boolean isInternetConnected(){
        try{
            final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(netInfo!=null && netInfo.isConnected()){
                return true;
            }
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }
        return false;
    }

    public final boolean isLocationProviderEnabled(String provider){
        if(locationManager==null){
            locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        }
        try{
            return locationManager.isProviderEnabled(provider);
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }
        return false;
    }

    public final Location getLastLocation(){
        Location locGPS = null;
        Location locNetWork = null;

        try{
            if(locationManager==null){
                locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            }
            if(isLocationProviderEnabled(LocationManager.GPS_PROVIDER)){
                locGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if(isLocationProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locNetWork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(locGPS!=null){
                if(isLoc1BetterLocation(locGPS,locNetWork)){
                    return locGPS;
                }else{
                    return locNetWork;
                }
            }else if(locNetWork!=null){
                return locNetWork;
            }
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }
        return null;
    }

    private void runUpdateTimer(final boolean bln){
        if(bln){
            if(isLocationProviderEnabled(LocationManager.GPS_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,          // 1000 = 1-second interval.
                        0,          // 10 = 10 meters.
                        this);
            }
            if(isLocationProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,          // 1000 = 1-second interval.
                        0,          // 10 = 10 meters.
                        this);
            }
            handler = new Handler();
            handler.postDelayed(runUpdateTimer,LOCATION_DETECTING_TIME);
        }else{
            handler.removeCallbacks(runUpdateTimer);
        }
    }

    private boolean isLoc1BetterLocation(Location location1,Location location2){
        final int TWO_MINUTES = 1000*60*2;
        if(location2==null){
            // A new location is always better than no location
            return true;
        }
        // Check whether the new location fix is newer or older
        long timeDelta = location1.getTime()-location2.getTime();
        boolean isSignificantlyNewer = timeDelta>TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta<-TWO_MINUTES;
        boolean isNewer = timeDelta>0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if(isSignificantlyNewer){
            return true;
            // If the new location is more than two minutes older, it must be worse
        }else if(isSignificantlyOlder){
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int)(location1.getAccuracy()-location2.getAccuracy());
        boolean isLessAccurate = accuracyDelta>0;
        boolean isMoreAccurate = accuracyDelta<0;
        boolean isSignificantlyLessAccurate = accuracyDelta>200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location1.getProvider(),location2.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if(isMoreAccurate){
            return true;
        }else if(isNewer && !isLessAccurate){
            return true;
        }else if(isNewer && !isSignificantlyLessAccurate && isFromSameProvider){
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1,String provider2){
        if(provider1==null){
            return provider2==null;
        }
        return provider1.equals(provider2);
    }

    public final int compareDistance(Location profileLoc,Location currentLoc){
        return Math.round(profileLoc.distanceTo(currentLoc)); // Returns in Metres
        //return profileLoc.distanceTo(currentLoc); // Returns in Metres
    }

    private Location getFinalLocation(){
        if(isLoc1BetterLocation(lastLocation,newLocation)){
            return lastLocation;
        }else{
            return newLocation;
        }
    }

    @Override
    public void onLocationChanged(Location location){
        Log.e("ProfileLocation","onLocationChanged: "+
                location.getAccuracy()+"/"+location.getLatitude()+"/"+location.getLongitude());
        this.newLocation = location;
        callback.updateLocation(location);
        if(location.getAccuracy()<40){//40 meter
            callback.updateLocationFinal(location);
            cancelUpdate();
        }
    }

    public final void cancelUpdate(){
        try{
            runUpdateTimer(false);
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }
        removeUpdate();
        mCLocker.close();
    }

    private void removeUpdate(){
        try{
            if(locationManager!=null){
                locationManager.removeUpdates(this);
            }
        }catch(Exception e){
            mCLocker.saveErrorLog(null,e);
        }
    }

    @Override
    public void onStatusChanged(String provider,int status,Bundle extras){
    }

    @Override
    public void onProviderEnabled(String provider){
    }

    @Override
    public void onProviderDisabled(String provider){
    }

    public interface LocationCallBack{
        void updateLocation(Location loc);

        void updateLocationFinal(Location loc);

        void updateLocationGpsError();

        void updateLocationError(String msg);
    }
}
