package com.connorrowe.igneoussmithy.setup;

import com.connorrowe.igneoussmithy.blocks.MagmaticAnvil;
import com.connorrowe.igneoussmithy.tools.BlockRegistryObject;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

import java.util.function.Supplier;

public class ModBlocks
{
    public static final BlockRegistryObject<Block> MAGMATIC_ANVIL = register("magmatic_anvil", MagmaticAnvil::new);

    static
    {

    }

    private ModBlocks()
    {
    }

    static void register()
    {
    }

    private static <T extends Block> BlockRegistryObject<T> registerNoItem(String name, Supplier<T> block)
    {
        return new BlockRegistryObject<>(Registration.BLOCKS.register(name, block));
    }

    private static <T extends Block> BlockRegistryObject<T> register(String name, Supplier<T> block)
    {
        BlockRegistryObject<T> ret = registerNoItem(name, block);
        Registration.ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
        return ret;
    }
}
