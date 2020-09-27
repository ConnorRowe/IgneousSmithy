package com.connorrowe.igneoussmithy.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class AnvilRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AnvilRecipe>
{
    @Override
    public AnvilRecipe read(ResourceLocation recipeId, JsonObject json)
    {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(3, Ingredient.EMPTY);
        JsonArray ingredientsJson = JSONUtils.getJsonArray(json, "ingredients");
        for (int i = 0; i < 3 && i < ingredientsJson.size(); i++)
        {
            JsonElement element = ingredientsJson.get(i);

            if (element != null)
                ingredients.set(i, Ingredient.deserialize(element));
        }

        ItemStack output = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "result"), true);

        return new AnvilRecipe(recipeId, ingredients, output);
    }

    @Nullable
    @Override
    public AnvilRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
    {
        NonNullList<Ingredient> ingredients = NonNullList.withSize(3, Ingredient.EMPTY);
        for (int i = 0; i < 3; i++)
        {
            ingredients.set(i, Ingredient.read(buffer));
        }

        ItemStack output = buffer.readItemStack();

        return new AnvilRecipe(recipeId, ingredients, output);
    }

    @Override
    public void write(PacketBuffer buffer, AnvilRecipe recipe)
    {
        for (int i = 0; i < 3; i++)
        {
            recipe.getIngredients().get(i).write(buffer);
        }

        buffer.writeItemStack(recipe.getRecipeOutput(), false);
    }
}
