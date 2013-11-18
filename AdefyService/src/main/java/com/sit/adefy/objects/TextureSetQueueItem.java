package com.sit.adefy.objects;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

public class TextureSetQueueItem {

  private static int idTrack;

  private Actor actor;
  private String name;
  private int id;

  public TextureSetQueueItem (Actor actor, String name) {
    this.actor = actor;
    this.name = name;

    id = TextureSetQueueItem.idTrack;
    TextureSetQueueItem.idTrack++;
  }

  public void apply() {
    actor.setTexture(name);
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }
}