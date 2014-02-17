package com.sit.adefy.actors;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;

import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.sit.adefy.AdefyRenderer;

public class RectangleActor extends Actor {

  protected float width;
  protected float height;

  public RectangleActor(AdefyRenderer renderer, int _id, float width, float height) {
    super(renderer, _id, null);

    if(width != 0 && height != 0) {
      this.width = width;
      this.height = height;

      updateVertices(generateVertexSet());
    }
  }

  protected float[] generateVertexSet() {
    float[] verts = new float[8];

    verts[0] = -width / 2.0f;
    verts[1] = -height / 2.0f;
    verts[2] = -width / 2.0f;
    verts[3] =  height / 2.0f;
    verts[4] =  width / 2.0f;
    verts[5] =  height / 2.0f;
    verts[6] =  width / 2.0f;
    verts[7] = -height / 2.0f;

    return verts;
  }

  @Override
  protected void generateUVs() {
    texVerts = new float[]{
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
    };
  }

  @Override
  protected PolygonShape generateShape() {
    PolygonShape shape = new PolygonShape();

    shape.setAsBox((width * AdefyRenderer.getMPP()) / 2, (height * AdefyRenderer.getMPP()) / 2);

    return shape;
  }
}
