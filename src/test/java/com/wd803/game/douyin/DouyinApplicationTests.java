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
                String s = SignatureUtils.signature(map, "abc123hello", "1234");
                System.out.println(s);
            });
        }
    }


    @Test
    void testReceiveMsg(){
        Map<String, String> map = new HashMap<>();
        map.put("x-nonce-str","7654321nonce");
        map.put("x-timestamp","99999time");
        map.put("x-roomid","room803");
        map.put("x-msg-type","live_comment");
        String payLoad = "[{\"msg_id\":\"123456783\",\"sec_openid\":\"xxxx\",\"like_num\":\"22\",\"avatar_url\":\"xxx\",\"nickname\":\"xxxx\",\"timestamp\":1649068964},{\"msg_id\":\"123456784\",\"sec_openid\":\"xxxx\",\"like_num\":\"22\",\"avatar_url\":\"xxx\",\"nickname\":\"xxxx\",\"timestamp\":1649068964},{\"msg_id\":\"123456785\",\"sec_openid\":\"xxxx\",\"like_num\":\"22\",\"avatar_url\":\"xxx\",\"nickname\":\"xxxx\",\"timestamp\":1649068964},{\"msg_id\":\"123456786\",\"sec_openid\":\"xxxx\",\"like_num\":\"22\",\"avatar_url\":\"xxx\",\"nickname\":\"xxxx\",\"timestamp\":1649068964},{\"msg_id\":\"123456783\",\"sec_openid\":\"xxxx\",\"like_num\":\"22\",\"avatar_url\":\"xxx\",\"nickname\":\"xxxx\",\"timestamp\":1649068964}]";
        System.out.println(gameService.receivePushedMsg(map, payLoad, "like"));
    }

    @Test
    void getMsg(){
        System.out.println(gameService.getMsg("room803"));
    }

}
