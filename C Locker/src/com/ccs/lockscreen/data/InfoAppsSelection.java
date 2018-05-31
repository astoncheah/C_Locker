package com.ccs.lockscreen.data;

import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable;

import com.ccs.lockscreen.myclocker.C;

import java.util.ArrayList;

public class InfoAppsSelection implements Parcelable{
    public static final Parcelable.Creator<InfoAppsSelection> CREATOR = new Parcelable.Creator<InfoAppsSelection>(){
        public InfoAppsSelection createFromParcel(Parcel in){
            return new InfoAppsSelection(in);
        }

        public InfoAppsSelection[] newArray(int size){
            return new InfoAppsSelection[size];
        }
    };
    private int id = -1;
    private int appType = -1;
    private int shortcutProfile = C.PROFILE_DEFAULT;
    private int shortcutId = -1;
    private int shortcutAction = DataAppsSelection.ACTION_DEFAULT;
    private int shortcutPIN = DataAppsSelection.PIN_NOT_REQUIRED;
    private String appPkg = "0";//put "0" for backup
    private String appClass = "0";//put "0" for backup
    private String appName = "0";//put "0" for backup
    private String appTag = "";
    private String appKey = "";
    private String appSubInfo = "0";//contact..//put "0" for backup
    private String appSubInfoContent = "";
    private int noticeAppId = -1;
    private int appPostCount = -1;
    private long appPostTime = -1;
    private boolean selected = false;
    private String allowDirectInput = null;
    private PendingIntent pi = null;
    private Notification.Action[] actions = null;
    private ArrayList<String> actionTitles = null;
    private ArrayList<Integer> actionIcons = null;

    public InfoAppsSelection(){
    }

    public InfoAppsSelection(int shortId){
        this.shortcutId = shortId;
    }

    @SuppressWarnings("unchecked")
    public InfoAppsSelection(Parcel in){
        this.id = in.readInt();
        this.appType = in.readInt();
        this.shortcutProfile = in.readInt();
        this.shortcutId = in.readInt();
        this.shortcutAction = in.readInt();
        this.shortcutPIN = in.readInt();
        this.appPkg = in.readString();
        this.appClass = in.readString();
        this.appName = in.readString();
        this.appTag = in.readString();
        this.appKey = in.readString();
        this.appSubInfo = in.readString();
        this.appSubInfoContent = in.readString();
        this.noticeAppId = in.readInt();
        this.appPostCount = in.readInt();
        this.appPostTime = in.readLong();
        this.actionTitles = (ArrayList<String>)in.readSerializable();
        this.actionIcons = (ArrayList<Integer>)in.readSerializable();
        this.allowDirectInput = in.readString();
        //? this.selected 			= false;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getAppType(){
        return appType;
    }

    public void setAppType(int appType){
        this.appType = appType;
    }

    public int getShortcutProfile(){
        return shortcutProfile;
    }

    public void setShortcutProfile(int shortcutProfile){
        this.shortcutProfile = shortcutProfile;
    }

    public int getShortcutId(){
        return shortcutId;
    }

    public void setShortcutId(int shortcutId){
        this.shortcutId = shortcutId;
    }

    public int getShortcutAction(){
        return shortcutAction;
    }

    public void setShortcutAction(int shortcutAction){
        this.shortcutAction = shortcutAction;
    }

    public int getShortcutPIN(){
        return shortcutPIN;
    }

    public void setShortcutPIN(int shortcutPIN){
        this.shortcutPIN = shortcutPIN;
    }

    public String getAppPkg(){
        return appPkg;
    }

    public void setAppPkg(String appPkg){
        this.appPkg = appPkg;
    }

    public String getAppClass(){
        return appClass;
    }

    public void setAppClass(String appClass){
        this.appClass = appClass;
    }

    public String getAppName(){
        return appName;
    }

    public void setAppName(String appName){
        this.appName = appName;
    }

    public String getAppTag(){
        return appTag;
    }

    public void setAppTag(String appTag){
        this.appTag = appTag;
    }

    public String getAppKey(){
        return appKey;
    }

    public void setAppKey(String appKey){
        this.appKey = appKey;
    }

    public String getAppSubInfo(){
        return appSubInfo;
    }

    public void setAppSubInfo(String appSubInfo){
        this.appSubInfo = appSubInfo;
    }

    public String getAppSubInfoContent(){
        return appSubInfoContent;
    }

    public void setAppSubInfoContent(String appSubInfoContent){
        this.appSubInfoContent = appSubInfoContent;
    }

    public int getNoticeAppId(){
        return noticeAppId;
    }

    public void setNoticeAppId(int noticeAppId){
        this.noticeAppId = noticeAppId;
    }

    public int getAppPostCount(){
        return appPostCount;
    }

    public void setAppPostCount(int appPostCount){
        this.appPostCount = appPostCount;
    }

    public long getAppPostTime(){
        return appPostTime;
    }

    public void setAppPostTime(long appPostTime){
        this.appPostTime = appPostTime;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
    public String getAllowDirectInput(){
        return allowDirectInput;
    }
    public void setAllowDirectInput(String allowDirectInput){
        this.allowDirectInput = allowDirectInput;
    }
    public PendingIntent getPi(){
        return pi;
    }

    public void setPi(PendingIntent pi){
        this.pi = pi;
    }

    public Notification.Action[] getActions(){
        return actions;
    }

    public void setActions(Notification.Action[] actions){
        this.actions = actions;
    }

    public ArrayList<String> getActionTitles(){
        return actionTitles;
    }

    public void setActionTitles(ArrayList<String> actionTitles){
        this.actionTitles = actionTitles;
    }

    public ArrayList<Integer> getActionIcons(){
        return actionIcons;
    }

    public void setActionIcons(ArrayList<Integer> actionIcons){
        this.actionIcons = actionIcons;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out,int flags){
        out.writeInt(id);
        out.writeInt(appType);
        out.writeInt(shortcutProfile);
        out.writeInt(shortcutId);
        out.writeInt(shortcutAction);
        out.writeInt(shortcutPIN);
        out.writeString(appPkg);
        out.writeString(appClass);
        out.writeString(appName);
        out.writeString(appTag);
        out.writeString(appKey);
        out.writeString(appSubInfo);
        out.writeString(appSubInfoContent);
        out.writeInt(noticeAppId);
        out.writeInt(appPostCount);
        out.writeLong(appPostTime);
        out.writeSerializable(actionTitles);
        out.writeSerializable(actionIcons);
        out.writeString(allowDirectInput);
        //? this.selected 			= false;
    }
}
