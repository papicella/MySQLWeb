package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.Utils;
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
public class UserController
{
    @Autowired
    GenericDAO genericDAO;

    @GetMapping(value = "/userinfo")
    public String userDetails
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        log.info("Received request to show user information");

        WebResult processList, privsList, sizeVariables;

        sizeVariables = genericDAO.runGenericQuery
                ("SHOW VARIABLES LIKE '%size%'", null, Utils.getConnectionSessionId(session), -1);


        privsList = genericDAO.runGenericQuery
                ("SHOW PRIVILEGES", null, Utils.getConnectionSessionId(session), -1);

        processList = genericDAO.runGenericQuery
                ("SHOW processlist", null, Utils.getConnectionSessionId(session), -1);

        model.addAttribute("privsList", privsList);
        model.addAttribute("processList", processList);
        model.addAttribute("sizeVariables", sizeVariables);

        return "info";
    }
}
