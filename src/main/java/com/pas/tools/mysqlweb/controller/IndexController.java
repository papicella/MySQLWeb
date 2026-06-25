package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.indexes.Index;
import com.pas.tools.mysqlweb.dao.indexes.IndexDAO;
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
public class IndexController
{
    @Autowired
    IndexDAO indexDAO;

    @Autowired
    GenericDAO genericDAO;

    @GetMapping(value = "/indexes")
    public String showIndexes
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception {

        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        String schema = null;
        WebResult indexStructure;

        log.info("Received request to show indexes");

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

        String idxAction = request.getParameter("idxAction");
        Result result = new Result();

        if (idxAction != null)
        {
            log.info("idxAction = " + idxAction);
            result = null;

            if (idxAction.equals("STRUCTURE"))
            {
                indexStructure =
                        indexDAO.getIndexDetails
                                (schema,
                                 (String)request.getParameter("tabName"),
                                 (String)request.getParameter("idxName"),
                                 Utils.getConnectionSessionId(session));

                model.addAttribute("indexStructure", indexStructure);
                model.addAttribute("indexname", (String)request.getParameter("idxName"));
            }
            else
            {
                result =
                        indexDAO.simpleindexCommand
                                (schema,
                                        (String) request.getParameter("idxName"),
                                        idxAction,
                                        (String) request.getParameter("tableName"),
                                        Utils.getConnectionSessionId(session));
                model.addAttribute("result", result);

                if (result.getMessage().startsWith("SUCCESS"))
                {
                    if (idxAction.equalsIgnoreCase("DROP"))
                    {
                        session.setAttribute("schemaMap",
                                            genericDAO.populateSchemaMap
                                                ((String)session.getAttribute("schema"),
                                                Utils.getConnectionSessionId(session)));
                    }
                }
            }
        }

        List<Index> indexes = indexDAO.retrieveIndexList
                (schema, null, Utils.getConnectionSessionId(session));

        model.addAttribute("records", indexes.size());
        model.addAttribute("estimatedrecords", indexes.size());
        model.addAttribute("indexes", indexes);

        model.addAttribute
                ("schemas", genericDAO.allSchemas(Utils.getConnectionSessionId(session)));

        model.addAttribute("chosenSchema", schema);

        return "indexes";
    }

    @PostMapping(value = "/indexes")
    public String performIndexAction
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("No active JDBC connection for session so new Login required");
            return null;
        }

        String schema = null;
        Result result = new Result();
        List<Index> indexes = null;

        log.info("Received request to perform an action on the indexes");

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
            indexes = indexDAO.retrieveIndexList
                    (schema,
                            (String)request.getParameter("search"),
                            Utils.getConnectionSessionId(session));

            model.addAttribute("search", (String)request.getParameter("search"));
        }
        else
        {
            String[] tableList  = request.getParameterValues("selected_idx[]");
            String   commandStr = request.getParameter("submit_mult");

            log.info("tableList = " + Arrays.toString(tableList));
            log.info("command = " + commandStr);

            // start actions now if tableList is not null

            if (tableList != null)
            {
                List<Result> al = new ArrayList<>();
                for (String index: tableList)
                {
                    result = null;
                    result = indexDAO.simpleindexCommand
                            (schema,
                             index,
                             commandStr,
                             "",
                             Utils.getConnectionSessionId(session));

                    al.add(result);
                }

                model.addAttribute("arrayresult", al);
            }

            indexes = indexDAO.retrieveIndexList
                    (schema, null, Utils.getConnectionSessionId(session));

        }

        model.addAttribute("records", indexes.size());
        model.addAttribute("estimatedrecords", indexes.size());
        model.addAttribute("indexes", indexes);

        model.addAttribute
                ("schemas", genericDAO.allSchemas(Utils.getConnectionSessionId(session)));

        model.addAttribute("chosenSchema", schema);

        return "indexes";

    }

}