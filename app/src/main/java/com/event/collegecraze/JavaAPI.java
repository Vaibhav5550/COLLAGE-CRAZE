package com.event.collegecraze;

import android.content.Context;
import android.os.AsyncTask;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaAPI extends AsyncTask<String, String, String>
{
    Context context;
    Properties pro;
    Session session;
    MimeMessage mMSG;
    String ToEMail, ToSubject, Tomessage;

    public JavaAPI(Context forgotPasswordActivity, String toEMail, String toSubject, String tomessage) {
        this.context = forgotPasswordActivity;
        this.ToEMail = toEMail;
        this.ToSubject = toSubject;
        this.Tomessage = tomessage;
    }

    @Override
    protected String doInBackground(String... strings)
    {
        pro = new Properties();
        pro.put("mail.smtp.host", "smtp.gmail.com");
        pro.put("mail.smtp.socketFactory.port", "465");
        pro.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        pro.put("mail.smtp.auth", "true");
        pro.put("mail.smtp.port", "465");

        //Creating a new session
        pro = new Properties();
        pro.put(Utils.Smtphostkey, Utils.SmatphostValue);
        pro.put(Utils.Sslsocketportkey, Utils.SslsocketportValue);
        pro.put(Utils.Sslclassfactorykey, Utils.SslclassfactoryValue);
        pro.put(Utils.Sslauthnticationkey, Utils.SslauthnticationValue);
        pro.put(Utils.Sslsmtpportkey, Utils.SslsmtpportValue);
        session = Session.getDefaultInstance(pro, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Utils.AEMAIL, Utils.APASSWORD);
            }
        });
        try {
            mMSG = new MimeMessage(session);
            mMSG.setFrom(new InternetAddress(Utils.AEMAIL));
            mMSG.addRecipients(Message.RecipientType.TO, String.valueOf(new InternetAddress(ToEMail)));
            mMSG.setSubject(ToSubject);
            mMSG.setText(Tomessage);
            Transport.send(mMSG);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
