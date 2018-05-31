package com.ccs.lockscreen_pro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.security.SecurityUnlockView;

public class SettingsPasswordUnlock extends Activity implements
        SecurityUnlockView.OnSecurityUnlockListener{
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        final int securityType = extras.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_PIN);
        setContentView(new SecurityUnlockView(this,true,this,securityType));
    }

    @Override
    public void onSettingResult(final int result,int securityType){
        final Intent i = new Intent();
        i.putExtra(C.SECURITY_TYPE,securityType);
        setResult(result,i);
        finish();
    }
}
