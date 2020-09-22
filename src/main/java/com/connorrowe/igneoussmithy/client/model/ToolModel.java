package com.connorrowe.igneoussmithy.client.model;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.items.DynamicPickaxe;
import com.connorrowe.igneoussmithy.items.Material;
import com.connorrowe.igneoussmithy.tools.ColourHelper;
import com.connorrowe.igneoussmithy.tools.ModelHelper;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
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
import java.util.*;
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

    public IBakedModel bake(boolean broken, List<Integer> matColours, String[] textures,
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

        for (int i = 0; i < 3; i++)
        {
            String texName;
            if (i == 0)
                texName = "pick_head";
            else if (i == 1)
                texName = "pick_bind";
            else texName = "handle";

            texName += "_" + textures[i];

            IBakedModel ingredientModel = ModelHelper
                    .getBakedLayerModel(owner, bakery, spriteGetter, modelTransform, overrides,
                            new ResourceLocation(IgneousSmithy.MODID, "item/tool/" + texName));
            ColourHelper.colorQuads(ingredientModel, i < matColours.size() ? matColours.get(i) : 0x000000, random, builder);
            particleSprite = ingredientModel.getParticleTexture(EmptyModelData.INSTANCE);
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
            System.out.println("getBakedModel: " + DynamicPickaxe.getMaterials(stack).get(0).name.getString() + " tool");

            boolean isBroken = DynamicPickaxe.getBroken(stack);
            String[] textures = DynamicPickaxe.getMatTextures(stack);

            List<Integer> matColours = new ArrayList<>();
            NonNullList<Material> materials = DynamicPickaxe.getMaterials(stack);
            materials.forEach(material -> matColours.add(material.colour));

            return this.model.bake(isBroken, matColours, textures, this.owner, this.bakery, this.spriteGetter,
                    this.modelTransform, ItemOverrideList.EMPTY);
        }
    }
}
