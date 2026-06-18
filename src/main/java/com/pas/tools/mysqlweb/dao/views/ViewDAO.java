package com.pas.tools.mysqlweb.dao.views;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;

public interface ViewDAO
{
    public void setDataSource(javax.sql.DataSource ds);

    public List<View> retrieveViewList(String schema, String search, String userKey) throws PivotalMySQLWebException;

    public Result simpleviewCommand (String schemaName, String viewName, String type, String userKey) throws PivotalMySQLWebException;

    public String getViewDefinition(String schemaName, String viewName, String userKey) throws PivotalMySQLWebException;
}
