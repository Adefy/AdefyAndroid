package com.sit.adefy.physics;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//


import com.badlogic.gdx.physics.box2d.BodyDef;

// Used for body creation
public class BodyQueueDef {

  private int actorID;
  private BodyDef bd;

  public BodyQueueDef(int _actorID, BodyDef _bd) {
    bd = _bd;
    actorID = _actorID;
  }

  public int getActorID() { return actorID; }
  public BodyDef getBd() { return bd; }
}