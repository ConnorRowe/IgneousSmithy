package com.connorrowe.igneoussmithy.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.codehaus.plexus.util.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ToolHead extends ToolPart
{
    private final ToolType toolType;

    public ToolHead(ToolType toolType)
    {
        super(PartType.HEAD);
        this.toolType = toolType;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack)
    {
        return new StringTextComponent(getMaterial(stack).name.getString() + " " + StringUtils.capitalizeFirstLetter(toolType.name) + " Head");
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        ToolPart.getMaterial(stack).headOnlyTraits.forEach(t -> tooltip.add(t.toTextComponent()));

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Nonnull
    public ToolType getToolType()
    {
        return toolType;
    }
}