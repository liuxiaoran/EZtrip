package com.eztrip.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by liuxiaoran on 15/3/23.
 * 城市
 */
@DatabaseTable(tableName = "db_city")
public class City {
    //表的主键
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(columnName = "name")
    private String name;
    @DatabaseField(columnName = "cid")
    private String cid;
    @DatabaseField(columnName = "fid")
    private String fid;
    @DatabaseField(columnName = "level")
    private int level;
    @DatabaseField(columnName = "namesort")
    private String nameSort;

    public City() {

    }

    public City(String name, String cid, String fid, int level, String nameSort) {
        this.name = name;
        this.cid = cid;
        this.fid = fid;
        this.level = level;
        this.nameSort = nameSort;

    }

    public String getNameSort() {
        return nameSort;
    }

    public void setNameSort(String nameSort) {
        this.nameSort = nameSort;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }


}
