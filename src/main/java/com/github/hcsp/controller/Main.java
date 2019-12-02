package com.github.hcsp.controller;

import com.github.hcsp.dao.JdbcCrawlerDao;


public class Main {
    @SuppressWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) {
        JdbcCrawlerDao dao = new JdbcCrawlerDao();
        Object lock = new Object();
        for (int i = 0; i < 4; i++) {
            synchronized (lock) {
                new SinaCrawler(dao).start();
            }
        }
    }
}
