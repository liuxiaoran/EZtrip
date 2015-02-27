package com.eztrip.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuxaoran
 */
public class DatabaseAcessObject {
    private DataBaseHelper helper;
    private Context context;

    public DatabaseAcessObject(Context context) {
        helper = new DataBaseHelper(context);
        this.context = context;
    }

//    public void add(MyProduct p) {
//        SQLiteDatabase db = helper.getWritableDatabase();
//        String pContent = p.getContent();
//        String pImageUrl = p.getImagePath();
//        String pContentUrl = p.getContentUrl();
//        String pHdimageUrl = p.getHDImagePath();
//
//        db.execSQL("insert into product (content, imageurl,contenturl,hdimageurl )values (?,?,?,?)", new Object[]{pContent, pImageUrl, pContentUrl, pHdimageUrl});
//
//        db.close();
//    }
//
//    public void addWeibo(MyWeibo weibo) {
//        SQLiteDatabase db = helper.getWritableDatabase();
//        String id = weibo.getId();
//        String content = weibo.getWeiboText();
//        String iamgeURl = weibo.getImagePath();
//        db.execSQL("insert into weibo (id,content,imageurl)values (?,?,?)", new Object[]{id, content, iamgeURl});
//
//
//        db.close();
//
//    }

    public void delete(int id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from product where id=?", new Object[]{id});
        db.close();
    }

    public void deleteWeibo(String id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from weibo where id=?", new Object[]{id});
        db.close();
    }

    public void delete(String pathUrl) {

        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from product where imageurl=?", new Object[]{pathUrl});
        db.close();
    }

    public boolean find(String littleImageUrl) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from product where imageurl=?", new String[]{littleImageUrl});
        try {
            if (cursor.moveToNext()) {
                return true;
            }
            return false;
        } finally {
            cursor.close();
        }

    }

//    public List<MyProduct> findAll() {
//        SQLiteDatabase db;
//        MyProduct myProduct;
//        ArrayList<MyProduct> productlist;
//        productlist = new ArrayList<MyProduct>();
//        db = helper.getReadableDatabase();
//        Cursor cursor = db.rawQuery("select * from product", null);
//        while (cursor.moveToNext()) {
//            int id = cursor.getInt(0);
//            String content = cursor.getString(1);
//            String imageurl = cursor.getString(2);
//            String contenturl = cursor.getString(3);
//            String hdimageurl = cursor.getString(4);
//            myProduct = new MyProduct(id, content, imageurl, contenturl, hdimageurl);
//            productlist.add(myProduct);
//
//            cursor.close();
//            db.close();
//        }
//
//        return productlist;
//    }
//
//    public ArrayList<MyWeibo> findAllWeibo() {
//        SQLiteDatabase db = helper.getReadableDatabase();
//        Cursor cursor = db.rawQuery("select * from weibo", null);
//        ArrayList<MyWeibo> weibolist = new ArrayList<MyWeibo>();
//        while (cursor.moveToNext()) {
//            String id = cursor.getString(0);
//            String content = cursor.getString(1);
//            String imageurl = cursor.getString(2);
//
//            MyWeibo weibo = new MyWeibo(id, content, imageurl);
//            weibolist.add(weibo);
//        }
//
//        cursor.close();
//        db.close();
//        return weibolist;
//    }
}
