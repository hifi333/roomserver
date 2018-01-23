package com.tianfang.skill.eclass;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchAutoConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
@Service
public class TianfangEclassController {

	@Autowired
	private EclassMapper eclassMapper;

//	@Autowired
//	JedisPool jedisPool;

	@Autowired
    private SamRedisService redisService;
	
//	private Jedis jedis() {
//		return jedisPool.getResource();
//	}

	@RequestMapping("/loadeclasswhiteboardobjects")
	public String loadeclasswhiteboardobjects(String timetableclassname) {
		// eclassMapper.insert("timetable001", "2017-11-8", "");
		// Eclass myclass = eclassMapper.findBytimetableclassname("timetable001");
		// return myclass.begintime;
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。
		
		String eclassshapeidlist_redisname = timetableclassname+ "-shapeidlist";
		String eclassviewidlist_redisname = timetableclassname +"-viewidlist";
		
		String eclassuserlocklist_redisname = timetableclassname+ "-userlocklist";

		//先找出本堂课对应的viewid
		String currentviewid_rediskey = timetableclassname+"-viewid";
		String currentviewid =  (String)redisService.get(currentviewid_rediskey);
		
		if(currentviewid ==null) //新课程，你是第一个进来的。务必要确认是老师进来的。
		{
			currentviewid= createDefaulteClassroom(timetableclassname);
			if(currentviewid.length()>0)
				redisService.set(currentviewid_rediskey,currentviewid);
			else
				System.out.println("严重错误，eclass初始化失败");
		}
//		long len1 = jedis().llen(eclassviewidlist_redisname);
//		List<String> viewidlist = jedis().lrange(eclassviewidlist_redisname, 0, len1);
//		
//		
//
//		long len = jedis().llen(eclassshapeidlist_redisname);
//		List<String> shapeIdlist = jedis().lrange(eclassshapeidlist_redisname, 0, len);
//		
//		long len2 = jedis().llen(eclassuserlocklist_redisname);
//		List<String> userlocklist = jedis().lrange(eclassuserlocklist_redisname, 0, len2);
//		
		List<String> viewidlist = redisService.getList(eclassviewidlist_redisname);
		List<String> shapeIdlist  = redisService.getList(eclassshapeidlist_redisname);
		List<String> userlocklist = redisService.getList(eclassuserlocklist_redisname);
		
		
		
		JSONObject back = new JSONObject();
		//对应客户端的loadwhitedata
//		let action = {
//                type: ActionTypes.INIT_FROM_SERVER,
//                payload: classwhiteboardobjects
//            };
		
		//然后传到到每个reducer里，找到自己关心的数据，然后处理。
//	case ActionTypes.INIT_FROM_SERVER:
//	      if (action.payload.shapes) {
//	        newState = {...state};
//	        for (let shape of action.payload.shapes) {
		
		//然后在每个component里把自己reducer里计算好的数据，转换成当前view 能够使用的properties	
//		const mapStateToProps = (state) => {
//			  return {
//			    views: state.views,
//			    shapes: state.shapes,

		
		back.put("eclassroomviewid", currentviewid);   

		back.put("views", this.getJsonList(viewidlist));   
		back.put("shapes", this.getJsonList(shapeIdlist));
		
		back.put("locks", this.getJsonList(userlocklist));


		String temp = JSONObject.toJSONString(back);
	//	System.out.println("loadeclasswhiteboardobjects:"+timetableclassname +"   " +  temp);
		return temp;
	}
	private String createDefaulteClassroom(String timetableclassname)
	{
		String defaultviewid = timetableclassname +"defaultviewid";
		long timestap =new Date().getTime();

		JSONObject viewjson = new JSONObject();
		viewjson.put("viewId", defaultviewid);
		viewjson.put("title", "defaulttitle");
		viewjson.put("type", "canvas");
		viewjson.put("x", 0);
		viewjson.put("y", 0);
		viewjson.put("width", 0);
		viewjson.put("height", 0);
		viewjson.put("timestamp", timestap);
		viewjson.put("createTime", timestap);

		updateonewhiteboardobject(timetableclassname,defaultviewid,"",viewjson);
		
		return defaultviewid;
	}
	private List<JSONObject> getJsonList(List<String> shapeIdlist)
	{

		List<JSONObject> shapelistback = new ArrayList<JSONObject>(shapeIdlist.size());

		// check empty
		if (CollectionUtils.isNotEmpty(shapeIdlist)) {
			// read values
//			String[] array = new String[shapeIdlist.size()];
//			array = shapeIdlist.toArray(array);
			List<String> objectValues= redisService.multiGet(shapeIdlist);
				
			// build values
			if (CollectionUtils.isNotEmpty(objectValues)) {
				for (String value : objectValues) {
					JSONObject jsonObject = JSONObject.parseObject(value);
					shapelistback.add(jsonObject);
				}
			}
		}
		
		return shapelistback;
		
		
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
	@RequestMapping(value= "/updateonewhiteboardobject", method=RequestMethod.POST)
	public String updateonewhiteboardobject(String timetableclassname,String viewid, String shapeid,@RequestBody JSONObject attachjson)
	{
		
		System.out.println(timetableclassname + "," + viewid + "," + shapeid + "," + attachjson);

		String eclassshapeidlist_redisname ="";
		String shape_rediskey ="";
		if(shapeid ==null || shapeid.length() ==0) //this call is to update view
		{
			eclassshapeidlist_redisname = timetableclassname +"-viewidlist";
			shape_rediskey = timetableclassname + "-" + viewid;
		}
		else {
		   eclassshapeidlist_redisname = timetableclassname+ "-shapeidlist";
		   shape_rediskey = timetableclassname + "-" + viewid +"-" +shapeid ;
		}
		
		 
		
		if((String)redisService.get(shape_rediskey) ==null)
			redisService.leftPush(eclassshapeidlist_redisname, shape_rediskey);
		
		redisService.set(shape_rediskey, attachjson.toJSONString());
		String aa =  shape_rediskey + "--"  + (String)redisService.get(shape_rediskey);
//		System.out.println(aa);
		return aa;
	}

	@RequestMapping(value= "/updateuserlockforshapes", method=RequestMethod.POST)
	public String updateoneshapelock(String timetableclassname,String userid, @RequestBody JSONObject attachjson)
	{
		//一次多选后产生一个lock，包括多个shapes， 但一个用户只能有一个lock， 超时，点击空白，选择其他shape，都会更新这个用户的lock
		
		System.out.println(timetableclassname + "," + userid  + "," + attachjson);

		String eclassuserlocklist_redisname ="";
		String userlock_rediskey ="";
		
		eclassuserlocklist_redisname = timetableclassname+ "-userlocklist";
		userlock_rediskey = timetableclassname + "-lock-" + userid ;
		
		
		 
		
		if((String)redisService.get(userlock_rediskey) ==null)
			redisService.leftPush(eclassuserlocklist_redisname, userlock_rediskey);
		
		redisService.set(userlock_rediskey, attachjson.toJSONString());
		String aa =  userlock_rediskey + "--"  + (String)redisService.get(userlock_rediskey);
//		System.out.println(aa);
		return aa;
	}	 
	
	@RequestMapping(value= "/updateui", method=RequestMethod.POST)
	public String updateui(String timetableclassname, @RequestBody JSONObject attachjson)
	{
		//一堂课GUI， 就一个ui state 对象
		
		System.out.println(timetableclassname   + "," + attachjson);

		String ui_rediskey =timetableclassname+ "ui";
				
		
		redisService.set(ui_rediskey, attachjson.toJSONString());
		String aa =  ui_rediskey + "--"  + (String)redisService.get(ui_rediskey);
		System.out.println(aa);
		return aa;
	}	
	
//
//	@RequestMapping(value= "/")
//	@ResponseBody
//	public String home()
//	{
//		return "this is home";
//	}
//	
}
