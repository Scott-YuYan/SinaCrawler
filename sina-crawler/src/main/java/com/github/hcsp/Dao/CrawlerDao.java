package com.github.hcsp.Dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface CrawlerDao {
    List<String> selectUrlFromDatabase(String sqlCommend) throws SQLException;

    void insertIntoNewsAndUpdate(String title, String url, String content) throws SQLException;

    void insertIntoLINKS_TOBE_PROCESSED(String url) throws SQLException;

    void insertIntoAlreadyAndDelete(String link) throws SQLException;

    ResultSet assertNoRepeatUrl(String link) throws SQLException;

}
