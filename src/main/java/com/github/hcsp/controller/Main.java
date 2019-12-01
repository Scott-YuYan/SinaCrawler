package com.github.hcsp.controller;

import com.github.hcsp.dao.JdbcCrawlerDao;


public class Main {
    @SuppressWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) {
        JdbcCrawlerDao dao = new JdbcCrawlerDao();
        for (int i = 0; i < 4; i++) {
            new SinaCrawler(dao).start();
        }
    }
}
