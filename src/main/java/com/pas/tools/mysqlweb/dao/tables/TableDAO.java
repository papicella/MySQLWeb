package com.pas.tools.mysqlweb.dao.tables;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;

public interface TableDAO
{
    public List<Table> retrieveTableList(String schema, String search, String sessionId) throws PivotalMySQLWebException;

    public Result simpletableCommand (String schemaName, String tableName, String type, String sessionId) throws PivotalMySQLWebException;

    public WebResult getTableStructure (String schema, String tableName, String sessionId) throws PivotalMySQLWebException;

    public WebResult getTableDetails (String schema, String tableName, String sessionId) throws PivotalMySQLWebException;

    public String runShowQuery (String schema, String tableName, String sessionId) throws PivotalMySQLWebException;

    public WebResult showIndexes(String schema, String tableName, String sessionId) throws PivotalMySQLWebException;
}
