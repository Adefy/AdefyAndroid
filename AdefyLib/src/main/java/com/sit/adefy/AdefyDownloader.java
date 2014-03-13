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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
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

/*
  This class is responsible for gathering user data, and sending it to the Adefy servers
  to request an ad. Ads are packaged in zip files, and include a manifest, the AdefyJS library,
  necessary textures, and the scene itself.
 */
public class AdefyDownloader {

  private String serverInterface;
  private Context ctx;
  private String APIKey;
  private String downloadPath;
  private String adType = null;
  private boolean landscape = false;

  // Gathers initial device information, packages it in a string ready to send to the server.
  public AdefyDownloader(Context _ctx, String _apikey) {
    this(_ctx, _apikey, "https://app.adefy.com/api/v1/serve", null);
  }

  public AdefyDownloader(Context _ctx, String _apikey, String _adType) {
    this(_ctx, _apikey, "https://app.adefy.com/api/v1/serve", _adType);
  }

  // Constructor that breaks out the server URL.
  // For staging, use https://app.adefy.com/api/v1/serve
  // For testing, use http://192.168.0.16:8080/api/v1/serve (with actual local IP)
  public AdefyDownloader(Context _ctx, String _apikey, String _serverURL, String _adType) {
    this.ctx = _ctx;
    this.APIKey = _apikey;
    this.serverInterface = _serverURL;
    this.adType = _adType;

    gatherUserInformation();
  }

  public boolean fetchAd(String folder) {
    return fetchAd(folder, -1);
  }

  public void setLandscape(boolean landscape) {
    this.landscape = landscape;
  }

  public boolean fetchAd(String folder, int duration) {
    if(!isNetworkAvailable()) {

      Log.e("Adefy", "Can't fetch ad, network not avaliable!");
      return false;
    }

    genDownloadPath();

    try {

      downloadArchive(establishConnection());
      if(folder != null) { unzipArchive(getDownloadName(), folder); }
      return true;

    } catch (Exception e) {

      e.printStackTrace();
      return false;
    }
  }

  public boolean adExists(String folder) {
    return new File(folder).exists();
  }

  private void genDownloadPath() {
    StringBuilder sb = new StringBuilder();
    String charSpace = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    for(int i = 0; i <  16; i++) {
      sb.append(charSpace.charAt((int)Math.floor(Math.random() * charSpace.length())));
    }

    downloadPath = sb.toString();
  }

  public String getDownloadName() {
    return downloadPath + ".ttx";
  }

  // Device UUID, the username, and the screen size is harvested.
  private String gatherUserInformation() {

    // Get display size
    Display display = ((WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);

    String userInfo = "";

    if(APIKey != null) {
      userInfo = "/" + APIKey;
    }

    userInfo += "?uuid=" + ((TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

    if(landscape) {
      userInfo += "&width=" + size.y;
      userInfo += "&height=" + size.x;
    } else {
      userInfo += "&width=" + size.x;
      userInfo += "&height=" + size.y;
    }

    if(adType != null) {
      userInfo += "&template=" + adType;
    }

    try {
      AccountManager accountManager = AccountManager.get(ctx);

      if(accountManager != null) {
        userInfo += "&username=" + accountManager.getAccounts()[0].name;
      }
    } catch (NullPointerException e) {
      // We ignore the error, only attaching the username if we can
    }

    return userInfo;
  }

  // Utility
  private boolean isNetworkAvailable() {
    ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();

    if(netInfo != null) {
      if(netInfo.isConnectedOrConnecting()) {
        return true;
      }
    }

    return false;
  }

  private HttpURLConnection establishConnection() throws IOException, KeyManagementException, NoSuchAlgorithmException {

    URL url = new URL(serverInterface + gatherUserInformation() + "&type=organic");

    Log.d("Adefy", "Fetching ad: " + serverInterface + gatherUserInformation() + "&type=organic");

    if(url.getProtocol().equals("https")) {
      SSLContext sslctx = SSLContext.getInstance("SSL");
      sslctx.init(null, new X509TrustManager[]{new TrustingTrustManager()}, new SecureRandom());

      HttpsURLConnection.setDefaultSSLSocketFactory(sslctx.getSocketFactory());
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

      con.setHostnameVerifier(new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) { return true; }
      });

      return con;
    } else {
      return (HttpURLConnection) url.openConnection();
    }
  }

  private void downloadArchive(HttpURLConnection con) throws Exception {

    // Create file in our cache
    File tempArchive = File.createTempFile(downloadPath, ".ttx", ctx.getCacheDir());

    // Download!
    InputStream input = con.getInputStream();
    FileOutputStream out = new FileOutputStream(tempArchive);

    byte data[] = new byte[1024];
    int count;

    // Write data
    while((count = input.read(data, 0, 1024)) != -1) {
      out.write(data, 0, count);
    }

    out.flush();
    out.close();
    input.close();

    if(!tempArchive.renameTo(new File(ctx.getCacheDir() + "/" + downloadPath + ".ttx"))) {
      throw new Exception("Failed to rename temp archive.");
    }
  }

  public void unzipArchive(String path, String dir) throws Exception {

    // Get streams
    FileInputStream fin = new FileInputStream(ctx.getCacheDir() + "/" + path);
    ZipInputStream zin = new ZipInputStream(fin);
    ZipEntry ze;

    // Operate on each entry
    while((ze = zin.getNextEntry()) != null) {

      // Create directory
      new File(ctx.getCacheDir() + "/" + dir).mkdirs();

      // Write file
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

  private class TrustingTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
  }
}
