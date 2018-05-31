package com.ccs.lockscreen.data;

import android.os.Parcel;
import android.os.Parcelable;


public class InfoCallLog implements Parcelable{
    public static final Parcelable.Creator<InfoCallLog> CREATOR = new Parcelable.Creator<InfoCallLog>(){
        public InfoCallLog createFromParcel(Parcel in){
            return new InfoCallLog(in);
        }

        public InfoCallLog[] newArray(int size){
            return new InfoCallLog[size];
        }
    };
    private String callNo;
    private String callName;
    private String callDateStr;
    private String isRead;
    private long callDate;

    //constructors
    public InfoCallLog(String callNo,String callName,String callDateStr,String isRead,
            long callDate){
        super();
        this.callNo = callNo;
        this.callName = callName;
        this.callDateStr = callDateStr;
        this.isRead = isRead;
        this.callDate = callDate;
    }

    public InfoCallLog(Parcel in){
        this.callNo = in.readString();
        this.callName = in.readString();
        this.callDateStr = in.readString();
        this.isRead = in.readString();
        this.callDate = in.readLong();
    }

    public String getCallNo(){
        return callNo;
    }

    public void setCallNo(String callNo){
        this.callNo = callNo;
    }

    public String getCallName(){
        return callName;
    }

    public void setCallName(String callName){
        this.callName = callName;
    }

    public String getCallDateStr(){
        return callDateStr;
    }

    public void setCallDateStr(String callDateStr){
        this.callDateStr = callDateStr;
    }

    public String getIsRead(){
        return isRead;
    }

    public void setIsRead(String isRead){
        this.isRead = isRead;
    }

    public long getCallDate(){
        return callDate;
    }

    public void setCallDate(long callDate){
        this.callDate = callDate;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out,int arg1){
        out.writeString(callNo);
        out.writeString(callName);
        out.writeString(callDateStr);
        out.writeString(isRead);
        out.writeLong(callDate);
    }
}