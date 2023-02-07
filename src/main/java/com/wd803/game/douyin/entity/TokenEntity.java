package com.wd803.game.douyin.entity;

import lombok.Data;

import java.util.Map;


@Data
public class TokenEntity {
    private int err_no;
    private String err_tips;
    private Map<String, Object> data;

}
