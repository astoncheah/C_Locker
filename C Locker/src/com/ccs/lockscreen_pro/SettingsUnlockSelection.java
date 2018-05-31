package com.ccs.lockscreen_pro;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.data.InfoAppsSelection;
import com.ccs.lockscreen.fragments.FragmentUnlockSelection;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;
import com.ccs.lockscreen.utils.MyAlertDialog;
import com.ccs.lockscreen.utils.MyAlertDialog.OnDialogListener;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.viewpagerindicator.LinePageIndicator;

import java.util.List;

public class SettingsUnlockSelection extends BaseActivity{
    private static final int PAGES = 6, UNLOCK_PIN_SET = 1;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private ViewPager pager;
    private TextView btnLeft;
    private int intLockStyle;
    private SpassFingerprint mSpassFingerprint;
    private SpassFingerprint.RegisterListener mRegisterListener = new SpassFingerprint.RegisterListener(){
        @Override
        public void onFinished(){
            try{
                final boolean hasRegisteredFinger = mSpassFingerprint.hasRegisteredFinger();
                if(hasRegisteredFinger){
                    finishFingerprintSetup();
                    return;
                }
            }catch(UnsupportedOperationException ignored){
            }
            Toast.makeText(SettingsUnlockSelection.this,getString(R.string.firnger_print_cancel),Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.unlocking_style));
        setBasicBackKeyAction();

        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            btnLeft = (TextView)findViewById(R.id.btnLeft);
            btnLeft.setTextColor(Color.BLACK);
            btnLeft.setBackgroundColor(Color.WHITE);
            btnLeft.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            btnLeft.setText(R.string.set);

            TextView btnRight = (TextView)findViewById(R.id.btnRight);
            btnRight.setVisibility(View.GONE);

            mSpassFingerprint = new SpassFingerprint(this);

            FragmentStatePagerAdapter adapter = new LineFragmentAdapter(getSupportFragmentManager());

            pager = (ViewPager)findViewById(R.id.pagerTips);
            //pager.setOffscreenPageLimit(1);
            pager.setAdapter(adapter);

            LinePageIndicator indicator = (LinePageIndicator)findViewById(R.id.indicatorTips);
            indicator.setViewPager(pager,this,null);
            indicator.setClickable(false);
            indicator.setBackgroundColor(Color.WHITE);
            onClickFunction();
            //load at last
            intLockStyle = prefs.getInt("intLockStyle",C.SIMPLE_UNLOCK);
            pager.setCurrentItem(loadLockStyle(intLockStyle));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy(){
        System.gc();
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.tips;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onClickFunction(){
        btnLeft.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent m){
                switch(m.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        btnLeft.setBackgroundColor(Color.GRAY);
                        break;
                    case MotionEvent.ACTION_UP:
                        btnLeft.setBackgroundColor(Color.WHITE);

                        int val = pager.getCurrentItem();
                        editor.putInt("intLockStyle",saveLockStyle(val));
                        editor.commit();

                        if(val==3 || val==4 || val==5){
                            finish();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private int loadLockStyle(int val){
        switch(val){
            case C.SIMPLE_UNLOCK://lollipop
                return 0;
            case C.GESTURE_UNLOCK://samsung unlock
                return 1;
            case C.FINGERPRINT_UNLOCK://fingerprint
                return 2;
            case C.CENTER_RING_UNLOCK://center ring
                return 3;
            case C.BOTTOM_RING_UNLOCK://bottom ring
                return 4;
            default://bottom triangle
                return 5;
        }
    }

    @TargetApi(23)
    private int saveLockStyle(int val){
        Intent i;
        switch(val){
            case 0://SIMPLE_UNLOCK
                new MyAlertDialog(this).selectSimpleUnlockPinRequired(new OnDialogListener(){
                    @Override
                    public void onDialogClickListener(int dialogType,int result){
                        editor.putInt("intLockStyle",C.SIMPLE_UNLOCK);
                        editor.commit();
                        finish();
                    }
                });
                return intLockStyle;
            case 1://gesture unlock
                new MyAlertDialog(this).selectGestureUnlock(new OnDialogListener(){
                    @Override
                    public void onDialogClickListener(int dialogType,int result){
                        editor.putInt("intLockStyle",C.GESTURE_UNLOCK);
                        editor.putInt("intGestureUnlockDirection",result);
                        editor.commit();
                        finish();
                    }
                });
                return intLockStyle;
            case 2://fingerprint
                try{
                    if(isSamsungFingerPrintSupported()){
                        boolean cBoxEnablePassUnlock = prefs.getBoolean("cBoxEnablePassUnlock",false);
                        if(!cBoxEnablePassUnlock){
                            new MyAlertDialog(this).simpleMsg(getString(R.string.firnger_print_enable_PIN),new OnDialogListener(){
                                @Override
                                public void onDialogClickListener(int dialogType,int result){
                                    new MyAlertDialog(SettingsUnlockSelection.this).selectSecurityUnlockType(new OnDialogListener(){
                                        @Override
                                        public void onDialogClickListener(int dialogType,
                                                int result){
                                            if(dialogType==MyAlertDialog.OK){
                                                int securityType;
                                                if(result==0){//pin
                                                    securityType = C.SECURITY_UNLOCK_PIN;
                                                }else{//pattern
                                                    securityType = C.SECURITY_UNLOCK_PATTERN;
                                                }
                                                final Intent i = new Intent(SettingsUnlockSelection.this,SettingsPasswordUnlock.class);
                                                i.putExtra(C.SECURITY_TYPE,securityType);
                                                startActivityForResult(i,UNLOCK_PIN_SET);
                                            }else{
                                                clearSecurity();
                                            }
                                        }
                                    });
                                }
                            });
                            return intLockStyle;
                        }
                        final boolean hasRegisteredFinger = mSpassFingerprint.hasRegisteredFinger();
                        if(!hasRegisteredFinger){
                            mSpassFingerprint.registerFinger(this,mRegisterListener);
                        }else{
                            return finishFingerprintSetup();
                        }
                    }else if(isAndroidFingerPrintSupported()){
                        boolean cBoxEnablePassUnlock = prefs.getBoolean("cBoxEnablePassUnlock",false);
                        if(!cBoxEnablePassUnlock){
                            new MyAlertDialog(this).simpleMsg(getString(R.string.firnger_print_enable_PIN),new OnDialogListener(){
                                @Override
                                public void onDialogClickListener(int dialogType,int result){
                                    new MyAlertDialog(SettingsUnlockSelection.this).selectSecurityUnlockType(new OnDialogListener(){
                                        @Override
                                        public void onDialogClickListener(int dialogType,
                                                int result){
                                            if(dialogType==MyAlertDialog.OK){
                                                int securityType;
                                                if(result==0){//pin
                                                    securityType = C.SECURITY_UNLOCK_PIN;
                                                }else{//pattern
                                                    securityType = C.SECURITY_UNLOCK_PATTERN;
                                                }
                                                final Intent i = new Intent(SettingsUnlockSelection.this,SettingsPasswordUnlock.class);
                                                i.putExtra(C.SECURITY_TYPE,securityType);
                                                startActivityForResult(i,UNLOCK_PIN_SET);
                                            }else{
                                                clearSecurity();
                                            }
                                        }
                                    });
                                }
                            });
                            return intLockStyle;
                        }
                        final FingerprintManager fm = (FingerprintManager)getSystemService(Context.FINGERPRINT_SERVICE);
                        if(!fm.hasEnrolledFingerprints()){
                            Toast.makeText(this,getString(R.string.fingerprint_registration_required),Toast.LENGTH_LONG).show();
                        }else{
                            return finishFingerprintSetup();
                        }
                    }else{
                        Toast.makeText(this,getString(R.string.fingerprint_unsupport),Toast.LENGTH_LONG).show();
                    }
                }catch(Exception e){
                    Toast.makeText(this,getString(R.string.fingerprint_unsupport),Toast.LENGTH_LONG).show();
                }
                return intLockStyle;
            case 3://center ring
                i = new Intent(this,SettingsShortcuts.class);
                i.putExtra("intLockStyle",C.CENTER_RING_UNLOCK);
                startActivity(i);
                return C.CENTER_RING_UNLOCK;
            case 4://bottom ring
                i = new Intent(this,SettingsShortcuts.class);
                i.putExtra("intLockStyle",C.BOTTOM_RING_UNLOCK);
                startActivity(i);
                return C.BOTTOM_RING_UNLOCK;
            default://bottom triangle
                i = new Intent(this,SettingsShortcuts.class);
                i.putExtra("intLockStyle",C.BOTTOM_TRIANGLE_UNLOCK);
                startActivity(i);
                return C.BOTTOM_TRIANGLE_UNLOCK;
        }
    }

    @TargetApi(23)
    private boolean isSamsungFingerPrintSupported(){
        try{
            final Spass spass = new Spass();
            spass.initialize(this);
            if(spass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT)){
                return true;
            }
        }catch(SsdkUnsupportedException | UnsupportedOperationException ignored){
        }
        return false;
    }

    private void clearSecurity(){
        editor.putBoolean("cBoxEnablePassUnlock",false);
        editor.putBoolean("cBoxBlockStatusbar",false);
        editor.putBoolean("cBoxBlockRecentApps",false);
        editor.putInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_NONE);
        //PIN
        editor.putInt("intPass2nd01",-1);
        editor.putInt("intPass2nd02",-1);
        editor.putInt("intPass2nd03",-1);
        editor.putInt("intPass2nd04",-1);
        editor.putInt("intPass2nd05",-1);
        editor.putInt("intPass2nd06",-1);
        editor.putInt("intPass2nd07",-1);
        editor.putInt("intPass2nd08",-1);
        //pattern
        editor.putString(C.PATTERN_LOCK,"");
        editor.commit();
    }

    private int finishFingerprintSetup(){
        new MyAlertDialog(this).selectHideFingerprintIcon(new OnDialogListener(){
            @Override
            public void onDialogClickListener(int dialogType,int result){
                editor.putInt("intLockStyle",C.FINGERPRINT_UNLOCK);
                editor.commit();
                finish();
            }
        });
        return C.FINGERPRINT_UNLOCK;
    }

    @TargetApi(23)
    private boolean isAndroidFingerPrintSupported(){
        if(C.isAndroid(Build.VERSION_CODES.M)){
            try{
                final FingerprintManager fm = (FingerprintManager)getSystemService(Context.FINGERPRINT_SERVICE);
                if(fm.isHardwareDetected()){
                    return true;
                }
            }catch(Exception ignored){
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        switch(requestCode){
            case UNLOCK_PIN_SET:
                if(resultCode==Activity.RESULT_OK){
                    final Bundle extras = data.getExtras();
                    int securityType = extras.getInt(C.SECURITY_TYPE,C.SECURITY_UNLOCK_PIN);

                    editor.putBoolean("cBoxEnablePassUnlock",true);
                    editor.putInt(C.SECURITY_TYPE,securityType);
                    editor.commit();

                    final DataAppsSelection db = new DataAppsSelection(this);
                    final List<InfoAppsSelection> appsList = db.getApps(DataAppsSelection.APP_TYPE_SHORTCUT);
                    for(InfoAppsSelection item : appsList){
                        if(item.getShortcutId()==DataAppsSelection.SHORTCUT_APP_01 && item.getShortcutAction()==DataAppsSelection.ACTION_DEFAULT){
                            db.setProfile(item.getShortcutProfile());// dont miss context!!
                            db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);

                        }else if(item.getShortcutAction()==DataAppsSelection.ACTION_UNLOCK){
                            db.setProfile(item.getShortcutProfile());// dont miss context!!
                            db.updateShortcutInfo(item.getShortcutId(),DataAppsSelection.KEY_SHORTCUT_PIN,DataAppsSelection.PIN_REQUIRED);
                        }
                    }
                    db.close();

                    saveLockStyle(pager.getCurrentItem());
                }else{
                    clearSecurity();
                    Toast.makeText(SettingsUnlockSelection.this,getString(R.string.firnger_print_cancel),Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private class LineFragmentAdapter extends FragmentStatePagerAdapter{
        LineFragmentAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            return FragmentUnlockSelection.newInstance(position);
        }

        @Override
        public int getCount(){
            return PAGES;
        }
    }
}