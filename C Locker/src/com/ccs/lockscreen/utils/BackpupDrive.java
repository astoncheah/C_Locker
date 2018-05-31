package com.ccs.lockscreen.utils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen_pro.ServiceNotification;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

@SuppressWarnings("deprecation")
public class BackpupDrive extends GoogleDrive{
    public static final int BACKUP = 1;
    public static final int RESTORE = 2;
    private static final String FOLDER_NAME = "C Locker Backup";
    private static final String FOLDER_ID = "getDriveFolderId";
    private static final String SUB_FOLDER_ID = "getDriveSubFolderId";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private InputStream inStream;
    private OutputStream outStream;
    private ArrayList<File> uploadList = new ArrayList<File>();
    private ArrayList<DownloadFile> downloadList = new ArrayList<DownloadFile>();
    private int uploadListIndex, downloadListIndex, task;
    private DriveListener driveListener;
    private ResultCallback<DriveFolderResult> subFolderCreatedCallback = new ResultCallback<DriveFolderResult>(){
        @Override
        public void onResult(DriveFolderResult result){
            if(!result.getStatus().isSuccess()){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup","subFolderCreatedCallback>Error: "+result.getStatus());
                return;
            }
            editor.putString(SUB_FOLDER_ID,result.getDriveFolder().getDriveId().encodeToString());
            editor.commit();

            Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
        }
    };
    private ResultCallback<DriveFolderResult> folderCreatedCallback = new ResultCallback<DriveFolderResult>(){
        @Override
        public void onResult(DriveFolderResult result){
            if(!result.getStatus().isSuccess()){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup","folderCreatedCallback>Error: "+result.getStatus());
                return;
            }
            editor.putString(FOLDER_ID,result.getDriveFolder().getDriveId().encodeToString());
            editor.commit();

            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(getBackupDate()).build();
            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),result.getDriveFolder().getDriveId());
            folder.createFolder(getGoogleApiClient(),changeSet).setResultCallback(subFolderCreatedCallback);

            //Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
        }
    };
    private ResultCallback<MetadataBufferResult> getMainFolderList = new ResultCallback<MetadataBufferResult>(){
        @Override
        public void onResult(MetadataBufferResult result){
            try{
                if(!result.getStatus().isSuccess()){
                    driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup","getMainFolderList>Problem while retrieving files"+result.getStatus().getStatusMessage());
                    return;
                }
                Iterator<Metadata> it = result.getMetadataBuffer().iterator();
                while(it.hasNext()){
                    Metadata md = it.next();
                    if(md.getTitle().equals(FOLDER_NAME) && md.isFolder()){
                        //DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),md.getDriveId());
                        //folder.delete(getGoogleApiClient());

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(getBackupDate()).build();
                        DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),md.getDriveId());
                        folder.createFolder(getGoogleApiClient(),changeSet).setResultCallback(subFolderCreatedCallback);
                        result.getMetadataBuffer().release();
                        return;
                    }
                    if(!md.isFolder()){
                        //Log.e("deleted",md.getTitle());
                        //DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(),md.getDriveId());
                        //file.delete(getGoogleApiClient());
                    }
                }
                result.getMetadataBuffer().release();
                //create main folder if there is no folder
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(FOLDER_NAME).build();
                Drive.DriveApi.getRootFolder(getGoogleApiClient()).createFolder(getGoogleApiClient(),changeSet).setResultCallback(folderCreatedCallback);

                //Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
            }catch(Exception e){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup",e.toString());
                getActivity().saveErrorLogs("Error: getMainFolderList",e);
            }
        }
    };
    private ResultCallback<DriveFileResult> uploadFileCallback = new ResultCallback<DriveFileResult>(){
        @Override
        public void onResult(DriveFileResult result){
            if(!result.getStatus().isSuccess()){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup","uploadFileCallback>Error: "+result.getStatus());
                return;
            }
            if(uploadList.size()>0 && uploadListIndex<uploadList.size()){
                Drive.DriveApi.newDriveContents(getGoogleApiClient()).setResultCallback(driveContentsCallback);
                uploadListIndex++;
                driveListener.onDriveProgress(uploadList.size(),uploadListIndex,"uploading files..");
            }else{
                driveListener.onDriveResult(RESULT_OK,"Backup to Google Drive successfully completed","uploadFileCallback>Backup completed ("+uploadList.size()+" files)");
            }
        }
    };
    private ResultCallback<DriveContentsResult> driveContentsCallback = new ResultCallback<DriveContentsResult>(){
        @Override
        public void onResult(DriveContentsResult result){
            if(!result.getStatus().isSuccess()){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup","driveContentsCallback>Error: "+result.getStatus());
                return;
            }
            if(uploadList.size()==0){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup","driveContentsCallback>No backup file found: "+uploadList.size());
            }else if(uploadList.size()>0 && uploadListIndex<uploadList.size()){
                upload(result.getDriveContents(),uploadListIndex);
            }else{
                driveListener.onDriveResult(RESULT_OK,"Backup to Google Drive successfully completed","driveContentsCallback>Backup completed ("+uploadList.size()+" files)");
            }
        }
    };
    private ResultCallback<DriveContentsResult> getDownloadFile = new ResultCallback<DriveContentsResult>(){
        @Override
        public void onResult(DriveContentsResult result){
            if(!result.getStatus().isSuccess()){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadFile>Error: "+result.getStatus().getStatusMessage());
                return;
            }
            if(downloadList.size()==0){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadFile>No backup file found: "+downloadList.size());
            }else if(downloadList.size()>0 && downloadListIndex<downloadList.size()){
                download(result.getDriveContents(),getBackupDir());
            }else{
                driveListener.onDriveResult(RESULT_OK,"Restore from Google Drive successfully completed","getDownloadFile>Restore completed ("+downloadList.size()+" files)");
            }
        }
    };
    private ResultCallback<MetadataBufferResult> getDownloadFileList = new ResultCallback<MetadataBufferResult>(){
        @Override
        public void onResult(MetadataBufferResult result){
            try{
                if(!result.getStatus().isSuccess()){
                    driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadFileList>Error: "+result.getStatus().getStatusMessage());
                    return;
                }
                Iterator<Metadata> it = result.getMetadataBuffer().iterator();
                while(it.hasNext()){
                    Metadata md = it.next();
                    downloadList.add(new DownloadFile(md.getTitle(),md.getDriveId().encodeToString()));
                }
                if(downloadList.size()==0){
                    driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadFileList>No backup file found: "+downloadList.size());
                }else if(downloadList.size()>0 && downloadListIndex<downloadList.size()){
                    driveListener.onDriveProgress(downloadList.size(),0,"downloading files..");

                    DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(),downloadList.get(downloadListIndex).getId());
                    file.open(getGoogleApiClient(),DriveFile.MODE_READ_ONLY,null).setResultCallback(getDownloadFile);
                }else{
                    driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadFileList>Error unknown ("+downloadList.size()+" files)");
                }
                result.getMetadataBuffer().release();
            }catch(Exception e){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore",e.toString());
                getActivity().saveErrorLogs("Error: getDownloadFileList",e);
            }
        }
    };
    private ResultCallback<MetadataBufferResult> getDownloadFolderList = new ResultCallback<MetadataBufferResult>(){
        @Override
        public void onResult(MetadataBufferResult result){
            try{
                if(!result.getStatus().isSuccess()){
                    driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadFolderList>Error: "+result.getStatus().getStatusMessage());
                    return;
                }
                Iterator<Metadata> it = result.getMetadataBuffer().iterator();
                while(it.hasNext()){
                    Metadata md = it.next();
                    if(md.getTitle().equals(FOLDER_NAME) && md.isFolder()){
                        DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),md.getDriveId());
                        folder.listChildren(getGoogleApiClient()).setResultCallback(getDownloadSubFolderList);
                        result.getMetadataBuffer().release();
                        return;
                    }
                }
                result.getMetadataBuffer().release();
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","gatMainFolderId>No id found");
            }catch(Exception e){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore",e.toString());
                getActivity().saveErrorLogs("Error: gatMainFolderId",e);
            }
        }
    };
    private ResultCallback<MetadataBufferResult> getDownloadSubFolderList = new ResultCallback<MetadataBufferResult>(){
        @Override
        public void onResult(MetadataBufferResult result){
            try{
                if(!result.getStatus().isSuccess()){
                    driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore","getDownloadSubFolderList>Error: "+result.getStatus().getStatusMessage());
                    return;
                }
                final ArrayList<DownloadFile> fileList = new ArrayList<DownloadFile>();
                //final ArrayList<String> titleList = new ArrayList<String>();
                final Iterator<Metadata> it = result.getMetadataBuffer().iterator();
                while(it.hasNext()){
                    Metadata md = it.next();
                    if(!md.isTrashed()){
                        fileList.add(new DownloadFile(md.getTitle(),md.getDriveId().encodeToString()));
                        //String str = md.getTitle();
                        //titleList.add(str.substring(0,str.length()-3)+"..");
                    }
                }
                //String[] stockArr = new String[titleList.size()];
                //stockArr = titleList.toArray(stockArr);

                new MyAlertDialog(getActivity()).selectRestoreList(new DriveListListener(){
                    @Override
                    public void onListResult(int result,DriveId id){
                        if(result==BackpupDrive.RESULT_OK){
                            driveListener.onDriveProgress(downloadList.size(),0,"downloading files..");
                            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),id);
                            folder.listChildren(getGoogleApiClient()).setResultCallback(getDownloadFileList);
                        }else if(result==BackpupDrive.RESULT_DELETE){
                            driveListener.onDriveProgress(downloadList.size(),0,"downloading files..");
                            DriveFolder folder = Drive.DriveApi.getFolder(getGoogleApiClient(),id);
                            folder.delete(getGoogleApiClient());
                            prepareDownload();
                        }else{
                            driveListener.onDriveResult(RESULT_ERROR,"Cancelled restore","Cancelled restore");
                        }
                    }
                },fileList);
                result.getMetadataBuffer().release();
            }catch(Exception e){
                driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore",e.toString());
                getActivity().saveErrorLogs("Error: downloadFolderList",e);
            }
        }
    };

    public BackpupDrive(BaseActivity activity){
        super(activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = prefs.edit();
        editor.commit();
    }

    public void doTask(int task,DriveListener driveListener){
        this.task = task;
        this.driveListener = driveListener;
        connect();
    }

    @Override
    public void onConnected(Bundle connectionHint){
        super.onConnected(connectionHint);
        if(task==BACKUP){
            prepareUpload(getBackupDir());
        }else if(task==RESTORE){
            prepareDownload();
        }
    }

    //upload
    private void prepareUpload(String fromDir){
        try{
            if(getGoogleApiClient()!=null && getGoogleApiClient().isConnected()){
                final File fileFrom = new File(fromDir);
                if(!fileFrom.isDirectory()){
                    getActivity().showToastLong("Error: no files found");
                }
                uploadList.clear();
                uploadListIndex = 0;
                final String[] children = fileFrom.list();
                for(int i = 0; i<children.length; i++){
                    String child = children[i];
                    if(!child.contains(C.FILE_PREFS) &&            // dont copy prefs.xml
                            !child.contains(C.FILE_BACKUP_RECORD) &&    // dont copy BackupRecord.txt
                            !child.contains(C.FILE_MY_LOG) &&            // dont copy CLockerlog.txt
                            !child.contains(C.FILE_SYSTEM_LOG) &&        // dont copy CLockerlog.txt
                            !child.contains(ServiceNotification.IMAGE_FORMAT)){ // dont copy notification icon

                        File file = new File(fileFrom,child);
                        if(file.isFile()){
                            uploadList.add(file);
                        }
                    }
                }
                driveListener.onDriveProgress(uploadList.size(),0,"preparing backup files..");
                DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
                //DriveFolder folder = Drive.DriveApi.getAppFolder(getGoogleApiClient());
                folder.listChildren(getGoogleApiClient()).setResultCallback(getMainFolderList);
            }else{
                driveListener.onDriveResult(RESULT_ERROR,"Error: GoogleApiClient not connected","Error: GoogleApiClient not connected (prepareUpload)");
            }
        }catch(Exception e){
            driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup",e.toString());
            getActivity().saveErrorLogs("Error: prepareUpload",e);
        }
    }

    //download
    private void prepareDownload(){
        try{
            if(getGoogleApiClient()!=null && getGoogleApiClient().isConnected()){
                downloadList.clear();
                downloadListIndex = 0;
                DriveFolder folder = Drive.DriveApi.getRootFolder(getGoogleApiClient());
                folder.listChildren(getGoogleApiClient()).setResultCallback(getDownloadFolderList);
            }else{
                driveListener.onDriveResult(RESULT_ERROR,"Error: GoogleApiClient not connected","Error: GoogleApiClient not connected (prepareDownload)");
            }
        }catch(Exception e){
            driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore",e.toString());
            getActivity().saveErrorLogs("Error: prepareDownload",e);
        }
    }

    private void upload(DriveContents driveContents,int index){
        try{
            File file = uploadList.get(index);
            //Log.e("upload",file.getName());
            final String getDriveFolderId = prefs.getString(SUB_FOLDER_ID,null);
            if(getDriveFolderId==null){
                return;
            }
            outStream = driveContents.getOutputStream();
            inStream = new FileInputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len = inStream.read(buf))>0){
                outStream.write(buf,0,len);
            }
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(file.getName()).build();
            //Drive.DriveApi.getAppFolder(getGoogleApiClient())
            Drive.DriveApi.getFolder(getGoogleApiClient(),DriveId.decodeFromString(getDriveFolderId)).createFile(getGoogleApiClient(),changeSet,driveContents).setResultCallback(uploadFileCallback);

            inStream.close();
            outStream.close();
        }catch(Exception e){
            driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing backup",e.toString());
            getActivity().saveErrorLogs("Error: upload",e);
        }
    }

    private void download(DriveContents driveContents,String toDir){
        try{
            DownloadFile df = downloadList.get(downloadListIndex);
            final File fileTo = new File(toDir,df.getTitle());
            //Log.e("download",fileTo.getName());
            if(!fileTo.getParentFile().exists()){
                fileTo.getParentFile().mkdir();
            }

            outStream = new FileOutputStream(fileTo);
            inStream = driveContents.getInputStream();
            byte[] buf = new byte[1024];
            int len;
            while((len = inStream.read(buf))>0){
                outStream.write(buf,0,len);
            }

            downloadListIndex++;
            if(downloadListIndex<downloadList.size()){
                driveListener.onDriveProgress(downloadList.size(),downloadListIndex,"downloading files..");

                DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(),downloadList.get(downloadListIndex).getId());
                file.open(getGoogleApiClient(),DriveFile.MODE_READ_ONLY,null).setResultCallback(getDownloadFile);
            }else{
                driveListener.onDriveResult(RESULT_OK,"Restore from Google Drive successfully completed","download>Restore completed ("+downloadList.size()+" files)");
            }

            inStream.close();
            outStream.close();
        }catch(Exception e){
            driveListener.onDriveResult(RESULT_ERROR,"Problem while preparing restore",e.toString());
            getActivity().saveErrorLogs("Error: download",e);
        }
    }

    public class DownloadFile{
        String title;
        String id;

        public DownloadFile(String title,String id){
            super();
            this.title = title;
            this.id = id;
        }

        public String getTitle(){
            return title;
        }

        public DriveId getId(){
            return DriveId.decodeFromString(id);
        }
    }
}