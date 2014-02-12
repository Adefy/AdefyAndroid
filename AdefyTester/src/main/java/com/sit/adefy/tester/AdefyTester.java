package com.sit.adefy.tester;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.sit.adefy.AdefyScene;

public class AdefyTester extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button fetchLocalButton = (Button) findViewById(R.id.fetchLocalButton);
    Button fetchStagingButton = (Button) findViewById(R.id.fetchStagingButton);
    Button fetchProductionButton = (Button) findViewById(R.id.fetchProductionButton);

    fetchLocalButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent adIntent = new Intent(getApplicationContext(), AdefyScene.class);
        adIntent.putExtra("apiKey", "lktEY4hQMf15h1CMqZGI2LIa");
        adIntent.putExtra("server", "http://192.168.0.16/api/v1/serve");
        startActivity(adIntent);
      }
    });

    fetchStagingButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent adIntent = new Intent(getApplicationContext(), AdefyScene.class);
        adIntent.putExtra("apiKey", "lktEY4hQMf15h1CMqZGI2LIa");
        adIntent.putExtra("server", "http://staging.adefy.com/api/v1/serve");
        startActivity(adIntent);
      }
    });

    fetchProductionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent adIntent = new Intent(getApplicationContext(), AdefyScene.class);
        adIntent.putExtra("apiKey", "lktEY4hQMf15h1CMqZGI2LIa");
        adIntent.putExtra("server", "http://app.adefy.com/api/v1/serve");
        startActivity(adIntent);
      }
    });
  }
}
