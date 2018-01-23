package com.tianfang.skill.eclass;



import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EclassMapper {
	@Select("SELECT * FROM ECLASS WHERE timetableclassname = #{timetableclassname}")
    Eclass findBytimetableclassname(@Param("timetableclassname") String timetableclassname);
    @Insert("INSERT INTO ECLASS(timetableclassname, begintime,attachjson) VALUES(#{timetableclassname}, #{begintime},#{attachjson})")
    int insert(@Param("timetableclassname") String timetableclassname, @Param("begintime") String begintime,@Param("attachjson") String attachjson);

}

