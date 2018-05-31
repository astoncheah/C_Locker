package com.ccs.lockscreen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataRSS extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "rssManager";
    private static final String TABLE_RSS = "rssTable";

    private static final String KEY_ID = "id";
    private static final String KEY_SOURCE = "rssSource";
    private static final String KEY_TITLE = "rssTitle";
    private static final String KEY_PUB_DATE = "rssPubDate";
    private static final String KEY_DESC = "rssDesc";
    private static final String KEY_CONTENT = "rssContent";
    private static final String KEY_CATEGORY = "rssCategory";
    private static final String KEY_LINK = "rssLink";

    public DataRSS(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public void addRSS(InfoRSS rss){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SOURCE,rss.getRssSource());
        values.put(KEY_TITLE,rss.getRssTitle());
        values.put(KEY_PUB_DATE,rss.getRssPubDate());
        values.put(KEY_DESC,rss.getRssDesc());
        values.put(KEY_CONTENT,rss.getRssContent());
        values.put(KEY_CATEGORY,rss.getRssCategory());
        values.put(KEY_LINK,rss.getRssLink());

        db.insert(TABLE_RSS,null,values);
        db.close();
    }    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "+TABLE_RSS+
                "("+
                KEY_ID+" INTEGER PRIMARY KEY,"+
                KEY_SOURCE+" TEXT,"+
                KEY_TITLE+" TEXT,"+
                KEY_PUB_DATE+" TEXT,"+
                KEY_DESC+" TEXT,"+
                KEY_CONTENT+" TEXT,"+
                KEY_CATEGORY+" TEXT,"+
                KEY_LINK+" TEXT"+
                ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public InfoRSS getRSS(int id){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        InfoRSS rss = null;
        try{
            db = this.getReadableDatabase();
            String[] columns = new String[]{KEY_ID,KEY_SOURCE,KEY_TITLE,KEY_PUB_DATE,KEY_DESC,KEY_CONTENT,KEY_CATEGORY,KEY_LINK};
            String[] selection = new String[]{String.valueOf(id)};

            cursor = db.query(TABLE_RSS,columns,KEY_ID+"=?",selection,null,null,null,null);
            try{
                if(cursor!=null){
                    cursor.moveToFirst();
                }else{
                    return null;
                }
                rss = new InfoRSS(Integer.parseInt(cursor.getString(0)),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7));
            }finally{
                if(db!=null){
                    db.close();
                    db = null;
                }
                cursor.close();
            }
            return rss;
        }catch(Exception e){
            if(db!=null){
                db.close();
                db = null;
            }
            if(cursor!=null){
                cursor.close();
                cursor = null;
            }
            if(rss!=null){
                rss = null;
            }
            return null;
        }
    }    @Override
    public void onUpgrade(SQLiteDatabase db,int arg1,int arg2){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_RSS);

        // Create tables again
        onCreate(db);
    }

    public List<InfoRSS> getAllRSS(){
        List<InfoRSS> rssList = new ArrayList<InfoRSS>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+TABLE_RSS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        try{
            if(cursor.moveToFirst()){
                do{
                    InfoRSS rss = new InfoRSS();
                    rss.setId(Integer.parseInt(cursor.getString(0)));
                    rss.setRssSource(cursor.getString(1));
                    rss.setRssTitle(cursor.getString(2));
                    rss.setRssPubDate(cursor.getString(3));
                    rss.setRssDesc(cursor.getString(4));
                    rss.setRssContent(cursor.getString(5));
                    rss.setRssCategory(cursor.getString(6));
                    rss.setRssLink(cursor.getString(7));
                    rssList.add(rss);
                }while(cursor.moveToNext());
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
        return rssList;
    }

    public int getRSSCount(){
        int count = 0;
        String countQuery = "SELECT * FROM "+TABLE_RSS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery,null);

        if(cursor!=null && !cursor.isClosed()){
            try{
                count = cursor.getCount();
            }finally{
                cursor.close();
            }
        }
        return count;
    }

    public int updateRSS(InfoRSS rss){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SOURCE,rss.getRssSource());
        values.put(KEY_TITLE,rss.getRssTitle());
        values.put(KEY_PUB_DATE,rss.getRssPubDate());
        values.put(KEY_DESC,rss.getRssDesc());
        values.put(KEY_CONTENT,rss.getRssContent());
        values.put(KEY_CATEGORY,rss.getRssCategory());
        values.put(KEY_LINK,rss.getRssLink());

        // updating row
        return db.update(TABLE_RSS,values,KEY_ID+" = ?",new String[]{String.valueOf(rss.getId())});
    }

    public void deleteRSS(InfoRSS rss){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] str = new String[]{String.valueOf(rss.getId())};
        //set null in "whereClause" to delete all row!
        db.delete(TABLE_RSS,KEY_ID+" = ?",str);
        db.close();
    }

    public void deleteAllRSS(){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        db.delete(TABLE_RSS,null,null);
        db.close();
    }




}
