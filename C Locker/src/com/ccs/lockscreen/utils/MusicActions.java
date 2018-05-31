package com.ccs.lockscreen.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.myclocker.SaveData;

public final class MusicActions{
    private static final String SERVICECMD = "com.android.music.musicservicecommand";
    private static final String META_CHANGED = "com.android.music.metachanged";
    private static final String PLAY_STATE_CHANGED = "com.android.music.playstatechanged";
    private static final String PLAY_BACK_COMPLETE = "com.android.music.playbackcomplete";
    private static final String QUEUE_CHANGED = "com.android.music.queuechanged";
    private static final String CMD_COMMAND = "command";
    //private static final String CMD_ARTIST 		= "artist";
    //private static final String CMD_ALBUM			= "album";
    //private static final String CMD_TRACK 		= "track";
    //private static final String CMDTOGGLEPAUSE 	= "togglepause";
    //private static final String CMDSTOP 			= "stop";
    private static final String CMD_PLAY = "play";
    private static final String CMD_PAUSE = "pause";
    private static final String CMD_PREVIOUS = "previous";
    private static final String CMD_NEXT = "next";
    private Context context;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String playState = "?", strMusicWidgetPkgName;
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context,Intent intent){
            try{
                String action = intent.getAction();
                //String cmd = intent.getStringExtra(CMD_COMMAND);
                //String artist = intent.getStringExtra(CMD_ARTIST);
                //String album = intent.getStringExtra(CMD_ALBUM);
                //String track = intent.getStringExtra(CMD_TRACK);
                //Log.e("MusicAction.onReceive ", action + " / " + cmd);
                //Log.e("MusicAction",artist+":"+album+":"+track);
                if(action.equals(META_CHANGED)){

                }else if(action.equals(PLAY_STATE_CHANGED)){
                    if(isMusicPlaying() && new SaveData(context).isLockerRunning()){
                        if(!playState.equals("?")){
                            ;
                            //Toast.makeText(context, "Media "+playState, Toast.LENGTH_SHORT).show();
                            Log.e("MusicActions","PLAY_STATE_CHANGED");
                        }
                    }
                }else if(action.equals(PLAY_BACK_COMPLETE)){

                }else if(action.equals(QUEUE_CHANGED)){

                }
            }catch(Exception e){
                new MyCLocker(context).saveErrorLog(null,e);
            }
        }
    };

    public MusicActions(Context context){
        this.context = context;

        prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        editor = prefs.edit();

        IntentFilter iF = new IntentFilter();
        iF.addAction(META_CHANGED);
        iF.addAction(PLAY_STATE_CHANGED);
        iF.addAction(PLAY_BACK_COMPLETE);
        iF.addAction(QUEUE_CHANGED);
        context.registerReceiver(mReceiver,iF);
    }

    public final void close(){
        try{
            context.unregisterReceiver(mReceiver);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public final boolean isMusicPlaying(){
        AudioManager myAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        return myAudioManager.isMusicActive();
    }

    public final void musicPlay(){
        try{
            if(isKeyEvent()){
                runMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
            }else{
                runMusicCommand(CMD_PLAY);
            }
            playState = "Media PLAY";
        }catch(Exception e){
            Toast.makeText(context,"Action not supported",Toast.LENGTH_SHORT).show();
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    private boolean isKeyEvent(){
        strMusicWidgetPkgName = prefs.getString("strMusicWidgetPkgName","");
        Log.e("strMusicWidgetPkgName",strMusicWidgetPkgName);
        if(strMusicWidgetPkgName.equals(C.PLAYER_POWER_AMP)){
            editor.putBoolean("cBoxMusicKeyMapType1",false);
            editor.putBoolean("cBoxMusicKeyMapType2",true);
            editor.commit();
            return true;
        }else if(strMusicWidgetPkgName.equals(C.PLAYER_GOOGLE) || strMusicWidgetPkgName.equals(C.PLAYER_STOCK)){
            editor.putBoolean("cBoxMusicKeyMapType1",true);
            editor.putBoolean("cBoxMusicKeyMapType2",false);
            editor.commit();
            return false;
        }
        return prefs.getBoolean("cBoxMusicKeyMapType1",true);
    }

    private void runMusicKeyEvent(int keyCode){
        //long eventtime = SystemClock.uptimeMillis();
        //Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON,null);
        //KeyEvent keyEvent = new KeyEvent(eventtime,eventtime,KeyEvent.ACTION_DOWN,keyCode,0);
        //i.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);
        //context.sendOrderedBroadcast(i, null);

        //keyEvent = KeyEvent.changeAction(keyEvent,KeyEvent.ACTION_UP);
        //i.putExtra(Intent.EXTRA_KEY_EVENT,keyEvent);
        //context.sendOrderedBroadcast(i, null);

        Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_DOWN,keyCode));
        context.sendOrderedBroadcast(i,null);

        i.putExtra(Intent.EXTRA_KEY_EVENT,new KeyEvent(KeyEvent.ACTION_UP,keyCode));
        context.sendOrderedBroadcast(i,null);
    }

    private void runMusicCommand(String cmd){
        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMD_COMMAND,cmd);
        context.sendBroadcast(i);
    }

    public final void musicPause(){
        try{
            if(isKeyEvent()){
                runMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);
            }else{
                runMusicCommand(CMD_PAUSE);
            }
            playState = "Media STOP";
            //Toast.makeText(context, "Media STOP", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            Toast.makeText(context,"Action not supported",Toast.LENGTH_SHORT).show();
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    public final void musicNext(){
        try{
            if(isKeyEvent()){
                runMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
            }else{
                runMusicCommand(CMD_NEXT);
            }
            playState = "Media NEXT";
        }catch(Exception e){
            Toast.makeText(context,"Action not supported",Toast.LENGTH_SHORT).show();
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }

    public final void musicBack(){
        try{
            if(isKeyEvent()){
                runMusicKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            }else{
                runMusicCommand(CMD_PREVIOUS);
            }
            playState = "Media PREVIOUS";
        }catch(Exception e){
            Toast.makeText(context,"Action not supported",Toast.LENGTH_SHORT).show();
            new MyCLocker(context).saveErrorLog(null,e);
        }
    }
}