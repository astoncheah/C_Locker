package com.ccs.lockscreen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataWeather extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    //Ver 2: Added Provider Column
    private static final String DATABASE_NAME = "weatherManager";
    private static final String TABLE_WEATHER = "weatherTable";
    private static final String KEY_ID = "id", KEY_PROVIDER = "weatherProvider", KEY_CODE = "weatherUpdateCode", KEY_DATE = "weatherDate", KEY_INFO_NAME = "weatherInfoName", KEY_DATA = "weatherData";

    public DataWeather(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        //this.context = context;
    }

    public void addWeather(String provider,int updateTimes,long date,String infoName,String data){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_PROVIDER,provider);
        values.put(KEY_CODE,updateTimes);
        values.put(KEY_DATE,date);
        values.put(KEY_INFO_NAME,infoName);
        values.put(KEY_DATA,data);

        db.insert(TABLE_WEATHER,null,values);
        db.close();
    }    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "+TABLE_WEATHER+
                "("+
                KEY_ID+" INTEGER PRIMARY KEY,"+
                KEY_PROVIDER+" TEXT,"+
                KEY_CODE+" INTEGER,"+
                KEY_DATE+" INTEGER,"+
                KEY_INFO_NAME+" TEXT,"+
                KEY_DATA+" TEXT"+
                ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public DownLoadWeather getWeather(String provider,int updateTimes,String infoName){
        DownLoadWeather weather = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getReadableDatabase();
            String[] columns = new String[]{KEY_ID,KEY_PROVIDER,KEY_CODE,KEY_DATE,KEY_INFO_NAME,KEY_DATA};
            String selection = "("+
                    KEY_PROVIDER+"=? AND "+
                    KEY_CODE+"=? AND "+
                    KEY_INFO_NAME+"=?"+
                    ")";
            String[] arg = new String[]{provider,String.valueOf(updateTimes),infoName};
            cursor = db.query(TABLE_WEATHER,columns,selection,arg,null,null,null,null);
            try{
                if(cursor!=null && cursor.getCount()>0){
                    cursor.moveToFirst();
                    weather = new DownLoadWeather(Integer.parseInt(cursor.getString(0)),cursor.getString(1),Integer.parseInt(cursor.getString(2)),cursor.getLong(3),cursor.getString(4),cursor.getString(5));
                }
            }finally{
                if(db!=null){
                    db.close();
                    db = null;
                }
                if(cursor!=null){
                    cursor.close();
                    cursor = null;
                }
            }
            return weather;
        }catch(Exception e){
            e.printStackTrace();
            if(db!=null){
                db.close();
                db = null;
            }
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
            return null;
        }
    }    @Override
    public void onUpgrade(SQLiteDatabase db,int arg1,int arg2){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_WEATHER);

        // Create tables again
        onCreate(db);
    }

    public List<DownLoadWeather> getWeather(){
        List<DownLoadWeather> weatherList = null;
        // Select All Query
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getWritableDatabase();
            String selectQuery = "SELECT * FROM "+TABLE_WEATHER;
            cursor = db.rawQuery(selectQuery,null);
            try{
                if(cursor!=null && cursor.getCount()>0){
                    cursor.moveToFirst();
                    weatherList = new ArrayList<DownLoadWeather>();
                    do{
                        DownLoadWeather weather = new DownLoadWeather();
                        weather.setId(Integer.parseInt(cursor.getString(0)));
                        weather.setWeatherProvider(cursor.getString(1));
                        weather.setWeatherUpdateCode(Integer.parseInt(cursor.getString(2)));
                        weather.setWeatherDate(cursor.getLong(3));
                        weather.setWeatherInfoName(cursor.getString(4));
                        weather.setWeatherData(cursor.getString(5));
                        weatherList.add(weather);
                    }while(cursor.moveToNext());
                }
            }finally{
                cursor.close();
            }
            return weatherList;
        }catch(Exception e){
            e.printStackTrace();
            if(db!=null){
                db.close();
                db = null;
            }
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
            return null;
        }
    }

    public int getWeatherCount(){
        int count = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getReadableDatabase();
            String countQuery = "SELECT * FROM "+TABLE_WEATHER;
            cursor = db.rawQuery(countQuery,null);
            try{
                if(cursor!=null && cursor.getCount()>0){
                    count = cursor.getCount();
                }
            }finally{
                cursor.close();
            }
            return count;
        }catch(Exception e){
            e.printStackTrace();
            if(db!=null){
                db.close();
                db = null;
            }
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
            return 0;
        }
    }

    public int updateWeather(DownLoadWeather weather){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROVIDER,weather.getWeatherProvider());
        values.put(KEY_CODE,weather.getWeatherUpdateCode());
        values.put(KEY_DATE,weather.getWeatherDate());
        values.put(KEY_INFO_NAME,weather.getWeatherInfoName());
        values.put(KEY_DATA,weather.getWeatherData());
        // updating row
        return db.update(TABLE_WEATHER,values,KEY_ID+" = ?",new String[]{String.valueOf(weather.getId())});
    }

    public void deleteWeather(String provider,int weatherUpdateCode){
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = "("+
                KEY_PROVIDER+"=? AND "+
                KEY_CODE+"=?"+
                ")";
        String[] str = new String[]{provider,String.valueOf(weatherUpdateCode)};
        //set null in "whereClause" to delete all row!
        db.delete(TABLE_WEATHER,selection,str);
        db.close();
    }

    public void deleteAllWeatherData(){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        db.delete(TABLE_WEATHER,null,null);
        db.close();
    }




}