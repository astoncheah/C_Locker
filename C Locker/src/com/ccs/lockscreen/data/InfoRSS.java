package com.ccs.lockscreen.data;

public class InfoRSS{
    private int id;
    private String rssSource;
    private String rssTitle;
    private String rssPubDate;
    private String rssDesc;
    private String rssContent;
    private String rssCategory;
    private String rssLink;

    //constructors
    public InfoRSS(){
    }

    public InfoRSS(String rssSource,String rssTitle,String rssPubDate,String rssDesc,
            String rssContent,String rssCategory,String rssLink){
        super();
        this.rssSource = rssSource;
        this.rssTitle = rssTitle;
        this.rssPubDate = rssPubDate;
        this.rssDesc = rssDesc;
        this.rssContent = rssContent;
        this.rssCategory = rssCategory;
        this.rssLink = rssLink;
    }

    public InfoRSS(int id,String rssSource,String rssTitle,String rssPubDate,String rssDesc,
            String rssContent,String rssCategory,String rssLink){
        super();
        this.id = id;
        this.rssSource = rssSource;
        this.rssTitle = rssTitle;
        this.rssPubDate = rssPubDate;
        this.rssDesc = rssDesc;
        this.rssContent = rssContent;
        this.rssCategory = rssCategory;
        this.rssLink = rssLink;
    }

    //getter&setter
    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getRssSource(){
        return rssSource;
    }

    public void setRssSource(String rssSource){
        this.rssSource = rssSource;
    }

    public String getRssTitle(){
        return rssTitle;
    }

    public void setRssTitle(String rssTitle){
        this.rssTitle = rssTitle;
    }

    public String getRssPubDate(){
        return rssPubDate;
    }

    public void setRssPubDate(String rssPubDate){
        this.rssPubDate = rssPubDate;
    }

    public String getRssDesc(){
        return rssDesc;
    }

    public void setRssDesc(String rssDesc){
        this.rssDesc = rssDesc;
    }

    public String getRssContent(){
        return rssContent;
    }

    public void setRssContent(String rssContent){
        this.rssContent = rssContent;
    }

    public String getRssCategory(){
        return rssCategory;
    }

    public void setRssCategory(String rssCategory){
        this.rssCategory = rssCategory;
    }

    public String getRssLink(){
        return rssLink;
    }

    public void setRssLink(String rssLink){
        this.rssLink = rssLink;
    }
}
