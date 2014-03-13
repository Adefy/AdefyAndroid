package com.sit.adefy.showcase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sit.adefy.AdefyDownloader;
import com.sit.adefy.AdefyScene;

public class LayoutListFragment extends ListFragment {

  private static AdListItem[] layouts = new AdListItem[] {
      new AdListItem(
          "Inactive publisher ad",
          "This ad gets delivered to inactive or disabled publishers, for testing purposes.",
          R.drawable.test,
          "test",
          ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      )
      // new AdListItem(
      //     "Simple shapes example",
      //     "A simple animation and physics example.",
      //     R.drawable.shapes,
      //     "adefy_shapes_template",
      //     ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
      // )
  };

  private boolean loadRequestMade = false;

  private void loadLayoutDemo(final AdListItem demo) {
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... voids) {

        AdefyDownloader adDownloader = new AdefyDownloader(getActivity(), null, demo.getType());

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

      for(int i = 0; i < LayoutListFragment.layouts.length; i++) {
        loadLayoutDemo(LayoutListFragment.layouts[i]);
      }
    }

    AdListAdapter adapter = new AdListAdapter(getActivity(), LayoutListFragment.layouts);
    setListAdapter(adapter);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    AdListItem demo = LayoutListFragment.layouts[position];

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