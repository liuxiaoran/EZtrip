package com.eztrip.model;

import java.io.Serializable;

/**
 * Created by liuxiaoran on 15/3/11.
 * <p/>
 * 用户类，在登录时得到User  并且将User写入sp
 */
public class User implements Serializable {

    //姓名
    private String name;
    //昵称
    private String nickname;
    //电话
    private String telephone;
    //邮箱
    private String email;
    //性别
    private String sex;
    //头像
    private String avatar;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User(String id, String name, String nickname, String telephone, String email, String sex, String avatar) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.telephone = telephone;
        this.email = email;
        this.sex = sex;
        this.avatar = avatar;
    }

    //当用户刚注册完之后，只是存了用户的id
    public User(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }


}
