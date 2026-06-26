package com.pas.tools.mysqlweb.service;

import com.pas.tools.mysqlweb.beans.Login;
import com.pas.tools.mysqlweb.beans.UserPref;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.Constants;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

@Slf4j
@Service
public class LoginSessionService
{
    public static final String DEFAULT_JDBC_URL = "jdbc:mysql://localhost:3306/apples";

    @Autowired
    UserPref userPref;

    @Autowired
    SessionConnectionRegistry sessionConnectionRegistry;

    @Autowired
    GenericDAO genericDAO;

    public Login defaultLogin()
    {
        return new Login("", "", DEFAULT_JDBC_URL, "apples");
    }

    public WebResult connectAndInitSession(
            String username,
            String password,
            String url,
            HttpSession session) throws Exception
    {
        String sessionId = session.getId();
        String connectedAt = new Date().toString();
        String dbUsername = resolveDbUsername(username);
        String dbPassword = resolveDbPassword(username, password);
        String displayUser = resolveDisplayUser(username);

        sessionConnectionRegistry.register(
                sessionId, url, dbUsername, dbPassword, displayUser, connectedAt);

        String schema = url.substring(url.lastIndexOf('/') + 1);

        session.setAttribute(Utils.SESSION_CONNECTION_ID, sessionId);
        session.setAttribute("user", displayUser);
        session.setAttribute("schema", schema);
        session.setAttribute("url", url);
        session.setAttribute("prefs", userPref);
        session.setAttribute("history", new LinkedList<>());
        session.setAttribute("connectedAt", connectedAt);

        Map<String, Long> schemaMap = genericDAO.populateSchemaMap(schema, sessionId);
        log.info("schemaMap={}", schemaMap);
        session.setAttribute("schemaMap", schemaMap);

        return genericDAO.runGenericQuery(Constants.DATABASE_LIST, null, sessionId, -1);
    }

    private static String resolveDisplayUser(String username)
    {
        if (username == null || username.trim().isEmpty())
        {
            return "";
        }
        return username.toUpperCase();
    }

    private static String resolveDbUsername(String username)
    {
        if (username == null || username.trim().isEmpty())
        {
            return null;
        }
        return username;
    }

    private static String resolveDbPassword(String username, String password)
    {
        if (username == null || username.trim().isEmpty())
        {
            return null;
        }
        return password;
    }
}
