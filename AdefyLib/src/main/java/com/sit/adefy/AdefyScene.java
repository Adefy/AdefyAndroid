package com.sit.adefy;

//
// Copyright © 2013 Spectrum IT Solutions Gmbh - All Rights Reserved
//

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class AdefyScene extends Activity {

  // Used to enable finishing from interface
  private static AdefyScene me = null;

  // Use to fetch custom ad type
  private static String adType = null;

  private AdefyView mView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    me = this;

    Intent launchedIntent = getIntent();
    String adName = launchedIntent.getStringExtra("adName");
    String apiKey = launchedIntent.getStringExtra("apiKey");
    String serverInterface = "https://app.adefy.com/api/v1/serve";

    setRequestedOrientation(launchedIntent.getIntExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));

    if(launchedIntent.getStringExtra("server") != null) {
      serverInterface = launchedIntent.getStringExtra("server");
    }

    // Null if not defined
    adType = launchedIntent.getStringExtra("type");

    mView = new AdefyView(apiKey, adName, serverInterface, this, adType);
    mView.setSceneActivity(this);
    setContentView(mView);
  }

  @Override
  protected void onPause() {
    super.onPause();
    finish();
  }

  @Override
  protected void onStop() {
    AdefyScene.me = null;
    super.onStop();
    finish();
  }

  public static AdefyScene getMe() {
    return AdefyScene.me;
  }

  public static void requestClose() {
    if(AdefyScene.me != null) {
      AdefyScene.me.finish();
      AdefyScene.me = null;
    }
  }

  public AdefyView getAdefyView() {
    return mView;
  }

  public static void setLoadPercentage(float percentage) {
    if(AdefyScene.me != null) {
      AdefyScene.me.getAdefyView().setLoadPercentage(percentage);
    }
  }
}
