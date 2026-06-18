package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class VariableController
{
    
    @GetMapping(value = "/variables")
    public String databaseVariables
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request to show database variables");

        WebResult dbVariables;

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        dbVariables = genericDAO.runGenericQuery
                ("SHOW VARIABLES", null, (String)session.getAttribute("user_key"), -1);

        model.addAttribute("dbVariables", dbVariables);

        return "variables";
    }

}
