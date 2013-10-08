package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.js.JSAnimationInterface;
import com.sit.adefy.js.JSEngineInterface;
import com.sit.adefy.physics.PhysicsEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class AdefyScene extends Activity {

  private JSONArray textureArray = new JSONArray();
  private static ArrayList<TextureDescriptor> textures = new ArrayList<TextureDescriptor>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent launchedIntent = getIntent();
    String adName = launchedIntent.getStringExtra("adName");
    String apiKey = launchedIntent.getStringExtra("apiKey");

    // Purely for debugging!
    // TODO: Remove
    String adId = launchedIntent.getStringExtra("adId");

    AdefyView mView = new AdefyView(apiKey, adName, adId, this);
    setContentView(mView);

    // String texturesPath = "";
    //refreshTextures("adefyFolder");
  }

  public void refreshTextures(String folderName)  {

    Log.v("Adefy", "refreshTextures called for the following folder: "  + folderName);

    for (int i = 0; i < textureArray.length(); i++)  {

      try {

        TextureDescriptor textureDescriptor = new TextureDescriptor(textureArray.getString(i));
        GLES20.glGenTextures(1, textureDescriptor.getHandle(), 0);

        Bitmap textureBitmap = BitmapFactory.decodeFile(this.getCacheDir() + "/" + folderName + "/textures/" + textureArray.getString(i));

        Log.v("Adefy", "Loaded bitmap " + textureArray.getString(i) + " size " + (textureBitmap.getByteCount() / 1024) + "kB");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDescriptor.getHandle()[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, textureBitmap, 0);

        textureBitmap.recycle();
        textures.add(textureDescriptor);

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    Log.v("Adefy", "Loaded " + textures.size() + " into GL memory");
  }

  // Make a class holding a String name and an int[1] handle
  private class TextureDescriptor {

    private String path;
    private int[] handle = new int[1];

    public TextureDescriptor(String _path) {
      path = _path;
    }

    public String getPath() { return path; }
    public int[] getHandle() { return handle; }
  }
}
