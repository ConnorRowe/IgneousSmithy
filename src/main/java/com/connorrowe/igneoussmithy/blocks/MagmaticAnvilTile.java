package com.connorrowe.igneoussmithy.blocks;

import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.items.*;
import com.connorrowe.igneoussmithy.setup.ModItems;
import com.connorrowe.igneoussmithy.setup.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class MagmaticAnvilTile extends TileEntity
{
    private final ReagentHandler reagentHandler = new ReagentHandler();
    private final ItemStackHandler diagramHandler = createHandler();
    private final ItemStackHandler outputHandler = createHandler();
    private final MagmaTank tankHandler = new MagmaTank();

    public final LazyOptional<ItemStackHandler> diagram = LazyOptional.of(() -> diagramHandler);
    public final LazyOptional<ReagentHandler> reagents = LazyOptional.of(() -> reagentHandler);
    public final LazyOptional<ItemStackHandler> output = LazyOptional.of(() -> outputHandler);
    private final LazyOptional<IFluidHandler> tank = LazyOptional.of(() -> tankHandler);

    private int hitCount = 0;

    public MagmaticAnvilTile()
    {
        super(ModTileEntities.MAGMATIC_ANVIL.get());

    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap.equals(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY))
        {
            if (side != null && side.equals(Direction.NORTH))
            {
                return diagram.cast();
            }
            return reagents.cast();
        } else if (cap.equals(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY))
        {
            return tank.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public @Nonnull
    BlockState getBlockState()
    {
        return super.getBlockState().with(BlockStateProperties.POWERED, tankHandler.fluidStack.getAmount() > 0);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        return new SUpdateTileEntityPacket(getPos(), -1, write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
    {
        this.read(getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public void read(@Nonnull BlockState state, @Nonnull CompoundNBT tag)
    {
        super.read(state, tag);

        reagentHandler.deserializeNBT(tag.getCompound("reagents"));
        diagramHandler.deserializeNBT(tag.getCompound("diagram"));
        outputHandler.deserializeNBT(tag.getCompound("output"));
        tankHandler.deserializeNBT(tag.getCompound("tank"));
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT tag)
    {
        tag.put("reagents", reagentHandler.serializeNBT());
        tag.put("diagram", diagramHandler.serializeNBT());
        tag.put("output", outputHandler.serializeNBT());
        tag.put("tank", tankHandler.serializeNBT());

        return super.write(tag);
    }

    // Returns true if the hit resulted in a craft
    public boolean hammerHit()
    {
        AtomicBoolean tooFewReagents = new AtomicBoolean(false);
        reagents.ifPresent(h -> tooFewReagents.set(h.getStackInSlot(0).getCount() < 1));
        if (tooFewReagents.get())
        {
            return false;
        }

        this.hitCount++;
        if (hitCount >= 3 && tankHandler.getFluidInTank(0).getAmount() >= 100)
        {
            resetHitCount();
            return attemptCraft();
        }

        return false;
    }

    public void resetHitCount()
    {
        this.hitCount = 0;
    }

    public boolean attemptCraft()
    {
        AtomicBoolean success = new AtomicBoolean(false);
        reagents.ifPresent(h ->
        {
            if (outputHandler.getStackInSlot(0).isEmpty())
            {
                if (diagramHandler.getStackInSlot(0).getItem() instanceof Diagram)
                {
                    Diagram diagram = (Diagram) diagramHandler.getStackInSlot(0).getItem();

                    // Check diagram crafting recipe
                    for (int i = 0; i < h.getSlots(); i++)
                    {
                        Material mat = MaterialManager.getRecipeResult(h.getStackInSlot(i).getItem());
                        List<Integer> slots = h.containsMultiple(h.getStackInSlot(i).getItem(), diagram.materialCost);
                        if (slots != null && mat != null)
                        {
                            ItemStack craftStack = diagram.craft(mat);

                            for (Integer j : slots)
                            {
                                ItemStack s = h.getStackInSlot(j);
                                s.setCount(s.getCount() - 1);
                            }

                            outputHandler.setStackInSlot(0, craftStack);
                            tankHandler.drain(100, IFluidHandler.FluidAction.EXECUTE);
                            success.set(true);
                            break;
                        }
                    }
                }

                //Dynamic tool craft
                if (!success.get())
                {
                    int headSlot = h.findStack(p -> p.getItem() instanceof ToolHead);
                    if (headSlot < 0)
                        return;

                    int bindSlot = h.findStack(p -> p.getItem() instanceof ToolPart && ((ToolPart) (p.getItem())).getPartType().equals(PartType.BINDING));
                    if (bindSlot < 0)
                        return;

                    int handSlot = h.findStack(p -> p.getItem() instanceof ToolPart && ((ToolPart) (p.getItem())).getPartType().equals(PartType.HANDLE));
                    if (handSlot < 0)
                        return;

                    //Slots should be valid - assemble tool
                    Item tool = Items.AIR;
                    ToolType toolType = ((ToolHead) (h.getStackInSlot(headSlot).getItem())).getToolType();

                    if (toolType.equals(ToolType.PICKAXE))
                        tool = ModItems.DYNAMIC_PICKAXE.get();
                    else if (toolType.equals(ToolType.SHOVEL))
                        tool = ModItems.DYNAMIC_SHOVEL.get();

                    ItemStack craftedStack = new ItemStack(tool);
                    DynamicTool.initialiseStack(craftedStack);
                    DynamicTool.setMaterials(craftedStack, ToolPart.getMaterial(h.getStackInSlot(headSlot)), ToolPart.getMaterial(h.getStackInSlot(bindSlot)), ToolPart.getMaterial(h.getStackInSlot(handSlot)));

                    for (ItemStack stack : h.getStacks())
                    {
                        stack.setCount(stack.getCount() - 1);
                    }

                    outputHandler.setStackInSlot(0, craftedStack);
                    tankHandler.drain(100, IFluidHandler.FluidAction.EXECUTE);
                    success.set(true);
                }

            }
        });

        return success.get();
    }

    // Updates client on load
    @Nonnull
    @Override
    public CompoundNBT getUpdateTag()
    {
        return this.write(super.getUpdateTag());
    }

    private ItemStackHandler createHandler()
    {
        return new ItemStackHandler(1)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                super.onContentsChanged(slot);
                markDirty();
            }

            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack)
            {
                return 1;
            }
        };
    }

    private class MagmaTank implements IFluidHandler
    {
        private final int maxCapacity;
        private FluidStack fluidStack;

        MagmaTank()
        {
            this.maxCapacity = 2000;
            this.fluidStack = new FluidStack(Fluids.LAVA.getFluid(), 0);
        }

        public void onTankChanged()
        {
            if (world != null)
            {
                tank.ifPresent(t ->
                {
                    if (t.getFluidInTank(0).getAmount() > 0)
                    {
                        world.setBlockState(pos, world.getBlockState(pos).with(BlockStateProperties.POWERED, true));
                    } else
                    {
                        world.setBlockState(pos, world.getBlockState(pos).with(BlockStateProperties.POWERED, false));
                    }
                });
            }

            markDirty();
        }

        public CompoundNBT serializeNBT()
        {
            CompoundNBT tag = new CompoundNBT();

            this.fluidStack.writeToNBT(tag);

            return tag;
        }

        public void deserializeNBT(CompoundNBT tag)
        {
            this.fluidStack = FluidStack.loadFluidStackFromNBT(tag);
            onTankChanged();
        }

        @Override
        public int getTanks()
        {
            return 1;
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank)
        {
            return this.fluidStack;
        }

        @Override
        public int getTankCapacity(int tank)
        {
            return this.maxCapacity;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
        {
            return stack.getFluid() == Fluids.LAVA.getFluid();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action)
        {
            if (isFluidValid(0, resource))
            {
                int currentAmount = this.fluidStack.getAmount();
                int space = getTankCapacity(0) - currentAmount;

                int fill = resource.getAmount();

                if (fill > space)
                    return 0;

                if (!action.simulate())
                {
                    if (this.fluidStack.isEmpty())
                        this.fluidStack = new FluidStack(resource.getFluid(), currentAmount);

                    this.fluidStack.setAmount(currentAmount + fill);

                    onTankChanged();
                }

                return fill;
            }

            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action)
        {
            if (isFluidValid(0, resource))
            {
                return drain(resource.getAmount(), action);
            }

            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action)
        {
            FluidStack fluidOut = new FluidStack(Fluids.LAVA, maxDrain);
            int drained = this.fluidStack.getAmount() - maxDrain;

            if (drained < 0)
            {
                fluidOut.setAmount(0);
                return fluidOut;
            }

            if (action.execute())
            {
                if (this.fluidStack.isEmpty())
                    this.fluidStack = new FluidStack(Fluids.LAVA, 0);

                this.fluidStack.setAmount(Math.max(0, this.fluidStack.getAmount() - maxDrain));
                onTankChanged();
            }

            return fluidOut;
        }
    }

    public class ReagentHandler extends ItemStackHandler
    {
        private static final int maxSlots = 3;

        ReagentHandler()
        {
            this.stacks = NonNullList.withSize(3, ItemStack.EMPTY);
        }

        @Override
        protected void validateSlotIndex(int slot)
        {
            super.validateSlotIndex(slot);
        }

        public ItemStack insertItem(ItemStack stack, boolean simulate)
        {
            int slot = this.getFirstFreeSlot();
            if (slot < 0)
            {
                return stack.copy();
            }

            if (!simulate)
                this.setStackInSlot(slot, stack.copy());

            return ItemStack.EMPTY;
        }

        public ItemStack extractLastItem(boolean simulate)
        {
            int slot = this.getLastFilledSlot();
            if (slot < 0)
            {
                return ItemStack.EMPTY;
            }

            if (!simulate)
                this.setStackInSlot(slot, ItemStack.EMPTY);

            return this.getStackInSlot(slot).copy();
        }

        public NonNullList<ItemStack> getStacks()
        {
            return stacks;
        }

        // Returns the slots that contain the item searched for
        @Nullable
        public List<Integer> containsMultiple(Item item, int count)
        {
            List<Integer> slots = new ArrayList<>();
            int slot = 0;
            int itr = 0;
            for (ItemStack stack : stacks)
            {
                if (stack.getItem().equals(item))
                {
                    itr++;
                    slots.add(slot);
                }
                slot++;
            }

            return itr >= count ? slots : null;
        }

        // Returns the first slot found containing the stack, -1 if none found
        public int findStack(Predicate<ItemStack> predicate)
        {
            int itr = 0;
            for (ItemStack stack : stacks)
            {
                if (predicate.test(stack))
                    return itr;

                itr++;
            }

            return -1;
        }

        @Override
        public int getSlots()
        {
            return super.getSlots();
        }

        private int getFirstFreeSlot()
        {
            for (int i = 0; i < maxSlots; i++)
            {
                if (this.getStackInSlot(i).isEmpty())
                    return i;
            }

            return -1;
        }

        private int getLastFilledSlot()
        {
            for (int i = maxSlots - 1; i >= 0; i--)
            {
                if (!this.getStackInSlot(i).isEmpty())
                    return i;
            }

            return -1;
        }

        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack)
        {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot)
        {
            super.onContentsChanged(slot);
            markDirty();
        }
    }
}
