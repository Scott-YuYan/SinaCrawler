package com.github.hcsp.reflection;

import com.github.hcsp.Dao.JdbcCrawlerDao;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.List;
import java.util.regex.Pattern;

public class SinaCrawler {
    JdbcCrawlerDao dao = new JdbcCrawlerDao();

    public void run() throws SQLException, IOException {
        insertLinksTobeProcessedUrlToDataBase();
        filterUrlAndInsertToAlreadyDatabase();
        getUsefulContentAndInsertIntoSinaNewDataBase();
    }

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws IOException, SQLException {
        SinaCrawler sinaCrawler = new SinaCrawler();
        sinaCrawler.run();
    }

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    private void insertLinksTobeProcessedUrlToDataBase() throws SQLException, IOException {
        List<String> list = dao.selectUrlFromDatabase("select url from LINKS_TOBE_PROCESSED");
        List<String> result = new ArrayList<>();
        for (String url : list
        ) {
            Document document = getUrlDocument(url);
            result.addAll(getUrlFromWeb(document));
        }
        for (String urlFromList : result
        ) {
            dao.insertIntoLINKS_TOBE_PROCESSED(urlFromList);
            System.out.println(urlFromList);
        }
    }

    @SuppressFBWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private void filterUrlAndInsertToAlreadyDatabase() throws SQLException {
        List<String> resultSetFromTobe = dao.selectUrlFromDatabase("select url from LINKS_TOBE_PROCESSED");
        Pattern pattern = Pattern.compile("(\\b(http|https)(.*)(pos=108)\\b)");
        while (!resultSetFromTobe.isEmpty()) {
            String link = resultSetFromTobe.remove(resultSetFromTobe.size() - 1);
            //保证重复的网址不会被插入已经处理完的数据库中
            ResultSet resultSet = dao.assertNoRepeatUrl(link);
            if (resultSet != null) {
                continue;
            }
            if (pattern.matcher(link).find()) {
                dao.insertIntoAlreadyAndDelete(link);
            }
        }
    }

    private void getUsefulContentAndInsertIntoSinaNewDataBase() throws SQLException, IOException {
        List<String> resultSet = dao.selectUrlFromDatabase("select url from LINKS_ALREADY_PROCESSED");
        while (!resultSet.isEmpty()) {
            String url = resultSet.remove(resultSet.size() - 1);
            Document document = getUrlDocument(url);
            String content = getContent(url);
            String title = document.select("section").select("article").select("h1").text();
            if (!title.isEmpty() && !content.isEmpty()) {
                dao.insertIntoNewsAndUpdate(title, url, content);
                System.out.println(title);
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

