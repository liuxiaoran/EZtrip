package com.eztrip;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;

import com.eztrip.model.User;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by liuxiaoran on 15/3/12.
 *
 */
public class MyContext {

    private SharedPreferences sharedPreferences;

    private Context context;
    public static MyContext newInstance(Context context){

        return new MyContext(context);
    }
    public MyContext(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

    }

    public SharedPreferences  getSharedPreferences(){
        return sharedPreferences;
    }

    public void saveCurrentUser(User user){

        ByteArrayOutputStream  bao = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(user);
            byte[] array = bao.toByteArray();

            String revertUser = new String (Base64.encode(array, Base64.DEFAULT));
            SharedPreferences sp=getSharedPreferences();
            sp.edit().putString("user",revertUser).commit();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public User getCurrentUser (){
        String userstring = getSharedPreferences().getString("user",null);
        if (userstring != null) {
            byte[] base64 = Base64.decode(userstring.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream bai = new ByteArrayInputStream(base64);
            try {
                ObjectInputStream ois = new ObjectInputStream(bai);
                User user = (User) ois.readObject();
                return user;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }


}
