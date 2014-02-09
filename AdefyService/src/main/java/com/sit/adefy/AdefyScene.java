package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.js.JSAnimationInterface;
import com.sit.adefy.js.JSEngineInterface;
import com.sit.adefy.physics.PhysicsEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class AdefyScene extends Activity {

  // Used to enable finishing from interface
  private static AdefyScene me = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    me = this;

    Intent launchedIntent = getIntent();
    String adName = launchedIntent.getStringExtra("adName");
    String apiKey = launchedIntent.getStringExtra("apiKey");
    String serverInterface = "https://app.adefy.com/api/v1/serve";

    if(launchedIntent.getStringExtra("server") != null) {
      serverInterface = launchedIntent.getStringExtra("server");
    }

    AdefyView mView = new AdefyView(apiKey, adName, serverInterface, this);
    setContentView(mView);
  }

  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  @Override
  protected void onStop() {
    super.onStop();
    finish();
  }

  public static AdefyScene getMe() {
    return me;
  }
}
