package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
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

    @GetMapping("/indexes")
    public String showIndexes(Model model, HttpServletResponse response,
                              HttpServletRequest request, HttpSession session) throws Exception
    {
        if (redirectIfNoConnection(response, session))
        {
            return null;
        }

        log.info("Received request to show indexes");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        String idxAction = request.getParameter("idxAction");

        if (idxAction != null)
        {
            log.info("idxAction = {}", idxAction);
            handleIdxAction(model, session, request, schema, connectionId, idxAction);
        }

        List<Index> indexes = indexDAO.retrieveIndexList(schema, null, connectionId);
        populateIndexesModel(model, session, schema, indexes);
        return "indexes";
    }

    @PostMapping("/indexes")
    public String performIndexAction(Model model, HttpServletResponse response,
                                     HttpServletRequest request, HttpSession session) throws Exception
    {
        if (redirectIfNoConnection(response, session))
        {
            return null;
        }

        log.info("Received request to perform an action on the indexes");

        String schema = resolveSchema(request, session);
        String connectionId = Utils.getConnectionSessionId(session);
        List<Index> indexes;

        if (request.getParameter("searchpressed") != null)
        {
            String search = request.getParameter("search");
            indexes = indexDAO.retrieveIndexList(schema, search, connectionId);
            model.addAttribute("search", search);
        }
        else
        {
            processBulkIndexCommands(model, request, schema, connectionId);
            indexes = indexDAO.retrieveIndexList(schema, null, connectionId);
        }

        populateIndexesModel(model, session, schema, indexes);
        return "indexes";
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

    private void populateIndexesModel(Model model, HttpSession session, String schema, List<Index> indexes)
            throws Exception
    {
        String connectionId = Utils.getConnectionSessionId(session);
        int count = indexes.size();
        model.addAttribute("records", count);
        model.addAttribute("estimatedrecords", count);
        model.addAttribute("indexes", indexes);
        model.addAttribute("schemas", genericDAO.allSchemas(connectionId));
        model.addAttribute("chosenSchema", schema);
    }

    private void handleIdxAction(Model model, HttpSession session, HttpServletRequest request,
                                 String schema, String connectionId, String idxAction) throws Exception
    {
        if ("STRUCTURE".equals(idxAction))
        {
            String tabName = request.getParameter("tabName");
            String idxName = request.getParameter("idxName");
            model.addAttribute("indexStructure",
                    indexDAO.getIndexDetails(schema, tabName, idxName, connectionId));
            model.addAttribute("indexname", idxName);
            return;
        }

        String idxName = request.getParameter("idxName");
        String tableName = request.getParameter("tableName");
        Result result = indexDAO.simpleindexCommand(schema, idxName, idxAction, tableName, connectionId);
        model.addAttribute("result", result);

        if (result.getMessage().startsWith("SUCCESS") && idxAction.equalsIgnoreCase("DROP"))
        {
            session.setAttribute("schemaMap",
                    genericDAO.populateSchemaMap((String) session.getAttribute("schema"), connectionId));
        }
    }

    private void processBulkIndexCommands(Model model, HttpServletRequest request,
                                          String schema, String connectionId) throws Exception
    {
        String[] indexList = request.getParameterValues("selected_idx[]");
        String command = request.getParameter("submit_mult");

        log.info("tableList = {}", Arrays.toString(indexList));
        log.info("command = {}", command);

        if (indexList == null)
        {
            return;
        }

        List<Result> results = new ArrayList<>();
        for (String index : indexList)
        {
            results.add(indexDAO.simpleindexCommand(schema, index, command, "", connectionId));
        }
        model.addAttribute("arrayresult", results);
    }
}
