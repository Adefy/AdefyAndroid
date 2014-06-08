package com.sit.adefy.animations;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.actors.Actor;
import com.sit.adefy.objects.Color3;

import java.util.Timer;
import java.util.TimerTask;

/*
  Computes and applies bezier animations. Great fun. Works in ms.
 */
public class BezAnimation extends Animation {

  private final Actor actor;
  private String property[];

  private Vector2 controlPoints[];
  private float endVal;
  private float startVal;
  private float duration;
  private int fps;
  private long startTime;
  private boolean firstRun;

  private Vector2 tempVec = new Vector2();

  public BezAnimation(
      Actor actor,
      float endVal,
      Vector2 controlPoints[],
      float duration,
      String property[],
      int fps
  ) {
    this(actor, endVal, controlPoints, duration, property, fps, "", "", "");
  }

  public BezAnimation(
      Actor actor,
      float endVal,
      Vector2 controlPoints[],
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

    if(property != null) { validate(); }
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
      actor.getPosition(tempVec);

      if(property[1].equals("x")) { startVal = tempVec.x; }
      else if(property[1].equals("y")) { startVal = tempVec.y; }
    } else if(property[0].equals("color")) {
      if(property[1].equals("r")) { startVal = actor.getColor().r; }
      else if(property[1].equals("g")) { startVal = actor.getColor().g; }
      else if(property[1].equals("b")) { startVal = actor.getColor().b; }
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

    float tIncr = 1 / (duration * ((float)fps / 1000.0f));

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
        actor.getPosition(tempVec);

        if(property[1].equals("x")) { tempVec.x = val; }
        else if(property[1].equals("y")) { tempVec.y = val; }

        actor.setPosition(tempVec);
      } else if(property[0].equals("color")) {

        Color3 col = actor.getColor();

        if(property[1].equals("r")) { col.r = (int)Math.floor(val); }
        else if(property[1].equals("g")) { col.g = (int)Math.floor(val); }
        else if(property[1].equals("b")) { col.b = (int)Math.floor(val); }

        actor.setColor(col);
      }
    }
  }

  // Called by the renderer with the current time. We use our start time, duration, and fps to step
  // update ourselves, and finish the animation when needed.
  public void rendererAnimateStep(long time) {
    if(!isActive()) { return; }
    if(time < startTime) { return; }

    if(firstRun) {
      getStartValue();
      firstRun = false;
    }

    float t = (time - startTime) / duration;

    if(t >= 1) {

      update(1);
      actor.getRenderer().animations.remove(this);
      setActive(false);

    } else {
      update(t);
    }
  }

  // Starts a timer, and the animation
  public void animate() { animate(0); }
  public void animate(long start) {
    if(isActive()) { return; }
    if(start < 0) { start = 0; }
    if(actor == null) {
      Log.w("BezAnimation", "Can't animate, no actor specified. Did you mean to preCalculate?");
      return;
    }

    setActive(true);
    firstRun = true;

    startTime = System.currentTimeMillis() + start;
    actor.getRenderer().animations.add(this);
  }
}
