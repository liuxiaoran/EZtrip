package com.eztrip.database;

import android.content.Context;
import android.util.Log;

import com.eztrip.model.City;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by liuxiaoran on 15/3/23.
 */
public class CityDao {


    private Context context;
    private Dao<City, Integer> cityDao;
    private DatabaseHelper helper;

    public CityDao(Context context) {
        this.context = context;
        try {
            helper = DatabaseHelper.getHelper(context);
            cityDao = helper.getDao(City.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //增加一个tag
    public void addCity(City city) {

        try {
            cityDao.create(city);
        } catch (SQLException e) {
            Log.v("databases", e.toString() + "--");
            e.printStackTrace();
        }

    }

    public ArrayList<City> queryForAll() {
        ArrayList result = null;
        try {
            result = (ArrayList) cityDao.queryForAll();
            for (int i = 0; i < result.size(); i++) {
                Log.v("test", ((City) result.get(i)).getId() + ((City) result.get(i)).getName() + ((City) result.get(i)).getNameSort());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void update(City city) {

        try {
            cityDao.update(city);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateRaw(String sql) {

        try {
            cityDao.updateRaw(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeRaw(String sql) {
        try {
            cityDao.executeRaw(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
