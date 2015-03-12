package com.eztrip.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.eztrip.R;

import java.util.HashMap;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.framework.utils.UIHandler;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;
import utils.UserService;

/**
 * Created by xiaoran on 2015/1/19.
 */
public class LoginActivity extends Activity implements View.OnClickListener, PlatformActionListener, Handler.Callback {

    //第三方用户注册时短信验证需要的变量
    private String APPKEY = "598fccf54148";
    private String APPSECRET = "fb16d3825a8d3e2babfbf2a81a432182";

    private static final int MSG_USERID_FOUND = 1;
    private static final int MSG_LOGIN = 2;
    private static final int MSG_AUTH_CANCEL = 3;
    private static final int MSG_AUTH_ERROR = 4;
    private static final int MSG_AUTH_COMPLETE = 5;
    private static final int MSG_NOT_REGISTER = 6;
    private static final String TAG = "LoginActivity";
    public static SharedPreferences sharedPreferences;
    private Button login_btn;
    private EditText login_phone;
    private EditText login_pw;
    private Button qq_login_button;
    private Button weixin_login_button;
    private Button weibo_login_button;
    private String nickName;
    private String otherplatformId;
    private int otherplatformType = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_activity);
        //store user info sharepreferences
        sharedPreferences = LoginActivity.this.getSharedPreferences("user", MODE_PRIVATE);
        //init sharesdk
        ShareSDK.initSDK(this);

        login_btn = (Button) findViewById(R.id.login_login_btn);
        login_btn.setOnClickListener(this);
        login_phone = (EditText) findViewById(R.id.login_phone);
        login_pw = (EditText) findViewById(R.id.login_pw);
        qq_login_button = (Button) findViewById(R.id.qq_login_btn);
        weixin_login_button = (Button) findViewById(R.id.weixin_login_btn);
        weibo_login_button = (Button) findViewById(R.id.weibo_login_btn);
        qq_login_button.setOnClickListener(this);
        weixin_login_button.setOnClickListener(this);
        weibo_login_button.setOnClickListener(this);
        if (sharedPreferences.getBoolean("ischecked", false)) {
//            //TODO:进入系统
//            String phone = sharedPreferences.getString("phone", "");
//            connectToIM.connectToIM();
        }


        //第三方用户注册时短信验证需要的做的初始化操作
        loadSharePrefrence();
        initSDK();

    }

    private void initSDK() {
        // 初始化短信SDK
        SMSSDK.initSDK(this, APPKEY, APPSECRET);
        final Handler handler = new Handler(this);
        EventHandler eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                Message msg = new Message();
                msg.arg1 = event;
                msg.arg2 = result;
                msg.obj = data;
                handler.sendMessage(msg);
            }
        };
        // 注册回调监听接口
        SMSSDK.registerEventHandler(eventHandler);

    }

    private void loadSharePrefrence() {
        SharedPreferences p = getSharedPreferences("SMSSDK_SAMPLE", Context.MODE_PRIVATE);

    }

    private void setSharePrefrence() {
        SharedPreferences p = getSharedPreferences("SMSSDK_SAMPLE", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = p.edit();
        edit.putString("APPKEY", APPKEY);
        edit.putString("APPSECRET", APPSECRET);
        edit.commit();
    }

    @Override
    protected void onDestroy() {
        ShareSDK.stopSDK(this);
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login_login_btn) {
            //click login button
            String phone = login_phone.getText().toString();
            String pw = login_pw.getText().toString();
            boolean phoneRet = UserService.phonePatternMatch(phone);
            boolean pwRet = UserService.pwPatternMatch(pw);
            if (phone.equals("") || pw.equals("")) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_LONG).show();
            } else if (pwRet && phoneRet) {
                new LoginTask().execute(phone, pw);

            } else {
                Toast.makeText(this, UserService.userInputError, Toast.LENGTH_SHORT).show();
            }
            view.setEnabled(false);

        } else {
            //click  other platform button
            if (view.getId() == R.id.qq_login_btn) {
                otherplatformType = 0;
                // 获取平台列表
                Platform[] tmp = ShareSDK.getPlatformList();
                Platform platform = null;
                for (Platform p : tmp) {
                    String name = p.getName();
                    if (name.equals("QQ")) {
                        platform = p;
                        break;
                    }
                }
                platform.setPlatformActionListener(this);
                platform.showUser(null);


            } else if (view.getId() == R.id.weixin_login_btn) {
                otherplatformType = 1;
                Platform[] tmp = ShareSDK.getPlatformList();
                Platform platform = null;
                for (Platform p : tmp) {
                    String name = p.getName();
                    if (name.equals("Wechat")) {
                        platform = p;
                        break;
                    }
                }
                platform.setPlatformActionListener(this);
                platform.showUser(null);

            } else {
                //weibo login
                otherplatformType = 2;
                // 获取平台列表
                Platform[] tmp = ShareSDK.getPlatformList();
                Platform platform = null;
                for (Platform p : tmp) {
                    String name = p.getName();
                    if (name.equals("SinaWeibo")) {
                        platform = p;
                        break;
                    }
                }
                platform.setPlatformActionListener(this);
                platform.showUser(null);
            }
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        //处理第三方登陆
        switch (msg.what) {
            case MSG_USERID_FOUND: {
                Toast.makeText(this, R.string.userid_found, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_LOGIN: {
                String text = getString(R.string.logining, msg.obj);
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_CANCEL: {
                Toast.makeText(this, R.string.auth_cancel, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_ERROR: {
                Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_AUTH_COMPLETE: {
                Toast.makeText(this, R.string.auth_complete, Toast.LENGTH_SHORT).show();
            }
            break;
            case MSG_NOT_REGISTER: {
                Toast.makeText(this, "您未注册，请注册", Toast.LENGTH_SHORT).show();
            }
            break;
        }

        //处理第三方用户短信验证
        int event = msg.arg1;
        int result = msg.arg2;
        Object data = msg.obj;
        if (event == SMSSDK.EVENT_SUBMIT_USER_INFO) {
            // 短信注册成功后，返回该activity
            if (result == SMSSDK.RESULT_COMPLETE) {
                Toast.makeText(this, R.string.smssdk_user_info_submited, Toast.LENGTH_SHORT).show();


            } else {
                ((Throwable) data).printStackTrace();
            }
        }
        return false;


    }

    @Override
    public void onComplete(Platform platform, int action,
                           HashMap<String, Object> res) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_COMPLETE, this);
//            login(platform.getName(), platform.getDb().getUserId(), res);

            //res  是用户资料  ，返回的json数据
            Log.v(TAG, res.toString());
            nickName = platform.getDb().getUserName();
            otherplatformId = platform.getDb().getUserId();
            // 判断这个平台的用户是否注册过
            boolean ret = UserService.isRegisted(otherplatformType, otherplatformId);
            if (ret) {
                //用户注册过
                //用户ID
//                connectToIM.connectToIM();

            } else {
                //用户没注册过，进入第三方注册流程
                UIHandler.sendEmptyMessage(MSG_NOT_REGISTER, this);
                // 打开短信验证页面（Registerpage 就是短信验证界面）
                RegisterPage registerPage = new RegisterPage();
                registerPage.setRegisterCallback(new EventHandler() {
                    public void afterEvent(int event, int result, Object data) {
                        // 解析注册结果
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            @SuppressWarnings("unchecked")
                            HashMap<String, Object> phoneMap = (HashMap<String, Object>) data;
                            String country = (String) phoneMap.get("country");
                            String phone = (String) phoneMap.get("phone");
                            //TODO：验证成功，进入系统并且向后台注册
//                          UserService.userRegister(phone,Integer.toString(otherplatformType),otherplatformId); 不能在主线程中使用网络操作
                            UserService.registerByAsynchronous(phone, Integer.toString(otherplatformType), otherplatformId);
//                          connectToIM.connectToIM();
                        }
                    }
                });
                registerPage.show(this);

            }
        }

    }

    @Override
    public void onError(Platform platform, int action, Throwable t) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_ERROR, this);
        }
        t.printStackTrace();
    }

    @Override
    public void onCancel(Platform platform, int action) {
        if (action == Platform.ACTION_USER_INFOR) {
            UIHandler.sendEmptyMessage(MSG_AUTH_CANCEL, this);
        }
    }

    private void login(String plat, String userId, HashMap<String, Object> userInfo) {
        Message msg = new Message();
        msg.what = MSG_LOGIN;
        msg.obj = plat;
        UIHandler.sendMessage(msg, this);
    }

    class LoginTask extends AsyncTask {
        ProgressDialog dialog;
        String phone;
        String pw;

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setTitle("请等待");
            dialog.setMessage("Logging");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        /**
         * object数组中为phone 、 pw
         */
        protected Object doInBackground(Object[] objects) {
            phone = objects[0].toString();
            pw = objects[1].toString();
            String result = UserService.userLogin(phone, pw);
            return result;
        }

        @Override
        protected void onPostExecute(Object result) {
            dialog.cancel();
            Log.v("login", result.toString());
            Toast.makeText(LoginActivity.this, result.toString(), Toast.LENGTH_LONG).show();
            SharedPreferences.Editor edit = sharedPreferences.edit();
            if (result.equals("success")) {

                edit.putBoolean("ischecked", true);
                edit.putString("phone", phone);
                edit.putString("pw", pw);
                //注意等登陆成功之后才能进入聊天
//                connectToIM.connectToIM();
            } else {
                edit.putBoolean("ischecked", false);
            }
            edit.commit();
        }
    }

}
