package com.tianfang.skill.eclass;

public class IBatisModel_users {

    String userid;
    String userfullname;
    String password;
    String bizunitid;
    int role;


    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserfullname() {
        return userfullname;
    }

    public void setUserfullname(String userfullname) {
        this.userfullname = userfullname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBizunitid() {
        return bizunitid;
    }

    public void setBizunitid(String bizunitid) {
        this.bizunitid = bizunitid;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
