package com.sit.adefy.objects;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.sit.adefy.Renderer;
import com.sit.adefy.materials.Material;
import com.sit.adefy.materials.SingleColorMaterial;
import com.sit.adefy.physics.BodyQueueDef;
import com.sit.adefy.physics.PhysicsEngine;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.json.JSONArray;

import javax.microedition.khronos.opengles.GL10;
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

  public int renderMode = 2;

  public Actor(int _id, float[] _vertices) {
    this.id = _id;

    // Build initial actor
    updateVertices(_vertices);

    // Start out with solid material
    material = new SingleColorMaterial();

    Renderer.actors.add(this);
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
    Renderer.actors.remove(this);
  }

  // Refreshes the internal vertex buffer
  protected void refreshVertBuffer() {

    // Allocate a new byte buffer to move the vertices into a FloatBuffer
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    vertBuffer = byteBuffer.asFloatBuffer();
    vertBuffer.put(vertices);
    vertBuffer.position(0);

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

    bd.position = Renderer.screenToWorld(position);

    // Add to physics world body creation queue, will be finalized when possible
    PhysicsEngine.requestBodyCreation(new BodyQueueDef(id, bd));
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
          verts[vertIndex] = new Vec2(psyxVertices[i] / Renderer.getPPM(), psyxVertices[i + 1] / Renderer.getPPM());
          vertIndex++;
        }

        shape.set(verts, verts.length);

      } else {
        Vec2[] verts = new Vec2[vertices.length / 3];

        int vertIndex = 0;
        for(int i = 0; i < vertices.length; i += 3) {
          verts[vertIndex] = new Vec2(vertices[i] / Renderer.getPPM(), vertices[i + 1] / Renderer.getPPM());
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

      body.createFixture(fd);
    }
  }

  // In reality, mearly registers the body for destruction, which occurs in another thread
  public void destroyPhysicsBody() {

    if(body == null) { return; }

    PhysicsEngine.destroyBody(body);
    body = null;
  }

  public void draw() {
    if(!visible) { return; }

    // Update local data from physics engine, if applicable
    if(body != null) {
      position = Renderer.worldToScreen(body.getPosition());
      rotation = body.getAngle() * 57.2957795786f;
    }

    Matrix.setIdentityM(modelView, 0);
    Matrix.translateM(modelView, 0, position.x, position.y, 1.0f);
    Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);

    if(renderMode == 1) {
      material.draw(vertBuffer, vertices.length / 3, GLES20.GL_TRIANGLE_STRIP, modelView);
    } else if(renderMode == 2) {
      material.draw(vertBuffer, vertices.length / 3, GLES20.GL_TRIANGLE_FAN, modelView);
    }
  }

  // Modify the actor or the body
  public void setPosition(Vec2 position) {
    if(body == null) {
      this.position = position;
    } else {
      body.setTransform(Renderer.screenToWorld(position), body.getAngle());
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
      return Renderer.worldToScreen(body.getPosition());
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
