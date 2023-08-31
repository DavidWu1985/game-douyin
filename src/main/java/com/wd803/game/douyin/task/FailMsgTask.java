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
     * get failure msgs from douyin server,  Execute once per second
     */
    @Scheduled(fixedRate = 1000)
    public void getMsgFormDouyin() {
        //get game room Id
        List<String> taskeys = new ArrayList<>(redisTemplate.keys(MsgTypeConstant.TASK_REDIS_KEY + "*"));
        List<TaskInfo> taskInfos = redisTemplate.opsForValue().multiGet(taskeys);
        if (taskInfos != null){
            List<TaskInfo> unStarted = taskInfos.stream().filter(t -> {
                return StringUtils.equals(t.getStarted(), "0");
            }).map(t -> {
                //avoid multiple task starts
                //update the status
                t.setStarted("1");
                redisTemplate.opsForValue().set(MsgTypeConstant.TASK_REDIS_KEY + t.getRoomid(), t);
                return t;
            }).collect(Collectors.toList());
            // run the job
            if (unStarted != null) {
                unStarted.forEach(t -> {
                    System.out.println(t.getRoomid());
                    //check the job started
                    threadPool.execute(new TaskThread(gameService, t));
                });
            }
        }
    }
}
