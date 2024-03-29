package com.sit.adefy.animations;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.actors.Actor;

import java.util.ArrayList;
import java.util.TimerTask;

/*
  Applies a series of changes to an actors vertices at specified times
 */
public class VertAnimation extends Animation {

  private final Actor actor;

  private int[] delays;
  private String[][] deltas;
  private float[] udata;

  // Signals that we've already animated, prevents future starts
  private boolean animated = false;

  public VertAnimation(
      Actor actor,
      int[] delays,
      String[][] deltas,
      float[] udata
  ) {
    this(actor, delays, deltas, udata, "", "", "");
  }

  public VertAnimation(
      Actor actor,
      int[] delays,
      String[][] deltas,
      float[] udata,
      String cbStart,
      String cbStep,
      String cbEnd
  ) {

    this.actor = actor;
    this.delays = delays;
    this.deltas = deltas;
    this.udata = udata;

    validate();
  }

  // Used by the animation interface to direct properties we support to us
  static public boolean canAnimate(String[] property) {
    return canAnimate(property[0]);
  }
  static public boolean canAnimate(String property) {
    return property.equals("vertices");
  }

  // Purely for debugging, validates arguments
  private void validate() {

    if(delays.length != deltas.length) {
      Log.e("VertAnimation", "Delay and delta counts don't match");
    }

    if(udata != null) {
      if(udata.length != deltas.length && udata.length > 1) {
        Log.e("VertAnimation", "Not enough user data supplied");
      }
    }
  }

  // Applies a delta set to the actor, relative to current vertices
  private void applyDeltaSet(String[] deltaSet) {

    float[] verts = actor.getVertices();
    ArrayList<Float> finalVerts = new ArrayList<Float>();

    boolean repeat = false;

    /*
         N    - Absolute update
        "-N"  - Negative change
        "+N"  - Positive change
        "."   - No change
        "|"   - Finished, break
        "..." - Repeat preceeding
    */
    for(int i = 0; i < deltaSet.length; i++) {
      String d = deltaSet[i];
      char prefix = d.toCharArray()[0];
      Float val = null;

      // Handle repeat
      if(d.equals("...")) {
        repeat = true;
        i = 0;
      }

      // Surpass boundary, break if repeating, otherwise continue with null
      if (i >= verts.length) {
        if(repeat) { break; }
      } else {
        val = verts[i];
      }

      if(prefix == '|') {
        break;
      } else if((prefix == '+' || prefix == '-') && val == null) {
        Log.e("VertAnimation", "Relative delta, but vert is out of bounds");
      } else if(prefix == '-') {
        val -= Float.parseFloat(d.substring(1));
      } else if(prefix == '+') {
        val += Float.parseFloat(d.substring(1));
      } else if(prefix == '`') {
        val = Float.parseFloat(d.substring(1));
      } else if(prefix != '.') {
        Log.e("VertAnimation", "Unknown prefix " + prefix);
      }

      finalVerts.add(val);
    }

    actor.updateVertices(finalVerts);
  }

  public void animate() { animate(0); }
  public void animate(long start) {
    if(animated) { return; } else { animated = true; }
    if(start < 0) { start = 0; }

    final int[] nextDSet = new int[1];
    nextDSet[0] = 0;

    for (int delay : delays) {

      AdefyRenderer.animationTimer.schedule(new TimerTask() {

        @Override
        public void run() {
          applyDeltaSet(deltas[nextDSet[0]]);

          nextDSet[0]++;
        }

      }, delay + start);
    }
  }
}
