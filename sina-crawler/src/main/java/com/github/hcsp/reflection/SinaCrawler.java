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
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SinaCrawler {
    private static final String cookie = "_zap=48672efa-5e10-497c-8621-369a524774f5; d_c0=\"APCpRxuOhA-PTptGCk6GAWaqYxaL-IMi50k=|1559373660\"; __gads=ID=b9edc2ab60012b9d:T=1561452410:S=ALNI_MbiQODQGHBxh4OmtXNMww6kaNzVkw; tst=r; z_c0=\"2|1:0|10:1562471425|4:z_c0|92:Mi4xQ2d3NUR3QUFBQUFBOEtsSEc0NkVEeVlBQUFCZ0FsVk5BYm9PWGdEYnB1UGdvQWtjdXFSVjRBWGo0bEpidVR1RFVn|f4333cc96c559432800088f3d6c83b4590477ba1ca9baa98892bb230a2e455bb\"; __utma=155987696.319545718.1566529526.1566529526.1566529526.1; __utmz=155987696.1566529526.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); q_c1=f62f6c5a1fb448e9ac2e95528934bfec|1568646069000|1559373662000; _xsrf=0a0d27a4-4e84-4a26-9984-79ecf1a08011; Hm_lvt_98beee57fd2ef70ccdd5ca52b9740c49=1569399857,1569844280,1569980980,1570160415; Hm_lpvt_98beee57fd2ef70ccdd5ca52b9740c49=1570167090; tgw_l7_route=4860b599c6644634a0abcd4d10d37251";
    private static final String url = "https://www.sina.cn";

    public static void main(String[] args) throws IOException {
        Document document = getUrlDocument(url);
        HashMap<String, String> hashMap = getUrlPool(document);
        showContent(hashMap);
    }


    private static Document getUrlDocument(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("user-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");
        httpGet.setHeader("cookie", cookie);
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
                System.out.println("");
            }
        }
    }
}

