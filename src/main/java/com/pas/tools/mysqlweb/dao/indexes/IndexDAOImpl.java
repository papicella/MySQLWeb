package com.pas.tools.mysqlweb.dao.indexes;

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

@Slf4j
@Repository
public class IndexDAOImpl implements IndexDAO
{
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(javax.sql.DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public List<Index> retrieveIndexList(String schema, String search, String userKey) throws PivotalMySQLWebException
    {
        List<Index>       indexes = null;
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

            indexes = jdbcTemplate.query(Constants.USER_INDEXES, new Object[]{schema, srch}, new IndexMapper());
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all indexes with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }

        return indexes;
    }

    @Override
    public Result simpleindexCommand(String schemaName, String indexName, String type, String tableName, String userKey) throws PivotalMySQLWebException
    {
        String            command = null;
        Result            res     = new Result();
        SingleConnectionDataSource dataSource = null;

        if (type != null)
        {
            if (type.equalsIgnoreCase("DROP"))
            {
                if (indexName.equalsIgnoreCase("PRIMARY"))
                {
                    command = String.format(Constants.DROP_INDEX_PRIMARY, schemaName, tableName);
                }
                else
                {
                    command = String.format(Constants.DROP_INDEX, schemaName, tableName, indexName);
                }
            }

        }

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
        res = genericDAO.runCommand(command, userKey);

        return res;
    }

    @Override
    public WebResult getIndexDetails(String schema, String tableName, String indexName, String userKey) throws PivotalMySQLWebException
    {
        javax.sql.DataSource dataSource;
        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
        WebResult webResult;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            webResult = genericDAO.runGenericQuery
                    (Constants.INDEX_DETAILS, new Object[]{schema, tableName, indexName}, userKey, -1);

        }
        catch (Exception ex) {
            log.info("Error retrieving index details for index " + indexName);
            throw new PivotalMySQLWebException(ex);
        }

        return webResult;
    }
}
