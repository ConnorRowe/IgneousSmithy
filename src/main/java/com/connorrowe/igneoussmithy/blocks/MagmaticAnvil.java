package com.connorrowe.igneoussmithy.blocks;

import com.connorrowe.igneoussmithy.items.Diagram;
import com.connorrowe.igneoussmithy.setup.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicBoolean;

public class MagmaticAnvil extends Block
{
    private static final VoxelShape shape = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 11.0D, 14.0D);

    public MagmaticAnvil()
    {
        this(Properties.create(Material.IRON).harvestLevel(0).sound(SoundType.ANVIL).hardnessAndResistance(4, 12).notSolid());
    }

    public MagmaticAnvil(Properties properties)
    {
        super(properties);

        this.setDefaultState(this.stateContainer.getBaseState().with(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(BlockStateProperties.FACING, context.getPlacementHorizontalFacing().getOpposite()).with(BlockStateProperties.POWERED, false);
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos)
    {
        return state.get(BlockStateProperties.POWERED) ? 10 : 0;
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new MagmaticAnvilTile();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(BlockStateProperties.FACING);
        builder.add(BlockStateProperties.POWERED);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context)
    {
        return shape;
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public ActionResultType onBlockActivated(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof MagmaticAnvilTile)
        {
            MagmaticAnvilTile anvil = (MagmaticAnvilTile) te;
            AtomicBoolean success = new AtomicBoolean(false);

            // Extract item
            if (player.isSneaking())
            {
                // Reagent
                anvil.reagents.ifPresent(h ->
                {
                    ItemStack extract = h.extractLastItem(true);
                    if (!extract.isEmpty())
                    {
                        if (player.addItemStackToInventory(extract))
                        {
                            h.extractLastItem(false);
                            worldIn.playSound(player, pos, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.BLOCKS, 1f, MathHelper.lerp(worldIn.rand.nextFloat(), 0.9f, 1.1f));
                            success.set(true);
                        }
                    }
                });
                if (success.get())
                    return ActionResultType.SUCCESS;

                // Diagram
                anvil.diagram.ifPresent(d ->
                {
                    if (!d.getStackInSlot(0).isEmpty())
                    {
                        ItemStack extract = d.extractItem(0, 1, true);
                        if (player.canPickUpItem(extract))
                        {
                            extract = d.extractItem(0, 1, false);
                            player.inventory.addItemStackToInventory(extract);
                            worldIn.playSound(player, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1f, MathHelper.lerp(worldIn.rand.nextFloat(), 0.9f, 1.1f));
                            success.set(true);
                        }
                    }
                });
                if (success.get())
                    return ActionResultType.SUCCESS;
            }

            ItemStack held = player.getHeldItemMainhand();

            // If output is occupied, always extract
            anvil.output.ifPresent(h ->
            {
                if (!h.getStackInSlot(0).isEmpty())
                {
                    ItemStack extract = h.extractItem(0, 1, true);
                    if (player.addItemStackToInventory(extract))
                    {
                        h.extractItem(0, 1, false);
                        success.set(true);
                        worldIn.playSound(player, pos, SoundEvents.ITEM_ARMOR_EQUIP_IRON, SoundCategory.BLOCKS, 1f, MathHelper.lerp(worldIn.rand.nextFloat(), 0.9f, 1.1f));
                    }
                }
            });
            if (success.get())
                return ActionResultType.SUCCESS;

            // Swap diagram
            if (held.getItem() instanceof Diagram)
            {
                anvil.diagram.ifPresent(d ->
                {
                    if (!d.getStackInSlot(0).isEmpty())
                    {
                        ItemStack extract = d.extractItem(0, 1, true);
                        if (player.inventory.addItemStackToInventory(extract))
                        {
                            ItemStack insert = held.copy();
                            insert.setCount(1);
                            d.setStackInSlot(0, insert);
                            held.setCount(held.getCount() - 1);
                            worldIn.playSound(player, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1f, MathHelper.lerp(worldIn.rand.nextFloat(), 0.9f, 1.1f));
                            success.set(true);
                        }
                    }
                });
            }
            if (success.get())
                return ActionResultType.SUCCESS;

            // Hammer hit
            if (held.getItem().equals(ModItems.BALL_PEEN_HAMMER.get()))
            {
                boolean didCraft = anvil.hammerHit();
                worldIn.playSound(player, pos, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 0.75f, MathHelper.lerp(worldIn.rand.nextFloat(), 0.8f, 1.2f));
                if (didCraft)
                    worldIn.playSound(player, pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1f, 1f);
                return ActionResultType.SUCCESS;
            }


            // Empty tank
            if (held.getItem().equals(Items.BUCKET))
            {
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(t ->
                {
                    if (t.drain(1000, IFluidHandler.FluidAction.SIMULATE).getAmount() >= 1000)
                    {
                        ItemStack extract = new ItemStack(Items.LAVA_BUCKET, 1);
                        if (player.inventory.addItemStackToInventory(extract))
                        {
                            held.setCount(held.getCount() - 1);
                            t.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                            worldIn.playSound(player, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1f, 1f);
                        }
                    }
                });

                return ActionResultType.SUCCESS;
            }

            // Fill tank
            if (held.getItem().equals(Items.LAVA_BUCKET))
            {
                te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).ifPresent(t ->
                {
                    if (t.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.SIMULATE) == 1000)
                    {
                        ItemStack extract = new ItemStack(Items.BUCKET, 1);
                        if (player.inventory.addItemStackToInventory(extract))
                        {
                            held.setCount(held.getCount() - 1);
                            t.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
                            worldIn.playSound(player, pos, SoundEvents.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1f, 1f);
                        }
                    }
                });

                return ActionResultType.SUCCESS;
            }

            // Add diagram
            AtomicBoolean diagramEmpty = new AtomicBoolean(false);
            anvil.diagram.ifPresent(h -> diagramEmpty.set(h.getStackInSlot(0).isEmpty()));
            if (diagramEmpty.get() && held.getItem() instanceof Diagram)
            {
                anvil.diagram.ifPresent(h ->
                {
                    ItemStack stackIn = held.copy();
                    ItemStack stackOut = held.copy();
                    stackIn.setCount(1);
                    stackOut.setCount(held.getCount() - 1);

                    h.insertItem(0, stackIn, false);
                    player.setHeldItem(Hand.MAIN_HAND, stackOut);
                    success.set(true);
                    worldIn.playSound(player, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1f, MathHelper.lerp(worldIn.rand.nextFloat(), 0.9f, 1.1f));
                });
            }
            if (success.get())
                return ActionResultType.SUCCESS;

            // Add reagent
            anvil.reagents.ifPresent(h ->
            {
                ItemStack stackIn = held.copy();
                stackIn.setCount(1);
                if (!held.isEmpty() && h.insertItem(stackIn, true).isEmpty())
                {
                    ItemStack stackOut = held.copy();
                    stackOut.setCount(held.getCount() - 1);

                    h.insertItem(stackIn, false);
                    player.setHeldItem(Hand.MAIN_HAND, stackOut);
                    success.set(true);
                }
            });
            if (success.get())
                return ActionResultType.SUCCESS;
        }

        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }
}
