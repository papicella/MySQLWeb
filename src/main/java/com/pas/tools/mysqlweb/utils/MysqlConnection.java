package com.pas.tools.mysqlweb.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MysqlConnection
{
    private String url;
    private String connectedAt;
    private String schema;

}
