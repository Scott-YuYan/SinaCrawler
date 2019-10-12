package com.github.hcsp.reflection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegixPattern {
    public static void main(String[] args) {
        String s = "http://cj.sina.cn/pos=108&his=0";
        String regix = "\\b(http|https)(.*)(his=0)\\b";
        Pattern pattern = Pattern.compile(regix);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
