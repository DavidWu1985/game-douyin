package com.wd803.game.douyin;

import com.wd803.game.douyin.service.TokenService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class DouyinApplicationTests {

    @Autowired
    private TokenService tokenService;

    @Test
    void getToken() {
        System.out.println(tokenService.getToken());
    }

}
