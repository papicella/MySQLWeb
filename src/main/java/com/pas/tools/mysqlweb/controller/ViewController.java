package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.views.View;
import com.pas.tools.mysqlweb.dao.views.ViewDAO;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
public class ViewController
{
    @Autowired
    ViewDAO viewDAO;

    @Autowired
    GenericDAO genericDAO;

    @GetMapping(value = "/views")
    public String showViews
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        String schema = null;

        log.info("Received request to show views");

        Result result = new Result();

        String viewAction = request.getParameter("viewAction");
        String selectedSchema = request.getParameter("selectedSchema");
        log.info("selectedSchema = " + selectedSchema);

        if (selectedSchema != null)
        {
            schema = selectedSchema;
        }
        else
        {
            schema = (String)session.getAttribute("schema");
        }

        log.info("schema = " + schema);

        if (viewAction != null)
        {
            log.info("viewAction = " + viewAction);

            if (viewAction.equals("DEF"))
            {
                String def =
                        viewDAO.getViewDefinition
                                (schema,
                                        (String)request.getParameter("viewName"),
                                        Utils.getConnectionSessionId(session));

                model.addAttribute("viewName", (String)request.getParameter("viewName"));
                model.addAttribute("viewdef", def);
            }
            else
            {
                result = null;
                result =
                        viewDAO.simpleviewCommand
                                (schema,
                                        (String)request.getParameter("viewName"),
                                        viewAction,
                                        Utils.getConnectionSessionId(session));

                model.addAttribute("result", result);

                if (result.getMessage().startsWith("SUCCESS"))
                {
                    if (viewAction.equalsIgnoreCase("DROP"))
                    {
                        session.setAttribute("schemaMap",
                                        genericDAO.populateSchemaMap
                                                ((String)session.getAttribute("schema"),
                                                Utils.getConnectionSessionId(session)));
                    }
                }
            }
        }

        List<View> views = viewDAO.retrieveViewList
                (schema,
                        null,
                        Utils.getConnectionSessionId(session));

        model.addAttribute("records", views.size());
        model.addAttribute("estimatedrecords", views.size());
        model.addAttribute("views", views);

        model.addAttribute
                ("schemas", genericDAO.allSchemas(Utils.getConnectionSessionId(session)));

        model.addAttribute("chosenSchema", schema);

        return "views";
    }

    @PostMapping(value = "/views")
    public String performViewAction
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        String schema = null;

        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        Result result = new Result();
        List<View> views = null;

        log.info("Received request to perform an action on the views");

        String selectedSchema = request.getParameter("selectedSchema");
        log.info("selectedSchema = " + selectedSchema);

        if (selectedSchema != null)
        {
            schema = selectedSchema;
        }
        else
        {
            schema = (String)session.getAttribute("schema");
        }

        log.info("schema = " + schema);

        if (request.getParameter("searchpressed") != null)
        {
            views = viewDAO.retrieveViewList
                    (schema,
                            (String)request.getParameter("search"),
                            Utils.getConnectionSessionId(session));

            model.addAttribute("search", (String)request.getParameter("search"));
        }
        else
        {
            String[] tableList  = request.getParameterValues("selected_view[]");
            String   commandStr = request.getParameter("submit_mult");

            log.info("tableList = " + Arrays.toString(tableList));
            log.info("command = " + commandStr);

            // start actions now if tableList is not null

            if (tableList != null)
            {
                List al = new ArrayList<Result>();
                for (String view: tableList)
                {
                    result = null;
                    result =
                            viewDAO.simpleviewCommand
                                    (schema,
                                            view,
                                            commandStr,
                                            Utils.getConnectionSessionId(session));
                    al.add(result);
                }

                model.addAttribute("arrayresult", al);
            }

            views = viewDAO.retrieveViewList
                    (schema,
                            null,
                            Utils.getConnectionSessionId(session));

        }

        model.addAttribute("records", views.size());
        model.addAttribute("estimatedrecords", views.size());
        model.addAttribute("views", views);
        model.addAttribute
                ("schemas", genericDAO.allSchemas(Utils.getConnectionSessionId(session)));

        model.addAttribute("chosenSchema", schema);

        return "views";
    }
}
