package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.data.MaterialManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
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
        return new StringTextComponent(getMaterial(stack).name.getString()).appendString(" ")
                .append(new TranslationTextComponent(toolType.nameKey)).appendString(" ")
                .append(new TranslationTextComponent(toolType.equals(ToolType.SWORD) ? "part.igneoussmithy.blade" : "part.igneoussmithy.head"));
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        Material mat = getMaterial(stack);

        if (mat != null && mat != MaterialManager.FAKE_MAT)
        {
            List<ITextComponent> info = getMaterialTooltip(mat, PartType.HEAD);

            List<Trait> traits = new ArrayList<>(mat.allTraits);

            mat.headOnlyTraits.forEach(t ->
            {
                if (!traits.contains(t))
                    traits.add(t);
            });

            traits.forEach(t -> tooltip.add(t.getName()));

            tooltip.addAll(info);
        }

        //super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    @Nonnull
    public ToolType getToolType()
    {
        return toolType;
    }
}
