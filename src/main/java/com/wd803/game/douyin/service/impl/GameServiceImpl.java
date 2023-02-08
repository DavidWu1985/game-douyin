package com.wd803.game.douyin.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.service.GameService;
import com.wd803.game.douyin.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GameServiceImpl implements GameService {


    @Autowired
    private TokenService tokenService;

    @Override
    public BaseEntity gameStart(String roomid, String msg_type) {

        String url = "https://webcast.bytedance.com/api/live_data/task/start";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", tokenService.getToken());
        headers.add("content-type", "application/json");
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("roomid", roomid);
        bodyMap.put("appid", "ttd616a0ab492900b510");
        bodyMap.put("msg_type", msg_type);
        System.out.println(JSONObject.toJSONString(bodyMap));
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
        ResponseEntity<BaseEntity> result = restTemplate.postForEntity(url, requestEntity, BaseEntity.class);
        System.out.println(JSONObject.toJSONString(result.getBody()));
        return result.getBody();
    }
}
