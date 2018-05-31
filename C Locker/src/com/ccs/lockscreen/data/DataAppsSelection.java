package com.ccs.lockscreen.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ccs.lockscreen.myclocker.C;

import java.util.ArrayList;
import java.util.List;

public class DataAppsSelection extends SQLiteOpenHelper{
    public static final String KEY_SHORTCUT_PROFILE = "appShortcutProfile";
    public static final String KEY_SHORTCUT_ACTION = "appShortcutAction";
    public static final String KEY_SHORTCUT_PIN = "appShortcutPIN";
    public static final String KEY_OTHER_INFO = "appOtherInfo";// contact..
    //KEY_APP_TYPE
    public static final int
        APP_TYPE_SHORTCUT = 1,
        APP_TYPE_CALENDAR_ACCOUNT = 2,
        APP_TYPE_BLOCKED_NOTICE_APPS = 3,
        APP_TYPE_BLOCKED_APPS = 4,
        APP_TYPE_SAVED_LOCATION = 5,
        APP_TYPE_WIDGET_BLOCK = 6;
    //TITLE FOR LAYOUT
    public static final int SHORTCUT_TYPE_VOLUME_KEY = 1, SHORTCUT_TYPE_LOCK_ICON = 2, SHORTCUT_TYPE_GESTURE_SINGLE = 3, SHORTCUT_TYPE_GESTURE_DOUBLE = 4, SHORTCUT_TYPE_TAPPING = 5;
    //KEY_SHORTCUT_TYPE
    public static final int SHORTCUT_APP_01 = 1, SHORTCUT_APP_02 = 2, SHORTCUT_APP_03 = 3, SHORTCUT_APP_04 = 4, SHORTCUT_APP_05 = 5, SHORTCUT_APP_06 = 6, SHORTCUT_APP_07 = 7, SHORTCUT_APP_08 = 8, VOLUME_UP = 9, VOLUME_DOWN = 10, GESTURE_UP = 21, GESTURE_DOWN = 22, GESTURE_LEFT = 23, GESTURE_RIGHT = 24, GESTURE_UP_2 = 31, GESTURE_DOWN_2 = 32, GESTURE_LEFT_2 = 33, GESTURE_RIGHT_2 = 34, DOUBLE_TAP1 = 35, DOUBLE_TAP2 = 36, TRIPLE_TAP1 = 37, TRIPLE_TAP2 = 38, //non shortcut id
        TRIGGER_WEATHER = 1001, TRIGGER_SETTINGS = 1002, TRIGGER_CALENDAR = 1003, TRIGGER_GMAIL = 1004, TRIGGER_MISS_CALL = 1005, TRIGGER_MISS_SMS = 1006, TRIGGER_RSS = 1007, TRIGGER_STATUSBAR = 1008, TRIGGER_WIDGET_CLICK = 1009, TRIGGER_CALL = 1010, TRIGGER_CAM = 1011;
    //KEY_SHORTCUT_ACTION
    public static final int ACTION_TYPE_NONE_SHORTCUTS = -2, ACTION_TYPE_DO_NOTHING = -1, ACTION_DEFAULT = 0, ACTION_SHORTCUT_LAUNCH_APP = 1, ACTION_SHORTCUT_HIDE = 2, ACTION_UNLOCK = 3, ACTION_DIRECT_SMS = 4, ACTION_DIRECT_CALL = 5, ACTION_EXPAND_STATUS_BAR = 6, //non shortcut actions
        ACTION_SMS_DETAIL = 7, ACTION_CALLLOG_DETAIL = 8, ACTION_EVENT_DETAIL = 9, ACTION_RSS_DETAIL = 10, ACTION_NOTICES = 11, ACTION_SETTINGS = 12, ACTION_WIDGET_CLICK = 13, ACTION_OFF_SCREEN = 14, ACTION_CALL = 15, ACTION_CAM = 16, ACTION_SOUND_MODE = 17, ACTION_FAVORITE_SHORTCUT = 18, //KEY_SHORTCUT_PIN
        PIN_REQUIRED = 1, PIN_NOT_REQUIRED = -1;
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "appsSelection";
    private static final String TABLE = "appsSelectionTable";
    private static final String KEY_ID = "id";
    private static final String KEY_APP_TYPE = "appType";
    private static final String KEY_SHORTCUT_ID = "appShortcutId";
    private static final String KEY_PKG_NAME = "pkgName";
    private static final String KEY_CLASS_NAME = "ClassName";
    private static final String KEY_APP_NAME = "appName";
    private int profile = C.PROFILE_DEFAULT;

    public DataAppsSelection(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        //mLocker = new MyCLocker(context);
    }

    public void setProfile(int profile){
        this.profile = profile;
    }    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_CONTACTS_TABLE = "CREATE TABLE "+TABLE+
            "("+
            KEY_ID+" INTEGER PRIMARY KEY,"+
            KEY_APP_TYPE+" INTEGER,"+
            KEY_SHORTCUT_PROFILE+" INTEGER,"+
            KEY_SHORTCUT_ID+" INTEGER,"+
            KEY_SHORTCUT_ACTION+" INTEGER,"+
            KEY_SHORTCUT_PIN+" INTEGER,"+
            KEY_PKG_NAME+" TEXT,"+
            KEY_CLASS_NAME+" TEXT,"+
            KEY_APP_NAME+" TEXT,"+
            KEY_OTHER_INFO+" TEXT"+
            ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    public int getShortcutInfo(int appShortcutId,String lookup,int profile){
        final String[] column = new String[]{lookup};
        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE,column,where,args,null,null,null);

        int action = ACTION_TYPE_DO_NOTHING;
        try{
            if(cursor.moveToFirst()){//If the cursor is empty, moveToFirst will be false
                action = Integer.parseInt(cursor.getString(0));
            }
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }finally{
            db.close();
            if(cursor!=null){
                cursor.close();
            }
        }
        return action;
    }    @Override
    public void onUpgrade(SQLiteDatabase db,int arg1,int arg2){
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);

        // Create tables again
        onCreate(db);
    }

    public String getShortcutName(int appShortcutId,int profile){
        final String[] column = new String[]{KEY_APP_NAME};
        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE,column,where,args,null,null,null);

        String action = "";
        try{
            if(cursor.moveToFirst()){//If the cursor is empty, moveToFirst will be false
                action = cursor.getString(0);
            }
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }finally{
            db.close();
            if(cursor!=null){
                cursor.close();
            }
        }
        return action;
    }

    public List<InfoAppsSelection> getApps(int appType){
        final List<InfoAppsSelection> appsList = new ArrayList<>();
        //String selectQuery = "SELECT * FROM " + TABLE ;
        final String where = KEY_APP_TYPE+" = ?";
        final String[] args = new String[]{String.valueOf(appType)};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE,null,where,args,null,null,null);
        try{
            if(cursor.moveToFirst()){//If the cursor is empty, moveToFirst will be false
                do{
                    InfoAppsSelection app = new InfoAppsSelection();
                    app.setId(Integer.parseInt(cursor.getString(0)));
                    app.setAppType(Integer.parseInt(cursor.getString(1)));
                    app.setShortcutProfile(Integer.parseInt(cursor.getString(2)));
                    app.setShortcutId(Integer.parseInt(cursor.getString(3)));
                    app.setShortcutAction(Integer.parseInt(cursor.getString(4)));
                    app.setShortcutPIN(Integer.parseInt(cursor.getString(5)));
                    app.setAppPkg(cursor.getString(6));
                    app.setAppClass(cursor.getString(7));
                    app.setAppName(cursor.getString(8));
                    app.setAppSubInfo(cursor.getString(9));
                    appsList.add(app);
                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }finally{
            db.close();
            if(cursor!=null){
                cursor.close();
            }
        }
        //Log.e("getShortcutApps2"," /size: "+appsList.size());
        return appsList;
    }

    public List<InfoAppsSelection> getAllApps(){
        List<InfoAppsSelection> appsList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        try{
            if(cursor.moveToFirst()){//If the cursor is empty, moveToFirst will be false
                do{
                    InfoAppsSelection app = new InfoAppsSelection();
                    app.setId(Integer.parseInt(cursor.getString(0)));
                    app.setAppType(Integer.parseInt(cursor.getString(1)));
                    app.setShortcutProfile(Integer.parseInt(cursor.getString(2)));
                    app.setShortcutId(Integer.parseInt(cursor.getString(3)));
                    app.setShortcutAction(Integer.parseInt(cursor.getString(4)));
                    app.setShortcutPIN(Integer.parseInt(cursor.getString(5)));
                    app.setAppPkg(cursor.getString(6));
                    app.setAppClass(cursor.getString(7));
                    app.setAppName(cursor.getString(8));
                    app.setAppSubInfo(cursor.getString(9));
                    appsList.add(app);
                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }finally{
            db.close();
            if(cursor!=null){
                cursor.close();
            }
        }
        return appsList;
    }

    public int getAppCount(){
        int count = 0;
        String countQuery = "SELECT * FROM "+TABLE;
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

    public int updateLocation(InfoAppsSelection app){
        //AppPkg = lat
        //SppClass = lon
        //AppName = city name
        //ShortcutAction = distance coverage
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_APP_TYPE,app.getAppType());
        values.put(KEY_PKG_NAME,app.getAppPkg());
        values.put(KEY_CLASS_NAME,app.getAppClass());
        values.put(KEY_APP_NAME,app.getAppName());
        values.put(KEY_SHORTCUT_ACTION,app.getShortcutAction());

        String where = KEY_ID+" = ?";
        String[] args = new String[]{String.valueOf(app.getId())};
        // updating row
        return db.update(TABLE,values,where,args);
    }

    public int updateShortcutInfo(int appShortcutId,String lookup,int info){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(lookup,info);

        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};
        // updating row
        return db.update(TABLE,values,where,args);
    }

    public int updateShortcutInfo(int appShortcutId,String lookup,String info){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(lookup,info);

        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};
        // updating row
        return db.update(TABLE,values,where,args);
    }

    public void updateFavoriteShortcut(int appsId,String uri,String appName){
        final InfoAppsSelection app = new InfoAppsSelection();
        app.setAppType(DataAppsSelection.APP_TYPE_SHORTCUT);
        app.setShortcutProfile(profile);
        app.setShortcutId(appsId);
        app.setShortcutAction(DataAppsSelection.ACTION_FAVORITE_SHORTCUT);
        app.setAppPkg(uri);
        app.setAppName(appName);

        final List<InfoAppsSelection> appsList = getShortcutApps(appsId,profile);
        if(appsList.isEmpty()){
            addApp(app);
        }else{
            updateApp(app);
        }
    }

    public List<InfoAppsSelection> getShortcutApps(int appShortcutId,int profile){
        List<InfoAppsSelection> appsList = new ArrayList<>();
        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};

        SQLiteDatabase db = null;
        Cursor cursor = null;
        try{
            db = this.getWritableDatabase();
            cursor = db.query(TABLE,null,where,args,null,null,null);
            if(cursor.moveToFirst()){//If the cursor is empty, moveToFirst will be false
                do{
                    InfoAppsSelection app = new InfoAppsSelection();
                    app.setId(Integer.parseInt(cursor.getString(0)));
                    app.setAppType(Integer.parseInt(cursor.getString(1)));
                    app.setShortcutProfile(Integer.parseInt(cursor.getString(2)));
                    app.setShortcutId(Integer.parseInt(cursor.getString(3)));
                    app.setShortcutAction(Integer.parseInt(cursor.getString(4)));
                    app.setShortcutPIN(Integer.parseInt(cursor.getString(5)));
                    app.setAppPkg(cursor.getString(6));
                    app.setAppClass(cursor.getString(7));
                    app.setAppName(cursor.getString(8));
                    app.setAppSubInfo(cursor.getString(9));
                    appsList.add(app);
                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }finally{
            if(db!=null){
                db.close();
            }
            if(cursor!=null){
                cursor.close();
            }
        }
        //Log.e("getShortcutApps","/id: "+appShortcutId+" /size: "+appsList.size()+" /profile: "+profile);
        return appsList;
    }

    public void addApp(InfoAppsSelection app){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_APP_TYPE,app.getAppType());
        values.put(KEY_SHORTCUT_PROFILE,app.getShortcutProfile());
        values.put(KEY_SHORTCUT_ID,app.getShortcutId());
        values.put(KEY_SHORTCUT_ACTION,app.getShortcutAction());
        values.put(KEY_SHORTCUT_PIN,app.getShortcutPIN());
        values.put(KEY_PKG_NAME,app.getAppPkg());
        values.put(KEY_CLASS_NAME,app.getAppClass());
        values.put(KEY_APP_NAME,app.getAppName());
        values.put(KEY_OTHER_INFO,app.getAppSubInfo());
        db.insert(TABLE,null,values);
        db.close();
    }

    public int updateApp(InfoAppsSelection app){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_APP_TYPE,app.getAppType());
        values.put(KEY_SHORTCUT_PROFILE,app.getShortcutProfile());
        values.put(KEY_SHORTCUT_ID,app.getShortcutId());
        values.put(KEY_SHORTCUT_ACTION,app.getShortcutAction());
        //values.put(KEY_SHORTCUT_PIN,app.getShortcutPIN()); //update individually
        values.put(KEY_PKG_NAME,app.getAppPkg());
        values.put(KEY_CLASS_NAME,app.getAppClass());
        values.put(KEY_APP_NAME,app.getAppName());
        values.put(KEY_OTHER_INFO,app.getAppSubInfo());

        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(app.getShortcutProfile()),String.valueOf(app.getShortcutId())};
        // updating row
        return db.update(TABLE,values,where,args);
    }

    public int resetShortcut(int appShortcutId,int profile){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_APP_TYPE,APP_TYPE_SHORTCUT);
        values.put(KEY_SHORTCUT_PROFILE,profile);
        values.put(KEY_SHORTCUT_ID,appShortcutId);
        values.put(KEY_SHORTCUT_ACTION,ACTION_DEFAULT);
        values.put(KEY_SHORTCUT_PIN,PIN_NOT_REQUIRED);
        values.put(KEY_PKG_NAME,"");
        values.put(KEY_CLASS_NAME,"");
        values.put(KEY_APP_NAME,"");
        values.put(KEY_OTHER_INFO,"");

        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};
        return db.update(TABLE,values,where,args);
    }

    public void deleteLocation(int locationId){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        String where = KEY_ID+" = ?";
        String[] args = new String[]{String.valueOf(locationId)};

        db.delete(TABLE,where,args);
        db.close();
    }

    public void deleteAllApps(int appType,int profile){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile)};

        db.delete(TABLE,where,args);
        db.close();
    }

    public void deleteAllApps(int appType){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        String where = KEY_APP_TYPE+" = ?";
        String[] args = new String[]{String.valueOf(appType)};

        db.delete(TABLE,where,args);
        db.close();
    }

    public void deleteAllApps(){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        db.delete(TABLE,null,null);
        db.close();
    }

    public boolean isUnlockSet(int profile){
        final List<InfoAppsSelection> appsList = getApps(APP_TYPE_SHORTCUT,profile);
        if(appsList.isEmpty()){
            checkShortcutData();
        }else{
            for(InfoAppsSelection item : appsList){
                if(item.getShortcutId()==SHORTCUT_APP_01){
                    if(item.getShortcutAction()==ACTION_UNLOCK || item.getShortcutAction()==ACTION_DEFAULT){
                        return true;
                    }
                }else if(item.getShortcutAction()==ACTION_UNLOCK){
                    return true;
                }
            }
        }
        return false;
    }

    public List<InfoAppsSelection> getApps(int appType,int profile){
        final List<InfoAppsSelection> appsList = new ArrayList<>();
        //String selectQuery = "SELECT * FROM " + TABLE ;
        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile)};

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE,null,where,args,null,null,null);
        try{
            if(cursor.moveToFirst()){//If the cursor is empty, moveToFirst will be false
                do{
                    InfoAppsSelection app = new InfoAppsSelection();
                    app.setId(Integer.parseInt(cursor.getString(0)));
                    app.setAppType(Integer.parseInt(cursor.getString(1)));
                    app.setShortcutProfile(Integer.parseInt(cursor.getString(2)));
                    app.setShortcutId(Integer.parseInt(cursor.getString(3)));
                    app.setShortcutAction(Integer.parseInt(cursor.getString(4)));
                    app.setShortcutPIN(Integer.parseInt(cursor.getString(5)));
                    app.setAppPkg(cursor.getString(6));
                    app.setAppClass(cursor.getString(7));
                    app.setAppName(cursor.getString(8));
                    app.setAppSubInfo(cursor.getString(9));
                    appsList.add(app);
                }while(cursor.moveToNext());
            }
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }finally{
            db.close();
            if(cursor!=null){
                cursor.close();
            }
        }
        //Log.e("getShortcutApps1"," /size: "+appsList.size()+" /profile: "+profile);
        return appsList;
    }

    public void checkShortcutData(){
        initialShortcutData(SHORTCUT_APP_01);
        initialShortcutData(SHORTCUT_APP_02);
        initialShortcutData(SHORTCUT_APP_03);
        initialShortcutData(SHORTCUT_APP_04);
        initialShortcutData(SHORTCUT_APP_05);
        initialShortcutData(SHORTCUT_APP_06);
        initialShortcutData(SHORTCUT_APP_07);
        initialShortcutData(SHORTCUT_APP_08);
        initialShortcutData(VOLUME_UP);
        initialShortcutData(VOLUME_DOWN);
        initialShortcutData(GESTURE_UP);
        initialShortcutData(GESTURE_DOWN);
        initialShortcutData(GESTURE_LEFT);
        initialShortcutData(GESTURE_RIGHT);
        initialShortcutData(GESTURE_UP_2);
        initialShortcutData(GESTURE_DOWN_2);
        initialShortcutData(GESTURE_LEFT_2);
        initialShortcutData(GESTURE_RIGHT_2);
        initialShortcutData(DOUBLE_TAP1);
        initialShortcutData(DOUBLE_TAP2);
        initialShortcutData(TRIPLE_TAP1);
        initialShortcutData(TRIPLE_TAP2);
    }

    private void initialShortcutData(int shortcutId){
        try{
            handleAppsList(shortcutId,C.PROFILE_DEFAULT);
            handleAppsList(shortcutId,C.PROFILE_MUSIC);
            handleAppsList(shortcutId,C.PROFILE_LOCATION);
            handleAppsList(shortcutId,C.PROFILE_TIME);
            handleAppsList(shortcutId,C.PROFILE_WIFI);
            handleAppsList(shortcutId,C.PROFILE_BLUETOOTH);
        }catch(Exception e){
            e.printStackTrace();
            //mLocker.saveErrorLog(null,e);
        }
    }

    private void handleAppsList(int shortcutId,int profile){
        final List<InfoAppsSelection> appsList = getShortcutApps(shortcutId,profile);
        if(appsList!=null){
            if(appsList.isEmpty()){
                final InfoAppsSelection app = new InfoAppsSelection(shortcutId);
                app.setAppType(DataAppsSelection.APP_TYPE_SHORTCUT);
                app.setShortcutProfile(profile);
                addApp(app);
            }else if(appsList.size()>1){
                deleteSingleApp(DataAppsSelection.APP_TYPE_SHORTCUT,appsList.get(0).getShortcutProfile(),appsList.get(0).getShortcutId());
                final InfoAppsSelection app = appsList.get(appsList.size()-1);//get the last data
                addApp(app);
            }
        }
    }

    public void deleteSingleApp(int appType,int profile,int appShortcutId){
        SQLiteDatabase db = this.getWritableDatabase();
        //set null in "whereClause" to delete all row!
        final String where = KEY_APP_TYPE+" = ? AND "+
            KEY_SHORTCUT_PROFILE+" = ? AND "+
            KEY_SHORTCUT_ID+" = ?";
        final String[] args = new String[]{String.valueOf(APP_TYPE_SHORTCUT),String.valueOf(profile),String.valueOf(appShortcutId)};

        db.delete(TABLE,where,args);
        db.close();
    }

    public boolean isAllDefaultAction(int profile){
        final List<InfoAppsSelection> appsList = getApps(APP_TYPE_SHORTCUT,profile);
        if(appsList.isEmpty()){
            checkShortcutData();
            return true;
        }else{
            for(InfoAppsSelection item : appsList){
                if(item.getShortcutAction()!=ACTION_DEFAULT){
                    return false;//user already setup shortcuts
                }
            }
        }
        return true;
    }
}