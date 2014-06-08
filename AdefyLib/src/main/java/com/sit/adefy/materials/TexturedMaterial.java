package com.sit.adefy.materials;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.util.Log;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.objects.Color3;

import java.nio.FloatBuffer;

public class TexturedMaterial extends Material {

  public static String name = "textured";

  // A bit of a strange name, but it fits. True if we were just used to draw something.
  // Skips some of the draw routine.
  public static boolean justUsed = false;
  public static int previousTexture;

  // Shader var handles
  private static int handle_Position;
  private static int handle_TexCoord;
  private static int handle_ModelView;
  private static int handle_Projection;
  private static int handle_TexSampler;
  private static int handle_UVScale;
  private static int handle_Color;
  private static int handle_HasTexture;
  private static int handle_Layer;

  private static int shader;

  protected static String vertCode =
      "attribute vec3 Position;" +
      "attribute vec2 aTexCoord;" +

      "uniform mat4 Projection;" +
      "uniform mat4 ModelView;" +
      "uniform vec2 UVScale;" +
      "uniform int Layer;" +

      "varying highp vec2 vTexCoord;" +

      "void main() {" +
      "  gl_Position = Projection * ModelView * vec4(Position.xy, Layer, 1);" +
      "  vTexCoord = aTexCoord * UVScale;" +
      "}\n";

  protected static String fragCode =
      "precision highp float;" +

      "varying highp vec2 vTexCoord;" +
      "uniform sampler2D uTexture;" +
      "uniform vec4 Color;" +
      "uniform int HasTexture;" +

      "void main() {" +
      "  if(HasTexture == 1) {" +
      "    gl_FragColor = texture2D(uTexture, vTexCoord);" +
      "  } else {" +
      "    gl_FragColor = Color;" +
      "  }" +
      "}\n";

  private int[] textureHandle;
  private float uScale = 1.0f;
  private float vScale = 1.0f;
  private Color3 color;
  private static float[] glColor = new float[4];

  public TexturedMaterial() {
    super(name);

    this.textureHandle = new int[1];
    this.textureHandle[0] = 0;

    this.uScale = 1.0f;
    this.vScale = 1.0f;

    this.color = new Color3(255, 255, 255);
  }

  public void setTextureHandle(int[] textureHandle) { this.textureHandle = textureHandle; }
  public void setUScale(float scale) { uScale = scale; }
  public void setVScale(float scale) { vScale = scale; }
  public void setColor(Color3 color) { this.color = color; }

  public int getShader() { return shader; }
  public Color3 getColor() { return color; }

  // Called by our renderer when we can create our shader
  public static void buildShader() {
    shader = AdefyRenderer.buildShader(vertCode, fragCode);

    handle_Position = GLES20.glGetAttribLocation(shader, "Position");
    handle_TexCoord = GLES20.glGetAttribLocation(shader, "aTexCoord");
    handle_UVScale = GLES20.glGetUniformLocation(shader, "UVScale");
    handle_ModelView = GLES20.glGetUniformLocation(shader, "ModelView");
    handle_Projection = GLES20.glGetUniformLocation(shader, "Projection");
    handle_TexSampler = GLES20.glGetUniformLocation(shader, "uTexture");
    handle_Color = GLES20.glGetUniformLocation(shader, "Color");
    handle_HasTexture = GLES20.glGetUniformLocation(shader, "HasTexture");
  }

  public static void initialSetup() {
    TexturedMaterial.justUsed = true;

    GLES20.glUniformMatrix4fv(handle_Projection, 1, false, AdefyRenderer.getProjection(), 0);

    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    GLES20.glEnable(GLES20.GL_BLEND);

    GLES20.glEnableVertexAttribArray(handle_Position);
    GLES20.glEnableVertexAttribArray(handle_TexCoord);

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
  }

  public void draw(FloatBuffer vertBuffer, FloatBuffer texBuffer, int vertCount, int mode, float[] modelView) {

    int glError = GLES20.glGetError();

    if(glError != 0) {
      Log.d("Adefy", "GL error: " + glError);
    }

    if(!TexturedMaterial.justUsed) {
      TexturedMaterial.initialSetup();
    }

    // Update texture if needed
    if(textureHandle[0] != previousTexture && textureHandle[0] != 0) {

      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
      GLES20.glUniform1i(handle_TexSampler, 0);
      GLES20.glUniform2f(handle_UVScale, uScale, vScale);

      previousTexture = textureHandle[0];
    }

    color.toFloatArray(glColor);

    if(textureHandle[0] == 0) {
      GLES20.glUniform1i(handle_HasTexture, 0);
    } else {
      GLES20.glUniform1i(handle_HasTexture, 1);
    }

    GLES20.glUniform4fv(handle_Color, 1, glColor, 0);
    GLES20.glUniformMatrix4fv(handle_ModelView, 1, false, modelView, 0);
    GLES20.glVertexAttribPointer(handle_Position, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
    GLES20.glVertexAttribPointer(handle_TexCoord, 2, GLES20.GL_FLOAT, false, 0, texBuffer);

    // Draw!
    GLES20.glDrawArrays(mode, 0, vertCount);
  }

  // Called by other materials if we were just drawing
  public static void postFinalDraw() {
    GLES20.glDisableVertexAttribArray(handle_TexCoord);
    GLES20.glDisableVertexAttribArray(handle_Position);
    GLES20.glDisable(GLES20.GL_BLEND);

    TexturedMaterial.justUsed = false;
  }
}
