package com.github.hcsp.service;

import com.github.hcsp.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockNews {
    private static final int MaxContainer = (int)Math.pow(10,5);
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
        //BATCH-批处理模式
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            Random random = new Random();
            List<News> news = sqlSession.selectList("selectNewFromNews");
            int count = MaxContainer-news.size();
            try {
                while (count-- > 0) {
                    int id = random.nextInt(news.size());
                    long randomLong = random.nextInt(365 * 24 * 3600);
                    News aNew = new News(news.get(id));
                    Instant createTime = aNew.getCreateTime().minusSeconds(randomLong);
                    aNew.setCreateTime(createTime);
                    aNew.setModifyTime(createTime);
                    sqlSession.insert("insertIntoNewsAndUpdate",aNew);
                    System.out.println("剩余"+count+"条");
                    if (count % 2000 ==0 ){
                        sqlSession.flushStatements();
                    }
                }
                sqlSession.commit();
            } catch (Exception e) {
                sqlSession.rollback();
                throw new RuntimeException(e);
            }
        }
    }
}
