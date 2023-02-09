package com.wd803.game.douyin.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.entity.MsgTypeEnum;
import com.wd803.game.douyin.service.GameService;
import com.wd803.game.douyin.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        return pushMsgToDouyin(url, roomid, msg_type);
    }

    @Override
    public BaseEntity gameEnd(String roomid, String msg_type) {
        String url = "https://webcast.bytedance.com/api/live_data/task/stop";
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
        String roomid = headers.get("x-roomid");
        //一种类型的消息放在同一个room的消息类型下
        String msgKey = roomid + ":" + msg_type;
        //消息ID放在set中，防止重复
        String msgIdKey = roomid + ":" + msg_type + ":ids";
        list.forEach(obj -> {
            String msgId = ((JSONObject) obj).get("msg_id").toString();
            //判断消息体的ID是否在集合中
            if (!redisTemplate.opsForSet().isMember(msgIdKey, msgId)) {
                redisTemplate.opsForSet().add(msgKey, obj);
                redisTemplate.opsForSet().add(msgIdKey, msgId);
            }
        });
        return "succ";
    }

    @Override
    public BaseEntity getMsg(String roomid) {
        String commentMsgKey = roomid + ":" + MsgTypeEnum.COMMENT.getType();
        Set comments = redisTemplate.opsForSet().members(commentMsgKey);
        String likeMsgKey = roomid + ":" + MsgTypeEnum.LIKE.getType();
        Set likes = redisTemplate.opsForSet().members(likeMsgKey);
        String giftMsgKey = roomid + ":" + MsgTypeEnum.GIFT.getType();
        Set gifts = redisTemplate.opsForSet().members(giftMsgKey);
        Map<String, Set<JSONObject>> map = new HashMap<>();
        map.put("comments", comments.isEmpty() ? new HashSet<>() : comments);
        map.put("like", likes.isEmpty() ? new HashSet<>() : likes);
        map.put("gifts", gifts.isEmpty() ? new HashSet<>() : gifts);
        BaseEntity entity = new BaseEntity();
        entity.setErr_no(0);
        entity.setErr_msg("succ");
        entity.setData(map);
        if (redisTemplate.opsForSet().size(commentMsgKey) > 0) {
            comments.forEach(msg -> {
                redisTemplate.opsForSet().remove(commentMsgKey, msg);
            });
        }
        if (redisTemplate.opsForSet().size(likeMsgKey) > 0) {
            likes.forEach(msg -> {
                redisTemplate.opsForSet().remove(likeMsgKey, msg);
            });
        }
        if (redisTemplate.opsForSet().size(giftMsgKey) > 0) {
            gifts.forEach(msg -> {
                redisTemplate.opsForSet().remove(giftMsgKey, msg);
            });
        }
        return entity;
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
        bodyMap.put("appid", appid);
        bodyMap.put("msg_type", msg_type);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
        ResponseEntity<BaseEntity> result = restTemplate.postForEntity(url, requestEntity, BaseEntity.class);
        System.out.println(JSONObject.toJSONString(result.getBody()));
        return result.getBody();
    }


}
