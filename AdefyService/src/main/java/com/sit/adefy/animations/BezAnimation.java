package com.sit.adefy.animations;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;

import com.sit.adefy.AdefyScene;
import com.sit.adefy.Renderer;
import com.sit.adefy.objects.Actor;

import org.jbox2d.common.Vec2;

import java.util.Arrays;
import java.util.TimerTask;

/*
  Computes and applies bezier animations. Great fun. Works in ms.
 */
public class BezAnimation {

  private final Actor actor;
  private String property[];

  private Vec2 controlPoints[];
  private float endVal;
  private float startVal;
  private float duration;
  private int fps;

  // JS callback method names, directly executable from our end on the webview.
  private String cbStart;
  private String cbEnd;
  private String cbStep;  // We pass the value at each step as an argument

  // Signals that we've already animated, prevents future starts
  private boolean animated = false;

  // Amount we increment t by on each step
  private float tIncr;

  public BezAnimation(
      Actor actor,
      float endVal,
      Vec2 controlPoints[],
      float duration,
      String property[],
      int fps
  ) {
    this(actor, endVal, controlPoints, duration, property, fps, "", "", "");
  }

  public BezAnimation(
      Actor actor,
      float endVal,
      Vec2 controlPoints[],
      float duration,
      String property[],
      int fps,
      String cbStart,
      String cbEnd,
      String cbStep) {

    this.actor = actor;
    this.property = property;

    this.controlPoints = controlPoints;
    this.endVal = endVal;
    this.duration = duration;
    this.fps = fps;

    this.cbStart = cbStart;
    this.cbEnd = cbEnd;
    this.cbStep = cbStep;

    if(property != null) { validate(); }

    tIncr = 1 / (duration * ((float)fps / 1000.0f));
  }

  // Used by the animation interface to direct properties we support to us
  static public boolean canAnimate(String[] property) {
    return canAnimate(property[0]);
  }
  static public boolean canAnimate(String property) {

    if(property.equals("position")) { return true; }
    else if(property.equals("color")) { return true; }
    else if(property.equals("rotation")) { return true; }

    return false;
  }

  // Checks the arguments we were supplied, and ensures we can actually animate
  //
  // We don't throw any errors or anything else of the sort, this is purely for debugging!
  // Out in the wild, there should never be any risk of an invalid property!
  private void validate() {

    // Property
    if(property.length == 0 || property.length > 2) {
      Log.e("BezAnimation", "Property must be defined as an array, between 1 and 2 elements long");
    } else if(property[0].equals("position")) {
      if(!property[1].equals("x") && !property[1].equals("y")) {
        Log.e("BezAnimation", "Position must be specified with an 'x' or 'y' component");
      }
    } else if(property[0].equals("color")) {
      if(!property[1].equals("r") && !property[1].equals("g") && !property[1].equals("b")) {
        Log.e("BezAnimation", "Color must be specified with an 'r', 'g', or 'b' component");
      }
    } else if(!property[0].equals("rotation")) {
      Log.e("BezAnimation", "Valid properties are 'position', 'color', and 'rotation'");
    }

    // Control points
    if(this.controlPoints != null) {
      if(this.controlPoints.length > 2) {
        Log.e("BezAnimation", "We only support 0, 1, and 2 degree beziers");
      }
    }
  }

  // Note that we don't perform any sanity checks, since that happens in our constructor!
  //
  // Fetchs the current value of the property we are meant to animate from our actor
  private void getStartValue() {

    // There must be a nicer way of doing this, but for the time being, this'll have to do.
    if(property[0].equals("rotation")) {
      startVal = actor.getRotation();
    } else if(property[0].equals("position")) {
      if(property[1].equals("x")) { startVal = actor.getPosition().x; }
      else if(property[1].equals("y")) { startVal = actor.getPosition().y; }
    } else if(property[0].equals("color")) {
      if(property[1].equals("r")) { startVal = actor.color.r; }
      else if(property[1].equals("g")) { startVal = actor.color.g; }
      else if(property[1].equals("b")) { startVal = actor.color.b; }
    }
  }

  // Calculates the bezier value for a certain t
  private float update(float t) { return update(t, true); }
  private float update(float t, boolean apply) {

    if(t > 1 || t < 0) {
      Log.e("BezAnimation", "t out of bounds!");
      return 0;
    }

    int degree = 0;
    float val = 0;

    if(controlPoints != null) {
      degree = controlPoints.length;
    }

    if(degree == 0) {
      val = startVal + ((endVal - startVal) * t);
    } else if(degree == 1) {

      // Speed things up by pre-calculating some elements
      float _Mt = 1 - t;
      float _Mt2 = _Mt * _Mt;
      float _t2 = t * t;

      // [x, y] = [(1 - t)^2]P0 + 2(1 - t)tP1 + (t^2)P2
      val = (_Mt2 * startVal) + (2 * _Mt * t * controlPoints[0].y) + _t2 + endVal;

    } else if(degree == 2) {

      // Speed things up by pre-calculating some elements
      float _Mt = 1 - t;
      float _Mt2 = _Mt * _Mt;
      float _Mt3 = _Mt2 * _Mt;
      float _t2 = t * t;
      float _t3 = _t2 * t;

      // [x, y] = [(1 - t)^3]P0 + 3[(1 - t)^2]P1 + 3(1 - t)(t^2)P2 + (t^3)P3
      val = (_Mt3 * startVal) + (3 * _Mt2 * t * controlPoints[0].y);
      val += (3 * _Mt * _t2 * controlPoints[1].y) + (_t3 * endVal);
    }

    if(apply) { applyValue(val); }

    return val;
  }

  // Go through and calculate values for each step, and return the result as a JSON array
  public String preCalculate(float _startVal) {

    StringBuilder sb = new StringBuilder();
    sb.append("{ \"stepTime\": ").append(duration * tIncr).append(", \"values\": [");

    float origStartVal = startVal;
    startVal = _startVal;

    float t = 0;
    int i = 0;

    while(t < 1) {
      sb.append('"').append(update(t, false)).append('"');
      if(t + tIncr < 1) { sb.append(","); }

      t += tIncr;
      i++;
    }

    sb.append("]}");

    startVal = origStartVal;

    return sb.toString();
  }

  // Applies a value to our designated property on our actor
  private void applyValue(float val) {

    if(actor == null) {
      Log.w("BezAnimation", "Can't apply value, no actor specified");
      return;
    }

    synchronized (actor) {

      if(property[0].equals("rotation")) {
        actor.setRotation(val);
      } else if(property[0].equals("position")) {
        Vec2 pos = actor.getPosition();

        if(property[1].equals("x")) { pos.x = val; }
        else if(property[1].equals("y")) { pos.y = val; }

        actor.setPosition(pos);
      } else if(property[0].equals("color")) {
        if(property[1].equals("r")) { actor.color.r = (int)Math.floor(val); }
        else if(property[1].equals("g")) { actor.color.g = (int)Math.floor(val); }
        else if(property[1].equals("b")) { actor.color.b = (int)Math.floor(val); }
      }
    }
  }

  // Starts a timer, and the animation
  public void animate() { animate(0); }
  public void animate(long start) {
    if(animated) { return; } else { animated = true; }
    if(start < 0) { start = 0; }
    if(actor == null) {
      Log.w("BezAnimation", "Can't animate, no actor specified. Did you mean to preCalculate?");
      return;
    }

    if(cbStart.length() > 0) {
      AdefyScene.getWebView().loadUrl("javascript:" + cbStart + "();");
    }

    final float[] t = new float[1];
    t[0] = -tIncr;

    Renderer.animationTimer.scheduleAtFixedRate(new TimerTask() {

      private boolean firstRun = true;

      @Override
      public void run() {
        t[0] += tIncr;

        if(firstRun) {
          getStartValue();
          firstRun = false;
        }

        if(t[0] > 1) {
          cancel();

          if(cbEnd.length() > 0) {
            AdefyScene.getWebView().loadUrl("javascript:" + cbEnd + "();");
          }
        } else {
          float val = update(t[0]);

          if(cbStep.length() > 0) {
            AdefyScene.getWebView().loadUrl("javascript:" + cbStep + "(" + val + ");");
          }
        }
      }
    }, start, 1000 / fps);
  }
}
