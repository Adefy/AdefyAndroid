package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.webkit.JavascriptInterface;

import com.sit.adefy.Renderer;

public class JSEngineInterface {

  @JavascriptInterface
  public void initialize(String ad, int width, int height, int logLevel, String id) {
    // TODO: Standardize this
  }

  @JavascriptInterface
  public void setClearColor(int r, int g, int b) {
    Renderer.clearColor.x = r;
    Renderer.clearColor.y = g;
    Renderer.clearColor.z = b;
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
