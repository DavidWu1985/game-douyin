package com.wd803.game.douyin.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.entity.MsgTypeConstant;
import com.wd803.game.douyin.entity.TaskInfo;
import com.wd803.game.douyin.service.GameService;
import com.wd803.game.douyin.service.TokenService;
import com.wd803.game.douyin.util.SignatureUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.wd803.game.douyin.entity.MsgTypeConstant.*;

@Slf4j
@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 2)
    public BaseEntity gameStart(String roomid, String msg_type) throws Exception {
        String url = "https://webcast.bytedance.com/api/live_data/task/start";
        TaskInfo task = new TaskInfo();
        task.setRoomid(roomid);
        BaseEntity baseEntity = pushMsgToDouyin(url, roomid, msg_type);
        if (redisTemplate.opsForValue().get(MsgTypeConstant.TASK_REDIS_KEY + roomid) == null) {
            redisTemplate.opsForValue().set(MsgTypeConstant.TASK_REDIS_KEY + roomid, task);
        }
//        //top_gift
//        if(StringUtils.equals(msg_type, GIFT)){
//            topGift(roomid);
//        }
        return baseEntity;
    }

    public BaseEntity topGift(String roomid){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String[] gifts = {"28rYzVFNyXEXFC8HI+f/WG+I7a6lfl3OyZZjUS+CVuwCgYZrPrUdytGHu0c=","fJs8HKQ0xlPRixn8JAUiL2gFRiLD9S6IFCFdvZODSnhyo9YN8q7xUuVVyZI=","PJ0FFeaDzXUreuUBZH6Hs+b56Jh0tQjrq0bIrrlZmv13GSAL9Q1hf59fjGk=",
        "IkkadLfz7O/a5UR45p/OOCCG6ewAWVbsuzR/Z+v1v76CBU+mTG/wPjqdpfg=","gx7pmjQfhBaDOG2XkWI2peZ66YFWkCWRjZXpTqb23O/epru+sxWyTV/3Ufs=","pGLo7HKNk1i4djkicmJXf6iWEyd+pfPBjbsHmd3WcX0Ierm2UdnRR7UINvI="};
        headers.add("x-token", tokenService.getToken());
        headers.add("content-type", "application/json");
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("room_id", roomid);
        bodyMap.put("app_id", MsgTypeConstant.APP_ID);
        bodyMap.put("sec_gift_id_list", gifts);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
        ResponseEntity<BaseEntity> result = restTemplate.postForEntity("https://webcast.bytedance.com/api/gift/top_gift", requestEntity, BaseEntity.class);
        log.info("礼物置顶结果：{}", result.getBody());
        return result.getBody();
    }

    @Override
    @Retryable(value = {Exception.class}, maxAttempts = 2)
    public BaseEntity gameEnd(String roomid, String msg_type) throws Exception {
        String url = "https://webcast.bytedance.com/api/live_data/task/stop";
        BaseEntity baseEntity = pushMsgToDouyin(url, roomid, msg_type);
        //房间关闭
        if (baseEntity.getErr_no() == 0) {
            //删除此房间的消息
            delRoomTaskAndMsg(roomid, msg_type);
        }
        return baseEntity;
    }

    private void delRoomTaskAndMsg(String roomid, String msg_type) {
        //任务结束，删除队列中的任务
        redisTemplate.delete(MsgTypeConstant.TASK_REDIS_KEY + roomid);
        redisTemplate.delete(roomid + ":" + msg_type);
        redisTemplate.delete(roomid + ":" + msg_type + ":ids");
    }

    @Override
    public String receivePushedMsg(Map<String, String> headers, String payLoad, String msg_type) {
        Map<String, String> map = new HashMap<>();
        map.put("x-nonce-str", headers.get("x-nonce-str"));
        map.put("x-timestamp", headers.get("x-timestamp"));
        map.put("x-roomid", headers.get("x-roomid"));
        map.put("x-msg-type", headers.get("x-msg-type"));
        String signature = null;
        switch (msg_type) {
            case MsgTypeConstant.COMMENT:
                signature = SignatureUtils.signature(map, payLoad, commentSecret);
                break;
            case MsgTypeConstant.LIKE:
                signature = SignatureUtils.signature(map, payLoad, likeSecret);
                break;
            case MsgTypeConstant.GIFT:
                signature = SignatureUtils.signature(map, payLoad, giftSecret);
        }
        log.info("msg type is {}, signature is {}", msg_type, signature);
        //校验签名
        if (!StringUtils.equals(signature, headers.get("x-signature"))) {
            return null;
        }
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
        String commentMsgKey = roomid + ":" + MsgTypeConstant.COMMENT;
        Set comments = redisTemplate.opsForSet().members(commentMsgKey);
        String likeMsgKey = roomid + ":" + MsgTypeConstant.LIKE;
        Set likes = redisTemplate.opsForSet().members(likeMsgKey);
        String giftMsgKey = roomid + ":" + MsgTypeConstant.GIFT;
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
        BaseEntity entity = null;
        try {
            entity = getMsgFromDouyin(url, roomid, msg_type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("查询任务{}状态，结果:{}", roomid, entity);
        if (entity.getErr_no() == 0) {
            int status = (int) ((Map) entity.getData()).get("status");
            if (status == 1) { //1表示任务不存在
                //删除任务和消息
                delRoomTaskAndMsg(roomid, msg_type);
            }
        }
        return entity;
    }


    public BaseEntity pushMsgToDouyin(String url, String roomid, String msg_type) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", tokenService.getToken());
        headers.add("content-type", "application/json");
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("roomid", roomid);
        bodyMap.put("appid", MsgTypeConstant.APP_ID);
        bodyMap.put("msg_type", msg_type);
        HttpEntity<String> requestEntity = new HttpEntity<>(JSONObject.toJSONString(bodyMap), headers);
        ResponseEntity<BaseEntity> result = restTemplate.postForEntity(url, requestEntity, BaseEntity.class);
        log.info("调用{}接口返回结果：{}", url, result.getBody());
        BaseEntity entity = result.getBody();
        refreshToken(entity);
        return entity;
    }

    private BaseEntity getMsgFromDouyin(String url, String roomid, String msg_type) throws Exception {
//        String url = "http://localhost:8080/game-service/failMsg";
        url = url + "?roomid=" + roomid + "&appid=" + MsgTypeConstant.APP_ID + "&msg_type=" + msg_type;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", tokenService.getToken());
        headers.add("content-type", "application/json");
        HttpEntity<MultiValueMap<String, Object>> requestbody = new HttpEntity<>(headers);
        ResponseEntity<BaseEntity> result = restTemplate.exchange(url, HttpMethod.GET, requestbody, BaseEntity.class);
        log.info("调用{}接口返回结果：{}", url, result.getBody());
        BaseEntity entity = result.getBody();
        refreshToken(entity);
        return entity;
    }

    private void refreshToken(BaseEntity entity) throws Exception {
        if (entity != null && entity.getErr_no() == 1 && StringUtils.contains(entity.getErr_msg(), "status=40022")) {
            tokenService.refreshToken();
            throw new Exception("token过期,请再次执行刚才的操作");
        }
    }

    @Override
    public void getFailMsgFromDouyin(TaskInfo task) {
        boolean run = true;
        try {
            while (run) {
                //1.先检查任务状态
                checkTaskStatus(task.getRoomid(), MsgTypeConstant.GIFT);
                //2 判断任务还在不在
                TaskInfo currentTask = (TaskInfo) redisTemplate.opsForValue().get(MsgTypeConstant.TASK_REDIS_KEY + task.getRoomid());
                if (currentTask == null) {
                    return;
                }
                BaseEntity failMsg = getFailMsgFromDouyin(task.getRoomid(), MsgTypeConstant.GIFT, task.getPageNum(), task.getPageSize());
                if (failMsg.getErr_no() == 0) {
                    Map<String, Object> data = (Map) failMsg.getData();
                    JSONArray data_list = JSONArray.parse(data.get("data_list").toString());
                    if (data_list != null) {
                        data_list.forEach(d -> {
                            //解析消息体
                            JSONArray list = JSONArray.parse(((JSONObject) d).get("payload").toString());
                            //一种类型的消息放在同一个room的消息类型下
                            String msgKey = task.getRoomid() + ":" + MsgTypeConstant.GIFT;
                            //消息ID放在set中，防止重复
                            String msgIdKey = task.getRoomid() + ":" + MsgTypeConstant.GIFT + ":ids";
                            list.forEach(obj -> {
                                String msgId = ((JSONObject) obj).get("msg_id").toString();
                                //判断消息体的ID是否在集合中
                                if (!redisTemplate.opsForSet().isMember(msgIdKey, msgId)) {
                                    redisTemplate.opsForSet().add(msgKey, obj);
                                    redisTemplate.opsForSet().add(msgIdKey, msgId);
                                }
                            });
                        });
                    }
                    int total = (int) data.get("total_count");
                    //数据没有获取完，继续获取
                    if ((task.getPageNum() * task.getPageSize()) <= total) {
                        task.setPageNum(task.getPageNum() + 1);
                    } else {
                        //数据获取结束，任务结束
                        run = false;
                        //修改任务状态，记录当前页数，把任务放回redis, 等待下一个定时任务周期
                        task.setStarted("0");
                        //等待0.5s把任务放回redis
                        Thread.sleep(200);
                        redisTemplate.opsForValue().set(MsgTypeConstant.TASK_REDIS_KEY + task.getRoomid(), task);
                    }
                    Thread.sleep(30);
                } else {
                    log.info("获取失败消失出错，消息体为[{}]", failMsg);
                    run = false;
                    task.setStarted("0");
                    //方法出错，立即修改任务状态，把当前新状态的任务(任务状态和pageNum)放回任务队列
                    redisTemplate.opsForValue().set(MsgTypeConstant.TASK_REDIS_KEY + task.getRoomid(), task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            task.setStarted("0");
            //方法出错，立即修改任务状态，把当前新状态的任务(任务状态和pageNum)放回任务队列
            redisTemplate.opsForValue().set(MsgTypeConstant.TASK_REDIS_KEY + task.getRoomid(), task);
        }
    }

    private BaseEntity getFailMsgFromDouyin(String roomid, String msg_type,
                                            int page_num, int page_size) {
//        String url = "http://localhost:8080/game-service/failMsg";
        String url = "https://webcast.bytedance.com/api/live_data/task/fail_data/get?" +
                "roomid=" + roomid + "&appid=" + MsgTypeConstant.APP_ID + "&msg_type=" + msg_type +
                "&page_num=" + page_num + "&page_size=" + page_size;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token", tokenService.getToken());
        headers.add("content-type", "application/json");
        HttpEntity<MultiValueMap<String, Object>> requestbody = new HttpEntity<>(headers);
        ResponseEntity<BaseEntity> result = restTemplate.exchange(url, HttpMethod.GET, requestbody, BaseEntity.class);
        log.info("查询失败消息返回结果：{}", result);
        return result.getBody();
    }

}
