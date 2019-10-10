package com.github.hcsp.reflection;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.List;
import java.util.regex.Pattern;

public class SinaCrawler {
    private static final String jdbcUrl = "jdbc:h2:file:H:/github item/SinaCrawler/sina-crawler/SinaCrawler";
    private static final String user = "root";
    private static final String password = "password";

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
        insertLinksTobeProcessedUrlToDataBase(connection);
        filterUrlAndInsertToAlreadyDatabase(connection);
        getUsefulContentAndInsertIntoSinaNewDataBase(connection);
        showResult(connection);
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

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    private static void showResult(Connection connection) throws SQLException {
        String sqlComment = "select id,url,title,content from sina_news";
        PreparedStatement preparedStatement = connection.prepareStatement(sqlComment);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getString("title"));
            System.out.println(resultSet.getString("content"));
            System.out.println(resultSet.getString("url"));
        }
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    private static void insertLinksTobeProcessedUrlToDataBase(Connection connection) throws SQLException, IOException {
        String sqlSelectCommend = "select url from  LINKS_TOBE_PROCESSED";
        List<String> list = executeSelectSqlCommendAndGetResultSet(connection, sqlSelectCommend);
        List<String> result = new ArrayList<>();
        for (String url : list
        ) {
            //根据数据库中初始的网站主页，获取主页中各个新闻的链接
            Document document = getUrlDocument(url);
            //将获取的网址加入结果集中
            result.addAll(getUrlFromWeb(document));
        }
        //将获取的链接，加入待处理数据库中
        PreparedStatement preparedStatement = connection.prepareStatement
                ("insert into LINKS_TOBE_PROCESSED(url) values(?)");
        for (String urlFromList : result
        ) {
            preparedStatement.setString(1, urlFromList);
            preparedStatement.executeUpdate();
        }
    }
    @SuppressFBWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private static void filterUrlAndInsertToAlreadyDatabase(Connection connection) throws SQLException {
        List<String> resultSetFromTobe = executeSelectSqlCommendAndGetResultSet(connection, "select url from LINKS_TOBE_PROCESSED");
        Pattern pattern = Pattern.compile("(\\b(http|https)(.*)(pos=108)\\b)");
        while (!resultSetFromTobe.isEmpty()) {
            String link = resultSetFromTobe.remove(resultSetFromTobe.size() - 1);
            boolean flag = false;
            //保证重复的网址不会被插入已经处理完的数据库中
            try (PreparedStatement preparedStatement = connection.prepareStatement("select url from LINKS_ALREADY_PROCESSED WHERE URL=?")) {
                preparedStatement.setString(1, link);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    flag = true;
                }
            }
            if (flag) {
                continue;
            }
            if (pattern.matcher(link).find()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED(url) values ?")) {
                    if (pattern.matcher(link).find()) {
                        preparedStatement.setString(1, link);
                        preparedStatement.executeUpdate();
                    }
                }
                //这时，可以将待处理的连接池中的这条url从池中删除
                try (PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TOBE_PROCESSED where url=?")) {
                    preparedStatement.setString(1, link);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    private static void getUsefulContentAndInsertIntoSinaNewDataBase(Connection connection) throws SQLException, IOException {
        List<String> resultSet = executeSelectSqlCommendAndGetResultSet(connection, "select url from LINKS_ALREADY_PROCESSED");
        while (!resultSet.isEmpty()) {
            String url = resultSet.remove(resultSet.size() - 1);
            Document document = getUrlDocument(url);
            String content = getContent(url);
            String title = document.select("section").select("article").select("h1").text();
            if (!title.isEmpty() && !content.isEmpty()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement
                        ("insert into SINA_NEWS(title,url,content,create_time,modify_time) values(?,?,?,current_timestamp,current_timestamp)")) {
                    preparedStatement.setString(1, title);
                    preparedStatement.setString(2, url);
                    preparedStatement.setString(3, content);
                    preparedStatement.executeUpdate();
                }
            }
        }
    }

    private static List<String> getUrlFromWeb(Document document) {
        List<String> result = new ArrayList<>();
        List<Element> newsList = document.select("section").select("a");
        for (Element e : newsList
        ) {
            String regex = "(http|https)(.*)";
            Pattern pattern = Pattern.compile(regex);
            String hrefTag = e.attr("href");
            if (pattern.matcher(hrefTag).find()) {
                result.add(hrefTag);
            }
        }
        return result;
    }

    /**
     * 根据一个链接获取链接中的文本
     *
     * @param url 网址
     * @return 网页中的文字
     * @throws IOException 链接有误
     */

    private static String getContent(String url) throws IOException {
        if (url.isEmpty()) {
            throw new NullPointerException("传入的连接池为空");
        }
        Document document = getUrlDocument(url);
        return document.select("section").select("p").text();
    }

    /**
     * @param connection 将要连接的数据库
     * @param sqlCommend 将要执行的select操作
     * @return 返回一个Url组成的链表
     * @throws SQLException 数据库连接有误
     */

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    private static List<String> executeSelectSqlCommendAndGetResultSet(Connection connection, String sqlCommend) throws SQLException {
        List<String> urlFromDatabase = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCommend)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                urlFromDatabase.add(resultSet.getString("url"));
            }
        }
        return urlFromDatabase;
    }
}

