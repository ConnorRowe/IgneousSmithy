package com.connorrowe.igneoussmithy.jei;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.recipes.AnvilRecipe;
import com.connorrowe.igneoussmithy.setup.ModBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipe>
{
    protected static final ResourceLocation uid = new ResourceLocation(IgneousSmithy.MODID, "anvil_recipe_category");

    private final IDrawableStatic background;
    private final IDrawable icon;

    public AnvilRecipeCategory(IGuiHelper guiHelper)
    {
        this.background = guiHelper.drawableBuilder(new ResourceLocation(IgneousSmithy.MODID, "textures/gui/anvil_recipe.png"), 0, 0, 116, 54).setTextureSize(116, 54).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.MAGMATIC_ANVIL.get().asItem()));
    }

    @Nonnull
    @Override
    public ResourceLocation getUid()
    {
        return uid;
    }

    @Nonnull
    @Override
    public Class<? extends AnvilRecipe> getRecipeClass()
    {
        return AnvilRecipe.class;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return ModBlocks.MAGMATIC_ANVIL.get().getTranslatedName().getString();
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @Nonnull
    @Override
    public IDrawable getIcon()
    {
        return this.icon;
    }

    @Override
    public void setIngredients(@Nonnull AnvilRecipe anvilRecipe, @Nonnull IIngredients ingredients)
    {
        final List<List<ItemStack>> inputList = new ArrayList<>();

        for (Ingredient i : anvilRecipe.getIngredients())
        {
            inputList.add(Arrays.asList(i.getMatchingStacks()));
        }

        ingredients.setInputLists(VanillaTypes.ITEM, inputList);
        ingredients.setOutput(VanillaTypes.ITEM, anvilRecipe.getRecipeOutput());

    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout iRecipeLayout, @Nonnull AnvilRecipe anvilRecipe, @Nonnull IIngredients iIngredients)
    {
        IGuiItemStackGroup guiItemStackGroup = iRecipeLayout.getItemStacks();
        NonNullList<Ingredient> recipeIngredients = anvilRecipe.getIngredients();


        guiItemStackGroup.init(0, true, 18, 8);
        guiItemStackGroup.set(0, Arrays.asList(recipeIngredients.get(0).getMatchingStacks()));

        guiItemStackGroup.init(1, true, 7, 28);
        guiItemStackGroup.set(1, Arrays.asList(recipeIngredients.get(1).getMatchingStacks()));

        guiItemStackGroup.init(2, true, 28, 28);
        guiItemStackGroup.set(2, Arrays.asList(recipeIngredients.get(2).getMatchingStacks()));

        guiItemStackGroup.init(3, false, 87, 18);
        guiItemStackGroup.set(3, anvilRecipe.getRecipeOutput());

        iRecipeLayout.getFluidStacks().init(0, true, 57, 9, 12, 8, 200, true, null);
        iRecipeLayout.getFluidStacks().set(0, new FluidStack(Fluids.LAVA, 100));
    }
}
