package com.connorrowe.igneoussmithy.setup;

import com.connorrowe.igneoussmithy.blocks.MagmaticAnvilTile;
import com.connorrowe.igneoussmithy.tools.IBlockProvider;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import java.util.Collection;
import java.util.function.Supplier;

public class ModTileEntities
{
    public static final RegistryObject<TileEntityType<MagmaticAnvilTile>> MAGMATIC_ANVIL = register("magmatic_anvil", MagmaticAnvilTile::new, ModBlocks.MAGMATIC_ANVIL);

    static void register()
    {
    }

    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> factoryIn, IBlockProvider validBlock)
    {
        return register(name, factoryIn, () -> ImmutableList.of(validBlock.asBlock()));
    }

    private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> factoryIn, Supplier<Collection<? extends Block>> validBlocksSupplier)
    {
        return Registration.TILE_ENTITIES.register(name, () ->
        {
            Block[] validBlocks = validBlocksSupplier.get().toArray(new Block[0]);
            //noinspection ConstantConditions -- null in build
            return TileEntityType.Builder.create(factoryIn, validBlocks).build(null);
        });
    }
}
