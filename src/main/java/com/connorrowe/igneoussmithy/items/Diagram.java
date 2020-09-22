package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class Diagram extends Item
{
    public final PartType partType;
    public final Supplier<ToolPart> partSupplier;
    public final int materialCost;

    public Diagram(PartType partType, Supplier<ToolPart> partSupplier, int materialCost)
    {
        super(new Properties().group(IgneousSmithy.IgneousGroup.instance));
        this.partType = partType;
        this.partSupplier = partSupplier;
        this.materialCost = materialCost;
    }

    public ItemStack craft(Material material)
    {
        ItemStack crafted = new ItemStack(partSupplier.get(), 1);
        ToolPart.setMaterial(crafted, material);

        return crafted;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        TextComponent matCost = new TextComponent()
        {
            @Nonnull
            @Override
            public TextComponent copyRaw()
            {
                return this;
            }

            @Override
            public String getUnformattedComponentText()
            {
                return "Material cost: " + materialCost;
            }

            @Override
            public Style getStyle()
            {
                return Style.EMPTY.setColor(net.minecraft.util.text.Color.func_240743_a_(Color.GRAY.getRGB()));
            }
        };

        tooltip.add(matCost);
    }
}
