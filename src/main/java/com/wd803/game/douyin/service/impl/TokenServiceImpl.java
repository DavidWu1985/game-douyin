package com.wd803.game.douyin.service.impl;

import com.wd803.game.douyin.entity.MsgTypeConstant;
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

    public final static String TOKEN_KEY = "DOUYIN:TOKEN:" + MsgTypeConstant.APP_ID;

    public final static String FORBID_KEY = "DOUYIN:TOKEN:FORBID:" + MsgTypeConstant.APP_ID;

    public final static int FORBID_KEY_TTL = 4;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public synchronized String getToken() {
        //此处要加分布式锁
        String token = (String) redisTemplate.opsForValue().get(TOKEN_KEY);
        if (StringUtils.isBlank(token)) {
            RestTemplate rest = new RestTemplate();
            String url = "https://developer.toutiao.com/api/apps/v2/token";
            Map<String, String> map = new HashMap<>();
            map.put("appid", MsgTypeConstant.APP_ID);
            map.put("secret", MsgTypeConstant.SECRET);
            map.put("grant_type", "client_credential");
            ResponseEntity<TokenEntity> response = rest.postForEntity(url, map, TokenEntity.class);
            log.info(response.getBody().toString());
            if (response.getBody() != null && response.getBody().getErr_no() == 0) {
                token = (String) response.getBody().getData().get("access_token");
                int expires_in = (int) response.getBody().getData().get("expires_in");
                redisTemplate.opsForValue().set(TOKEN_KEY, token, expires_in - 10, TimeUnit.SECONDS);
            } else {
                token = "";
            }
        }
        return token;
    }


    @Override
    public synchronized void refreshToken() {
        log.info("token过期，刷新token");
        //禁止刷新时间区间
        String forbidTimeSection = (String)redisTemplate.opsForValue().get(FORBID_KEY);
        //在禁止刷新时间内
        if (StringUtils.isNotBlank(forbidTimeSection)) {
            log.info("在限制期内，不刷新token");
            return;
        } else {
            log.info("设置刷新标记，删除token");
            redisTemplate.opsForValue().set(FORBID_KEY, "forbid", FORBID_KEY_TTL, TimeUnit.SECONDS);
            redisTemplate.delete(TOKEN_KEY);
        }
    }

}
