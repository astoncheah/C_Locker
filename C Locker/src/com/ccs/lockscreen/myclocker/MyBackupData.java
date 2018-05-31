package com.ccs.lockscreen.myclocker;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

public class MyBackupData extends BackupAgentHelper{
    private static final String MY_PREFS_BACKUP_KEY = "MyBackupDataKey";

    @Override
    public void onCreate(){
        super.onCreate();
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this,C.PREFS_NAME);
        addHelper(MY_PREFS_BACKUP_KEY,helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState,BackupDataOutput data,
            ParcelFileDescriptor newState) throws IOException{
        super.onBackup(oldState,data,newState);
    }

    @Override
    public void onRestore(BackupDataInput data,int appVersionCode,
            ParcelFileDescriptor newState) throws IOException{
        super.onRestore(data,appVersionCode,newState);
    }
    /*private void copyImage(String fromDir,String toDir,String fileName){
        try {
			fileFrom = new File(fromDir,fileName);
			if(!fileFrom.getParentFile().exists()){
				writeToFile(backupDir,ServiceMain.FILE_BACKUP_RECORD,"copyImage: No image found");
			}
			
	        inStream = new FileInputStream(fileFrom);	        
	        bmIcon = BitmapFactory.decodeStream(inStream);
	        inStream.close();
	        
	        fileTo = new File(toDir,fileName);
	        if (!fileTo.getParentFile().exists())fileTo.getParentFile().mkdir();
	        
	        outStream = new FileOutputStream(fileTo);
	        bmIcon.compress(Bitmap.CompressFormat.PNG, 100, outStream);
	        outStream.flush();
	        outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}*/
}
