package com.connorrowe.igneoussmithy.items;

import java.util.ArrayList;

public final class PartType
{
    public static ArrayList<PartType> ALL_PARTS = new ArrayList<>();

    public static final PartType HEAD = create("Head", "part.igneoussmithy.head");
    public static final PartType BINDING = create("Binding", "part.igneoussmithy.binding");
    public static final PartType HANDLE = create("Handle", "part.igneoussmithy.handle");

    private static PartType create(String id, String nameKey)
    {
        if (ALL_PARTS == null)
            ALL_PARTS = new ArrayList<>();

        PartType newPart = new PartType(id, nameKey);
        ALL_PARTS.add(newPart);
        return newPart;
    }

    public final String id;
    public final String nameKey;

    PartType(String id, String nameKey)
    {
        this.id = id;
        this.nameKey = nameKey;
    }
}
