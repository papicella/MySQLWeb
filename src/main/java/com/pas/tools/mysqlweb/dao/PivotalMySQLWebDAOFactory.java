package com.pas.tools.mysqlweb.dao;

import com.pas.tools.mysqlweb.dao.constraints.ConstraintDAO;
import com.pas.tools.mysqlweb.dao.constraints.ConstraintDAOImpl;
import com.pas.tools.mysqlweb.dao.generic.GenericDAO;
import com.pas.tools.mysqlweb.dao.generic.GenericDAOImpl;
import com.pas.tools.mysqlweb.dao.indexes.IndexDAO;
import com.pas.tools.mysqlweb.dao.indexes.IndexDAOImpl;
import com.pas.tools.mysqlweb.dao.tables.TableDAO;
import com.pas.tools.mysqlweb.dao.tables.TableDAOImpl;
import com.pas.tools.mysqlweb.dao.views.ViewDAO;
import com.pas.tools.mysqlweb.dao.views.ViewDAOImpl;

public class PivotalMySQLWebDAOFactory
{
    public static TableDAO getTableDAO()
    {
        return new TableDAOImpl();
    }

    public static ViewDAO getViewDAO()
    {
        return new ViewDAOImpl();
    }

    public static IndexDAO getIndexDAO()
    {
        return new IndexDAOImpl();
    }

    public static ConstraintDAO getConstraintDAO()
    {
        return new ConstraintDAOImpl();
    }

    public static GenericDAO getGenericDAO()
    {
        return new GenericDAOImpl();
    }
}
