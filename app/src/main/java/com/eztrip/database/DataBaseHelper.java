package com.eztrip.database;

/**
 * Created by liuxaoran
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DataBaseHelper extends SQLiteOpenHelper {


    private static final int VERSION = 1;


    public DataBaseHelper(Context context) {
        super(context, "travel.db", null, VERSION);
    }

    @Override // 第一次建立时调用
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table travelbag (id text, content text,imageurl text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

    }


    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
