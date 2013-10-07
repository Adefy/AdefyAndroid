package com.sit.adefy.objects;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import org.jbox2d.common.Vec3;

public class Color3 {

  public int r;
  public int g;
  public int b;

  public float rF;
  public float gF;
  public float bF;

  public Color3() {
    r = g = b = 0;
  }
  public Color3(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.rF = (float)r / 255.0f;
    this.gF = (float)g / 255.0f;
    this.bF = (float)b / 255.0f;
  }
  public Color3(float r, float g, float b) {
    this.r = (int)(r * 255.0f);
    this.g = (int)(g * 255.0f);
    this.b = (int)(b * 255.0f);
    this.rF = r;
    this.gF = g;
    this.bF = b;
  }

  public Vec3 toFloat() {
    return new Vec3((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f);
  }

  public float[] toFloatArray() {
    return new float[]{ (float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f, 1.0f };
  }

  public void setR(int r) {
    this.r = r;
    rF = (float)r / 255.0f;
  }

  public void setG(int g) {
    this.g = g;
    gF = (float)g / 255.0f;
  }

  public void setB(int b) {
    this.b = b;
    bF = (float)b / 255.0f;
  }
}
