package com.pas.tools.mysqlweb.dao.generic;

import com.pas.tools.mysqlweb.beans.CommandResult;
import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.beans.WebResult;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;
import java.util.Map;

public interface GenericDAO
{
    void setDataSource(javax.sql.DataSource ds);
    WebResult runGenericQuery (String sql, Object[] args, String userKey, int maxRows) throws PivotalMySQLWebException;
    CommandResult runStatement(String sql, String elapsedTime, String ddl, String userKey) throws PivotalMySQLWebException;

    Map<String, Long> populateSchemaMap(String schema, String userKey) throws PivotalMySQLWebException;

    List<String> allSchemas (String userKey) throws PivotalMySQLWebException;

    Result runCommand (String sql, String userKey);
}
