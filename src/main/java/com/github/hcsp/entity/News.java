package com.github.hcsp.entity;

import java.time.Instant;

public class News {
    String url;
    String title;
    String content;
    Instant createTime;
    Instant modifyTime;

    public News(String url, String title, String content, Instant createTime, Instant modifyTime) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModifyTime() {
        return modifyTime;
    }
}
