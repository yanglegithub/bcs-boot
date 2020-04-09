package com.phy.bcs.redis.config;/*
package com.phy.bcs.plugin.redis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

*/
/**
 * redis配置
 *//*

@Configuration
@EnableCaching
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.expiration}")
    private int expiration;


    @Bean
    public RedisCacheManager cacheManager(JedisConnectionFactory redisConnectFactory){
        RedisCacheManager cacheManager = RedisCacheManager.create(redisConnectFactory);
        //cacheManager.setDefaultExpiration(expiration);
        */
/*Map<String, Long> expMap = this.getExpMap();
        cacheManager.setExpires(expMap);*//*

        return cacheManager;
    }

    @Bean
    public JedisConnectionFactory redisConnectFactory() {
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
        jedisConnectionFactory.setHostName(host);
        jedisConnectionFactory.setPort(port);
        jedisConnectionFactory.afterPropertiesSet();
        return jedisConnectionFactory;
    }

    */
/*@Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }*//*


    */
/**
     * 获取缓存过期设置
     * @return
     *//*

    */
/*public Map<String, Long> getExpMap(){
        String[] keyArray = new String[]{};
        Map<String, Long> expMap = new ConcurrentHashMap<String, Long>(20);
        for(String key : keyArray){
            expMap.put(key, 300L);
        }
        return expMap;
    }*//*

}
*/
