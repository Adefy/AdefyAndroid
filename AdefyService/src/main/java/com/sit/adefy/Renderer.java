package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.sit.adefy.materials.SingleColorMaterial;
import com.sit.adefy.materials.TexturedMaterial;
import com.sit.adefy.objects.Actor;
import com.sit.adefy.objects.Color3;

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;

public class Renderer implements GLSurfaceView.Renderer {

  private static float PPM = 128.0f;
  public static Vec2 screenToWorld(Vec2 cords) { return new Vec2(cords.x / PPM, cords.y / PPM); }
  public static Vec2 worldToScreen(Vec2 cords) { return new Vec2(cords.x * PPM, cords.y * PPM); }
  public static float getPPM() { return PPM; }
  public static float getMPP() { return 1.0f / PPM; }

  public static ArrayList<Actor> actors = new ArrayList<Actor>();

  private static int screenW = 0;
  private static int screenH = 0;
  private int targetFPS = 60;
  private int targetFrameTime = 16;

  private static float[] projection = new float[16];
  public static Timer animationTimer = new Timer();
  public static Vec3 clearCol = new Vec3(0.0f, 0.0f, 0.0f);
  private String material = "";

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {

    // Use our clear color
    GLES20.glClearColor(clearCol.x, clearCol.y, clearCol.z, 1.0f);

    // Build shaders
    SingleColorMaterial.buildShader();
    TexturedMaterial.buildShader();
  }

  public static int buildShader(String vertSrc, String fragSrc) {

    // Create program
    int shader = GLES20.glCreateProgram();

    // Compile shaders
    int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    GLES20.glShaderSource(vertShader, vertSrc);
    GLES20.glCompileShader(vertShader);

    final int[] compileStatus = new int[1];
    GLES20.glGetShaderiv(vertShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    if(compileStatus[0] == 0) {
      Log.d("adefy", "error creating vert shader");
      Log.d("adefy", GLES20.glGetShaderInfoLog(vertShader));
    }

    int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
    GLES20.glShaderSource(fragShader, fragSrc);
    GLES20.glCompileShader(fragShader);

    GLES20.glGetShaderiv(fragShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

    if(compileStatus[0] == 0) {
      Log.d("adefy", "error creating frag shader");
      Log.d("adefy", GLES20.glGetShaderInfoLog(fragShader));
    }

    // Attach shaders
    GLES20.glAttachShader(shader, vertShader);
    GLES20.glAttachShader(shader, fragShader);

    // Link and use the program
    GLES20.glLinkProgram(shader);
    GLES20.glGetProgramiv(shader, GLES20.GL_LINK_STATUS, compileStatus, 0);

    if(compileStatus[0] == 0) {
      Log.d("adefy", "error linking");
      Log.d("adefy", GLES20.glGetProgramInfoLog(shader));
    }

    return shader;
  }

  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {

    GLES20.glViewport(0, 0, width, height);

    // Set ortho projection
    Matrix.orthoM(projection, 0, 0, width, 0, height, -10, 10);

    screenW = width;
    screenH = height;
  }

  @Override
  public void onDrawFrame(GL10 unused) {

    // Keep track of frame time
    long startTime = System.currentTimeMillis();

    GLES20.glClearColor(clearCol.x, clearCol.y, clearCol.z, 1.0f);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    for(Actor a : Renderer.actors) {

      if(!a.getMaterialName().equals(material)) {
        GLES20.glUseProgram(a.getMaterial().getShader());
        material = a.getMaterialName();
      }

      a.draw();
    }

    long endTime = System.currentTimeMillis();

    // Ensure at most 60 FPS
    if(endTime - startTime < targetFrameTime) {
      try {
        Thread.sleep(targetFrameTime - (endTime - startTime));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public static float[] getProjection() {
    return projection;
  }

  public static int getScreenW() { return screenW; }
  public static int getScreenH() { return screenH; }

  // Target FPS manipulation
  public void setTargetFPS(int fps) {
    this.targetFPS = fps;
    this.targetFrameTime = 1000 / fps;
  }

  public int getTargetFPS() { return targetFPS; }
}
