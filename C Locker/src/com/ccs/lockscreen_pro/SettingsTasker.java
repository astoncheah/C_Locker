package com.ccs.lockscreen_pro;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.utils.BaseActivity;

public class SettingsTasker extends BaseActivity{
    //=====
    private SharedPreferences.Editor editor;
    private SharedPreferences prefs;
    private CheckBox cBoxTaskerShortcutCount;
    private LinearLayout lytTaskerShortcutCount;
    private TextView txtPkgName;
    private EditText editTaskerUnlock;
    private Button btnCopy;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_tasker);
        setBasicBackKeyAction();

        prefs = getSharedPreferences(C.PREFS_NAME,MODE_PRIVATE);
        editor = prefs.edit();

        try{
            lytTaskerShortcutCount = (LinearLayout)findViewById(R.id.lytTaskerShortcutCount);
            lytTaskerShortcutCount.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    cBoxTaskerShortcutCount.performClick();
                }
            });

            cBoxTaskerShortcutCount = (CheckBox)findViewById(R.id.cBoxTaskerShortcutCount);
            txtPkgName = (TextView)findViewById(R.id.txtPkgName);
            editTaskerUnlock = (EditText)findViewById(R.id.editTaskerUnlock);
            btnCopy = (Button)findViewById(R.id.btnCopy);
            btnCopy.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v){
                    if(editTaskerUnlock.getText().toString().equals("")){
                        Toast.makeText(SettingsTasker.this,getString(R.string.tasker_unlock_desc2)+" can not be empty!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final String str = txtPkgName.getText()+editTaskerUnlock.getText().toString();
                    final ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    final ClipData clip = ClipData.newPlainText(str,str);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(SettingsTasker.this,"Copied: "+str,Toast.LENGTH_SHORT).show();
                }
            });

            loadSettings();
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }catch(Exception e){
            saveErrorLogs(null,e);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    @Override
    protected int getLayoutResource(){
        return R.layout.settings_tasker;
    }

    private void loadSettings(){
        cBoxTaskerShortcutCount.setChecked(prefs.getBoolean("cBoxTaskerShortcutCount",false));
        editTaskerUnlock.setText(prefs.getString("strTaskerUnlock",""));
        txtPkgName.setText(getPackageName()+C.TASKER);
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

    @Override
    protected void onResume(){
        super.onResume();
    }

    private void saveSettings(){
        editor.putBoolean("cBoxTaskerShortcutCount",cBoxTaskerShortcutCount.isChecked());
        editor.putString("strTaskerUnlock",editTaskerUnlock.getText().toString());
        editor.commit();
    }
}