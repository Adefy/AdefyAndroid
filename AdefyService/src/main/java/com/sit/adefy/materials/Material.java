package com.sit.adefy.materials;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import java.nio.FloatBuffer;

// Base material
public class Material {

  private String name;
  protected static int shader;
  protected static String vertCode = "";
  protected static String fragCode = "";

  Material(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getShader() {
    return shader;
  }
}
