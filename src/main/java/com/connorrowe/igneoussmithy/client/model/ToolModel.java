package com.connorrowe.igneoussmithy.client.model;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.items.*;
import com.connorrowe.igneoussmithy.tools.ColourHelper;
import com.connorrowe.igneoussmithy.tools.ModelHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

// The whole model baking system is modified from Culinary Construct https://github.com/TheIllusiveC4/CulinaryConstruct
public final class ToolModel implements IModelGeometry<ToolModel>
{
    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
                            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                            ItemOverrideList overrides, ResourceLocation modelLocation)
    {
        IBakedModel model = ModelHelper
                .getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                        new ResourceLocation(IgneousSmithy.MODID, "item/tool/broken"));
        TextureAtlasSprite particleSprite = model.getParticleTexture(EmptyModelData.INSTANCE);
        return new PerspectiveItemModel(ImmutableList.of(), particleSprite,
                PerspectiveMapWrapper.getTransforms(modelTransform),
                new BakedToolOverrideHandler(this, owner, bakery, spriteGetter, modelTransform,
                        modelLocation), modelTransform.getRotation().isIdentity(), owner.isSideLit(),
                owner.getCameraTransforms());
    }

    public IBakedModel bake(boolean broken, NonNullList<ToolLayer> layers, NonNullList<Material> materials, ToolType toolType, NonNullList<Modifier> modifiers,
                            IModelConfiguration owner, ModelBakery bakery,
                            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                            ItemOverrideList overrides)
    {

        Random random = new Random();
        random.setSeed(42);
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        IBakedModel model = ModelHelper
                .getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                        new ResourceLocation(IgneousSmithy.MODID, "item/tool/broken"));
        TextureAtlasSprite particleSprite = model.getParticleTexture(EmptyModelData.INSTANCE);

        for (ToolLayer layer : layers)
        {
            boolean fallBack = false;


            String texName = layer.baseTexture;

            // test if texture exists
            try
            {
                Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(IgneousSmithy.MODID, "textures/item/tool/" + texName + "_" + materials.get(layer.materialIndex).texture + ".png"));
                if (layer.materialIndex != null)
                    texName += "_" + materials.get(layer.materialIndex).texture;

            } catch (IOException e)
            {
                // fallback to dull texture
                texName += "_dull";
                fallBack = true;
            }

            IBakedModel layerModel = ModelHelper.getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                    new ResourceLocation(IgneousSmithy.MODID, "item/tool/" + texName));

            if (layer.materialIndex != null && (materials.get(layer.materialIndex).applyColour || fallBack))
                ColourHelper.colorQuads(layerModel, materials.get(layer.materialIndex).colour, random, builder);
            else
                builder.addAll(layerModel.getQuads(null, null, random, EmptyModelData.INSTANCE));

            if (materials.get(layer.materialIndex).texture.equals("shiny"))
            {
                IBakedModel glintModel = ModelHelper.getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                        new ResourceLocation(IgneousSmithy.MODID, "item/tool/" + layer.baseTexture + "_glint"));

                builder.addAll(glintModel.getQuads(null, null, random, EmptyModelData.INSTANCE));
            }
        }

        for (Modifier modifier : modifiers)
        {
            if (modifier.texture != null)
            {
                ResourceLocation tex = new ResourceLocation(modifier.texture.getNamespace(), "item/tool/" + toolType.id + "_" + modifier.texture.getPath());

                IBakedModel modifierModel = ModelHelper.getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides, tex);

                if (modifier.colour != 0xFFFFFF)
                    ColourHelper.colorQuads(modifierModel, modifier.colour, random, builder);
                else
                    builder.addAll(modifierModel.getQuads(null, null, random, EmptyModelData.INSTANCE));
            }
        }

        if (broken)
        {
            IBakedModel brokenModel = ModelHelper.getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                    new ResourceLocation(IgneousSmithy.MODID, "item/tool/broken"));
            builder.addAll(brokenModel.getQuads(null, null, random, EmptyModelData.INSTANCE));
        }

        return new PerspectiveItemModel(builder.build(), particleSprite,
                PerspectiveMapWrapper.getTransforms(modelTransform), overrides,
                modelTransform.getRotation().isIdentity(), owner.isSideLit(), owner.getCameraTransforms());
    }

    @Override
    public Collection<RenderMaterial> getTextures(IModelConfiguration owner,
                                                  Function<ResourceLocation, IUnbakedModel> modelGetter,
                                                  Set<Pair<String, String>> missingTextureErrors)
    {
        return Collections.emptyList();
    }

    private static final class BakedToolOverrideHandler extends
            IgneousOverrideHandler<ToolModel>
    {

        public BakedToolOverrideHandler(ToolModel model, IModelConfiguration owner,
                                        ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
                                        IModelTransform modelTransform, ResourceLocation modelLocation)
        {
            super(model, owner, bakery, spriteGetter, modelTransform, modelLocation);
        }

        @Override
        protected IBakedModel getBakedModel(IBakedModel originalModel, ItemStack stack,
                                            @Nullable World world, @Nullable LivingEntity entity)
        {
            System.out.println("getBakedModel: " + DynamicTool.getHeadMat(stack).name.getString() + DynamicTool.getToolType(stack).id);

            boolean isBroken = DynamicTool.getBroken(stack);

            return this.model.bake(isBroken, DynamicTool.getLayers(stack), DynamicTool.getMaterials(stack), DynamicTool.getToolType(stack), DynamicTool.getModifiers(stack), this.owner, this.bakery, this.spriteGetter,
                    this.modelTransform, ItemOverrideList.EMPTY);
        }
    }
}
