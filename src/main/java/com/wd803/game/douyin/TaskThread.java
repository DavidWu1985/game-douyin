package com.wd803.game.douyin;

import com.wd803.game.douyin.entity.TaskInfo;
import com.wd803.game.douyin.service.GameService;

public class TaskThread implements Runnable {

    private GameService gameService;

    private TaskInfo task;

    public TaskThread(GameService gameService, TaskInfo task) {
        this.gameService = gameService;
        this.task = task;
    }

    @Override
    public void run() {
        gameService.getFailMsgFromDouyin(task);
    }
}
