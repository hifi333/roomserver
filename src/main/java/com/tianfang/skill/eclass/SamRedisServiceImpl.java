package com.tianfang.skill.eclass;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class SamRedisServiceImpl implements SamRedisService {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    public void set(String key, Object value) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
         vo.set(key, value);
    }
    public Object get(String key) {
        ValueOperations<String,Object> vo = redisTemplate.opsForValue();
        return vo.get(key);
    }
    
    public List multiGet(List keys) {
    
    	return redisTemplate.opsForValue().multiGet(keys);
    	
    }
    
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    	
    }
    
    

    public List getList(String key) {
         long len= redisTemplate.opsForList().size(key);
         return redisTemplate.opsForList().range(key, 0, len);
    }

    public void leftPush(String key,Object value){
   
 	   redisTemplate.opsForList().leftPush(key, value);
 	   
    }

    
    public void addSet(String key,String value){
    	   
  	   redisTemplate.opsForSet().add(key, value);
  	     	   
     }
    
    
    public void removeSet(String key,String value){
    	   
  	   redisTemplate.opsForSet().remove(key, value);
  	   
     } 
    
    public long removeSet(String key,Set values){
 	   
   	   return redisTemplate.opsForSet().remove(key, values.toArray());
   	   
      } 
    
    public Set getSet(String key){
    	   
  	    return  redisTemplate.opsForSet().members(key);
  	   
     } 
    
}