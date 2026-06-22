package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Login;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.service.LoginSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class LoginController
{
    @Autowired
    LoginSessionService loginSessionService;

    @GetMapping("/")
    public String showLoginPage(Model model, HttpSession session)
    {
        log.info("Received request to show login page");
        model.addAttribute("loginObj", loginSessionService.defaultLogin());
        loginSessionService.applyDefaultTheme(session);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String url,
            Model model,
            HttpSession session)
    {
        log.info("Received request to login for user {} at {}", username, url);
        Login loginObj = new Login(username, password, url, "");

        try
        {
            WebResult databaseList = loginSessionService.connectAndInitSession(
                    username, password, url, session);
            model.addAttribute("databaseList", databaseList);
            return "main";
        }
        catch (Exception ex)
        {
            log.warn("Login failed for user {}: {}", username, ex.getMessage());
            model.addAttribute("loginerror", ex.getMessage());
            model.addAttribute("loginObj", loginObj);
            loginSessionService.applyDefaultTheme(session);
            return "login";
        }
    }
}
