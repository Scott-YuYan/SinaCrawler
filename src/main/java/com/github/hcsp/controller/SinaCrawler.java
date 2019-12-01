package com.github.hcsp.controller;

import com.github.hcsp.dao.JdbcCrawlerDao;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinaCrawler extends Thread {
    private JdbcCrawlerDao dao;

    public SinaCrawler(JdbcCrawlerDao dao) {
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            insertLinksTobeProcessedUrlToDataBase();
            filterUrlAndInsertToAlreadyDatabase();
            getUsefulContentAndInsertIntoSinaNewDataBase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @SuppressWarnings("OBL_UNSATISFIED_OBLIGATION")
    private void insertLinksTobeProcessedUrlToDataBase() throws SQLException, IOException {
        List<String> list = dao.selectUrlFromDatabase();
        List<String> result = new ArrayList<>();
        for (String url : list
        ) {
            Document document = getUrlDocument(url);
            result.addAll(getUrlFromWeb(document));
        }
        Pattern pattern = Pattern.compile("^(http|https).*");

        for (String urlFromList : result
        ) {
            Matcher matcher = pattern.matcher(urlFromList);
            if (matcher.find()) {
                dao.insertIntoLINKS_TOBE_PROCESSED(urlFromList);
            }
        }
    }

    @SuppressWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private synchronized void filterUrlAndInsertToAlreadyDatabase() throws SQLException, IOException {
        List<String> resultSetFromTobe = dao.selectUrlFromDatabase();
        while (!resultSetFromTobe.isEmpty()) {
            //从Link最后面拿数据更有效率
            String link = resultSetFromTobe.remove(resultSetFromTobe.size() - 1);
            //保证重复的网址不会被插入已经处理完的数据库中
            if (!dao.assertNoRepeatUrl(link)) {
                continue;
            }
            dao.insertIntoAlreadyAndDelete(link);
        }
    }

    private void getUsefulContentAndInsertIntoSinaNewDataBase() throws SQLException, IOException {
//        "select url from LINKS_ALREADY_PROCESSED"
        List<String> resultSet = dao.selectUrlFromDatabase();
        while (!resultSet.isEmpty()) {
            String url = resultSet.remove(resultSet.size() - 1);
            Document document = getUrlDocument(url);
            String content = getContent(url);
            Map<String, Object> map = new HashMap<>();
            map.put("url", url);
            map.put("content", content);
            String title = document.select("section").select("article").select("h1").text();
            map.put("title", title);
            if (!title.isEmpty() && !content.isEmpty()) {
                System.out.println(title);
                dao.insertIntoNewsAndUpdate(map);
            }
        }
    }

    private static List<String> getUrlFromWeb(Document document) {
        List<String> result = new ArrayList<>();
        List<Element> newsList = document.select("section").select("a");
        for (Element e : newsList
        ) {
            String hrefTag = e.attr("href");
            result.add(hrefTag);
        }
        return result;
    }

    private static String getContent(String url) throws IOException {
        if (url.isEmpty()) {
            throw new NullPointerException("传入的连接池为空");
        }
        Document document = getUrlDocument(url);
        return document.select("section").select("p").text();
    }

    private static Document getUrlDocument(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(CookieSpecs.STANDARD).build())
                .build();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("user-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");
        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            InputStream inputStream = entity1.getContent();
            String html = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(html);
            EntityUtils.consume(entity1);
            return document;
        }
    }
}

