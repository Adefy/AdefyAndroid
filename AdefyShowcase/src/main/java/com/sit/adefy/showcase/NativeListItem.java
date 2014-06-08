package com.sit.adefy.showcase;

public class NativeListItem {

  private String title;
  private String location;
  private String price;
  private int image;
  private boolean expanded = false;

  public NativeListItem(String title, String location, String price, int image) {
    this.title = title;
    this.location = location;
    this.price = price;
    this.image = image;
  }

  public String getTitle() {
    return title;
  }

  public String getLocation() {
    return location;
  }

  public String getPrice() {
    return price;
  }

  public int getImage() {
    return image;
  }

  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
  }

  public boolean isExpanded() {
    return expanded;
  }
}
