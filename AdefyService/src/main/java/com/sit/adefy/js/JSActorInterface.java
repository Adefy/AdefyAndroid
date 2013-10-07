package com.sit.adefy.js;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//
// Provides:
//   createActor(String verts) -> Number id
//   updateVertices(String verts, Number id) -> Bool success
//   getVertices(Number id) -> String verts
//   destroyActor(Number id) -> Bool success
//   setPhysicsVertices(String verts, Number id) -> Bool success
//   setRenderMode(Number mode, Number id) -> Bool success
//   setActorPosition(Number x, Number y, Number id) -> Bool success
//   getActorPosition(Number id) -> String position
//   setActorRotation(Number angle, Number id, Boolean radians) -> Bool success
//   getActorRotation(Number id, Boolean radians) -> Number angle
//   setActorColor(Number r, Number g, Number b, Number id) -> Bool success
//   getActorColor(Number id) -> String color
//   enableActorPhysics(Number mass, Number friction, Number elasticity, Number id) -> Bool success
//   destroyPhysicsBody(Number id) -> Bool success

import android.util.Log;
import android.webkit.JavascriptInterface;

import com.sit.adefy.Renderer;
import com.sit.adefy.objects.Actor;
import com.sit.adefy.objects.Color3;

import org.jbox2d.common.Vec2;

// For full, proper documentation, check the AWGL implementation
public class JSActorInterface {

  private int nextID = 0;
  private int getNextID() { return nextID++; }

  private Actor findActor(int id) {
    for(Actor a : Renderer.actors) {
      if(a.getId() == id) { return a; }
    }

    return null;
  }

  @JavascriptInterface
  public int createActor(String verts) {

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
    new Actor(id, _verts);

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
