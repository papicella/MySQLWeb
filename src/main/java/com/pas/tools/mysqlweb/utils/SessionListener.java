package com.pas.tools.mysqlweb.utils;

import com.pas.tools.mysqlweb.service.SessionConnectionRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@Slf4j
@Component
public class SessionListener implements HttpSessionListener
{
    @Autowired
    SessionConnectionRegistry sessionConnectionRegistry;

    @Override
    public void sessionCreated(HttpSessionEvent event)
    {
        log.info("Session created for id {}", event.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event)
    {
        HttpSession session = event.getSession();
        sessionConnectionRegistry.release(session.getId());
        log.info("Session destroyed for id {}", session.getId());
    }
}
