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

    public News(News old) {
        this.url = old.url;
        this.title = old.title;
        this.content = old.content;
        this.createTime = old.createTime;
        this.modifyTime = old.modifyTime;
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }

    public void setModifyTime(Instant modifyTime) {
        this.modifyTime = modifyTime;
    }
}
