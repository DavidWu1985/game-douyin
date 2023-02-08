package com.wd803.game.douyin;

import com.wd803.game.douyin.service.GameService;
import com.wd803.game.douyin.service.TokenService;
import com.wd803.game.douyin.util.SignatureUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(SpringRunner.class)
@SpringBootTest
class DouyinApplicationTests {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private GameService gameService;

    @Test
    void getToken() {
        System.out.println(tokenService.getToken());
    }

    @Test
    void gameStart(){
        System.out.println(gameService.gameStart("roomid", "msg_type"));
    }

    @Test
    void testSignature(){
        ExecutorService executor = Executors.newCachedThreadPool();
        for(int i = 0; i < 1; i++){
            executor.execute(()->{
                Map<String, String> map = new HashMap<>();
                map.put("x-nonce-str","7654321nonce");
                map.put("x-timestamp","99999time");
                map.put("x-roomid","room803");
                map.put("x-msg-type","live_like");
                String s = SignatureUtils.signature(map, "abc123hello");
                System.out.println(s);
            });
        }
    }

}
