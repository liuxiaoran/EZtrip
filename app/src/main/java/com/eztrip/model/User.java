package com.eztrip.model;

import java.io.Serializable;

/**
 * Created by liuxiaoran on 15/3/11.
 *
 * 用户类，在登录时得到User  并且将User写入sp
 */
public class User implements Serializable {

    private String name;
    private String nickname;
    private String telephone;
    private String email;
    private String sex;
    private String avatar;

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
