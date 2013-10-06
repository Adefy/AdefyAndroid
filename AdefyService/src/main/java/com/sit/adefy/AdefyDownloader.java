package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.sit.adefy.objects.UserInformation;

import org.json.JSONException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

// This class replaces the service, offers functions to manually download ad
// packages
public class AdefyDownloader {

  // TODO: Make this private! It's only public for testing
  public String serverInterface = "https://192.168.0.102:3000/r/fetch/";
  private UserInformation uInfo;
  private Context ctx;
  private String APIKey;
  private String paths = "";

  public AdefyDownloader(Context _ctx, String _apikey) {
    this.ctx = _ctx;
    this.APIKey = _apikey;

    gatherUserInformation();
  }

  // TODO: Add error returns
  public void fetchAd(String zipName) {

    // TODO: Do something else
    if(!isNetworkAvaliable()) {
      return;
    }

    // TODO: Consider better handling
    try {
      downloadArchive(zipName,  establishConnection());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Initially, the device is profiled if not already done, and the
  // information is prepared to be sent to the Adefy servers
  private void gatherUserInformation() {

    // Get display size
    Display display = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);

    uInfo = new UserInformation();
    uInfo.uuid = ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    uInfo.username = AccountManager.get(ctx).getAccounts()[0].name;
    uInfo.screenWidth = size.x;
    uInfo.screenHeight = size.y;
  }

  // Utility
  private boolean isNetworkAvaliable() {
    ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    if(netInfo != null) {
      if(netInfo.isConnectedOrConnecting()) {
        return true;
      }
    }

    return false;
  }

  private HttpsURLConnection establishConnection() throws NoSuchAlgorithmException, KeyManagementException, IOException, JSONException {

    Log.v("Adefy", "Attempting to connect to server...");

    SSLContext sslctx = SSLContext.getInstance("SSL");
    sslctx.init(null, new X509TrustManager[]{new TrustingTrustManager()}, new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sslctx.getSocketFactory());

    URL url = new URL(serverInterface);

    HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
    con.setRequestMethod("POST");
    con.setDoInput(true);
    con.setDoOutput(true);
    con.setHostnameVerifier(new HostnameVerifier() {
      @Override
      public boolean verify(String hostname, SSLSession session) { return true; }
    });

    String content = "apikey=" + APIKey + "&uinfo=" + uInfo.toJSONString();

    con.setRequestProperty("Content-Length", Integer.toString(content.length()));
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

    OutputStream os = con.getOutputStream();
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

    writer.write(content);
    writer.flush();
    os.flush();
    writer.close();
    os.close();

    con.connect();

    Log.v("Adefy", "Connected to server, response length " + con.getContentLength());

    return con;
  }

  private void downloadArchive(String path, HttpsURLConnection con) throws IOException {

    Log.v("Adefy", "Requesting archive " + ctx.getCacheDir() + "/" + path);

    // Create file in our cache
    File tempArchive = File.createTempFile(path, ".zip", ctx.getCacheDir());

    // Download!
    InputStream input = con.getInputStream();
    FileOutputStream out = new FileOutputStream(tempArchive);

    byte data[] = new byte[1024];
    int count;

    // Write data
    while((count = input.read(data)) != -1) {
      out.write(data, 0, count);
    }

    out.flush();
    out.close();
    input.close();

    Log.v("Adefy", "Full fname: " + tempArchive.getAbsolutePath());
    tempArchive.renameTo(new File(ctx.getCacheDir() + "/" + path + ".zip"));
  }

  public void unzipArchive(String path, String dir) throws IOException {

    // Get streams
    FileInputStream fin = new FileInputStream(ctx.getCacheDir() + "/" + path);
    ZipInputStream zin = new ZipInputStream(fin);
    ZipEntry ze;

    // Operate on each entry
    while((ze = zin.getNextEntry()) != null) {

      Log.v("Adefy", "Decompressing downloaded ad resources " + ze.getName() + "->" + ctx.getCacheDir() + "/" + dir + "/" + ze.getName());

      if(ze.isDirectory()) {
        File dirCheck = new File(ctx.getCacheDir() + "/" + dir + "/" + ze.getName());

        // Create directory if necessary
        dirCheck.mkdirs(); // TODO: Fix
      } else {

        // Write file
        File fout_dirCreation = new File(ctx.getCacheDir() + "/" + dir);
        fout_dirCreation.mkdirs();

        FileOutputStream fout = new FileOutputStream(ctx.getCacheDir() + "/" + dir + "/" + ze.getName());

        byte data[] = new byte[1024];
        int count;

        while((count = zin.read(data, 0, 1024)) != -1) {
          fout.write(data, 0, count);
        }

        zin.closeEntry();
        fout.close();
      }
    }
  }

  private class TrustingTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
  }
}
