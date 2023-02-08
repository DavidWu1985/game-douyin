package com.wd803.game.douyin.controller;


import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


}
