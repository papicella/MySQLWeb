package com.pas.tools.mysqlweb.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class SessionJdbcSupport
{
    private final SessionConnectionRegistry sessionConnectionRegistry;

    public SessionJdbcSupport(SessionConnectionRegistry sessionConnectionRegistry)
    {
        this.sessionConnectionRegistry = sessionConnectionRegistry;
    }

    public JdbcTemplate getJdbcTemplate(String sessionId) throws Exception
    {
        return new JdbcTemplate(requireDataSource(sessionId));
    }

    public JdbcTemplate getJdbcTemplate(String sessionId, int maxRows) throws Exception
    {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(sessionId);
        if (maxRows != -1)
        {
            jdbcTemplate.setMaxRows(maxRows);
        }
        return jdbcTemplate;
    }

    private DataSource requireDataSource(String sessionId) throws Exception
    {
        DataSource dataSource = sessionConnectionRegistry.getDataSource(sessionId);
        if (dataSource == null)
        {
            throw new Exception("No JDBC connection registered for session " + sessionId);
        }
        return dataSource;
    }
}
