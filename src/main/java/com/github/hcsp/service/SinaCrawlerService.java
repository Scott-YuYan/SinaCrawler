package com.github.hcsp.service;

import java.util.Random;

public class SinaCrawlerService {
    public static void main(String[] args) {
        System.out.println(getRandomUrl());
    }

    public static long getRandomInteger(int size){
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int parameter1 = random.nextInt(9);
            stringBuilder.append(parameter1);
        }
        return Long.parseLong(stringBuilder.toString());
    }

    public static String getRandomUrl(){
        long param1 = getRandomInteger(18);
        long param2 = getRandomInteger(12);
        String s1 = "https://cre.dp.sina.cn/api/v3/get?callback=jsonp_";
        String s2 = "&timestamp=";
        String s3 = "&zhiding=1&filters=url,wapurl,thumbs,thumbscount,title,intro,style,media,type,videos,picscount,dataid,showtags,commentcount&cateid=sina_all&cre=tianyi&mod=whf&merge=3&statics=1&ver=550&area_code=&action=1&length=10&up=1&ad={%22rotate_count%22:2691,%22page_url%22:%22https%3A%2F%2Fsina.cn%2Findex%2Ffeed%3Ffrom%3Dtouch%26Ver%3D10%22,%22platform%22:%22wap%22,%22timestamp%22:1575081979000,%22net%22:null,%22channel%22:%22-10002%22}";
        return s1 + param1 + s2 + param2 + s3;
    }
}
