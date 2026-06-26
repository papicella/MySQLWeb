package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Login;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.service.LoginSessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest
{
    @Mock
    private LoginSessionService loginSessionService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private LoginController loginController;

    @Test
    void showLoginPage_returnsLoginViewWithDefaults()
    {
        Login defaultLogin = new Login("", "", LoginSessionService.DEFAULT_JDBC_URL, "apples");
        when(loginSessionService.defaultLogin()).thenReturn(defaultLogin);
        Model model = new ExtendedModelMap();

        String view = loginController.showLoginPage(model, session);

        assertEquals("login", view);
        assertEquals(defaultLogin, model.getAttribute("loginObj"));
    }

    @Test
    void login_success_returnsMainViewWithDatabaseList() throws Exception
    {
        WebResult databaseList = new WebResult(
                new String[] {"Database"},
                List.of(Map.of("Database", "apples")));

        when(loginSessionService.connectAndInitSession(
                eq("user"),
                eq("pass"),
                eq("jdbc:mysql://localhost:3306/apples"),
                eq(session)))
                .thenReturn(databaseList);

        Model model = new ExtendedModelMap();

        String view = loginController.login(
                "user",
                "pass",
                "jdbc:mysql://localhost:3306/apples",
                model,
                session);

        assertEquals("main", view);
        assertEquals(databaseList, model.getAttribute("databaseList"));
    }

    @Test
    void login_failure_returnsLoginViewWithError() throws Exception
    {
        when(loginSessionService.connectAndInitSession(
                eq("user"),
                eq("badpass"),
                eq("jdbc:mysql://localhost:3306/apples"),
                eq(session)))
                .thenThrow(new Exception("Connection refused"));

        Model model = new ExtendedModelMap();

        String view = loginController.login(
                "user",
                "badpass",
                "jdbc:mysql://localhost:3306/apples",
                model,
                session);

        assertEquals("login", view);
        assertEquals("Connection refused", model.getAttribute("loginerror"));

        Login loginObj = (Login) model.getAttribute("loginObj");
        assertEquals("user", loginObj.getUsername());
        assertEquals("badpass", loginObj.getPassword());
        assertEquals("jdbc:mysql://localhost:3306/apples", loginObj.getUrl());
    }
}
