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


    @PostMapping("msg-receive")
    public BaseEntity receivePushedMsg(@RequestHeader Map<String,String> headers, @RequestBody String payLoad){
        return null;
    }



}
