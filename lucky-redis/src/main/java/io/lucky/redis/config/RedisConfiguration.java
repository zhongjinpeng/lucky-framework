package io.lucky.redis.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public class RedisConfiguration {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Bean("redisLettuceClient")
    public RedisConnection getRedisConnectionByLettuce(){
        return redisConnectionFactory.getConnection();
    }

}
