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

/*
  This class is responsible for gathering user data, and sending it to the Adefy servers
  to request an ad. Ads are packaged in zip files, and include a manifest, the AdefyJS library,
  necessary textures, and the scene itself.
 */
public class AdefyDownloader {

  // TODO: Make this private! It's only public for testing
  public String serverInterface = "https://cloud.adefy.eu/api/r";
  private UserInformation uInfo;
  private Context ctx;
  private String APIKey;
  private String downloadPath;

  // TODO: Remove this!
  private String adId = null;

  // Gathers initial device information, packages it in a string ready to send to the server.
  public AdefyDownloader(Context _ctx, String _apikey) {
    this.ctx = _ctx;
    this.APIKey = _apikey;

    gatherUserInformation();
  }

  // Contacts the server, sends userinfo, and expects to receive an ad. Unzips if a
  // folder is provided. Otherwise, unzip
  //
  // Returns success
  public boolean fetchAd(String folder) { return fetchAd(folder, null); }
  public boolean fetchAd(String folder, String id) {
    if(!isNetworkAvaliable()) {

      Log.e("Adefy", "Can't fetch ad, network not avaliable!");
      return false;
    }

    genDownloadPath();

    if(id != null) { adId = id; }

    try {

      downloadArchive(establishConnection());
      if(folder != null) { unzipArchive(getDownloadName(), folder); }
      return true;

    } catch (Exception e) {

      e.printStackTrace();
      return false;
    }
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

  // Assuming we've downloaded an archive, and the name is in downloadPath
  public void cleanArchive() {

    if(downloadPath.length() == 16) {
      File archive = new File(downloadPath + ".ttx");
      if(archive.exists()) {
        archive.delete();
      }
    }
  }

  // Device UUID, the username, and the screen size is harvested.
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

    // TODO: Remove
    if(adId != null) {
      content += "&id=" + adId;
    }

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

    return con;
  }

  private void downloadArchive(HttpsURLConnection con) throws IOException {

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

    tempArchive.renameTo(new File(ctx.getCacheDir() + "/" + downloadPath + ".ttx"));
  }

  public void unzipArchive(String path, String dir) throws IOException {

    // Get streams
    FileInputStream fin = new FileInputStream(ctx.getCacheDir() + "/" + path);
    ZipInputStream zin = new ZipInputStream(fin);
    ZipEntry ze;

    // Operate on each entry
    while((ze = zin.getNextEntry()) != null) {

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

  private class TrustingTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
  }
}
