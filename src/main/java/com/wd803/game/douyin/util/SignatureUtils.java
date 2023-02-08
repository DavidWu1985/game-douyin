package com.wd803.game.douyin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class SignatureUtils {

    /**
     * 获取消息加密后的签名
     *
     * @param headers
     * @param payLoad
     * @return
     */
    public static String signature(Map<String, String> headers, String payLoad, String secret) {
        List<String> keyList = new ArrayList();
        headers.forEach((key, val) -> keyList.add(key));
        Collections.sort(keyList, String::compareTo);
        List<String> kvList = new ArrayList<>(4);
        for (String key : keyList) {
            kvList.add(key + "=" + headers.get(key));
        }
        String urlParams = String.join("&", kvList);
        String rawData = urlParams + payLoad + secret;
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        md.update(rawData.getBytes());
        return Base64.getEncoder().encodeToString(md.digest());
    }
}
