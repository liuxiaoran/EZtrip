package com.eztrip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eztrip.findspot.FindSpotMainFragment;
import com.eztrip.navigator.NavigationDrawerFragment;
import com.eztrip.routemaker.RouteMakerFragment;

import java.io.File;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final int ACTIVITY_PICKLOCAL = 2;
    private static final int ACTIVITY_PICKCAMERA = 1;
    public static Bitmap bitmap;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Fragment currFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private Toolbar mToolbar;
    private File imgFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = "首页";

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager;
        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                currFragment = MainFragment.newInstance(getBaseContext());
                fragmentManager.beginTransaction()
                        .replace(R.id.container, currFragment)
                        .commit();
                break;
//            case 1:
//                fragmentManager = getSupportFragmentManager();
////                currFragment = ProductHallFragment.newInstance(getBaseContext());
//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, currFragment)
//                        .commit();
//                break;
            case 2:
                fragmentManager = getSupportFragmentManager();
                currFragment = FindSpotMainFragment.newInstance(getBaseContext());
                fragmentManager.beginTransaction()
                        .replace(R.id.container, currFragment)
                        .commit();
                break;
            case 3:
                fragmentManager = getSupportFragmentManager();
                currFragment = new RouteMakerFragment();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, currFragment)
                        .commit();
                break;
//            case 4:
//                fragmentManager = getSupportFragmentManager();
////                currFragment = FinalModelSpaceFragment.newInstance(getBaseContext());
//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, currFragment)
//                        .commit();
//                break;
//            case 5:
//                fragmentManager = getSupportFragmentManager();
////                currFragment = DesignChooseFragment.newInstance(getBaseContext());
//                fragmentManager.beginTransaction()
//                        .replace(R.id.container, currFragment)
//                        .commit();
//                break;


            default:
                break;
        }
        onSectionAttached(position + 1);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4_fragment1);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                mTitle = getString(R.string.title_section6);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void setActionbarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.action_clear_cache) {
            cleanInternalCache(MainActivity.this);
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (currFragment instanceof DesignChooseFragment)
//            currFragment.onActivityResult(requestCode, resultCode, data);
//    }

    public void cleanInternalCache(Context context) {//清除缓存
        deleteFilesByDirectory(context.getCacheDir());
    }

    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
            Toast.makeText(MainActivity.this, "缓存清理完毕", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_fragment, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            if (currFragment instanceof RouteMakerFragment) {
                ((RouteMakerFragment) currFragment).moveToLastStep();
            }
        return true;
    }
}
