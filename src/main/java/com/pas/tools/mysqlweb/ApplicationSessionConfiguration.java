package com.pas.tools.mysqlweb;

import com.pas.tools.mysqlweb.utils.SessionListener;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSessionListener;

@Configuration
public class ApplicationSessionConfiguration
{
    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> sessionListenerRegistration(
            SessionListener sessionListener)
    {
        return new ServletListenerRegistrationBean<>(sessionListener);
    }
}
