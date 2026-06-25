package com.pas.tools.mysqlweb.dao.constraints;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;

public interface ConstraintDAO
{
    public List<Constraint> retrieveConstraintList(String schema, String search, String sessionId) throws PivotalMySQLWebException;
    public Result simpleconstraintCommand (String schemaName, String constraintName, String tableName, String contraintType, String type, String sessionId) throws PivotalMySQLWebException;

}
