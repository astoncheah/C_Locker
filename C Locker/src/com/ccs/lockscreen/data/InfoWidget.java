package com.ccs.lockscreen.data;

public class InfoWidget{
    private int id;
    private int widgetProfile;
    private int widgetId;
    private int widgetType;
    private String widgetPkgsName;
    private String widgetClassName;
    private int widgetIndex;
    private int widgetX;
    private int widgetY;
    private int widgetSpanX;
    private int widgetSpanY;
    private int widgetMinWidth;
    private int widgetMinHeight;

    public InfoWidget(){
    }

    public InfoWidget(int id,int widgetProfile,int widgetId,int widgetType,String widgetPkgsName,
            String widgetClassName,int widgetIndex,int widgetX,int widgetY,int widgetSpanX,
            int widgetSpanY,int widgetMinWidth,int widgetMinHeight){
        super();
        this.id = id;
        this.widgetProfile = widgetProfile;
        this.widgetId = widgetId;
        this.widgetType = widgetType;
        this.widgetPkgsName = widgetPkgsName;
        this.widgetClassName = widgetClassName;
        this.widgetIndex = widgetIndex;
        this.widgetX = widgetX;
        this.widgetY = widgetY;
        this.widgetSpanX = widgetSpanX;
        this.widgetSpanY = widgetSpanY;
        this.widgetMinWidth = widgetMinWidth;
        this.widgetMinHeight = widgetMinHeight;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getWidgetProfile(){
        return widgetProfile;
    }

    public void setWidgetProfile(int widgetProfile){
        this.widgetProfile = widgetProfile;
    }

    public int getWidgetId(){
        return widgetId;
    }

    public void setWidgetId(int widgetId){
        this.widgetId = widgetId;
    }

    public int getWidgetType(){
        return widgetType;
    }

    public void setWidgetType(int widgetType){
        this.widgetType = widgetType;
    }

    public String getWidgetPkgsName(){
        return widgetPkgsName;
    }

    public void setWidgetPkgsName(String widgetPkgsName){
        this.widgetPkgsName = widgetPkgsName;
    }

    public String getWidgetClassName(){
        return widgetClassName;
    }

    public void setWidgetClassName(String widgetClassName){
        this.widgetClassName = widgetClassName;
    }

    public int getWidgetIndex(){
        return widgetIndex;
    }

    public void setWidgetIndex(int widgetIndex){
        this.widgetIndex = widgetIndex;
    }

    public int getWidgetX(){
        return widgetX;
    }

    public void setWidgetX(int widgetX){
        this.widgetX = widgetX;
    }

    public int getWidgetY(){
        return widgetY;
    }

    public void setWidgetY(int widgetY){
        this.widgetY = widgetY;
    }

    public int getWidgetSpanX(){
        return widgetSpanX;
    }

    public void setWidgetSpanX(int widgetSpanX){
        this.widgetSpanX = widgetSpanX;
    }

    public int getWidgetSpanY(){
        return widgetSpanY;
    }

    public void setWidgetSpanY(int widgetSpanY){
        this.widgetSpanY = widgetSpanY;
    }

    public int getWidgetMinWidth(){
        return widgetMinWidth;
    }

    public void setWidgetMinWidth(int widgetMinWidth){
        this.widgetMinWidth = widgetMinWidth;
    }

    public int getWidgetMinHeight(){
        return widgetMinHeight;
    }

    public void setWidgetMinHeight(int widgetMinHeight){
        this.widgetMinHeight = widgetMinHeight;
    }
}
