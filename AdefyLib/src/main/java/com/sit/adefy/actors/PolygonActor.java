package com.sit.adefy.actors;

import com.sit.adefy.AdefyRenderer;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;

public class PolygonActor extends Actor {

  public PolygonActor(AdefyRenderer renderer, int _id, float[] _vertices) {
    super(renderer, _id, _vertices);
  }

  @Override
  protected void generateUVs() {

    // Polygon textures not currently supported ;(
    texVerts = new float[]{
        0.0f
    };
  }

  @Override
  protected PolygonShape generateShape() {
    PolygonShape shape = new PolygonShape();

    if(psyxVertices != null) {
      Vec2[] verts = new Vec2[psyxVertices.length / 2];

      int vertIndex = 0;
      for(int i = 0; i < psyxVertices.length; i += 2) {
        verts[vertIndex] = new Vec2(psyxVertices[i] / AdefyRenderer.getPPM(), psyxVertices[i + 1] / AdefyRenderer.getPPM());
        vertIndex++;
      }

      shape.set(verts, verts.length);

    } else {
      Vec2[] verts = new Vec2[vertices.length / 3];

      int vertIndex = 0;
      for(int i = 0; i < vertices.length; i += 3) {
        verts[vertIndex] = new Vec2(vertices[i] / AdefyRenderer.getPPM(), vertices[i + 1] / AdefyRenderer.getPPM());
        vertIndex++;
      }

      shape.set(verts, verts.length);
    }

    return shape;
  }
}
