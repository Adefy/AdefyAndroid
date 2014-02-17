package com.sit.adefy.actors;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.Shape;
import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.js.JSActorInterface;
import com.sit.adefy.materials.Material;
import com.sit.adefy.materials.SingleColorMaterial;
import com.sit.adefy.materials.TexturedMaterial;
import com.sit.adefy.objects.Color3;
import com.sit.adefy.physics.BodyQueueDef;
import com.sit.adefy.physics.PhysicsEngine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public abstract class Actor {

  public boolean visible = true;
  public int renderMode = 2;

  private int id;
  private Body body = null;
  private int layer = 0;
  private int physicsLayer = 0;

  private FloatBuffer vertBuffer;
  private FloatBuffer texBuffer;
  private float friction;
  private float density;
  private float restitution;

  protected float vertices[];
  protected float texVerts[] = null;
  protected float psyxVertices[] = null;
  private float[] modelView = new float[16];

  private Vector2 position = new Vector2(0.0f, 0.0f);
  private Vector2 renderOffset = new Vector2(0.0f, 0.0f);
  private float rotation = 0.0f;

  private Material material;
  private AdefyRenderer renderer;
  private Actor attachment = null;

  public Actor(AdefyRenderer renderer, int _id, float[] _vertices) {
    this.id = _id;
    this.renderer = renderer;

    material = new SingleColorMaterial();
    if(_vertices != null) { updateVertices(_vertices); }
    addToRenderer();
  }

  ///
  /// Getters
  ///
  public int getLayer() { return layer; }
  public boolean hasAttachment() { return attachment != null; }
  public Actor getAttachment() { return attachment; }
  public Material getMaterial() { return material; }
  public String getMaterialName() { return material.getName(); }
  public AdefyRenderer getRenderer() { return renderer; }
  public int getId() { return id; }

  public void getPosition(Vector2 store) {
    if(body == null) {
      store.x = position.x;
      store.y = position.y;
    } else {
      AdefyRenderer.worldToScreen(body.getPosition(), store);
    }
  }

  public float getRotation() {
    if(body == null) { return rotation; }

    return body.getAngle() * 57.2957795786f;
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

  public Color3 getColor() {
    if(material.getName().equals(SingleColorMaterial.name)) {
      return ((SingleColorMaterial)material).getColor();
    }

    return null;
  }

  ///
  /// Setters
  ///
  public void setLayer(int l) {
    layer = l;
    removeFromRenderer();
    addToRenderer();
  }

  public void setPhysicsLayer(int l) {
    physicsLayer = l;
    destroyPhysicsBody();
    createPhysicsBody();
  }

  public void setColor(Color3 color) {
    if(material.getName().equals(SingleColorMaterial.name)) {
      ((SingleColorMaterial)material).setColor(color);
    }
  }

  // Modify the actor or the body
  public void setPosition(Vector2 position) {
    if(body == null) {
      this.position = position;
    } else {
      Vector2 targetPosition = new Vector2();
      AdefyRenderer.screenToWorld(position, targetPosition);
      body.setTransform(targetPosition, body.getAngle());
    }
  }

  public void setPosition(Actor actor) {
    actor.getPosition(position);

    if(body != null) {
      Vector2 targetPosition = new Vector2();
      AdefyRenderer.screenToWorld(position, targetPosition);
      body.setTransform(targetPosition, body.getAngle());
    }
  }

  // Modify the actor or the body
  public void setRotation(float rotation) {
    if(body == null) {
      this.rotation = rotation;
    } else {

      // Convert to radians
      body.setTransform(body.getPosition(), rotation * 0.0174532925f);
    }
  }

  public void setRenderOffset(float x, float y) { setRenderOffset(new Vector2(x, y)); }
  public void setRenderOffset(Vector2 offset) {
    renderOffset = offset;
  }

  ///
  /// Render list
  ///

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

  private void removeFromRenderer() {
    renderer.actors.remove(this);
  }

  ///
  /// Attachments
  ///

  public Actor attachTexture(String name, float w, float h, float offx, float offy, float angle, boolean setTexture) {
    if(attachment != null) { attachment.destroy(); }

    int id = JSActorInterface.getNextID();
    attachment = new RectangleActor(renderer, id, w, h);
    attachment.setRotation(angle);
    attachment.setRenderOffset(offx, offy);

    // setTexture is false when the texture is not yet loaded, and the set operation
    // has been queued
    if(setTexture) { attachment.setTexture(name); }

    return attachment;
  }

  public boolean removeAttachment() {
    if(attachment == null) { return false; }
    attachment.destroy();
    attachment = null;

    return true;
  }

  public boolean setAttachmentVisibility(boolean visible) {
    if(attachment == null) { return false; }
    attachment.visible = visible;

    return true;
  }

  ///
  /// Textures
  ///

  // Use the texture material if we are not already doing so, and set the texture name
  public void setTexture(String name) {

    // Bail if we don't have exactly 15 vertices (box)
    if(vertices.length != 12) {
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

  ///
  /// Vertex management
  ///

  public void setPhysicsVertices(float[] verts) {
    psyxVertices = verts;
    refreshVertBuffers();
  }

  private void refreshVertBuffers() {

    generateUVs();
    refreshVertBuffer();
    refreshTexVertBuffer();

    if(body != null) {
      destroyPhysicsBody();
      createPhysicsBody(density, friction, restitution);
    }
  }

  private void refreshVertBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    vertBuffer = byteBuffer.asFloatBuffer();
    vertBuffer.put(vertices);
    vertBuffer.position(0);
  }

  private void refreshTexVertBuffer() {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(texVerts.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    texBuffer = byteBuffer.asFloatBuffer();
    texBuffer.put(texVerts);
    texBuffer.position(0);
  }

  protected abstract void generateUVs();

  // Rebuild our vertices using a list
  public void updateVertices(ArrayList<Float> _vertices) {

    this.vertices = new float[(int)(_vertices.size() * 1.5f)];

    // Add z cord of 1
    int set = 0;
    for(int i = 0; i < this.vertices.length; i += 3) {
      this.vertices[i] = _vertices.get(set);
      this.vertices[i + 1] = _vertices.get(set + 1);
      this.vertices[i + 2] = 1;

      set += 2;
    }

    // Rebuild buffers
    refreshVertBuffers();
  }

  // Rebuild our vertices using a flat array
  public void updateVertices(float[] _vertices) {

    this.vertices = new float[(int)(_vertices.length * 1.5f)];

    // Add z cord of 1
    int set = 0;
    for(int i = 0; i < this.vertices.length; i += 3) {
      this.vertices[i] = _vertices[set];
      this.vertices[i + 1] = _vertices[set + 1];
      this.vertices[i + 2] = 1;

      set += 2;
    }

    refreshVertBuffers();
  }

  ///
  /// Physics
  ///

  public void createPhysicsBody() { createPhysicsBody(density, friction, restitution); }
  public void createPhysicsBody(float _density, float _friction, float _restitution) {

    if(body != null) { return; }

    // Save values
    friction = _friction;
    density = _density;
    restitution = _restitution;

    // Create the body
    BodyDef bd = new BodyDef();

    if(density > 0) {
      bd.type = BodyDef.BodyType.DynamicBody;
    } else {
      bd.type = BodyDef.BodyType.StaticBody;
    }

    Vector2 targetPosition = new Vector2();
    AdefyRenderer.screenToWorld(position, targetPosition);

    bd.position.set(targetPosition);
    bd.angle = rotation * 0.0174532925f;

    renderer.getPsyx().requestBodyCreation(new BodyQueueDef(id, bd));
  }

  public void onBodyCreation(Body body) {
    Shape shape = this.generateShape();

    FixtureDef fd = new FixtureDef();
    fd.shape = shape;
    fd.density = density;
    fd.friction = friction;
    fd.restitution = restitution;

    // Layer
    fd.filter.categoryBits = PhysicsEngine.getCategoryBits(physicsLayer);
    fd.filter.maskBits = PhysicsEngine.getMaskBits(physicsLayer);

    body.createFixture(fd);

    if(density > 0) {

      // Mass
      MassData massdata = body.getMassData();
      float scaleFactor = density / massdata.mass;
      massdata.mass *= scaleFactor;
      massdata.I *= scaleFactor;

      body.setMassData(massdata);
    }

    this.body = body;
  }

  protected abstract Shape generateShape();

  // In reality, merely registers the body for destruction, which occurs in another thread
  public void destroyPhysicsBody() {
    if(body == null) { return; }

    renderer.getPsyx().destroyBody(body);
    body = null;
  }

  ///
  /// Draw routine!
  ///

  public void draw() {
    if(!visible) { return; }

    updateWorldState();
    setupRenderMatrix();
    drawMaterial();
  }

  private void updateWorldState() {
    if(body != null) {
      AdefyRenderer.worldToScreen(body.getPosition(), position);
      rotation = (float)Math.toDegrees(body.getAngle());
    }
  }

  private int getGLRenderMode() {
    if(renderMode == 2) {
      return GLES20.GL_TRIANGLE_FAN;
    } else {
      return GLES20.GL_TRIANGLE_STRIP;
    }
  }

  private void setupRenderMatrix() {
    Matrix.setIdentityM(modelView, 0);
    Matrix.translateM(modelView, 0, position.x + renderOffset.x - AdefyRenderer.camX, position.y + renderOffset.y - AdefyRenderer.camY, 1.0f);
    Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);
  }

  private void drawMaterial() {
    int drawMode = getGLRenderMode();
    final Material drawMaterial = material;

    if(drawMaterial.getName().equals(SingleColorMaterial.name)) {
      ((SingleColorMaterial)drawMaterial).draw(vertBuffer, vertices.length / 3, drawMode, modelView);
    } else if(drawMaterial.getName().equals(TexturedMaterial.name)) {
      ((TexturedMaterial)drawMaterial).draw(vertBuffer, texBuffer, vertices.length / 3, drawMode, modelView);
    }
  }

  public void destroy() {
    if(body != null) { destroyPhysicsBody(); }
    removeFromRenderer();
  }

}
