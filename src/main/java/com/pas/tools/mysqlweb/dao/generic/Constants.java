package com.pas.tools.mysqlweb.dao.generic;

public interface Constants
{
    String DATABASE_LIST =
            "SELECT SCHEMA_NAME 'database', default_character_set_name 'charset', DEFAULT_COLLATION_NAME 'collation' FROM information_schema.SCHEMATA";

    String SCHEMA_MAP_QUERY = com.pas.tools.mysqlweb.dao.tables.Constants.USER_TABLES_COUNT +
            "union " +
            com.pas.tools.mysqlweb.dao.views.Constants.USER_VIEWS_COUNT +
            "union " +
            com.pas.tools.mysqlweb.dao.indexes.Constants.USER_INDEXES_COUNT +
            "union " +
            com.pas.tools.mysqlweb.dao.constraints.Constants.USER_CONSTRAINTS_COUNT;

    String ALL_SCHEMAS = "select schema_name from information_schema.schemata order by 1";

}
