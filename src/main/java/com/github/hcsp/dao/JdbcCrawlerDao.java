package com.github.hcsp.dao;

import com.github.hcsp.entity.News;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class JdbcCrawlerDao {

    private SqlSessionFactory sqlSessionFactory;

    public JdbcCrawlerDao() {
        String resources = "db/mybatis/config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

    }

    /**
     * @return 返回一个Url组成的链表
     */

    public List<String> selectUrlFromDatabase() {
        List<String> urlFromDatabase;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            urlFromDatabase = session.selectList("selectUrlFromDatabase");
        }
        return urlFromDatabase;
    }

    public List<String> selectUrlFromAlreadyDatabase() {
        List<String> urlFromDatabase;
        try (SqlSession session = sqlSessionFactory.openSession()) {
            urlFromDatabase = session.selectList("selectUrlFromAlreadyDatabase");
        }
        return urlFromDatabase;
    }

    public void insertIntoNewsAndUpdate(News news) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("insertIntoNewsAndUpdate", news);
        }
    }

    public void insertIntoLINKS_TOBE_PROCESSED(String url) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("insertIntoLINKS_TOBE_PROCESSED", url);
        }
    }

    public synchronized void insertIntoAlreadyAndDelete(String link) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("insertIntoLINKS_ALREADY_PROCESSED", link);
            sqlSession.delete("deleteFromLINKS_TOBE_PROCESSED", link);
        }
    }

    public Boolean assertNoRepeatUrl(String url) {
        int count;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            count = sqlSession.selectOne("assertNoRepeatUrl", url);
        }
        return count == 0;
    }
}
