package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.js.JSAnimationInterface;
import com.sit.adefy.js.JSEngineInterface;
import com.sit.adefy.objects.Actor;
import com.sit.adefy.physics.PhysicsEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.microedition.khronos.opengles.GL10;

public class AdefyScene extends Activity {

  private static Renderer renderer = null;
  private static PhysicsEngine psyx = null;
  private static WebView web = null;

  private JSONObject jsonObj = new JSONObject();
  private JSONArray textureArray = new JSONArray();
  private static ArrayList<TextureDescriptor> textures = new ArrayList<TextureDescriptor>();

  private static String adFolderName;
  private static AdefyScene me = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    me = this;
    renderer = new Renderer();
    psyx = new PhysicsEngine();

    GLSurfaceView mView = new GLSurfaceView(this);
    mView.setEGLContextClientVersion(2);
    mView.setRenderer(renderer);

    setContentView(mView);

    Intent launchedIntent = getIntent();
    adFolderName = launchedIntent.getStringExtra("folder");
    File jsonFile = new File(this.getCacheDir() + "/" + adFolderName + "/package.json");
    String jsonText = "";
    BufferedReader br = null;
    String texturesPath = "";

    try {
      br = new BufferedReader(new FileReader(jsonFile.toString()));
      String crtLine;

      while((crtLine = br.readLine()) != null) { jsonText += crtLine; }

      jsonObj = new JSONObject(jsonText);
      //textureArray = jsonObj.getJSONArray("textures");

    } catch (Exception e) {
      e.printStackTrace();
    }

    //refreshTextures("adefyFolder");

    // Create webview
    web = new WebView(this);
    web.getSettings().setJavaScriptEnabled(true);

    // Set up console logging
    web.setWebChromeClient(new WebChromeClient() {

      public boolean onConsoleMessage(ConsoleMessage cm) {

        Log.d("adefy", cm.message() + " -- line " + cm.lineNumber() + " of " + cm.sourceId());
        return true;
      }
    });

    // Load the initial interface
    web.addJavascriptInterface(new WebViewLoadNotify(), "__iface_load");

    // Set up actual Adefy interfaces
    web.addJavascriptInterface(new JSEngineInterface(), "__iface_engine");
    web.addJavascriptInterface(new JSActorInterface(), "__iface_actors");
    web.addJavascriptInterface(new JSAnimationInterface(), "__iface_animations");

    // Calls our load interface
    String loadJS = "" +
        "javascript:(function(){" +
          "window.onload=function(){" +
            "__iface_load.onLoad();" +
          "};" +
        "})()";

    // Inject!
    web.loadData("", "text/html", null);
    web.loadUrl(loadJS);
  }

  final Handler onLoadHandler = new Handler();
  final Runnable onLoadRunnable = new Runnable() {

    @Override
    public void run() {
      me.executeOnLoad();
    }
  };

  private void executeOnLoad() {

    String ifaceDef = "" +
        "window.AdefyGLI = {};" +
        "window.AdefyGLI.Engine = function(){ return window.__iface_engine; };" +
        "window.AdefyGLI.Actors = function(){ return window.__iface_actors; };" +
        "window.AdefyGLI.Animations = function(){ return window.__iface_animations; };";

    try {

      // Load AdefyJS
      String scene = getJS("adefy.js", adFolderName);
      scene += getJS(jsonObj.getJSONObject("scenes").getString("1"), adFolderName);

      // TODO: We are executing arbitrary JS, preform some security checks!

      // Execute the sent scene
      web.loadUrl("javascript:(function(){" + ifaceDef + scene + "})()");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // We move the code into a class that exposes a JS interface, which gets called
  // on the window onload() event. This then loads up our stuff.
  private final class WebViewLoadNotify {

    @JavascriptInterface
    public void onLoad() {

      onLoadHandler.post(onLoadRunnable);
    }
  }

  public static WebView getWebView() {
    return web;
  }

  private String getJS(String name, String folder) {

    try {

      File jsFile = new File(this.getCacheDir() + "/" + folder + "/" + name);
      BufferedReader br = new BufferedReader(new FileReader(jsFile.toString()));
      StringBuilder sb = new StringBuilder((int)jsFile.length());

      char[] buffer = new char[2048];
      int count;

      while((count = br.read(buffer, 0, 2048)) >= 0) {
        sb.append(new String(buffer, 0, count));
      }

      return sb.toString();

    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public void refreshTextures(String folderName)  {

    Log.v("Adefy", "refreshTextures called for the following folder: "  + folderName);

    for (int i = 0; i < textureArray.length(); i++)  {

      try {

        TextureDescriptor textureDescriptor = new TextureDescriptor(textureArray.getString(i));
        GLES20.glGenTextures(1, textureDescriptor.getHandle(), 0);

        Bitmap textureBitmap = BitmapFactory.decodeFile(this.getCacheDir() + "/" + folderName + "/textures/" + textureArray.getString(i));

        Log.v("Adefy", "Loaded bitmap " + textureArray.getString(i) + " size " + (textureBitmap.getByteCount() / 1024) + "kB");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDescriptor.getHandle()[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, textureBitmap, 0);

        textureBitmap.recycle();
        textures.add(textureDescriptor);

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    Log.v("Adefy", "Loaded " + textures.size() + " into GL memory");
  }

  public static PhysicsEngine getPhysicsEngine() { return psyx; }
  public static Renderer getRenderer() { return renderer; }

  // Make a class holding a String name and an int[1] handle
  private class TextureDescriptor {

    private String path;
    private int[] handle = new int[1];

    public TextureDescriptor(String _path) {
      path = _path;
    }

    public String getPath() { return path; }
    public int[] getHandle() { return handle; }
  }
}
