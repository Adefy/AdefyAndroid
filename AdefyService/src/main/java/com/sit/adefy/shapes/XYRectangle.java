package com.sit.adefy.shapes;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;
import android.widget.AutoCompleteTextView;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class XYRectangle extends BaseShape {

  private float width;
  private float height;
  private float hWidth;
  private float hHeight;

  public XYRectangle(float width, float height) {
    super();

    vertices = new float[12];

    setWidth(width);
    setHeight(height);
    refreshVertices();
  }

  public void setTexture(GL10 gl, Bitmap texBitmap) {

    if(texVerts == null) {
      texVerts = new float[8];

      texVerts[0] = 0.0f; texVerts[1] = 1.0f;
      texVerts[2] = 0.0f; texVerts[3] = 0.0f;
      texVerts[4] = 1.0f; texVerts[5] = 1.0f;
      texVerts[6] = 1.0f; texVerts[7] = 0.0f;

      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(texVerts.length * 4);
      byteBuffer.order(ByteOrder.nativeOrder());
      texBuffer = byteBuffer.asFloatBuffer();
      texBuffer.put(texVerts);
      texBuffer.position(0);
    }

    gl.glGenTextures(1, texture, 0);
    gl.glBindTexture(GL10.GL_TEXTURE_2D, texture[0]);

    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, texBitmap, 0);

    texBitmap.recycle();
  }

  protected void refreshVertices() {
    vertices[0] = -hWidth;
    vertices[1] = -hHeight;
    vertices[2] = 1.0f;

    vertices[3] = -hWidth;
    vertices[4] = hHeight;
    vertices[5] = 1.0f;

    vertices[6] = hWidth;
    vertices[7] = -hHeight;
    vertices[8] = 1.0f;

    vertices[9] = hWidth;
    vertices[10] = hHeight;
    vertices[11] = 1.0f;

    refreshVertBuffer();
  }

  public void setWidth(float width) {
    this.width = width;
    this.hWidth = width / 2.0f;
    refreshVertices();
  }
  public void setHeight(float height) {
    this.height = height;
    this.hHeight = height / 2.0f;
    refreshVertices();
  }

  @Override
  public void setPosition(Vec2 position) {
    super.setPosition(position);
    refreshVertices();
  }

  public float getWidth() { return this.width; }
  public float getHeight() { return this.height; }
}
