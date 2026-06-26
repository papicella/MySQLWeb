package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.tables.Table;
import com.pas.tools.mysqlweb.dao.tables.TableDAO;
import com.pas.tools.mysqlweb.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TableControllerTest
{
    private static final String CONNECTION_ID = "test-session";
    private static final String SCHEMA = "apples";

    @Mock
    private TableDAO tableDAO;

    @Mock
    private GenericDAO genericDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TableController tableController;

    @Test
    void showTables_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = tableController.showTables(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    @Test
    void showTables_returnsTablesViewWithSessionSchema() throws Exception
    {
        Table table = new Table("def", SCHEMA, "users", "BASE TABLE");
        List<String> schemas = List.of(SCHEMA, "mysql");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(tableDAO.retrieveTableList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of(table));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(schemas);

            String view = tableController.showTables(model, response, request, session);

            assertEquals("tables", view);
            assertEquals(List.of(table), model.getAttribute("tables"));
            assertEquals(1, model.getAttribute("records"));
            assertEquals(1, model.getAttribute("estimatedrecords"));
            assertEquals(schemas, model.getAttribute("schemas"));
            assertEquals(SCHEMA, model.getAttribute("chosenSchema"));
        }
    }

    @Test
    void showTables_usesSelectedSchemaFromRequest() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(request.getParameter("selectedSchema")).thenReturn("information_schema");
            when(tableDAO.retrieveTableList("information_schema", null, CONNECTION_ID))
                    .thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            Model model = new ExtendedModelMap();
            tableController.showTables(model, response, request, session);

            assertEquals("information_schema", model.getAttribute("chosenSchema"));
            verify(tableDAO).retrieveTableList("information_schema", null, CONNECTION_ID);
        }
    }

    @Test
    void showTables_detailsAction_addsTableDetailsToModel() throws Exception
    {
        WebResult details = new WebResult(
                new String[] {"TABLE_NAME"},
                List.of(Map.of("TABLE_NAME", "users")));
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("tabAction")).thenReturn("DETAILS");
            when(request.getParameter("tabName")).thenReturn("users");
            when(tableDAO.getTableDetails(SCHEMA, "users", CONNECTION_ID)).thenReturn(details);
            when(tableDAO.retrieveTableList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = tableController.showTables(model, response, request, session);

            assertEquals("tables", view);
            assertEquals(details, model.getAttribute("tableDetails"));
            assertEquals("users", model.getAttribute("tablename"));
        }
    }

    @Test
    void showTables_ddlAction_addsTrimmedDdl() throws Exception
    {
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("tabAction")).thenReturn("DDL");
            when(request.getParameter("tabName")).thenReturn("users");
            when(tableDAO.runShowQuery(SCHEMA, "users", CONNECTION_ID)).thenReturn(" CREATE TABLE users (id INT) ");
            when(tableDAO.retrieveTableList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            tableController.showTables(model, response, request, session);

            assertEquals("CREATE TABLE users (id INT)", model.getAttribute("tableDDL"));
            assertEquals("users", model.getAttribute("tablename"));
        }
    }

    @Test
    void showTables_dropAction_success_refreshesSchemaMap() throws Exception
    {
        Result dropResult = new Result();
        dropResult.setMessage("SUCCESS: dropped");
        Map<String, Long> schemaMap = Map.of("Table", 1L);
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("tabAction")).thenReturn("DROP");
            when(request.getParameter("tabName")).thenReturn("users");
            when(tableDAO.simpletableCommand(SCHEMA, "users", "DROP", CONNECTION_ID)).thenReturn(dropResult);
            when(genericDAO.populateSchemaMap(SCHEMA, CONNECTION_ID)).thenReturn(schemaMap);
            when(tableDAO.retrieveTableList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            tableController.showTables(model, response, request, session);

            assertEquals(dropResult, model.getAttribute("result"));
            verify(session).setAttribute("schemaMap", schemaMap);
        }
    }

    @Test
    void performTableAction_search_addsSearchResults() throws Exception
    {
        Table table = new Table("def", SCHEMA, "users", "BASE TABLE");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("searchpressed")).thenReturn("searchpressed");
            when(request.getParameter("search")).thenReturn("user");
            when(tableDAO.retrieveTableList(SCHEMA, "user", CONNECTION_ID)).thenReturn(List.of(table));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of(SCHEMA));

            String view = tableController.performTableAction(model, response, request, session);

            assertEquals("tables", view);
            assertEquals("user", model.getAttribute("search"));
            assertEquals(List.of(table), model.getAttribute("tables"));
        }
    }

    @Test
    void performTableAction_bulkCommands_addsArrayResult() throws Exception
    {
        Result first = new Result();
        first.setMessage("SUCCESS");
        Result second = new Result();
        second.setMessage("SUCCESS");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameterValues("selected_tbl[]")).thenReturn(new String[] {"users", "orders"});
            when(request.getParameter("submit_mult")).thenReturn("Drop");
            when(tableDAO.simpletableCommand(eq(SCHEMA), eq("users"), eq("Drop"), eq(CONNECTION_ID)))
                    .thenReturn(first);
            when(tableDAO.simpletableCommand(eq(SCHEMA), eq("orders"), eq("Drop"), eq(CONNECTION_ID)))
                    .thenReturn(second);
            when(tableDAO.retrieveTableList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = tableController.performTableAction(model, response, request, session);

            assertEquals("tables", view);
            assertEquals(List.of(first, second), model.getAttribute("arrayresult"));
        }
    }

    @Test
    void performTableAction_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = tableController.performTableAction(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    private void stubConnectedSession(MockedStatic<Utils> utils)
    {
        utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(false);
        utils.when(() -> Utils.getConnectionSessionId(session)).thenReturn(CONNECTION_ID);
    }
}
