package com.pas.tools.mysqlweb.dao.views;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;

public interface ViewDAO
{
    public List<View> retrieveViewList(String schema, String search, String sessionId) throws PivotalMySQLWebException;

    public Result simpleviewCommand (String schemaName, String viewName, String type, String sessionId) throws PivotalMySQLWebException;

    public String getViewDefinition(String schemaName, String viewName, String sessionId) throws PivotalMySQLWebException;
}
