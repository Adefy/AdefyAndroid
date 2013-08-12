package com.sit.adefy.js;

public class JSInterface {

  private static int nextID = 0;

  public String generateJSOrg() {

    return "";
  }

  public int createActor() {

    int actorID = getID();

    return actorID;
  }

  public static int getID() {
    return nextID++;
  }
}
