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
import com.pas.tools.mysqlweb.utils.AdminUtil;
import com.pas.tools.mysqlweb.utils.Utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class GenericDAOImpl implements GenericDAO
{
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(javax.sql.DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public WebResult runGenericQuery (String sql, Object[] args, String userKey, int maxRows) throws PivotalMySQLWebException
    {
        javax.sql.DataSource dataSource = null;
        WebResult webResult = null;
        List<Map<String, Object>> resultList = null;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            if (maxRows != -1)
            {
                jdbcTemplate.setMaxRows(maxRows);
            }

            if (args == null) {
                resultList = jdbcTemplate.queryForList(sql);
            }
            else {
                resultList = jdbcTemplate.queryForList(sql, args);
            }

            String[] columnNames = null;
            // Get Column Names
            if (resultList.size() > 0) {
                Set<String> keySet = resultList.get(0).keySet();
                columnNames = keySet.toArray(new String[keySet.size()]);
            }

            webResult = new WebResult(columnNames, resultList);

        }
        catch (Exception ex)
        {
            log.info("Error running generic query");
            throw new PivotalMySQLWebException(ex);
        }

        return webResult;
    }

    @Override
    public CommandResult runStatement(String sql, String elapsedTime, String ddl, String userKey) throws PivotalMySQLWebException {
        javax.sql.DataSource dataSource;
        CommandResult res = new CommandResult();

        try {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);
            res.setCommand(sql);

            long start = System.currentTimeMillis();

            if (ddl.equals("Y")) {
                jdbcTemplate.execute(sql);
                res.setRows(-1);
            }
            else {
                int rowsAffected  = jdbcTemplate.update(sql);
                res.setRows(rowsAffected);
            }

            long end = System.currentTimeMillis();

            double timeTaken = new Double(end - start).doubleValue();
            DecimalFormat df = new DecimalFormat("#.##");

            // no need to commit it's auto commit already as it's DDL statement.
            res.setMessage("SUCCESS");

            if (elapsedTime.equals("Y"))
            {
                res.setElapsedTime("" + df.format(timeTaken));
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
    public Map<String, Long> populateSchemaMap(String schema, String userKey) throws PivotalMySQLWebException {
        javax.sql.DataSource dataSource;
        CommandResult res = new CommandResult();
        Map<String, Long> schemaMap = new HashMap<String, Long>();
        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList
                    (Constants.SCHEMA_MAP_QUERY, new Object[]{schema, schema, schema, schema});

            schemaMap = Utils.getSchemaMap();

            for(Map result: rows)
            {
                schemaMap.put((String)result.get("object_type"), (Long)result.get("count(*)"));
            }

        }
        catch (Exception ex)
        {
            log.info("Error populating schema map");
            throw new PivotalMySQLWebException(ex);
        }

        return schemaMap;
    }

    @Override
    public List<String> allSchemas(String userKey) throws PivotalMySQLWebException
    {
        javax.sql.DataSource dataSource;
        List<String> schemas;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            schemas = jdbcTemplate.queryForList(Constants.ALL_SCHEMAS,String.class);

        }
        catch (Exception ex)
        {
            log.info("Error retrieving all schemas");
            throw new PivotalMySQLWebException(ex);
        }

        return schemas;
    }

    @Override
    public Result runCommand (String sql, String userKey)
    {
        Result res = new Result();

        javax.sql.DataSource dataSource;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);
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
