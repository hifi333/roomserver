package com.tianfang.skill.eclass;

import java.util.Date;

public class IBatisModel_v_classscheduletable {

    String lessionid;
    String userid;
    String lessionname;
    Date begintime;
    Date  endtime;
    String teacheruserfullname;

    int status;
    String summary;


    public String getLessionid() {
        return lessionid;
    }

    public void setLessionid(String lessionid) {
        this.lessionid = lessionid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
