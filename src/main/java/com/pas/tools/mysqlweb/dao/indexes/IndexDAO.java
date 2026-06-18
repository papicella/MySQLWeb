package com.pas.tools.mysqlweb.dao.indexes;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;

public interface IndexDAO
{
    public void setDataSource(javax.sql.DataSource ds);
    public List<Index> retrieveIndexList(String schema, String search, String userKey) throws PivotalMySQLWebException;
    public Result simpleindexCommand (String schemaName, String indexName, String type, String tableName, String userKey) throws PivotalMySQLWebException;
    public WebResult getIndexDetails(String schema, String tableName, String indexName, String userKey) throws PivotalMySQLWebException;

}
