package com.tianfang.skill.eclass;


import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IBatisMapper_users {

    @Select("SELECT * FROM users WHERE userid = #{userid}")
    IBatisModel_users findByName(@Param("userid") String userid);


    @Insert("INSERT INTO users(userid, userfullname, password,bizunitid,role) VALUES(#{userid}, #{userfullname},#{password}, #{bizunitid}, #{role})")
    int insert(@Param("userid") String userid, @Param("userfullname") String userfullname, @Param("password") String password,@Param("bizunitid") String bizunitid,@Param("role") Integer role);

}
