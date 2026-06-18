package com.pas.tools.mysqlweb.controller;

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
import java.util.Map;

@Slf4j
@Controller
public class RefreshController
{

    @GetMapping(value = "/refresh")
    public String refreshPage
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request refresh schema object list");

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        Map<String, Long> schemaMap;
        schemaMap = genericDAO.populateSchemaMap((String)session.getAttribute("schema"),
                                                 (String)session.getAttribute("user_key"));

        log.info("schemaMap=" + schemaMap);
        session.setAttribute("schemaMap", schemaMap);

        return "main";
    }
}
