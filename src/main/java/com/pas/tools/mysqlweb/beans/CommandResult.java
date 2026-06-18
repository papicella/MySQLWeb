package com.pas.tools.mysqlweb.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommandResult
{
    private String command;
    private String message;
    private int rows;
    private String elapsedTime;

}

