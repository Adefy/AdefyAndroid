package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//
// Provides:
//   createActor(String verts) -> Num id
//   attachTexture(String texture, Num w, Num h, Num x, Num y, Num angle, Num id) -> Bool success
//   removeAttachment(Num id) -> Bool success
//   setAttachmentVisiblity(Bool visible, Num id) -> Bool success
//   setActorLayer(Num layer, Num id) -> Bool success
//   setActorPhysicsLayer(Num layer, Num id) -> Bool success
//   updateVertices(String verts, Num id) -> Bool success
//   getVertices(Num id) -> String verts
//   destroyActor(Num id) -> Bool success
//   setPhysicsVertices(String verts, Num id) -> Bool success
//   setRenderMode(Num mode, Num id) -> Bool success
//   setActorPosition(Num x, Num y, Number id) -> Bool success
//   getActorPosition(Num id) -> String position
//   setActorRotation(Num angle, Num id, Boolean radians) -> Bool success
//   getActorRotation(Num id, Boolean radians) -> Num angle
//   setActorColor(Num r, Num g, Num b, Num id) -> Bool success
//   getActorColor(Num id) -> String color
//   enableActorPhysics(Num mass, Num friction, Num elasticity, Num id) -> Bool success
//   destroyPhysicsBody(Num id) -> Bool success

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.objects.Actor;
import com.sit.adefy.objects.Color3;

import org.jbox2d.common.Vec2;

// For full, proper documentation, check the AWGL implementation
public class JSActorInterface {

  private static int nextID = 0;
  public static int getNextID() { return nextID++; }
  private AdefyRenderer renderer;

  public JSActorInterface(AdefyRenderer renderer) {
    this.renderer = renderer;
  }

  private Actor findActor(int id) {
    for(Actor a : renderer.actors) {
      if(a.getId() == id) { return a; }
    }

    return null;
  }

  @JavascriptInterface
  public int createActor(String verts, String texVerts) {

    // Generate vert array
    // Verts are seperated by commas
    String[] vertsArray = verts.split(",");
    float[] _verts = new float[vertsArray.length];

    // Convert to floats
    for(int i = 0; i < vertsArray.length; i++) {

      String vert = vertsArray[i];

      if(i == 0) { vert = vertsArray[i].substring(1); }
      else if(i == vertsArray.length - 1) {
        vert = vertsArray[i].substring(0, vertsArray[i].length() - 1);
      }

      _verts[i] = Float.parseFloat(vert);
    }

    // Ship actor
    int id = getNextID();
    new Actor(renderer, id, _verts);

    return id;
  }

  @JavascriptInterface
  public boolean destroyActor(int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.destroy();

    return true;
  }

  @JavascriptInterface
  public boolean attachTexture(String name, float w, float h, float x, float y, float angle, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    boolean queue = false;

    // Esure the renderer has the texture loaded
    if(!renderer.textureExists(name)) {
      queue = true;
    }

    Actor attachment = a.attachTexture(name, w, h, x, y, angle, !queue);

    if(queue) {
      renderer.queueTextureSet(attachment, name);
    }

    return true;
  }

  @JavascriptInterface
  public boolean removeAttachment(int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    return a.removeAttachment();
  }

  @JavascriptInterface
  public boolean setAttachmentVisiblity(boolean visible, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    return a.setAttachmentVisibility(visible);
  }

  @JavascriptInterface
  public boolean setActorLayer(int layer, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.setLayer(layer);
    return true;
  }

  @JavascriptInterface
  public boolean setActorPhysicsLayer(int layer, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.setPhysicsLayer(layer);
    return true;
  }

  @JavascriptInterface
  public boolean setRenderMode(int mode, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.renderMode = mode;

    return true;
  }

  @JavascriptInterface
  public boolean setPhysicsVertices(String verts, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    if(verts == null) {
      a.setPhysicsVertices(null);
    } else if(verts.length() == 0) {
      a.setPhysicsVertices(null);
    }

    // Parse
    String[] vertsArray = verts.split(",");
    float[] _verts = new float[vertsArray.length];

    // Convert to floats
    for(int i = 0; i < vertsArray.length; i++) {

      String vert = vertsArray[i];

      if(i == 0) { vert = vertsArray[i].substring(1); }
      else if(i == vertsArray.length - 1) {
        vert = vertsArray[i].substring(0, vertsArray[i].length() - 1);
      }

      _verts[i] = Float.parseFloat(vert);
    }

    a.setPhysicsVertices(_verts);

    return true;
  }

  @JavascriptInterface
  public boolean updateVertices(String verts, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    // Parse
    String[] vertsArray = verts.split(",");
    float[] _verts = new float[vertsArray.length];

    // Convert to floats
    for(int i = 0; i < vertsArray.length; i++) {

      String vert = vertsArray[i];

      if(i == 0) { vert = vertsArray[i].substring(1); }
      else if(i == vertsArray.length - 1) {
        vert = vertsArray[i].substring(0, vertsArray[i].length() - 1);
      }

      _verts[i] = Float.parseFloat(vert);
    }

    a.updateVertices(_verts);

    return true;
  }

  @JavascriptInterface
  public String getVertices(int id) {
    Actor a = findActor(id);
    if (a == null) { return ""; }

    StringBuilder sb = new StringBuilder();
    sb.append('[');
    float[] verts = a.getVertices();

    for(int i = 0; i < verts.length; i++) {
      sb.append('"').append(verts[i]).append('"');
      if(i < verts.length - 1) { sb.append(','); }
    }

    sb.append(']');

    return sb.toString();
  }

  // Set actor position using id
  // Fails with false if actor is not found
  @JavascriptInterface
  public boolean setActorPosition(float x, float y, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.setPosition(new Vec2(x, y));

    return true;
  }

  // Get actor position using id
  // Returns position as a JSON representation of a primitive {x, y} object!
  //
  // Returns an empty string if the actor was not found
  @JavascriptInterface
  public String getActorPosition(int id) {
    Actor a = findActor(id);
    if (a == null) { return ""; }

    Vec2 v = a.getPosition();
    return "{ x: \"" + v.x + "\", y: \"" + v.y + "\" }";
  }

  // Set actor texture by name, fails with false if the actor is not found
  @JavascriptInterface
  public boolean setActorTexture(String name, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    // Esure the renderer has the texture loaded
    if(!renderer.textureExists(name)) {
      Log.d("adefy", "Queueing texture set for " + name);
      renderer.queueTextureSet(a, name);
      return false;
    }

    a.setTexture(name);
    return true;
  }

  // Set actor rotation in radians, fails with false if the actor is not found
  @JavascriptInterface
  public boolean setActorRotation(float angle, int id, boolean radians) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    if(radians) {
      angle = angle * 57.2957795f; // Convert to degrees
    }

    a.setRotation(angle);

    return true;
  }

  // Get actor rotation in radians or degrees using an id
  // Fails with null
  @JavascriptInterface
  public float getActorRotation(int id, boolean radians) {
    Actor a = findActor(id);
    if (a == null) { return 0.000001f; }

    if(radians) {
      return a.getRotation() * 0.0174532925f;
    } else {
      return a.getRotation();
    }
  }

  // Set actor color using component values and id
  // Range for values is 0-255
  // Fails with false
  @JavascriptInterface
  public boolean setActorColor(int r, int g, int b, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.setColor(new Color3(r, g, b));

    return true;
  }

  // Get actor color as a JSON triple using id, fails with empty string
  @JavascriptInterface
  public String getActorColor(int id) {
    Actor a = findActor(id);
    if (a == null) { return ""; }

    Color3 col = a.getColor();
    if(col == null) {
      return "{ r: \"255\", g: \"255\", b: \"255\" }";
    } else {
      return "{ r: \"" + col.r + "\", g: \"" + col.g + "\", b: \"" + col.b + "\" }";
    }
  }

  // Enable actor physics using id, fails with false if actor is not found
  // TODO: Mass is currently capped at 1.0, and truncated. Add better handling
  @JavascriptInterface
  public boolean enableActorPhysics(float mass, float friction, float elasticity, int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    if(mass > 1.0f) { mass = 1.0f; }

    a.createPhysicsBody(mass, friction, elasticity);
    return true;
  }

  // Destroy physics body using id, fails with false if actor is not found
  @JavascriptInterface
  public boolean destroyPhysicsBody(int id) {
    Actor a = findActor(id);
    if (a == null) { return false; }

    a.destroyPhysicsBody();
    return true;
  }
}
