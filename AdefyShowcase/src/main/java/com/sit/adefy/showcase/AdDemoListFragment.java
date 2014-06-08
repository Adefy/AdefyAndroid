package com.sit.adefy.showcase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sit.adefy.AdefyDownloader;
import com.sit.adefy.AdefyScene;

public class AdDemoListFragment extends ListFragment {

  private static AdListItem[] techDemos = new AdListItem[] {
      new AdListItem(
          "Angry Birds meet Skittles", "Physics, skittles, and some good ol' suave marketing. (Also acts as a physics stress test)",
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

  private boolean loadRequestMade = false;

  private void loadTechdemo(final AdListItem demo) {
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... voids) {

        AdefyDownloader adDownloader = new AdefyDownloader(getActivity(), null, demo.getType());

        if(demo.getOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
          adDownloader.setLandscape(true);
        }

        adDownloader.fetchAd(demo.getType());
        demo.setLoaded();

        return null;
      }
    }.execute();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // Load all demos
    if(!loadRequestMade) {
      loadRequestMade = true;

      for(int i = 0; i < AdDemoListFragment.techDemos.length; i++) {
        loadTechdemo(AdDemoListFragment.techDemos[i]);
      }
    }

    AdListAdapter adapter = new AdListAdapter(getActivity(), AdDemoListFragment.techDemos);
    setListAdapter(adapter);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    AdListItem demo = AdDemoListFragment.techDemos[position];

    if(!demo.isLoaded()) {
      Toast.makeText(getActivity(), "Demo hasn't loaded yet, try again in a few seconds...", Toast.LENGTH_SHORT).show();
    } else {
      Intent adIntent = new Intent(getActivity(), AdefyScene.class);
      adIntent.putExtra("adName", demo.getType());
      adIntent.putExtra("orientation", demo.getOrientation());
      startActivity(adIntent);
    }
  }
}
