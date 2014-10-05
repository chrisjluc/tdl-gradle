package com.ac.tdl;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.adapter.NavDrawerListAdapter;
import com.ac.tdl.managers.HashtagManager;
import com.ac.tdl.model.Hashtag;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    // used to store app title
    private CharSequence mTitle;

    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;

    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;

    // Fragments
    private HomeFragment homeFragment;
    private CalendarFragment calendarFragment;

    // Listeners
    public interface HashtagManagerListener{
        public void notifyOnDistinctHashtagChanged();
    }

    private HashtagManagerListener hashtagManagerListener = new HashtagManagerListener() {
        @Override
        public void notifyOnDistinctHashtagChanged() {
            updateDrawerList();
        }
    };

    //Managers
    private HashtagManager hashtagManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DbHelper.getInstance(this);

        hashtagManager = HashtagManager.getInstance();
        hashtagManager.setHashtagManagerListener(hashtagManagerListener);

        // enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        updateDrawerList();

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle("");
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView();
        }

    }

    /*
     *  Load drawer list
     */
    public void updateDrawerList() {

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        if (mTitle == null) {
            mTitle = navMenuTitles[0];
            setTitle(mTitle);
        }

        // nav drawer icons from resources
        navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        if(mDrawerLayout == null)
            mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_drawer);

        if(mDrawerList == null)
            mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        if (navDrawerItems == null)
            navDrawerItems = new ArrayList<NavDrawerItem>();
        else
            navDrawerItems.clear();

        // adding nav drawer items to array
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));

        for (String hashtag : hashtagManager.getDistinctHashtags())
            navDrawerItems.add(new NavDrawerItem(hashtag, navMenuIcons.getResourceId(1, -1)));

        // Recycle the typed array
        navMenuIcons.recycle();

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

        // setting the nav drawer list adapter
        if(adapter == null) {
            adapter = new NavDrawerListAdapter(getApplicationContext(),
                    navDrawerItems);
            mDrawerList.setAdapter(adapter);
        }
        adapter.notifyDataSetChanged();
    }

    /*
    *   Load task list
    */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1)
            if (resultCode == this.RESULT_OK) {
                boolean result = data.getBooleanExtra("isSaved", false);
                if (result) {
                    HomeFragment fragment = getHomeFragment();
                    fragment.notifyDataSetChanged();
                }
            }
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // display view for selected nav drawer item
            displayView(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle nav drawer on selecting action bar app icon/title
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* *
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        mTitle = navDrawerItems.get(position).getTitle();
        HomeFragment fragment = getHomeFragment();
        if (position == 0)
            fragment.loadTasks();
        else{
            fragment.loadTasks(mTitle.toString());
            mTitle = "#" + mTitle;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView() {

        FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager.findFragmentById(R.id.frame_calendar) == null) {
            fragmentManager.beginTransaction().add(R.id.frame_calendar, new CalendarFragment()).commit();
        }
        if (getFragmentManager().findFragmentById(R.id.frame_container) == null) {
            fragmentManager.beginTransaction().add(R.id.frame_container, new HomeFragment()).commit();
        }
    }

    public HomeFragment getHomeFragment() {
        if (homeFragment == null)
            homeFragment = (HomeFragment) getFragmentManager().findFragmentById(R.id.frame_container);
        return homeFragment;
    }

    public CalendarFragment getCalendarFragment() {
        if (calendarFragment == null)
            calendarFragment = (CalendarFragment) getFragmentManager().findFragmentById(R.id.frame_calendar);
        return calendarFragment;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
