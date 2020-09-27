package com.connorrowe.igneoussmithy.setup;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Registration
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, IgneousSmithy.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, IgneousSmithy.MODID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, IgneousSmithy.MODID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, IgneousSmithy.MODID);
    public static final DeferredRegister<IRecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, IgneousSmithy.MODID);

    private Registration()
    {
    }

    public static void register()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        CONTAINERS.register(modEventBus);
        ITEMS.register(modEventBus);
        TILE_ENTITIES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        ModBlocks.register();
        ModItems.register();
        ModTileEntities.register();
        ModRecipeSerializers.register();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Block> Collection<T> getBlocks(Class<T> clazz)
    {
        return BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .filter(clazz::isInstance)
                .map(b -> (T) b)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Item> Collection<T> getItems(Class<T> clazz)
    {
        return ITEMS.getEntries().stream()
                .map(RegistryObject::get)
                .filter(clazz::isInstance)
                .map(i -> (T) i)
                .collect(Collectors.toList());
    }

    public static Collection<Item> getItems(Predicate<Item> predicate)
    {
        return ITEMS.getEntries().stream()
                .map(RegistryObject::get)
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
