package com.pas.tools.mysqlweb.dao.indexes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Index
{
    public String catalog;
    public String schemaName;
    public String tableName;
    public String indexName;

}
