package com.pas.tools.mysqlweb.service;

import lombok.Getter;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Getter
public class SessionConnectionContext
{
    private final String sessionId;
    private final String url;
    private final String displayUser;
    private final String connectedAt;
    private final SingleConnectionDataSource dataSource;

    public SessionConnectionContext(
            String sessionId,
            String url,
            String displayUser,
            String connectedAt,
            SingleConnectionDataSource dataSource)
    {
        this.sessionId = sessionId;
        this.url = url;
        this.displayUser = displayUser;
        this.connectedAt = connectedAt;
        this.dataSource = dataSource;
    }
}
