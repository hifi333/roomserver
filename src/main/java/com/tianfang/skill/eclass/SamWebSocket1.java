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

    private RoomBiz samRoomBiz = null;
    
    private RoomBiz getRoomBiz()
    {
        if (samRoomBiz ==null)
        	samRoomBiz = (RoomBiz) appconent.getBean(RoomBiz.class);
        
        return samRoomBiz;
    }
	 private static ApplicationContext appconent = null;
	 public static void setAppContext(ApplicationContext aa){
	        appconent = aa;
	    }

    
    @OnOpen
    public void onOpen(Session session){
       
    		getRoomBiz().newStudentCome(session);

    }
   
    @OnMessage
    public void onMessage(String message,Session session){
    	
		getRoomBiz().oneStudentDraw(message, session);

        
        
    }
    
    @OnClose
    public void onClose(Session session){
    		getRoomBiz().oldStudentQuit(session);
    }
    
    @OnError
    public void onError(Throwable t){
    	
        System.out.println("onError:" +t.getMessage());

    }
    
    
      
}