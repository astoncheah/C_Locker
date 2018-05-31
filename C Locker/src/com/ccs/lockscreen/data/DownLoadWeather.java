package com.ccs.lockscreen.data;

public class DownLoadWeather{
    private int id;
    private String weatherProvider;
    private int weatherUpdateCode;
    private long weatherDate;
    private String weatherInfoName;
    private String weatherData;

    //constructors
    public DownLoadWeather(){
    }

    public DownLoadWeather(int id,String weatherProvider,int weatherUpdateCode,long weatherDate,
            String weatherInfoName,String weatherData){
        super();
        this.id = id;
        this.weatherProvider = weatherProvider;
        this.weatherUpdateCode = weatherUpdateCode;
        this.weatherDate = weatherDate;
        this.weatherInfoName = weatherInfoName;
        this.weatherData = weatherData;
    }

    public DownLoadWeather(String weatherProvider,int weatherUpdateCode,long weatherDate,
            String weatherInfoName,String weatherData){
        super();
        this.weatherProvider = weatherProvider;
        this.weatherUpdateCode = weatherUpdateCode;
        this.weatherDate = weatherDate;
        this.weatherInfoName = weatherInfoName;
        this.weatherData = weatherData;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getWeatherProvider(){
        return weatherProvider;
    }

    public void setWeatherProvider(String weatherProvider){
        this.weatherProvider = weatherProvider;
    }

    public int getWeatherUpdateCode(){
        return weatherUpdateCode;
    }

    public void setWeatherUpdateCode(int weatherUpdateCode){
        this.weatherUpdateCode = weatherUpdateCode;
    }

    public long getWeatherDate(){
        return weatherDate;
    }

    public void setWeatherDate(long weatherDate){
        this.weatherDate = weatherDate;
    }

    public String getWeatherInfoName(){
        return weatherInfoName;
    }

    public void setWeatherInfoName(String weatherInfoName){
        this.weatherInfoName = weatherInfoName;
    }

    public String getWeatherData(){
        return weatherData;
    }

    public void setWeatherData(String weatherData){
        this.weatherData = weatherData;
    }

}