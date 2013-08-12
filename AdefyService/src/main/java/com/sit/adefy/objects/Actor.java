package com.sit.adefy.objects;

import android.opengl.GLES20;
import android.opengl.Matrix;
import com.sit.adefy.Renderer;
import com.sit.adefy.physics.BodyQueueDef;
import com.sit.adefy.physics.PhysicsEngine;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Actor {

  private int id;
  private Body body = null;

  protected FloatBuffer vertBuffer;
  protected FloatBuffer texBuffer;

  protected float vertices[];
  protected float texVerts[] = null;
  protected int[] texture = new int[1];

  protected Vec2 position = new Vec2(0.0f, 0.0f);
  protected float rotation = 0.0f;

  public Color3 color = new Color3(255, 255, 255);

  public boolean lit = false;
  public boolean visible = true;

  // Saved for when body is recreated on a vert refresh
  private float friction;
  private float density;
  private float restitution;

  private int positionHandle = GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");
  private int colorHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "Color");
  private int modelHandle = GLES20.glGetUniformLocation(Renderer.getShaderProg(), "ModelView");

  public Actor(int _id, float[] _vertices) {

    this.id = _id;
    this.vertices = _vertices;

    Renderer.actors.add(this);

    // Build initial actor
    refreshVertBuffer();
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
      Vec2[] verts = new Vec2[vertices.length / 3];

      int vertIndex = 0;
      for(int i = 0; i < vertices.length; i += 3) {
        verts[vertIndex] = new Vec2(vertices[i] / Renderer.getPPM(), vertices[i + 1] / Renderer.getPPM());
        vertIndex++;
      }

      shape.set(verts, verts.length);

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

    // Construct mvp to be applied to every vertex
    float[] modelView = new float[16];

    // Equivalent of gl.glLoadIdentity()
    Matrix.setIdentityM(modelView, 0);

    // gl.glTranslatef()
    Matrix.translateM(modelView, 0, position.x, position.y, 1.0f);

    // gl.glRotatef()
    Matrix.rotateM(modelView, 0, rotation, 0, 0, 1.0f);

    // Load our matrix and color into our shader
    GLES20.glUniformMatrix4fv(modelHandle, 1, false, modelView, 0);
    GLES20.glUniform4fv(colorHandle, 1, color.toFloatArray(), 0);

    // Set up pointers, and draw using our vertBuffer as before
    GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertBuffer);
    GLES20.glEnableVertexAttribArray(positionHandle);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
    GLES20.glDisableVertexAttribArray(positionHandle);
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

  public float[] getVertices() { return vertices; }
  public int getId() { return id; }
}
