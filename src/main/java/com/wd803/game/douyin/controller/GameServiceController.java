package com.wd803.game.douyin.controller;


import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/game-service/*")
public class GameServiceController {

    @Autowired
    private GameService gameService;

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
    @PostMapping("msg-receive/comment")
    public String receivePushedMsgComment(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return gameService.receivePushedMsg(headers, payLoad, "comment");
    }

    /**
     * 接收点赞消息接口
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msg-receive/like")
    public String receivePushedMsgLike(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return gameService.receivePushedMsg(headers, payLoad, "like");
    }

    /**
     * 接收礼物消息接口
     * @param headers
     * @param payLoad
     * @return
     */
    @PostMapping("msg-receive/gift")
    public String receivePushedMsgGift(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return gameService.receivePushedMsg(headers, payLoad, "gift");
    }


    @GetMapping("/msg")
    public BaseEntity getMsg(@RequestParam("roomid") String roomid){
        return gameService.getMsg(roomid);

    }



}
