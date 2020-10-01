package com.connorrowe.igneoussmithy.jei;

import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.items.Material;
import com.connorrowe.igneoussmithy.items.ToolPart;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import net.minecraft.item.ItemStack;

public class ToolPartSubtypeInterpreter implements ISubtypeInterpreter
{
    public static final ToolPartSubtypeInterpreter INSTANCE = new ToolPartSubtypeInterpreter();

    private ToolPartSubtypeInterpreter()
    {
    }

    @Override
    public String apply(ItemStack itemStack)
    {
        Material mat = ToolPart.getMaterial(itemStack);

        if (mat == null || mat == MaterialManager.FAKE_MAT)
        {
            return ISubtypeInterpreter.NONE;
        }

        String matName = mat.getId().getPath();

        return matName + ((ToolPart) (itemStack.getItem())).getPartType().name;
    }
}
