package com.connorrowe.igneoussmithy.jei;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.recipes.DiagramRecipe;
import com.connorrowe.igneoussmithy.setup.ModItems;
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

public class DiagramRecipeCategory implements IRecipeCategory<DiagramRecipe>
{
    protected static final ResourceLocation uid = new ResourceLocation(IgneousSmithy.MODID, "diagram_recipe_category");

    private final IDrawableStatic background;
    private final IDrawable icon;

    public DiagramRecipeCategory(IGuiHelper guiHelper)
    {
        this.background = guiHelper.drawableBuilder(new ResourceLocation(IgneousSmithy.MODID, "textures/gui/diagram_recipe.png"), 0, 0, 116, 54).setTextureSize(116, 54).build();
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModItems.DIAGRAM_PICKAXE_HEAD.get()));
    }

    @Nonnull
    @Override
    public ResourceLocation getUid()
    {
        return uid;
    }

    @Nonnull
    @Override
    public Class<? extends DiagramRecipe> getRecipeClass()
    {
        return DiagramRecipe.class;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return "Diagram";
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
    public void setIngredients(@Nonnull DiagramRecipe diagramRecipe, @Nonnull IIngredients ingredients)
    {
        final List<List<ItemStack>> inputList = new ArrayList<>();

        for (Ingredient i : diagramRecipe.getIngredients())
        {
            inputList.add(Arrays.asList(i.getMatchingStacks()));
        }

        ingredients.setInputLists(VanillaTypes.ITEM, inputList);
        ingredients.setOutput(VanillaTypes.ITEM, diagramRecipe.getRecipeOutput());

    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayout iRecipeLayout, @Nonnull DiagramRecipe diagramRecipe, @Nonnull IIngredients iIngredients)
    {
        IGuiItemStackGroup guiItemStackGroup = iRecipeLayout.getItemStacks();
        NonNullList<Ingredient> recipeIngredients = diagramRecipe.getIngredients();


        guiItemStackGroup.init(0, true, 20, 8);
        guiItemStackGroup.set(0, Arrays.asList(recipeIngredients.get(0).getMatchingStacks()));

        guiItemStackGroup.init(1, true, 18, 32);
        guiItemStackGroup.set(1, Arrays.asList(recipeIngredients.get(1).getMatchingStacks()));

        guiItemStackGroup.init(2, false, 87, 18);
        guiItemStackGroup.set(2, diagramRecipe.getRecipeOutput());

        iRecipeLayout.getFluidStacks().init(0, true, 21, 31, 16, 4, 100, true, null);
        iRecipeLayout.getFluidStacks().set(0, new FluidStack(Fluids.LAVA, 100));
    }
}