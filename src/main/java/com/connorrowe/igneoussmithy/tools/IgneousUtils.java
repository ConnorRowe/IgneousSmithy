package com.connorrowe.igneoussmithy.tools;

public final class IgneousUtils
{
    public static String CapitaliseString(String str)
    {
        if(str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
