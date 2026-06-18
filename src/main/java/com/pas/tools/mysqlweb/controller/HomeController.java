package com.pas.tools.mysqlweb.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.Constants;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController
{
    @GetMapping(value = "/home")
    public String login(Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {

        log.info("Received request to show home page");

        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
        WebResult databaseList;

        databaseList = genericDAO.runGenericQuery
                (Constants.DATABASE_LIST, null, session.getId(), -1);

        model.addAttribute("databaseList", databaseList);

        return "main";
    }

}
