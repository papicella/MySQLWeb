package com.pas.tools.mysqlweb.utils;

import com.pas.tools.mysqlweb.service.SessionConnectionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class AdminUtil
{
    private static SessionConnectionRegistry sessionConnectionRegistry;

    @Autowired
    public AdminUtil(SessionConnectionRegistry sessionConnectionRegistry)
    {
        AdminUtil.sessionConnectionRegistry = sessionConnectionRegistry;
    }

    public static Connection getConnection(String sessionId) throws Exception
    {
        requireRegistry();
        Connection connection = sessionConnectionRegistry.getConnection(sessionId);
        if (connection == null)
        {
            throw new Exception("No JDBC connection registered for session " + sessionId);
        }
        return connection;
    }

    public static javax.sql.DataSource getDataSource(String sessionId) throws Exception
    {
        requireRegistry();
        javax.sql.DataSource dataSource = sessionConnectionRegistry.getDataSource(sessionId);
        if (dataSource == null)
        {
            throw new Exception("No JDBC connection registered for session " + sessionId);
        }
        return dataSource;
    }

    /** @deprecated Use {@link SessionConnectionRegistry#register} instead. */
    @Deprecated
    public static SingleConnectionDataSource newSingleConnectionDataSource(
            String url,
            String username,
            String password)
    {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(url);

        if (username != null)
        {
            dataSource.setUsername(username);
        }

        if (password != null)
        {
            dataSource.setPassword(password);
        }

        dataSource.setSuppressClose(true);
        return dataSource;
    }

    private static void requireRegistry()
    {
        if (sessionConnectionRegistry == null)
        {
            throw new IllegalStateException("SessionConnectionRegistry is not initialized");
        }
    }
}
