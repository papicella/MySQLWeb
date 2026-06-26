package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.constraints.Constraint;
import com.pas.tools.mysqlweb.dao.constraints.ConstraintDAO;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConstraintControllerTest
{
    private static final String CONNECTION_ID = "test-session";
    private static final String SCHEMA = "apples";

    @Mock
    private ConstraintDAO constraintDAO;

    @Mock
    private GenericDAO genericDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ConstraintController constraintController;

    @Test
    void showConstraints_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = constraintController.showConstraints(new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    @Test
    void showConstraints_returnsConstraintsViewWithSessionSchema() throws Exception
    {
        Constraint constraint = new Constraint("def", SCHEMA, "fk_users", "users", "FOREIGN KEY");
        List<String> schemas = List.of(SCHEMA, "mysql");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(constraintDAO.retrieveConstraintList(SCHEMA, null, CONNECTION_ID))
                    .thenReturn(List.of(constraint));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(schemas);

            String view = constraintController.showConstraints(model, response, request, session);

            assertEquals("constraints", view);
            assertEquals(List.of(constraint), model.getAttribute("constraints"));
            assertEquals(1, model.getAttribute("records"));
            assertEquals(1, model.getAttribute("estimatedrecords"));
            assertEquals(schemas, model.getAttribute("schemas"));
            assertEquals(SCHEMA, model.getAttribute("chosenSchema"));
        }
    }

    @Test
    void showConstraints_usesSelectedSchemaFromRequest() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(request.getParameter("selectedSchema")).thenReturn("information_schema");
            when(constraintDAO.retrieveConstraintList("information_schema", null, CONNECTION_ID))
                    .thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            Model model = new ExtendedModelMap();
            constraintController.showConstraints(model, response, request, session);

            assertEquals("information_schema", model.getAttribute("chosenSchema"));
            verify(constraintDAO).retrieveConstraintList("information_schema", null, CONNECTION_ID);
        }
    }

    @Test
    void showConstraints_dropAction_addsResultToModel() throws Exception
    {
        Result dropResult = new Result();
        dropResult.setMessage("SUCCESS: dropped");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("constraintAction")).thenReturn("DROP");
            when(request.getParameter("constraintName")).thenReturn("fk_users");
            when(request.getParameter("tableName")).thenReturn("users");
            when(request.getParameter("constraintType")).thenReturn("FOREIGN KEY");
            when(constraintDAO.simpleconstraintCommand(
                    SCHEMA, "fk_users", "users", "FOREIGN KEY", "DROP", CONNECTION_ID))
                    .thenReturn(dropResult);
            when(constraintDAO.retrieveConstraintList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = constraintController.showConstraints(model, response, request, session);

            assertEquals("constraints", view);
            assertEquals(dropResult, model.getAttribute("result"));
        }
    }

    @Test
    void showConstraints_nonDropAction_ignoresAction() throws Exception
    {
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("constraintAction")).thenReturn("ALTER");
            when(constraintDAO.retrieveConstraintList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            constraintController.showConstraints(model, response, request, session);

            assertNull(model.getAttribute("result"));
        }
    }

    @Test
    void performConstraintAction_search_addsSearchResults() throws Exception
    {
        Constraint constraint = new Constraint("def", SCHEMA, "fk_users", "users", "FOREIGN KEY");
        Model model = new ExtendedModelMap();

        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            stubConnectedSession(utils);
            when(session.getAttribute("schema")).thenReturn(SCHEMA);
            when(request.getParameter("searchpressed")).thenReturn("searchpressed");
            when(request.getParameter("search")).thenReturn("fk");
            when(constraintDAO.retrieveConstraintList(SCHEMA, "fk", CONNECTION_ID))
                    .thenReturn(List.of(constraint));
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of(SCHEMA));

            String view = constraintController.performConstraintAction(model, response, request, session);

            assertEquals("constraints", view);
            assertEquals("fk", model.getAttribute("search"));
            assertEquals(List.of(constraint), model.getAttribute("constraints"));
        }
    }

    @Test
    void performConstraintAction_bulkCommands_addsArrayResult() throws Exception
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
            when(request.getParameterValues("selected_constraint[]"))
                    .thenReturn(new String[] {"fk_users", "pk_orders"});
            when(request.getParameter("submit_mult")).thenReturn("Drop");
            when(constraintDAO.simpleconstraintCommand(
                    eq(SCHEMA), eq("fk_users"), eq("Drop"), eq(""), eq(""), eq(CONNECTION_ID)))
                    .thenReturn(first);
            when(constraintDAO.simpleconstraintCommand(
                    eq(SCHEMA), eq("pk_orders"), eq("Drop"), eq(""), eq(""), eq(CONNECTION_ID)))
                    .thenReturn(second);
            when(constraintDAO.retrieveConstraintList(SCHEMA, null, CONNECTION_ID)).thenReturn(List.of());
            when(genericDAO.allSchemas(CONNECTION_ID)).thenReturn(List.of());

            String view = constraintController.performConstraintAction(model, response, request, session);

            assertEquals("constraints", view);
            assertEquals(List.of(first, second), model.getAttribute("arrayresult"));
        }
    }

    @Test
    void performConstraintAction_noConnection_returnsNull() throws Exception
    {
        try (MockedStatic<Utils> utils = mockStatic(Utils.class))
        {
            utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(true);

            String view = constraintController.performConstraintAction(
                    new ExtendedModelMap(), response, request, session);

            assertNull(view);
        }
    }

    private void stubConnectedSession(MockedStatic<Utils> utils)
    {
        utils.when(() -> Utils.verifyConnection(response, session)).thenReturn(false);
        utils.when(() -> Utils.getConnectionSessionId(session)).thenReturn(CONNECTION_ID);
    }
}
