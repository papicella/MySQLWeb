package com.pas.tools.mysqlweb.dao.constraints;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;

import java.util.List;

public interface ConstraintDAO
{
    void setDataSource(javax.sql.DataSource ds);
    public List<Constraint> retrieveConstraintList(String schema, String search, String userKey) throws PivotalMySQLWebException;
    public Result simpleconstraintCommand (String schemaName, String constraintName, String tableName, String contraintType, String type, String userKey) throws PivotalMySQLWebException;

}
