package com.ccs.lockscreen.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;

import com.ccs.lockscreen.R;
import com.ccs.lockscreen.myclocker.C;
import com.ccs.lockscreen.myclocker.MyCLocker;
import com.ccs.lockscreen.security.GoogleApiTokenTask.OnGoogleApiTokenListener;
import com.ccs.lockscreen.utils.LocationNameHandler;
import com.ccs.lockscreen.utils.LocationNameHandler.LocationNameCallBack;
import com.ccs.lockscreen.utils.MyLocationManager;
import com.ccs.lockscreen.utils.MyLocationManager.LocationCallBack;
import com.ccs.lockscreen_pro.SettingsSecuritySelfie;
import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GmailSender implements OnGoogleApiTokenListener, LocationCallBack,
        LocationNameCallBack{
    public Context context;
    public MyCLocker mLocker;
    private String email, location, coordinate;

    public GmailSender(Context context){
        this.context = context;
        mLocker = new MyCLocker(context);
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       Email address of the receiver.
     * @param from     Email address of the sender, the mailbox account.
     * @param subject  Subject of the email.
     * @param bodyText Body text of the email.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */
    @SuppressWarnings("unused")
    private MimeMessage createEmail(String to,String from,String subject,String bodyText) throws
            MessagingException{
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props,null);

        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);

        email.setFrom(fAddress);
        email.addRecipient(javax.mail.Message.RecipientType.TO,tAddress);
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    //
    public void sendEmail(){
        //send email to the owner
        final SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
        final boolean cBoxEmailSelfie = prefs.getBoolean("cBoxEmailSelfie",false);
        email = prefs.getString("GetGoogleApiTokenTaskEmail",null);
        if(!cBoxEmailSelfie){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender cBoxEmailSelfie not enabled");
            return;
        }
        if(email==null){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender no email found");
            return;
        }
        if(C.isInternetConnected(context)){
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender getting location");
            MyLocationManager mLocation = new MyLocationManager(context,this);
            mLocation.runLocationChecker();
        }else{
            mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender saveEmailPending, no internet connection");
            saveEmailPending(context,true);
        }
    }

    private void saveEmailPending(Context context,boolean pandding){
        final SharedPreferences.Editor editor = context.getSharedPreferences(C.PREFS_NAME,0).edit();
        editor.putBoolean("isNewSecuritySelfieSendEmailPending",pandding);
        editor.commit();
    }

    @Override
    public void onGoogleApiTokenTaskResult(final String token,final String email){
        new Thread(){
            public void run(){
                try{
                    SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME,Context.MODE_PRIVATE);
                    String latestThiefSelfieFile = prefs.getString("latestThiefSelfieFile","");
                    String latestThiefSelfieTime = prefs.getString("latestThiefSelfieTime","");

                    String appName = "C Locker Free";
                    if(context.getPackageName().equals(C.PKG_NAME_PRO)){
                        appName = "C Locker Pro";
                    }
                    MimeMessage mM = createEmailWithAttachment(email,email,"C Locker Security Selfie: ",context.getString(R.string.auto_security_selfie_desc1)+latestThiefSelfieTime+
                            "\nLocation:  "+location+
                            "\nGPS Coordinate:  "+coordinate+
                            "\n\nSent from "+appName+
                            "\nDEVICE BRAND {"+Build.BRAND+"}"+
                            "\nMANUFACTURER {"+Build.MANUFACTURER+"}"+
                            "\nMODEL {"+Build.MODEL+"}",AutoSelfie.getDir(),latestThiefSelfieFile);
                    if(mM==null){
                        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender email failed: MimeMessage==null");
                        return;
                    }
                    sendMessage(token,mM);
                    saveEmailPending(context,false);
                    mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender email sent");
                }catch(Exception e){
                    mLocker.saveErrorLog("Exception",e);
                }
            }
        }.start();
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       Email address of the receiver.
     * @param from     Email address of the sender, the mailbox account.
     * @param subject  Subject of the email.
     * @param bodyText Body text of the email.
     * @param fileDir  Path to the directory containing attachment.
     * @param filename Name of file to be attached.
     * @return MimeMessage to be used to send email.
     * @throws MessagingException
     */
    private MimeMessage createEmailWithAttachment(String to,String from,String subject,
            String bodyText,String fileDir,String filename) throws MessagingException, IOException{
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props,null);

        MimeMessage email = new MimeMessage(session);
        InternetAddress tAddress = new InternetAddress(to);
        InternetAddress fAddress = new InternetAddress(from);

        email.setFrom(fAddress);
        email.addRecipient(javax.mail.Message.RecipientType.TO,tAddress);
        email.setSubject(subject);

        //content
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(bodyText,"text/plain");
        mimeBodyPart.setHeader("Content-Type","text/plain; charset=\"UTF-8\"");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        //attachment
        String name = fileDir+File.separator+filename;
        File file = new File(name);

        mimeBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(file);
        mimeBodyPart.setDataHandler(new DataHandler(source));
        mimeBodyPart.setFileName(filename);
        String mimeType = new MimetypesFileTypeMap().getContentType(file);

        //import java.nio.file.FileSystems;
        //import java.nio.file.Files;
        //contentType = Files.probeContentType(FileSystems.getDefault().getPath(fileDir, filename));
        //Log.e("to",""+to);
        //Log.e("file",""+file.getAbsolutePath());
        //Log.e("mimeType",""+mimeType);
        //"image/pjpeg","image/jpeg"

        if(mimeType==null){
            return null;
        }
        mimeBodyPart.setHeader("Content-Type",mimeType+"; name=\""+filename+"\"");
        mimeBodyPart.setHeader("Content-Transfer-Encoding","base64");
        multipart.addBodyPart(mimeBodyPart);

        email.setContent(multipart);
        return email;
    }

    /**
     * Send an email from the user's mailbox to its recipient.
     *
     * @param service Authorized Gmail API instance.
     * @param userId  User's email address. The special value "me"
     *                can be used to indicate the authenticated user.
     * @param email   Email to be sent.
     * @throws MessagingException
     * @throws IOException
     */
    private void sendMessage(String token,MimeMessage email) throws MessagingException, IOException{
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        GoogleTokenResponse response = new GoogleTokenResponse();
        response.setAccessToken(token);
        GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

        Gmail gm = new Gmail.Builder(httpTransport,jsonFactory,credential).setApplicationName("C Locker").build();

        Message message = createMessageWithEmail(email);
        message = gm.users().messages().send("me",message).execute();
    }

    /**
     * Create a Message from an email
     *
     * @param email Email to be set to raw of message
     * @return Message containing base64url encoded email.
     * @throws IOException
     * @throws MessagingException
     */
    private Message createMessageWithEmail(MimeMessage email) throws MessagingException,
            IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    @Override
    public void updateLocation(Location loc){
    }

    @Override
    public void updateLocationFinal(Location loc){
        try{
            LatLng latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
            coordinate = "http://maps.google.com/maps?q="+latLng.latitude+","+latLng.longitude;
            new LocationNameHandler(context,this).execute(latLng);
            return;
        }catch(Exception e){
            mLocker.saveErrorLog(null,e);
        }
        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender email sending 1");
        new GoogleApiTokenTask(context,email,SettingsSecuritySelfie.SCOPE_GMAIL_FULL,this).execute();
    }

    @Override
    public void updateLocationGpsError(){
    }//skip gps location and use network/wifi

    @Override
    public void updateLocationError(String msg){
        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender>updateLocationError: "+msg);
        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender email sending 2");
        new GoogleApiTokenTask(context,email,SettingsSecuritySelfie.SCOPE_GMAIL_FULL,this).execute();
    }

    //get location name
    @Override
    public void onGetLocationNameFinished(Boolean resultOK,String streetName,String cityName,
            String countryName){
        mLocker.writeToFile(C.FILE_BACKUP_RECORD,"GmailSender email sending 3");
        if(streetName!=null && cityName!=null && countryName!=null){
            location = streetName+", "+cityName+", "+countryName;
        }else if(streetName!=null && cityName!=null){
            location = streetName+", "+cityName;
        }else if(streetName!=null){
            location = streetName;
        }else if(cityName!=null){
            location = cityName;
        }
        new GoogleApiTokenTask(context,email,SettingsSecuritySelfie.SCOPE_GMAIL_FULL,this).execute();
    }
}