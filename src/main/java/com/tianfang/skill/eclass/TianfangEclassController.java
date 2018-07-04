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

	@RequestMapping(value="/loadeclasswhiteboardobjects", produces="application/json")
	public String loadeclasswhiteboardobjects(String t,String classname) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadeclasswhiteboardobjects(classname,back);
	}

	@RequestMapping(value="/loadebanshukuobjects", produces="application/json")
	public String loadebanshukuobjects(String t) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadebanshukuobjects(back);
	}



	@RequestMapping(value="/loadteacherkejianku", produces="application/json")
	public String loadteacherkejianku(String t) {

		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
		return samAutowiredRoomBiz.loadteacherkejianku(back);
	}

	@RequestMapping(value="/loadteacherbansuku", produces="application/json")
	public String loadteacherbansuku(String t) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadteacherbansuku(back);

	}

	@RequestMapping(value="/loadteacherroomboardsku", produces="application/json")
	public String loadteacherroomboardsku(String t,String classname) {
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadteacherroomboardsku(back,classname);


	}



	@RequestMapping(value= "/saveteacherkejianku", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String saveteacherkejianku(String t, @RequestBody JSONObject kejianku)
	{
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherkejianku(back, kejianku.toJSONString());
	}



	@RequestMapping(value= "/saveteacherbanshuku", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String saveteacherbanshuku(String t, @RequestBody JSONObject banshuku)
	{
		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherbanshuku(back, banshuku.toJSONString());

	}
	@RequestMapping(value= "/saveteacherroomboardsku", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String saveteacherroomboardsku(String t, String classname, @RequestBody JSONObject roomboardsku)
	{

		JSONObject back  = checkSession(t);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherroomboardsku(back, classname,roomboardsku.toJSONString());

	}


	@RequestMapping(value= "/login", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String login(@RequestBody JSONObject loginmeta)
	{


		String userid = (String)loginmeta.get("userid");
		String password = (String) loginmeta.get("password");

		System.out.println("login rerequest:" + userid ) ;


		String myRet="";
		if(userid!=null  && password!=null) {
			 myRet = samAutowiredRoomBiz.login(userid, password);
			System.out.println("Login:" + userid + " status:" + myRet);
		}

		JSONObject responsebody = new JSONObject();
		responsebody.put("loginSessionToken", myRet);

		return responsebody.toJSONString();

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

	@RequestMapping(value= "/loadlessontable", method=RequestMethod.POST, produces="application/json")
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




	@RequestMapping(value= "/joinclassroom", method=RequestMethod.POST, produces="application/json")
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






	@RequestMapping(value= "/perteacherkejian_banshuweihu", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String perteacherkejian_banshuweihu(String token, @RequestBody JSONObject meta)
	{

		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
		{
			return  samAutowiredRoomBiz.perteacherkejian_banshuweihu(back);

		}

	}





		@RequestMapping(value= "/test123", method=RequestMethod.GET)
	@ResponseBody
	public String test123(String timetableclassname, @RequestBody JSONObject shapesjson)
	{
		System.out.println(timetableclassname);
//		System.out.println(shapesjson.toJSONString());

		
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
