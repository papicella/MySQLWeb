package com.pas.tools.mysqlweb.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Configuration
@PropertySource("classpath:preferences.properties")
public class UserPref
{
    @Value("${recordsToDisplay}")
    private int recordsToDisplay;

    @Value("${maxRecordsinSQLQueryWindow}")
    private int maxRecordsinSQLQueryWindow;

    @Value("${autoCommit}")
    private String autoCommit;

    @Value("${historySize}")
    private int historySize;

    @Value("${sampleDataSize}")
    private int sampleDataSize;
}
