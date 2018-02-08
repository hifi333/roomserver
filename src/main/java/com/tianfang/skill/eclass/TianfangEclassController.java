package com.tianfang.skill.eclass;

import java.util.List;
import java.util.Set;

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

	@Autowired
	RoomBiz  samAutowired;

	@Autowired
    private SamRedisService redisService;
	
//	private Jedis jedis() {
//		return jedisPool.getResource();
//	}

	@RequestMapping("/loadeclasswhiteboardobjects")
	public String loadeclasswhiteboardobjects(String timetableclassname) {
		return samAutowired.loadeclasswhiteboardobjects(timetableclassname);
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
