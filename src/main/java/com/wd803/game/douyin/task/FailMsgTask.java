package com.wd803.game.douyin.task;


import com.wd803.game.douyin.entity.MsgTypeConstant;
import com.wd803.game.douyin.entity.TaskInfo;
import com.wd803.game.douyin.service.GameService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class FailMsgTask {


    @Resource
    private GameService gameService;

    @Resource
    private RedisTemplate redisTemplate;

    ExecutorService threadPool = Executors.newFixedThreadPool(3);

    /**
     * 获取失败信息，每秒执行一次
     */
    @Scheduled(fixedRate = 1000)
    public void getMsgFormDouyin() {
        //查找要获取消息的房间号
        List<String> taskeys = new ArrayList<>(redisTemplate.keys(MsgTypeConstant.TASK_REDIS_KEY + "*"));
        List<TaskInfo> taskInfos = redisTemplate.opsForValue().multiGet(taskeys);
        if (taskInfos != null){
            List<TaskInfo> unStarted = taskInfos.stream().filter(t -> {
                return StringUtils.equals(t.getStarted(), "0");
            }).map(t -> {
                //此处这么做，是避免任务重复多次启动，每次先修改任务状态再启动任务，避免时间延时
                //修改状态，再加进去
                t.setStarted("1");
                redisTemplate.opsForValue().set(MsgTypeConstant.TASK_REDIS_KEY + t.getRoomid(), t);
                return t;
            }).collect(Collectors.toList());
            //启动
            if (unStarted != null) {
                unStarted.forEach(t -> {
                    System.out.println(t.getRoomid());
                    //如果任务还没启动
                    threadPool.execute(new TaskThread(gameService, t));
                });
            }
        }
    }
}
