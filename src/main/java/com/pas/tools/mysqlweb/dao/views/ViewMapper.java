package com.pas.tools.mysqlweb.dao.views;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewMapper implements RowMapper<View>
{
    @Override
    public View mapRow(ResultSet resultSet, int i) throws SQLException
    {
        View view = new View();
        view.setCatalog(resultSet.getString("table_catalog"));
        view.setSchemaName(resultSet.getString("table_schema"));
        view.setViewName(resultSet.getString("table_name"));
        return view;
    }
}
