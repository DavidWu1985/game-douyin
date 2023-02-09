package com.wd803.game.douyin.entity;

public enum MsgTypeEnum {
    COMMENT("comments"),
    LIKE("like"),
    GIFT("gifts");

    String type;

    MsgTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
