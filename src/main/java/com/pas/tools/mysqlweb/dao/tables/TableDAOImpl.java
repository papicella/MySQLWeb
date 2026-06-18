package com.pas.tools.mysqlweb.dao.tables;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.utils.AdminUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Slf4j
@Repository
public class TableDAOImpl implements TableDAO {

    private JdbcTemplate jdbcTemplate;

    public void setDataSource(javax.sql.DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public List<Table> retrieveTableList(String schema, String search, String userKey) throws PivotalMySQLWebException
    {
        List<Table>       tbls = null;
        String            srch = null;
        javax.sql.DataSource dataSource = null;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            if (search == null)
                srch = "%";
            else
                srch = "%" + search + "%";

            tbls = jdbcTemplate.query(Constants.USER_TABLES, new Object[]{schema, srch}, new TableMapper());

        }
        catch (Exception ex)
        {
            log.info("Error retrieving all tables with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }

        return tbls;
    }

    @Override
    public Result simpletableCommand(String schemaName, String tableName, String type, String userKey) throws PivotalMySQLWebException
    {
        String            command = null;
        Result            res     = new Result();
        SingleConnectionDataSource dataSource = null;

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

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
        res = genericDAO.runCommand(command, userKey);

        return res;
    }

    @Override
    public WebResult getTableStructure(String schema, String tableName, String userKey) throws PivotalMySQLWebException
    {
        WebResult webResult;
        javax.sql.DataSource dataSource;
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            webResult = genericDAO.runGenericQuery
                    (String.format(Constants.TABLE_STRUCTURE, schema, tableName), null, userKey, -1);

        }
        catch (Exception ex)
        {
            log.info("Error retrieving table structure for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }

        return webResult;
    }

    @Override
    public WebResult getTableDetails (String schema, String tableName, String userKey) throws PivotalMySQLWebException
    {
        javax.sql.DataSource dataSource = null;
        WebResult webResult = null;
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            webResult = genericDAO.runGenericQuery
                    (Constants.TABLE_DETAILS, new Object[]{schema, tableName}, userKey, -1);

        }
        catch (Exception ex)
        {
            log.info("Error retrieving table details for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }

        return webResult;
    }

    @Override
    public String runShowQuery (String schema, String tableName, String userKey) throws PivotalMySQLWebException
    {
        String queryData = null;
        javax.sql.DataSource dataSource = null;
        List<Map<String, Object>> resultList = null;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            resultList = jdbcTemplate.queryForList
                    (String.format(Constants.CREATE_TABLE_QUERY, schema, tableName));

            queryData = (String) resultList.get(0).get("Create Table");

            if (queryData == null)
            {
                queryData = (String) resultList.get(0).get("Create View");
            }
        }
        catch (Exception ex)
        {
            log.info("Error running runShowQuery table details for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }

        return queryData;
    }

    public WebResult showIndexes(String schema, String tableName, String userKey) throws PivotalMySQLWebException
    {
        javax.sql.DataSource dataSource = null;
        WebResult webResult = null;
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            webResult = genericDAO.runGenericQuery
                    (String.format(Constants.SHOW_INDEXES, schema, tableName), null, userKey, -1);

        }
        catch (Exception ex)
        {
            log.info("Error running index query for table  " + tableName);
            throw new PivotalMySQLWebException(ex);
        }

        return webResult;
    }

}
