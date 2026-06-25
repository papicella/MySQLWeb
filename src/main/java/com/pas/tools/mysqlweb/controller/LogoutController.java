package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Login;
import com.pas.tools.mysqlweb.service.LoginSessionService;
import com.pas.tools.mysqlweb.service.SessionConnectionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class LogoutController
{
    @Autowired
    SessionConnectionRegistry sessionConnectionRegistry;

    @GetMapping(value = "/exit")
    public String logout(
            Model model,
            HttpSession session,
            HttpServletResponse response,
            HttpServletRequest request) throws Exception
    {
        log.info("Received request to logout of PivotalMySQL*Web");

        sessionConnectionRegistry.release(session.getId());
        session.invalidate();

        model.addAttribute("loginObj", new Login("", "", LoginSessionService.DEFAULT_JDBC_URL, ""));

        return "login";
    }
}
