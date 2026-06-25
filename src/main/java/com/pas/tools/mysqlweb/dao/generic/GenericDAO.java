package com.pas.tools.mysqlweb.dao.generic;

import com.pas.tools.mysqlweb.beans.CommandResult;
import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;
import java.util.Map;

public interface GenericDAO
{
    WebResult runGenericQuery (String sql, Object[] args, String sessionId, int maxRows) throws PivotalMySQLWebException;
    CommandResult runStatement(String sql, String elapsedTime, String ddl, String sessionId) throws PivotalMySQLWebException;

    Map<String, Long> populateSchemaMap(String schema, String sessionId) throws PivotalMySQLWebException;

    List<String> allSchemas (String sessionId) throws PivotalMySQLWebException;

    Result runCommand (String sql, String sessionId);
}
