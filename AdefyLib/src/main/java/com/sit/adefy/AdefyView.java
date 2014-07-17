package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Handler;
import android.test.ActivityInstrumentationTestCase2;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.js.JSAnimationInterface;
import com.sit.adefy.js.JSEngineInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.GregorianCalendar;

public class AdefyView extends GLSurfaceView {

  private AdefyRenderer renderer;
  private AdefyScene sceneActivity = null;
  private WebView web = null;

  private String adName = null;
  private String apiKey = null;
  private String serverInterface = null;

  private String adSourcePath;
  private String adefySourcePath;

  private String adIcon = null;
  private String impressionURL = null;
  private String clickURL = null;
  private String pushTitle;
  private String pushDesc;
  private String pushURL;
  private String adType = null;

  private StringBuilder adRuntime;
  private JSONArray textureArray = null;

  private Boolean clicked = false;
  private float remindMeButtonX = -1;
  private float remindMeButtonY = -1;
  private float remindMeButtonW = 0;
  private float remindMeButtonH = 0;

  private Boolean delayInit = false;

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
  public AdefyView(String apiKey, String adName, String serverInterface, Context context) {
    super(context);

    if(apiKey != null) { this.apiKey = apiKey; }
    if(adName != null) { this.adName = adName; }
    if(serverInterface != null) { this.serverInterface = serverInterface; }

    init(context, null);
  }

  // Constructor that allows us to specify a custom ad type
  public AdefyView(String apiKey, String adName, String serverInterface, Context context, String adType) {
    super(context);

    if(adType != null) { this.adType = adType; }
    if(apiKey != null) { this.apiKey = apiKey; }
    if(adName != null) { this.adName = adName; }
    if(serverInterface != null) { this.serverInterface = serverInterface; }

    init(context, null);
  }

  // As above, except we can also control if init runs immediately
  public AdefyView(String apiKey, String adName, String serverInterface, Context context, String adType, Boolean delayInit) {
    super(context);

    if(adType != null) { this.adType = adType; }
    if(apiKey != null) { this.apiKey = apiKey; }
    if(adName != null) { this.adName = adName; }
    if(delayInit != null) { this.delayInit = delayInit; }
    if(serverInterface != null) { this.serverInterface = serverInterface; }

    init(context, null);
  }

  public AdefyView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public void setRemindMeButton(float x, float y, float w, float h) {
    remindMeButtonX = x;
    remindMeButtonY = y;
    remindMeButtonW = w;
    remindMeButtonH = h;
  }

  // Used to run postInit() on the UI Thread from the AsyncTask responsible for downloading
  private final Handler uiHandler = new Handler();

  public void setSceneActivity(AdefyScene a) {
    sceneActivity = a;
  }

  // Called from constructor
  private void init(Context context, AttributeSet attrs) {

    // Clear any animations
    AdefyRenderer.animationTimer.cancel();
    AdefyRenderer.animationTimer.purge();

    // Set up renderer and GL ES 2.0
    renderer = new AdefyRenderer();
    setEGLContextClientVersion(2);
    setPreserveEGLContextOnPause(true);
    setRenderer(renderer);

    renderer.getPsyx().renderer = renderer;

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
      Log.v("AdefyView", "No ad name provided, manually fetching one...");

      // Generate a random name for the fetched ad
      adName = genRandomString(16);

      // Spawn an AdefyDownloader, get to work.
      new AsyncTask<Void, Void, Void>() {

        @Override
        protected Void doInBackground(Void... voids) {
          AdefyDownloader downloader = new AdefyDownloader(getContext(), apiKey, serverInterface, adType);

          if(!downloader.fetchAd(adName)) {
            Log.e("AdefyView", "Ad fetch failed!");

            if(sceneActivity != null) {
              sceneActivity.finish();
            }
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
    if(delayInit) { return; }

    boolean result = parseDelivered();

    if(result) {

      adRuntime = new StringBuilder();
      adRuntime.append("javascript:(function(){");
      getJS(adRuntime, adefySourcePath);  // AdefyJS
      getJS(adRuntime, adSourcePath);     // Ad code
      adRuntime.append("})()");

      setupWebView();

    } else {
      Toast.makeText(getContext(), "Ad failed to load", Toast.LENGTH_LONG).show();

      if (AdefyScene.getMe() != null) {
        AdefyScene.getMe().finish();
      }
    }
  }

  // Parses and inspects the package.json, grabs necessary paths
  private boolean parseDelivered() {

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
      impressionURL = manifestObj.getString("impression");
      clickURL = manifestObj.getString("click");

      pushTitle = manifestObj.getString("pushTitle");
      pushDesc = manifestObj.getString("pushDesc");
      pushURL = manifestObj.getString("pushURL");

      adIcon = manifestObj.getString("pushIcon");
      textureArray = manifestObj.getJSONArray("textures");

      if(impressionURL == null) {
        return false;
      }

      // Send texture information to renderer
      renderer.setTextureInfo(textureArray, getContext().getCacheDir() + "/" + adName);
      return true;

    } catch (Exception e) {
      e.printStackTrace();
      return false;
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

  public void continueInit() {
    if(delayInit) {
      delayInit = false;
      finalInit();
    }
  }

  // Creates our webview, attaches interfaces and onLoad trigger
  private void setupWebView() {

    // Where the magic happens
    web = new WebView(getContext());
    web.getSettings().setJavaScriptEnabled(true);
    web.clearCache(true);
    web.clearHistory();

    // Set up console logging
    web.setWebChromeClient(new WebChromeClient() {

      public boolean onConsoleMessage(ConsoleMessage cm) {

        Log.d("adefy", "WebView: " + cm.message() + " -- line " + cm.lineNumber() + " of " + cm.sourceId());
        return true;
      }
    });

    // onLoad() listener interface
    web.addJavascriptInterface(new WebViewLoadNotify(), "__iface_load");

    // AJS intefaces
    web.addJavascriptInterface(new JSEngineInterface(this), "__iface_engine");
    web.addJavascriptInterface(new JSActorInterface(renderer), "__iface_actors");
    web.addJavascriptInterface(new JSAnimationInterface(renderer), "__iface_animations");

    // Load interface access
    String loadJS =
        "javascript:(function(){" +
          "window.onload = function(){" +
            "__iface_load.onLoad();" +
          "};" +
        "})()";

    // Inject!
    web.loadDataWithBaseURL(null, "", null, "utf-8", null);
    web.loadUrl(loadJS);

    // Register impression!
    registerImpression();
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

  // Call the impression URL we were provided
  private void registerImpression() {
    if(impressionURL == null) { return; }
    if(impressionURL.length() == 0) { return; }

    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void...arg0) {

        try {
          Log.d("Adefy", "Registering impression: " + impressionURL);

          URL url = new URL(impressionURL);
          HttpURLConnection con = (HttpURLConnection) url.openConnection();

          InputStream input = con.getInputStream();
          byte data[] = new byte[1024];
          while(input.read(data, 0, 1024) != -1) {}
          input.close();

        } catch (Exception e) {
          e.printStackTrace();
          Log.e("Adefy", "Failed to register impression! " + impressionURL);
        }

        return null;
      }
    }.execute();
  }

  // Call the click URL we were provided
  private void registerClick() {
    if(clicked) { return; }
    if(clickURL == null) { return; }
    if(clickURL.length() == 0) { return; }

    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void...arg0) {

        try {
          clicked = true;

          // Register click with server
          URL url = new URL(clickURL);
          HttpURLConnection con = (HttpURLConnection) url.openConnection();

          InputStream input = con.getInputStream();
          byte data[] = new byte[1024];
          while(input.read(data, 0, 1024) != -1) {}
          input.close();

        } catch (Exception e) {
          e.printStackTrace();
          Log.e("Adefy", "Failed to register click! " + clickURL);
        }

        // Schedule notification
        try {
          Long time = new GregorianCalendar().getTimeInMillis() + (5 * 60 * 1000);
          Intent intentAlarm = new Intent(getContext(), AdefyReminder.class);

          if(adIcon != null) {
            for(int i = 0; i < textureArray.length(); i++) {
              JSONObject texture = textureArray.getJSONObject(i);

              if(texture.getString("name").equals(adIcon)) {
                String iconPath = texture.getString("path");
                intentAlarm.putExtra("icon", getContext().getCacheDir() + "/" + adName + "/" + iconPath);
                break;
              }
            }
          }

          intentAlarm.putExtra("title", pushTitle);
          intentAlarm.putExtra("desc", pushDesc);
          intentAlarm.putExtra("url", pushURL);

          AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
          alarmManager.set(AlarmManager.RTC_WAKEUP,time, PendingIntent.getBroadcast(getContext(), 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

          Toast.makeText(getContext(), "You'll get a notification soon :)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
          e.printStackTrace();
        }

        return null;
      }
    }.execute();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();

    if(action == MotionEvent.ACTION_DOWN && !clicked) {
      float x = event.getX();
      float y = event.getY();

      if(x >= remindMeButtonX && y >= remindMeButtonY) {
        if(x <= remindMeButtonX + remindMeButtonW && y <= remindMeButtonY + remindMeButtonH) {
          registerClick();
        }
      }
    }

    return true;
  }
}
