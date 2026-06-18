package com.pas.tools.mysqlweb.dao.indexes;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IndexMapper implements RowMapper<Index>
{
    @Override
    public Index mapRow(ResultSet resultSet, int i) throws SQLException {
        Index index = new Index();
        index.setCatalog(resultSet.getString("Catalog"));
        index.setSchemaName(resultSet.getString("Schema"));
        index.setTableName(resultSet.getString("Name"));
        index.setIndexName(resultSet.getString("Index Name"));
        return index;
    }
}
