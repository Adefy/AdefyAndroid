package com.sit.adefy.objects;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.graphics.*;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.opengles.GL10;

public class TextActor { //extends Sprite {
/*
  private Vec2 offset = new Vec2(0, 0); // Takes into account padding

  // Default is white on black
  public TextActor(GL10 gl, String text, int size) {
    create(gl, text, size, new Vec2(0, 0), new Color3(255, 255, 255));
  }
  public TextActor(GL10 gl, String text, int size, Vec2 position) {
    create(gl, text, size, position, new Color3(255, 255, 255));
  }
  public TextActor(GL10 gl, String text, int size, Vec2 position, Color3 fg) {
    create(gl, text, size, position, fg);
  }

  // Method take from http://stackoverflow.com/questions/1339136/draw-text-in-opengl-es-android
  protected void create(GL10 gl, String text, int size, Vec2 position, Color3 fg) {

    // Render properties
    Paint textPaint = new Paint();
    textPaint.setTextSize(size);
    textPaint.setAntiAlias(true);
    textPaint.setARGB(255, fg.r, fg.g, fg.b);
    textPaint.setTextAlign(Paint.Align.LEFT);

    Paint bgPaint = new Paint();
    bgPaint.setARGB(0, 0, 0, 0);

    // Calculate necessary canvas size
    Rect bounds = new Rect();
    textPaint.getTextBounds(text, 0, text.length(), bounds);

    float x = bounds.width();
    float y = bounds.height();

    y += size * 0.4f; // Add vertical padding for lowercase hanging letters

    x = (float)Math.pow(2, Math.ceil(Math.log(x) / Math.log(2)));
    y = (float)Math.pow(2, Math.ceil(Math.log(y) / Math.log(2)));

    offset = new Vec2(x / 2.0f, y / 2.0f);

    // Create canvas, draw text
    Bitmap canvasBitmap = Bitmap.createBitmap((int)x, (int)y, Bitmap.Config.ARGB_4444);
    Canvas canvas = new Canvas(canvasBitmap);
    canvas.drawRect(0, 0, x, y, bgPaint);
    canvas.drawText(text, 0, bounds.height(), textPaint);

    createWithTexture(gl, canvasBitmap, new Vec2(position.x + offset.x, position.y + offset.y));
  }

  public void setPosition(Vec2 position) {
    super.setPosition(new Vec2(position.x + offset.x, position.y + offset.y));
  }

  public Vec2 getPosition() {
    return new Vec2(super.getPosition().x - offset.x, super.getPosition().y - offset.y);
  } */
}
