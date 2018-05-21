package com.tianfang.skill.eclass;
import java.net.InetAddress;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class LBServerMakeConnect2OtherLBServer {
	
	HashSet<String> allLBIPlist_cached = new HashSet<String>();
	//HashMap<String,LBClient>  lbwsSessionList = new HashMap<String,LBClient>();
	HashSet<LBWSSession> lbwsSessionList = new HashSet<LBWSSession>();

	String   localLbServerIP = "";
	String   lbwsPort = "8887";
	
	
    public SamRedisService redisService;  //因为Java类会先执行构造方法，然后再给注解了@Autowired 的redisService注入值，
    //所以要在构造函数里使用别的@Autowired bean , 就需要在构造函数上加@Autowired, 然后把@Autowired 的Bean 通过参数传进来的.

	@Autowired
	public LBServerMakeConnect2OtherLBServer(SamRedisService redisService){
		
		this.redisService = redisService;

		
		try {
			localLbServerIP = InetAddress.getLocalHost().getHostAddress();
		}catch (Exception ee) {ee.printStackTrace();}


	     
		Thread aa = new Thread() {
			public void run() {
				while(true){
					//get all lbIP list from redis
				     Set  allLBIPlist = redisService.getSet(LBServer.rediskey_lbiplist);
//					Iterator it3 = allLBIPlist.iterator();
//					while(it3.hasNext()) {
//						System.out.println("allLBIPlist:" + it3.next());
//					}



					System.out.println("系统所有负载服务器allLBIPlist:" +allLBIPlist);

					System.out.println("Connected to current host LB list:" + lbwsSessionList);



//				     Iterator<LBWSSession> it = lbwsSessionList.iterator();
//					  while(it.hasNext()) {
//						  LBWSSession temp = it.next();
//						  System.out.println("Connected to current LB's LBIP:" + temp.targetip);
//					  }
				     
			         if(!allLBIPlist_cached.equals(allLBIPlist))  //LB server发生变化
			         {
			        	   System.out.println("update:" + allLBIPlist.toString());
			        	   System.out.println("old:" + allLBIPlist_cached.toString());
			        	 
			            allLBIPlist_cached = (HashSet) allLBIPlist;
			        	 	makeLBConnectionClients(); //if(IP changed) keep new connection 
			         }
					 try {
						Thread.sleep(60000);  //10秒扫一遍
					}catch (Exception ee) {ee.printStackTrace();}
				}
				
			}
			
		};
		
		aa.start();
		
		
	}
	
	private boolean isNewLbServerComein(String ip){
		
		  boolean findOne =false;
		  Iterator<LBWSSession> it = lbwsSessionList.iterator();
		  while(it.hasNext()) {
			  LBWSSession temp = it.next();
			  if(temp.targetip.equals(ip)){
			     findOne = true;
			     break;
			  }
		  }
		
		
		  if(findOne)  return false;
		  else return true;
		
	}
	
	private void makeLBConnectionClients()
	{
		
		  Iterator<String> it = allLBIPlist_cached.iterator();
		  
		  while(it.hasNext()) {
			  String lbIP =  it.next();
				 if(isNewLbServerComein(lbIP))   //new LB IP,  下线的留在发送出现错误的时候去除.
				 {
					
					 if(lbIP.equals(localLbServerIP))  //不能和自己链接
						 continue;
					  try {
						  LBWSSession aa = new LBWSSession(new URI("ws://"+ lbIP + ":" + lbwsPort),this);
						  aa.connect();
						  System.out.println("本地ip:" + localLbServerIP +  "建立连接 TO 目标LB:" + lbIP + " OK");
					  }catch (Exception ee) {ee.printStackTrace();}
			    }
			  
		  }

	}
	
	
	public void pushToOtherLBServer(String message) {
 
		

		Iterator<LBWSSession> it = lbwsSessionList.iterator();
		  
		  while(it.hasNext()) {
			  LBWSSession oneLbWsclient = null;
//			  try {
			  oneLbWsclient = it.next();
			  if (oneLbWsclient.targetip != localLbServerIP) {
				  // System.out.println("----------------通知LB:" + oneLbWsclient.targetip);
				  oneLbWsclient.addMoreMessage_produce(message);
			  }
		  }
//			  }catch (Exception ee) {
				  //todo remove this lbsession, 这个lbSever 下线了, 
//				  System.out.println("通知LB发生异常: 去除这个LB? 不能把, 该咋办还在想呢" + oneLbWsclient.targetip );
//				  lbwsSessionList.remove(lbip);
//				  redisService.removeSet(WsConnectionLBServer.lbiplist, oneLbWsclient.lbIP);
				  /*
				  //and 重连一下 rebuild it.
				  try {
					  LBWSSession aa = new LBWSSession(new URI("ws://"+ oneLbWsclient.targetip + ":" + lbwsPort),this.lbwsSessionList);
					  aa.connect();
				      //补发一下.
					  aa.send(message);
//					  lbwsSessionList.put(lbip,new LBClient(aa,lbip));
//					  redisService.addSet(WsConnectionLBServer.lbiplist, lbip);

					  System.out.println("通知LB发生异常: 去除这个LB, 重建恢复了." +  oneLbWsclient.targetip  );

				  }catch (Exception ee1) { System.out.println("重连LBserver 失败, 看来是真的下线了,目前不准确,connet后,马上send失败."); ee1.printStackTrace();}
				  */
//				  ee.printStackTrace();
				  
				  


		
		
		
	}
	
	
	
	public static void main(String args[]) {
		
		
		
	}
	

}

