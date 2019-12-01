package com.github.hcsp.service;

import com.github.hcsp.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Random;

public class MockNews {
    private static final int MaxContainer = (int)Math.pow(10,3);
    private SqlSessionFactory sqlSessionFactory ;

    public MockNews() {
        String resources = "db/mybatis/config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    }

    public static void main(String[] args) {
        MockNews mockNews = new MockNews();
        mockNews.insertDataIntoNews();
    }

    public void insertDataIntoNews() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            int count = sqlSession.selectOne("selectCountFromOldNewsDB");
            Random random = new Random();
            try {
                while ((MaxContainer - count) > 0) {
                    int id = random.nextInt(count - 1)+1;
                    long randomLong = random.nextInt(365 * 24 * 3600);
                    News news = sqlSession.selectOne("selectNewFromNews", id);
                    Instant createTime = news.getCreateTime().minusSeconds(randomLong);
                    news.setCreateTime(createTime);
                    news.setModifyTime(createTime);
                    sqlSession.insert("insertIntoNewsAndUpdate");
                    count++;
                    System.out.println(count);
                }
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
