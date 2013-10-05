package com.sit.adefy.shapes;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.opengles.GL10;

public class XYnGon extends BaseShape {

  private double radius;
  private int segments; // TODO: add smart accessor

  public XYnGon(Vec2 position, double radius, int segments) {
    super();

    this.position = position;
    this.radius = radius;
    this.segments = segments;
    refreshVertices();
  }

  private void refreshVertices() {

    // Algo taken from http://slabode.exofire.net/circle_draw.shtml
    double x = this.radius;
    double y = 0;

    double theta = (2.0f * 3.1415926f) / segments;
    double tanFactor = Math.tan(theta);
    double radFactor = Math.cos(theta);

    vertices = new float[3 * segments];

    for(int i = 0; i < segments; i++) {

      int index = i * 3;
      vertices[index] = (float)x;
      vertices[index + 1] = (float)y;
      vertices[index + 2] = 1.0f;

      double tx = -y;
      double ty = x;

      x += tx * tanFactor;
      y += ty * tanFactor;

      x *= radFactor;
      y *= radFactor;
    }

    refreshVertBuffer();
  }

  public void draw(GL10 gl) {

    if(visible) {
      gl.glPushMatrix();
      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

      if(lit) { gl.glEnable(GL10.GL_LIGHTING); }
      else { gl.glDisable(GL10.GL_LIGHTING); }

      gl.glTranslatef(position.x, position.y, 1.0f);

      gl.glRotatef(rotation.x, 1.0f, 0.0f, 0.0f);
      gl.glRotatef(rotation.y, 0.0f, 1.0f, 0.0f);
      gl.glRotatef(rotation.z, 0.0f, 0.0f, 1.0f);

      if(texVerts == null) {
        Vec3 renderCol = color.toFloat();
        gl.glColor4f(renderCol.x, renderCol.y, renderCol.z, 1.0f);
      } else {
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
      }

      gl.glFrontFace(GL10.GL_CW);
      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertBuffer);
      gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, vertCount / 3);

      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

      gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
      gl.glPopMatrix();
    }
  }

  public void setRadius(double radius) {
    this.radius = radius;
    refreshVertices();
  }
  public void setSegments(int segments) {
    this.segments = segments;
    refreshVertices();
  }

  public int getSegments() { return segments; }
  public double getRadius() { return radius; }
}
