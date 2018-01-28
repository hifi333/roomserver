package com.tianfang.skill.eclass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.websocket.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class RoomBiz {
	
	@Autowired
    private SamRedisService redisService;
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SS");

	
	HashMap  allClassRoomsConnecions = new HashMap();
	
	public void newStudentCome(Session session) {

		String openQuery = session.getQueryString();  //key=classname1
		String className = openQuery.substring(openQuery.indexOf("=")+1);
		String userIdToken = "userIdToken";
		String connetionID = session.getId();

		HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
		if(thisClassroom == null)
			allClassRoomsConnecions.put(className, new HashSet());
		
		thisClassroom.add(new UserSession(userIdToken,session));  
		
		System.out.println("new Connection:" + thisClassroom.size()+  " " + connetionID + " " + className + " " + userIdToken);

		
	}
	
	public void oldStudentQuit(Session session) {

		System.out.println("onclose  websocke ..");

//		HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
//		thisClassroom.remove(aSession);
		
	}
	

	public void oneStudentDraw( String message,Session session) {
		
		try {
			

	    	    if(message.indexOf("ping")!=-1) 
	    	    {
	    	    	   return;
	    	    }
	    	    
    	    
			JSONObject jsonObject = JSONObject.parseObject(message);
			String className = jsonObject.getString("eclassname"); // when user login and choose one classroom from his schedule just bought. 
	
			
			
			String viewId = jsonObject.getString("viewId");
			String shapeId = jsonObject.getString("shapeId");
			String jsondata = jsonObject.getString("jsondata");
			String userId = jsonObject.getString("userid"); //for lock update		
			
			
			pushMessageToClassmates(className,jsondata,session);
			
			saveThisDraw(className,viewId,shapeId,userId,jsondata);

		
		}catch (Exception ee) {ee.printStackTrace();}
		
		
	}
	
	private void pushMessageToClassmates(String className, String jsondata,Session session) {
		//push message to all other clients in this classroom

		try {
			HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
	
			Iterator it = thisClassroom.iterator();  
	        while (it.hasNext()) {  	            
	            UserSession temp = (UserSession)it.next();
	            try {
		            if(temp.session != session) { //不能发给自己了.
		          		temp.session.getBasicRemote().sendText(jsondata);  //one shape
		          		System.out.println(thisClassroom.size() + " " + df.format(new Date()));
		            }
	            }catch (Exception ee) {
	                    System.out.println("send error to client" +  temp.userID +"  " +ee.getMessage());
	                    removeThisSessionFromServer(className, temp);
	         		   }
	        }
	            
        }  
        catch (Exception eee) {eee.printStackTrace();}
    
	}
	
	private void removeThisSessionFromServer(String className, UserSession  aSession) {
		HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
		thisClassroom.remove(aSession);
		
	}
	
	private void saveThisDraw(String className,String viewId, String shapeId, String userId,String jsondata) {
	  	
    		String eclassshapeidlist_redisname ="";
		String shape_rediskey ="";
		if(shapeId ==null || shapeId.length() ==0) //this call is to update view
		{
			eclassshapeidlist_redisname = className +"-viewidlist";
			shape_rediskey = className + "-" + viewId;
		}
		else {
		   eclassshapeidlist_redisname = className+ "-shapeidlist";
		   shape_rediskey = className + "-" + viewId +"-" +shapeId ;
		}
		
//		System.out.println("------"+ eclassshapeidlist_redisname);
//		System.out.println("------"+ shape_rediskey);

		try {
			if(redisService.get(shape_rediskey) ==null)
				redisService.leftPush(eclassshapeidlist_redisname, shape_rediskey);
			
			redisService.set(shape_rediskey, jsondata);  //保存这个shape
			
	//		String aa =  shape_rediskey + "--"  + getRedisService().get(shape_rediskey);
	//		System.out.println("---------------------------------");
	//
	//		System.out.println(aa);
		}catch (Exception ee) {ee.printStackTrace();}
		
//		
//		
//		if(jsonObject.getString("objectType") =="view"){
//			 saveThisView(timetableclassname,jsonObject);
//		}else if(jsonObject.getString("objectType") =="shape") {
//			 saveThisShape(timetableclassname,jsonObject);
//		}else if(jsonObject.getString("objectType") =="lock") {
//			 saveThisLock(timetableclassname,jsonObject);
//		}else if(jsonObject.getString("objectType") =="ui") {
//			 saveThisUi(timetableclassname,jsonObject);
//
//		}
    	
//    	
        
		
	}
	
	
	
	/*

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
    */
    
	
    public static void main(String argsp[])
    {
    	
    	   System.out.println(df.format(new Date()));
    }
 
	
}

class UserSession{
	
	String userID;
	Session  session;
	public UserSession(String userID, Session session) {
		this.userID = userID;
		this.session = session;
	}
	
	
	
}


