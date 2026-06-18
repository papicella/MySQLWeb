package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.constraints.Constraint;
import com.pas.tools.mysqlweb.dao.constraints.ConstraintDAO;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ConstraintController
{
    protected static Logger logger = LoggerFactory.getLogger(ConstraintController.class);

    @GetMapping(value = "/constraints")
    public String showConstraints
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        String schema = null;

        log.info("Received request to show constraints");

        ConstraintDAO constraintDAO = PivotalMySQLWebDAOFactory.getConstraintDAO();
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        String selectedSchema = request.getParameter("selectedSchema");
        log.info("selectedSchema = " + selectedSchema);

        if (selectedSchema != null)
        {
            schema = selectedSchema;
        }
        else
        {
            schema = (String) session.getAttribute("schema");
        }

        log.info("schema = " + schema);

        String constraintAction = request.getParameter("constraintAction");
        Result result = new Result();

        if (constraintAction != null)
        {
            log.info("constraintAction = " + constraintAction);
            result = null;

            if (constraintAction.equals("DROP"))
            {
                result =
                        constraintDAO.simpleconstraintCommand
                                (schema,
                                (String) request.getParameter("constraintName"),
                                (String) request.getParameter("tableName"),
                                (String) request.getParameter("constraintType"),
                                constraintAction,
                                (String) session.getAttribute("user_key"));
                model.addAttribute("result", result);
            }
        }

        List<Constraint> constraints = constraintDAO.retrieveConstraintList
                (schema, null, (String)session.getAttribute("user_key"));

        model.addAttribute("records", constraints.size());
        model.addAttribute("estimatedrecords", constraints.size());
        model.addAttribute("constraints", constraints);

        model.addAttribute
                ("schemas", genericDAO.allSchemas((String) session.getAttribute("user_key")));

        model.addAttribute("chosenSchema", schema);

        return "constraints";

    }

    @PostMapping(value = "/constraints")
    public String performConstraintAction
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {

        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        String schema = null;
        Result result = new Result();
        List<Constraint> constraints = null;

        log.info("Received request to perform an action on the constraints");

        ConstraintDAO constraintDAO = PivotalMySQLWebDAOFactory.getConstraintDAO();
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        String selectedSchema = request.getParameter("selectedSchema");
        log.info("selectedSchema = " + selectedSchema);

        if (selectedSchema != null)
        {
            schema = selectedSchema;
        }
        else
        {
            schema = (String) session.getAttribute("schema");
        }

        log.info("schema = " + schema);

        if (request.getParameter("searchpressed") != null)
        {
            constraints = constraintDAO.retrieveConstraintList
                    (schema,
                    (String)request.getParameter("search"),
                    (String)session.getAttribute("user_key"));

            model.addAttribute("search", (String)request.getParameter("search"));
        }
        else
        {
            String[] tableList  = request.getParameterValues("selected_constraint[]");
            String   commandStr = request.getParameter("submit_mult");

            log.info("tableList = " + Arrays.toString(tableList));
            log.info("command = " + commandStr);

            // start actions now if tableList is not null

            if (tableList != null)
            {
                List al = new ArrayList<Result>();
                for (String constraint: tableList)
                {
                    result = null;
                    result = constraintDAO.simpleconstraintCommand
                            (schema,
                             constraint,
                             commandStr,
                             "",
                             "",
                             (String)session.getAttribute("user_key"));

                    al.add(result);
                }

                model.addAttribute("arrayresult", al);
            }

            constraints = constraintDAO.retrieveConstraintList
                    (schema, null, (String)session.getAttribute("user_key"));

        }

        model.addAttribute("records", constraints.size());
        model.addAttribute("estimatedrecords", constraints.size());
        model.addAttribute("constraints", constraints);

        model.addAttribute
                ("schemas", genericDAO.allSchemas((String) session.getAttribute("user_key")));

        model.addAttribute("chosenSchema", schema);

        return "constraints";
    }
}
