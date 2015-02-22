package com.eztrip.register;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.R;
import  utils.UserService;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * Created by xiaoran on 2015/1/19.
 */
public class RegisterActivity extends Activity implements View.OnClickListener {
    public static String phone;
    // 填写从短信SDK应用后台注册得到的APPKEY
    private static String APPKEY = "598fccf54148";
    // 填写从短信SDK应用后台注册得到的APPSECRET
    private static String APPSECRET = "fb16d3825a8d3e2babfbf2a81a432182";
    String pw;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            int event = msg.arg1;
            int result = msg.arg2;
            Object data = msg.obj;
            Log.e("event", "event=" + event);
            if (result == SMSSDK.RESULT_COMPLETE) {
                Log.v("event","arrive");
                //短信注册成功后，返回此Activity
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {//提交验证码成功
                    Toast.makeText(getApplicationContext(), "提交验证码成功", Toast.LENGTH_SHORT).show();
                    //短信验证成功，注册
                    new RegisterTask().execute(phone, pw);

                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                    Toast.makeText(getApplicationContext(), "验证码已经发送", Toast.LENGTH_SHORT).show();
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {//返回支持发送验证码的国家列表
                    Toast.makeText(getApplicationContext(), "获取国家列表成功", Toast.LENGTH_SHORT).show();

                }
            } else {
                Toast.makeText(getApplicationContext(), "验证码错误", Toast.LENGTH_SHORT).show();
                ((Throwable) data).printStackTrace();
            }

        }

    };
    private TextView register_tv;
    private EditText phone_et, pw_et;
    private Button register_btn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        phone_et = (EditText) findViewById(R.id.register_phone_et);
        pw_et = (EditText) findViewById(R.id.register_pw_et);
        register_tv = (TextView) findViewById(R.id.register_protocol);
        register_tv.setText(
                Html.fromHtml(
                        "点击注册表示同意" +
                                "<a href=\"http://www.google.com\">《eztrip软件许可及服务协议》</a> "));
        register_tv.setMovementMethod(LinkMovementMethod.getInstance());
        register_btn = (Button) findViewById(R.id.register_btn);
        register_btn.setOnClickListener(this);

        SMSSDK.initSDK(this, APPKEY, APPSECRET);
        EventHandler eh = new EventHandler() {

            @Override
            public void afterEvent(int event, int result, Object data) {

                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
                Log.v("event","arrive event "+msg.obj +" "+msg.arg1+" "+msg.arg2);
            }

        };
        SMSSDK.registerEventHandler(eh);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterAllEventHandler();

    }

    @Override
    public void onClick(View view) {

        phone = phone_et.getText().toString();

        pw = pw_et.getText().toString();
        boolean phoneRet = UserService.phonePatternMatch(phone);
        boolean pwRet = UserService.pwPatternMatch(pw);

        if (phone.equals("") || pw.equals("")) {
            Toast.makeText(this, "电话或者密码不能为空", Toast.LENGTH_LONG).show();
        } else if (pwRet && phoneRet) {
            //TODO:短信验证andvalidate success之后向后台注册
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("验证手机号");
            builder.setMessage("将发送验证码到您的手机");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    SMSSDK.getVerificationCode("86", phone);
                    Intent intent = new Intent(RegisterActivity.this, ValidateSMS.class);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }

            });
            builder.create().show();

        } else {
            Toast.makeText(this, UserService.userInputError, Toast.LENGTH_LONG).show();
        }
    }


    class RegisterTask extends AsyncTask {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(RegisterActivity.this);
            dialog.setTitle("请等待");
            dialog.setMessage("Logging");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return UserService.userRegister(objects[0].toString(), objects[1].toString());
        }

        protected void onPostExecute(Object result) {
            dialog.cancel();
            boolean ret = Boolean.parseBoolean(result.toString());
            if (ret) {
//                //注册成功,进入系统
//                ConnectToIM connectToIM=new ConnectToIM(RegisterActivity.this);
//                connectToIM.connectToIM();

            } else {
                Toast.makeText(RegisterActivity.this, UserService.registerErrorMessage, Toast.LENGTH_LONG).show();

            }

        }
    }
}
