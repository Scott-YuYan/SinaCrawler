package com.github.hcsp.Dao;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCrawlerDao implements CrawlerDao {
    private static final String jdbcUrl = "jdbc:h2:file:H:/github item/SinaCrawler/sina-crawler/SinaCrawler";
    private static final String user = "root";
    private static final String password = "password";
    private final Connection connection;

    public JdbcCrawlerDao() {
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param sqlCommend 将要执行的select操作
     * @return 返回一个Url组成的链表
     * @throws SQLException 数据库连接有误
     */

    @SuppressFBWarnings("OBL_UNSATISFIED_OBLIGATION")
    public List<String> selectUrlFromDatabase(String sqlCommend) throws SQLException {
        List<String> urlFromDatabase = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCommend)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                urlFromDatabase.add(resultSet.getString("url"));
            }
        }
        return urlFromDatabase;
    }

    public void insertIntoNewsAndUpdate(String title, String url, String content) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement
                ("insert into SINA_NEWS(title,url,content,create_time,modify_time) values(?,?,?,current_timestamp,current_timestamp)")) {
            preparedStatement.setString(1, title);
            preparedStatement.setString(2, url);
            preparedStatement.setString(3, content);
            preparedStatement.executeUpdate();
        }
    }

    public void insertIntoLINKS_TOBE_PROCESSED(String url) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_TOBE_PROCESSED(url) values(?)")) {
            preparedStatement.setString(1, url);
            preparedStatement.executeUpdate();
        }
    }

    public void insertIntoAlreadyAndDelete(String link) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("insert into LINKS_ALREADY_PROCESSED(url) values ?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
        //这时，可以将待处理的连接池中的这条url从池中删除
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from LINKS_TOBE_PROCESSED where url=?")) {
            preparedStatement.setString(1, link);
            preparedStatement.executeUpdate();
        }
    }

    public ResultSet assertNoRepeatUrl(String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("select url from LINKS_ALREADY_PROCESSED WHERE URL=?")) {
            preparedStatement.setString(1, link);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return null;
    }
}
