package com.sit.adefy.showcase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.sit.adefy.AdefyScene;

public class LayoutListFragment extends ListFragment {

  private AdListItem[] layouts;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    layouts = new AdListItem[] {
        new AdListItem(
            "Flat Template",
            "One of our template offerings; Creatives can be generated for this template from google play store app pages.",
            R.drawable.templateportrait,
            "flat_template",
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        )
    };

    AdListAdapter adapter = new AdListAdapter(getActivity(), layouts);
    setListAdapter(adapter);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    AdListItem demo = layouts[position];

    Intent adIntent = new Intent(getActivity(), AdefyScene.class);
    adIntent.putExtra("type", demo.getType());
    adIntent.putExtra("orientation", demo.getOrientation());
    startActivity(adIntent);
  }
}
