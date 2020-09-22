package com.connorrowe.igneoussmithy.items;

import java.util.ArrayList;

public final class PartType
{
    public static ArrayList<PartType> ALL_PARTS = new ArrayList<>();

    public static final PartType HEAD = create("Head");
    public static final PartType BINDING = create("Binding");
    public static final PartType HANDLE = create("Handle");

    private static PartType create(String name)
    {
        if (ALL_PARTS == null)
            ALL_PARTS = new ArrayList<>();

        PartType newPart = new PartType(name);
        ALL_PARTS.add(newPart);
        return newPart;
    }

    public final String name;

    PartType(String name)
    {
        this.name = name;
    }
}
