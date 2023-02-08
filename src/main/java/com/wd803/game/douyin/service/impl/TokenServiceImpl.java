package com.wd803.game.douyin.service.impl;

import com.wd803.game.douyin.entity.TokenEntity;
import com.wd803.game.douyin.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    public final static String TOKEN_KEY = "DOUYIN:LIVE:";

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String getToken() {
        String tokenKey = TOKEN_KEY + "ttd616a0ab492900b510";
        //此处要加分布式锁
        String token = (String) redisTemplate.opsForValue().get(tokenKey);
        if (StringUtils.isBlank(token)) {
            RestTemplate rest = new RestTemplate();
            String url = "https://developer.toutiao.com/api/apps/v2/token";
            Map<String, String> map = new HashMap<>();
            map.put("appid", "ttd616a0ab492900b510");
            map.put("secret", "8e2d8208fbc3feb0e70d0b19a3f08509b25491a7");
            map.put("grant_type", "client_credential");
            ResponseEntity<TokenEntity> response = rest.postForEntity(url, map, TokenEntity.class);
            log.info(response.getBody().toString());
            if (response.getBody() != null && response.getBody().getErr_no() == 0) {
                token = (String) response.getBody().getData().get("access_token");
                int expires_in = (int) response.getBody().getData().get("expires_in");
                redisTemplate.opsForValue().set(tokenKey, token, expires_in - 10, TimeUnit.SECONDS);
            } else {
                token = "";
            }
        }
        return token;
    }

}
