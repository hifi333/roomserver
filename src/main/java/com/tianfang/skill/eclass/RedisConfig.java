package com.tianfang.skill.eclass;





import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration  
@EnableCaching//开启注解  
public class RedisConfig extends CachingConfigurerSupport {
    @Bean
    public CacheManager cacheManager(RedisTemplate<?,?> redisTemplate) {
       CacheManager cacheManager = new RedisCacheManager(redisTemplate);
       return cacheManager;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
       RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
       redisTemplate.setConnectionFactory(factory);
       return redisTemplate;
    }
}


/*

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@PropertySource("classpath:application.properties")
public class RedisConfig {

	private static Logger logger = LoggerFactory.getLogger(RedisConfig.class);

	@Value("${redis.host}")
	private String host;
	@Value("${redis.port}")
	private int port;

	@Bean
	public JedisPool createjedisPool() {
		JedisPoolConfig config = new JedisPoolConfig();
//		config.setMaxTotal(1024);
//		config.setMaxIdle(10);
//		config.setMaxWaitMillis(1000);
		config.setTestOnBorrow(true);
		config.setTestOnReturn(true);
		JedisPool jedisPool = new JedisPool(config, host, port, 2000);
		logger.info("Jedis pool created.");
		return jedisPool;
	}

	
}

*/