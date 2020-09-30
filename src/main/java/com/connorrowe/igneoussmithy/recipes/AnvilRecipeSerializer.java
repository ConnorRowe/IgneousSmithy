package com.connorrowe.igneoussmithy.recipes;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.items.DynamicTool;
import com.connorrowe.igneoussmithy.items.ToolHead;
import com.connorrowe.igneoussmithy.setup.ModItems;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
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
import java.util.*;

public class AnvilRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<AnvilRecipe>
{
    public static final Map<ResourceLocation, AnvilRecipe> MAP = Collections.synchronizedMap(new LinkedHashMap<>());

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

        AnvilRecipe recipe = new AnvilRecipe(recipeId, ingredients, output);

        synchronized (MAP)
        {
            if (!MAP.containsKey(recipeId))
            {
                MAP.put(recipeId, recipe);
            }
        }

        return recipe;
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

        AnvilRecipe recipe = new AnvilRecipe(recipeId, ingredients, output);

        synchronized (MAP)
        {
            if (!MAP.containsKey(recipeId))
            {
                MAP.put(recipeId, recipe);
            }
        }

        return recipe;
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

    public static List<AnvilRecipe> getFakeRecipes()
    {
        List<AnvilRecipe> fakeRecipes = new ArrayList<>();
        fakeRecipes.add(makeFakeAnvilToolRecipe(new ResourceLocation(IgneousSmithy.MODID, "fake_pickaxe"), ModItems.PICKAXE_HEAD.get(), ModItems.DYNAMIC_PICKAXE.get()));
        fakeRecipes.add(makeFakeAnvilToolRecipe(new ResourceLocation(IgneousSmithy.MODID, "fake_shovel"), ModItems.SHOVEL_HEAD.get(), ModItems.DYNAMIC_SHOVEL.get()));
        fakeRecipes.add(makeFakeAnvilToolRecipe(new ResourceLocation(IgneousSmithy.MODID, "fake_hatchet"), ModItems.HATCHET_HEAD.get(), ModItems.DYNAMIC_HATCHET.get()));
        fakeRecipes.add(makeFakeAnvilToolRecipe(new ResourceLocation(IgneousSmithy.MODID, "fake_sword"), ModItems.SWORD_HEAD.get(), ModItems.DYNAMIC_SWORD.get()));
        return fakeRecipes;
    }

    private static AnvilRecipe makeFakeAnvilToolRecipe(ResourceLocation id, ToolHead toolHead, Item output)
    {
        ItemStack outputStack = new ItemStack(output);
        DynamicTool.setMaterials(outputStack, MaterialManager.FAKE_MAT, MaterialManager.FAKE_MAT, MaterialManager.FAKE_MAT);

        return new AnvilRecipe(id, NonNullList.from(Ingredient.EMPTY, MaterialManager.getPartIngredient(toolHead), MaterialManager.getPartIngredient(ModItems.BINDING.get()), MaterialManager.getPartIngredient(ModItems.HANDLE.get())), outputStack);
    }
}
