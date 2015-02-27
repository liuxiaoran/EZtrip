package com.eztrip.findspot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.eztrip.R;

/**
 * Created by liuxiaoran on 2015/2/26.
 */
public class SceneryWebView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.findspot_activity_webview);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        WebView myWebView = (WebView) findViewById(R.id.findspot_webview);
        myWebView.loadUrl(url);
    }
}
