package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.webkit.JavascriptInterface;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.AdefyScene;

import org.jbox2d.common.Vec3;

public class JSEngineInterface {

  private AdefyRenderer renderer;

  public JSEngineInterface(AdefyRenderer renderer) {
    this.renderer = renderer;
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
}
