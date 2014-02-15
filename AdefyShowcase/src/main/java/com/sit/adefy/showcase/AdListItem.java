package com.sit.adefy.showcase;

public class AdListItem {
  private String title;
  private String teaser;
  private int thumbnail;
  private String type;
  private int orientation;

  public AdListItem(String title, String teaser, int thumbnail, String type, int orientation) {
    this.title = title;
    this.teaser = teaser;
    this.thumbnail = thumbnail;
    this.type = type;
    this.orientation = orientation;
  }

  public String getTitle() { return title; }
  public String getTeaser() { return teaser; }
  public int getThumbnail() { return thumbnail; }
  public String getType() { return type; }
  public int getOrientation() { return orientation; }
}
