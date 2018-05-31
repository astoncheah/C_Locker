package com.ccs.lockscreen.security;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen_pro.SettingsSecuritySelfie;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

public class GoogleApiTokenTask extends AsyncTask<Void,Void,String>{
    private Context context;
    private MyCLocker mLocker;
    private String mScope;
    private String mEmail;
    private Account mAccount;
    private OnGoogleApiTokenListener callback;

    public GoogleApiTokenTask(Context context,String email,String scope,
            OnGoogleApiTokenListener callback){
        this.context = context;
        this.mScope = scope;
        this.mEmail = email;
        this.callback = callback;

        mLocker = new MyCLocker(context);
    }

    public GoogleApiTokenTask(Context context,Account account,String scope,
            OnGoogleApiTokenListener callback){
        this.context = context;
        this.mScope = scope;
        this.mAccount = account;
        this.callback = callback;

        mLocker = new MyCLocker(context);
    }

    @Override
    protected String doInBackground(Void... params){
        try{
            return fetchToken();
        }catch(IOException e){
            mLocker.saveErrorLog("IOException",e);
        }catch(Exception e){
            mLocker.saveErrorLog("Exception",e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String token){
        callback.onGoogleApiTokenTaskResult(token,mEmail);
        super.onPostExecute(token);
    }

    @SuppressWarnings("deprecation")
    protected String fetchToken() throws IOException{
        try{
            //return GoogleAuthUtil.getTokenWithNotification(context, mEmail, mScope, null);
            if(mAccount!=null){
                return GoogleAuthUtil.getToken(context,mAccount,mScope);
            }else{
                return GoogleAuthUtil.getToken(context,mEmail,mScope);
            }
        }catch(UserRecoverableAuthException e){
            if(context instanceof Activity){
                Intent intent = ((UserRecoverableAuthException)e).getIntent();
                ((Activity)context).startActivityForResult(intent,SettingsSecuritySelfie.REQUEST_CODE_PLAY_SERVICES_ERROR);
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"UserRecoverableAuthException error: context instanceof Activity");
            }else{
                mLocker.writeToFile(C.FILE_BACKUP_RECORD,"UserRecoverableAuthException error: context NOT instanceof Activity");
            }
            mLocker.saveErrorLog("UserRecoverableAuthException",e);
        }catch(GoogleAuthException e){
            mLocker.saveErrorLog("GoogleAuthException",e);
        }
        return null;
    }

    public interface OnGoogleApiTokenListener{
        void onGoogleApiTokenTaskResult(String token,String mEmail);
    }
}