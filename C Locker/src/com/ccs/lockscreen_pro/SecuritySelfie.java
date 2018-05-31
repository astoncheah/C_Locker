package com.ccs.lockscreen_pro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.security.AutoSelfie;
import com.ccs.lockscreen.utils.BaseActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SecuritySelfie extends BaseActivity{
    private static final String IMG_TYPE = "image/jpeg";
    private TextView txtSecuritySelfieDesc;
    private ImageView imgSecuritySelfieDesc;
    private Button btnDelete, btnShare;
    private File pictureFile, tempFile;
    private Bitmap bm;
    private String pictureFileDir;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.auto_security_selfie);
        setBasicBackKeyAction();
        try{
            pictureFileDir = AutoSelfie.getDir();
            SharedPreferences prefs = getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            String latestThiefSelfieFile = prefs.getString("latestThiefSelfieFile","");
            String latestThiefSelfieTime = prefs.getString("latestThiefSelfieTime","");
            String filename = pictureFileDir+File.separator+latestThiefSelfieFile;
            tempFile = new File(pictureFileDir+File.separator+"t_"+latestThiefSelfieFile);

            pictureFile = new File(filename);
            if(!pictureFile.exists()){
                saveLogs("SecuritySelfie file not exist: "+pictureFile.getPath());
                finish();
                return;
            }
            bm = BitmapFactory.decodeFile(pictureFile.getPath());

            txtSecuritySelfieDesc = (TextView)findViewById(R.id.txtSecuritySelfieDesc);
            imgSecuritySelfieDesc = (ImageView)findViewById(R.id.imgSecuritySelfieDesc);
            btnDelete = (Button)this.findViewById(R.id.btnDelete);
            btnShare = (Button)this.findViewById(R.id.btnShare);

            int pad = C.dpToPx(this,10);
            txtSecuritySelfieDesc.setText(getString(R.string.auto_security_selfie_desc1)+latestThiefSelfieTime);
            txtSecuritySelfieDesc.setPadding(pad,pad,pad,pad);

            imgSecuritySelfieDesc.setImageBitmap(bm);
            imgSecuritySelfieDesc.setScaleType(ScaleType.FIT_XY);
            imgSecuritySelfieDesc.setAdjustViewBounds(true);

            onClick();
        }catch(Exception e){
            saveErrorLogs(null,e);
            finish();
        }catch(OutOfMemoryError e){
            saveErrorLogs("OutOfMemoryError",null);
            finish();
        }
    }

    @Override
    protected void onDestroy(){
        if(imgSecuritySelfieDesc!=null){
            imgSecuritySelfieDesc.setImageBitmap(null);
            imgSecuritySelfieDesc = null;
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.security_selfie;
    }

    private void onClick(){
        imgSecuritySelfieDesc.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                try{
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    //intent.setType("image/jpeg");
                    //intent.setData(Uri.fromFile(pictureFile));
                    intent.setDataAndType(Uri.fromFile(pictureFile),IMG_TYPE);
                    startActivity(intent);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        btnDelete.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                pictureFile.delete();
                finish();
            }
        });
        btnShare.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                Bitmap bm = SecuritySelfie.this.bm;
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG,100,bytes);

                try{
                    tempFile.createNewFile();
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
                }catch(IOException e){
                    e.printStackTrace();
                }

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType(IMG_TYPE);
                share.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(tempFile));
                startActivityForResult(Intent.createChooser(share,"Share Image"),1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==1){
            tempFile.delete();
        }
    }
}
