package com.eztrip.model;

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
        if (travelBag != null) {
            travelBag = new TravelBag();
            scenerySpotArrayList = new ArrayList<>();
        }
        return travelBag;
    }

    public void addScenery(ScenerySpot scenerySpot) {
        scenerySpotArrayList.add(scenerySpot);
    }

    public ArrayList<ScenerySpot> getScenerySpotList() {
        return this.scenerySpotArrayList;
    }
}
