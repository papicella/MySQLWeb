package com.pas.tools.mysqlweb.dao.tables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableMapper implements RowMapper<Table>
{
    @Override
    public Table mapRow(ResultSet resultSet, int i) throws SQLException {
        Table table = new Table();
        table.setCatalog(resultSet.getString("Catalog"));
        table.setSchemaName(resultSet.getString("Schema"));
        table.setTableName(resultSet.getString("Name"));
        table.setTableType(resultSet.getString("Type"));
        return table;
    }
}
