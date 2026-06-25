package com.pas.tools.mysqlweb.dao.indexes;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.service.SessionJdbcSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class IndexDAOImpl implements IndexDAO
{
    private final SessionJdbcSupport jdbcSupport;
    private final GenericDAO genericDAO;

    public IndexDAOImpl(SessionJdbcSupport jdbcSupport, GenericDAO genericDAO)
    {
        this.jdbcSupport = jdbcSupport;
        this.genericDAO = genericDAO;
    }

    @Override
    public List<Index> retrieveIndexList(String schema, String search, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            String srch = search == null ? "%" : "%" + search + "%";
            return jdbcTemplate.query(Constants.USER_INDEXES, new Object[]{schema, srch}, new IndexMapper());
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all indexes with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public Result simpleindexCommand(
            String schemaName,
            String indexName,
            String type,
            String tableName,
            String sessionId) throws PivotalMySQLWebException
    {
        String command = null;

        if (type != null && type.equalsIgnoreCase("DROP"))
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

        return genericDAO.runCommand(command, sessionId);
    }

    @Override
    public WebResult getIndexDetails(String schema, String tableName, String indexName, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            return genericDAO.runGenericQuery(
                    Constants.INDEX_DETAILS,
                    new Object[]{schema, tableName, indexName},
                    sessionId,
                    -1);
        }
        catch (Exception ex)
        {
            log.info("Error retrieving index details for index " + indexName);
            throw new PivotalMySQLWebException(ex);
        }
    }
}
