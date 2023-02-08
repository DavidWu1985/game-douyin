package com.wd803.game.douyin.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.service.GameService;
import com.wd803.game.douyin.service.TokenService;
import com.wd803.game.douyin.util.SignatureUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private TokenService tokenService;

    private static String appid = "ttd616a0ab492900b510";

    @Autowired
    private RedisTemplate redisTemplate;

    private static String commentSecret = "iMZXsaKrXGKbRzrHWATshPhmZarpdTAP";
    private static String giftSecret = "FadBeREMKQSMQZjCbymcGExexnzDWmwQ";
    private static String likeSecret = "NxfHwwYhYCehAHzfSbCezJxSTNEjBJCp";

    @Override
    public BaseEntity gameStart(String roomid, String msg_type) {

        String url = "https://webcast.bytedance.com/api/live_data/task/start";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("access-token", tokenService.getToken());
//        headers.add("content-type", "application/json");
//        Map<String, String> bodyMap = new HashMap<>();
//        bodyMap.put("roomid", roomid);
//        bodyMap.put("appid", "ttd616a0ab492900b510");
//        bodyMap.put("msg_type", msg_type);
//        System.out.println(JSONObject.toJSONString(bodyMap));
//        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
//        ResponseEntity<BaseEntity> result = restTemplate.postForEntity(url, requestEntity, BaseEntity.class);
//        System.out.println(JSONObject.toJSONString(result.getBody()));
        return pushMsgToDouyin(url, roomid, msg_type);
    }

    @Override
    public BaseEntity gameEnd(String roomid, String msg_type) {
        String url = "https://webcast.bytedance.com/api/live_data/task/stop";
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("access-token", tokenService.getToken());
//        headers.add("content-type", "application/json");
//        Map<String, String> bodyMap = new HashMap<>();
//        bodyMap.put("roomid", roomid);
//        bodyMap.put("appid", "ttd616a0ab492900b510");
//        bodyMap.put("msg_type", msg_type);
//        System.out.println(JSONObject.toJSONString(bodyMap));
//        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
//        ResponseEntity<BaseEntity> result = restTemplate.postForEntity(url, requestEntity, BaseEntity.class);
//        System.out.println(JSONObject.toJSONString(result.getBody()));
        return pushMsgToDouyin(url, roomid, msg_type);
    }

    @Override
    public String receivePushedMsg(Map<String, String> headers, String payLoad, String msg_type) {
        Map<String, String> map = new HashMap<>();
        map.put("x-nonce-str", headers.get("x-nonce-str"));
        map.put("x-timestamp", headers.get("x-timestamp"));
        map.put("x-roomid", headers.get("x-roomid"));
        map.put("x-msg-type", headers.get("x-msg-type"));
//        String signature = null;
//
//        switch (msg_type) {
//            case "comment":
//                signature = SignatureUtils.signature(map, payLoad, commentSecret);
//                break;
//            case "like":
//                signature = SignatureUtils.signature(map, payLoad, likeSecret);
//                break;
//            case "gift":
//                signature = SignatureUtils.signature(map, payLoad, giftSecret);
//        }
//        //校验签名
//        if (!StringUtils.equals(signature, headers.get("x-signature"))) {
//            return null;
//        }
        //签名校验通过
        //解析消息体
        JSONArray list = JSONArray.parse(payLoad);
        String x_msg_type = headers.get("x-msg-type");
        String roomid = headers.get("x-roomid");
        //一种类型的消息放在同一个room的消息类型下
        String msgKey = roomid + ":" + x_msg_type;
        //消息ID放在set中，防止重复
        String msgIdKey = roomid + ":" + x_msg_type + ":ids";
        list.forEach(obj->{
            String msgId = ((JSONObject)obj).get("msg_id").toString();
            //判断消息体的ID是否在集合中
            if (!redisTemplate.opsForSet().members(msgIdKey).contains(msgId)) {
                redisTemplate.opsForSet().add(msgKey, obj);
                redisTemplate.opsForSet().add(msgIdKey, msgId);
            }
        });
        return "succ";
    }

    @Override
    public BaseEntity checkTaskStatus(String roomid, String msg_type) {
        String url = "https://webcast.bytedance.com/api/live_data/task/get";
        return pushMsgToDouyin(url, roomid, msg_type);
    }


    private BaseEntity pushMsgToDouyin(String url, String roomid, String msg_type) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", tokenService.getToken());
        headers.add("content-type", "application/json");
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("roomid", roomid);
        bodyMap.put("appid", "ttd616a0ab492900b510");
        bodyMap.put("msg_type", msg_type);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
        ResponseEntity<BaseEntity> result = restTemplate.postForEntity(url, requestEntity, BaseEntity.class);
        System.out.println(JSONObject.toJSONString(result.getBody()));
        return result.getBody();
    }


}
