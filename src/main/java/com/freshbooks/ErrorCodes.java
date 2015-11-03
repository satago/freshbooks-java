package com.freshbooks;

import java.util.ArrayList;
import java.util.List;

public class ErrorCodes
{
    public static final int CLIENT_DELETED = 50010;

    public static final List<Integer> HANDLED_CODES = new ArrayList<Integer>();

    static
    {
        HANDLED_CODES.add(CLIENT_DELETED);
    }
}
