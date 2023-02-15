package com.wd803.game.douyin.cache;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redisson(){
        // 1. 创建配置
        Config config = new Config();
        // 一定要加redis://
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        // 2. 根据config创建出redissonClient实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
