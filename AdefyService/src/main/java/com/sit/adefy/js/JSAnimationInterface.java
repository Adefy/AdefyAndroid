package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.Manifest;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.sit.adefy.Renderer;
import com.sit.adefy.animations.BezAnimation;
import com.sit.adefy.objects.Actor;

import org.jbox2d.common.Vec2;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSAnimationInterface {

  private Actor findActor(int id) {
    for(Actor a : Renderer.actors) {
      if(a.getId() == id) { return a; }
    }

    return null;
  }

  @JavascriptInterface
  public boolean canAnimate(String property) {

    // Fugly, but it gets the job done. My elegance is lacking in Java, epic sadness.
    if(property.equals("position")) { return true; }
    else if(property.equals("color")) { return true; }
    else if(property.equals("rotation")) { return true; }

    return false;
  }

  @JavascriptInterface
  public String getAnimationName(String property) {

    if(property.equals("position")) { return "bezier"; }
    else if(property.equals("color")) { return "bezier"; }
    else if(property.equals("rotation")) { return "bezier"; }

    return "";
  }

  @JavascriptInterface
  public void animate(int id, String properties, String options) {
    Actor a = findActor(id);
    if (a == null) { return; }

    try {

      JSONArray prop = new JSONArray(properties);
      JSONObject opt = new JSONObject(options);

      // Build property name
      String property[] = new String[2];
      property[0] = prop.getString(0);
      if(prop.length() == 2) { property[1] = prop.getString(1); }

      // Bezier animation
      if(BezAnimation.canAnimate(property)) {

        Vec2 cPoints[] = null;

        float endVal = opt.getLong("endVal");
        float duration = opt.getInt("duration");
        int fps = opt.getInt("fps");
        int start = opt.getInt("start");

        // Load control points
        if(opt.getJSONArray("controlPoints") != null) {
          JSONArray JSON_cpoints = opt.getJSONArray("controlPoints");

          cPoints = new Vec2[JSON_cpoints.length()];

          for(int p = 0; p < cPoints.length; p++) {
            cPoints[p].x = JSON_cpoints.getJSONObject(p).getLong("x");
            cPoints[p].y = JSON_cpoints.getJSONObject(p).getLong("y");
          }
        }

        BezAnimation anim = new BezAnimation(a, endVal, cPoints, duration, property, fps, "", "", "");
        anim.animate(start);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @JavascriptInterface
  public String preCalculateBez(String options) {
    return "[]";
  }
}
