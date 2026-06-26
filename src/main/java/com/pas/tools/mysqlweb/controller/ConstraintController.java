package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.constraints.Constraint;
import com.pas.tools.mysqlweb.dao.constraints.ConstraintDAO;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
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
public class ConstraintController
{
    private static final String CONSTRAINT_LIST_PARAM = "selected_constraint[]";

    @Autowired
    ConstraintDAO constraintDAO;

    @Autowired
    GenericDAO genericDAO;

    @GetMapping("/constraints")
    public String showConstraints(Model model, HttpServletResponse response,
                                  HttpServletRequest request, HttpSession session) throws Exception
    {
        if (requireConnection(response, session))
        {
            return null;
        }

        log.info("Received request to show constraints");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        String constraintAction = request.getParameter("constraintAction");

        if (constraintAction != null)
        {
            log.info("constraintAction = {}", constraintAction);
            handleConstraintAction(model, request, schema, connectionId, constraintAction);
        }

        return renderConstraintsPage(model, schema, connectionId, null);
    }

    @PostMapping("/constraints")
    public String performConstraintAction(Model model, HttpServletResponse response,
                                          HttpServletRequest request, HttpSession session) throws Exception
    {
        if (requireConnection(response, session))
        {
            return null;
        }

        log.info("Received request to perform an action on the constraints");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        String search = request.getParameter("searchpressed") != null ? request.getParameter("search") : null;

        if (search == null)
        {
            processBulkConstraintCommands(model, request, schema, connectionId);
        }

        return renderConstraintsPage(model, schema, connectionId, search);
    }

    private String renderConstraintsPage(Model model, String schema, String connectionId, String search)
            throws Exception
    {
        List<Constraint> constraints = constraintDAO.retrieveConstraintList(schema, search, connectionId);
        populateConstraintsModel(model, schema, connectionId, constraints);
        if (search != null)
        {
            model.addAttribute("search", search);
        }
        return "constraints";
    }

    private boolean requireConnection(HttpServletResponse response, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return true;
        }
        return false;
    }

    private String resolveSchema(HttpServletRequest request, HttpSession session)
    {
        String selectedSchema = request.getParameter("selectedSchema");
        log.info("selectedSchema = {}", selectedSchema);
        String schema = selectedSchema != null ? selectedSchema : (String) session.getAttribute("schema");
        log.info("schema = {}", schema);
        return schema;
    }

    private void populateConstraintsModel(Model model, String schema, String connectionId,
                                          List<Constraint> constraints) throws Exception
    {
        int count = constraints.size();
        model.addAttribute("records", count);
        model.addAttribute("estimatedrecords", count);
        model.addAttribute("constraints", constraints);
        model.addAttribute("schemas", genericDAO.allSchemas(connectionId));
        model.addAttribute("chosenSchema", schema);
    }

    private void handleConstraintAction(Model model, HttpServletRequest request, String schema,
                                        String connectionId, String constraintAction) throws Exception
    {
        if (!"DROP".equals(constraintAction))
        {
            return;
        }

        applyConstraintDrop(model, request, schema, connectionId);
    }

    private void applyConstraintDrop(Model model, HttpServletRequest request, String schema,
                                     String connectionId) throws Exception
    {
        Result result = constraintDAO.simpleconstraintCommand(
                schema,
                request.getParameter("constraintName"),
                request.getParameter("tableName"),
                request.getParameter("constraintType"),
                "DROP",
                connectionId);
        model.addAttribute("result", result);
    }

    private void processBulkConstraintCommands(Model model, HttpServletRequest request,
                                               String schema, String connectionId) throws Exception
    {
        String[] constraintList = request.getParameterValues(CONSTRAINT_LIST_PARAM);
        String command = request.getParameter("submit_mult");

        log.info("constraintList = {}", Arrays.toString(constraintList));
        log.info("command = {}", command);

        if (constraintList == null)
        {
            return;
        }

        List<Result> results = new ArrayList<>();
        for (String constraint : constraintList)
        {
            results.add(constraintDAO.simpleconstraintCommand(schema, constraint, command, "", "", connectionId));
        }
        model.addAttribute("arrayresult", results);
    }
}
