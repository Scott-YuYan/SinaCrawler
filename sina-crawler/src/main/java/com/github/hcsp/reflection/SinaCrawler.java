package com.github.hcsp.reflection;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinaCrawler {
    private static final String url = "https://www.sina.cn";
    private static final String jdbcUrl = "jdbc:h2:file:H:/github item/SinaCrawler/sina-crawler/SinaCrawler";

    public static void main(String[] args) throws IOException, SQLException {
        Document document = getUrlDocument(url);
        HashMap<String, String> hashMap = getUrlPool(document);
        showContent(hashMap);
    }

    private static List<String> executeSelectSqlCommendAndGetResultSet(String sqlCommend) throws SQLException {
        List<String> urlFromDatabase = new ArrayList<>();
        Connection connection = DriverManager.getConnection(jdbcUrl, "root", "password");
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCommend)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                urlFromDatabase.add(resultSet.getString(1));
            }
        }
        return urlFromDatabase;
    }


    private static Document getUrlDocument(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
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

    /**
     * Get a database name and the url which you want insert into database.
     *
     * @param connection Database connection.
     * @param webUrl     Insert the url into database.
     * @param sqlName    Which database you want alter.
     */

    private static void insertUrlToDatabase(Connection connection, String webUrl, String sqlName) throws SQLException {
        List<String> list = executeSelectSqlCommendAndGetResultSet("select url from " + sqlName);
        if (list.contains(webUrl)) {
            throw new IllegalStateException("该链接已经存在，为避免死循环，拒绝插入该链接");
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into ?(url) values('?'")) {
            preparedStatement.setString(1, sqlName);
            preparedStatement.setString(2, webUrl);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static HashMap<String, String> getUrlPool(Document document) {
        HashMap<String, String> result = new HashMap<>();
        List<Element> newsList = document.select("section").select("a");
        Pattern pattern = Pattern.compile("(\\b(http|https)(.*)(pos=108)\\b)");
        for (Element e : newsList
        ) {
            String title = e.attr("title");
            String urlPool = e.attr("href");
            Matcher matcher = pattern.matcher(urlPool);
            if ((!title.isEmpty()) && (matcher.find())) {
                result.put(title, urlPool);
            }
        }
        return result;
    }

//    private static HashMap<String, String> getUrlPool(Document document) {
//        HashMap<String, String> result = new HashMap<>();
//        List<Element> newsList = document.select("section").select("a");
//        Pattern pattern = Pattern.compile("(\\b(http|https)(.*)(pos=108)\\b)");
//        for (Element e : newsList
//        ) {
//            String title = e.attr("title");
//            String urlPool = e.attr("href");
//            Matcher matcher = pattern.matcher(urlPool);
//            if ((!title.isEmpty()) && (matcher.find())) {
//                result.put(title, urlPool);
//            }
//        }
//        return result;
//    }


    private static String getContent(String url) throws IOException {
        if (url.isEmpty()) {
            throw new NullPointerException("传入的连接池为空");
        }
        Document document = getUrlDocument(url);
        return document.select("section").select("p").text();
    }

    private static void showContent(HashMap<String, String> hashMap) throws IOException {
        Set<String> url = hashMap.keySet();
        for (String s : url
        ) {
            String webUrl = hashMap.get(s);
            String content = getContent(webUrl);
            if (!content.isEmpty()) {
                System.out.println("新闻标题：" + s);
                System.out.println("新闻链接：" + webUrl);
                System.out.println("新闻内容：" + content);
                System.out.println();
            }
        }
    }
}

