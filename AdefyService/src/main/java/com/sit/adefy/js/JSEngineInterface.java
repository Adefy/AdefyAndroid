package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.sit.adefy.Renderer;

public class JSEngineInterface {

  @JavascriptInterface
  public void initialize(String ad, int width, int height, int logLevel, String id) {
    // TODO: Standardize this
  }

  @JavascriptInterface
  public void setClearColor(float r, float g, float b) {

    Renderer.clearColor.x = r / 255.0f;
    Renderer.clearColor.y = g / 255.0f;
    Renderer.clearColor.z = b / 255.0f;
  }

  @JavascriptInterface
  public String getClearColor() {
    return "{ r: " + Renderer.clearColor.x + ", g: " + Renderer.clearColor.y + ", b: " + Renderer.clearColor.z + " }";
  }

  @JavascriptInterface
  public void setLogLevel(int level) {
    // Does nothing for now
  }
}
