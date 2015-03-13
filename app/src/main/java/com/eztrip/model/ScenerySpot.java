package com.eztrip.model;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by liuxiaoran on 2015/2/26.
 * 景点
 */
public class ScenerySpot implements Serializable {
    public String seller;
    public String title;
    public String grade;
    public String price_min;
    public String comm_cnt;
    public String cityId;    //城市编号，目前只是北京市
    public String address;
    public String sid;        //景点id
    public String url;       //景点url
    public String imgurl;    //图片url
    public String intro;     //景点介绍

    public ScenerySpot(String title, String price_min, String comm_cnt, String url, String imgurl, String intro, String position) {
        this.title = title;
        this.price_min = price_min;
        this.comm_cnt = comm_cnt;
        this.url = url;
        this.imgurl = imgurl;
        this.intro = intro;
        this.address = position;
    }

    public ScenerySpot(String title, String price_min, String comm_cnt, String url, String imgurl, String intro, String position, String grade) {
        this.title = title;
        this.price_min = price_min;
        this.comm_cnt = comm_cnt;
        this.url = url;
        this.imgurl = imgurl;
        this.intro = intro;
        this.address = position;
        this.grade = grade;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIntro() {
        return intro;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPrice_min() {
        return price_min;
    }

    public void setPrice_min(String price_min) {
        this.price_min = price_min;
    }

    public String getComm_cnt() {
        return comm_cnt;
    }

    public void setComm_cnt(String comm_cnt) {
        this.comm_cnt = comm_cnt;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }


    public HashMap<String, String> toHashMap() {
        HashMap hashMap = new HashMap();
        hashMap.put("sid", sid);
        hashMap.put("city_id", cityId);
        hashMap.put("url", url);
        hashMap.put("seller", seller);
        hashMap.put("title", title);
        hashMap.put("grade", grade);
        hashMap.put("price_min", price_min);
        hashMap.put("comm_cnt", comm_cnt);
        hashMap.put("address", address);
        hashMap.put("imgurl", imgurl);
        hashMap.put("intro", intro);
        return hashMap;
    }


}
