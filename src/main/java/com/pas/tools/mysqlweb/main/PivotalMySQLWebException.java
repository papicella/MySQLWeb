package com.pas.tools.mysqlweb.main;

public class PivotalMySQLWebException extends Exception
{
    private static final long serialVersionUID = 1L;

    public PivotalMySQLWebException()
    {
    }

    public PivotalMySQLWebException(final Throwable cause)
    {
        super(cause);
    }

    public PivotalMySQLWebException
            (final String msg,
             final Throwable cause)
    {
        super(msg, cause);
    }

    public PivotalMySQLWebException(final String msg)
    {
        super(msg);
    }
}
