package com.sit.adefy.objects;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

public class Texture {

  private String name;
  private int[] handle = new int[1];

  public Texture(String name) {
    this.name = name;
  }

  public String getName() { return name; }
  public int[] getHandle() { return handle; }

  public float clipScaleU = 1.0f;
  public float clipScaleV = 1.0f;
}