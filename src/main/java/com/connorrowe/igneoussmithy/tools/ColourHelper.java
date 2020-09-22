package com.connorrowe.igneoussmithy.tools;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.VertexTransformer;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;
import java.util.Random;

public class ColourHelper
{

    // From Culinary Construct https://github.com/TheIllusiveC4/CulinaryConstruct
    public static void colorQuads(IBakedModel bakedModel, int color, Random random,
                                  ImmutableList.Builder<BakedQuad> builder)
    {
        List<BakedQuad> quads = bakedModel.getQuads(null, null, random, EmptyModelData.INSTANCE);

        for (BakedQuad quad : quads)
        {
            ColorTransformer transformer = new ColorTransformer(color, quad);
            quad.pipe(transformer);
            builder.add(transformer.build());
        }
    }

    // Color Transformer from Mantle
    private static class ColorTransformer extends VertexTransformer
    {

        private final float r, g, b, a;

        public ColorTransformer(int color, BakedQuad quad)
        {
            super(new BakedQuadBuilder(quad.func_187508_a()));

            int a = (color >> 24);

            if (a == 0)
            {
                a = 255;
            }
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color) & 0xFF;

            this.r = (float) r / 255F;
            this.g = (float) g / 255F;
            this.b = (float) b / 255F;
            this.a = (float) a / 255F;
        }

        @Override
        public void put(int element, @Nonnull float... data)
        {
            VertexFormatElement.Usage usage = parent.getVertexFormat().getElements().get(element)
                    .getUsage();

            // Transform normals and position
            if (usage == VertexFormatElement.Usage.COLOR && data.length >= 4)
            {
                data[0] = r;
                data[1] = g;
                data[2] = b;
                data[3] = a;
            }
            super.put(element, data);
        }

        public BakedQuad build()
        {
            return ((BakedQuadBuilder) parent).build();
        }
    }

    public static net.minecraft.util.text.Color lerpColours(int a, int b, float alpha)
    {
        Color color1 = new Color(a);
        Color color2 = new Color(b);

        return net.minecraft.util.text.Color.func_240743_a_(lerpColours(color1, color2, alpha).getRGB());
    }

    public static Color lerpColours(Color c1, Color c2, float ratio)
    {
        float[] floats1 = new float[3];
        float[] floats2 = new float[3];

        c1.getRGBColorComponents(floats1);
        c2.getRGBColorComponents(floats2);

        for (int i = 0; i < 3; i++)
        {
            floats1[i] = clampColF(floats1[i] + ratio * (floats2[i] - floats1[i]));
        }

        return new Color(floats1[0], floats1[1], floats1[2]);
    }

    private static float clampColF(float f)
    {
        return clampF(f, 0, 255);
    }

    private static float clampF(float f, float min, float max)
    {
        return Math.max(min, Math.min(max, f));
    }
}