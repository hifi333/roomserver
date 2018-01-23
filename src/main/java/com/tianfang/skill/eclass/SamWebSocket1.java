package com.tianfang.skill.eclass;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Vector;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@ServerEndpoint("/wstest")
@Service
public class SamWebSocket1 {

	
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Vector<Session> eClassroom = new Vector<Session>();
    
    private String allwsSessionRedisKey= "allwsSessionRedisKey";

    private SamRedisService redisService = null;
    
    private SamRedisService getRedisService()
    {
        if (redisService ==null)
        	  redisService = (SamRedisService) appconent.getBean(SamRedisServiceImpl.class);
        
        return redisService;
    }
	 private static ApplicationContext appconent = null;
	 public static void setAppContext(ApplicationContext aa){
	        appconent = aa;
	    }

    
    @OnOpen
    public void onOpen(Session session){
        System.out.println("client new connection:" +session.getUserProperties().toString());
        try {
        eClassroom.addElement(session);
//         getRedisService().set(allwsSessionRedisKey, eClassroom); //wsSession 不能序列号，不能集中方到redis里。 要想群发，只能调用所有服务器让他们群发给他们的客户。
        }catch(Exception ee) {ee.printStackTrace();}
//        System.out.println("client new connection:" +session.getQueryString());
//        System.out.println("client new connection:" +session.getId());
//        System.out.println("client new connection:" +session.getRequestURI());
//        System.out.println("client new connection:" +session.getRequestParameterMap().toString());

        
    }
   
    @OnMessage
    public void onMessage(String message,Session session){
    	
//	    System.out.println("new message coming");
//	    System.out.println(message);
    	    
		JSONObject jsonObject = JSONObject.parseObject(message);
		String timetableclassname = jsonObject.getString("eclassname"); // when user login and choose one classroom from his schedule just bought. 

		String viewid = jsonObject.getString("viewId");
		String shapeid = jsonObject.getString("shapeId");
		String jsondata = jsonObject.getString("jsondata");
		
		String userid = jsonObject.getString("userid"); //for lock update

		
		

	//	System.out.println("------"+ timetableclassname + "," + viewid + "," + shapeid);
		
		
		
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
		
//		System.out.println("------"+ eclassshapeidlist_redisname);
//		System.out.println("------"+ shape_rediskey);

		try {
		if(getRedisService().get(shape_rediskey) ==null)
			getRedisService().leftPush(eclassshapeidlist_redisname, shape_rediskey);
		
		getRedisService().set(shape_rediskey, jsondata);
		
//		String aa =  shape_rediskey + "--"  + getRedisService().get(shape_rediskey);
//		System.out.println("---------------------------------");
//
//		System.out.println(aa);
		}catch (Exception ee) {ee.printStackTrace();}
		
		
		
		if(jsonObject.getString("objectType") =="view"){
			 saveThisView(timetableclassname,jsonObject);
		}else if(jsonObject.getString("objectType") =="shape") {
			 saveThisShape(timetableclassname,jsonObject);
		}else if(jsonObject.getString("objectType") =="lock") {
			 saveThisLock(timetableclassname,jsonObject);
		}else if(jsonObject.getString("objectType") =="ui") {
			 saveThisUi(timetableclassname,jsonObject);

		}
    	
//    	
		//push message to all other client
//        for(Session se : eClassroom){
		
		//List allSessionList = getRedisService().getList(allwsSessionRedisKey);
		for(int i=0;i< eClassroom.size();i++)
		{
			Session se = (Session)eClassroom.get(i);
            //发送消息给远程的其他用户
            if(se != session)
            {
            	   try {
            	//	se.getAsyncRemote().sendText(jsondata);  //one shape
            		se.getBasicRemote().sendText(jsondata);  //one shape
//            		System.out.println("send.");
            	   }catch (Exception ee) {ee.printStackTrace();}
            }
        }
        
        
        
    }
    
    @OnClose
    public void onClose(Session session){
    	System.out.println("onclose  websocke ..");
    	eClassroom.remove(session);
    }
    
    @OnError
    public void onError(Throwable t){
    }
    
    
    
    private void saveThisView(String timetableclassname, JSONObject jsonObject)
    {
    	
    		this.saveThisShape(timetableclassname, jsonObject);  //the save logic
    	
    }
    
    private void saveThisShape(String timetableclassname,JSONObject jsonObject)
    {
	    	String  viewid = jsonObject.getString("viewId");
		String shapeid = jsonObject.getString("shapeId");
	
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
		
		 
		
		if(getRedisService().get(shape_rediskey) ==null)
			getRedisService().leftPush(eclassshapeidlist_redisname, shape_rediskey);  //save the id to the list
		
		getRedisService().set(shape_rediskey, jsonObject.toJSONString());  //save this object
		
		String aa =  shape_rediskey + "--"  + getRedisService().get(shape_rediskey);
		//System.out.println(aa);
    	
    }
    private void saveThisLock(String timetableclassname,JSONObject jsonObject)
    {
    		//一次多选后产生一个lock，包括多个shapes， 但一个用户只能有一个lock， 超时，点击空白，选择其他shape，都会更新这个用户的lock
	
    		String lockid = jsonObject.getString("lockId");
    	
		String eclassuserlocklist_redisname ="";
		String userlock_rediskey ="";
		
		eclassuserlocklist_redisname = timetableclassname+ "-userlocklist";
		userlock_rediskey = timetableclassname + "-lock-" + lockid ;
					 
		
		if(getRedisService().get(userlock_rediskey) ==null)
			getRedisService().leftPush(eclassuserlocklist_redisname, userlock_rediskey);
		
		getRedisService().set(userlock_rediskey, jsonObject.toJSONString());
		String aa =  userlock_rediskey + "--"  + getRedisService().get(userlock_rediskey);
		//System.out.println(aa);
    	
    }
    private void saveThisUi(String timetableclassname,JSONObject jsonObject)
    {
    			//一堂课GUI， 就一个ui state 对象
		
    			String ui_rediskey =timetableclassname+ "ui";
    					
    			
    			getRedisService().set(ui_rediskey, jsonObject.toJSONString());
    			String aa =  ui_rediskey + "--"  + getRedisService().get(ui_rediskey);
    			//System.out.println(aa);
    	
    }
   
}