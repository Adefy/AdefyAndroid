package com.sit.adefy.showcase;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends FragmentActivity {

  private String menuTitles[] = { "Ad Demos", "Tech Demos", "Native Demo", "About" };

  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private ActionBarDrawerToggle mDrawerToggle;

  private CharSequence mDrawerTitle = "Demo Categories";
  private CharSequence mTitle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.left_drawer);

    mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuTitles));
    mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
        R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_closed) {

      public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
        getActionBar().setTitle(mTitle);
      }

      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        getActionBar().setTitle(mDrawerTitle);
      }
    };

    mDrawerLayout.setDrawerListener(mDrawerToggle);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);

    // Select first item initially
    selectItem(0);
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private class DrawerItemClickListener implements ListView.OnItemClickListener {

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      selectItem(i);
    }
  }

  private void selectItem(int position) {

    Fragment fragment = null;

    if(position == 0) {
      fragment = new AdDemoListFragment();
    } else if (position == 1) {
      fragment = new TechDemoListFragment();
    } else if (position == 2) {
      fragment = new NativeDemoFragment();
    } else if (position == 3) {
      fragment = new AboutFragment();
    } else {
      return;
    }

    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction()
        .replace(R.id.content_frame, fragment)
        .commit();

    mDrawerList.setItemChecked(position, true);
    setTitle(menuTitles[position]);
    mDrawerLayout.closeDrawer(mDrawerList);
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    getActionBar().setTitle(title);
  }
}
