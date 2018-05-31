package com.ccs.lockscreen.data;

import android.os.Parcel;
import android.os.Parcelable;


public class InfoCalendarEvent implements Parcelable{
    public static final Parcelable.Creator<InfoCalendarEvent> CREATOR = new Parcelable.Creator<InfoCalendarEvent>(){
        public InfoCalendarEvent createFromParcel(Parcel in){
            return new InfoCalendarEvent(in);
        }

        public InfoCalendarEvent[] newArray(int size){
            return new InfoCalendarEvent[size];
        }
    };
    private int id;
    private int cldAccountId;
    private int cldEventId;
    private String cldTitle;
    private long cldDateStart;
    private long cldDateEnd;
    private String cldLocation;
    private String cldAllDayEvent;

    //constructors
    public InfoCalendarEvent(){
    }

    public InfoCalendarEvent(int cldAccountId,int cldEventId,String cldTitle,long cldDateStart,
            long cldSateEnd,String cldLocation,String cldAllDayEvent){
        super();
        this.cldAccountId = cldAccountId;
        this.cldEventId = cldEventId;
        this.cldTitle = cldTitle;
        this.cldDateStart = cldDateStart;
        this.cldDateEnd = cldSateEnd;
        this.cldLocation = cldLocation;
        this.cldAllDayEvent = cldAllDayEvent;
    }

    private InfoCalendarEvent(Parcel in){
        this.cldAccountId = in.readInt();
        this.cldEventId = in.readInt();
        this.cldTitle = in.readString();
        this.cldDateStart = in.readLong();
        this.cldDateEnd = in.readLong();
        this.cldLocation = in.readString();
        this.cldAllDayEvent = in.readString();
    }

    //getter&setter
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getCldAccountId(){
        return cldAccountId;
    }

    public void setCldAccountId(int cldAccountId){
        this.cldAccountId = cldAccountId;
    }

    public int getCldEventId(){
        return cldEventId;
    }

    public void setCldEventId(int cldEventId){
        this.cldEventId = cldEventId;
    }

    public String getCldTitle(){
        return cldTitle;
    }

    public void setCldTitle(String cldTitle){
        this.cldTitle = cldTitle;
    }

    public long getCldDateStart(){
        return cldDateStart;
    }

    public void setCldDateStart(long cldDateStart){
        this.cldDateStart = cldDateStart;
    }

    public long getCldDateEnd(){
        return cldDateEnd;
    }

    public void setCldDateEnd(long cldDateEnd){
        this.cldDateEnd = cldDateEnd;
    }

    public String getCldLocation(){
        return cldLocation;
    }

    public void setCldLocation(String cldLocation){
        this.cldLocation = cldLocation;
    }

    public String getCldAllDayEvent(){
        return cldAllDayEvent;
    }

    public void setCldAllDayEvent(String cldAllDayEvent){
        this.cldAllDayEvent = cldAllDayEvent;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out,int arg1){
        out.writeInt(cldAccountId);
        out.writeInt(cldEventId);
        out.writeString(cldTitle);
        out.writeLong(cldDateStart);
        out.writeLong(cldDateEnd);
        out.writeString(cldLocation);
        out.writeString(cldAllDayEvent);
    }
}