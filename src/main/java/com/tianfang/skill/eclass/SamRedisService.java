package com.tianfang.skill.eclass;

import java.util.List;

public interface SamRedisService {
	
	
		public void set(String key, Object value);  
	    public Object get(String key);  
	    public List multiGet(List keys);
	    
	    
	    public List getList(String key);

	    public void leftPush(String key,Object value);

}
