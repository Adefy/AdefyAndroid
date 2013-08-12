package com.sit.adefy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
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
  JSONObject jsonObj = new JSONObject();
  JSONArray textureArray = new JSONArray();
  public static int [] bitmapTexture;

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
  }

  public void refreshTextures(GL10 gl, Bitmap textureBitmap, String folderName)  {

    BitmapFactory tempTexture = new BitmapFactory();
    String tempString = "";
    for (int i=0; i<textureArray.length(); i++)  {

      try {
        tempString = textureArray.getString(i);
      } catch (JSONException e) {
        e.printStackTrace();
      }
      tempTexture.decodeFile(this.getCacheDir() + "/" + folderName + "/textures/" + tempString);

      gl.glGenTextures(1, bitmapTexture, 0);
      gl.glBindTexture(GL10.GL_TEXTURE_2D, bitmapTexture[i]);

      textureBitmap.recycle();
    }
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
}
