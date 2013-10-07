package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.sit.adefy.Renderer;
import com.sit.adefy.objects.Color3;

import org.jbox2d.common.Vec3;

public class JSEngineInterface {

  @JavascriptInterface
  public void initialize(String ad, int width, int height, int logLevel, String id) {
    // TODO: Standardize this
  }

  @JavascriptInterface
  public void setClearColor(float r, float g, float b) {
    Renderer.clearCol.x = r / 255.0f;
    Renderer.clearCol.y = g / 255.0f;
    Renderer.clearCol.z = b / 255.0f;
  }

  @JavascriptInterface
  public String getClearColor() {
    Vec3 col = Renderer.clearCol;
    return "{ r: " + col.x + ", g: " + col.y + ", b: " + col.z + " }";
  }

  @JavascriptInterface
  public void setLogLevel(int level) {
    // Does nothing for now
  }
}
