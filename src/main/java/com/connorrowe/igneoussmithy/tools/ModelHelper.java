package com.connorrowe.igneoussmithy.tools;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ItemLayerModel;

import java.util.Objects;
import java.util.function.Function;

// From Culinary Construct https://github.com/TheIllusiveC4/CulinaryConstruct
public class ModelHelper
{

    public static IBakedModel getBakedLayerModel(IModelConfiguration owner, ModelBakery bakery,
                                                 Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
                                                 ItemOverrideList overrides, ResourceLocation modelLocation)
    {
        return new ItemLayerModel(ImmutableList
                .of(new RenderMaterial(PlayerContainer.LOCATION_BLOCKS_TEXTURE, modelLocation)))
                .bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
    }

    //Cache Key from Tinkers' Construct
    public static class CacheKey
    {

        final IBakedModel parent;
        final CompoundNBT data;

        public CacheKey(IBakedModel parent, ItemStack stack)
        {
            this.parent = parent;
            this.data = stack.getOrCreateTag();
        }

        @Override
        public boolean equals(Object o)
        {

            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;

            if (parent != null ? parent != cacheKey.parent : cacheKey.parent != null)
            {
                return false;
            }
            return Objects.equals(data, cacheKey.data);
        }

        @Override
        public int hashCode()
        {
            int result = parent != null ? parent.hashCode() : 0;
            result = 31 * result + (data != null ? data.hashCode() : 0);
            return result;
        }
    }
}
