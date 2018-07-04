package com.tianfang.skill.eclass;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.midi.SysexMessage;
import javax.websocket.Session;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class RoomBiz {
	
	@Autowired
    private SamRedisService redisService;
	
	@Autowired
    private LBServerMakeConnect2OtherLBServer LB2LBConntionKeeper;

	@Autowired
	private IBatisMapper_users  iBatisMapper_users;

	@Autowired
	private IBatisMapper_v_classscheduletable  iBatisMapper_v_classscheduletable;

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SS");

	
	public HashMap  allClassRoomsConnecions = new HashMap();

	private String getUseridFromWsSession(Session session){
		String openQuery = session.getQueryString();
		//t=" + global.loginSessionToken + "&classroom=" +global.loginClassName
		int  k = openQuery.indexOf("&");
		String token = openQuery.substring(2,k);

		//check ws session
		String userId =(String)redisService.get(token);

//		System.out.println("WSOpen:" + openQuery);
//		System.out.println("token:" + token);
//		System.out.println("userId:" + userId);

		return userId;

	}
	private String getClassNameFromWsSession(Session session){
		String openQuery = session.getQueryString();
		//t=" + global.loginSessionToken + "&c=" +global.loginClassName
		int  k = openQuery.indexOf("&");
		String className = openQuery.substring(k+3);
		return className;

	}

	public void newStudentCome(Session session) {


		String userId = this.getUseridFromWsSession(session);

		if(userId !=null ) //有效loginSessionToken
		{
			String className= this.getClassNameFromWsSession(session);
			Hashtable thisClassroomWsSessions = (Hashtable) allClassRoomsConnecions.get(className);
			if (thisClassroomWsSessions == null) {
				allClassRoomsConnecions.put(className, new Hashtable());
				thisClassroomWsSessions = (Hashtable) allClassRoomsConnecions.get(className);
			}
			//覆盖session, 但要关闭旧的session
			UserSession oldSession = (UserSession)thisClassroomWsSessions.get(userId);
			if(oldSession!=null){

				try {
					oldSession.session.close();
				} catch (IOException e) {

					System.out.println("覆盖session, 但要关闭旧的session:" + userId);

				}
			}

			thisClassroomWsSessions.put(userId,new UserSession(userId, session));

			System.out.println("new student for this classroom:"+ userId +"-->"+ className + " size:" + thisClassroomWsSessions.size() + " new student wssession:" + session.toString());

		}
	}
	
	public void oldStudentQuit(Session session) {

		//check ws session
		String userId = this.getUseridFromWsSession(session);
		if(userId !=null ) //有效loginSessionToken
		{
			String className= this.getClassNameFromWsSession(session);
			Hashtable thisClassroomSessions = (Hashtable) allClassRoomsConnecions.get(className);
			thisClassroomSessions.remove(userId);
			System.out.println("student quit onclose sessioin:" + userId + " it's wssession removed from classroom:" + className);


//			Iterator it = thisClassroom.values().iterator();
//			while (it.hasNext()) {
//					UserSession temp = (UserSession) it.next();
//					if (temp.session == session) {
//						it.remove();  //找打这个session,删除它.
//						System.out.println("student quit onclose sessioin:" + userId + " it's wssession removed from classroom:" + className);
//					}
//				}

		}
		
	}
	

	public void oneStudentDraw( String message,Session session) {


		//check ws session
		String userId = this.getUseridFromWsSession(session);
		if(userId !=null) {  //有效loginSessionToken

			try {
					if (message.indexOf("ping") != -1) {
						return;
					}

					String className= this.getClassNameFromWsSession(session);

					JSONObject messagejsonObject = JSONObject.parseObject(message);


					//System.out.println("----------------本地client push开始....");
				    //只有上课模式,才需要push 消息给其他同班同学的.  如何从className 上判定是上课模式呢?
				    // 初始化ws 的只有上课模式, 和备课模式..  除去了备课classname, 就一定是上课的,就转发了.
					if(className !=getTeacherBeikeFakeClassname(userId)) {
						pushMessageToClassmates(className, message, session);
						//System.out.println("----------------通知其他LB 开始.....");
						pushToOtherLBServer(message);
					}
					//System.out.println("----------------保存redis开始.....");

					save2Redis(className,messagejsonObject);


			} catch (Exception ee) {
				ee.printStackTrace();
			}

		}
	}
	private void cloneBanshuViewToRoom(String banshuClassName, String viewId, String roomClassName){


	}
	
	private void save2Redis(String className,JSONObject messagejsonObject) {
		

		String xmethod = messagejsonObject.getString("xmethod"); // when user login and choose one classroom from his schedule just bought.

		if(xmethod.equals("updateShape")) {
			String viewId = messagejsonObject.getString("viewId");
			String shapeId = messagejsonObject.getString("shapeId");
			String payload = messagejsonObject.getString("payload");

			//System.out.println("updateShape:" + message);
			saveUpdateShape(className,viewId,shapeId,payload);

			
		}else if(xmethod.equals("deleteOneShape")) {
			String viewId = messagejsonObject.getString("viewId");
			String shapeId = messagejsonObject.getString("shapeId");

			saveDeleteShape(className,viewId,shapeId);
		
		}else if(xmethod.equals("createView")) {
			String viewId = messagejsonObject.getString("viewId");
			String payload = messagejsonObject.getString("payload");

			System.out.println("eclassname just created new view:" +   className + "," + viewId );
			saveUpdateView(className,viewId,payload);

			//切换本堂课当前打开的view
			//String currentviewid_rediskey = className+"-openingViewid";
			redisService.set(this.redisKey_4OneClassOpeningViewId(className),viewId);
				
		}else if(xmethod.equals("switchView")) {
			String viewId = messagejsonObject.getString("viewId");
			//切换本堂课当前打开的view
			redisService.set(this.redisKey_4OneClassOpeningViewId(className),viewId);
				
		}else if(xmethod.equals("cleanOneView")) {
			String viewId = messagejsonObject.getString("viewId");
		
			savecleanOneView(className,viewId);

			
		}else if(xmethod.equals("updateOneShapelock")) {
			String userId = messagejsonObject.getString("userid"); //for lock update		
			String payload = messagejsonObject.getString("payload");
			//todo , lock 先不要保存了.

		}else if(xmethod.equals("updateViewTitle")) { 
			String viewId = messagejsonObject.getString("viewId");
			String payload = messagejsonObject.getString("payload");
			saveUpdateView(className,viewId,payload);

		    System.out.println("updateViewTitle:" + payload);

		}else {
			System.out.println("not supoort method");	
		}
		
		
		
	}
	
	public void fromlbpushToEachLocalClients(String message) {
		try {
			
			JSONObject jsonObject = JSONObject.parseObject(message);
			String className = jsonObject.getString("eclassname"); // when user login and choose one classroom from his schedule just bought. 

			Hashtable thisClassroomSessions = (Hashtable)allClassRoomsConnecions.get(className);
			
			if(thisClassroomSessions!=null && thisClassroomSessions.size()!=0) //这个LB 已经有这个classroom 的学生了.
			{
	      		//System.out.println("fromlbpushToEachLocalClients: " + thisClassroom.size() + " " + df.format(new Date()));
	
				Iterator it = thisClassroomSessions.values().iterator();
		        while (it.hasNext()) {  	            
		            UserSession temp = (UserSession)it.next();
//			          	temp.session.getBasicRemote().sendText(message);  //one shape
//			          	temp.session.getAsyncRemote().sendText(message);  //老报这个错The remote endpoint was in state [TEXT_FULL_WRITING] which is an invalid state for called method
						temp.addMoreMessage_produce(message);

		        }
			}//else  	 System.out.println("fromlbpushToEachLocalClients: 没有目标教室的学生需要推送" + df.format(new Date()));

	            
        }  
        catch (Exception eee) {eee.printStackTrace();}
    
		
	}
	
	private void pushToOtherLBServer(String message) {
		//get LBserver IP list
		//make WS connecion to LBServer List
		//keep the list of LBWSConnecions
		//push jsondata to each LBServer here.

		LB2LBConntionKeeper.pushToOtherLBServer(message);
		
	}
	
	private void pushMessageToClassmates( String className, String message ,Session session) {
		//push message to all other clients in this classroom

		try {
			Hashtable thisClassroomSessions = (Hashtable)allClassRoomsConnecions.get(className);
      		//System.out.println(thisClassroom.size() + " " + df.format(new Date()));
            if(thisClassroomSessions ==null){ System.out.println("pushMessageToClassmates: find thisClassroomSessions ==null");return;} //没有其他client, 应该在第一个人进入的时候, 就要初始化.. todo, 后面改.
			Iterator it = thisClassroomSessions.values().iterator();
	        while (it.hasNext()) {  	            
	            UserSession temp = (UserSession)it.next();
//	            try {
		            if(temp.session != session) { //不能发给自己了.
//		          		temp.session.getBasicRemote().sendText(message);  //one shape
						temp.addMoreMessage_produce(message);


		            }
//	            }catch (Exception ee) {
//	                    System.out.println("send error to client, 该咋办? 不能去除这个client吧" +  temp.userId +"  " +ee.getMessage());
////	                    it.remove(); //发给一个同学,但对方接收出错, 这是不能把同学的连接去除啊, 去除后这个同学再也接收不要消息了. 只能重新登录系统了.
//	         		   }
	        }
	            
        }  
        catch (Exception eee) {eee.printStackTrace();}
    
	}
	
	private void removeThisSessionFromServer(String className, UserSession  aSession) {
		Hashtable thisClassroomSessions = (Hashtable)allClassRoomsConnecions.get(className);
		thisClassroomSessions.remove(aSession.userId);
		
	}
	
	private void saveUpdateShape(String className,String viewId, String shapeId,String payload) {
	  	
		String redisKey_4OneClassShapeIdList = redisKey_4OneClassShapeIdsList(className);
		String redisKey_4OneShapeId = redisKey_4OneShapeId(className,viewId,shapeId);
		try {
			if(redisService.get(redisKey_4OneShapeId) ==null)
				redisService.addSet(redisKey_4OneClassShapeIdList, redisKey_4OneShapeId);
			
			redisService.set(redisKey_4OneShapeId, payload);  //保存这个shape
			
		}catch (Exception ee) {ee.printStackTrace();}
				
	}
	
	

	private String redisKey_4OneClassOpeningViewId(String className) {
		return "redisKey_" + className + "-" + "openingViewid";
	}

	private String redisKey_4OneShapeId(String className,String viewId,String shapeId) {
		return "redisKey_" + className + "-" + viewId +"-" +shapeId ;
	}

	private String redisKey_4OneViewId(String className,String viewId) {
		return "redisKey_" + className + "-" + viewId ;
	}
	private String redisKey_4OneClassShapeIdsList(String className) {
		return "redisKey_" + className + "-" + "shapeidslist" ;
	}
	private String redisKey_4OneClassViewIdsList(String className) {
		return "redisKey_" + className + "-" + "viewidslist" ;
	}
	private String redisKey_4OneClassUserlocklist(String className) {
		return "redisKey_" + className + "-" + "userlockslist" ;
	}

	private String make_redisKey_4OneTeacherKejianKu(String teacheruserid){
		return "redisKey_" + teacheruserid + "-" + "kejianku" ;
	}
	private String make_redisKey_4OneTeacherBanshuKu(String teacheruserid){
		return "redisKey_" + teacheruserid + "-" + "banshuku" ;
	}

	private String make_redisKey_4OneTeacherRoomboardsku(String userId,String classname) {
		return "redisKey_"  + userId +"_" + classname +"_roomboardsku";

	}
	private String redisKey_4OneTeacherKejianKu_public(){
		return "redisKey_"   + "kejainku_public" ;

	}
	private String redisKey_4OneTeacherBanshunKu_public(){
		return "redisKey_"   + "banshuku_public" ;

	}



	private void saveUpdateView(String className,String viewId,String payload) {
	  	
		String redisKey_4OneClassViewIdList = redisKey_4OneClassViewIdsList(className);
		String redisKey_4OneViewId = redisKey_4OneViewId(className,viewId);
		try {
			if(redisService.get(redisKey_4OneViewId) ==null)
				redisService.addSet(redisKey_4OneClassViewIdList, redisKey_4OneViewId);
			
			redisService.set(redisKey_4OneViewId, payload);  //保存这个view
			
		}catch (Exception ee) {ee.printStackTrace();}
				
	}
	
   private void saveDeleteShape(String className,String viewId, String shapeId) {
	  	
		String redisKey_4OneClassShapeIdList = redisKey_4OneClassShapeIdsList(className);
		String redisKey_4OneShapeId = redisKey_4OneShapeId(className,viewId,shapeId);
		try {
			redisService.removeSet(redisKey_4OneClassShapeIdList,redisKey_4OneShapeId);
			redisService.deleteKey(redisKey_4OneShapeId);//彻底删除, 免得浪费redis 空间.
			
		}catch (Exception ee) {ee.printStackTrace();}
				
	}
	
   private void savecleanOneView(String className,String viewId) {
	  	
		String redisKey_4OneClassShapeIdList = redisKey_4OneClassShapeIdsList(className);

		try {  //todo  不能全部删除, 要分view 的,  redis 里的存储结构keys 组织要好好设计一下.
			Set allshapeid = redisService.getSet(redisKey_4OneClassShapeIdList);
						
			Iterator it = allshapeid.iterator();
			while(it.hasNext()) {
				String  tempShapeId = (String)it.next();
				String shapeJsonString = (String)redisService.get(tempShapeId);
				if(shapeJsonString.indexOf(viewId) !=-1) {  //找打了,就是这个view的,要删除它
					redisService.removeSet(redisKey_4OneClassShapeIdList,allshapeid);
					redisService.deleteKey(tempShapeId);//彻底删除, 免得浪费redis 空间.
				}
			}
		}catch (Exception ee) {ee.printStackTrace();}
				
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
 
    
    
    
	public String loadeclasswhiteboardobjects(String className,JSONObject back) {
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。
		
		String redisKey_4OneClassViewIdList = redisKey_4OneClassViewIdsList(className);
		String redisKey_4OneClassShapeIdList = redisKey_4OneClassShapeIdsList(className);		
		String redisKey_4OneClassUserlockList = redisKey_4OneClassUserlocklist(className);
		//先找出本堂课当前打开的view
		String redisKey_4OneClassOpeningViewId = redisKey_4OneClassOpeningViewId(className);
		String currentviewid =  (String)redisService.get(redisKey_4OneClassOpeningViewId);
		
		if(currentviewid ==null) //新课程，你是第一个进来的。务必要确认是老师进来的。
		{
			currentviewid= createDefaulteClassroom(className);
			redisService.set(redisKey_4OneClassOpeningViewId,currentviewid);
			
		}
//		
		Set<String> viewidlist = redisService.getSet(redisKey_4OneClassViewIdList);
		Set<String> shapeIdlist  = redisService.getSet(redisKey_4OneClassShapeIdList);
		Set<String> userlocklist = redisService.getSet(redisKey_4OneClassUserlockList);
		
		
		
		JSONObject backbody = new JSONObject();
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


		backbody.put("eclassroomcurrentviewid", currentviewid);

		backbody.put("views", this.getJsonList(viewidlist));
		backbody.put("shapes", this.getJsonList(shapeIdlist));

		backbody.put("locks", this.getJsonList(userlocklist));


		back.put("data",backbody);

		String temp = JSONObject.toJSONString(back);
		//System.out.println("loadeclasswhiteboardobjects:"+className +"   " +  temp);
		return temp;
	}


	private String createDefaulteClassroom(String timetableclassname)
	{
		String defaultviewid = timetableclassname +"defaultviewid";
		long timestap =new Date().getTime();

		JSONObject viewjson = new JSONObject();
		viewjson.put("viewId", defaultviewid);
		viewjson.put("title", defaultviewid+"defaulttitle");
		viewjson.put("type", "canvas");
		viewjson.put("x", 0);
		viewjson.put("y", 0);
		viewjson.put("width", 0);
		viewjson.put("height", 0);
		viewjson.put("objectType", "view");
		
		viewjson.put("timestamp", timestap);
		viewjson.put("createTime", timestap);

		saveUpdateView(timetableclassname,defaultviewid,viewjson.toJSONString());
		
		return defaultviewid;
	}
	private List<JSONObject> getJsonList(Set<String> shapeIdlist)
	{

		List<JSONObject> shapelistback = new ArrayList<JSONObject>(shapeIdlist.size());

		// check empty
		if (CollectionUtils.isNotEmpty(shapeIdlist)) {
			// read values
//			String[] array = new String[shapeIdlist.size()];
//			array = shapeIdlist.toArray(array);
			List<String> objectValues= redisService.multiGet(new ArrayList(shapeIdlist));
				
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

    ////////////////////////////////////////////////////////////Teacher profile data.
/*
	kejianku= {
		"subjectlist": [
		{
			"subjectname": "初三数学hide",
				"tohide":0,
				"categorylist": [
			{
				"categoryname": "函数",
					"kejianlist": [
				{"type": "234","kejianname":"一元函数", "path": "xiongmao.jpg"},
				{"type": "234","kejianname":"三角形", "path": "computer/yun.png"},
				{"type": "234","kejianname":"一元函数", "path": "computer/yun.png"},
				{"type": "234","kejianname":"三角形", "path": "computer/yun.png"}
                                        ]
			}

                                ]
		},

		 {
			"subjectname": "初三科学",
					"tohide":0,
					"categorylist": [
			{
				"categoryname": "重力",
					"kejianlist": [
				{"type": "image", "kejianname":"重力G", "path": "computer/yun.png"}
                                        ]
			},
			{
				"categoryname": "摩擦力",
					"kejianlist": [
				{"type": "image", "path": "computer/yun.png"},
				{"type": "image", "kejianname":"摩擦力分类", "path": "computer/yun.png"}
                                        ]
			}

                                ]
		}



                        ]
	};
	*/


	public String loadebanshukuobjects(JSONObject back){

		String teacheruserid = (String)back.get("userId");

		String className = this.getTeacherBeikeFakeClassname(teacheruserid);
		return this.loadeclasswhiteboardobjects(className,back);

	}


	public String loadteacherkejianku(JSONObject back) {
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。

		String teacheruserid = (String)back.get("userId");

		String redisKey_4OneTeacherKejianKu = make_redisKey_4OneTeacherKejianKu(teacheruserid);

		String kejiankuJsonString =  (String)redisService.get(redisKey_4OneTeacherKejianKu);

		if(kejiankuJsonString ==null) //老师第一次进来, 还没有自己的课件库, 系统给他分配一个缺省的课件库, 以后他可以自己修改定制
		{
			//系统要有外部工具来维护这个公共课件库
			//服务器启动的时候, 要初始化一个简单的公共课件库
			//如何都没有,返回"",  客户端要自己初始化数据结构.
			kejiankuJsonString=  (String)redisService.get(redisKey_4OneTeacherKejianKu_public());

		}



		back.put("data",JSONObject.parseObject(kejiankuJsonString));

		String temp =JSONObject.toJSONString(back);

		System.out.println("loadteacherkejianku:" + temp);
		return 	temp;
	}

	public String loadteacherbansuku(JSONObject back)
	{
		String teacheruserid = (String)back.get("userId");
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。

		String redisKey_4OneTeacherBanshuKu = make_redisKey_4OneTeacherBanshuKu(teacheruserid);

		String banshukuJsonString =  (String)redisService.get(redisKey_4OneTeacherBanshuKu);

		if(banshukuJsonString ==null) //老师第一次进来, 还没有自己的课件库, 系统给他分配一个缺省的课件库, 以后他可以自己修改定制
		{
			//系统要有外部工具来维护这个公共课件库
			//服务器启动的时候, 要初始化一个简单的公共课件库
			//如何都没有,返回"",  客户端要自己初始化数据结构.
			banshukuJsonString=  (String)redisService.get(redisKey_4OneTeacherBanshunKu_public());

		}


		back.put("data",JSONObject.parseObject(banshukuJsonString));

		String temp =JSONObject.toJSONString(back);

		System.out.println("loadteacherbansuku:" + temp);
		return 	temp;

	}

	public String loadteacherroomboardsku(JSONObject back,String classname){
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。
		String teacheruserid = (String)back.get("userId");

		String roomboardsku =  (String)redisService.get(this.make_redisKey_4OneTeacherRoomboardsku(teacheruserid,classname));

		back.put("data",JSONObject.parseObject(roomboardsku));

		String temp =JSONObject.toJSONString(back);

		System.out.println("loadteacherroomboardsku:" + temp);

		return temp;


	}

	public String saveteacherkejianku(JSONObject back, String kejiankujsonstring){

		String teacheruserid = (String)back.get("userId");

		String redisKey_4OneTeacherKejianKu = make_redisKey_4OneTeacherKejianKu(teacheruserid);
		redisService.set(redisKey_4OneTeacherKejianKu,kejiankujsonstring);

		back.put("data","ok");
		String temp =JSONObject.toJSONString(back);
		return temp;
	}

	public String saveteacherbanshuku(JSONObject back, String banshukujsonstring){

		String teacheruserid = (String)back.get("userId");
		String redisKey_4OneTeacherBanshuKu = make_redisKey_4OneTeacherBanshuKu(teacheruserid);
		redisService.set(redisKey_4OneTeacherBanshuKu,banshukujsonstring);
		back.put("data","ok");
		String temp =JSONObject.toJSONString(back);
		return temp;
	}
	public String saveteacherroomboardsku(JSONObject back, String classname,String roomboardskujsonstring){

		String teacheruserid = (String)back.get("userId");
		String redisKey= make_redisKey_4OneTeacherRoomboardsku(teacheruserid,classname);
		redisService.set(redisKey,roomboardskujsonstring);
		back.put("data","ok");
		String temp =JSONObject.toJSONString(back);
		return temp;
	}


	public String login(String userId, String password){


		System.out.println("login check database");


		String myRet = "";
		//check user*password
		boolean bCheckOK = false;
		String role = "S"; //"S" means student, T  means Teacher

		try {

//			int role =1;
//			if(userId.startsWith("139")) role=0;
//			iBatisMapper_users.insert(userId, password, "sangu", role);
			IBatisModel_users oneuser = iBatisMapper_users.findByName(userId);
			if(oneuser.password.equals(password))
                  bCheckOK = true;

			if(oneuser.role ==1)
				role ="T";
			if(oneuser.role ==0)
				role ="S";


		}catch (Exception eee) {System.out.println(eee.getMessage());}


		//make the token

		if(bCheckOK) {
			String newloginSessioinToken = role + userId + "tempsessiontoken" + new Date().getTime();
			redisService.set(newloginSessioinToken,userId);  //lgoinSessionToken 作为Key 直接放到redis里, value = UesrId, 查找效率高. 失效时间后面定.
			myRet = newloginSessioinToken;
		}

		return myRet;  //frontEnd check this token, if "", means login failed, if start with T , means Teacher, S means sutdnet

	}

	private String getTeacherBeikeFakeClassname(String userId) {
		return userId + "_beikefakeclassroom";
	}





	String perteacherkejian_banshuweihu(JSONObject back){

		//get userId
		String userId = back.getString("userId");

		//check userId  and it's role and lessonTable and this action to see OK or not,  if OK , back this classroom
		//role, 1: teacher, 0:sutdent
		//action:  1: shangke, 2: beike (only used for teacher) 3:huigu (for teacher and student)
		// use targetClassroom to get this classroom's id, front show the lessontable can only display classroom NOT ID

		boolean bOK = true;
		if(bOK) {

			back.put("eclassroom", getTeacherBeikeFakeClassname(userId));  //一个老师所有的备课板书都存储在这个特殊的classroom下
			back.put("workmodel", 0);  //0 means 维护


		}

		System.out.println("------perteacherkejian_banshuweihu:" + JSONObject.toJSONString(back));
		return JSONObject.toJSONString(back);


	}



	String joinclassroom(JSONObject back,String targetClassroom,int action){

		//get userId
		String userId = back.getString("userId");

		//check userId  and it's role and lessonTable and this action to see OK or not,  if OK , back this classroom
		//role, 1: teacher, 0:sutdent
		//action:  1: shangke, 2: beike (only used for teacher) 3:huigu (for teacher and student)
		// use targetClassroom to get this classroom's id, front show the lessontable can only display classroom NOT ID

		boolean bOK = true;
		if(bOK) {
			String classroomId = targetClassroom;

			System.out.println("joinclass:" + classroomId + "  workmodel:" + action);
			if(action ==1 || action==2 || action==3)  //1上课, 2备课， 3回顾
				back.put("workmodel", action);
			else
			{
				System.out.println("unkown workmodel aciton:" + action);
			}

			back.put("eclassroom", classroomId);


		}

		System.out.println("------JoinClassRoom:" + JSONObject.toJSONString(back));
		return JSONObject.toJSONString(back);


	}

	String loadlessontable(JSONObject back){

		//get userId
		String userId = back.getString("userId");

		//get lessonTable by this UserId

//		if(back.session)  //正常session

//		console.log(back.data);
//
//		let result={
//
//				'lessionlist':[
//		{
//			lessonid:"alessioid001a",
//					date: "2018-5-8",
//				name:"数学",
//				time: "17:30 ~ 18:30",
//				teacher: "高加盟",
//				status: "open"
//		},
//		{
//			lessonid:"alessioid002a",
//					date: "2018-5-8",
//				name:"数学",
//				time: "17:30 ~ 18:30",
//				teacher: "高加盟",
//				status: "open"
//		},
//
//
//

		ArrayList<JSONObject> lessionlist  = new ArrayList<JSONObject>();

		List<IBatisModel_v_classscheduletable>  aa = iBatisMapper_v_classscheduletable.getLessonlistbyuserid(userId);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		for(int i=0;i< aa.size();i++) {
			IBatisModel_v_classscheduletable bb = aa.get(i);


			JSONObject jsonObject = new JSONObject();
			jsonObject.put("lessionid",bb.lessionid);
			jsonObject.put("lessionname",bb.lessionname);
			jsonObject.put("begintime",formatter.format(bb.begintime));
			jsonObject.put("endtime",formatter.format(bb.endtime));
			jsonObject.put("teacheruserfullname",bb.teacheruserfullname);
			jsonObject.put("summary",bb.summary);

			lessionlist.add(jsonObject);

			System.out.println(bb.lessionid + "," + bb.lessionname + "," + bb.begintime + " ~ " + bb.endtime + "  " + bb.teacheruserfullname + "  " + bb.summary);
		}


		System.out.println("load lession table:" + lessionlist.size());
		back.put("data",lessionlist);
		return JSONObject.toJSONString(back);


	}



}

class UserSession{
	
	String 	 userId;
	Session  session;
	private Thread sendTothisWsSessionThread;
	private ArrayBlockingQueue dataBulk = new ArrayBlockingQueue(1000*10);
	//消息栈, 外面不断push进来, 内部线程不断发送出去
	long  badtrytimes_add=0;
	long  firstBadtrytimelong_add =0;
	long  lastBadtrytimeLong_add=0;



	long  badtrytimes_takesend=0;
	long  firstBadtrytimelong_takesend =0;
	long  lastBadtrytimeLong_takesend=0;


	public boolean getHealth_addBulk(){


		if(badtrytimes_add ==0) return true;
		else  if(lastBadtrytimeLong_add - firstBadtrytimelong_add > 1000*60)  //已经尝试了60秒了.
			return false;
		else
			return true;

		//badtrytimes 已经丢了的信息条数.

	}


	public boolean getHealth_takesend(){


		if(badtrytimes_takesend ==0) return true;
		else  if(lastBadtrytimeLong_takesend - firstBadtrytimelong_takesend > 1000*60)  //已经尝试了60秒了.
			return false;
		else
			return true;

		//badtrytimes 已经丢了的信息条数.

	}

	public void addMoreMessage_produce(String newMsg){

//		if(userId.equals("sxiaomi")) {
//				System.out.println( Thread.currentThread().getName() + "  UserSession: " + userId + " dataBulk size:" + dataBulk.size());
////			if (dataBulk.size() == 9)
//				System.out.println("UserSession:" + userId + "dataBulk 满了......快了");
//		}
		try {
			dataBulk.add(newMsg);
			badtrytimes_add=0;
		} catch (Exception e) {
			if(badtrytimes_add==0) firstBadtrytimelong_add = new Date().getTime();
			lastBadtrytimeLong_add = new Date().getTime();
			badtrytimes_add++;

			System.out.println(Thread.currentThread().getName() + " ===  "+e.getMessage() + "dataBulk.add(newMsg) Queue full, new message droped for this wssession.. " +
					"and need remove this wssession for it is bad, + 已经丢了消息数:" + badtrytimes_add
					+ " 持续时间:(s)" + (lastBadtrytimeLong_add - firstBadtrytimelong_add)/1000);
		}
	}

//	synchronized  public ArrayList cutAllMessageto_consume(){
//
//		if(this.dataBulk.size()>0) {
//			ArrayList temp = this.dataBulk;
//
//			this.dataBulk = new ArrayList();
//
//			return temp;
//		}
//
//		return null;
//	}



	public UserSession(String userID, Session session) {
		this.userId = userID;
		this.session = session;


		sendTothisWsSessionThread = new Thread() {
			public void run() {

				while (true) {


					try {
						String oneNewMsg = (String) dataBulk.take();
						session.getBasicRemote().sendText(oneNewMsg);
						badtrytimes_takesend =0;
//
//						if(userID.equals("sxiaomi"))
//							Thread.sleep(1000*20);

					}  catch (Exception e) {

						if(badtrytimes_takesend==0) firstBadtrytimelong_takesend = new Date().getTime();
						lastBadtrytimeLong_takesend = new Date().getTime();
						badtrytimes_takesend++;

						System.out.println(Thread.currentThread().getName() + "send error to client, 该咋办? 不能去除这个client吧" + userId + "  "
								+ e.getMessage() + "已经丢了消息数:" + badtrytimes_takesend
								+ " 持续时间:(s)" + (lastBadtrytimeLong_takesend - firstBadtrytimelong_takesend)/1000);
					}


					//Cut the dataBulk current data to this thread to send out
//
//					ArrayList toSendout = cutAllMessageto_consume();
//
//					if (toSendout != null) {
////						System.out.println("cutAllMessageto_consume:" + toSendout.size());
//
//						for (int i = 0; i < toSendout.size(); i++) {
//
//							String message = (String) toSendout.get(i);
//
//							try {
//								session.getBasicRemote().sendText(message);
//
//							} catch (Exception ee) {
//								System.out.println("send error to client, 该咋办? 不能去除这个client吧" + userId + "  " + ee.getMessage());
//							}
//						}
//					}
//
//					try {
//						Thread.sleep(100);  //10秒扫一遍
//					} catch (Exception ee) {
//						ee.printStackTrace();
//					}
//
//

				}
			}
		};

		sendTothisWsSessionThread.start();
	}
	
	
}


