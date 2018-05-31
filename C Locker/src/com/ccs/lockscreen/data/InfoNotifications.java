package com.ccs.lockscreen.data;

public class InfoNotifications{
    private int id;
    private int type;
    private int contact;
    private String content;
    private long dateReceived;

    //constructors
    public InfoNotifications(int type,int contact,String content,long dateReceived){
        this.type = type;
        this.contact = contact;
        this.content = content;
        this.dateReceived = dateReceived;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getType(){
        return type;
    }

    public void setType(int type){
        this.type = type;
    }

    public int getContact(){
        return contact;
    }

    public void setContact(int contact){
        this.contact = contact;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content){
        this.content = content;
    }

    public long getDateReceived(){
        return dateReceived;
    }

    public void setDateReceived(long dateReceived){
        this.dateReceived = dateReceived;
    }

}
