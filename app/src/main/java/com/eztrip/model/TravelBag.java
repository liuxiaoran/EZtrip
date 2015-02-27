package com.eztrip.model;

import java.util.ArrayList;

/**
 * Created by liuxiaoran on 2015/2/27.
 * 单次行囊
 */
public class TravelBag {

    public ArrayList<ScenerySpot> scenerySpotArrayList = new ArrayList<>();

    private static TravelBag travelBag = new TravelBag();

    // 使用这个方法得到默认bag
    public static TravelBag getDefaultTravelBag() {
        return TravelBag.travelBag;
    }

    public void addScenery(ScenerySpot scenerySpot) {
        scenerySpotArrayList.add(scenerySpot);
    }

    public ArrayList<ScenerySpot> getScenerySpotList() {
        return this.scenerySpotArrayList;
    }
}
