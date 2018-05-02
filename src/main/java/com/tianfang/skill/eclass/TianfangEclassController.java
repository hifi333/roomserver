package com.tianfang.skill.eclass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

@RestController
@Service
public class TianfangEclassController {

//	@Autowired
//	private EclassMapper eclassMapper;

	@Autowired
	RoomBiz samAutowiredRoomBiz;

	@Autowired
    private SamRedisService redisService;
	
//	private Jedis jedis() {
//		return jedisPool.getResource();
//	}

	@RequestMapping("/loadeclasswhiteboardobjects")
	public String loadeclasswhiteboardobjects(String timetableclassname) {
		return samAutowiredRoomBiz.loadeclasswhiteboardobjects(timetableclassname);
	}

	@RequestMapping("/loadebanshukuobjects")
	public String loadebanshukuobjects(String teacheruserid) {
		return samAutowiredRoomBiz.loadebanshukuobjects(teacheruserid);
	}



	@RequestMapping("/loadteacherkejianku")
	public String loadteacherkejianku(String teacheruserid) {

		return samAutowiredRoomBiz.loadteacherkejianku(teacheruserid);
	}

	@RequestMapping("/loadteacherbansuku")
	public String loadteacherbansuku(String teacheruserid) {
		return samAutowiredRoomBiz.loadteacherbansuku(teacheruserid);
	}

	@RequestMapping("/loadteacherroomboardsku")
	public String loadteacherroomboardsku(String teacherUesrId_loginClassname) {
		return samAutowiredRoomBiz.loadteacherroomboardsku(teacherUesrId_loginClassname);
	}



	@RequestMapping(value= "/saveteacherkejianku", method=RequestMethod.POST)
	@ResponseBody
	public String saveteacherkejianku(String teacheruserid, @RequestBody JSONObject kejianku)
	{

		System.out.println("saveteacherkejianku:" + teacheruserid);
		System.out.println("saveteacherkejianku:" + kejianku.toJSONString());
		 samAutowiredRoomBiz.saveteacherkejianku(teacheruserid, kejianku.toJSONString());
		 return "";
	}



	@RequestMapping(value= "/saveteacherbanshuku", method=RequestMethod.POST)
	@ResponseBody
	public String saveteacherbanshuku(String teacheruserid, @RequestBody JSONObject banshuku)
	{

		System.out.println("saveteacherkejianku:" + teacheruserid);
		System.out.println("saveteacherkejianku:" + banshuku.toJSONString());
		samAutowiredRoomBiz.saveteacherbanshuku(teacheruserid, banshuku.toJSONString());
		return "";
	}
	@RequestMapping(value= "/saveteacherroomboardsku", method=RequestMethod.POST)
	@ResponseBody
	public String saveteacherroomboardsku(String teacherUesrId_loginClassname, @RequestBody JSONObject roomboardsku)
	{

		System.out.println("saveteacherroomboardsku:" + teacherUesrId_loginClassname);
		System.out.println("saveteacherroomboardsku:" + roomboardsku.toJSONString());

		samAutowiredRoomBiz.saveteacherroomboardsku(teacherUesrId_loginClassname, roomboardsku.toJSONString());
		return "";
	}



	
	@RequestMapping(value= "/test123", method=RequestMethod.POST)
	@ResponseBody
	public String test123(String timetableclassname, @RequestBody JSONObject shapesjson)
	{
		System.out.println(timetableclassname);
		System.out.println(shapesjson.toJSONString());

		
//		InputStream is= null;     
//		     String contentStr="";     
//		     try {         
//		         is = request.getInputStream();         
//		         contentStr= IOUtils.toString(is, "utf-8");     
//		         } catch (IOException e) {
//		                  e.printStackTrace();     
//		             }     
//		       String id=request.getParameter("id");  
		
		
		return "post return " + timetableclassname;
 	}
	}
