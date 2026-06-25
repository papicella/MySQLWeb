package com.pas.tools.mysqlweb.dao.tables;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.service.SessionJdbcSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class TableDAOImpl implements TableDAO
{
    private final SessionJdbcSupport jdbcSupport;
    private final GenericDAO genericDAO;

    public TableDAOImpl(SessionJdbcSupport jdbcSupport, GenericDAO genericDAO)
    {
        this.jdbcSupport = jdbcSupport;
        this.genericDAO = genericDAO;
    }

    @Override
    public List<Table> retrieveTableList(String schema, String search, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            String srch = search == null ? "%" : "%" + search + "%";
            return jdbcTemplate.query(Constants.USER_TABLES, new Object[]{schema, srch}, new TableMapper());
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all tables with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public Result simpletableCommand(String schemaName, String tableName, String type, String sessionId)
            throws PivotalMySQLWebException
    {
        String command = null;

        if (type != null)
        {
            if (type.equalsIgnoreCase("DROP"))
            {
                if (schemaName.equalsIgnoreCase("public"))
                {
                    command = String.format(Constants.DROP_TABLE_PUBLIC, tableName);
                }
                else
                {
                    command = String.format(Constants.DROP_TABLE, schemaName, tableName);
                }
            }
            else if (type.equalsIgnoreCase("EMPTY"))
            {
                if (schemaName.equalsIgnoreCase("public"))
                {
                    command = String.format(Constants.TRUNCATE_TABLE_PUBLIC, tableName);
                }
                else
                {
                    command = String.format(Constants.TRUNCATE_TABLE, schemaName, tableName);
                }
            }
        }

        return genericDAO.runCommand(command, sessionId);
    }

    @Override
    public WebResult getTableStructure(String schema, String tableName, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            return genericDAO.runGenericQuery(
                    String.format(Constants.TABLE_STRUCTURE, schema, tableName),
                    null,
                    sessionId,
                    -1);
        }
        catch (Exception ex)
        {
            log.info("Error retrieving table structure for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public WebResult getTableDetails(String schema, String tableName, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            return genericDAO.runGenericQuery(
                    Constants.TABLE_DETAILS,
                    new Object[]{schema, tableName},
                    sessionId,
                    -1);
        }
        catch (Exception ex)
        {
            log.info("Error retrieving table details for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public String runShowQuery(String schema, String tableName, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(
                    String.format(Constants.CREATE_TABLE_QUERY, schema, tableName));

            String queryData = (String) resultList.get(0).get("Create Table");
            if (queryData == null)
            {
                queryData = (String) resultList.get(0).get("Create View");
            }
            return queryData;
        }
        catch (Exception ex)
        {
            log.info("Error running runShowQuery table details for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public WebResult showIndexes(String schema, String tableName, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            return genericDAO.runGenericQuery(
                    String.format(Constants.SHOW_INDEXES, schema, tableName),
                    null,
                    sessionId,
                    -1);
        }
        catch (Exception ex)
        {
            log.info("Error running index query for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }
    }
}
