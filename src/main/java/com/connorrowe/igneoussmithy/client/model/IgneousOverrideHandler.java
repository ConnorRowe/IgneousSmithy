package com.connorrowe.igneoussmithy.client.model;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.tools.ModelHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class IgneousOverrideHandler<T extends IModelGeometry<T>> extends
        ItemOverrideList
{

    protected final T model;
    protected final ModelBakery bakery;
    protected final IModelConfiguration owner;
    protected final Function<RenderMaterial, TextureAtlasSprite> spriteGetter;
    protected final IModelTransform modelTransform;
    protected final ResourceLocation modelLocation;
    private final Cache<ModelHelper.CacheKey, IBakedModel> bakedModelCache = CacheBuilder.newBuilder().maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES).build();

    public IgneousOverrideHandler(T model, IModelConfiguration owner, ModelBakery bakery,
                                   Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                                   ResourceLocation modelLocation) {
        super();
        this.model = model;
        this.owner = owner;
        this.bakery = bakery;
        this.spriteGetter = spriteGetter;
        this.modelLocation = modelLocation;
        this.modelTransform = modelTransform;
    }

    @Nonnull
    @Override
    public IBakedModel func_239290_a_(@Nonnull IBakedModel originalModel, @Nonnull ItemStack stack,
                                      @Nullable ClientWorld worldIn, @Nullable LivingEntity entityIn) {
        CompoundNBT data = stack.getOrCreateTag();
        IBakedModel output = originalModel;

        if (!data.isEmpty()) {
            ModelHelper.CacheKey key = getCacheKey(originalModel, stack);
            try {
                output = bakedModelCache
                        .get(key, () -> getBakedModel(originalModel, stack, worldIn, entityIn));
            } catch (ExecutionException e) {
                IgneousSmithy.LOGGER.error("Error baking model!");
            }
        }
        return output;
    }

    protected abstract IBakedModel getBakedModel(IBakedModel originalModel, ItemStack stack,
                                                 @Nullable World world, @Nullable LivingEntity entity);

    ModelHelper.CacheKey getCacheKey(IBakedModel original, ItemStack stack) {
        return new ModelHelper.CacheKey(original, stack);
    }
}