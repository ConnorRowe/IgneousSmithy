package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.data.MaterialManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ToolPart extends Item
{
    private static final String NBT_MATERIAL_NAMESPACE = "mat_namespace";
    private static final String NBT_MATERIAL_PATH = "mat_path";
    private final PartType partType;

    public ToolPart(PartType partType)
    {
        super(new Properties());
        this.partType = partType;
    }

    public static void setMaterial(ItemStack stack, Material material)
    {
        stack.getOrCreateTag().putString(NBT_MATERIAL_NAMESPACE, material.getId().getNamespace());
        stack.getOrCreateTag().putString(NBT_MATERIAL_PATH, material.getId().getPath());
    }

    public static Material getMaterial(ItemStack stack)
    {
        if (stack.getOrCreateTag().getString(NBT_MATERIAL_NAMESPACE).equals("") || stack.getOrCreateTag().getString(NBT_MATERIAL_PATH).equals(""))
        {
            return MaterialManager.FAKE_MAT;
        } else
        {
            return MaterialManager.get(new ResourceLocation(stack.getOrCreateTag().getString(NBT_MATERIAL_NAMESPACE), stack.getOrCreateTag().getString(NBT_MATERIAL_PATH)));
        }
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack)
    {
        return new StringTextComponent(getMaterial(stack).name.getString()).appendString(" ").append(new TranslationTextComponent(partType.nameKey));
    }

    public PartType getPartType()
    {
        return partType;
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<ITextComponent> tooltip, @NotNull ITooltipFlag flagIn)
    {
        Material mat = getMaterial(stack);

        if (mat != null && mat != MaterialManager.FAKE_MAT)
        {
            List<ITextComponent> info = getMaterialTooltip(mat, partType);

            mat.allTraits.forEach(t -> tooltip.add(t.getName()));

            tooltip.addAll(info);
        }
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public static List<ITextComponent> getMaterialTooltip(Material mat, PartType part)
    {
        List<ITextComponent> info = new ArrayList<>();

        if (part == PartType.HEAD)
        {
            info.add(new TooltipText("Durability: " + mat.durability));
            info.add(new TooltipText("Efficiency: " + mat.efficiency));
            info.add(new TooltipText("Attack: " + mat.attackDamage));
            info.add(new TooltipText("Harvest lvl: " + mat.harvestLevel));
        } else if (part == PartType.BINDING)
        {
            info.add(new TooltipText("Multiplier: x" + mat.bindingMultiplier));
            info.add(new TooltipText("Durability: " + mat.bindingDurability));
        } else if (part == PartType.HANDLE)
        {
            //TODO: add stats for handle
        }

        return info;
    }

    private static class TooltipText extends TextComponent
    {
        private final String text;

        public TooltipText(String text)
        {
            this.text = text;
        }

        @Nonnull
        @Override
        public String getUnformattedComponentText()
        {
            return text;
        }

        @Nonnull
        @Override
        public TextComponent copyRaw()
        {
            return this;
        }

        @Nonnull
        @Override
        public Style getStyle()
        {
            return Style.EMPTY.setColor(Color.func_240743_a_(java.awt.Color.gray.getRGB()));
        }
    }
}
