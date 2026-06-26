package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.service.LoginSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class AutoLoginController
{
    @Autowired
    LoginSessionService loginSessionService;

    @GetMapping("/autologin")
    public String autoLogin(Model model, HttpSession session, HttpServletRequest request)
    {
        String username = nullToEmpty(request.getParameter("username"));
        String password = nullToEmpty(request.getParameter("passwd"));
        String url = nullToEmpty(request.getParameter("url"));

        log.info("Received auto login for user {} at {}", username, url);

        try
        {
            WebResult databaseList = loginSessionService.connectAndInitSession(
                    username, password, url, session);
            model.addAttribute("databaseList", databaseList);
            return "main";
        }
        catch (Exception ex)
        {
            log.warn("Auto login failed for user {}: {}", username, ex.getMessage());
            model.addAttribute("loginerror", ex.getMessage());
            model.addAttribute("loginObj", loginSessionService.defaultLogin());
            return "login";
        }
    }

    private static String nullToEmpty(String value)
    {
        return value == null ? "" : value;
    }
}
