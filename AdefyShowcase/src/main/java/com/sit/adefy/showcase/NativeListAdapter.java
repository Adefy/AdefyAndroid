package com.sit.adefy.showcase;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sit.adefy.AdefyView;

public class NativeListAdapter extends ArrayAdapter {
  private final Context context;
  private final NativeListItem[] values;

  public NativeListAdapter(Context context, NativeListItem[] values) {
    super(context, R.layout.ad_list_row);

    this.context = context;
    this.values = values;
  }

  @Override
  public int getCount() {
    return values.length;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    NativeListItem item = values[position];

    if(!item.isExpanded()) {

      // Un-expanded and un-initialized
      if (row == null) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.native_list_row, parent, false);
      }

      // Un-expanded and initialized, just update data
      if (row != null) {
        TextView title = (TextView) row.findViewById(R.id.title);
        TextView location = (TextView) row.findViewById(R.id.location);
        TextView price = (TextView) row.findViewById(R.id.price);
        ImageView image = (ImageView) row.findViewById(R.id.image);

        title.setText(item.getTitle());
        location.setText(item.getLocation());
        price.setText(item.getPrice());
        image.setImageResource(item.getImage());
      }

    } else {

      final int targetHeight = 720;

      final View adView = getAdView();
      row = adView;
      row.setVisibility(View.VISIBLE);
      row.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64));

      // Set up a handler to expand us
      Animation a = new Animation()
      {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {

          adView.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
          adView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
          return true;
        }

      };

      a.setDuration(500);
      a.setStartOffset(100);

      a.setAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
          ((AdefyView) adView).continueInit();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
      });

      row.startAnimation(a);
    }

    return row;
  }

  private View getAdView() {

    // Create ad scene
    String adName = "watch_template";
    String serverInterface = "https://app.adefy.com/api/v1/serve";

    return new AdefyView(null, adName, serverInterface, context, null, true);
  }
}