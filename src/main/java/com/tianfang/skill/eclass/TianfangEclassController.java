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

	@RequestMapping(value="/loadclasswhiteboardobjects", produces="application/json")
	public String loadeclasswhiteboardobjects(String token,String roomid) {
		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadeclasswhiteboardobjects(roomid,back);
	}

	@RequestMapping(value="/loadteacherbanshukuobjects", produces="application/json")
	public String loadebanshukuobjects(String token) {
		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadebanshukuobjects(back);
	}



	@RequestMapping(value="/loadteacherkejiankutree", produces="application/json")
	public String loadteacherkejianku(String token) {

		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
		return samAutowiredRoomBiz.loadteacherkejianku(back);
	}

	@RequestMapping(value="/loadteacherbanshukutree", produces="application/json")
	public String loadteacherbansuku(String token) {
		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadteacherbansuku(back);

	}

	@RequestMapping(value="/loadclasswhiteboardsequence", produces="application/json")
	public String loadclasswhiteboardsequence(String token,String roomid) {
		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.loadteacherroomboardsku(back,roomid);


	}



	@RequestMapping(value= "/saveteacherkejiankutree", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String saveteacherkejiankutree(@RequestBody JSONObject meta)
	{
		String token = meta.getString("token");
		JSONObject kejiankutree = meta.getJSONObject("kejiankutree");

		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherkejianku(back, kejiankutree.toJSONString());
	}



	@RequestMapping(value= "/saveteacherbanshukutree", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String saveteacherbanshukutree( @RequestBody JSONObject meta)
	{
		String token = meta.getString("token");
		JSONObject banshukutree = meta.getJSONObject("banshukutree");


		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherbanshuku(back, banshukutree.toJSONString());

	}
	@RequestMapping(value= "/saveclasswhiteboardsequence", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String saveclasswhiteboardsequence(@RequestBody JSONObject meta)
	{

		String token = meta.getString("token");
		String roomid = meta.getString("roomid");

		JSONObject whiteboardsequence = meta.getJSONObject("whiteboardsequence");

		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			return JSONObject.toJSONString(back);
		else
			return samAutowiredRoomBiz.saveteacherroomboardsku(back, roomid,whiteboardsequence.toJSONString());

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
	public String loadlessontable(@RequestBody JSONObject meta)
	{

		String token = meta.getString("token");

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
	public String joinclassroom( @RequestBody JSONObject meta)
	{

		String token = meta.getString("token");

		JSONObject back  = checkSession(token);
		if(!back.getBoolean("session"))  // session bad
			 return JSONObject.toJSONString(back);
		else
		{
			String roomid = (String)meta.get("roomid");
			int roomaction =  meta.getIntValue("roomaction");

			return  samAutowiredRoomBiz.joinclassroom(back,roomid,roomaction);

		}

	}






	@RequestMapping(value= "/jointeacherskillweihu", method=RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String jointeacherskillweihu(@RequestBody JSONObject meta)
	{

		String token = meta.getString("token");

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
