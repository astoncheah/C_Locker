package com.ccs.lockscreen.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.data.DataAppsSelection;
import com.ccs.lockscreen.fragments.SettingsSecurity;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.P;
import com.ccs.lockscreen.utils.BackpupDrive.DownloadFile;
import com.ccs.lockscreen.utils.GoogleDrive.DriveListListener;
import com.ccs.lockscreen_pro.ServiceDialog;
import com.google.android.gms.drive.DriveId;

import java.util.ArrayList;

public class MyAlertDialog{
    public static final int OK = -1;
    public static final int CANCEL = -2;
    public static final int SIMPLE_DIALOG = 0;
    private Context context;
    private MyCLocker appHandler;
    private AlertDialog alert;

    public MyAlertDialog(Context context){
        this.context = context;
        appHandler = new MyCLocker(context);
    }

    public final void alert(String message,DialogInterface.OnClickListener onClick){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setPositiveButton(context.getString(android.R.string.ok),onClick);
        alert = builder.create();
        alert.show();
    }

    public final void simpleMsg(String message){
        simpleMsg(message,null);
    }

    public final void simpleMsg(String message,final OnDialogListener listener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        if(listener!=null){
            builder.setNegativeButton(android.R.string.no,null);
        }
        builder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int id){
                if(listener!=null){
                    listener.onDialogClickListener(SIMPLE_DIALOG,OK);
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void usage(final String prefWindow,final int resId,String message){
        usage(prefWindow,resId,message,null);
    }

    public final void usage(final String prefWindow,final int resId,String message,
            final OnDialogListener listener){
        usage(prefWindow,resId,null,message,null);
    }

    public final void usage(final String prefWindow,final int resId,String title,String message,
            final OnDialogListener listener){
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        //final String version = getAppVersion();
        //if(!prefs.getBoolean(prefWindow+version,false)){
        if(!prefs.getBoolean(prefWindow,false)){
            final ScrollView scrl = new ScrollView(context);
            final LinearLayout lyt = new LinearLayout(context);
            final ImageView img = new ImageView(context);
            final TextView txt = new TextView(context);
            final CheckBox cBox = new CheckBox(context);
            final int pad = C.dpToPx(context,10);


            txt.setText(message+"\n");
            txt.setPadding(pad,pad,0,0);
            cBox.setText(context.getString(R.string.settings_dont_show_again));
            cBox.setPadding(pad,0,0,0);
            lyt.setGravity(Gravity.CENTER);
            lyt.setOrientation(LinearLayout.VERTICAL);
            lyt.setPadding(pad,pad,pad,pad);
            lyt.addView(cBox);
            if(resId!=0){
                img.setLayoutParams(new LinearLayout.LayoutParams(C.dpToPx(context,250),C.dpToPx(context,200)));
                img.setImageResource(resId);
                lyt.addView(img);
            }
            lyt.addView(txt);
            scrl.addView(lyt);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if(title==null){
                builder.setTitle(context.getString(R.string.usage_info));
            }else{
                builder.setTitle(title);
            }
            //builder.setMessage(message);
            builder.setView(scrl);
            builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){
                    //editor.putBoolean(prefWindow+version,cBox.isChecked());
                    editor.putBoolean(prefWindow,cBox.isChecked());
                    editor.commit();

                    if(listener!=null){
                        listener.onDialogClickListener(SIMPLE_DIALOG,OK);
                    }
                }
            });
            builder.setNegativeButton(R.string.later,new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int id){
                    editor.putBoolean(prefWindow,cBox.isChecked());
                    editor.commit();

                    if(listener!=null){
                        listener.onDialogClickListener(SIMPLE_DIALOG,CANCEL);
                    }
                }
            });
            alert = builder.create();
            alert.show();
        }
    }

    public final void tips(String title,String desc){
        try{
            final TextView txt = new TextView(context);
            txt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
            txt.setPadding(C.dpToPx(context,15),C.dpToPx(context,10),C.dpToPx(context,15),C.dpToPx(context,10));
            txt.setText(desc);
            txt.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            final LinearLayout lyt = new LinearLayout(context);
            lyt.setLayoutParams(new LinearLayout.LayoutParams(-2,-2));
            lyt.setPadding(0,C.dpToPx(context,10),0,C.dpToPx(context,10));
            lyt.setOrientation(LinearLayout.VERTICAL);
            lyt.addView(txt);
            builder.setTitle(title);
            builder.setView(lyt);
            builder.setPositiveButton(context.getString(android.R.string.ok),null);
            alert = builder.create();
            alert.setCanceledOnTouchOutside(true);
            alert.show();
        }catch(Exception e){
        }
    }

    public final void upgradeLocker(){
        try{
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.dialog_pro_version_title));
            builder.setMessage(context.getString(R.string.dialog_pro_version));
            builder.setNegativeButton(context.getString(R.string.later),new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){

                }
            });
            builder.setPositiveButton(context.getString(R.string.next),new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){
                    Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?"+"id=com.ccs.lockscreen_pro"));
                    context.startActivity(intent);
                }
            });
            alert = builder.create();
            alert.show();
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    public final void selectSimpleUnlockPinRequired(final OnDialogListener listener){
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        final LinearLayout lyt = linearLayout();
        final CheckBox cBoxCall = checkBox(context.getString(R.string.call));
        final CheckBox cBoxCamera = checkBox(context.getString(R.string.camera));

        lyt.addView(cBoxCall);
        lyt.addView(cBoxCamera);

        cBoxCall.setChecked(prefs.getBoolean("simpleUnlockRequiredPINCall",true));
        cBoxCamera.setChecked(prefs.getBoolean("simpleUnlockRequiredPINCamera",true));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.pin_required);
        builder.setMessage(R.string.pin_required_desc);
        builder.setView(lyt);
        builder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int id){
                editor.putBoolean("simpleUnlockRequiredPINCall",cBoxCall.isChecked());
                editor.putBoolean("simpleUnlockRequiredPINCamera",cBoxCamera.isChecked());
                editor.commit();
                if(listener!=null){
                    listener.onDialogClickListener(OK,OK);
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    //
    private LinearLayout linearLayout(){
        //final Drawable d = this.getResources().getDrawable(R.drawable.layout_selector);
        final int pad = C.dpToPx(context,16);
        final LinearLayout v = new LinearLayout(context);
        v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        v.setPadding(0,C.dpToPx(context,10),0,C.dpToPx(context,10));
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(pad,pad,pad,pad);
        v.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });
        return v;
    }

    private CheckBox checkBox(String txt){
        final int pad = C.dpToPx(context,8);
        final CheckBox v = new CheckBox(context);
        v.setText(txt);
        v.setPadding(pad,pad,pad,pad);
        return v;
    }

    public final void selectHideFingerprintIcon(final OnDialogListener listener){
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();

        final LinearLayout lyt = linearLayout();
        final CheckBox hideFingerprintIcon = checkBox(context.getString(R.string.hide_fingerprint_icon));

        lyt.addView(hideFingerprintIcon);
        hideFingerprintIcon.setChecked(prefs.getBoolean("hideFingerprintIcon",false));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(lyt);
        builder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int id){
                editor.putBoolean("hideFingerprintIcon",hideFingerprintIcon.isChecked());
                editor.commit();
                if(listener!=null){
                    listener.onDialogClickListener(OK,OK);
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void selectSecurityUnlockType(final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,new String[]{context.getString(R.string.pin),context.getString(R.string.pattern)});
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.select_security_type);
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                listener.onDialogClickListener(OK,item);
            }
        });
        builder.setOnCancelListener(new OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                listener.onDialogClickListener(CANCEL,CANCEL);
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void foundBackup(final OnDialogListener listener){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.settings_main_backup_found);
        builder.setMessage(R.string.settings_main_backup_found_desc);
        builder.setNegativeButton(android.R.string.no,null);
        builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0,int item){
                listener.onDialogClickListener(SIMPLE_DIALOG,item);
            }
        });
        alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    public final void selectBackupRestore(final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,new String[]{context.getString(R.string.settings_backup),context.getString(R.string.settings_backup)+" (Drive)",context.getString(R.string.settings_restore),context.getString(R.string.settings_restore)+" (Drive)"});
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.settings_backup_restore));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                listener.onDialogClickListener(SIMPLE_DIALOG,item);
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void selectRestoreList(final DriveListListener listener,
            ArrayList<DownloadFile> fileList){
        final ScrollView scrl = new ScrollView(context);
        final LinearLayout lyt = new LinearLayout(context);
        final int pad = C.dpToPx(context,10);

        lyt.setOrientation(LinearLayout.VERTICAL);
        lyt.setPadding(pad*2,pad,pad*2,pad);
        for(DownloadFile item : fileList){
            lyt.addView(lytRestoreList(listener,item.getTitle(),item.getId()));
        }
        scrl.addView(lyt);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.settings_restore));
        builder.setView(scrl);
        builder.setOnCancelListener(new OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialog){
                listener.onListResult(BackpupDrive.RESULT_ERROR,null);
            }
        });
        alert = builder.create();
        alert.show();
    }

    //
    private LinearLayout lytRestoreList(final DriveListListener listener,String title,
            final DriveId id){
        final LinearLayout lyt = new LinearLayout(context);
        lyt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        lyt.setOrientation(LinearLayout.HORIZONTAL);
        lyt.setGravity(Gravity.CENTER_VERTICAL);
        lyt.addView(addTxtHead(listener,title,id));
        lyt.addView(img(listener,R.drawable.ic_delete_white_48dp,id));
        return lyt;
    }

    private TextView addTxtHead(final DriveListListener listener,String str,final DriveId id){
        final int pad = C.dpToPx(context,16);
        final Drawable d = ContextCompat.getDrawable(context,R.drawable.layout_selector);
        final TextView txt = new TextView(context);
        txt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,1));
        txt.setPadding(0,pad,0,pad);
        txt.setText(str);
        txt.setTextSize(TypedValue.COMPLEX_UNIT_PX,context.getResources().getDimensionPixelSize(R.dimen.text_size_title));
        txt.setEllipsize(TruncateAt.END);
        txt.setMaxLines(1);
        txt.setBackground(d);
        txt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                listener.onListResult(BackpupDrive.RESULT_OK,id);
                alert.dismiss();
            }
        });
        return txt;
    }

    private ImageView img(final DriveListListener listener,int resid,final DriveId id){
        //final Drawable d = this.getResources().getDrawable(R.drawable.layout_selector);
        final int size = C.dpToPx(context,(int)(C.ICON_HEIGHT*0.6f));
        final Drawable d = ContextCompat.getDrawable(context,R.drawable.layout_selector);
        final RelativeLayout.LayoutParams pr = new RelativeLayout.LayoutParams(size,size);
        final ImageView img = new ImageView(context);
        img.setLayoutParams(pr);
        img.setImageResource(resid);
        img.setBackground(d);
        img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                listener.onListResult(BackpupDrive.RESULT_DELETE,id);
                alert.dismiss();
            }
        });
        return img;
    }

    public final void selectRestoreFile(final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,new String[]{"C Locker Free Backup","C Locker Pro Backup"});
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.settings_main_backup_found);
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                listener.onDialogClickListener(SIMPLE_DIALOG,item);
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void selectDirectCall(final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,new String[]{context.getString(R.string.direct_call),context.getString(R.string.open_dia_pad)});
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.settings_main_backup_found);
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
                switch(item){
                    case 0:
                        editor.putBoolean("directCall",true);
                        editor.commit();
                        break;
                    case 1:
                        editor.putBoolean("directCall",false);
                        editor.commit();
                        break;
                }
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void addNewWidgets(final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,addNewWidgetStrings());
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.settings_widget_dialog));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                listener.onDialogClickListener(SIMPLE_DIALOG,item);
            }
        });
        alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    //
    private String[] addNewWidgetStrings(){
        return new String[]{context.getString(R.string.settings_widget_locker_widget),context.getString(R.string.settings_widget_apps_widget),context.getString(R.string.settings_widget_delete_all),context.getString(R.string.widget_grid),context.getString(R.string.wallpaper)};
    }

    public final void widgetLongPress(int id,final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,longPressWidgetStrings(id));
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.apps_shortcut));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                listener.onDialogClickListener(SIMPLE_DIALOG,item);
            }
        });
        alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

    private String[] longPressWidgetStrings(int id){
        String[] items = null;
        switch(id){
            case C.CLOCK_ANALOG_CENTER:
            case C.CLOCK_DIGITAL_CENTER:
            case C.CLOCK_OWNER_MSG:
            case C.CLOCK_EVENT_LIST:
            case C.CLOCK_RSS:
                items = new String[]{context.getString(R.string.resize_widget),context.getString(R.string.settings_widget_delete),context.getString(R.string.settings_widget_delete_all),context.getString(R.string.settings)};
                break;
            default:
                items = new String[]{context.getString(R.string.resize_widget),context.getString(R.string.settings_widget_delete),context.getString(R.string.settings_widget_delete_all)};
                break;
        }
        return items;
    }

    public final void selectDefaultWidgets(final OnDialogListener listener){
        final String[] items = new String[]{context.getString(R.string.settings_widget_analog_center),context.getString(R.string.settings_widget_digital_center),context.getString(R.string.personal_msg),context.getString(R.string.settings_widget_event_list),context.getString(R.string.settings_widget_rss_list)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,items);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.apps_shortcut));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                listener.onDialogClickListener(SIMPLE_DIALOG,item);

            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void selectWidgetGridWidth(final OnDialogListener listener){
        final int max = C.getDefaultMaxWidgetSpanX(context);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,widgetGridStrings(true,max));
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.widget_grid_w);
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE).edit();
                editor.putInt("intWidgetGridWidth",max-2+item);
                editor.commit();
                listener.onDialogClickListener(OK,item);
            }
        });
        alert = builder.create();
        alert.show();
    }

    private String[] widgetGridStrings(boolean isW,int max){
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        int grid = prefs.getInt("intWidgetGridWidth",-1);
        if(!isW){
            grid = prefs.getInt("intWidgetGridHeight",-1);
        }
        return new String[]{setWgString(grid,max,-2),setWgString(grid,max,-1),setWgString(grid,max,0),setWgString(grid,max,1),setWgString(grid,max,2),setWgString(grid,max,3),setWgString(grid,max,4),setWgString(grid,max,5),setWgString(grid,max,6),setWgString(grid,max,7),setWgString(grid,max,8),setWgString(grid,max,9),setWgString(grid,max,10),setWgString(grid,max,11),setWgString(grid,max,12)};
    }

    private String setWgString(int grid,int max,int val){
        if(val==0){
            return "    "+max+" ("+context.getString(R.string.default_)+")";
        }else if((max+val)==grid){
            return "    "+(max+val)+" (Current)";
        }
        return "    "+(max+val);
    }

    public final void selectWidgetGridHeight(final OnDialogListener listener){
        final int max = C.getDefaultMaxWidgetSpanY(context);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,widgetGridStrings(false,max));
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.widget_grid_h);
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE).edit();
                editor.putInt("intWidgetGridHeight",max-2+item);
                editor.commit();
                listener.onDialogClickListener(OK,item);
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void selectBlockKey(final OnDialogListener listener){
        final int pad = C.dpToPx(context,16);
        final LinearLayout lyt = new LinearLayout(context);
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View line = (View)inflater.inflate(R.layout.view_line_vertical,lyt,false);
        lyt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
        lyt.setOrientation(LinearLayout.VERTICAL);
        lyt.setPadding(pad,pad,pad,pad);
        lyt.addView(listTitleDesc(lyt,context.getString(R.string.block_kome_key1),context.getString(R.string.block_kome_key1_desc),listener,SettingsSecurity.HOME_BLOCK_SERVICE_ACCESS));
        lyt.addView(line);
        lyt.addView(listTitleDesc(lyt,context.getString(R.string.block_kome_key2),context.getString(R.string.block_kome_key2_desc),listener,SettingsSecurity.HOME_BLOCK_SET_LAUNCHER));

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.security_set_homekey_);
        builder.setView(lyt);
        alert = builder.create();
        alert.show();
    }

    //
    private View listTitleDesc(ViewGroup root,String title,String desc,
            final OnDialogListener listener,final int type){
        final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout mainView = (LinearLayout)inflater.inflate(R.layout.list_title_desc,root,false);
        mainView.setBackground(ContextCompat.getDrawable(context,R.drawable.layout_selector));
        mainView.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                listener.onDialogClickListener(0,type);
                close();
            }
        });
        View v = mainView.getChildAt(0);
        if(v instanceof TextView){
            TextView txt = (TextView)v;
            txt.setText(title);
        }
        v = mainView.getChildAt(1);
        if(v instanceof TextView){
            TextView txt = (TextView)v;
            txt.setText(desc);
        }
        return mainView;
    }

    public final void close(){
        if(alert!=null && alert.isShowing()){
            alert.dismiss();
        }
    }

    @SuppressWarnings("deprecation")
    public final void timePicker(){
        try{
            final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = prefs.edit();

            final LinearLayout view = (LinearLayout)LinearLayout.inflate(context,R.layout.dialog_time_picker,null);
            final TimePicker timePickerStart = (TimePicker)view.findViewById(R.id.timePickerStart);
            final TimePicker timePickerEnd = (TimePicker)view.findViewById(R.id.timePickerEnd);

            final CheckBox cBoxMon = (CheckBox)view.findViewById(R.id.cBoxMon);
            final CheckBox cBoxTue = (CheckBox)view.findViewById(R.id.cBoxTue);
            final CheckBox cBoxWed = (CheckBox)view.findViewById(R.id.cBoxWed);
            final CheckBox cBoxThu = (CheckBox)view.findViewById(R.id.cBoxThu);
            final CheckBox cBoxFri = (CheckBox)view.findViewById(R.id.cBoxFri);
            final CheckBox cBoxSat = (CheckBox)view.findViewById(R.id.cBoxSat);
            final CheckBox cBoxSun = (CheckBox)view.findViewById(R.id.cBoxSun);

            timePickerStart.setIs24HourView(true);
            timePickerEnd.setIs24HourView(true);

            timePickerStart.setCurrentHour(prefs.getInt("timePickerStartHour",0));
            timePickerStart.setCurrentMinute(prefs.getInt("timePickerStartMin",0));
            timePickerEnd.setCurrentHour(prefs.getInt("timePickerEndHour",0));
            timePickerEnd.setCurrentMinute(prefs.getInt("timePickerEndMin",0));

            cBoxMon.setChecked(prefs.getBoolean("timeProfileMon",true));
            cBoxTue.setChecked(prefs.getBoolean("timeProfileTue",true));
            cBoxWed.setChecked(prefs.getBoolean("timeProfileWed",true));
            cBoxThu.setChecked(prefs.getBoolean("timeProfileThu",true));
            cBoxFri.setChecked(prefs.getBoolean("timeProfileFri",true));
            cBoxSat.setChecked(prefs.getBoolean("timeProfileSat",true));
            cBoxSun.setChecked(prefs.getBoolean("timeProfileSun",true));

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.fragment_time_profile));
            builder.setView(view);
            builder.setNegativeButton(context.getString(android.R.string.cancel),new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){

                }
            });
            builder.setPositiveButton(context.getString(android.R.string.ok),new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){
                    editor.putInt("timePickerStartHour",timePickerStart.getCurrentHour());
                    editor.putInt("timePickerStartMin",timePickerStart.getCurrentMinute());
                    editor.putInt("timePickerEndHour",timePickerEnd.getCurrentHour());
                    editor.putInt("timePickerEndMin",timePickerEnd.getCurrentMinute());

                    editor.putBoolean("timeProfileMon",cBoxMon.isChecked());
                    editor.putBoolean("timeProfileTue",cBoxTue.isChecked());
                    editor.putBoolean("timeProfileWed",cBoxWed.isChecked());
                    editor.putBoolean("timeProfileThu",cBoxThu.isChecked());
                    editor.putBoolean("timeProfileFri",cBoxFri.isChecked());
                    editor.putBoolean("timeProfileSat",cBoxSat.isChecked());
                    editor.putBoolean("timeProfileSun",cBoxSun.isChecked());
                    editor.commit();
                }
            });
            alert = builder.create();
            alert.show();
        }catch(Exception e){
            appHandler.saveErrorLog(null,e);
        }
    }

    public final void colorTheme(final String color){
        try{
            final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();

            final String strNo = context.getString(android.R.string.cancel);
            final String strOk = context.getString(android.R.string.ok);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.color_dialog_title));
            builder.setMessage(context.getString(R.string.color_dialog_desc));

            final View view = View.inflate(context,R.layout.dialog_apply_all_color,null);
            final CheckBox cBoxText = (CheckBox)view.findViewById(R.id.cBoxColorText);
            final CheckBox cBoxAnaClockText = (CheckBox)view.findViewById(R.id.cBoxColorAnalogClock);
            final CheckBox cBoxBlinking = (CheckBox)view.findViewById(R.id.cBoxColorBlinking);
            final CheckBox cBoxBg = (CheckBox)view.findViewById(R.id.cBoxColorBg);
            final CheckBox cBoxBttyLvl = (CheckBox)view.findViewById(R.id.cBoxColorBttyLvl);
            final CheckBox cBoxBottomBarText = (CheckBox)view.findViewById(R.id.cBoxColorBottomBarText);
            final CheckBox cBoxBottomBarTab = (CheckBox)view.findViewById(R.id.cBoxColorBottomBarTab);

            builder.setView(view);
            builder.setNegativeButton(strNo,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){

                }
            });
            builder.setPositiveButton(strOk,new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog,int id){
                    if(cBoxText.isChecked()){
                        editor.putString(P.STR_COLOR_LOCKER_MAIN_TEXT,color);
                    }
                    if(cBoxAnaClockText.isChecked()){
                        editor.putString(P.STR_COLOR_ANALOG_CLOCK,color);
                    }
                    if(cBoxBlinking.isChecked()){
                        editor.putString(P.STR_COLOR_LOCK_ANIMATION,color);
                    }
                    if(cBoxBg.isChecked()){
                        editor.putString(P.STR_COLOR_ICON_BG_ON_PRESS,color);
                    }
                    if(cBoxBttyLvl.isChecked()){
                        editor.putString(P.STR_COLOR_BATTERY_BAR,color);
                    }
                    if(cBoxBottomBarText.isChecked()){
                        editor.putString(P.STR_COLOR_BOTTOMBAR_TEXT,color);
                    }
                    if(cBoxBottomBarTab.isChecked()){
                        editor.putString(P.STR_COLOR_BOTTOMBAR_TAB,color);
                    }
                    if(cBoxAnaClockText.isChecked() && !cBoxBottomBarTab.isChecked()){
                        new SaveAnalogClockImage(context,1).execute(color);
                    }else if(!cBoxAnaClockText.isChecked() && cBoxBottomBarTab.isChecked()){
                        new SaveAnalogClockImage(context,2).execute(color);
                    }else if(cBoxAnaClockText.isChecked() && cBoxBottomBarTab.isChecked()){
                        new SaveAnalogClockImage(context,3).execute(color);
                    }
                    editor.commit();
                }
            });
            final AlertDialog alert = builder.create();
            alert.show();
        }catch(Exception e){
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    public final void selectGestureUnlock(final OnDialogListener listener){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.select_dialog_item,new String[]{context.getString(R.string.shortcut_gesture1_up),context.getString(R.string.shortcut_gesture1_down),context.getString(R.string.shortcut_gesture1_left),context.getString(R.string.shortcut_gesture1_right)});
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.gesture_unlock));
        builder.setAdapter(adapter,new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog,int item){
                int unlock = DataAppsSelection.GESTURE_UP;
                switch(item){
                    case 1:
                        unlock = DataAppsSelection.GESTURE_DOWN;
                        break;
                    case 2:
                        unlock = DataAppsSelection.GESTURE_LEFT;
                        break;
                    case 3:
                        unlock = DataAppsSelection.GESTURE_RIGHT;
                        break;
                }
                listener.onDialogClickListener(SIMPLE_DIALOG,unlock);
            }
        });
        alert = builder.create();
        alert.show();
    }

    public final void showServiceDialog(int isStr){
        showServiceDialog(context.getString(isStr));
    }

    public final void showServiceDialog(String str){
        Intent intent = new Intent(context,ServiceDialog.class);
        intent.putExtra("desc",str);
        context.startService(intent);
    }

    public interface OnDialogListener{
        public void onDialogClickListener(int dialogType,int result);//-1 = ok, -2 = cancel;
    }
}