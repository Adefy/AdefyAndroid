package com.sit.adefy.materials;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.util.Log;

import com.sit.adefy.AdefyRenderer;

import java.nio.FloatBuffer;

public class TexturedMaterial extends Material {

  public static String name = "textured";

  // A bit of a strange name, but it fits. True if we were just used to draw something.
  // Skips some of the draw routine.
  public static boolean justUsed = false;
  public static int previousTexture;

  // Shader var handles
  private static int positionHandle;
  private static int texCoordHandle;
  private static int modelHandle;
  private static int projectionHandle;
  private static int texSamplerHandle;
  private static int uvScaleHandle;

  private static int shader;

  protected static String vertCode =
      "attribute vec3 Position;" +
          "attribute vec2 aTexCoord;" +
          "attribute vec2 aUVScale;" +

          "uniform mat4 Projection;" +
          "uniform mat4 ModelView;" +

          "varying highp vec2 vTexCoord;" +
          "varying highp vec2 vUVScale;" +

          "void main() {" +
          "  gl_Position = Projection * ModelView * vec4(Position.xyz, 1);" +
          "  vTexCoord = aTexCoord;" +
          "  vUVScale = aUVScale;" +
          "}\n";

  protected static String fragCode =
      "precision highp float;" +

      "varying highp vec2 vTexCoord;" +
      "uniform sampler2D uTexture;" +
      "varying highp vec2 vUVScale;" +

      "void main() {" +
      "  vec4 baseColor = texture2D(uTexture, vTexCoord * vUVScale);" +
      "  if(baseColor.rgb == vec3(1.0, 0.0, 1.0))" +
      "    discard;" +
      "  gl_FragColor = baseColor;" +
      "}\n";

  private int[] textureHandle = null;
  private float uScale = 1.0f;
  private float vScale = 1.0f;

  public TexturedMaterial(int[] textureHandle, float uScale, float vScale) {
    super(name);
    this.textureHandle = textureHandle;
    this.uScale = uScale;
    this.vScale = vScale;
  }

  public void setTextureHandle(int[] textureHandle) {
    this.textureHandle = textureHandle;
  }
  public void setUScale(float scale) { uScale = scale; }
  public void setVScale(float scale) { vScale = scale; }

  public int getShader() {
    return shader;
  }

  // Called by our renderer when we can create our shader
  public static void buildShader() {
    shader = AdefyRenderer.buildShader(vertCode, fragCode);
    positionHandle = GLES20.glGetAttribLocation(shader, "Position");
    texCoordHandle = GLES20.glGetAttribLocation(shader, "aTexCoord");
    uvScaleHandle = GLES20.glGetAttribLocation(shader, "aUVScale");
    modelHandle = GLES20.glGetUniformLocation(shader, "ModelView");
    projectionHandle = GLES20.glGetUniformLocation(shader, "Projection");
    texSamplerHandle = GLES20.glGetUniformLocation(shader, "uTexture");
  }

  public void draw(FloatBuffer vertBuffer, FloatBuffer texBuffer, int vertCount, int mode, float[] modelView) {

    if(!TexturedMaterial.justUsed) {
      if(SingleColorMaterial.justUsed) { SingleColorMaterial.postFinalDraw(); }
      SingleColorMaterial.justUsed = false;
      TexturedMaterial.justUsed = true;

      GLES20.glUniformMatrix4fv(projectionHandle, 1, false, AdefyRenderer.getProjection(), 0);

      GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
      GLES20.glEnable(GLES20.GL_BLEND);

      GLES20.glEnableVertexAttribArray(positionHandle);
      GLES20.glEnableVertexAttribArray(texCoordHandle);

      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    if(textureHandle[0] != previousTexture) {

      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
      GLES20.glUniform1i(texSamplerHandle, 0);
      GLES20.glVertexAttrib2f(uvScaleHandle, uScale, vScale);

      previousTexture = textureHandle[0];
    }

    GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);

    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
    GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);

    // Draw!
    GLES20.glDrawArrays(mode, 0, vertCount);
  }

  // Called by other materials if we were just drawing
  public static void postFinalDraw() {
    GLES20.glDisableVertexAttribArray(texCoordHandle);
    GLES20.glDisableVertexAttribArray(positionHandle);
    GLES20.glDisable(GLES20.GL_BLEND);
  }
}
