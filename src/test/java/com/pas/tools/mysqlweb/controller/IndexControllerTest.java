package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.indexes.Index;
import com.pas.tools.mysqlweb.dao.indexes.IndexDAO;
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
class IndexControllerTest
{
    private static final String CONNECTION_ID = "test-session";
    private static final String SCHEMA = "apples";

    @Mock
    private IndexDAO indexDAO;

    @Mock
    private GenericDAO genericDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private IndexController indexController;

    @Test
    void showIndexes_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = indexController.showIndexes(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    @Test
    void showIndexes_returnsIndexesViewWithSessionSchema() throws Exception
    {
        Index index = new Index("def", SCHEMA, "users", "idx_users");
        List<String> schemas = List.of(SCHEMA, "mysql");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(indexDAO.retrieveIndexList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of(index));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(schemas);

            String view = indexController.showIndexes(model, response, request, session);

            assertEquals("indexes", view);
            assertEquals(List.of(index), model.getAttribute("indexes"));
            assertEquals(1, model.getAttribute("records"));
            assertEquals(1, model.getAttribute("estimatedrecords"));
            assertEquals(schemas, model.getAttribute("schemas"));
            assertEquals(SCHEMA, model.getAttribute("chosenSchema"));
        }
    }

    @Test
    void showIndexes_usesSelectedSchemaFromRequest() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(request.getParameter("selectedSchema")).thenReturn("information_schema");
            when(indexDAO.retrieveIndexList("information_schema", null, CONNECTION_ID))
                    .thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            Model model = new ExtendedModelMap();
            indexController.showIndexes(model, response, request, session);

            assertEquals("information_schema", model.getAttribute("chosenSchema"));
            verify(indexDAO).retrieveIndexList("information_schema", null, CONNECTION_ID);
        }
    }

    @Test
    void showIndexes_structureAction_addsIndexStructureToModel() throws Exception
    {
        WebResult structure = new WebResult(
                new String[] {"Column_name"},
                List.of(Map.of("Column_name", "id")));
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("idxAction")).thenReturn("STRUCTURE");
            when(request.getParameter("tabName")).thenReturn("users");
            when(request.getParameter("idxName")).thenReturn("idx_users");
            when(indexDAO.getIndexDetails(SCHEMA, "users", "idx_users", CONNECTION_ID)).thenReturn(structure);
            when(indexDAO.retrieveIndexList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = indexController.showIndexes(model, response, request, session);

            assertEquals("indexes", view);
            assertEquals(structure, model.getAttribute("indexStructure"));
            assertEquals("idx_users", model.getAttribute("indexname"));
        }
    }

    @Test
    void showIndexes_dropAction_success_refreshesSchemaMap() throws Exception
    {
        Result dropResult = new Result();
        dropResult.setMessage("SUCCESS: dropped");
        Map<String, Long> schemaMap = Map.of("Index", 2L);
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("idxAction")).thenReturn("DROP");
            when(request.getParameter("idxName")).thenReturn("idx_users");
            when(request.getParameter("tableName")).thenReturn("users");
            when(indexDAO.simpleindexCommand(SCHEMA, "idx_users", "DROP", "users", CONNECTION_ID))
                    .thenReturn(dropResult);
            when(genericDAO.populateSchemaMap(SCHEMA, CONNECTION_ID)).thenReturn(schemaMap);
            when(indexDAO.retrieveIndexList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            indexController.showIndexes(model, response, request, session);

            assertEquals(dropResult, model.getAttribute("result"));
            verify(session).setAttribute("schemaMap", schemaMap);
        }
    }

    @Test
    void performIndexAction_search_addsSearchResults() throws Exception
    {
        Index index = new Index("def", SCHEMA, "users", "idx_users");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("searchpressed")).thenReturn("searchpressed");
            when(request.getParameter("search")).thenReturn("idx");
            when(indexDAO.retrieveIndexList(SCHEMA, "idx", CONNECTION_ID)).thenReturn(List.of(index));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of(SCHEMA));

            String view = indexController.performIndexAction(model, response, request, session);

            assertEquals("indexes", view);
            assertEquals("idx", model.getAttribute("search"));
            assertEquals(List.of(index), model.getAttribute("indexes"));
        }
    }

    @Test
    void performIndexAction_bulkCommands_addsArrayResult() throws Exception
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
            when(request.getParameterValues("selected_idx[]")).thenReturn(new String[] {"idx_users", "idx_orders"});
            when(request.getParameter("submit_mult")).thenReturn("Drop");
            when(indexDAO.simpleindexCommand(eq(SCHEMA), eq("idx_users"), eq("Drop"), eq(""), eq(CONNECTION_ID)))
                    .thenReturn(first);
            when(indexDAO.simpleindexCommand(eq(SCHEMA), eq("idx_orders"), eq("Drop"), eq(""), eq(CONNECTION_ID)))
                    .thenReturn(second);
            when(indexDAO.retrieveIndexList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = indexController.performIndexAction(model, response, request, session);

            assertEquals("indexes", view);
            assertEquals(List.of(first, second), model.getAttribute("arrayresult"));
        }
    }

    @Test
    void performIndexAction_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = indexController.performIndexAction(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    private void stubConnectedSession(MockedStatic<Utils> utils)
    {
        utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(false);
        utils.when(() -> Utils.getConnectionSessionId(session)).thenReturn(CONNECTION_ID);
    }
}
