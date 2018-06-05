package com.tianfang.skill.eclass;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IBatisMapper_v_classscheduletable {

    @Select("SELECT * FROM v_classscheduletable WHERE userid = #{userid} and lessionid = #{lessionid}")
    IBatisModel_v_classscheduletable getLessonInfobyuserid_lessionid(@Param("userid") String userid , @Param("lessionid") String lessionid);

//    @Select("SELECT * FROM v_classscheduletable WHERE userid = #{userid} and begintime between #{date1} and #{date2}")
//    List<IBatisModel_v_classscheduletable> getLessonInfobyuserid_begindate(@Param("userid") String userid , @Param("date1") String date1,@Param("date2") String date2);

    @Select("SELECT * FROM v_classscheduletable WHERE userid = #{userid}")
    List<IBatisModel_v_classscheduletable> getLessonlistbyuserid(@Param("userid") String userid);

}
