package com.connorrowe.igneoussmithy.recipes;

import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.items.Diagram;
import com.connorrowe.igneoussmithy.items.Material;
import com.connorrowe.igneoussmithy.items.ToolPart;
import com.connorrowe.igneoussmithy.setup.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.List;

public class DiagramRecipe implements IDiagramRecipe
{
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack output;

    public DiagramRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack output)
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
        //TODO make new serializer
        //return ModRecipeSerializers.ANVIL_SERIALIZER.get();
        return null;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return this.ingredients;
    }

    private static List<DiagramRecipe> makeDiagramRecipes(Diagram diagram)
    {
        List<DiagramRecipe> recipes = new ArrayList<>();

        for (Material mat : MaterialManager.getValues())
        {
            List<Ingredient> ingredients = new ArrayList<>(2);

            ItemStack[] stacks = new ItemStack[mat.repairItems.size()];
            for (int i = 0; i < stacks.length; i++)
            {
                stacks[i] = new ItemStack(mat.repairItems.get(i), diagram.materialCost);
            }
            ingredients.add(Ingredient.fromStacks(stacks));

            for (ResourceLocation res : mat.repairTags)
            {
                ITag<Item> tag = Material.itemTags.get(res);
                if (tag != null)
                {
                    List<Item> allElements = tag.getAllElements();
                    ItemStack[] stacks1 = new ItemStack[allElements.size()];
                    for (int j = 0; j < stacks1.length; j++)
                    {
                        stacks1[j] = new ItemStack(allElements.get(j), diagram.materialCost);
                    }

                    ingredients.add(Ingredient.fromStacks(stacks1));
                }
            }

            Ingredient ingredient = Ingredient.merge(ingredients);

            NonNullList<Ingredient> recipeIngredients = NonNullList.withSize(2, Ingredient.EMPTY);
            recipeIngredients.set(0, ingredient);
            recipeIngredients.set(1, Ingredient.fromItems(diagram));

            ItemStack output = new ItemStack(diagram.partSupplier.get(), 1);
            ToolPart.setMaterial(output, mat);

            recipes.add(new DiagramRecipe(diagram.getRegistryName(), recipeIngredients, output));
        }

        return recipes;
    }

    public static List<DiagramRecipe> getDiagramRecipes()
    {
        List<DiagramRecipe> recipes = new ArrayList<>();

        recipes.addAll(makeDiagramRecipes(ModItems.DIAGRAM_PICKAXE_HEAD.get()));
        recipes.addAll(makeDiagramRecipes(ModItems.DIAGRAM_SHOVEL_HEAD.get()));
        recipes.addAll(makeDiagramRecipes(ModItems.DIAGRAM_HATCHET_HEAD.get()));
        recipes.addAll(makeDiagramRecipes(ModItems.DIAGRAM_SWORD_HEAD.get()));
        recipes.addAll(makeDiagramRecipes(ModItems.DIAGRAM_BINDING.get()));
        recipes.addAll(makeDiagramRecipes(ModItems.DIAGRAM_HANDLE.get()));

        return recipes;
    }
}
