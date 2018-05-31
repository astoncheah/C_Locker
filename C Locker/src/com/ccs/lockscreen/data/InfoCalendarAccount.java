package com.ccs.lockscreen.data;

public class InfoCalendarAccount{
    private String id;
    private String email;
    private String name;
    private boolean selected;

    public InfoCalendarAccount(String id,String email,String name){
        this.id = id;
        this.email = email;
        this.name = name;
        selected = false;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public boolean isSelected(){
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }
}
