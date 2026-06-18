package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Login;
import com.pas.tools.mysqlweb.beans.UserPref;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.Constants;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.LinkedList;
import java.util.Map;

@Slf4j
@Controller
public class LoginController
{

    @Autowired
    UserPref userPref;

    @GetMapping(value = "/")
    public String login(Model model, HttpSession session) throws Exception
    {
        log.info("Received request to show login page");
        model.addAttribute("loginObj", new Login("", "", "jdbc:mysql://localhost:3306/apples", "apples"));
        session.setAttribute("themeMain", Themes.defaultTheme);
        session.setAttribute("themeMin", Themes.defaultThemeMin);
        return "login";
    }

    @PostMapping(value = "/login")
    public String login
            (@RequestParam(value="username", required=true) String username,
             @RequestParam(value="password", required=true) String password,
             @RequestParam(value="url", required=true) String url,
             Model model,
             HttpSession session) throws Exception
    {
        log.info("Received request to login");

        WebResult databaseList, schemaMapResult;
        SingleConnectionDataSource ds = new SingleConnectionDataSource();

        ConnectionManager cm = ConnectionManager.getInstance();

        Login loginObj = new Login(username, password, url, "");

        log.info("url {" + loginObj.getUrl() + "}");
        log.info("user {" + loginObj.getUsername() + "}");

        try
        {

            MysqlConnection newConn =
                    new MysqlConnection
                            (url,
                                    new java.util.Date().toString(),
                                    username.toUpperCase());

            cm.addConnection(newConn, session.getId());
            cm.addDataSourceConnection(AdminUtil.newSingleConnectionDataSource
                    (url, username, password), session.getId());

            String schema = url.substring(url.lastIndexOf("/") + 1);

            session.setAttribute("user_key", session.getId());
            session.setAttribute("user", username.toUpperCase());
            session.setAttribute("schema", schema);
            session.setAttribute("url", url);
            session.setAttribute("prefs", userPref);
            session.setAttribute("history", new LinkedList());
            session.setAttribute("connectedAt", new java.util.Date().toString());
            session.setAttribute("themeMain", Themes.defaultTheme);
            session.setAttribute("themeMin", Themes.defaultThemeMin);

            GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

            databaseList = genericDAO.runGenericQuery
                    (Constants.DATABASE_LIST, null, session.getId(), -1);

            Map<String, Long> schemaMap;
            schemaMap = genericDAO.populateSchemaMap(schema, session.getId());

            log.info("schemaMap=" + schemaMap);
            session.setAttribute("schemaMap", schemaMap);

            model.addAttribute("databaseList", databaseList);

            return "main";
        }
        catch (Exception ex)
        {
            model.addAttribute("loginerror", ex.getMessage());
            model.addAttribute("loginObj");
            session.setAttribute("themeMain", Themes.defaultTheme);
            session.setAttribute("themeMin", Themes.defaultThemeMin);
            return "login";
        }
    }

}
