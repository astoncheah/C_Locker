package com.ccs.lockscreen_pro;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.security.AutoSelfie;
import com.ccs.lockscreen.security.GoogleApiTokenTask;
import com.ccs.lockscreen.security.GoogleApiTokenTask.OnGoogleApiTokenListener;
import com.ccs.lockscreen.utils.BaseActivity;
import com.google.android.gms.common.AccountPicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SettingsSecuritySelfie extends BaseActivity implements OnGoogleApiTokenListener{
    public static final int //request code
            REQUEST_CODE_PICK_ACCOUNT = 2, REQUEST_CODE_PLAY_SERVICES_ERROR = 3;
    //
    public static final String SCOPE_GMAIL_FULL = "oauth2:https://mail.google.com/";
    public static final String SCOPE_GMAIL_COMPOSE = "oauth2:https://www.googleapis.com/auth/gmail.compose";//cant send email
    //=====
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private AutoSelfie as;
    private LinearLayout lytSecuritySelfie, lytEmailSelfie, lytShowSelfie, lytFailAttempts;
    private CheckBox cBoxSecuritySelfie, cBoxEmailSelfie, cBoxShowSelfie;
    private Spinner spnPinFailAttempts;
    private String mEmail;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.auto_security_selfie);
        setBasicBackKeyAction();

        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            as = new AutoSelfie(this);

            lytSecuritySelfie = (LinearLayout)findViewById(R.id.lytSecuritySelfie);
            lytEmailSelfie = (LinearLayout)findViewById(R.id.lytEmailSelfie);
            lytShowSelfie = (LinearLayout)findViewById(R.id.lytShowSelfie);
            lytFailAttempts = (LinearLayout)findViewById(R.id.lytFailAttempts);

            cBoxSecuritySelfie = (CheckBox)findViewById(R.id.cBoxSecuritySelfie);
            cBoxEmailSelfie = (CheckBox)findViewById(R.id.cBoxEmailSelfie);
            cBoxShowSelfie = (CheckBox)findViewById(R.id.cBoxShowSelfie);

            spinnerFunction();
            onClickFunction();
            loadSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        try{
            if(as!=null){
                as.releaseCamera("SettingsSecuritySelfie>onDestroy");
            }
        }catch(Exception e){
        }
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_security_selfie;
    }

    private void spinnerFunction(){
        spnPinFailAttempts = (Spinner)findViewById(R.id.spnFailAttempts);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPinFailAttempts.setAdapter(dataAdapter);
    }

    private void onClickFunction(){
        lytSecuritySelfie.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                cBoxSecuritySelfie.performClick();
            }
        });
        lytEmailSelfie.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                cBoxEmailSelfie.performClick();
            }
        });
        lytShowSelfie.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                cBoxShowSelfie.performClick();
            }
        });
        lytFailAttempts.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View arg0){
                spnPinFailAttempts.performClick();
            }
        });

        cBoxSecuritySelfie.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxSecuritySelfie.isChecked()){
                    lytEmailSelfie.setVisibility(View.VISIBLE);
                    lytShowSelfie.setVisibility(View.VISIBLE);
                    lytFailAttempts.setVisibility(View.VISIBLE);
                }else{
                    lytEmailSelfie.setVisibility(View.GONE);
                    lytShowSelfie.setVisibility(View.GONE);
                    lytFailAttempts.setVisibility(View.GONE);
                }
            }
        });
        cBoxEmailSelfie.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                if(cBoxEmailSelfie.isChecked()){
                    cBoxEmailSelfie.setChecked(false);
                    getAuthorization();
                }
            }
        });
    }

    //
    private void loadSettings(){
        cBoxSecuritySelfie.setChecked(prefs.getBoolean("cBoxSecuritySelfie",false));
        cBoxEmailSelfie.setChecked(prefs.getBoolean("cBoxEmailSelfie",false));
        cBoxShowSelfie.setChecked(prefs.getBoolean("cBoxShowSelfie",true));

        spnPinFailAttempts.setSelection(prefs.getInt("securitySelfieAttempts",2));

        //mEmail = prefs.getString("GetGoogleApiTokenTaskEmail",null);
        if(!cBoxSecuritySelfie.isChecked()){
            lytEmailSelfie.setVisibility(View.GONE);
            lytShowSelfie.setVisibility(View.GONE);
            lytFailAttempts.setVisibility(View.GONE);
        }
    }

    private void getAuthorization(){
        if(mEmail==null){
            pickUserAccount();
        }else{
            if(C.isInternetConnected(this)){
                new GoogleApiTokenTask(this,mEmail,SCOPE_GMAIL_FULL,this).execute();
            }else{
                Toast.makeText(this,R.string.network_error,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickUserAccount(){
        String[] accountTypes = new String[]{"com.google"};
        //param #4: true to always display account selection
        Intent intent = AccountPicker.newChooseAccountIntent(null,null,accountTypes,false,null,null,null,null);
        startActivityForResult(intent,REQUEST_CODE_PICK_ACCOUNT);
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        try{
            switch(requestCode){
                case REQUEST_CODE_PICK_ACCOUNT:
                    handleResult(resultCode,data);
                    break;
                case REQUEST_CODE_PLAY_SERVICES_ERROR:
                    handleAuthorizeResult(resultCode,data);
                    break;
            }
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try{
            saveSettings();
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    private void saveSettings(){
        editor.putBoolean("cBoxSecuritySelfie",cBoxSecuritySelfie.isChecked());
        editor.putBoolean("cBoxEmailSelfie",cBoxEmailSelfie.isChecked());
        editor.putBoolean("cBoxShowSelfie",cBoxShowSelfie.isChecked());

        int val = spnPinFailAttempts.getSelectedItemPosition();
        editor.putInt("securitySelfieAttempts",val);

        //editor.putString("GetGoogleApiTokenTaskEmail",mEmail);
        editor.commit();
    }

    @Override
    public void onGoogleApiTokenTaskResult(final String token,final String email){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_h:mm:ssa");
        String date = dateFormat.format(new Date());
        if(token!=null && !token.isEmpty()){
            cBoxEmailSelfie.setChecked(true);
            editor.putString("GetGoogleApiTokenTaskEmail",mEmail);
            editor.commit();
            saveLogs("GetGoogleApiTokenTask Successful: "+date);
        }else{
            saveLogs("GetGoogleApiTokenTask failed: "+date);
        }
    }

    private void handleResult(int resultCode,Intent data){
        if(resultCode==Activity.RESULT_OK){
            mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            getAuthorization();
            /*String userName = "";//get user name from email
            if (mEmail!=null && !mEmail.isEmpty()) {
                String email = mEmail;
                String[] parts = email.split("@");
                if (parts.length > 1)userName = parts[0];
            }
            Account account = new Account(userName, mEmail);
            AccountManager.get(context).confirmCredentials(account, null, context, new AccountManagerCallback<Bundle>(){
				@Override public void run(AccountManagerFuture<Bundle> future) {
				    try{
				    	Bundle result = future.getResult();			
				    	result.containsKey(AccountManager.KEY_INTENT);
				    	if (result.containsKey(AccountManager.KEY_INTENT)) {
				    		String str = result.getString(AccountManager.KEY_INTENT);
				    		Log.e("confirmCredentials",""+str);
				    	}	
				    	getAthu();
				    }catch (OperationCanceledException e){
				      // User cancelled login
				      e.printStackTrace();
				    }catch (AuthenticatorException e){
				      // Failed to login
				      e.printStackTrace();
				    }catch (IOException e){
				      e.printStackTrace();
				    }
				}            	
            },handler);*/
        }else if(resultCode==Activity.RESULT_CANCELED){
            //cBoxEmailSelfie.setChecked(false);
        }
    }

    private void handleAuthorizeResult(int resultCode,Intent data){
        if(resultCode==Activity.RESULT_CANCELED || data==null){
            //cBoxEmailSelfie.setChecked(false);
            saveLogs("handleAuthorizeResult failed: "+resultCode+"/"+data);
            return;
        }
        if(resultCode==Activity.RESULT_OK){
            getAuthorization();
            return;
        }
    }
}