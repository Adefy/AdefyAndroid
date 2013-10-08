package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.webkit.JavascriptInterface;

import com.sit.adefy.AdefyRenderer;

import org.jbox2d.common.Vec3;

public class JSEngineInterface {

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
}
