package com.connorrowe.igneoussmithy.recipes;

import com.connorrowe.igneoussmithy.setup.ModRecipeSerializers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class AnvilRecipe implements IAnvilRecipe
{
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;

    public AnvilRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack output)
    {
        this.id = id;
        this.ingredients = ingredients;
        this.output = output;
    }

    @Override
    public boolean matches(RecipeWrapper inv, World worldIn)
    {
        boolean match = true;

        for (Ingredient ingredient : this.ingredients)
        {
            boolean matched = false;

            for (int i = 0; i < inv.getSizeInventory(); i++)
            {
                if (ingredient.test(inv.getStackInSlot(i)))
                {
                    matched = true;
                    break;
                }
            }

            if (!matched)
                match = matched;
        }

        return match;
    }

    @Override
    public ItemStack getCraftingResult(RecipeWrapper inv)
    {
        return this.output;
    }

    @Override
    public ItemStack getRecipeOutput()
    {
        return this.output;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.ANVIL_SERIALIZER.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return this.ingredients;
    }
}
