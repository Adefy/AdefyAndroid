package com.sit.adefy.actors;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.objects.Color3;
import com.sit.adefy.objects.Texture;

import java.util.ArrayList;

public class TextActor extends RectangleActor {

  private Paint textPaint;
  private Paint bgPaint;

  private String text;
  private int textSize;
  private Color3 fgColor;

  private Rect textPaintBounds;

  // Keep track of all instantiated text actors, so we can refresh their textures when needed
  private static ArrayList<TextActor> textActorList = new ArrayList<TextActor>();

  public TextActor(AdefyRenderer renderer, int _id, String text, int textSize, Color3 fg) {
    super(renderer, _id, 0, 0);

    this.text = text;
    this.textSize = textSize;
    this.fgColor = fg;

    calculateDimensions();
    updateVertices(generateVertexSet());
    createPaintObjects();
    generateTexture();

    TextActor.textActorList.add(this);
  }

  private void createPaintObjects() {
    textPaint = new Paint();
    textPaint.setTextSize(textSize);
    textPaint.setAntiAlias(true);
    textPaint.setARGB(255, fgColor.r, fgColor.g, fgColor.b);
    textPaint.setTextAlign(Paint.Align.LEFT);

    bgPaint = new Paint();
    bgPaint.setARGB(0, 0, 0, 0);
  }

  private void calculateDimensions() {
    textPaintBounds = new Rect();
    textPaint.getTextBounds(text, 0, text.length(), textPaintBounds);

    width = textPaintBounds.width();
    height = textPaintBounds.height();

    height += textSize * 0.4f; // Add vertical padding for lowercase hanging letters

    width = (float)Math.pow(2, Math.ceil(Math.log(width) / Math.log(2)));
    height = (float)Math.pow(2, Math.ceil(Math.log(height) / Math.log(2)));

  }

  private void generateTexture() {
    Bitmap canvasBitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_4444);
    Canvas canvas = new Canvas(canvasBitmap);
    canvas.drawRect(0, 0, width, height, bgPaint);
    canvas.drawText(text, 0, textPaintBounds.height(), textPaint);

    String textureName = "textActor_texture_" + getId();
    Texture texture = getRenderer().createTextureFromBitmap(textureName, canvasBitmap);

    setTexture(texture.getName());
  }

  public static void reloadTextures() {

    for(TextActor actor : textActorList) {
      actor.generateTexture();
    }
  }
}
