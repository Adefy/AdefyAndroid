package com.sit.adefy.actors;

import android.util.Log;

import com.sit.adefy.AdefyRenderer;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;

import java.util.Arrays;

public class CircleActor extends Actor {

  private float radius;

  public CircleActor(AdefyRenderer renderer, int _id, float[] _vertices, float radius) {
    super(renderer, _id, _vertices);
    this.radius = radius;
  }

  @Override
  protected void generateUVs() {

    // Circle textures not currently supported ;(
    texVerts = new float[]{
        0.0f
    };
  }

  @Override
  protected Shape generateShape() {
    CircleShape shape = new CircleShape();

    shape.setRadius(radius * AdefyRenderer.getMPP());

    return shape;
  }
}
