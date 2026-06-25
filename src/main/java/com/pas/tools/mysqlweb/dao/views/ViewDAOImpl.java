package com.pas.tools.mysqlweb.dao.views;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.service.SessionJdbcSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class ViewDAOImpl implements ViewDAO
{
    private final SessionJdbcSupport jdbcSupport;
    private final GenericDAO genericDAO;

    public ViewDAOImpl(SessionJdbcSupport jdbcSupport, GenericDAO genericDAO)
    {
        this.jdbcSupport = jdbcSupport;
        this.genericDAO = genericDAO;
    }

    @Override
    public List<View> retrieveViewList(String schema, String search, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            String srch = search == null ? "%" : "%" + search + "%";
            return jdbcTemplate.query(Constants.USER_VIEWS, new Object[]{schema, srch}, new ViewMapper());
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all views with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public Result simpleviewCommand(String schemaName, String viewName, String type, String sessionId)
            throws PivotalMySQLWebException
    {
        String command = null;

        if (type != null && type.equalsIgnoreCase("DROP"))
        {
            if (schemaName.equalsIgnoreCase("public"))
            {
                command = String.format(Constants.DROP_VIEW_PUBLIC, viewName);
            }
            else
            {
                command = String.format(Constants.DROP_VIEW, schemaName, viewName);
            }
        }

        return genericDAO.runCommand(command, sessionId);
    }

    @Override
    public String getViewDefinition(String schemaName, String viewName, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            return jdbcTemplate.queryForObject(
                    Constants.USER_VIEW_DEF,
                    new Object[]{schemaName, viewName},
                    String.class);
        }
        catch (Exception ex)
        {
            log.info("Error retrieving view definition for view = " + viewName);
            throw new PivotalMySQLWebException(ex);
        }
    }
}
