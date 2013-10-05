package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.sit.adefy.objects.Actor;

import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.Timer;

public class Renderer implements GLSurfaceView.Renderer {

  private static float PPM = 128.0f;
  public static Vec2 screenToWorld(Vec2 cords) { return new Vec2(cords.x / PPM, cords.y / PPM); }
  public static Vec2 worldToScreen(Vec2 cords) { return new Vec2(cords.x * PPM, cords.y * PPM); }
  public static float getPPM() { return PPM; }
  public static float getMPP() { return 1.0f / PPM; }

  public static ArrayList<Actor> actors = new ArrayList<Actor>();
  public static Vec3 clearColor = new Vec3(0.0f, 0.0f, 0.0f);

  private static int nextId = 0;
  private static int screenW = 0;
  private static int screenH = 0;
  private int targetFPS = 60;
  private int targetFrameTime = 16;

  // Target FPS manipulation
  public void setTargetFPS(int fps) {
    this.targetFPS = fps;
    this.targetFrameTime = 1000 / fps;
  }

  public int getTargetFPS() { return targetFPS; }

  private static String vertShaderCode =
      "attribute vec3 Position;" +
          "uniform mat4 Projection;" +
          "uniform mat4 ModelView;" +
          "void main() {" +
          "  mat4 mvp = Projection * ModelView;" +
          "  gl_Position = mvp * vec4(Position.xyz, 1);" +
          "}\n";

  private static String fragShaderCode =
      "precision mediump float;" +
          "uniform vec4 Color;" +
          "void main() {" +
          "  gl_FragColor = Color;" +
          "}\n";

  private static int shaderProg;
  private static float[] projection = new float[16];

  public static int getShaderProg() { return shaderProg; }

  public static Timer animationTimer = new Timer();

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {

    // Create program
    shaderProg = GLES20.glCreateProgram();

    // Compile shaders
    int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
    GLES20.glShaderSource(vertShader, vertShaderCode);
    GLES20.glCompileShader(vertShader);

    int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
    GLES20.glShaderSource(fragShader, fragShaderCode);
    GLES20.glCompileShader(fragShader);

    // Attach shaders
    GLES20.glAttachShader(shaderProg, vertShader);
    GLES20.glAttachShader(shaderProg, fragShader);

    // Link and use the program
    GLES20.glLinkProgram(shaderProg);
    GLES20.glUseProgram(shaderProg);

    // Normal stuff
    // TODO: Convert clearColor to a Color3
    GLES20.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
  }

  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {

    GLES20.glViewport(0, 0, width, height);

    // Set ortho projection
    int projectionHandle = GLES20.glGetUniformLocation(shaderProg, "Projection");
    Matrix.orthoM(projection, 0, 0, width, 0, height, -10, 10);
    GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projection, 0);

    screenW = width;
    screenH = height;
  }

  public static int getScreenW() { return screenW; }
  public static int getScreenH() { return screenH; }

  ////
  ////
  ////

  @Override
  public void onDrawFrame(GL10 unused) {

    // Keep track of frame time
    long startTime = System.currentTimeMillis();

    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    for(Actor a : Renderer.actors) {
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
}
