package com.ccs.lockscreen.data;

import android.os.Parcel;
import android.os.Parcelable;


public class InfoGmail implements Parcelable{
    public static final Parcelable.Creator<InfoGmail> CREATOR = new Parcelable.Creator<InfoGmail>(){
        public InfoGmail createFromParcel(Parcel in){
            return new InfoGmail(in);
        }

        public InfoGmail[] newArray(int size){
            return new InfoGmail[size];
        }
    };
    private String mailBox;
    private String read;
    private String unread;

    //constructors
    public InfoGmail(String mailBox,String read,String unread){
        super();
        this.mailBox = mailBox;
        this.read = read;
        this.unread = unread;
    }

    public InfoGmail(Parcel in){
        this.mailBox = in.readString();
        this.read = in.readString();
        this.unread = in.readString();
    }

    public String getMailBox(){
        return mailBox;
    }

    public void setMailBox(String mailBox){
        this.mailBox = mailBox;
    }

    public String getRead(){
        return read;
    }

    public void setRead(String read){
        this.read = read;
    }

    public String getUnread(){
        return unread;
    }

    public void setUnread(String unread){
        this.unread = unread;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out,int arg1){
        out.writeString(mailBox);
        out.writeString(read);
        out.writeString(unread);
    }
}