package com.sit.adefy.objects;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.materials.Material;
import com.sit.adefy.materials.SingleColorMaterial;
import com.sit.adefy.materials.TexturedMaterial;
import com.sit.adefy.physics.BodyQueueDef;
import com.sit.adefy.physics.PhysicsEngine;

import org.jbox2d.collision.shapes.MassData;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Actor {

  private int id;
  private Body body = null;

  protected FloatBuffer vertBuffer;
  protected FloatBuffer texBuffer;

  protected float vertices[];
  protected float texVerts[] = null;
  protected int[] texture = new int[1];
  protected float psyxVertices[] = null;

  private float[] modelView = new float[16];

  protected Vec2 position = new Vec2(0.0f, 0.0f);
  protected float rotation = 0.0f;

  public boolean lit = false;
  public boolean visible = true;

  // Saved for when body is recreated on a vert refresh
  private float friction;
  private float density;
  private float restitution;

  private Material material;
  private AdefyRenderer renderer;

  public int renderMode = 2;

  private Actor attachment = null;
  private float attachmentOffx = 0;
  private float attachmentOffy = 0;

  private int layer = 0;
  private int physicsLayer = 0;

  public Actor(AdefyRenderer renderer, int _id, float[] _vertices) {
    this.id = _id;
    this.renderer = renderer;

    // Build initial actor
    updateVertices(_vertices);

    // Start out with solid material
    material = new SingleColorMaterial();

    addToRenderer();
  }

  public int getLayer() { return layer; }
  public int getPhysicsLayer() { return physicsLayer; }

  public void setLayer(int l) {
    layer = l;
    removeFromRenderer();
    addToRenderer();
  }

  public void setPhysicsLayer(int l) {
    physicsLayer = l;
    destroyPhysicsBody();
    createPhysicsBody(density, friction, restitution);
  }

  // Add us to the render list, taking layers into account
  private void addToRenderer() {

    // Add as first item
    if(renderer.actors.size() == 0) {
      renderer.actors.add(this);
    } else {

      // Go through and add between two larger and smaller actors
      for(int i = 0; i < renderer.actors.size(); i++) {

        // Find smaller actor behind us
        if(renderer.actors.get(i).getLayer() <= layer) {

          // Make sure there is at least one more actor
          if(renderer.actors.size() >= i + 2) {

            // Check if the next actor is larger than us. If so, insert
            if(renderer.actors.get(i + 1).getLayer() >= layer) {
              renderer.actors.add(i + 1, this);
              return;
            }

          // We are at the end, just insert ourselves
          } else {
            renderer.actors.add(this);
            return;
          }
        }
      }

      // If we still haven't returned, it means we are the smallest actor. So ship ittt
      renderer.actors.add(0, this);
    }
  }

  // Remove us from the renderer list
  private void removeFromRenderer() {
    renderer.actors.remove(this);
  }

  public boolean hasAttachment() { return attachment != null; }
  public Actor getAttachment() { return attachment; }

  // setTexture is false when the texture is not yet loaded, and the set operation
  // has been queued
  public Actor attachTexture(String name, float w, float h, float offx, float offy, float angle, boolean setTexture) {

    // If we already have an attachment, kill it
    if(attachment != null) {
      attachment.destroy();
      attachment = null;
    }

    // Create new actor
    float[] verts = new float[10];

    verts[0] = -w;
    verts[1] = -h;
    verts[2] = -w;
    verts[3] =  h;
    verts[4] =  w;
    verts[5] =  h;
    verts[6] =  w;
    verts[7] = -h;
    verts[8] = -w;
    verts[9] = -h;

    int id = JSActorInterface.getNextID();
    attachment = new Actor(renderer, id, verts);
    if(setTexture) { attachment.setTexture(name); }
    attachment.setRotation(angle);

    attachmentOffx = offx;
    attachmentOffy = offy;

    return attachment;
  }

  public boolean removeAttachment() {
    if(attachment == null) { return false; }
    else { attachment.destroy(); attachment = null; return true; }
  }

  public boolean setAttachmentVisibility(boolean visible) {
    if(attachment == null) { return false; }

    attachment.visible = visible;
    return true;
  }

  public void setPhysicsVertices(float[] verts) {
    this.psyxVertices = verts;
    refreshVertBuffer();
  }

  // Update material color if possible
  public void setColor(Color3 color) {
    if(material.getName().equals(SingleColorMaterial.name)) {
      ((SingleColorMaterial)material).setColor(color);
    }
  }

  public Color3 getColor() {
    if(material.getName().equals(SingleColorMaterial.name)) {
      return ((SingleColorMaterial)material).getColor();
    }

    return null;
  }

  // Use the texture material if we are not already doing so, and set the texture name
  public void setTexture(String name) {

    // Bail if we don't have exactly 15 vertices (box)
    if(vertices.length != 15) {
      Log.d("adefy", "Can't set texture on non-box object " + vertices.length);
      return;
    }

    // Go through and find the texture handle
    int[] handle = renderer.getTextureHandle(name);

    if(!material.getName().equals(TexturedMaterial.name)) {
      this.material = new TexturedMaterial(handle);
    } else {
      ((TexturedMaterial)this.material).setTextureHandle(handle);
    }
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public Material getMaterial() {
    return material;
  }

  public String getMaterialName() {
    return material.getName();
  }

  public void destroy() {

    if(body != null) { destroyPhysicsBody(); }
    removeFromRenderer();
  }

  // Refreshes the internal vertex buffer
  protected void refreshVertBuffer() {

    // Allocate a new byte buffer to move the vertices into a FloatBuffer
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    vertBuffer = byteBuffer.asFloatBuffer();
    vertBuffer.put(vertices);
    vertBuffer.position(0);

    // Set up UV coords
    texVerts = new float[]{
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f,
        0.0f, 1.0f
    };

    // Set up texture coordinates
    byteBuffer = ByteBuffer.allocateDirect(texVerts.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    texBuffer = byteBuffer.asFloatBuffer();
    texBuffer.put(texVerts);
    texBuffer.position(0);

    if(body != null) {
      destroyPhysicsBody();
      createPhysicsBody(density, friction, restitution);
    }
  }

  // Rebuild our vertices using a list
  public void updateVertices(ArrayList<Float> _vertices) {

    this.vertices = new float[(int)(_vertices.size() * 1.5f)];

    // Add z coord of 1
    int set = 0;
    for(int i = 0; i < this.vertices.length; i += 3) {
      this.vertices[i] = _vertices.get(set);
      this.vertices[i + 1] = _vertices.get(set + 1);
      this.vertices[i + 2] = 1;

      set += 2;
    }

    // Rebuild buffers
    refreshVertBuffer();
  }

  // Rebuild our vertices using a flat array
  public void updateVertices(float[] _vertices) {

    this.vertices = new float[(int)(_vertices.length * 1.5f)];

    // Add z coord of 1
    int set = 0;
    for(int i = 0; i < this.vertices.length; i += 3) {
      this.vertices[i] = _vertices[set];
      this.vertices[i + 1] = _vertices[set + 1];
      this.vertices[i + 2] = 1;

      set += 2;
    }

    // Rebuild buffers
    refreshVertBuffer();
  }

  // Manipulate the physics body
  public void createPhysicsBody(float _density, float _friction, float _restitution) {

    if(body != null) { return; }

    // Save values
    friction = _friction;
    density = _density;
    restitution = _restitution;

    // Create the body
    BodyDef bd = new BodyDef();

    if(density > 0) {
      bd.type = BodyType.DYNAMIC;
    } else {
      bd.type = BodyType.STATIC;
    }

    bd.position = AdefyRenderer.screenToWorld(position);
    bd.angle = rotation * 0.0174532925f;

    // Add to physics world body creation queue, will be finalized when possible
    renderer.getPsyx().requestBodyCreation(new BodyQueueDef(id, bd));
  }

  // Meant to be called by the PhysicsEngine only!
  public void onBodyCreation(Body _body) {

    // Threads ftw
    synchronized (this) {
      body = _body;

      // Body has been created, make fixture and finalize it
      // Physics world waits for completion before continuing

      // Create fixture from vertices
      PolygonShape shape = new PolygonShape();

      if(psyxVertices != null) {
        Vec2[] verts = new Vec2[psyxVertices.length / 2];

        int vertIndex = 0;
        for(int i = 0; i < psyxVertices.length; i += 2) {
          verts[vertIndex] = new Vec2(psyxVertices[i] / AdefyRenderer.getPPM(), psyxVertices[i + 1] / AdefyRenderer.getPPM());
          vertIndex++;
        }

        shape.set(verts, verts.length);

      } else {
        Vec2[] verts = new Vec2[vertices.length / 3];

        int vertIndex = 0;
        for(int i = 0; i < vertices.length; i += 3) {
          verts[vertIndex] = new Vec2(vertices[i] / AdefyRenderer.getPPM(), vertices[i + 1] / AdefyRenderer.getPPM());
          vertIndex++;
        }

        shape.set(verts, verts.length);
      }

      // Attach fixture
      FixtureDef fd = new FixtureDef();
      fd.shape = shape;
      fd.density = density;
      fd.friction = friction;
      fd.restitution = restitution;

      // Setup physics layer
      fd.filter.categoryBits = PhysicsEngine.getCategoryBits(physicsLayer);
      fd.filter.maskBits = PhysicsEngine.getMaskBits(physicsLayer);

      body.createFixture(fd);

      // Force mass
      MassData massdata = new MassData();
      body.getMassData(massdata);

      float scaleFactor = density / massdata.mass;
      massdata.mass *= scaleFactor;
      massdata.I *= scaleFactor;

      body.setMassData(massdata);
    }
  }

  // In reality, mearly registers the body for destruction, which occurs in another thread
  public void destroyPhysicsBody() {

    if(body == null) { return; }

    renderer.getPsyx().destroyBody(body);
    body = null;
  }

  public void draw() {
    if(!visible) { return; }

    // Update local data from physics engine, if applicable
    if(body != null) {
      position = AdefyRenderer.worldToScreen(body.getPosition());
      rotation = body.getAngle() * 57.2957795786f;
    }

    Matrix.setIdentityM(modelView, 0);
    Matrix.translateM(modelView, 0, position.x - AdefyRenderer.camX, position.y - AdefyRenderer.camY, 1.0f);
    Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);

    // Default (renderMode 1)
    int drawMode = GLES20.GL_TRIANGLE_STRIP;

    if(renderMode == 2) {
      drawMode = GLES20.GL_TRIANGLE_FAN;
    }

    final Material myMaterial = material;

    if(myMaterial.getName().equals(SingleColorMaterial.name)) {
      ((SingleColorMaterial)myMaterial).draw(vertBuffer, vertices.length / 3, drawMode, modelView);
    } else if(myMaterial.getName().equals(TexturedMaterial.name)) {
      ((TexturedMaterial)myMaterial).draw(vertBuffer, texBuffer, vertices.length / 3, drawMode, modelView);
    }
  }

  // Modify the actor or the body
  public void setPosition(Vec2 position) {
    if(body == null) {
      this.position = position;
    } else {
      body.setTransform(AdefyRenderer.screenToWorld(position), body.getAngle());
    }
  }

  // Modify the actor or the body
  public void setRotation(float rotation) {
    if(body == null) {
      this.rotation = rotation;
    } else {
      body.setTransform(body.getPosition(), rotation * 0.0174532925f); // Convert to radians
    }
  }

  // Get from the physics body if avaliable
  public Vec2 getPosition() {
    if(body == null) {
      return position;
    } else {
      return AdefyRenderer.worldToScreen(body.getPosition());
    }
  }
  public float getRotation() {
    if(body == null) {
      return rotation;
    } else {
      return body.getAngle() * 57.2957795786f;
    }
  }

  public float[] getVertices() {

    float[] ret = new float[(vertices.length / 3) * 2];

    int set = 0;
    for(int i = 0; i < vertices.length; i+= 3) {
      ret[set] = vertices[i];
      ret[set + 1] = vertices[i + 1];

      set += 2;
    }

    return ret;
  }

  public int getId() { return id; }
}
