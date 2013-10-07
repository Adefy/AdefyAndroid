package com.sit.adefy.materials;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;

import com.sit.adefy.Renderer;

import java.nio.FloatBuffer;

public class TexturedMaterial extends Material {

  public static String name = "textured";

  // Shader var handles
  private static int positionHandle;
  private static int texCoordHandle;
  private static int modelHandle;
  private static int projectionHandle;

  private static int shader;

  protected static String vertCode =
      "attribute vec3 Position;" +
          "attribute vec2 aTexCoord;" +

          "uniform mat4 Projection;" +
          "uniform mat4 ModelView;" +

          "varying highp vec2 vTexCoord;" +

          "void main() {" +
          "  gl_Position = Projection * ModelView * vec4(Position.xyz, 1);" +
          "  vTexCoord = aTexCoord;" +
          "}\n";

  protected static String fragCode =
      "precision highp float;" +

      "varying highp vec2 vTexCoord;" +
      "uniform sampler2D uSampler;" +

      "void main() {" +
      "  gl_FragColor = texture2D(uSampler, vTexCoord);" +
      "}\n";

  public TexturedMaterial() {
    super(name);
  }

  public int getShader() {
    return shader;
  }

  // Called by our renderer when we can create our shader
  public static void buildShader() {
    shader = Renderer.buildShader(vertCode, fragCode);
    positionHandle = GLES20.glGetAttribLocation(shader, "Position");
    texCoordHandle = GLES20.glGetAttribLocation(shader, "aTexCoord");
    modelHandle = GLES20.glGetUniformLocation(shader, "ModelView");
    projectionHandle = GLES20.glGetUniformLocation(shader, "Projection");
  }

  public void draw(FloatBuffer vertBuffer, int vertCount, int mode, float[] modelView) {

    // Set up handles
    GLES20.glUniformMatrix4fv(projectionHandle, 1, false, Renderer.getProjection(), 0);
    GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);
    //GLES20.glUniform4fv(colorHandle, 1, color.toFloatArray(), 0);

    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);

    // Draw!
    GLES20.glDrawArrays(mode, 0, vertCount);

    GLES20.glDisableVertexAttribArray(positionHandle);
  }
}
