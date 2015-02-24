package com.eztrip.register;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eztrip.R;

import cn.smssdk.SMSSDK;

/**
 * Created by liuxiaoran on 2015/1/27.
 */
public class ValidateSMS extends Activity implements View.OnClickListener{

    private EditText auth_code_et;
    private Button send_again_btn,validate_btn;
    private TextView show_time_tv;
    Timer t;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_validate_sms);
        auth_code_et= (EditText) findViewById(R.id.auth_code_et);
        send_again_btn= (Button) findViewById(R.id.send_again_btn);
        validate_btn= (Button) findViewById(R.id.validate_button);
        show_time_tv= (TextView) findViewById(R.id.show_time_tv);
        validate_btn.setOnClickListener(this);
        send_again_btn.setOnClickListener(this);

        t = new Timer(60000,1000);
        t.start();

    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.validate_button){
            SMSSDK.submitVerificationCode("86", RegisterActivity.phone, auth_code_et.getText().toString());
        }
        else if (view.getId()==R.id.send_again_btn){
            SMSSDK.getVerificationCode("86", RegisterActivity.phone);
            send_again_btn.setVisibility(View.INVISIBLE);
            show_time_tv.setVisibility(View.VISIBLE);
            t.start();
        }
    }

    class Timer extends CountDownTimer{


        public Timer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long l) {
           show_time_tv.setText(""+l/1000);
        }

        @Override
        public void onFinish() {
            send_again_btn.setVisibility(View.VISIBLE);
            show_time_tv.setVisibility(View.INVISIBLE);
            auth_code_et.setText("");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        t.cancel();
    }
}
