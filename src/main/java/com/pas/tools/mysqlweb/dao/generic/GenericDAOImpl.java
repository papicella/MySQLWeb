package com.pas.tools.mysqlweb.dao.generic;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pas.tools.mysqlweb.beans.CommandResult;
import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.service.SessionJdbcSupport;
import com.pas.tools.mysqlweb.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class GenericDAOImpl implements GenericDAO
{
    private final SessionJdbcSupport jdbcSupport;

    public GenericDAOImpl(SessionJdbcSupport jdbcSupport)
    {
        this.jdbcSupport = jdbcSupport;
    }

    @Override
    public WebResult runGenericQuery(String sql, Object[] args, String sessionId, int maxRows)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId, maxRows);
            List<Map<String, Object>> resultList;

            if (args == null)
            {
                resultList = jdbcTemplate.queryForList(sql);
            }
            else
            {
                resultList = jdbcTemplate.queryForList(sql, args);
            }

            String[] columnNames = null;
            if (resultList.size() > 0)
            {
                Set<String> keySet = resultList.get(0).keySet();
                columnNames = keySet.toArray(new String[keySet.size()]);
            }

            return new WebResult(columnNames, resultList);
        }
        catch (Exception ex)
        {
            log.info("Error running generic query");
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public CommandResult runStatement(String sql, String elapsedTime, String ddl, String sessionId)
            throws PivotalMySQLWebException
    {
        CommandResult res = new CommandResult();

        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            res.setCommand(sql);

            long start = System.currentTimeMillis();

            if (ddl.equals("Y"))
            {
                jdbcTemplate.execute(sql);
                res.setRows(-1);
            }
            else
            {
                int rowsAffected = jdbcTemplate.update(sql);
                res.setRows(rowsAffected);
            }

            long end = System.currentTimeMillis();
            DecimalFormat df = new DecimalFormat("#.##");
            res.setMessage("SUCCESS");

            if (elapsedTime.equals("Y"))
            {
                res.setElapsedTime("" + df.format(end - start));
            }
        }
        catch (Exception ex)
        {
            log.info("Error running generic DML: " + ex.getMessage());
            res.setMessage("ERROR: " + ex.getMessage());
            res.setRows(-1);
        }

        return res;
    }

    @Override
    public Map<String, Long> populateSchemaMap(String schema, String sessionId) throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    Constants.SCHEMA_MAP_QUERY,
                    new Object[]{schema, schema, schema, schema});

            Map<String, Long> schemaMap = Utils.getSchemaMap();

            for (Map result : rows)
            {
                schemaMap.put((String) result.get("object_type"), (Long) result.get("count(*)"));
            }

            return schemaMap;
        }
        catch (Exception ex)
        {
            log.info("Error populating schema map");
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public List<String> allSchemas(String sessionId) throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            return jdbcTemplate.queryForList(Constants.ALL_SCHEMAS, String.class);
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all schemas");
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public Result runCommand(String sql, String sessionId)
    {
        Result res = new Result();

        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            res.setCommand(sql);
            jdbcTemplate.execute(sql);
            res.setMessage("SUCCESS");
        }
        catch (Exception ex)
        {
            res.setMessage(ex.getMessage());
        }

        return res;
    }
}
