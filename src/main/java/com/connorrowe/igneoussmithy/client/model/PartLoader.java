package com.connorrowe.igneoussmithy.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

import javax.annotation.Nonnull;

public enum PartLoader implements IModelLoader<PartModel>
{

    INSTANCE;

    @Override
    public void onResourceManagerReload(@Nonnull IResourceManager resourceManager) {

    }

    @Nonnull
    @Override
    public PartModel read(@Nonnull JsonDeserializationContext deserializationContext,
                          @Nonnull JsonObject modelContents) {
        return new PartModel();
    }
}
