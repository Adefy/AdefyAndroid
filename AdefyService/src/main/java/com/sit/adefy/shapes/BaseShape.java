package com.sit.adefy.shapes;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import com.sit.adefy.objects.Color3;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BaseShape {

  protected FloatBuffer vertBuffer;
  protected FloatBuffer texBuffer;
  protected int vertCount;
  protected float vertices[];
  protected float texVerts[] = null;
  protected int[] texture = new int[1];

  protected Vec2 position = new Vec2(0.0f, 0.0f);
  protected Vec3 rotation = new Vec3(0.0f, 0.0f, 0.0f);
  public Color3 color = new Color3(255, 255, 255);
  public boolean lit = false;
  public boolean visible = true;

  public BaseShape() { }

  protected void refreshVertBuffer() {

    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    vertBuffer = byteBuffer.asFloatBuffer();
    vertBuffer.put(vertices);
    vertBuffer.position(0);

    this.vertCount = vertices.length;
  }

  public void draw(GL10 gl) {

    if(visible) {
      gl.glPushMatrix();

      if(texVerts != null) {
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);
      }

      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
      if(texVerts != null) { gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY); }

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
      if(texVerts != null) { gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuffer); }

      gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertCount / 3);

      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
      if(texVerts != null) { gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); }

      if(texVerts != null) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        gl.glDisable(GL10.GL_TEXTURE_2D);
      }

      gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
      gl.glPopMatrix();
    }
  }

  public void setPosition(Vec2 position) { this.position = position; }
  public void setRotation(Vec3 rotation) { this.rotation = rotation; }

  public Vec2 getPosition() { return position; }
  public Vec3 getRotation() { return rotation; }
  public float[] getVertices() { return vertices; }
}
