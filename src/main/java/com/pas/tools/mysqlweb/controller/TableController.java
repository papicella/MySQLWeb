package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
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

    @GetMapping(value = "/tables")
    public String showTables
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {

        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        String schema = null;
        WebResult tableStructure, tableDetails, tableIndexes;

        log.info("Received request to show tables");

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

        String tabAction = request.getParameter("tabAction");
        Result result = new Result();

        if (tabAction != null)
        {
            log.info("tabAction = " + tabAction);
            result = null;

            if (tabAction.equalsIgnoreCase("STRUCTURE"))
            {

                tableStructure =
                        tableDAO.getTableStructure
                                (schema,
                                        (String)request.getParameter("tabName"),
                                        Utils.getConnectionSessionId(session));


                model.addAttribute("tableStructure", tableStructure);
                model.addAttribute("tablename", (String)request.getParameter("tabName"));
            }
            else if (tabAction.equalsIgnoreCase("DETAILS"))
            {
                tableDetails =
                        tableDAO.getTableDetails
                                (schema,
                                        (String)request.getParameter("tabName"),
                                        Utils.getConnectionSessionId(session));


                model.addAttribute("tableDetails", tableDetails);
                model.addAttribute("tablename", (String)request.getParameter("tabName"));
            }
            else if (tabAction.equalsIgnoreCase("DDL"))
            {
                String ddl = tableDAO.runShowQuery(schema,
                                                   (String)request.getParameter("tabName"),
                                                   Utils.getConnectionSessionId(session));

                model.addAttribute("tableDDL", ddl.trim());
                model.addAttribute("tablename", (String)request.getParameter("tabName"));
            }
            else if (tabAction.equalsIgnoreCase("INDEXES"))
            {
                tableIndexes = tableDAO.showIndexes(schema,
                        (String)request.getParameter("tabName"),
                        Utils.getConnectionSessionId(session));

                model.addAttribute("tableIndexes", tableIndexes);
                model.addAttribute("tablename", (String)request.getParameter("tabName"));
            }
            else
            {
                result =
                        tableDAO.simpletableCommand
                                (schema,
                                        (String)request.getParameter("tabName"),
                                        tabAction,
                                        Utils.getConnectionSessionId(session));
                model.addAttribute("result", result);

                if (result.getMessage().startsWith("SUCCESS"))
                {
                    if (tabAction.equalsIgnoreCase("DROP"))
                    {
                        session.setAttribute("schemaMap",
                                             genericDAO.populateSchemaMap
                                               ((String)session.getAttribute("schema"),
                                                Utils.getConnectionSessionId(session)));
                    }
                }
            }
        }

        List<Table> tbls = tableDAO.retrieveTableList
                  (schema, null, Utils.getConnectionSessionId(session));

        model.addAttribute("records", tbls.size());
        model.addAttribute("estimatedrecords", tbls.size());
        model.addAttribute("tables", tbls);

        model.addAttribute
                ("schemas",
                 genericDAO.allSchemas(Utils.getConnectionSessionId(session)));

        model.addAttribute("chosenSchema", schema);

        return "tables";
    }

    @PostMapping(value = "/tables")
    public String performTableAction
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        String schema = null;
        Result result = new Result();
        List<Table> tbls = null;

        log.info("Received request to perform an action on the tables");

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
            tbls = tableDAO.retrieveTableList
                            (schema,
                            (String)request.getParameter("search"),
                            Utils.getConnectionSessionId(session));

            model.addAttribute("search", (String)request.getParameter("search"));
        }
        else
        {
            String[] tableList  = request.getParameterValues("selected_tbl[]");
            String   commandStr = request.getParameter("submit_mult");

            log.info("tableList = " + Arrays.toString(tableList));
            log.info("command = " + commandStr);

            // start actions now if tableList is not null

            if (tableList != null)
            {
                List al = new ArrayList<Result>();
                for (String table: tableList)
                {
                    result = null;
                    result = tableDAO.simpletableCommand
                            (schema,
                                    table,
                                    commandStr,
                                    Utils.getConnectionSessionId(session));

                    al.add(result);
                }

                model.addAttribute("arrayresult", al);
            }

            tbls = tableDAO.retrieveTableList
                            (schema, null, Utils.getConnectionSessionId(session));
        }

        model.addAttribute("records", tbls.size());
        model.addAttribute("estimatedrecords", tbls.size());
        model.addAttribute("tables", tbls);

        model.addAttribute
                ("schemas", genericDAO.allSchemas(Utils.getConnectionSessionId(session)));

        model.addAttribute("chosenSchema", schema);

        return "tables";
    }
}
