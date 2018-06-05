package com.tianfang.skill.eclass;

import java.util.Date;

public class IBatisModel_lessions {

    String lessionid;
    String bizunitid;
    String lessionname;
    Date  begintime;
    Date  endtime;
    String teacheruserid;
    String summary;
    int status;
    String teacheruserfullname;


    public String getTeacheruserid() {
        return teacheruserid;
    }

    public void setTeacheruserid(String teacheruserid) {
        this.teacheruserid = teacheruserid;
    }

    public String getLessionid() {
        return lessionid;
    }

    public void setLessionid(String lessionid) {
        this.lessionid = lessionid;
    }

    public String getBizunitid() {
        return bizunitid;
    }

    public void setBizunitid(String bizunitid) {
        this.bizunitid = bizunitid;
    }

    public String getLessionname() {
        return lessionname;
    }

    public void setLessionname(String lessionname) {
        this.lessionname = lessionname;
    }

    public Date getBegintime() {
        return begintime;
    }

    public void setBegintime(Date begintime) {
        this.begintime = begintime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public String getTeacheruserfullname() {
        return teacheruserfullname;
    }

    public void setTeacheruserfullname(String teacheruserfullname) {
        this.teacheruserfullname = teacheruserfullname;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
