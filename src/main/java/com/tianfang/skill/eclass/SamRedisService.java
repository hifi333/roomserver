package com.tianfang.skill.eclass;

import java.util.List;
import java.util.Set;

public interface SamRedisService {
	
	
		public void set(String key, Object value);  
	    public Object get(String key);  
	    public List multiGet(List keys);
	    
	    public void deleteKey(String key);

	    public List getList(String key);

	    public void leftPush(String key,Object value);
	    
	    
	    
	    public void addSet(String key,String value);
	    public void removeSet(String key,String value);
	    public long removeSet(String key,Set values);

	    public Set getSet(String key);
	    

}
