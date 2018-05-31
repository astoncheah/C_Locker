package com.ccs.lockscreen.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;

public abstract class BaseActivity extends AppCompatActivity{
    private MyCLocker appHandler;
    private Toolbar toolbar;

    @SuppressLint({"InflateParams","InlinedApi","NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        int lyt = getLayoutResource();
        if(lyt!=0){
            setContentView(lyt);
        }

        appHandler = new MyCLocker(this);
        toolbar = (Toolbar)findViewById(R.id.toolbar);

        if(toolbar!=null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }else{
            toolbar = (Toolbar)getLayoutInflater().inflate(R.layout.toolbar,null);
            setSupportActionBar(toolbar);
        }
        toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.colorPrimary));
        //toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.windowBackgroundColor));

        if(C.isAndroid(Build.VERSION_CODES.LOLLIPOP)){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorPrimaryDark));
        }
    }

    @Override
    protected void onDestroy(){
        try{
            if(appHandler!=null){
                appHandler.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    protected abstract int getLayoutResource();

    protected Toolbar getToolbar(){
        return toolbar;
    }

    protected void setToolbarTitle(int res){
        toolbar.setTitle(res);
    }

    protected void setToolbarTitle(String str){
        toolbar.setTitle(str);
    }

    protected void setBasicBackKeyAction(){
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        //toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
            }
        });
    }

    protected void setActionBarIcon(int iconRes){
        toolbar.setNavigationIcon(iconRes);
    }

    //
    public void showToast(int id){
        Toast.makeText(this,id,Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(int id){
        Toast.makeText(this,id,Toast.LENGTH_LONG).show();
    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void showToastLong(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    public void saveLogs(String message){
        if(appHandler!=null){
            appHandler.writeToFile(C.FILE_BACKUP_RECORD,message);
        }
    }

    public void saveErrorLogs(String message,Exception e){
        if(appHandler!=null){
            appHandler.saveErrorLog(message,e);
        }
    }

    public MyCLocker getAppHandler(){
        return appHandler;
    }
}