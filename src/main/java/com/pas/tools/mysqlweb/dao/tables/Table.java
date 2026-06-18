package com.pas.tools.mysqlweb.dao.tables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Table
{
    public String catalog;
    public String schemaName;
    public String tableName;
    public String tableType;

}
