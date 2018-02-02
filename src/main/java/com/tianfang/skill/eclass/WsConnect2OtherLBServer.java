package com.tianfang.skill.eclass;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI; 
import java.net.URISyntaxException; 
import java.nio.ByteBuffer; 
import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.java_websocket.WebSocket.READYSTATE; 
import org.java_websocket.client.WebSocketClient; 


import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class WsConnect2OtherLBServer {
	
	HashSet<String>  lbserverIPset = new HashSet<String>();
	//HashMap<String,LBClient>  lbserverWSClients = new HashMap<String,LBClient>();
	HashSet<LbWebSocketClient>  lbserverWSClients = new HashSet<LbWebSocketClient>();

	String   localLbServerIP = "";
	String   lbwsPort = "8887";
	
	
    public SamRedisService redisService;  //因为Java类会先执行构造方法，然后再给注解了@Autowired 的redisService注入值，
    //所以要在构造函数里使用别的@Autowired bean , 就需要在构造函数上加@Autowired, 然后把@Autowired 的Bean 通过参数传进来的.

	@Autowired
	public WsConnect2OtherLBServer(SamRedisService redisService){
		
		this.redisService = redisService;
		
		
		try {
			localLbServerIP = InetAddress.getLocalHost().getHostAddress();
		}catch (Exception ee) {ee.printStackTrace();}


	     
		Thread aa = new Thread() {
			public void run() {
				while(true){
					//get all lbIP list from redis
				     Set  allLBIPlist = redisService.getSet(WsConnectionLBServer.lbiplist); 
				     System.out.println("found allLBIPlist:" + allLBIPlist + "lbserverWSClients:" + lbserverWSClients.size());
				     
				     Iterator<LbWebSocketClient> it = lbserverWSClients.iterator();
					  while(it.hasNext()) {
						  LbWebSocketClient temp = it.next();	
						  System.out.println("LbWebSocketClient:" + temp.targetip);
					  }
				     
			         if(!lbserverIPset.equals(allLBIPlist))  //LB server发生变化	
			         {
			        	   System.out.println("update:" + allLBIPlist.toString());
			        	   System.out.println("old:" + lbserverIPset.toString());
			        	 
			            lbserverIPset = (HashSet) allLBIPlist;
			        	 	makeLBConnectionClients(); //if(IP changed) keep new connection 
			         }
					 try {
						Thread.sleep(10000);  //10秒扫一遍
					}catch (Exception ee) {ee.printStackTrace();}
				}
				
			}
			
		};
		
		aa.start();
		
		
	}
	
	private boolean isNewLbServerComein(String ip){
		
		  boolean findOne =false;
		  Iterator<LbWebSocketClient> it = lbserverWSClients.iterator();
		  while(it.hasNext()) {
			  LbWebSocketClient temp = it.next();				  
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
		
		  Iterator<String> it = lbserverIPset.iterator();
		  
		  while(it.hasNext()) {
			  String lbIP =  it.next();
				 if(isNewLbServerComein(lbIP))   //new LB IP,  下线的留在发送出现错误的时候去除.
				 {
					
					 if(lbIP.equals(localLbServerIP))  //不能和自己链接
						 continue;
					  try {
						  System.out.println("本地ip:" + localLbServerIP);
						  System.out.println("目标ip:" + lbIP);
						  LbWebSocketClient aa = new LbWebSocketClient(new URI("ws://"+ lbIP + ":" + lbwsPort),this);
						  aa.connect();					  
						  System.out.println("new LB conneciton:" +lbIP);
					  }catch (Exception ee) {ee.printStackTrace();}
			    }
			  
		  }

	}
	
	
	public void pushToOtherLBServer(String message) {
 
		

		Iterator<LbWebSocketClient> it = lbserverWSClients.iterator();
		  
		  while(it.hasNext()) {
			  LbWebSocketClient oneLbWsclient = null;
			  try {
				   oneLbWsclient = it.next();
				  if(oneLbWsclient.targetip != localLbServerIP)
				  {
					  System.out.println("----------------通知LB:" + oneLbWsclient.targetip);
					  oneLbWsclient.send(message);
				  }
			  }catch (Exception ee) {
				  //todo remove this lbsession, 这个lbSever 下线了, 
				  System.out.println("通知LB发生异常: 去除这个LB" + oneLbWsclient.targetip );
//				  lbserverWSClients.remove(lbip);
//				  redisService.removeSet(WsConnectionLBServer.lbiplist, oneLbWsclient.lbIP);
				  /*
				  //and 重连一下 rebuild it.
				  try {
					  LbWebSocketClient aa = new LbWebSocketClient(new URI("ws://"+ oneLbWsclient.targetip + ":" + lbwsPort),this.lbserverWSClients);
					  aa.connect();
				      //补发一下.
					  aa.send(message);
//					  lbserverWSClients.put(lbip,new LBClient(aa,lbip));
//					  redisService.addSet(WsConnectionLBServer.lbiplist, lbip);

					  System.out.println("通知LB发生异常: 去除这个LB, 重建恢复了." +  oneLbWsclient.targetip  );

				  }catch (Exception ee1) { System.out.println("重连LBserver 失败, 看来是真的下线了,目前不准确,connet后,马上send失败."); ee1.printStackTrace();}
				  */
				  ee.printStackTrace();
				  
				  
				  }
		  }
		
		
		
	}
	
	
	
	public static void main(String args[]) {
		
		
		
	}
	

}

class LbWebSocketClient extends WebSocketClient{
		
	WsConnect2OtherLBServer lbServer;
	   String targetip;
	   public LbWebSocketClient(URI serverUri,WsConnect2OtherLBServer lbServer) {
			super(serverUri);
			this.lbServer = lbServer;
			this.targetip = serverUri.getHost();
		}
	   
	   @Override
       public void onOpen(ServerHandshake arg0) {
           System.out.println("LbWebSocketClient 打开链接, 保存这个wsclient  "  + this.targetip);
           this.lbServer.lbserverWSClients.add(this);
       }

    

	  @Override
       public void onMessage(String arg0) {
       }

       @Override
       public void onError(Exception arg0) {
           System.out.println("LbWebSocketClient发生错误 " + this.targetip);
       }

       @Override
       public void onClose(int arg0, String arg1, boolean arg2) {
           System.out.println("LbWebSocketClient链接已关闭.去除wsclient,去除redis "  +  this.targetip+ arg0 +"  " +arg1);
           this.lbServer.redisService.removeSet(WsConnectionLBServer.lbiplist, this.targetip);
           this.lbServer.lbserverWSClients.remove(this);

       }

     

	
}

class LBClient{
	
	LbWebSocketClient wsclient;
	String lbIP;
	public LBClient(LbWebSocketClient wsclient, String lbIP) {
		super();
		this.wsclient = wsclient;
		this.lbIP = lbIP;
	}
	
	
	
}


