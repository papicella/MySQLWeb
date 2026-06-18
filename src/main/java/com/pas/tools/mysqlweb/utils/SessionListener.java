package com.pas.tools.mysqlweb.utils;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionListener implements HttpSessionListener
{
    private HttpSession session = null;

    public void sessionCreated(HttpSessionEvent event)
    {
        // no need to do anything here as connection may not have been established yet
        session  = event.getSession();
        log.info("Session created for id " + session.getId());
    }

    public void sessionDestroyed(HttpSessionEvent event)
    {
        session  = event.getSession();
        ConnectionManager cm = null;

        try
        {
            cm = ConnectionManager.getInstance();
            cm.removeDataSource(session.getId());
            log.info("Session destroyed for id " + session.getId());
        }
        catch (Exception e)
        {
            log.info("SesssionListener.sessionDestroyed was unable to obtain Connection", e);
        }
    }
}

