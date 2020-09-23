package com.connorrowe.igneoussmithy.items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ToolLayer
{
    public final String baseTexture;
    public final Integer materialIndex;

    /**
     * @param baseTexture is the base tool texture name without a material suffix, ie. "pickaxe_head".
     * @param materialIndex is the index of the material used to retrieve the material suffix and colour.
     *                      If null, no suffix will be added, and the colour will be 0xFFFFFF.
     */
    public ToolLayer(@Nonnull String baseTexture, @Nullable Integer materialIndex)
    {
        this.baseTexture = baseTexture;
        this.materialIndex = materialIndex;
    }
}
