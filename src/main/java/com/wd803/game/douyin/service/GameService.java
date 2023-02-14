package com.wd803.game.douyin.service;

import com.wd803.game.douyin.entity.BaseEntity;
import com.wd803.game.douyin.entity.TaskInfo;

import java.util.Map;

public interface GameService {

    BaseEntity gameStart(String roomid, String msg_type) throws Exception;

    BaseEntity gameEnd(String roomid, String msg_type) throws Exception;

    BaseEntity checkTaskStatus(String roomid, String msg_type);

    String receivePushedMsg(Map<String, String> headers, String payLoad, String msg_type);

    BaseEntity getMsg(String roomid);

    void getFailMsgFromDouyin(TaskInfo task);

}
