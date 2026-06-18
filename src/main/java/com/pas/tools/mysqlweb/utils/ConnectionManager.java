package com.pas.tools.mysqlweb.utils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@Slf4j
public class ConnectionManager
{

    private Map<String,MysqlConnection> conList = new HashMap<String,MysqlConnection>();
    private Map<String,SingleConnectionDataSource> dsList = new HashMap<String,SingleConnectionDataSource>();
    private static ConnectionManager instance = null;

    static
    {
        instance = new ConnectionManager();
    }

    private ConnectionManager()
    {
        // Exists only to defeat instantiation.
    }

    public static ConnectionManager getInstance() throws Exception
    {
        return instance;
    }

    public void addConnection (MysqlConnection conn, String key)
    {
        conList.put(key, conn);
        log.info("Connection added with key " + key);
    }

    public void addDataSourceConnection (SingleConnectionDataSource dataSource, String key)
    {
        dsList.put(key, dataSource);
        log.info("SingleConnectionDataSource added with key " + key);
    }

    public javax.sql.DataSource getDataSource (String key)
    {
        return dsList.get(key);
    }

    public void removeDataSource(String key) throws SQLException
    {
        if (dsList.containsKey(key))
        {
            SingleConnectionDataSource ds = dsList.get(key);
            ds.destroy();
            dsList.remove(key);
            conList.remove(key);
            log.info("SingleConnectionDataSource removed with key " + key);
        }
        else
        {
            log.info("No SingleConnectionDataSource with key " + key + " exists");
        }
    }

    public Map <String,MysqlConnection> getConnectionMap()
    {
        return conList;
    }

    public int getConnectionListSize ()
    {
        return conList.size();
    }

}
