package com.connorrowe.igneoussmithy.setup;

import com.connorrowe.igneoussmithy.recipes.AnvilRecipe;
import com.connorrowe.igneoussmithy.recipes.AnvilRecipeSerializer;
import com.connorrowe.igneoussmithy.recipes.IAnvilRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class ModRecipeSerializers
{
    public static final IRecipeSerializer<AnvilRecipe> ANVIL_RECIPE_SERIALIZER = new AnvilRecipeSerializer();
    public static final IRecipeType<IAnvilRecipe> ANVIL_TYPE = registerType(IAnvilRecipe.RECIPE_TYPE_ID);

    public static final RegistryObject<IRecipeSerializer<?>> ANVIL_SERIALIZER = register("anvil", () -> ANVIL_RECIPE_SERIALIZER);

    private static <T extends IRecipeSerializer<?>> RegistryObject<T> register(String name, final Supplier<? extends T> sup)
    {
        return Registration.RECIPE_SERIALIZERS.register(name, sup);
    }

    static void register()
    {
    }

    private static class RegistryType<T extends IRecipe<?>> implements IRecipeType<T>
    {
        @Override
        public String toString()
        {
            return Registry.RECIPE_TYPE.getKey(this).toString();
        }
    }

    private static <T extends IRecipeType> T registerType(ResourceLocation recipeTypeId)
    {
        return (T) Registry.register(Registry.RECIPE_TYPE, recipeTypeId, new RegistryType<>());
    }
}
