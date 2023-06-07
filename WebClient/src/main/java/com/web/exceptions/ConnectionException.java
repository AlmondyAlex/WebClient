package com.web.exceptions;

/**
 * Exception class used for business logic
 *
 * @author Aleksei Karlovich
 */
public class ConnectionException extends RuntimeException
{
    public ConnectionException(String s)
    {
        super(s);
    }
    public ConnectionException(Throwable cause) { super(cause); }
}
