package com.ccs.lockscreen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DataWidgets extends SQLiteOpenHelper{
    public static final int WIDGET_TYPE_DEFAULT = 1, WIDGET_TYPE_CUSTOM = 2,//WIDGET_TYPE_MUSIC = 3,
            DEFAULT_ANALOG_CLOCK_CENTER = 1, DEFAULT_ANALOG_CLOCK_LEFT = 2, DEFAULT_DIGITAL_CLOCK_CENTER = 3, DEFAULT_DIGITAL_CLOCK_LEFT = 4, REQUEST_PICK_APPWIDGET = 1001, REQUEST_CREATE_APPWIDGET = 1002, REQUEST_PICK_APPWIDGET_JB = 1003, REQUEST_CREATE_APPWIDGET_JB = 1004, REQUEST_BIND_APPWIDGET_JB = 1005;
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "widgetManager";
    private static final String TABLE_WIDGET = "widgetTable";
    private static final String KEY_ID = "id";
    private static final String KEY_PROFILE = "widgetProfile";
    private static final String KEY_WIDGET_ID = "widgetId";
    private static final String KEY_TYPE = "widgetType";
    private static final String KEY_PKG_NAME = "widgetPkgName";
    private static final String KEY_CLASS_NAME = "widgetClassName";
    private static final String KEY_INDEX = "widgetinDex";
    private static final String KEY_X = "widgetX";
    private static final String KEY_Y = "widgetY";
    private static final String KEY_X_SPAN = "widgetSpanX";
    private static final String KEY_Y_SPAN = "widgetSpanY";
    private static final String KEY_MIN_WIDTH = "widgetMinWidth";
    private static final String KEY_MIN_HEIGHT = "widgetMinHeight";

    public DataWidgets(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    public void addWidget(InfoWidget widgets){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROFILE,widgets.getWidgetProfile());
        values.put(KEY_WIDGET_ID,widgets.getWidgetId());
        values.put(KEY_TYPE,widgets.getWidgetType());
        values.put(KEY_PKG_NAME,widgets.getWidgetPkgsName());
        values.put(KEY_CLASS_NAME,widgets.getWidgetClassName());
        values.put(KEY_INDEX,widgets.getWidgetIndex());
        values.put(KEY_X,widgets.getWidgetX());
        values.put(KEY_Y,widgets.getWidgetY());
        values.put(KEY_X_SPAN,widgets.getWidgetSpanX());
        values.put(KEY_Y_SPAN,widgets.getWidgetSpanY());
        values.put(KEY_MIN_WIDTH,widgets.getWidgetMinWidth());
        values.put(KEY_MIN_HEIGHT,widgets.getWidgetMinHeight());

        db.insert(TABLE_WIDGET,null,values);
        db.close();
    }    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "+TABLE_WIDGET+
                "("+
                KEY_ID+" INTEGER PRIMARY KEY,"+
                KEY_PROFILE+" INTEGER,"+
                KEY_WIDGET_ID+" INTEGER,"+
                KEY_TYPE+" INTEGER,"+
                KEY_PKG_NAME+" TEXT,"+
                KEY_CLASS_NAME+" TEXT,"+
                KEY_INDEX+" INTEGER,"+
                KEY_X+" INTEGER,"+
                KEY_Y+" INTEGER,"+
                KEY_X_SPAN+" INTEGER,"+
                KEY_Y_SPAN+" INTEGER,"+
                KEY_MIN_WIDTH+" INTEGER,"+
                KEY_MIN_HEIGHT+" INTEGER"+
                ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public InfoWidget getWidget(int widgetId){
        SQLiteDatabase db = null;
        Cursor cursor = null;
        InfoWidget widgets = null;
        try{
            db = this.getReadableDatabase();
            String[] columns = new String[]{KEY_ID,KEY_PROFILE,KEY_WIDGET_ID,KEY_TYPE,KEY_PKG_NAME,KEY_CLASS_NAME,KEY_INDEX,KEY_X,KEY_Y,KEY_X_SPAN,KEY_Y_SPAN,KEY_MIN_WIDTH,KEY_MIN_HEIGHT};
            String[] selection = new String[]{String.valueOf(widgetId)};

            cursor = db.query(TABLE_WIDGET,columns,KEY_WIDGET_ID+"=?",selection,null,null,null,null);
            try{
                if(cursor!=null){
                    cursor.moveToFirst();
                }else{
                    return null;
                }
                widgets = new InfoWidget(Integer.parseInt(cursor.getString(0)),Integer.parseInt(cursor.getString(1)),Integer.parseInt(cursor.getString(2)),Integer.parseInt(cursor.getString(3)),cursor.getString(4),cursor.getString(5),Integer.parseInt(cursor.getString(6)),Integer.parseInt(cursor.getString(7)),Integer.parseInt(cursor.getString(8)),Integer.parseInt(cursor.getString(9)),Integer.parseInt(cursor.getString(10)),Integer.parseInt(cursor.getString(11)),Integer.parseInt(cursor.getString(12)));
            }finally{
                if(db!=null){
                    db.close();
                    db = null;
                }
                cursor.close();
            }
            return widgets;
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
            if(widgets!=null){
                widgets = null;
            }
            return null;
        }
    }    @Override
    public void onUpgrade(SQLiteDatabase db,int arg1,int arg2){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_WIDGET);

        // Create tables again
        onCreate(db);
    }

    public List<InfoWidget> getAllWidgets(int profile){
        List<InfoWidget> widgetsList = new ArrayList<InfoWidget>();
        final String where = KEY_PROFILE+" = ?";
        final String[] args = new String[]{String.valueOf(profile)};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_WIDGET,null,where,args,null,null,null);
        try{
            if(cursor.moveToFirst()){
                do{
                    InfoWidget widgets = new InfoWidget();
                    widgets.setId(Integer.parseInt(cursor.getString(0)));
                    widgets.setWidgetProfile(Integer.parseInt(cursor.getString(1)));
                    widgets.setWidgetId(Integer.parseInt(cursor.getString(2)));
                    widgets.setWidgetType(Integer.parseInt(cursor.getString(3)));
                    widgets.setWidgetPkgsName(cursor.getString(4));
                    widgets.setWidgetClassName(cursor.getString(5));
                    widgets.setWidgetIndex(Integer.parseInt(cursor.getString(6)));
                    widgets.setWidgetX(Integer.parseInt(cursor.getString(7)));
                    widgets.setWidgetY(Integer.parseInt(cursor.getString(8)));
                    widgets.setWidgetSpanX(Integer.parseInt(cursor.getString(9)));
                    widgets.setWidgetSpanY(Integer.parseInt(cursor.getString(10)));
                    widgets.setWidgetMinWidth(Integer.parseInt(cursor.getString(11)));
                    widgets.setWidgetMinHeight(Integer.parseInt(cursor.getString(12)));
                    widgetsList.add(widgets);
                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            deleteAllWidgets();
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
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
        return widgetsList;
    }

    public void deleteAllWidgets(){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        db.delete(TABLE_WIDGET,null,null);
        db.close();
    }

    public List<InfoWidget> getAllWidgets(){
        List<InfoWidget> widgetsList = new ArrayList<InfoWidget>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+TABLE_WIDGET;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        try{
            if(cursor.moveToFirst()){
                do{
                    InfoWidget widgets = new InfoWidget();
                    widgets.setId(Integer.parseInt(cursor.getString(0)));
                    widgets.setWidgetProfile(Integer.parseInt(cursor.getString(1)));
                    widgets.setWidgetId(Integer.parseInt(cursor.getString(2)));
                    widgets.setWidgetType(Integer.parseInt(cursor.getString(3)));
                    widgets.setWidgetPkgsName(cursor.getString(4));
                    widgets.setWidgetClassName(cursor.getString(5));
                    widgets.setWidgetIndex(Integer.parseInt(cursor.getString(6)));
                    widgets.setWidgetX(Integer.parseInt(cursor.getString(7)));
                    widgets.setWidgetY(Integer.parseInt(cursor.getString(8)));
                    widgets.setWidgetSpanX(Integer.parseInt(cursor.getString(9)));
                    widgets.setWidgetSpanY(Integer.parseInt(cursor.getString(10)));
                    widgets.setWidgetMinWidth(Integer.parseInt(cursor.getString(11)));
                    widgets.setWidgetMinHeight(Integer.parseInt(cursor.getString(12)));
                    widgetsList.add(widgets);
                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            deleteAllWidgets();
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
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
        return widgetsList;
    }

    public int getWidgetsCount(){
        int count = 0;
        String countQuery = "SELECT * FROM "+TABLE_WIDGET;
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

    public int updateWidgets(InfoWidget widgets){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PROFILE,widgets.getWidgetProfile());
        values.put(KEY_WIDGET_ID,widgets.getWidgetId());
        values.put(KEY_TYPE,widgets.getWidgetType());
        values.put(KEY_PKG_NAME,widgets.getWidgetPkgsName());
        values.put(KEY_CLASS_NAME,widgets.getWidgetClassName());
        values.put(KEY_INDEX,widgets.getWidgetIndex());
        values.put(KEY_X,widgets.getWidgetX());
        values.put(KEY_Y,widgets.getWidgetY());
        values.put(KEY_X_SPAN,widgets.getWidgetSpanX());
        values.put(KEY_Y_SPAN,widgets.getWidgetSpanY());
        values.put(KEY_MIN_WIDTH,widgets.getWidgetMinWidth());
        values.put(KEY_MIN_HEIGHT,widgets.getWidgetMinHeight());

        String where = KEY_PROFILE+" = ? AND "+KEY_WIDGET_ID+" = ?";
        String[] args = new String[]{String.valueOf(widgets.getWidgetProfile()),String.valueOf(widgets.getWidgetId())};
        // updating row
        return db.update(TABLE_WIDGET,values,where,args);
    }

    public int updateWidgetIndex(int widgetProfile,int widgetId,int index){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INDEX,index);

        String where = KEY_PROFILE+" = ? AND "+KEY_WIDGET_ID+" = ?";
        String[] args = new String[]{String.valueOf(widgetProfile),String.valueOf(widgetId)};
        // updating row
        return db.update(TABLE_WIDGET,values,where,args);
    }

    public int updateWidgetSize(int widgetProfile,int widgetId,int x,int y,int spanX,int spanY){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_X,x);
        values.put(KEY_Y,y);
        values.put(KEY_X_SPAN,spanX);
        values.put(KEY_Y_SPAN,spanY);

        String where = KEY_PROFILE+" = ? AND "+KEY_WIDGET_ID+" = ?";
        String[] args = new String[]{String.valueOf(widgetProfile),String.valueOf(widgetId)};
        // updating row
        return db.update(TABLE_WIDGET,values,where,args);
    }

    public void deleteWidgets(int widgetProfile,int widgetId){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        String where = KEY_PROFILE+" = ? AND "+KEY_WIDGET_ID+" = ?";
        String[] args = new String[]{String.valueOf(widgetProfile),String.valueOf(widgetId)};

        db.delete(TABLE_WIDGET,where,args);
        db.close();
    }

    public void deleteAllWidgets(int widgetProfile){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        String where = KEY_PROFILE+" = ? OR "+KEY_PROFILE+" <= ?";
        String[] args = new String[]{String.valueOf(widgetProfile),"0"};

        db.delete(TABLE_WIDGET,where,args);
        db.close();
    }




}
