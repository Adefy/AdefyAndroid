package com.sit.adefy.showcase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.sit.adefy.AdefyScene;

public class TechDemoListFragment extends ListFragment {

  private AdListItem[] techDemos;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    techDemos = new AdListItem[] {
        new AdListItem(
            "Angry Birds meet Skittles", "Physics, skittles, and some good 'ol suave marketing.",
            R.drawable.skittles,
            "skittle_template",
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ),
        new AdListItem(
            "Concrete-proof car",
            "A car maker shows off the durability and safety of a new model.",
            R.drawable.car,
            "car_template",
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        ),
        new AdListItem(
            "Watch de-construction",
            "A quality watch is great inside and out. Have a look at the internals of an elegant & precise accessory.",
            R.drawable.watch,
            "watch_template",
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )
    };

    AdListAdapter adapter = new AdListAdapter(getActivity(), techDemos);
    setListAdapter(adapter);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    AdListItem demo = techDemos[position];

    Intent adIntent = new Intent(getActivity(), AdefyScene.class);
    adIntent.putExtra("type", demo.getType());
    adIntent.putExtra("orientation", demo.getOrientation());
    startActivity(adIntent);
  }
}
