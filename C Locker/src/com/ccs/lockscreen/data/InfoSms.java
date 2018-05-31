package com.ccs.lockscreen.data;

import android.os.Parcel;
import android.os.Parcelable;


public class InfoSms implements Parcelable{
    public static final Parcelable.Creator<InfoSms> CREATOR = new Parcelable.Creator<InfoSms>(){
        public InfoSms createFromParcel(Parcel in){
            return new InfoSms(in);
        }

        public InfoSms[] newArray(int size){
            return new InfoSms[size];
        }
    };
    private String smsNo;
    private String smsName;
    private String smsDateStr;
    private String smsContent;
    private String unReadCount;
    private long smsDate;

    //constructors
    public InfoSms(String smsNo,String smsName,String smsDateStr,String smsContent,
            String unReadCount,long smsDate){
        super();
        this.smsNo = smsNo;
        this.smsName = smsName;
        this.smsDateStr = smsDateStr;
        this.smsContent = smsContent;
        this.unReadCount = unReadCount;
        this.smsDate = smsDate;
    }

    public InfoSms(Parcel in){
        this.smsNo = in.readString();
        this.smsName = in.readString();
        this.smsDateStr = in.readString();
        this.smsContent = in.readString();
        this.unReadCount = in.readString();
        this.smsDate = in.readLong();
    }

    public String getSmsNo(){
        return smsNo;
    }

    public void setSmsNo(String smsNo){
        this.smsNo = smsNo;
    }

    public String getSmsName(){
        return smsName;
    }

    public void setSmsName(String smsName){
        this.smsName = smsName;
    }

    public String getSmsDateStr(){
        return smsDateStr;
    }

    public void setSmsDateStr(String smsDateStr){
        this.smsDateStr = smsDateStr;
    }

    public String getSmsContent(){
        return smsContent;
    }

    public void setSmsContent(String smsContent){
        this.smsContent = smsContent;
    }

    public String getUnReadCount(){
        return unReadCount;
    }

    public void setUnReadCount(String unReadCount){
        this.unReadCount = unReadCount;
    }

    public long getSmsDate(){
        return smsDate;
    }

    public void setSmsDate(long smsDate){
        this.smsDate = smsDate;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out,int arg1){
        out.writeString(smsNo);
        out.writeString(smsName);
        out.writeString(smsDateStr);
        out.writeString(smsContent);
        out.writeString(unReadCount);
        out.writeLong(smsDate);
    }
}