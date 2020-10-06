package com.connorrowe.igneoussmithy.client.model;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.items.Material;
import com.connorrowe.igneoussmithy.items.PartType;
import com.connorrowe.igneoussmithy.items.ToolHead;
import com.connorrowe.igneoussmithy.items.ToolPart;
import com.connorrowe.igneoussmithy.tools.ColourHelper;
import com.connorrowe.igneoussmithy.tools.ModelHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
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

public final class PartModel implements IModelGeometry<PartModel>
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
                new BakedPartOverrideHandler(this, owner, bakery, spriteGetter, modelTransform,
                        modelLocation), modelTransform.getRotation().isIdentity(), owner.isSideLit(),
                owner.getCameraTransforms());
    }

    public IBakedModel bake(Material material, PartType partType, @Nullable ToolHead toolHead,
                            IModelConfiguration owner, ModelBakery bakery,
                            Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                            ItemOverrideList overrides)
    {

        Random random = new Random();
        random.setSeed(42);
        boolean fallBack = false;
        String texName = "";

        if (partType == PartType.HEAD)
        {
            texName = "item/tool/pickaxe_head";
            if (toolHead != null)
            {
                texName = "item/tool/" + toolHead.getToolType().id + "_head";
            }
        } else if (partType == PartType.BINDING)
            texName = "item/part/binding";
        else if (partType == PartType.HANDLE)
            texName = "item/tool/handle";

        // test if texture exists
        try
        {
            Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(IgneousSmithy.MODID, "textures/" + texName + "_" + material.texture + ".png"));
            texName += "_" + material.texture;
        } catch (IOException e)
        {
            // fallback to dull texture
            texName += "_dull";
            fallBack = true;
        }

        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
        IBakedModel model = ModelHelper
                .getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                        new ResourceLocation(IgneousSmithy.MODID, texName));

        builder.addAll(model.getQuads(null, null, random, EmptyModelData.INSTANCE));

        if (material.applyColour || fallBack)
            ColourHelper.colorQuads(model, material.colour, random, builder);

        // Glint
        if (material.texture.equals("shiny"))
        {
            IBakedModel glintModel = ModelHelper.getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                    new ResourceLocation(IgneousSmithy.MODID, "item/" +
                            (partType.equals(PartType.BINDING) ? "part/" : "tool/") +
                            (partType.equals(PartType.HEAD) && toolHead != null ? toolHead.getToolType().id + "_head" : partType.id) +
                            "_glint"));
            builder.addAll(glintModel.getQuads(null, null, random, EmptyModelData.INSTANCE));
        }

        TextureAtlasSprite particleSprite = model.getParticleTexture(EmptyModelData.INSTANCE);

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

    private static final class BakedPartOverrideHandler extends
            IgneousOverrideHandler<PartModel>
    {

        public BakedPartOverrideHandler(PartModel model, IModelConfiguration owner,
                                        ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
                                        IModelTransform modelTransform, ResourceLocation modelLocation)
        {
            super(model, owner, bakery, spriteGetter, modelTransform, modelLocation);
        }

        @Override
        protected IBakedModel getBakedModel(IBakedModel originalModel, ItemStack stack,
                                            @Nullable World world, @Nullable LivingEntity entity)
        {
            System.out.println("CONNOR - getBakedModel");

            Material material = ToolPart.getMaterial(stack);
            PartType partType = PartType.HEAD;
            ToolHead toolHead = null;

            if (stack.getItem() instanceof ToolPart)
            {
                ToolPart p = (ToolPart) stack.getItem();
                partType = p.getPartType();

                if (p instanceof ToolHead)
                {
                    toolHead = (ToolHead) p;
                }
            }

            return this.model.bake(material, partType, toolHead, this.owner, this.bakery, this.spriteGetter,
                    this.modelTransform, ItemOverrideList.EMPTY);
        }
    }
}
