package com.pas.tools.mysqlweb.dao.views;

import com.pas.tools.mysqlweb.beans.Result;
import com.pas.tools.mysqlweb.dao.PivotalMySQLWebDAOFactory;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.main.PivotalMySQLWebException;
import com.pas.tools.mysqlweb.utils.AdminUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
public class ViewDAOImpl implements ViewDAO
{
    private JdbcTemplate jdbcTemplate;

    public void setDataSource(javax.sql.DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    @Override
    public List<View> retrieveViewList(String schema, String search, String userKey) throws PivotalMySQLWebException
    {
        List<View> views;
        String srch;

        javax.sql.DataSource dataSource = null;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            if (search == null)
                srch = "%";
            else
                srch = "%" + search + "%";

            views = jdbcTemplate.query(Constants.USER_VIEWS, new Object[]{schema, srch}, new ViewMapper());

        }
        catch (Exception ex)
        {
            log.info("Error retrieving all views with search string = " + search);
            throw new PivotalMySQLWebException(ex);
        }

        return views;
    }

    @Override
    public Result simpleviewCommand(String schemaName, String viewName, String type, String userKey) throws PivotalMySQLWebException
    {
        String            command = null;
        Result            res     = new Result();
        SingleConnectionDataSource dataSource = null;

        if (type != null)
        {
            if (type.equalsIgnoreCase("DROP"))
            {
                if (schemaName.equalsIgnoreCase("public"))
                {
                    command = String.format(Constants.DROP_VIEW_PUBLIC, viewName);
                }
                else
                {
                    command = String.format(Constants.DROP_VIEW, schemaName, viewName);
                }
            }
        }

        GenericDAO genericDAO = PivotalMySQLWebDAOFactory.getGenericDAO();
        res = genericDAO.runCommand(command, userKey);

        return res;
    }

    @Override
    public String getViewDefinition(String schemaName, String viewName, String userKey) throws PivotalMySQLWebException
    {
        String            def;
        javax.sql.DataSource dataSource;

        try
        {
            dataSource = AdminUtil.getDataSource(userKey);
            setDataSource(dataSource);

            def = jdbcTemplate.queryForObject
                    (Constants.USER_VIEW_DEF, new Object[]{schemaName, viewName}, String.class);

        }
        catch (Exception ex)
        {
            log.info("Error retrieving view definition for view = " + viewName);
            throw new PivotalMySQLWebException(ex);
        }

        return def;

    }

}
