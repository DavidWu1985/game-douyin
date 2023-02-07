package com.wd803.game.douyin.service.impl;

import com.wd803.game.douyin.entity.TokenEntity;
import com.wd803.game.douyin.service.TokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {

    @Override
    public String getToken() {
        RestTemplate rest = new RestTemplate();
        String url = "https://developer.toutiao.com/api/apps/v2/token";
        Map<String, String> map = new HashMap<>();
        map.put("appid", "ttd616a0ab492900b510");
        map.put("secret", "8e2d8208fbc3feb0e70d0b19a3f08509b25491a7");
        map.put("grant_type", "client_credential");
        ResponseEntity<TokenEntity> response = rest.postForEntity(url, map, TokenEntity.class);
        System.out.println(response.getBody());
        if(response.getBody() != null && response.getBody().getErr_no() == 0){
            return (String)response.getBody().getData().get("access_token");
        }
        return null;
    }

}
