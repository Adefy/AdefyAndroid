package com.sit.adefy.objects;

import org.json.JSONException;
import org.json.JSONObject;

// Used by AdefyDownloader, stores user information, will be used for targeting
public class UserInformation {

  public String uuid;
  public String username;
  public int screenWidth;
  public int screenHeight;

  public String toJSONString() {

    try {

      JSONObject json = new JSONObject();

      json.put("uuid", uuid);
      json.put("username", username);
      json.put("screenwidth", screenWidth);
      json.put("screenheight", screenHeight);

      return json.toString();

    } catch (JSONException e) {
      e.printStackTrace();
    }

    return "";
  }
}