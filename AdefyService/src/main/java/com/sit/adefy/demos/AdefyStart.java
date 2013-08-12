package com.sit.adefy.demos;

/*
import com.sit.adefy.AdefyTest;
import com.sit.adefy.Renderer;
import com.sit.adefy.objects.Camera;
import com.sit.adefy.objects.Color3;
import com.sit.adefy.objects.TextActor;
import com.sit.adefy.physics.PhysicsEngine;
import com.sit.adefy.physics.objects.BaseObject;
import com.sit.adefy.physics.objects.ShapeBody;
import com.sit.adefy.shapes.XYRectangle;
import com.sit.adefy.shapes.XYRightTriangle;
import org.jbox2d.common.Vec2;

import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
*/
public class AdefyStart extends Demo {
/*
  private TextActor logo = null;

  private XYRectangle sliderLeft = null;
  private XYRectangle sliderRight = null;

  private ArrayList<BaseObject> triangles = new ArrayList<BaseObject>();
  private ArrayList<BaseObject> removalQueue = new ArrayList<BaseObject>();

  public void setupGL(GL10 gl)  {

    logo = new TextActor(gl, "Adefy", 128);
    logo.setPosition(new Vec2(-logo.getWidth(), Renderer.getScreenH() - logo.getHeight() * 2));

    // Create shifting backgrounds
    sliderLeft = new XYRectangle(Renderer.getScreenW(), Renderer.getScreenH());
    sliderRight = new XYRectangle(Renderer.getScreenW(), Renderer.getScreenH());

    sliderLeft.color = new Color3(0, 229, 179);
    sliderRight.color = new Color3(255, 241, 156);

    sliderLeft.setPosition(new Vec2(Renderer.getScreenW() * -0.5f, Renderer.getScreenH() * 0.75f));
    sliderRight.setPosition(new Vec2(Renderer.getScreenW() * 1.5f, 0));

    PhysicsEngine.getWorld().setGravity(new Vec2(10.0f, 0));

    // Animate!
    new Thread(new Runnable() {

      @Override
      public void run() {

        long eventTrigger = System.currentTimeMillis();

        while(sliderLeft.getPosition().x < Renderer.getScreenW() / 2.0f) {
          if(System.currentTimeMillis() - eventTrigger > 1) {

            sliderLeft.setPosition(new Vec2(sliderLeft.getPosition().x + 1.0f, sliderLeft.getPosition().y));
            sliderRight.setPosition(new Vec2(sliderRight.getPosition().x - 1.0f, sliderRight.getPosition().y));

            logo.setPosition(new Vec2(logo.getPosition().x + 1.0f, logo.getPosition().y));

            eventTrigger = System.currentTimeMillis();
          }
        }

        // Prepare for the end (DUN DUN DUN)
        new Timer().schedule(new TimerTask() {
          @Override
          public void run() {

            AdefyTest.triggerEnd();
          }
        }, 2000);
      }
    }).start();

    // Schedule triangle flood
    new Timer().schedule(new TimerTask() {

      @Override
      public void run() {

        long eventTrigger = System.currentTimeMillis();

        //Spam triangles, will be terminated when the timer goes off
        while(true) {
          if(System.currentTimeMillis() - eventTrigger > 150) {
            try {

              // 3 at a time
              for(int i = 0; i < 3; i++) {
                float offX = -40.0f * (float)Math.random();
                float offY = (Renderer.getScreenH() * 0.4f) + ((float)Math.random() * Renderer.getScreenH() * 0.3f) - (Renderer.getScreenH() * 0.1f);

                XYRightTriangle triangle = new XYRightTriangle(new Vec2(), 50.0f);
                triangle.color = new Color3((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));

                // Wait for permission
                while (PhysicsEngine.isWorldStepping()) {
                  Thread.yield();
                }

                ShapeBody triangleBody = new ShapeBody(new Vec2(offX, offY), triangle, 1.0f, 0.5f, 0.1f);
                triangleBody.getBody().setTransform(triangleBody.getBody().getPosition(), -1.57079633f);
                triangles.add(triangleBody);
              }

            } catch (Exception e) {
              e.printStackTrace();
            }

            eventTrigger = System.currentTimeMillis();
          }

          // Go through and remove offscreen triangles
          for (BaseObject triangle : triangles) {
            if (Camera.worldToScreen(triangle.getBody().getPosition()).x > Renderer.getScreenW()) {
              removalQueue.add(triangle);
            }
          }

          for (BaseObject obj : removalQueue) {
            triangles.remove(obj);
          }
        }
      }
    }, 1000);

    isSetup = true;
  }

  public void render(GL10 gl) {

    sliderLeft.draw(gl);
    sliderRight.draw(gl);

    logo.draw(gl);

    for (BaseObject triangle : triangles) {
      triangle.draw(gl);
    }
  }

  public void reset() {

  }*/
}
