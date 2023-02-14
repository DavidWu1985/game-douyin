package com.wd803.game.douyin.controller;


import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.entity.MsgTypeConstant;
import com.wd803.game.douyin.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/gameservice/*")
public class GameServiceController {

    @Autowired
    private GameService gameService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 游戏启动
     * @return
     */
    @GetMapping("start")
    public BaseEntity gameStart(@RequestParam("roomid") String roomid, @RequestParam String msg_type){
        return gameService.gameStart(roomid, msg_type);
    }

    /**
     * 停止任务
     */
    @GetMapping("end")
    public BaseEntity gameEnd(@RequestParam("roomid") String roomid, @RequestParam String msg_type){
        return gameService.gameEnd(roomid, msg_type);
    }


    /**
     * 查询任务状态
     */
    @GetMapping("task/status")
    public BaseEntity checkTaskStatus(@RequestParam("roomid") String roomid, @RequestParam String msg_type){
        return gameService.checkTaskStatus(roomid, msg_type);
    }


    /**
     * 接收评论消息接口
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msgreceive/comment")
    public String receivePushedMsgComment(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return gameService.receivePushedMsg(headers, payLoad, MsgTypeConstant.COMMENT);
    }

    /**
     * 接收点赞消息接口
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msgreceive/like")
    public String receivePushedMsgLike(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return gameService.receivePushedMsg(headers, payLoad, MsgTypeConstant.LIKE);
    }

    /**
     * 接收礼物消息接口
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msgreceive/gift")
    public String receivePushedMsgGift(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return gameService.receivePushedMsg(headers, payLoad, MsgTypeConstant.GIFT);
    }


    /**
     * 手机获取消息接口
     * @param roomid
     * @return
     */
    @GetMapping("/msg")
    public BaseEntity getMsg(@RequestParam("roomid") String roomid){
        return gameService.getMsg(roomid);
    }


    @GetMapping("/failMsg")
    public BaseEntity failMsg(){
        BaseEntity  entity  = new BaseEntity();
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
