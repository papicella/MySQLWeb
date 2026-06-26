package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.views.View;
import com.pas.tools.mysqlweb.dao.views.ViewDAO;
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
class ViewControllerTest
{
    private static final String CONNECTION_ID = "test-session";
    private static final String SCHEMA = "apples";

    @Mock
    private ViewDAO viewDAO;

    @Mock
    private GenericDAO genericDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ViewController viewController;

    @Test
    void showViews_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = viewController.showViews(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    @Test
    void showViews_returnsViewsViewWithSessionSchema() throws Exception
    {
        View viewEntity = new View("def", SCHEMA, "active_users");
        List<String> schemas = List.of(SCHEMA, "mysql");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(viewDAO.retrieveViewList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of(viewEntity));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(schemas);

            String view = viewController.showViews(model, response, request, session);

            assertEquals("views", view);
            assertEquals(List.of(viewEntity), model.getAttribute("views"));
            assertEquals(1, model.getAttribute("records"));
            assertEquals(1, model.getAttribute("estimatedrecords"));
            assertEquals(schemas, model.getAttribute("schemas"));
            assertEquals(SCHEMA, model.getAttribute("chosenSchema"));
        }
    }

    @Test
    void showViews_usesSelectedSchemaFromRequest() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(request.getParameter("selectedSchema")).thenReturn("information_schema");
            when(viewDAO.retrieveViewList("information_schema", null, CONNECTION_ID))
                    .thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            Model model = new ExtendedModelMap();
            viewController.showViews(model, response, request, session);

            assertEquals("information_schema", model.getAttribute("chosenSchema"));
            verify(viewDAO).retrieveViewList("information_schema", null, CONNECTION_ID);
        }
    }

    @Test
    void showViews_defAction_addsViewDefinitionToModel() throws Exception
    {
        String viewDef = "SELECT id, name FROM users WHERE active = 1";
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("viewAction")).thenReturn("DEF");
            when(request.getParameter("viewName")).thenReturn("active_users");
            when(viewDAO.getViewDefinition(SCHEMA, "active_users", CONNECTION_ID)).thenReturn(viewDef);
            when(viewDAO.retrieveViewList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = viewController.showViews(model, response, request, session);

            assertEquals("views", view);
            assertEquals("active_users", model.getAttribute("viewName"));
            assertEquals(viewDef, model.getAttribute("viewdef"));
        }
    }

    @Test
    void showViews_dropAction_success_refreshesSchemaMap() throws Exception
    {
        Result dropResult = new Result();
        dropResult.setMessage("SUCCESS: dropped");
        Map<String, Long> schemaMap = Map.of("View", 1L);
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("viewAction")).thenReturn("DROP");
            when(request.getParameter("viewName")).thenReturn("active_users");
            when(viewDAO.simpleviewCommand(SCHEMA, "active_users", "DROP", CONNECTION_ID)).thenReturn(dropResult);
            when(genericDAO.populateSchemaMap(SCHEMA, CONNECTION_ID)).thenReturn(schemaMap);
            when(viewDAO.retrieveViewList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            viewController.showViews(model, response, request, session);

            assertEquals(dropResult, model.getAttribute("result"));
            verify(session).setAttribute("schemaMap", schemaMap);
        }
    }

    @Test
    void performViewAction_search_addsSearchResults() throws Exception
    {
        View viewEntity = new View("def", SCHEMA, "active_users");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("searchpressed")).thenReturn("searchpressed");
            when(request.getParameter("search")).thenReturn("active");
            when(viewDAO.retrieveViewList(SCHEMA, "active", CONNECTION_ID)).thenReturn(List.of(viewEntity));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of(SCHEMA));

            String view = viewController.performViewAction(model, response, request, session);

            assertEquals("views", view);
            assertEquals("active", model.getAttribute("search"));
            assertEquals(List.of(viewEntity), model.getAttribute("views"));
        }
    }

    @Test
    void performViewAction_bulkCommands_addsArrayResult() throws Exception
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
            when(request.getParameterValues("selected_view[]"))
                    .thenReturn(new String[] {"active_users", "recent_orders"});
            when(request.getParameter("submit_mult")).thenReturn("Drop");
            when(viewDAO.simpleviewCommand(eq(SCHEMA), eq("active_users"), eq("Drop"), eq(CONNECTION_ID)))
                    .thenReturn(first);
            when(viewDAO.simpleviewCommand(eq(SCHEMA), eq("recent_orders"), eq("Drop"), eq(CONNECTION_ID)))
                    .thenReturn(second);
            when(viewDAO.retrieveViewList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = viewController.performViewAction(model, response, request, session);

            assertEquals("views", view);
            assertEquals(List.of(first, second), model.getAttribute("arrayresult"));
        }
    }

    @Test
    void performViewAction_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = viewController.performViewAction(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    private void stubConnectedSession(MockedStatic<Utils> utils)
    {
        utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(false);
        utils.when(() -> Utils.getConnectionSessionId(session)).thenReturn(CONNECTION_ID);
    }
}
