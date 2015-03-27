package com.eztrip.model;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by liuxiaoran on 2015/2/27.
 * 单次行囊
 */
public class TravelBag {

    public static ArrayList<ScenerySpot> scenerySpotArrayList;

    private static TravelBag travelBag;

    // 使用这个方法得到默认bag
    public static TravelBag getInstance() {
        if (travelBag == null) {
            travelBag = new TravelBag();
        }
        if(scenerySpotArrayList == null)
            scenerySpotArrayList = new ArrayList<>();
        return travelBag;
    }

    public boolean addScenery(ScenerySpot scenerySpot) {
        boolean flag = true;
        for(int i = 0; i < scenerySpotArrayList.size(); i++) {
            if(scenerySpotArrayList.get(i).title.equals(scenerySpot.title))
                flag = false;
        }
        if(flag)
            scenerySpotArrayList.add(scenerySpot);
        return flag;
    }

    public ArrayList<ScenerySpot> getScenerySpotList() {
        return TravelBag.scenerySpotArrayList;
    }
}
