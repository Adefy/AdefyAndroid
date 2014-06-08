package com.sit.adefy.showcase;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.sit.adefy.AdefyView;

public class NativeDemoFragment extends ListFragment {

  private static NativeListItem[] layouts = new NativeListItem[] {
      new NativeListItem("Lotus Elise Sportwagen / Coupé", "50.000 km | EZ 1999", "€ 21.000", R.drawable.lotus),
      new NativeListItem("Ferrari 612 Scaglietti F1 Sportwagen / Coupé", "11.390 km | EZ 2010", "€ 179.900", R.drawable.ferrari),
      new NativeListItem("Need for Speed™ Most Wanted", "Sponsored", "€ 4.49", R.drawable.nfs),
      new NativeListItem("Porsche Panamera Turbo DSG Sportwagen / Coupé", "152 km | EZ 2014", "€ 185.911", R.drawable.porsche),
      new NativeListItem("Ferrari FF Sportwagen / Coupé", "10.446 km | EZ 2012", "€ 297.000", R.drawable.ferrariff),
      new NativeListItem("Mercedes-Benz SLS AMG GT Sportwagen / Coupé", "- | EZ 2014", "€ 439.900", R.drawable.mercedes)
  };

  private boolean loadRequestMade = false;
  private NativeListAdapter mAdapter = null;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mAdapter = new NativeListAdapter(getActivity(), NativeDemoFragment.layouts);
    setListAdapter(mAdapter);
  }

  private boolean nativeToggled = false;

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if(position == 2) {
      if(!nativeToggled) {
        try {

          NativeDemoFragment.layouts[2].setExpanded(true);
          mAdapter.notifyDataSetChanged();

          nativeToggled = true;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
}