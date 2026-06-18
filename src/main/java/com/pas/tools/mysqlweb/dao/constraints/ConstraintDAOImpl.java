package com.pas.tools.mysqlweb.dao.constraints;

import java.util.List;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.utils.AdminUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class ConstraintDAOImpl implements ConstraintDAO
{
    
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(javax.sql.DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public List<Constraint> retrieveConstraintList(String schema, String search, String userKey) throws PivotalMySQLWebException
    {
        List<Constraint>       constraints = null;
        String            srch = null;

        javax.sql.DataSource dataSource = null;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            if (search == null)
                srch = "%";
            else
                srch = "%" + search + "%";

            constraints = jdbcTemplate.query(Constants.USER_CONSTRAINTS, new Object[]{schema, srch}, new ConstraintMapper());
        }
        catch (Exception ex)
        {
            log.info("Error retrieving all constraints with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }


        return constraints;
    }

    @Override
    public Result simpleconstraintCommand(String schemaName, String constraintName, String tableName, String contraintType, String type, String userKey) throws PivotalMySQLWebException
    {
        String            command = null;
        Result            res     = new Result();

        if (type != null)
        {
            if (type.equalsIgnoreCase("DROP")) {
                if (contraintType.equalsIgnoreCase("UNIQUE")) {
                    command = String.format(Constants.DROP_CONSTRAINT_UNIQUE, schemaName, tableName, constraintName);
                } else if (contraintType.equalsIgnoreCase("FOREIGN KEY")) {
                    command = String.format(Constants.DROP_CONSTRAINT_FK, schemaName, tableName, constraintName);
                } else if (contraintType.equalsIgnoreCase("PRIMARY KEY")) {
                    command = String.format(Constants.DROP_CONSTRAINT_PRIMARY_KEY, schemaName, tableName);
                } else {
                    // not really expecting anything here
                }
            }
        }

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
        res = genericDAO.runCommand(command, userKey);

        return res;
    }

}
