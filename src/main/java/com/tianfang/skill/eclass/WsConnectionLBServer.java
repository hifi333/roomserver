package com.tianfang.skill.eclass;

import java.io.BufferedReader;  
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.net.InetAddress;  
import java.net.InetSocketAddress;  
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;  
  
import org.java_websocket.WebSocket;  
import org.java_websocket.server.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.java_websocket.handshake.ClientHandshake;  
@Service
public class WsConnectionLBServer  {  
  
	
	@Autowired
    private RoomBiz myRoomBiz;
	
	@Autowired
    private SamRedisService redisService;
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SS");

	public static String lbiplist = "rediskey-LBIPLIST";

	WebSocketServer lsWsServer=  null;
	String localIP = null;
	
    public WsConnectionLBServer(){  
	    	InetSocketAddress  aa =null;
	    	try {
	    		  localIP = InetAddress.getLocalHost().getHostAddress();
	    		  aa = new InetSocketAddress(localIP, 8887);  
	        
	    	}catch (Exception ee) {ee.printStackTrace();}
	        
	    
    	
	    	lsWsServer=  	new WebSocketServer(aa) {
  
		    
		    @Override
			public void onStart() {System.out.println( "WsConnectionLBServer onStart ");  }
		
			@Override  
		    public void onOpen( WebSocket conn, ClientHandshake handshake ) { 
				System.out.println( "WsConnectionLBServer onOpen for new client"); 
			
			}  
		  
		    @Override  
		    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {  System.out.println( "WsConnectionLBServer onClose ");  }  
		  
		    @Override  
		    public void onMessage( WebSocket conn, String message ) {  
		    	try {  
				 //  System.out.println("----------------接收到 其他的通知LB:" + df.format(new Date()));
		          	myRoomBiz.fromlbpushToEachLocalClients(message);
		          	
		        } catch ( Exception ex ) {  
		            ex.printStackTrace();  
		        }  
		    }  
		    @Override  
		    public void onError( WebSocket conn, Exception ex ) {  ex.printStackTrace();  }  
		};
  
    }    
    
    public void startServer() {
    	
    	//注册当前LB IP ..
        redisService.addSet(lbiplist , localIP);        
        System.out.println("注册LbServer:" + localIP);
        this.lsWsServer.start();
        
        
    }
    public static void main( String[] args ) throws InterruptedException , IOException {  
        //连接部份  
//        int port = 8887;  
//        try {
//        		WsConnectionLBServer s = new WsConnectionLBServer( port );  
//        		s.start();  
//        		System.out.println( "WsConnectionLBServer started");  
//
//        }catch (Exception ee) {ee.printStackTrace();}
//        
        
        
      try {
    	                 InetAddress address = InetAddress.getLocalHost();
    	                 String hostAddress = address.getHostAddress();
    	                 System.out.println("localIP:" + hostAddress);
    	                 
    	                 System.out.println("localhostIP:" + InetAddress.getByName( "localhost" ));

    	                 
    	                 
    	                 InetAddress address1 = InetAddress.getByName("www.wodexiangce.cn");//获取的是该网站的ip地址，比如我们所有的请求都通过nginx的，所以这里获取到的其实是nginx服务器的IP地 
    	                 String hostAddress1 = address1.getHostAddress();
    	                 System.out.println("lwww.wodexiangce.cn IP:" + hostAddress1);

    	                 InetAddress[] addresses = InetAddress.getAllByName("www.baidu.com");//根据主机名返回其可能的所有InetAddress对象 
    	                 for(InetAddress addr:addresses){ 
    	                 System.out.println(addr);//www.baidu.com/14.215.177.38 
    	                //www.baidu.com/14.215.177.37 
    	              } 
    	           } catch (UnknownHostException e) { 
    	                e.printStackTrace();
    	         } 
       
       
  
        //服务端 发送消息处理部份  
//        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );  
//        while ( true ) {  
//            String in = sysin.readLine();  
//            s.sendToAll( in );  
//        }  
    }  
  
} 
