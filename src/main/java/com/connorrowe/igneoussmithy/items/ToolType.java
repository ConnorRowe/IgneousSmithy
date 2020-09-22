package com.connorrowe.igneoussmithy.items;

public final class ToolType
{

    public static final ToolType PICKAXE = create("pickaxe");

    private static ToolType create(String name)
    {
        return new ToolType(name);
    }

    private final String name;

    ToolType(String name)
    {
        this.name = name;
    }
}
