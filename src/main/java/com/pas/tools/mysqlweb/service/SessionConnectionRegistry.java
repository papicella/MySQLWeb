package com.pas.tools.mysqlweb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SessionConnectionRegistry
{
    private final ConcurrentHashMap<String, SessionConnectionContext> sessions =
            new ConcurrentHashMap<>();

    public SessionConnectionContext register(
            String sessionId,
            String url,
            String username,
            String password,
            String displayUser,
            String connectedAt) throws SQLException
    {
        release(sessionId);

        SingleConnectionDataSource dataSource = createDataSource(url, username, password);
        validateConnection(dataSource);

        SessionConnectionContext context = new SessionConnectionContext(
                sessionId, url, displayUser, connectedAt, dataSource);
        sessions.put(sessionId, context);
        log.info("Registered JDBC connection for session {}", sessionId);
        return context;
    }

    public DataSource getDataSource(String sessionId)
    {
        SessionConnectionContext context = sessions.get(sessionId);
        return context == null ? null : context.getDataSource();
    }

    public Connection getConnection(String sessionId) throws SQLException
    {
        DataSource dataSource = getDataSource(sessionId);
        if (dataSource == null)
        {
            return null;
        }
        return dataSource.getConnection();
    }

    public boolean isRegistered(String sessionId)
    {
        return sessions.containsKey(sessionId);
    }

    public boolean isConnectionValid(String sessionId, int timeoutSeconds) throws SQLException
    {
        if (!isRegistered(sessionId))
        {
            return false;
        }

        try (Connection connection = getConnection(sessionId))
        {
            return connection != null && connection.isValid(timeoutSeconds);
        }
    }

    public void release(String sessionId)
    {
        SessionConnectionContext context = sessions.remove(sessionId);
        if (context == null)
        {
            log.info("No JDBC connection registered for session {}", sessionId);
            return;
        }

        context.getDataSource().destroy();
        log.info("Released JDBC connection for session {}", sessionId);
    }

    private static SingleConnectionDataSource createDataSource(
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

        // Keep the session connection open when JdbcTemplate closes it.
        dataSource.setSuppressClose(true);
        return dataSource;
    }

    private static void validateConnection(SingleConnectionDataSource dataSource) throws SQLException
    {
        try (Connection connection = dataSource.getConnection())
        {
            if (!connection.isValid(5))
            {
                throw new SQLException("JDBC connection is not valid");
            }
        }
    }
}
