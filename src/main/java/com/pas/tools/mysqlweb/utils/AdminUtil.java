package com.pas.tools.mysqlweb.utils;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.Connection;

public class AdminUtil
{

    static public SingleConnectionDataSource newSingleConnectionDataSource
            (String url,
             String username,
             String passwd)
    {
        SingleConnectionDataSource ds = new SingleConnectionDataSource();

        //ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(url);

        if (username != null)
            ds.setUsername(username);

        if (passwd != null)
            ds.setPassword(passwd);

        return ds;
    }


    /*
     * Get connection from ConnectionManager conList Map
     */
    static public Connection getConnection(String userKey) throws Exception
    {
        ConnectionManager cm = ConnectionManager.getInstance();
        return cm.getDataSource(userKey).getConnection();
    }

    /*
     * Get DataSource from ConnectionManager conList Map
     */
    static public javax.sql.DataSource getDataSource(String userKey) throws Exception
    {
        ConnectionManager cm = ConnectionManager.getInstance();
        return cm.getDataSource(userKey);
    }

}
