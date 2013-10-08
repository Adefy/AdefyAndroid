package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.webkit.JavascriptInterface;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.animations.BezAnimation;
import com.sit.adefy.animations.VertAnimation;
import com.sit.adefy.objects.Actor;

import org.jbox2d.common.Vec2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSAnimationInterface {

  private Actor findActor(int id) {
    for(Actor a : AdefyRenderer.actors) {
      if(a.getId() == id) { return a; }
    }

    return null;
  }

  @JavascriptInterface
  public boolean canAnimate(String property) {

    // Fugly, but it gets the job done. My elegance is lacking in Java, epic sadness.
    if(BezAnimation.canAnimate(property)) { return true; }
    else if(VertAnimation.canAnimate(property)) { return true; }

    return false;
  }

  @JavascriptInterface
  public String getAnimationName(String property) {

    if(BezAnimation.canAnimate(property)) { return "bezier"; }
    else if(VertAnimation.canAnimate(property)) { return "vert"; }

    return "";
  }

  @JavascriptInterface
  public void animate(int id, String properties, String options) {

    Actor a = findActor(id);
    if (a == null) { return; }

    try {

      JSONArray prop = new JSONArray(properties);

      // Build property name
      String property[] = new String[2];
      property[0] = prop.getString(0);
      if(prop.length() == 2) { property[1] = prop.getString(1); }

      // Bezier animation
      if(BezAnimation.canAnimate(property)) {
        buildBezierAnimation(a, property, new JSONObject(options), false);
      } else if(VertAnimation.canAnimate(property)) {
        buildVertAnimation(a, new JSONObject(options));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void buildVertAnimation(Actor a, JSONObject options) throws JSONException {

    JSONArray delaysJSON = options.getJSONArray("delays");
    JSONArray deltasJSON = options.getJSONArray("deltas");

    int[] delays = new int[delaysJSON.length()];
    String[][] deltas = new String[deltasJSON.length()][];

    for(int i = 0; i < delaysJSON.length(); i++) {

      JSONArray deltasSub = deltasJSON.getJSONArray(i);

      delays[i] = delaysJSON.getInt(i);
      deltas[i] = new String[deltasSub.length()];

      for(int d = 0; d < deltasSub.length(); d++) {
        deltas[i][d] = deltasSub.getString(d);
      }
    }

    new VertAnimation(a, delays, deltas, null).animate();
  }

  // Returns preCalculated values if requested, otherwise an empty string
  private String buildBezierAnimation(
      Actor a,
      String[] property,
      JSONObject options,
      boolean preCalc
  ) throws JSONException {

    Vec2 cPoints[] = null;

    float endVal = options.getLong("endVal");
    float duration = options.getInt("duration");
    int fps = options.getInt("fps");
    int start = options.getInt("start");

    // Load control points
    if(options.getJSONArray("controlPoints") != null) {
      JSONArray JSON_cpoints = options.getJSONArray("controlPoints");

      cPoints = new Vec2[JSON_cpoints.length()];

      for(int p = 0; p < cPoints.length; p++) {
        cPoints[p].x = JSON_cpoints.getJSONObject(p).getLong("x");
        cPoints[p].y = JSON_cpoints.getJSONObject(p).getLong("y");
      }
    }

    BezAnimation anim;

    if(preCalc) {
      anim = new BezAnimation(null, endVal, cPoints, duration, null, fps);
      return anim.preCalculate(options.getLong("startVal"));
    } else {
      anim = new BezAnimation(a, endVal, cPoints, duration, property, fps, "", "", "");
      anim.animate(start);
      return "";
    }
  }

  @JavascriptInterface
  public String preCalculateBez(String options) {

    try {
      return buildBezierAnimation(null, null, new JSONObject(options), true);

    } catch (JSONException e) {
      e.printStackTrace();
      return "[]";
    }
  }
}
