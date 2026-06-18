package com.pas.tools.mysqlweb.utils;

import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCUtil
{

    public static void close(ResultSet resultSet)
    {
        try
        {
            if (resultSet != null)
                resultSet.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void close(Statement statement)
    {
        try
        {
            if (statement != null)
                statement.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
