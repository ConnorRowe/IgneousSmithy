package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.tools.IgneousUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModifierItem extends Item
{
    public final String modifierName;

    public ModifierItem(String modifierName)
    {
        super(new Properties().maxStackSize(1).group(IgneousSmithy.IgneousGroup.instance));
        this.modifierName = modifierName;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {

        if (Screen.hasShiftDown())
        {
            tooltip.add(new IgneousUtils.BasicTextComponent("Can be used by:", Style.EMPTY.setColor(Color.func_240743_a_(java.awt.Color.gray.getRGB()))));
            for (ToolType toolType : Modifier.get(modifierName).applicableTools)
            {
                tooltip.add(new IgneousUtils.BasicTextComponent(IgneousUtils.CapitaliseString(toolType.name), Style.EMPTY.setColor(Color.func_240743_a_(0xA9A9A9))));
            }
        } else
        {
            tooltip.add(new IgneousUtils.BasicTextComponent("Provides:", Style.EMPTY.setColor(Color.func_240743_a_(java.awt.Color.gray.getRGB()))));
            tooltip.add(Modifier.get(modifierName).trait.toTextComponent());
            tooltip.add(new IgneousUtils.BasicTextComponent("Hold shift for applicable tools", Style.EMPTY.setColor(Color.func_240743_a_(0x808080)).setItalic(true)));
        }


        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
