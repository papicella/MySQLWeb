package com.pas.tools.mysqlweb.service;

import com.pas.tools.mysqlweb.beans.Login;
import com.pas.tools.mysqlweb.beans.UserPref;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.Constants;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.AdminUtil;
import com.pas.tools.mysqlweb.utils.ConnectionManager;
import com.pas.tools.mysqlweb.utils.MysqlConnection;
import com.pas.tools.mysqlweb.utils.Themes;
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

    public Login defaultLogin()
    {
        return new Login("", "", DEFAULT_JDBC_URL, "apples");
    }

    public void applyDefaultTheme(HttpSession session)
    {
        session.setAttribute("themeMain", Themes.defaultTheme);
        session.setAttribute("themeMin", Themes.defaultThemeMin);
    }

    public WebResult connectAndInitSession(
            String username,
            String password,
            String url,
            HttpSession session) throws Exception
    {
        ConnectionManager cm = ConnectionManager.getInstance();
        String sessionId = session.getId();
        String connectedAt = new Date().toString();
        String dbUsername = resolveDbUsername(username);
        String dbPassword = resolveDbPassword(username, password);
        String displayUser = resolveDisplayUser(username);

        cm.addConnection(new MysqlConnection(url, connectedAt, displayUser), sessionId);
        cm.addDataSourceConnection(
                AdminUtil.newSingleConnectionDataSource(url, dbUsername, dbPassword),
                sessionId);

        String schema = url.substring(url.lastIndexOf('/') + 1);

        session.setAttribute("user_key", sessionId);
        session.setAttribute("user", displayUser);
        session.setAttribute("schema", schema);
        session.setAttribute("url", url);
        session.setAttribute("prefs", userPref);
        session.setAttribute("history", new LinkedList<>());
        session.setAttribute("connectedAt", connectedAt);
        applyDefaultTheme(session);

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
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
