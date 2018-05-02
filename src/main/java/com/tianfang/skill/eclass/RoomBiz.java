package com.tianfang.skill.eclass;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.websocket.Session;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service
public class RoomBiz {
	
	@Autowired
    private SamRedisService redisService;
	
	@Autowired
    private WsConnect2OtherLBServer lbwsclient;
	

	
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SS");

	
	HashMap  allClassRoomsConnecions = new HashMap();
	
	public void newStudentCome(Session session) {

		String openQuery = session.getQueryString();  //key=classname1
		String className = openQuery.substring(openQuery.indexOf("=")+1);
		String userIdToken = "userIdToken";
		String connetionID = session.getId();

		HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
		if(thisClassroom == null) {
			allClassRoomsConnecions.put(className, new HashSet());
			thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
		}
		thisClassroom.add(new UserSession(userIdToken,session));  
		
		System.out.println("new Connection:" + thisClassroom.size()+  " " + connetionID + " " + className + " " + userIdToken);

		
	}
	
	public void oldStudentQuit(Session session) {

		System.out.println("onclose sessioin... ..");
		
		Iterator it = allClassRoomsConnecions.values().iterator();  
        while (it.hasNext()) {  	 
        	  
        	   HashSet oneClassRoomSessions = (HashSet) it.next();
        	   
       		Iterator them = oneClassRoomSessions.iterator();  
            while (them.hasNext()) {  
            	 UserSession temp =(UserSession) them.next();
            	 if(temp.session == session)  {
            		 oneClassRoomSessions.remove(temp);  //找打这个session,删除它. 
            		 System.out.println("this session found and ....removed");

            	 }
            }
        	
        	
        }
		
	}
	

	public void oneStudentDraw( String message,Session session) {
		
		try {

			if(message.indexOf("ping")!=-1)
			{
				   return;
			}

			JSONObject messagejsonObject = JSONObject.parseObject(message);

    	    
			//System.out.println("----------------本地client push开始....");

			pushMessageToClassmates(messagejsonObject,message,session);

			//System.out.println("----------------通知其他LB 开始.....");

			pushToOtherLBServer(message);

			//System.out.println("----------------保存redis开始.....");

			save2Redis(messagejsonObject,message);
			
		
		}catch (Exception ee) {ee.printStackTrace();}
		
		
	}
	private void cloneBanshuViewToRoom(String banshuClassName, String viewId, String roomClassName){


	}
	
	private void save2Redis(JSONObject messagejsonObject, String message) {
		

		String className = messagejsonObject.getString("eclassname"); // when user login and choose one classroom from his schedule just bought. 
		String xmethod = messagejsonObject.getString("xmethod"); // when user login and choose one classroom from his schedule just bought. 

		if(xmethod.equals("updateShape")) {
			String viewId = messagejsonObject.getString("viewId");
			String shapeId = messagejsonObject.getString("shapeId");
			String payload = messagejsonObject.getString("payload");

			System.out.println("updateShape:" + message);
			saveUpdateShape(className,viewId,shapeId,payload);

			
		}else if(xmethod.equals("deleteOneShape")) {
			String viewId = messagejsonObject.getString("viewId");
			String shapeId = messagejsonObject.getString("shapeId");

			saveDeleteShape(className,viewId,shapeId);
		
		}else if(xmethod.equals("createView")) {
			String viewId = messagejsonObject.getString("viewId");
			String payload = messagejsonObject.getString("payload");

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

			HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
			
			if(thisClassroom!=null && thisClassroom.size()!=0) //这个LB 已经有这个classroom 的学生了.
			{
	      		//System.out.println("fromlbpushToEachLocalClients: " + thisClassroom.size() + " " + df.format(new Date()));
	
				Iterator it = thisClassroom.iterator();  
		        while (it.hasNext()) {  	            
		            UserSession temp = (UserSession)it.next();
		            try {
			          //	temp.session.getBasicRemote().sendText(message);  //one shape
			          	temp.session.getAsyncRemote().sendText(message);  //one shape

		            }catch (Exception ee) {
		                    System.out.println("fromlbpushToEachLocalClients" + "send error to client" +  temp.userID +"  " +ee.getMessage());
		                    removeThisSessionFromServer(className, temp);
		         		   }
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
		
		lbwsclient.pushToOtherLBServer(message);
		
	}
	
	private void pushMessageToClassmates( JSONObject messagejsonObject, String message ,Session session) {
		//push message to all other clients in this classroom

		try {
			String className = messagejsonObject.getString("eclassname");
			HashSet thisClassroom = (HashSet)allClassRoomsConnecions.get(className);
      		//System.out.println(thisClassroom.size() + " " + df.format(new Date()));
            if(thisClassroom ==null) return; //没有其他client, 应该在第一个人进入的时候, 就要初始化.. todo, 后面改.
			Iterator it = thisClassroom.iterator();  
	        while (it.hasNext()) {  	            
	            UserSession temp = (UserSession)it.next();
	            try {
		            if(temp.session != session) { //不能发给自己了.
		          		temp.session.getAsyncRemote().sendText(message);  //one shape
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

	private String make_redisKey_4OneTeacherRoomboardsku(String pk) {
		return "redisKey_" + pk;

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
 
    
    
    
	public String loadeclasswhiteboardobjects(String className) {
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

		
		back.put("eclassroomcurrentviewid", currentviewid);   

		back.put("views", this.getJsonList(viewidlist));   
		back.put("shapes", this.getJsonList(shapeIdlist));
		
		back.put("locks", this.getJsonList(userlocklist));


		String temp = JSONObject.toJSONString(back);
		//System.out.println("loadeclasswhiteboardobjects:"+className +"   " +  temp);
		return temp;
	}

	public String loadebanshukuobjects(String teacheruserid){

    	String classroom = teacheruserid + "_" + "beikefakeclassname";
    	return this.loadeclasswhiteboardobjects(classroom);

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

	public String loadteacherkejianku(String teacheruserid )
	{
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。

		String redisKey_4OneTeacherKejianKu = make_redisKey_4OneTeacherKejianKu(teacheruserid);

		String kejiankuJsonString =  (String)redisService.get(redisKey_4OneTeacherKejianKu);

		if(kejiankuJsonString ==null) //老师第一次进来, 还没有自己的课件库, 系统给他分配一个缺省的课件库, 以后他可以自己修改定制
		{
			//系统要有外部工具来维护这个公共课件库
			//服务器启动的时候, 要初始化一个简单的公共课件库
			//如何都没有,返回"",  客户端要自己初始化数据结构.
			kejiankuJsonString=  (String)redisService.get(redisKey_4OneTeacherKejianKu_public());

		}

		System.out.println("kejiankuJsonString:" + kejiankuJsonString);

		return kejiankuJsonString;

	}

	public String loadteacherbansuku(String teacheruserid )
	{
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

		System.out.println("banshukuJsonString:" + banshukuJsonString);

		return banshukuJsonString;

	}

	public String loadteacherroomboardsku(String teacherUesrId_loginClassname){
		// 从redis 里读取所有json字符串数据， 转成json对象， 然后联合成整体json 字符串返回。

		String temp =  (String)redisService.get(this.make_redisKey_4OneTeacherRoomboardsku(teacherUesrId_loginClassname));
		System.out.println("loadteacherroomboardsku:"   + teacherUesrId_loginClassname);
		System.out.println("loadteacherroomboardsku:"   + temp);

		return temp;


	}

	public void saveteacherkejianku(String teacheruserid, String kejiankujsonstring){

		String redisKey_4OneTeacherKejianKu = make_redisKey_4OneTeacherKejianKu(teacheruserid);
		redisService.set(redisKey_4OneTeacherKejianKu,kejiankujsonstring);

	}

	public void saveteacherbanshuku(String teacheruserid, String banshukujsonstring){

		String redisKey_4OneTeacherBanshuKu = make_redisKey_4OneTeacherBanshuKu(teacheruserid);
		redisService.set(redisKey_4OneTeacherBanshuKu,banshukujsonstring);

	}
	public void saveteacherroomboardsku(String teacherUesrId_loginClassname, String roomboardskujsonstring){


		String redisKey= make_redisKey_4OneTeacherRoomboardsku(teacherUesrId_loginClassname);

		redisService.set(redisKey,roomboardskujsonstring);

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


