package com.pas.tools.mysqlweb.dao.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class View
{
    public String catalog;
    public String schemaName;
    public String viewName;

}
