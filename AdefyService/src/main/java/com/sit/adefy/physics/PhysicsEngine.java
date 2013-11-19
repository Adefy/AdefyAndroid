package com.sit.adefy.physics;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.util.Log;

import com.sit.adefy.AdefyRenderer;
import com.sit.adefy.objects.Actor;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

import java.util.ArrayList;

// Wrapper for BulletPhysics, keeps track of objects and whatnot
public class PhysicsEngine {

  public int velIterations = 6;
  public int posIterations = 2;

  private Vec2 gravity = new Vec2(0, 0);
  private PhysicsThread pThread = null;

  private final ArrayList<BodyQueueDef> bodyCreateQ = new ArrayList<BodyQueueDef>();
  private final ArrayList<Body> bodyDestroyQ = new ArrayList<Body>();

  public AdefyRenderer renderer;
  private int bodyCount = 0;
  private boolean destroyAll = false;

  private static int[] physicsLayers = new int[] {
      0x0001, // 1
      0x0002, // 2
      0x0004, // 3
      0x0008, // 4
      0x0010, // 5
      0x0020, // 6
      0x0040, // 7
      0x0080, // 8
      0x0100, // 9
      0x0200, // 10
      0x0400, // 11
      0x0800, // 12
      0x1000, // 13
      0x2000, // 14
      0x4000, // 15
      0x8000  // 16
  };

  public static int getCategoryBits(int layer) {
    if(layer > physicsLayers.length - 1) {
      return 0;
    } else {
      return physicsLayers[layer];
    }
  }

  public static int getMaskBits(int layer) {

    // Layer 0 collides with everything
    if(layer == 0) {
      return 0xffff;
    }

    if(layer > physicsLayers.length - 1) {
      return 0;
    } else {
      return 0xffff & ~physicsLayers[layer];
    }
  }

  // Schedules the body for processing before the next world step
  //
  // Creates the world if no bodies currently exist
  public void requestBodyCreation(BodyQueueDef bq) {

    // Ship it to our queue
    bodyCreateQ.add(bq);

    if(bodyCount == 0) {

      // If the thread already exists, then wait for it to finish running before re-creating
      // Technically one could just restart the thread, but recreating is simpler
      if(pThread != null) {
        destroyAll = true;
        while(pThread.isRunning()) { }
      }

      destroyAll = false;

      pThread = new PhysicsThread();
      pThread.start();
    }

    // Take note of the new body
    bodyCount++;
  }

  // Queue up
  public void destroyBody(Body body) {
    bodyDestroyQ.add(body);
  }

  public void destroyAllBodies() {
    if(bodyCount > 0) {
      destroyAll = true;
      bodyCount = 0;
    }
  }

  public boolean waitingOnDestroy() {
    return destroyAll;
  }

  // Thread definition, this is where the physics magic happens
  private class PhysicsThread extends Thread {

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
        if(destroyAll) { destroyAll = false; break; }

        // Record the start time, so we know how long it took to sim everything
        long startTime = System.currentTimeMillis();

        if(bodyDestroyQ.size() > 0) {
          try {
            synchronized (bodyDestroyQ) {

              for(Body body : bodyDestroyQ) {
                physicsWorld.destroyBody(body);
                bodyCount--;
              }

              bodyDestroyQ.clear();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        try {

          if(bodyCreateQ.size() > 0) {
            synchronized (bodyCreateQ) {

                // Handle creations
                for (BodyQueueDef bq : bodyCreateQ) {
                  for (Actor a : renderer.actors) {
                    if(a.getId() == bq.getActorID()) {
                      a.onBodyCreation(physicsWorld.createBody(bq.getBd()));
                      break;
                    }
                  }
                }

              bodyCreateQ.clear();
            }
          }

        } catch (Exception e) {
          e.printStackTrace();
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

  public void setGravity(Vec2 grav) {
    if(pThread != null) {
      pThread.setGravity(grav);
    }
    gravity = grav;
  }

  public Vec2 getGravity() {
    return gravity;
  }
}
