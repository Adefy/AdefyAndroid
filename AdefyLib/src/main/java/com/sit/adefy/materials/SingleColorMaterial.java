package com.sit.adefy.materials;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.util.Log;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.objects.Color3;

import java.nio.FloatBuffer;

// Renders an actor in a single color
public class SingleColorMaterial extends Material {

  public static String name = "single_color";

  // A bit of a strange name, but it fits. True if we were just used to draw something.
  // Skips some of the draw routine.
  public static boolean justUsed = false;

  private Color3 color;

  // Shader var handles
  private static int positionHandle;
  private static int colorHandle;
  private static int modelHandle;
  private static int projectionHandle;

  private static float[] glColor = new float[3];

  private static int shader;

  protected static String vertCode =
      "attribute vec3 Position;" +

          "uniform mat4 Projection;" +
          "uniform mat4 ModelView;" +

          "void main() {" +
          "  mat4 mvp = Projection * ModelView;" +
          "  gl_Position = mvp * vec4(Position.xyz, 1);" +
          "}\n";

  protected static String fragCode =
      "precision mediump float;" +

          "uniform vec4 Color;" +

          "void main() {" +
          "  gl_FragColor = Color;" +
          "}\n";

  // Initialized with a color, components 0-255
  public SingleColorMaterial() { this(new Color3(255, 255, 255)); }
  public SingleColorMaterial(Color3 color) {
    super(name);
    this.color = color;
  }

  public int getShader() {
    return shader;
  }

  // Called by our renderer when we can create our shader
  public static void buildShader() {
    shader = AdefyRenderer.buildShader(vertCode, fragCode);
    positionHandle = GLES20.glGetAttribLocation(shader, "Position");
    colorHandle = GLES20.glGetUniformLocation(shader, "Color");
    modelHandle = GLES20.glGetUniformLocation(shader, "ModelView");
    projectionHandle = GLES20.glGetUniformLocation(shader, "Projection");
  }

  public void setColor(Color3 color) {
    this.color = color;
  }

  public Color3 getColor() {
    return color;
  }

  public void draw(FloatBuffer vertBuffer, int vertCount, int mode, float[] modelView) {

    try {

      // Pull in color, store in static float[] array to prevent allocation
      color.toFloatArray(glColor);

      // Set up handles
      GLES20.glUniformMatrix4fv(projectionHandle, 1, false, AdefyRenderer.getProjection(), 0);
      GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);
      GLES20.glUniform4fv(colorHandle, 1, glColor, 0);

      GLES20.glEnableVertexAttribArray(positionHandle);
      GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);

      if(!SingleColorMaterial.justUsed) {
        if(TexturedMaterial.justUsed) { TexturedMaterial.postFinalDraw(); }
        TexturedMaterial.justUsed = false;
        SingleColorMaterial.justUsed = true;
      }

      // Draw!
      GLES20.glDrawArrays(mode, 0, vertCount);

      GLES20.glDisableVertexAttribArray(positionHandle);

    } catch (Exception e) {
      Log.d("Adefy", "SCM Exception");
      e.printStackTrace();
    }
  }

  // Called by other materials if we were just drawing
  public static void postFinalDraw() {
    GLES20.glDisableVertexAttribArray(positionHandle);
  }
}
