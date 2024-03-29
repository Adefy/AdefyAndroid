package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sit.adefy.actors.TextActor;
import com.sit.adefy.animations.Animation;
import com.sit.adefy.materials.TexturedMaterial;
import com.sit.adefy.actors.Actor;
import com.sit.adefy.objects.Texture;
import com.sit.adefy.objects.TextureSetQueueItem;
import com.sit.adefy.physics.PhysicsEngine;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;

public class AdefyRenderer implements GLSurfaceView.Renderer {

  private static float PPM = 128.0f;
  public static float getPPM() { return PPM; }
  public static float getMPP() { return 1.0f / PPM; }

  public static void screenToWorld(Vector2 cords, Vector2 store) {
    store.x = cords.x / PPM;
    store.y = cords.y / PPM;
  }

  public static void worldToScreen(Vector2 cords, Vector2 store) {
    store.x = cords.x * PPM;
    store.y = cords.y * PPM;
  }

  public final ArrayList<Actor> actors = new ArrayList<Actor>();
  public final ArrayList<Animation> animations = new ArrayList<Animation>();
  private ArrayList<Texture> textures = new ArrayList<Texture>();

  private static int screenW = 0;
  private static int screenH = 0;
  private int targetFPS = 60;
  private int targetFrameTime = 1000 / 60;

  private static float[] projection = new float[16];
  public static Timer animationTimer = new Timer();
  public static Vector3 clearCol = null;
  private String material = "";

  private JSONArray textureJSON = null;
  private String texturePath = null;
  private boolean textureLoadQueued;

  public static float camX;
  public static float camY;

  private PhysicsEngine psyx = null;

  public AdefyRenderer() {
    super();

    psyx = new PhysicsEngine();

    TexturedMaterial.justUsed = false;
    TexturedMaterial.previousTexture = -1;
  }

  public PhysicsEngine getPsyx() {
    return psyx;
  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {

    // Use our clear color
    animationTimer.cancel();
    animationTimer = new Timer();
    clearCol = new Vector3(0.0f, 0.0f, 0.0f);
    GLES20.glClearColor(clearCol.x, clearCol.y, clearCol.z, 1.0f);

    // Build shaders
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

    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glDepthFunc(GLES20.GL_LEQUAL);

    // Set ortho projection
    Matrix.orthoM(projection, 0, 0, width, 0, height, -100, 100);

    reloadTextures();

    screenW = width;
    screenH = height;
  }

  // Texture path needs to be complete, including cache dir!
  public void setTextureInfo(JSONArray textureJSON, String texturePath) {
    this.textureJSON = textureJSON;
    this.texturePath = texturePath;
    this.textureLoadQueued = true;
  }

  public void queueTextureSet(Actor a, String name) {

    TextureSetQueueItem item = new TextureSetQueueItem(a, name);

    synchronized (textureSetQueue) {
      textureSetQueue.add(item);
    }
  }

  private final ArrayList<TextureSetQueueItem> textureSetQueue = new ArrayList<TextureSetQueueItem>();

  @Override
  public void onDrawFrame(GL10 unused) {

    if(textureLoadQueued) { reloadTextures(); }

    // Keep track of frame time
    long frameStartTime = System.currentTimeMillis();

    // Step animations
    for(int i = 0; i < animations.size(); i++) {
      Animation animation = animations.get(i);

      if(animation.isActive()) {
        animation.rendererAnimateStep(frameStartTime);

        // If the animation is now inactive, it was just deleted, so backtrack one spot.
        if(!animation.isActive()) {
          i--;
        }
      }
    }

    // This crashes sometimes when closing the ad. I don't have time to figure out how to deal with
    // an invalid GL context in the instant before close, so we'll just wrap it for now.
    try {
      // Clear screen
      GLES20.glClearColor(clearCol.x, clearCol.y, clearCol.z, 1.0f);
      GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

      // Render actors
      synchronized (actors) {
        for (int i = 0; i < actors.size(); i++) {
          Actor renderObject = actors.get(i);

          if (renderObject.mVisible) {
            if (renderObject.hasAttachment()) {

              // Render attachment
              if (renderObject.getAttachment().mVisible) {

                // Store for now to update state
                Actor attachment = renderObject.getAttachment();
                attachment.setPosition(renderObject);
                attachment.setRotation(renderObject.getRotation());

                // Switch render subject
                renderObject = attachment;
              }
            }

            // Render either
            if (!renderObject.getMaterialName().equals(material)) {
              GLES20.glUseProgram(renderObject.getMaterial().getShader());
              material = renderObject.getMaterialName();
            }

            renderObject.draw();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    long endTime = System.currentTimeMillis();

    // Ensure at most 60 FPS
    if(endTime - frameStartTime < targetFrameTime) {
      try {
        Thread.sleep(targetFrameTime - (endTime - frameStartTime));
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

  // Checks if we have a texture loaded by name
  public boolean textureExists(String name) {
    for(int i = 0; i < textures.size(); i++) {
      if(textures.get(i).getName().equals(name)) {
        return true;
      }
    }

    return false;
  }

  public int[] getTextureHandle(String name) {
    for(int i = 0; i < textures.size(); i++) {
      if(textures.get(i).getName().equals(name)) {
        return textures.get(i).getHandle();
      }
    }

    return null;
  }

  public Texture getTexture(String name) {
    for(int i = 0; i < textures.size(); i++) {
      if(textures.get(i).getName().equals(name)) {
        return textures.get(i);
      }
    }

    return null;
  }

  public void clearTextures() {
    textures.clear();
  }

  // Creates bitmaps and uploads textures according to textureJSON and texturePath
  private void reloadTextures() {
    if(textureJSON == null) { return; }

    textures.clear();

    reloadManifestTextures();
    TextActor.reloadTextures();

    textureLoadQueued = false;
  }

  private void reloadManifestTextures() {
    for (int i = 0; i < textureJSON.length(); i++)  {

      try {

        // Only supports single image-per-texture loading for now!
        JSONObject tex = textureJSON.getJSONObject(i);

        String name = tex.getString("name");
        String type = tex.getString("type");
        String compression = tex.getString("compression");
        String path = texturePath + "/" + tex.getString("path");

        loadAndCreateTexture(name, path, type, compression);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public Texture loadAndCreateTexture(String name, String path, String type, String compression) throws IOException {

    Texture texture = null;

    if(!type.equals("image")) {
      Log.d("AdefyRenderer", "Can't load texture, unsupported type: " + type);
    } else if(!compression.equals("none") && !compression.equals("etc1")) {
      Log.d("AdefyRenderer", "Can't load texture, unsupported compression: " + compression);
    } else {

      texture = _newTexture(name);
      _applyTextureOptions();

      // Image, load up
      if(compression.equals("none")) {

        // Create bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        // Check if texture is NPOT
        if((bitmap.getWidth() & (bitmap.getWidth() - 1)) != 0 || (bitmap.getHeight() & (bitmap.getHeight() - 1)) != 0) {

          float originalWidth = bitmap.getWidth();
          float originalHeight = bitmap.getHeight();

          float newWidth = nearestPowerOfTwo(bitmap.getWidth());
          float newHeight = nearestPowerOfTwo(bitmap.getHeight());

          Bitmap paddedBitmap = Bitmap.createBitmap((int)newWidth, (int)newHeight, Bitmap.Config.ARGB_8888);

          Canvas canvas = new Canvas(paddedBitmap);
          canvas.drawARGB(0x00, 0x00, 0x00, 0x00);
          canvas.drawBitmap(
              bitmap,
              0,
              0,
              new Paint(Paint.FILTER_BITMAP_FLAG));

          // Save scale
          texture.clipScaleU = originalWidth / newWidth;
          texture.clipScaleV = originalHeight / newHeight;

          GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, paddedBitmap, 0);

          paddedBitmap.recycle();
          bitmap.recycle();
        } else {

          GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
          bitmap.recycle();
        }

      } else if(compression.equals("etc1")) {

        // Create ETC1
        FileInputStream stream = new FileInputStream(new File(path));
        ETC1Util.loadTexture(GLES20.GL_TEXTURE_2D, 0, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, stream);
        stream.close();
      }

      textures.add(texture);
      applyTextureSets(texture.getName());
    }

    return texture;
  }

  private int nearestPowerOfTwo(int v) {

    v--;
    v |= v >> 1;
    v |= v >> 2;
    v |= v >> 4;
    v |= v >> 8;
    v |= v >> 16;
    v++;

    return v;
  }

  public Texture createTextureFromBitmap(String name, Bitmap bitmap) {

    Texture texture = _newTexture(name);
    _applyTextureOptions();

    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);

    return texture;
  }

  private void _applyTextureOptions() {
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
  }

  private Texture _newTexture(String name) {
    Texture texture = new Texture(name);
    GLES20.glGenTextures(1, texture.getHandle(), 0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getHandle()[0]);

    return texture;
  }

  private void _textureFromETC1File(String path) {}

  private void applyTextureSets(String textureName) {
    synchronized (textureSetQueue) {

      ArrayList<TextureSetQueueItem> appliedSets = new ArrayList<TextureSetQueueItem>();

      for(TextureSetQueueItem request: textureSetQueue) {

        if(request.getName().equals(textureName)) {
          request.apply();
          appliedSets.add(request);
        }
      }

      for(TextureSetQueueItem req : appliedSets) {
        textureSetQueue.remove(req);
      }

      appliedSets.clear();
    }
  }
}
