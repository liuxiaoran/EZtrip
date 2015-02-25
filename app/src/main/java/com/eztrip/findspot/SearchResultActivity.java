package com.eztrip.findspot;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

/**
 * Created by liuxiaoran on 2015/2/25.
 */
public class SearchResultActivity extends ActionBarActivity {

    private static String TAG = "SearchResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //处理search intent
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        String query = intent.getStringExtra(SearchManager.QUERY);
        //use the query to search
        Log.v(TAG, query);
    }

}
