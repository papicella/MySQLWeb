package com.pas.tools.mysqlweb.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString (exclude = "password")
public class Login
{
    private String username;
    private String password;
    private String url;
    private String schema;

}
