package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.AdefyScene;
import com.sit.adefy.AdefyView;

import org.jbox2d.common.Vec3;

public class JSEngineInterface {

  private AdefyRenderer renderer;
  private AdefyView view;

  public JSEngineInterface(AdefyRenderer renderer, AdefyView view) {
    this.renderer = renderer;
    this.view = view;
  }

  @JavascriptInterface
  public void initialize(String ad, int width, int height, int logLevel, String id) {
    // TODO: Standardize this
  }

  @JavascriptInterface
  public void setClearColor(float r, float g, float b) {
    AdefyRenderer.clearCol.x = r / 255.0f;
    AdefyRenderer.clearCol.y = g / 255.0f;
    AdefyRenderer.clearCol.z = b / 255.0f;
  }

  @JavascriptInterface
  public String getClearColor() {
    Vec3 col = AdefyRenderer.clearCol;
    return "{ r: " + col.x + ", g: " + col.y + ", b: " + col.z + " }";
  }

  @JavascriptInterface
  public void setRemindMeButton(float x, float y, float w, float h) {
    view.setRemindMeButton(x, y, w, h);
  }

  @JavascriptInterface
  public void setLogLevel(int level) {
    // Does nothing for now
  }

  @JavascriptInterface
  public void setCameraPosition(float x, float y) {
    AdefyRenderer.camX = x;
    AdefyRenderer.camY = y;
  }

  @JavascriptInterface
  public String getCameraPosition() {
    return "{ x: " + AdefyRenderer.camX + ", y: " + AdefyRenderer.camY + " }";
  }

  @JavascriptInterface
  public void triggerEnd() {
    if(AdefyScene.getMe() != null) {
      AdefyScene.getMe().finish();
    }
  }

  @JavascriptInterface
  public void setOrientation(String o) {
    if(AdefyScene.getMe() != null) {
      if(o.equals("portrait")) {
        AdefyScene.getMe().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      } else if(o.equals("landscape")) {
        AdefyScene.getMe().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      }
    }
  }
}
