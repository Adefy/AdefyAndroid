package com.sit.adefy.showcase;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdListAdapter extends ArrayAdapter {
  private final Context context;
  private final AdListItem[] values;

  public AdListAdapter(Context context, AdListItem[] values) {
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
    AdListItem item = values[position];

    if(row == null) {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      row = inflater.inflate(R.layout.ad_list_row, parent, false);
    }

    if (row != null) {
      TextView title = (TextView) row.findViewById(R.id.title);
      TextView teaser = (TextView) row.findViewById(R.id.teaser);
      ImageView icon = (ImageView) row.findViewById(R.id.icon);

      title.setText(item.getTitle());
      teaser.setText(item.getTeaser());
      icon.setImageResource(item.getThumbnail());
    }

    return row;
  }
}