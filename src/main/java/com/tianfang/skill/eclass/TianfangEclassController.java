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
	public String loadeclasswhiteboardobjects(String t,String classname) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadeclasswhiteboardobjects(classname,back);
	}

	@RequestMapping("/loadebanshukuobjects")
	public String loadebanshukuobjects(String t,String classname) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadebanshukuobjects(classname,back);
	}



	@RequestMapping("/loadteacherkejianku")
	public String loadteacherkejianku(String t) {

		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
		return samAutowiredRoomBiz.loadteacherkejianku(back);
	}

	@RequestMapping("/loadteacherbansuku")
	public String loadteacherbansuku(String t) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadteacherbansuku(back);

	}

	@RequestMapping("/loadteacherroomboardsku")
	public String loadteacherroomboardsku(String t) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadteacherroomboardsku(back);


	}



	@RequestMapping(value= "/saveteacherkejianku", method=RequestMethod.POST)
	@ResponseBody
	public String saveteacherkejianku(String t, @RequestBody JSONObject kejianku)
	{
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherkejianku(back, kejianku.toJSONString());
	}



	@RequestMapping(value= "/saveteacherbanshuku", method=RequestMethod.POST)
	@ResponseBody
	public String saveteacherbanshuku(String t, @RequestBody JSONObject banshuku)
	{
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherbanshuku(back, banshuku.toJSONString());

	}
	@RequestMapping(value= "/saveteacherroomboardsku", method=RequestMethod.POST)
	@ResponseBody
	public String saveteacherroomboardsku(String t, @RequestBody JSONObject roomboardsku)
	{

		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherroomboardsku(back, roomboardsku.toJSONString());

	}


	@RequestMapping(value= "/login", method=RequestMethod.POST)
	@ResponseBody
	public String login(@RequestBody JSONObject loginmeta)
	{


		String userid = (String)loginmeta.get("userid");
		String password = (String) loginmeta.get("password");

		String myRet = samAutowiredRoomBiz.login(userid,password);

		System.out.println("Login:" + userid + " status:" + myRet);
		return myRet;

	}
	private JSONObject checkSession(String token){


		JSONObject back = new JSONObject();
		String userId = (String)redisService.get(token);
		if(userId!=null) {
			back.put("session", true);
			back.put("userId", userId);
		}else
			back.put("session", false);

		return back;

	}

	@RequestMapping(value= "/loadlessontable", method=RequestMethod.POST)
	@ResponseBody
	public String loadlessontable(String token,@RequestBody JSONObject meta)
	{
		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
		{
			return  samAutowiredRoomBiz.loadlessontable(back);

		}

	}




	@RequestMapping(value= "/joinclassroom", method=RequestMethod.POST)
	@ResponseBody
	public String joinclassroom(String token, @RequestBody JSONObject meta)
	{

		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			 return JSONObject.toJSONString(back);
		else
		{
			String targetClassroom = (String)meta.get("targetClassroom");
			int action =  meta.getIntValue("action");

			return  samAutowiredRoomBiz.joinclassroom(back,targetClassroom,action);

		}

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
