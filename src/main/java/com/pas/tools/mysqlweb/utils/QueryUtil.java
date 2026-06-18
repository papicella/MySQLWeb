package com.pas.tools.mysqlweb.utils;

import com.opencsv.CSVWriter;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QueryUtil
{

    static public String runQueryForCSV (Connection conn, String query) throws SQLException, IOException
    {
        Statement stmt  = null;
        ResultSet rset  = null;
        StringWriter sw = new StringWriter();

        try
        {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(query);

            CSVWriter csvWriter = new CSVWriter(sw, ',', '"', '\"');
            csvWriter.writeAll(rset, true);
            csvWriter.flush();

        }
        finally
        {
            JDBCUtil.close(stmt);
            JDBCUtil.close(rset);
        }

        return sw.toString();
    }

    static public String runQueryForJSON (Connection conn, String query) throws SQLException, IOException
    {
        Statement stmt  = null;
        ResultSet rset  = null;
        String jsonResult = null;

        try
        {
            stmt = conn.createStatement();
            rset = stmt.executeQuery(query);

            jsonResult = DSL.using(conn).fetch(rset).formatJSON();

        }
        finally
        {
            JDBCUtil.close(stmt);
            JDBCUtil.close(rset);
        }

        return jsonResult;
    }

}
