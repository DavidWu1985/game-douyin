package com.wd803.game.douyin.entity;


import lombok.Data;

@Data
public class BaseEntity{

    private int err_no;
    private String err_msg;
    private String logid;
    private Object data;

}
