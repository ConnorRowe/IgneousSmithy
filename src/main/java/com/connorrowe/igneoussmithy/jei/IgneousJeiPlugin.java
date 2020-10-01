package com.connorrowe.igneoussmithy.jei;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.recipes.AnvilRecipeSerializer;
import com.connorrowe.igneoussmithy.recipes.DiagramRecipe;
import com.connorrowe.igneoussmithy.setup.ModBlocks;
import com.connorrowe.igneoussmithy.setup.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@JeiPlugin
public class IgneousJeiPlugin implements IModPlugin
{
    private static final ResourceLocation pluginUid = new ResourceLocation(IgneousSmithy.MODID, "igneous_jei_plugin");

    @Nonnull
    @Override
    public ResourceLocation getPluginUid()
    {
        return pluginUid;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration)
    {
        registration.useNbtForSubtypes(ModItems.DYNAMIC_PICKAXE.get(), ModItems.DYNAMIC_SHOVEL.get(),
                ModItems.DYNAMIC_HATCHET.get(), ModItems.DYNAMIC_SWORD.get());

        registration.registerSubtypeInterpreter(ModItems.PICKAXE_HEAD.get(), ToolPartSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.SHOVEL_HEAD.get(), ToolPartSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.HATCHET_HEAD.get(), ToolPartSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.SWORD_HEAD.get(), ToolPartSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.HANDLE.get(), ToolPartSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(ModItems.BINDING.get(), ToolPartSubtypeInterpreter.INSTANCE);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(new ArrayList<>(AnvilRecipeSerializer.MAP.values()), AnvilRecipeCategory.uid);
        registration.addRecipes(AnvilRecipeSerializer.getFakeRecipes(), AnvilRecipeCategory.uid);

        registration.addRecipes(DiagramRecipe.getDiagramRecipes(), DiagramRecipeCategory.uid);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MAGMATIC_ANVIL), AnvilRecipeCategory.uid);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BALL_PEEN_HAMMER.get()), AnvilRecipeCategory.uid);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MAGMATIC_ANVIL), DiagramRecipeCategory.uid);
        registration.addRecipeCatalyst(new ItemStack(ModItems.BALL_PEEN_HAMMER.get()), DiagramRecipeCategory.uid);

    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        registration.addRecipeCategories(new AnvilRecipeCategory(registration.getJeiHelpers().getGuiHelper()));

        registration.addRecipeCategories(new DiagramRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }
}
