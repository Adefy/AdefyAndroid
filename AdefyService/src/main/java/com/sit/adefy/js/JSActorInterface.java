package com.sit.adefy.js;

import android.webkit.JavascriptInterface;

import com.sit.adefy.Renderer;
import com.sit.adefy.objects.Actor;

import org.jbox2d.common.Vec2;

import java.lang.reflect.GenericSignatureFormatError;

// For full, proper documentation, check the AWGL implementation
public class JSActorInterface {

  private int nextID = 0;
  private int getNextID() { return nextID++; }

  @JavascriptInterface
  public int createActor(String verts) {

    // Generate vert array
    // Verts are seperated by commas
    String[] vertsArray = verts.split(",");
    float[] _verts = new float[vertsArray.length];

    // Convert to floats
    for(int i = 0; i < vertsArray.length; i++) {
      _verts[i] = Float.parseFloat(vertsArray[i]);
    }

    // Ship actor
    int id = getNextID();
    Renderer.actors.add(new Actor(id, _verts));

    return id;
  }

  // Set actor position using id
  // Fails with false if actor is not found
  @JavascriptInterface
  public boolean setActorPosition(int id, float x, float y) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        a.setPosition(new Vec2(x, y));
        return true;
      }
    }

    return false;
  }

  // Get actor position using id
  // Returns position as a JSON representation of a primitive {x, y} object!
  //
  // Returns an empty string if the actor was not found
  @JavascriptInterface
  public String getActorPosition(int id) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        Vec2 v = a.getPosition();
        return "{ x: " + v.x + ", y: " + v.y + " }";
      }
    }

    return "";
  }

  // Set actor rotation in radians, fails with false if the actor is not found
  @JavascriptInterface
  public boolean setActorRotation(float angle, int id, boolean radians) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        if(radians) {
          angle = angle * 57.2957795f; // Convert to degrees
        }
        a.setRotation(angle);
        return true;
      }
    }

    return false;
  }

  // Get actor rotation in radians or degrees using an id
  // Fails with -1
  @JavascriptInterface
  public float getActorRotation(int id, boolean radians) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        if(radians) {
          return a.getRotation() * 0.0174532925f;
        } else {
          return a.getRotation();
        }
      }
    }

    return -1.0f;
  }

  // Set actor color using component values and id
  // Range for values is 0-255
  // Fails with false
  @JavascriptInterface
  public boolean setActorColor(int r, int g, int b, int id) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        a.color.r = r;
        a.color.g = g;
        a.color.b = b;
        return true;
      }
    }

    return false;
  }

  // Get actor color as a JSON triple using id, fails with empty string
  @JavascriptInterface
  public String getActorColor(int id) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        return "{ r: " + a.color.r + ", g: " + a.color.g + ", b: " + a.color.b + " }";
      }
    }

    return "";
  }

  // Enable actor physics using id, fails with false if actor is not found
  // TODO: Mass is currently ignored, and defaults to 1.0
  @JavascriptInterface
  public boolean enableActorPhysics(float mass, float friction, float elasticity, int id) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        a.createPhysicsBody(1.0f, friction, elasticity);
        return true;
      }
    }

    return false;
  }

  // Destroy physics body using id, fails with false if actor is not found
  @JavascriptInterface
  public boolean destroyPhysicsBody(int id) {

    for(Actor a : Renderer.actors) {
      if(a.getId() == id) {
        a.destroyPhysicsBody();
        return true;
      }
    }

    return false;
  }
}
