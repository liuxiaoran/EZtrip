package com.eztrip.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import com.eztrip.R;
import com.eztrip.login.LoginActivity;
import com.eztrip.register.RegisterActivity;

/**
 * Created by xiaoran on 2015/1/19.
 */
public class WelcomeActivity extends Activity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_activity);

        Button login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
        Button register_button = (Button) findViewById(R.id.register_button);
        register_button.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        Intent intent;
        if (view.getId() == R.id.login_button) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, RegisterActivity.class);
            intent.putExtra("whosend", 0);
        }

        startActivity(intent);
    }
}
