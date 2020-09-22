package com.connorrowe.igneoussmithy.client;

import com.connorrowe.igneoussmithy.blocks.MagmaticAnvilTile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

@OnlyIn(Dist.CLIENT)
public class MagmaticAnvilRenderer extends TileEntityRenderer<MagmaticAnvilTile>
{
    public MagmaticAnvilRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
    {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(MagmaticAnvilTile tileEntityIn, float partialTicks, MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
    {
        NonNullList<ItemStack> reagents = NonNullList.create();
        tileEntityIn.reagents.ifPresent(h -> reagents.addAll(h.getStacks()));

        float scale = 1f;

        Direction dir = tileEntityIn.getBlockState().get(BlockStateProperties.FACING);

        // Render stuff on top of anvil
        matrixStackIn.push();

        // Initial transforms
        matrixStackIn.scale(.25f * scale, .25f * scale, .25f * scale);
        matrixStackIn.translate(2D / scale, 2.8D / scale, 2D / scale);
        matrixStackIn.rotate(Vector3f.YN.rotationDegrees(dir.getHorizontalAngle())); //this one rotates horizontally
        matrixStackIn.rotate(Vector3f.XN.rotationDegrees(90f));

        int itr = 0;
        for (ItemStack stack : reagents)
        {
            if (itr == 1)
            {
                matrixStackIn.translate(0.3D / scale, 0.15D / scale, 0.05D / scale);
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(11.25f));
            } else if (itr == 2)
            {
                matrixStackIn.translate(-0.6D / scale, -0.2D / scale, 0.05D / scale);
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(-16.875f));
            }
            renderItem(stack, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, tileEntityIn);
            itr++;
        }

        matrixStackIn.pop();

        // Render output
        tileEntityIn.output.ifPresent(h ->
        {
            if (!h.getStackInSlot(0).isEmpty())
            {
                matrixStackIn.push();
                matrixStackIn.scale(.5f, .5f, .5f);
                matrixStackIn.translate(1D, 1.4D, 1D);
                matrixStackIn.rotate(Vector3f.YN.rotationDegrees(dir.getHorizontalAngle())); //this one rotates horizontally
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90f));

                renderItem(h.getStackInSlot(0), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, tileEntityIn);

                matrixStackIn.pop();
            }
        });

        // Render diagram
        AtomicReference<ItemStack> diagramStackAtom = new AtomicReference<>(ItemStack.EMPTY);
        tileEntityIn.diagram.ifPresent(t -> diagramStackAtom.set(t.getStackInSlot(0)));

        if (!diagramStackAtom.get().isEmpty())
        {
            matrixStackIn.push();
            //matrixStackIn.rotate(dir.getRotation());
            matrixStackIn.translate(.5D, .26D, .5D);
            matrixStackIn.rotate(Vector3f.YN.rotationDegrees(dir.getHorizontalAngle()));

            matrixStackIn.translate(0D, 0D, .325D);

            matrixStackIn.scale(.25f, .25f, .25f);

            renderItem(diagramStackAtom.get(), matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, tileEntityIn);
            matrixStackIn.pop();
        }
    }

    private void renderItem(ItemStack stack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlay, TileEntity te)
    {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        IBakedModel ibakedmodel = itemRenderer.getItemModelWithOverrides(stack, te.getWorld(), null);

        itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, true, matrixStackIn, bufferIn, combinedLightIn, combinedOverlay, ibakedmodel);
    }
}
