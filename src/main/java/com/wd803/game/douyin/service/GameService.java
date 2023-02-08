package com.wd803.game.douyin.service;

import com.wd803.game.douyin.entity.BaseEntity;

public interface GameService {

    BaseEntity gameStart(String roomid, String msg_type);
}
