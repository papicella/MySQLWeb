package com.pas.tools.mysqlweb.dao.constraints;

import java.util.List;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.service.SessionJdbcSupport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ConstraintDAOImpl implements ConstraintDAO
{
    private final SessionJdbcSupport jdbcSupport;
    private final GenericDAO genericDAO;

    public ConstraintDAOImpl(SessionJdbcSupport jdbcSupport, GenericDAO genericDAO)
    {
        this.jdbcSupport = jdbcSupport;
        this.genericDAO = genericDAO;
    }

    @Override
    public List<Constraint> retrieveConstraintList(String schema, String search, String sessionId)
            throws PivotalMySQLWebException
    {
        try
        {
            JdbcTemplate jdbcTemplate = jdbcSupport.getJdbcTemplate(sessionId);
            String srch = search == null ? "%" : "%" + search + "%";
            return jdbcTemplate.query(
                    Constants.USER_CONSTRAINTS,
                    new Object[]{schema, srch},
                    new ConstraintMapper());
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all constraints with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }
    }

    @Override
    public Result simpleconstraintCommand(
            String schemaName,
            String constraintName,
            String tableName,
            String contraintType,
            String type,
            String sessionId) throws PivotalMySQLWebException
    {
        String command = null;

        if (type != null && type.equalsIgnoreCase("DROP"))
        {
            if (contraintType.equalsIgnoreCase("UNIQUE"))
            {
                command = String.format(Constants.DROP_CONSTRAINT_UNIQUE, schemaName, tableName, constraintName);
            }
            else if (contraintType.equalsIgnoreCase("FOREIGN KEY"))
            {
                command = String.format(Constants.DROP_CONSTRAINT_FK, schemaName, tableName, constraintName);
            }
            else if (contraintType.equalsIgnoreCase("PRIMARY KEY"))
            {
                command = String.format(Constants.DROP_CONSTRAINT_PRIMARY_KEY, schemaName, tableName);
            }
        }

        return genericDAO.runCommand(command, sessionId);
    }
}
