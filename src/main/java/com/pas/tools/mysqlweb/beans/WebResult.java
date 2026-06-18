package com.pas.tools.mysqlweb.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebResult
{
    private String[] columnNames;
    private List<Map<String, Object>> rows;

}
