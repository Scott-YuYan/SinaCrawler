package com.github.hcsp.dao;

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

    public void insertIntoNewsAndUpdate(Map<String, Object> hashMap) throws SQLException {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            sqlSession.insert("insertIntoNewsAndUpdate", hashMap);
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
        List<String> list;
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            list = sqlSession.selectList("assertNoRepeatUrl", url);
        }
        return list == null;
    }
}
