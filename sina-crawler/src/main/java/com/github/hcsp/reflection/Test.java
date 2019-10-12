package com.github.hcsp.reflection;

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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    private static final String url = "https://www.sina.cn";

    public static void main(String[] args) throws IOException, SQLException {
        Document document = getUrlDocument(url);
        HashMap<String, String> hashMap = getUrlPool(document);
        showContent(hashMap);
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

    private static HashMap<String, String> getUrlPool(Document document) {
        HashMap<String, String> result = new HashMap<>();
        List<Element> newsList = document.select("section").select("a");
        //#j_card_intenews > a:nth-child(7)
        //#j_card_intenews > a:nth-child(6)
        Pattern pattern = Pattern.compile("(\\b(http|https)(.*)(his=0)\\b)");
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

