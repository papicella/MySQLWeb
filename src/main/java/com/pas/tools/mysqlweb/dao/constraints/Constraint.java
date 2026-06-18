package com.pas.tools.mysqlweb.dao.constraints;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Constraint
{
    public String catalog;
    public String schemaName;
    public String constraintName;
    public String tableName;
    public String constraintType;

}
