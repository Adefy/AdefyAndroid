package com.sit.adefy.demos;
/*
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.sit.adefy.AdefyTest;
import com.sit.adefy.R;
import com.sit.adefy.Renderer;
import com.sit.adefy.objects.Color3;
import com.sit.adefy.objects.Sprite;
import com.sit.adefy.objects.TextActor;
import com.sit.adefy.physics.PhysicsEngine;
import com.sit.adefy.physics.objects.BaseObject;
import com.sit.adefy.physics.objects.CircleBody;
import com.sit.adefy.shapes.XYRectangle;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;

import javax.microedition.khronos.opengles.GL10;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
*/
public class AdefyEnd extends Demo {
/*
  private long lastSpawnTime;

  private ArrayList<BaseObject> actors = new ArrayList<BaseObject>();
  private ArrayList<BaseObject> removalQueue = new ArrayList<BaseObject>();

  private float xSpread;
  private float ySpread;
  private float rSpread;

  private Sprite cloud = null;
  private Bitmap cloudTex = null;

  private TextActor teaser1 = null;
  private TextActor teaser2 = null;
  private TextActor teaser3 = null;
  private TextActor teaser4 = null;

  private XYRectangle bg1 = null;
  private XYRectangle bg2 = null;
  private XYRectangle bg3 = null;
  private XYRectangle bg4 = null;

  // Really hacky, unelegant, blegh!
  private static boolean runBGMove = true;

  public AdefyEnd(Context ctx) {

    // Texture is applied in setupGL()
    cloudTex = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.adefy_cloud);
  }

  public void setupGL(GL10 gl)  {

    lastSpawnTime = System.currentTimeMillis();

    // Position cloud 1/8th of it's width from the left and top edges
    float offX = Renderer.getScreenW();
    float offY = Renderer.getScreenH() - ((cloudTex.getHeight() / 2.0f) + (cloudTex.getHeight() / 8.0f));

    cloud = new Sprite(gl, cloudTex, new Vec2(offX, offY));

    // Set rain formation spread
    xSpread = cloud.getWidth() * 0.7f;
    ySpread = cloud.getHeight() * 0.2f;
    rSpread = 30.0f;

    // Create text
    teaser1 = new TextActor(gl, "Native GL 1.1", 32, new Vec2(60, 600), new Color3(0, 0, 0));
    teaser2 = new TextActor(gl, "Javascript API", 32, new Vec2(120, 500), new Color3(0, 0, 0));
    teaser3 = new TextActor(gl, "Box2D integration", 32, new Vec2(180, 400), new Color3(0, 0, 0));
    teaser4 = new TextActor(gl, "Painfully fast", 32, new Vec2(240, 300), new Color3(0, 0, 0));

    try {

      teaser1.createBody();
      teaser2.createBody();
      teaser3.createBody();
      teaser4.createBody();

      teaser1.getBody().setTransform(teaser1.getBody().getPosition(), 0.523598776f);
      teaser2.getBody().setTransform(teaser2.getBody().getPosition(), 0.523598776f);
      teaser3.getBody().setTransform(teaser3.getBody().getPosition(), 0.523598776f);
      teaser4.getBody().setTransform(teaser4.getBody().getPosition(), 0.523598776f);

    } catch (Exception e) {
      e.printStackTrace();
    }

    // Create shifting backgrounds
    bg1 = new XYRectangle(Renderer.getScreenW() * 3, Renderer.getScreenH() * 0.7f);
    bg2 = new XYRectangle(Renderer.getScreenW() * 3, Renderer.getScreenH() * 0.7f);
    bg3 = new XYRectangle(Renderer.getScreenW() * 3, Renderer.getScreenH() * 0.7f);
    bg4 = new XYRectangle(Renderer.getScreenW() * 3, Renderer.getScreenH() * 0.7f);

    bg1.color = new Color3(0, 229, 179);
    bg2.color = new Color3(255, 241, 156);
    bg3.color = new Color3(255, 143, 180);
    bg4.color = new Color3(143, 255, 143);

    bg1.setRotation(new Vec3(0, 0, 40));
    bg2.setRotation(new Vec3(0, 0, 40));
    bg3.setRotation(new Vec3(0, 0, 40));
    bg4.setRotation(new Vec3(0, 0, 40));

    bg1.setPosition(new Vec2(Renderer.getScreenW() / 2.5f, 0));
    bg2.setPosition(new Vec2(Renderer.getScreenW() / 2.0f, Renderer.getScreenH() * 1.5f));
    bg3.setPosition(new Vec2(0, Renderer.getScreenH() * 0.8f));
    bg4.setPosition(new Vec2(Renderer.getScreenW() / 2.0f, Renderer.getScreenH() * 0.3f));

    // Move cloud
    new Thread(new Runnable() {

      @Override
      public void run() {

        long lastMoveTime = System.currentTimeMillis();

        while(cloud.getPosition().x > (cloudTex.getWidth() / 2.0f) + (cloudTex.getWidth() / 8.0f)) {
          if(System.currentTimeMillis() - lastMoveTime > 10) {
            cloud.setPosition(new Vec2(cloud.getPosition().x - 2.5f, cloud.getPosition().y));

            lastMoveTime = System.currentTimeMillis();
          }
        }

        new Timer().schedule(new TimerTask() {

          @Override
          public void run() {

            AdefyTest.triggerEnd();
            runBGMove = false;

          }
        }, 3000);
      }

    }).start();

    // Move backgrounds
    new Thread(new Runnable() {
      @Override
      public void run() {

        long lastMoveTime = System.currentTimeMillis();

        while(runBGMove) {
          if(System.currentTimeMillis() - lastMoveTime > 50) {

            bg1.setPosition(new Vec2(bg1.getPosition().x + 0.8f, bg1.getPosition().y - 0.5f));
            bg2.setPosition(new Vec2(bg2.getPosition().x - 1.5f, bg2.getPosition().y - 0.8f));
            bg3.setPosition(new Vec2(bg3.getPosition().x + 0.1f, bg3.getPosition().y + 1.7f));
            bg4.setPosition(new Vec2(bg4.getPosition().x - 0.4f, bg4.getPosition().y + 1.0f));

            lastMoveTime = System.currentTimeMillis();
          }
        }
      }
    }).start();

    isSetup = true;
  }

  public void render(GL10 gl) {

    // Add rain
    if(System.currentTimeMillis() - lastSpawnTime > 150 && !PhysicsEngine.isWorldStepping()) {

      try {

        float offX1 = cloud.getPosition().x + (float)(Math.random() * xSpread) - (xSpread / 2.0f);
        float offY1 = cloud.getPosition().y + (float)(Math.random() * ySpread) - (ySpread / 2.0f);
        float offX2 = cloud.getPosition().x + (float)(Math.random() * xSpread) - (xSpread / 2.0f);
        float offY2 = cloud.getPosition().y + (float)(Math.random() * ySpread) - (ySpread / 2.0f);
        float r = (float)(Math.random() * (rSpread / 2.0f)) + (rSpread / 8.0f);

        CircleBody circle1 = new CircleBody(new Vec2(offX1, offY1), r, 1.0f, 0.3f, 0.2f);
        CircleBody circle2 = new CircleBody(new Vec2(offX2, offY2), r, 1.0f, 0.3f, 0.2f);

        circle1.visual.color = new Color3(117, 216, 255);
        circle2.visual.color = new Color3(117, 216, 255);

        actors.add(circle1);
        actors.add(circle2);

      } catch (Exception e) {
        e.printStackTrace();
      }

      lastSpawnTime = System.currentTimeMillis();
    }

    bg4.draw(gl);
    bg3.draw(gl);
    bg2.draw(gl);
    bg1.draw(gl);

    for (BaseObject actor : actors) {

      // Draw balls
      actor.draw(gl);

      // Check if balls are not visible
      Vec2 ballPosition = actor.getBody().getPosition();

      // Add to removal queue if off-screen
      if (ballPosition.x < 0 || ballPosition.x > Renderer.getScreenW() || ballPosition.y < 0 || ballPosition.y > Renderer.getScreenH()) {
        removalQueue.add(actor);
      }
    }

    cloud.draw(gl);
    teaser1.draw(gl);
    teaser2.draw(gl);
    teaser3.draw(gl);
    teaser4.draw(gl);

    // Remove actors
    for (BaseObject obj : removalQueue) {

      obj.dispose();
      actors.remove(obj);
    }

    removalQueue.clear();
  }

  public void reset() {

  } */
}
