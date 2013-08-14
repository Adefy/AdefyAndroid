package com.sit.adefy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.util.Log;

import com.sit.adefy.objects.Actor;
import com.sit.adefy.physics.PhysicsEngine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.microedition.khronos.opengles.GL10;

public class AdefyScene extends Activity {

  private static ArrayList<Actor> actors = new ArrayList<Actor>();
  private static Renderer renderer = null;
  private static PhysicsEngine psyx = null;
  private JSONObject jsonObj = new JSONObject();
  private JSONArray textureArray = new JSONArray();
  private static ArrayList<TextureDescriptor> textures = new ArrayList<TextureDescriptor>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    renderer = new Renderer();
    psyx = new PhysicsEngine();

    GLSurfaceView mView = new GLSurfaceView(this);
    mView.setEGLContextClientVersion(2);
    mView.setRenderer(renderer);

    setContentView(mView);

    Intent launchedIntent = getIntent();
    String folderName = launchedIntent.getStringExtra("folder"); //should be adefyFolder
    File jsonFile = new File(this.getCacheDir() + "/" + folderName + "/package.json");
    String jsonText = "";
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(jsonFile.toString()));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    try {

      String crtLine;

      while((crtLine = br.readLine()) != null)
        jsonText += crtLine;
    } catch (IOException e) {
      e.printStackTrace();
    }

    String texturesPath = "";

    Log.v("Adefy", "This is what the file contains: " + jsonText);

    try {
      jsonObj = new JSONObject(jsonText);
      Log.v("Adefy", "Content of JSON key:" + jsonObj.getJSONArray("textures"));
      textureArray = jsonObj.getJSONArray("textures");
      Log.v("Adefy", "path to textures:" + this.getCacheDir() + "/" + folderName + "/textures");

    }
    catch (JSONException e){
      e.printStackTrace();
      Log.v("Adefy", "JSON EXCEPTION");
    }

    refreshTextures("adefyFolder");
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

  public void unzipArchive(String path, String dir) throws IOException {

    // Get streams
    FileInputStream fin = new FileInputStream(this.getCacheDir() + "/" + path);
    ZipInputStream zin = new ZipInputStream(fin);
    ZipEntry ze;

    // Operate on each entry
    while((ze = zin.getNextEntry()) != null) {

      Log.v("Adefy", "Decompressing downloaded ad resources " + ze.getName() + "->" + this.getCacheDir() + "/" + dir + "/" + ze.getName());

      if(ze.isDirectory()) {
        File dirCheck = new File(this.getCacheDir() + "/" + dir + "/" + ze.getName());

        // Create directory if necessary
        dirCheck.mkdirs(); // TODO: Fix
      } else {

        // Write file
        File fout_dirCreation = new File(this.getCacheDir() + "/" + dir);
        fout_dirCreation.mkdirs();

        FileOutputStream fout = new FileOutputStream(this.getCacheDir() + "/" + dir + "/" + ze.getName());

        int c;
        while((c = zin.read()) != -1) {
          fout.write(c);
        }

        zin.closeEntry();
        fout.close();
      }
    }
  }

  public static ArrayList<Actor> getActors() { return actors; }
  public static PhysicsEngine getPhysicsEngine() { return psyx; }
  public static Renderer getRenderer() { return renderer; }

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
