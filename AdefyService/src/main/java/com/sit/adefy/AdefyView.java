package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.content.Context;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.js.JSAnimationInterface;
import com.sit.adefy.js.JSEngineInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class AdefyView extends GLSurfaceView {

  private AdefyRenderer renderer;
  private WebView web;

  private String adName = null;
  private String apiKey = null;

  // Purely for debugging!
  // TODO: Remove
  private String adId = null;

  private String adSourcePath;
  private String adefySourcePath;
  private JSONArray textureArray;

  private StringBuilder adRuntime = new StringBuilder();

  // Interfaces!
  private String ifaceDef =
      "javascript:(function(){" +
        "window.AdefyGLI = {};" +
          "window.AdefyGLI.Engine = function(){ return window.__iface_engine; };" +
          "window.AdefyGLI.Actors = function(){ return window.__iface_actors; };" +
          "window.AdefyGLI.Animations = function(){ return window.__iface_animations; };" +
      "})()";

  // Sets up a renderer, checks for a supplied ad name. If none is found, requests an ad using
  // AdefyDownloader, and then proceeds with that.
  //
  // adId is purely for debugging!
  // TODO: Remove adId
  public AdefyView(String apiKey, String adName, String adId, Context context) {
    super(context);

    if(apiKey != null) { this.apiKey = apiKey; }
    if(adName != null) { this.adName = adName; }

    // Purely for debugging!
    // TODO: Remove
    if(adId != null) { this.adId = adId; }

    init(context, null);
  }
  public AdefyView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  // Used to run postInit() on the UI Thread from the AsyncTask responsible for downloading
  private final Handler uiHandler = new Handler();

  // Called from constructor
  private void init(Context context, AttributeSet attrs) {

    // Set up renderer and GL ES 2.0
    renderer = new AdefyRenderer();
    setEGLContextClientVersion(2);
    setRenderer(renderer);

    if(attrs != null && context.getTheme() != null) {

      // Get folder name, and prepare things
      TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AdefyView, 0, 0);
      if(a != null) {

        try {
          adName = a.getString(R.styleable.AdefyView_localName);
          apiKey = a.getString(R.styleable.AdefyView_apiKey);
        } finally {
          a.recycle();
        }
      }
    }

    if(adName == null) {
      Log.v("AdefyView", "No ad name provded, manually fetching one...");

      // Generate a random name for the fetched ad
      adName = genRandomString(16);

      // Spawn an AdefyDownloader, get to work.
      new AsyncTask<Void, Void, Void>() {

        @Override
        protected Void doInBackground(Void... voids) {
          AdefyDownloader downloader = new AdefyDownloader(getContext(), apiKey);

          boolean success;

          if(adId != null) { success = downloader.fetchAd(adName, adId); }
          else { success = downloader.fetchAd(adName); }

          if(!success) {
            Log.e("AdefyView", "Ad fetch failed!");
          } else {

            // Run finalInit() on ui thread
            uiHandler.post(new Runnable() {
              @Override
              public void run() {
                finalInit();
              }
            });
          }

          return null;
        }
      }.execute();

    } else {

      finalInit();
    }
  }

  private String genRandomString(int length) {
    StringBuilder sb = new StringBuilder();
    String charSpace = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    for(int i = 0; i < length; i++) {
      sb.append(charSpace.charAt((int)Math.floor(Math.random() * charSpace.length())));
    }

    return sb.toString();
  }

  // Called once an ad is locally available
  private void finalInit() {

    parseDelivered();

    adRuntime.append("javascript:(function(){");
    getJS(adRuntime, adefySourcePath);  // AdefyJS
    getJS(adRuntime, adSourcePath);     // Ad code
    adRuntime.append("})()");

    setupWebView();
  }

  // Parces and inspects the package.json, grabs necessary paths
  private void parseDelivered() {

    try {
      File manifest = new File(getContext().getCacheDir() + "/" + adName + "/package.json");
      BufferedReader br = new BufferedReader(new FileReader(manifest.toString()));
      StringBuilder sb = new StringBuilder((int)manifest.length());

      char[] buffer = new char[1024];
      int count;

      while((count = br.read(buffer, 0, 1024)) >= 0) {
        sb.append(new String(buffer, 0, count));
      }

      JSONObject manifestObj = new JSONObject(sb.toString());
      adSourcePath = manifestObj.getString("ad");
      adefySourcePath = manifestObj.getString("lib");
      textureArray = manifestObj.getJSONArray("textures");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Reads the designated JS file into a stringbuilder (appends!)
  private void getJS(StringBuilder sb, String name) {

    try {

      File jsFile = new File(getContext().getCacheDir() + "/" + adName + "/" + name);
      BufferedReader br = new BufferedReader(new FileReader(jsFile.toString()));

      char[] buffer = new char[1024];
      int count;

      while((count = br.read(buffer, 0, 1024)) >= 0) {
        sb.append(new String(buffer, 0, count));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Creates our webview, attaches interfaces and onLoad trigger
  private void setupWebView() {

    // Where the magic happens
    web = new WebView(getContext());
    web.getSettings().setJavaScriptEnabled(true);

    // Set up console logging
    web.setWebChromeClient(new WebChromeClient() {

      public boolean onConsoleMessage(ConsoleMessage cm) {

        Log.d("adefy", cm.message() + " -- line " + cm.lineNumber() + " of " + cm.sourceId());
        return true;
      }
    });

    // onLoad() listener interface
    web.addJavascriptInterface(new WebViewLoadNotify(), "__iface_load");

    // AJS intefaces
    web.addJavascriptInterface(new JSEngineInterface(), "__iface_engine");
    web.addJavascriptInterface(new JSActorInterface(), "__iface_actors");
    web.addJavascriptInterface(new JSAnimationInterface(), "__iface_animations");

    // Load interface access
    String loadJS =
        "javascript:(function(){" +
          "window.onload = function(){" +
            "__iface_load.onLoad();" +
          "};" +
        "})()";

    // Inject!
    web.loadData("", "text/html", null);
    web.loadUrl(loadJS);
  }

  final Handler onLoadHandler = new Handler();

  // Tiny class, passed into the webview to listen for the loadevent. Once fired, we inject our
  // runtime
  private final class WebViewLoadNotify {

    @JavascriptInterface
    public void onLoad() {

      onLoadHandler.post(new Runnable() {
        @Override
        public void run() {
          executeOnLoad();
        }
      });
    }
  }

  // Called by WebViewLoadNotify when the webview onLoad event fires. Injects AJS and the ad code
  // and gets the ball rolling, so to speak
  private void executeOnLoad() {

    // TODO: We are executing arbitrary JS, preform some security checks!

    // Inject our global object, that provides access to the interfaces
    web.loadUrl(ifaceDef);

    // Now our runtime
    web.loadUrl(adRuntime.toString());
  }
}
