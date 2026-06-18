package com.pas.tools.mysqlweb.dao.constraints;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ConstraintMapper implements RowMapper<Constraint> {
    @Override
    public Constraint mapRow(ResultSet resultSet, int i) throws SQLException {
        Constraint constraint = new Constraint();
        constraint.setCatalog(resultSet.getString("CONSTRAINT_CATALOG"));
        constraint.setSchemaName(resultSet.getString("CONSTRAINT_SCHEMA"));
        constraint.setConstraintName(resultSet.getString("CONSTRAINT_NAME"));
        constraint.setTableName(resultSet.getString("TABLE_NAME"));
        constraint.setConstraintType(resultSet.getString("CONSTRAINT_TYPE"));
        return constraint;
    }
}
