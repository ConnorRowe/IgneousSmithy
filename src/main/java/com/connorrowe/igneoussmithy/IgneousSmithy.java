package com.connorrowe.igneoussmithy;

import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.items.DynamicTool;
import com.connorrowe.igneoussmithy.items.Material;
import com.connorrowe.igneoussmithy.items.ToolPart;
import com.connorrowe.igneoussmithy.setup.ModBlocks;
import com.connorrowe.igneoussmithy.setup.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(IgneousSmithy.MODID)
public class IgneousSmithy
{
    @SuppressWarnings("SpellCheckingInspection")
    public static final String MODID = "igneoussmithy";
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static IgneousSmithy INSTANCE;
    public static IProxy PROXY;

    public IgneousSmithy()
    {
        INSTANCE = this;
        PROXY = DistExecutor.safeRunForDist(() -> SideProxy.Client::new, () -> SideProxy.Server::new);
    }

    public static class IgneousGroup extends ItemGroup
    {
        public static final IgneousGroup instance = new IgneousGroup(ItemGroup.GROUPS.length, "igneous_tab");

        private IgneousGroup(int idx, String label)
        {
            super(idx, label);
        }

        public static void load()
        {
        }

        @Nonnull
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(ModBlocks.MAGMATIC_ANVIL.asItem());
        }

        @Override
        public void fill(@Nonnull NonNullList<ItemStack> items)
        {
            super.fill(items);
            items.add(new ItemStack((ModBlocks.MAGMATIC_ANVIL.get())));

            for (Material material : MaterialManager.getValues())
            {
                ItemStack stack = new ItemStack(ModItems.PICKAXE_HEAD.get());
                ToolPart.setMaterial(stack, material);
                items.add(stack);
                stack = new ItemStack(ModItems.SHOVEL_HEAD.get());
                ToolPart.setMaterial(stack, material);
                items.add(stack);
                stack = new ItemStack(ModItems.BINDING.get());
                ToolPart.setMaterial(stack, material);
                items.add(stack);
                stack = new ItemStack(ModItems.HANDLE.get());
                ToolPart.setMaterial(stack, material);
                items.add(stack);
            }

            for (Material material : MaterialManager.getValues())
            {
                ItemStack stack = new ItemStack(ModItems.DYNAMIC_PICKAXE.get());
                DynamicTool.initialiseStack(stack);
                DynamicTool.setMaterials(stack, material, material, material);
                items.add(stack);
                stack = new ItemStack(ModItems.DYNAMIC_SHOVEL.get());
                DynamicTool.initialiseStack(stack);
                DynamicTool.setMaterials(stack, material, material, material);
                items.add(stack);
            }
        }
    }
}
