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
    private static final String VIEW_LIST_PARAM = "selected_view[]";

    @Autowired
    ViewDAO viewDAO;

    @Autowired
    GenericDAO genericDAO;

    @GetMapping("/views")
    public String showViews(Model model, HttpServletResponse response,
                            HttpServletRequest request, HttpSession session) throws Exception
    {
        if (requireConnection(response, session))
        {
            return null;
        }

        log.info("Received request to show views");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        String viewAction = request.getParameter("viewAction");

        if (viewAction != null)
        {
            log.info("viewAction = {}", viewAction);
            handleViewAction(model, session, schema, connectionId, viewAction, request.getParameter("viewName"));
        }

        return renderViewsPage(model, schema, connectionId, null);
    }

    @PostMapping("/views")
    public String performViewAction(Model model, HttpServletResponse response,
                                    HttpServletRequest request, HttpSession session) throws Exception
    {
        if (requireConnection(response, session))
        {
            return null;
        }

        log.info("Received request to perform an action on the views");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        String search = request.getParameter("searchpressed") != null ? request.getParameter("search") : null;

        if (search == null)
        {
            processBulkViewCommands(model, request, schema, connectionId);
        }

        return renderViewsPage(model, schema, connectionId, search);
    }

    private String renderViewsPage(Model model, String schema, String connectionId, String search)
            throws Exception
    {
        List<View> views = viewDAO.retrieveViewList(schema, search, connectionId);
        populateViewsModel(model, schema, connectionId, views);
        if (search != null)
        {
            model.addAttribute("search", search);
        }
        return "views";
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

    private void populateViewsModel(Model model, String schema, String connectionId, List<View> views)
            throws Exception
    {
        int count = views.size();
        model.addAttribute("records", count);
        model.addAttribute("estimatedrecords", count);
        model.addAttribute("views", views);
        model.addAttribute("schemas", genericDAO.allSchemas(connectionId));
        model.addAttribute("chosenSchema", schema);
    }

    private void handleViewAction(Model model, HttpSession session, String schema, String connectionId,
                                  String viewAction, String viewName) throws Exception
    {
        if ("DEF".equalsIgnoreCase(viewAction))
        {
            model.addAttribute("viewName", viewName);
            model.addAttribute("viewdef", viewDAO.getViewDefinition(schema, viewName, connectionId));
            return;
        }

        applyViewCommand(model, session, schema, connectionId, viewName, viewAction);
    }

    private void applyViewCommand(Model model, HttpSession session, String schema, String connectionId,
                                    String viewName, String command) throws Exception
    {
        Result result = viewDAO.simpleviewCommand(schema, viewName, command, connectionId);
        model.addAttribute("result", result);
        refreshSchemaMapOnSuccessfulDrop(session, connectionId, result, command);
    }

    private void refreshSchemaMapOnSuccessfulDrop(HttpSession session, String connectionId,
                                                  Result result, String command) throws Exception
    {
        if (result.getMessage().startsWith("SUCCESS") && "DROP".equalsIgnoreCase(command))
        {
            session.setAttribute("schemaMap",
                    genericDAO.populateSchemaMap((String) session.getAttribute("schema"), connectionId));
        }
    }

    private void processBulkViewCommands(Model model, HttpServletRequest request,
                                         String schema, String connectionId) throws Exception
    {
        String[] viewList = request.getParameterValues(VIEW_LIST_PARAM);
        String command = request.getParameter("submit_mult");

        log.info("viewList = {}", Arrays.toString(viewList));
        log.info("command = {}", command);

        if (viewList == null)
        {
            return;
        }

        List<Result> results = new ArrayList<>();
        for (String view : viewList)
        {
            results.add(viewDAO.simpleviewCommand(schema, view, command, connectionId));
        }
        model.addAttribute("arrayresult", results);
    }
}
