package com.sit.adefy.actors;

import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.sit.adefy.AdefyRenderer;

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
