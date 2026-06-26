package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.tables.Table;
import com.pas.tools.mysqlweb.dao.tables.TableDAO;
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
public class TableController
{
    @Autowired
    TableDAO tableDAO;

    @Autowired
    GenericDAO genericDAO;

    @GetMapping("/tables")
    public String showTables(Model model, HttpServletResponse response,
                             HttpServletRequest request, HttpSession session) throws Exception
    {
        if (redirectIfNoConnection(response, session))
        {
            return null;
        }

        log.info("Received request to show tables");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        String tabAction = request.getParameter("tabAction");
        String tabName = request.getParameter("tabName");

        if (tabAction != null)
        {
            log.info("tabAction = {}", tabAction);
            handleTabAction(model, session, schema, connectionId, tabAction, tabName);
        }

        List<Table> tables = tableDAO.retrieveTableList(schema, null, connectionId);
        populateTablesModel(model, session, schema, tables);
        return "tables";
    }

    @PostMapping("/tables")
    public String performTableAction(Model model, HttpServletResponse response,
                                     HttpServletRequest request, HttpSession session) throws Exception
    {
        if (redirectIfNoConnection(response, session))
        {
            return null;
        }

        log.info("Received request to perform an action on the tables");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        List<Table> tables;

        if (request.getParameter("searchpressed") != null)
        {
            String search = request.getParameter("search");
            tables = tableDAO.retrieveTableList(schema, search, connectionId);
            model.addAttribute("search", search);
        }
        else
        {
            processBulkTableCommands(model, request, schema, connectionId);
            tables = tableDAO.retrieveTableList(schema, null, connectionId);
        }

        populateTablesModel(model, session, schema, tables);
        return "tables";
    }

    private boolean redirectIfNoConnection(HttpServletResponse response, HttpSession session) throws Exception
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

    private void populateTablesModel(Model model, HttpSession session, String schema, List<Table> tables)
            throws Exception
    {
        String connectionId = Utils.getConnectionSessionId(session);
        int count = tables.size();
        model.addAttribute("records", count);
        model.addAttribute("estimatedrecords", count);
        model.addAttribute("tables", tables);
        model.addAttribute("schemas", genericDAO.allSchemas(connectionId));
        model.addAttribute("chosenSchema", schema);
    }

    private void handleTabAction(Model model, HttpSession session, String schema, String connectionId,
                                 String tabAction, String tabName) throws Exception
    {
        switch (tabAction.toUpperCase())
        {
            case "STRUCTURE":
                model.addAttribute("tableStructure", tableDAO.getTableStructure(schema, tabName, connectionId));
                model.addAttribute("tablename", tabName);
                break;
            case "DETAILS":
                model.addAttribute("tableDetails", tableDAO.getTableDetails(schema, tabName, connectionId));
                model.addAttribute("tablename", tabName);
                break;
            case "DDL":
                model.addAttribute("tableDDL", tableDAO.runShowQuery(schema, tabName, connectionId).trim());
                model.addAttribute("tablename", tabName);
                break;
            case "INDEXES":
                model.addAttribute("tableIndexes", tableDAO.showIndexes(schema, tabName, connectionId));
                model.addAttribute("tablename", tabName);
                break;
            default:
                Result result = tableDAO.simpletableCommand(schema, tabName, tabAction, connectionId);
                model.addAttribute("result", result);
                if (result.getMessage().startsWith("SUCCESS") && tabAction.equalsIgnoreCase("DROP"))
                {
                    session.setAttribute("schemaMap",
                            genericDAO.populateSchemaMap((String) session.getAttribute("schema"), connectionId));
                }
                break;
        }
    }

    private void processBulkTableCommands(Model model, HttpServletRequest request,
                                          String schema, String connectionId) throws Exception
    {
        String[] tableList = request.getParameterValues("selected_tbl[]");
        String command = request.getParameter("submit_mult");

        log.info("tableList = {}", Arrays.toString(tableList));
        log.info("command = {}", command);

        if (tableList == null)
        {
            return;
        }

        List<Result> results = new ArrayList<>();
        for (String table : tableList)
        {
            results.add(tableDAO.simpletableCommand(schema, table, command, connectionId));
        }
        model.addAttribute("arrayresult", results);
    }
}
