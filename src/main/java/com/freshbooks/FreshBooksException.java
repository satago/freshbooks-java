package com.freshbooks;

public class FreshBooksException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public FreshBooksException(String message)
    {
        super(message);
    }
}
