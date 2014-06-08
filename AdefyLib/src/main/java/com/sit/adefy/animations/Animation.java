package com.sit.adefy.animations;

public class Animation {

  // Signals that we've already animated, prevents future starts
  private boolean mActive = false;

  public void rendererAnimateStep(long time) {}

  public boolean isActive() {
    return mActive;
  }

  public void setActive(boolean active) {
    mActive = active;
  }
}
