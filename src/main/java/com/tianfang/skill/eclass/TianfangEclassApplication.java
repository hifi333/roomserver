package com.tianfang.skill.eclass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication

public class TianfangEclassApplication {

	
	public static void main(String[] args) {
		ConfigurableApplicationContext  aa = SpringApplication.run(TianfangEclassApplication.class, args);
			
	      SamWSServer.setAppContext(aa);

	      LBServer wsLbServer= (LBServer) aa.getBean(LBServer.class);
	      wsLbServer.startServer();
	        
	}
}
