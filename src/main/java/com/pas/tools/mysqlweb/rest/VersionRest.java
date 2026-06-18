package com.pas.tools.mysqlweb.rest;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionRest
{
    @GetMapping(value = "/version")
    public String sampleOutput() {
        return "Pivotal MySQLWeb version 1.2";
    }
}
