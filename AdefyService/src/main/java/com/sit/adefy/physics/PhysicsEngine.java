package com.sit.adefy.physics;

import com.sit.adefy.Renderer;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;

// Wrapper for BulletPhysics, keeps track of objects and whatnot
public class PhysicsEngine {

  public static int velIterations = 6;
  public static int posIterations = 2;

  private static Vec2 gravity = new Vec2(0, 0);
  private static PhysicsThread pThread = null;

  private static ArrayList<BodyQueueDef> bodyCreateQ = new ArrayList<BodyQueueDef>();
  private static ArrayList<Body> bodyDestroyQ = new ArrayList<Body>();

  private static int bodyCount = 0;

  // Schedules the body for processing before the next world step
  //
  // Creates the world if no bodies currently exist
  public static void requestBodyCreation(BodyQueueDef bq) {

    // Ship it to our queue
    bodyCreateQ.add(bq);

    if(bodyCount == 0) {

      // If the thread already exists, then wait for it to finish running before re-creating
      // Technically one could just restart the thread, but recreating is simpler
      if(pThread != null) {
        while(pThread.isRunning()) { }
      }

      pThread = new PhysicsThread();
      pThread.start();
    }

    // Take note of the new body
    bodyCount++;
  }

  // Queue up
  public static void destroyBody(Body body) {
    bodyDestroyQ.add(body);
  }

  // Thread definition, this is where the physics magic happens
  private static class PhysicsThread extends Thread {

    // Setting this to true exits the internal update loop, and ends the thread
    public boolean stop = false;

    // We need to know if the thread is still running or not, just in case we try to create it
    // after telling it to stop, but before it can finish.
    private boolean running = false;

    // The world itself
    private World physicsWorld = null;

    public boolean isRunning() { return running; }

    public void setGravity(Vec2 grav) {
      if(physicsWorld != null) {
        physicsWorld.setGravity(grav);
      }
    }

    public Vec2 getGravity() {
      if(physicsWorld != null) {
        return physicsWorld.getGravity();
      } else {
        return null;
      }
    }

    @Override
    public void run() {

      running = true;

      // Create world with saved gravity
      physicsWorld = new World(new Vec2(0, -10));
      physicsWorld.setAllowSleep(true);

      // Step!
      while(!stop) {

        // Record the start time, so we know how long it took to sim everything
        long startTime = System.currentTimeMillis();

        if(bodyDestroyQ.size() > 0) {
          synchronized (bodyDestroyQ) {

            for(Body body : bodyDestroyQ) {
              physicsWorld.destroyBody(body);
              bodyCount--;
            }

            bodyDestroyQ.clear();
          }
        }

        if(bodyCreateQ.size() > 0) {
          synchronized (bodyCreateQ) {

            // Handle creations
            for (BodyQueueDef bq : bodyCreateQ) {
              Renderer.actors.get(bq.getActorID()).onBodyCreation(physicsWorld.createBody(bq.getBd()));
            }

            bodyCreateQ.clear();
          }
        }

        // Perform step, calculate elapsed time and divide by 1000 to get it
        // in seconds
        physicsWorld.step(0.016666666f, velIterations, posIterations);

        if(bodyCount == 0) { stop = true; }

        long simTime = System.currentTimeMillis() - startTime;

        if(simTime < 16) {
          try {
            Thread.sleep(16 - simTime);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

      running = false;
    }
  }

  public static void setGravity(Vec2 grav) {
    if(pThread != null) {
      pThread.setGravity(grav);
    }
    gravity = grav;
  }

  public static Vec2 getGravity() {
    return gravity;
  }
}
