package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.UserPref;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.tables.Constants;
import com.pas.tools.mysqlweb.dao.tables.TableDAO;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class TableViewerController
{

    private String tableRows = "select * from %s.%s limit %s";

    @GetMapping(value = "/tableviewer")
    public String showTables
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request to show table viewer page");

        UserPref userPrefs = (UserPref) session.getAttribute("prefs");

        String schema = null;
        WebResult describeStructure, tableData, queryResultsDescribe, tableDetails, tableIndexes;

        TableDAO tableDAO = PivotalMySQLWebDAOFactory.getTableDAO();
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        String selectedSchema = request.getParameter("selectedSchema");
        String tabName = (String)request.getParameter("tabName");

        log.info("selectedSchema = " + selectedSchema);
        log.info("tabName = " + tabName);

        if (selectedSchema != null)
        {
            schema = selectedSchema;
        }
        else
        {
            schema = (String) session.getAttribute("schema");
        }

        // describe table
        String ddl = tableDAO.runShowQuery(schema,
                tabName,
                (String)session.getAttribute("user_key"));

        model.addAttribute("tableDDL", ddl);
        model.addAttribute("tablename", tabName.toUpperCase());

        // get table rows
        tableData = genericDAO.runGenericQuery
                (String.format(tableRows, schema, tabName, userPrefs.getSampleDataSize()), null, (String)session.getAttribute("user_key"), -1);

        model.addAttribute("queryResults", tableData);
        model.addAttribute("queryResultsSize", tableData.getRows().size());

        // describe table
        queryResultsDescribe = genericDAO.runGenericQuery
                (String.format(Constants.TABLE_STRUCTURE, schema, tabName), null, (String)session.getAttribute("user_key"), -1);
        model.addAttribute("queryResultsDescribe", queryResultsDescribe);

        // view all table details
        tableDetails =
                tableDAO.getTableDetails
                        (schema, (String)request.getParameter("tabName"), (String)session.getAttribute("user_key"));


        model.addAttribute("tableDetails", tableDetails);

        // view table indexes
        tableIndexes = tableDAO.showIndexes(schema,
                tabName,
                (String)session.getAttribute("user_key"));

        model.addAttribute("tableIndexes", tableIndexes);

        return "tableviewer";
    }
}
