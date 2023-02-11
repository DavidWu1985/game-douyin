package com.wd803.game.douyin.entity;

import lombok.Data;

@Data
public class TaskInfo {
    private String roomid;
    private int pageNum = 1;
    private int pageSize = 100;
    private String started = "0"; //1启动，0关闭

}
