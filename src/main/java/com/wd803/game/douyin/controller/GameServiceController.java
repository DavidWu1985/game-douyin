package com.wd803.game.douyin.controller;


import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.entity.MsgTypeConstant;
import com.wd803.game.douyin.service.GameService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/gameservice/*")
public class GameServiceController {

    @Autowired
    private GameService gameService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * start game
     *
     * @return
     */
    @GetMapping("start")
    public BaseEntity gameStart(@RequestParam("roomid") String roomid, @RequestParam String msg_type) {
        if(StringUtils.isBlank(roomid)){
            BaseEntity entity = new BaseEntity();
            entity.setErr_no(1);
            entity.setErr_msg("缺少roomid参数");
            log.info("游戏启动失败，缺少roomid参数");
            return entity;
        }
        try {
            return gameService.gameStart(roomid, msg_type);
        } catch (Exception e) {
            BaseEntity entity = new BaseEntity();
            entity.setErr_no(1);
            entity.setErr_msg(e.getMessage());
            return entity;
        }
    }

    @GetMapping("top_gift")
    public BaseEntity topGift(@RequestParam("roomid") String roomid) {
        return gameService.topGift(roomid);
    }

    /**
     * stop game
     */
    @GetMapping("end")
    public BaseEntity gameEnd(@RequestParam("roomid") String roomid, @RequestParam String msg_type) {
        try {
            return gameService.gameEnd(roomid, msg_type);
        } catch (Exception e) {
            BaseEntity entity = new BaseEntity();
            entity.setErr_no(1);
            entity.setErr_msg(e.getMessage());
            return entity;
        }
    }


    /**
     * check task status
     */
    @GetMapping("task/status")
    public BaseEntity checkTaskStatus(@RequestParam("roomid") String roomid, @RequestParam String msg_type) {
        return gameService.checkTaskStatus(roomid, msg_type);
    }


    /**
     * get comment msg
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msg/comment")
    public String receivePushedMsgComment(@RequestHeader Map<String, String> headers, @RequestBody String payLoad) {
        log.info("POST comment headers: {}", headers);
        log.info("POST comment MSG: {}", payLoad);
        return gameService.receivePushedMsg(headers, payLoad, MsgTypeConstant.COMMENT);
    }

    /**
     * receive like msg
     *
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msg/like")
    public String receivePushedMsgLike(@RequestHeader Map<String, String> headers, @RequestBody String payLoad) {
        log.info("POST like headers: {}", headers);
        log.info("POST like MSG: {}", payLoad);
        return gameService.receivePushedMsg(headers, payLoad, MsgTypeConstant.LIKE);
    }

    /**
     * receive gifts by this interface
     *
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msg/gift")
    public String receivePushedMsgGift(@RequestHeader Map<String, String> headers, @RequestBody String payLoad) {
        log.info("POST gift headers: {}", headers);
        log.info("POST gift MSG: {}", payLoad);
        return gameService.receivePushedMsg(headers, payLoad, MsgTypeConstant.GIFT);
    }


    /**
     * mobile phone get msg by this interface
     * @param roomid
     * @return
     */
    @GetMapping("/msg")
    public BaseEntity getMsg(@RequestParam("roomid") String roomid) {
        return gameService.getMsg(roomid);
    }


    @GetMapping("/failMsg")
    public BaseEntity failMsg() {
        BaseEntity entity = new BaseEntity();
        entity.setErr_msg("0");
        entity.setErr_no(0);
        Map<String, Object> map = new HashMap<>();
        map.put("page_num", 1);
        map.put("total_count", 350);
        map.put("data_list", redisTemplate.opsForValue().get("payload"));
        entity.setData(map);
        return entity;
    }


}
