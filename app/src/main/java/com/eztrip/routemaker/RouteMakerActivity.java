package com.eztrip.routemaker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eztrip.R;
import com.eztrip.navigator.NavigationDrawerFragment;

import java.util.ArrayList;

/**
 * Created by Steve on 2015/2/4.
 */
public class RouteMakerActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks{

    private ArrayList<Fragment> fragments;
    private int currStep;
    private final String titleHead = "线路规划—";
    private String[] titles = new String[]{"基本设置","景点及住宿设置","交通设置","饮食设置","时间安排微调","最后一步"};
    private Toolbar toolbar;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_maker_activity);
        initView();
    }

    private void initView(){
        fragments = new ArrayList<>();
        Fragment basicSettings = new Fragment(){
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                return super.onCreateView(inflater, container, savedInstanceState);
            }
        };
        Fragment spotSettings = new Fragment();
        Fragment trafficSettings = new Fragment();
        Fragment dietSettings = new Fragment();
        Fragment timeSettings = new Fragment();
        Fragment finishSettings = new Fragment();
        Fragment dietInfo = new Fragment();
        fragments.add(basicSettings);
        fragments.add(spotSettings);
        fragments.add(trafficSettings);
        fragments.add(dietSettings);
        fragments.add(timeSettings);
        fragments.add(finishSettings);
        fragments.add(dietInfo);

        toolbar = (Toolbar)findViewById(R.id.routemaker_toolbar);
        setSupportActionBar(toolbar);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = "1";

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        currStep = 0;
    }



    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(titleHead + titles[currStep]);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Toast.makeText(RouteMakerActivity.this,Integer.toString(position),Toast.LENGTH_LONG).show();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_routemaker, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.routemaker_last_step){
            FragmentManager fragmentManager = getSupportFragmentManager();
            int nextStep = getFragment(-1,currStep);
            if(nextStep != -1){
                fragmentManager.beginTransaction().replace(R.id.drawerlayout_container,fragments.get(nextStep)).commit();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private int getFragment(int direction,int currentStep){
        if(currentStep == 6){
            currStep =  6 + direction * 3;
            return currStep;
        }
        else{
            if(currentStep == 0 && direction == -1){
                Toast.makeText(getApplicationContext(),"已经是第一步",Toast.LENGTH_SHORT).show();
                return -1;
            }else if(currentStep == 5 && direction == 1){
                Toast.makeText(getApplicationContext(),"完成",Toast.LENGTH_SHORT).show();
                return -1;
            }else {
                currStep = direction + currentStep;
                return currentStep;
            }
        }
    }
}
