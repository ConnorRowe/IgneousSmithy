package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.tools.IgneousUtils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ToolItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("SpellCheckingInspection")
public class DynamicTool extends ToolItem
{
    private static final String NBT_MATERIAL_NAMESPACE = "mat_namespace";
    private static final String NBT_MATERIAL_PATH = "mat_path";
    private static final String NBT_MAT_RESOURCE_LOCATIONS = "mat_res_locs";
    private static final String NBT_BROKEN = "broken";

    public ToolType toolType;
    private final NonNullList<ToolLayer> layers;

    public DynamicTool(ToolType toolType, ToolLayer... layers)
    {
        super(1, -2.8f, ItemTier.WOOD, toolType.effectiveBlocks, new Properties().group(IgneousSmithy.IgneousGroup.instance));
        this.toolType = toolType;
        this.layers = NonNullList.from(new ToolLayer("", null), layers);
    }

    @Override
    public boolean canHarvestBlock(ItemStack stack, BlockState blockIn)
    {
        int i = getHeadMat(stack).harvestLevel;
        if (blockIn.getHarvestTool() == toolType.forgeType)
        {
            return i >= blockIn.getHarvestLevel();
        } else
        {
            net.minecraft.block.material.Material material = blockIn.getMaterial();
            return material == net.minecraft.block.material.Material.ROCK || material == net.minecraft.block.material.Material.IRON || material == net.minecraft.block.material.Material.ANVIL;
        }
    }

    public static void initialiseStack(ItemStack stack)
    {
        CompoundNBT compound = stack.getOrCreateTag();

        compound.putBoolean(NBT_BROKEN, false);
    }

    public static void setMaterials(ItemStack stack, Material headMaterial, Material bindMaterial, Material handleMaterial)
    {
        ListNBT matResourceLocsNBT = new ListNBT();

        for (int i = 0; i < 3; i++)
        {
            Material mat;
            switch (i)
            {
                case 0:
                    mat = handleMaterial;
                    break;
                case 1:
                    mat = bindMaterial;
                    break;
                case 2:
                    mat = headMaterial;
                    break;
                default:
                    mat = Material.DEFAULT;
                    break;
            }

            CompoundNBT resourceNBT = new CompoundNBT();
            resourceNBT.putString(NBT_MATERIAL_NAMESPACE, mat.getId().getNamespace());
            resourceNBT.putString(NBT_MATERIAL_PATH, mat.getId().getPath());

            matResourceLocsNBT.add(resourceNBT);
        }

        stack.getOrCreateTag().put(NBT_MAT_RESOURCE_LOCATIONS, matResourceLocsNBT);
    }

    public static NonNullList<Material> getMaterials(ItemStack stack)
    {
        CompoundNBT compound = stack.getOrCreateTag();

        ListNBT NBTMatColours = compound.getList(NBT_MAT_RESOURCE_LOCATIONS, 10);

        NonNullList<Material> materials = NonNullList.withSize(NBTMatColours.size(), Material.DEFAULT);
        for (int i = 0; i < NBTMatColours.size(); i++)
        {
            CompoundNBT resourceNBT = NBTMatColours.getCompound(i);

            ResourceLocation id = new ResourceLocation(resourceNBT.getString(NBT_MATERIAL_NAMESPACE), resourceNBT.getString(NBT_MATERIAL_PATH));
            materials.set(i, MaterialManager.get(id));
        }

        return materials;
    }

    public static Material getMaterial(ItemStack stack, int num)
    {
        CompoundNBT resourceNBT = stack.getOrCreateTag().getList(NBT_MAT_RESOURCE_LOCATIONS, 10).getCompound(num);

        return MaterialManager.get(new ResourceLocation(resourceNBT.getString(NBT_MATERIAL_NAMESPACE), resourceNBT.getString(NBT_MATERIAL_PATH)));
    }

    public static Material getHeadMat(ItemStack stack)
    {
        return getMaterial(stack, 2);
    }

    public static Material getBindMat(ItemStack stack)
    {
        return getMaterial(stack, 1);
    }

    public static Material getHandMat(ItemStack stack)
    {
        return getMaterial(stack, 0);
    }

    public static NonNullList<ToolLayer> getLayers(ItemStack stack)
    {
        if (stack.getItem() instanceof DynamicTool)
        {
            return ((DynamicTool) stack.getItem()).getLayers();
        } else
            return NonNullList.create();
    }

    public NonNullList<ToolLayer> getLayers()
    {
        return this.layers;
    }

    public static boolean getBroken(ItemStack stack)
    {
        CompoundNBT compound = stack.getOrCreateTag();

        return compound.getBoolean(NBT_BROKEN);
    }

    @Nullable
    public static ToolType getToolType(ItemStack stack)
    {
        if (!(stack.getItem() instanceof DynamicTool))
            return null;

        return ((DynamicTool) stack.getItem()).toolType;
    }

    @Override
    public void setDamage(ItemStack stack, int damage)
    {
        super.setDamage(stack, damage);
        int dmg = getDamage(stack);
        int maxdmg = getMaxDamage(stack);

        setBroken(stack, dmg >= maxdmg - 1);
    }


    public static void setBroken(ItemStack stack, boolean value)
    {
        CompoundNBT compound = stack.getOrCreateTag();

        compound.putBoolean(NBT_BROKEN, value);
    }

    @Override
    public int getMaxDamage(ItemStack stack)
    {
        return Math.max(1, ((int) ((((float) getHeadMat(stack).durability) * getBindMat(stack).bindingMultiplier) + getBindMat(stack).bindingDurability)));
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull Entity entityIn, int itemSlot, boolean isSelected)
    {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);

        getAllTraitsForEvent(stack, Trait.TraitEvent.inventoryTick).forEach(t ->
                t.traitConsumer.execute(stack, null, entityIn, worldIn));
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken)
    {
        if (stack.getDamage() + amount >= stack.getMaxDamage())
        {
            setBroken(stack, true);
            return 0;
        }

        getAllTraitsForEvent(stack, Trait.TraitEvent.damageItem).forEach(t ->
                t.traitConsumer.execute(stack, entity, null, null));

        return amount;
    }

    @Override
    public boolean isRepairable(@Nonnull ItemStack stack)
    {
        return true;
    }

    @Override
    public boolean getIsRepairable(@Nonnull ItemStack toRepair, ItemStack repair)
    {
        return MaterialManager.getRecipeResult(repair.getItem()) != Material.DEFAULT;
    }

    public static void repairStack(ItemStack stack, int amount)
    {
        stack.setDamage(stack.getDamage() - amount);
    }

    public static String[] getMatTextures(ItemStack stack)
    {
        String[] stringsOut = new String[3];

        NonNullList<Material> materials = getMaterials(stack);

        for (int i = 0; i < materials.size(); i++)
        {
            stringsOut[i] = materials.get(i).texture;
        }

        return stringsOut;
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state)
    {
        if (getBroken(stack))
            return 0.1f;

        if (getToolTypes(stack).stream().anyMatch(state::isToolEffective)) return getHeadMat(stack).efficiency;
        return toolType.effectiveBlocks.contains(state.getBlock()) ? getHeadMat(stack).efficiency : 1.0F;
    }

    @Nonnull
    @Override
    public Set<net.minecraftforge.common.ToolType> getToolTypes(@Nonnull ItemStack stack)
    {
        return ImmutableSet.of(toolType.forgeType);
    }

    @Override
    public int getItemEnchantability()
    {
        return super.getItemEnchantability();
    }

    @Override
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull net.minecraftforge.common.ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState)
    {
        return getHeadMat(stack).harvestLevel;
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items)
    {
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack)
    {
        return new StringTextComponent(getHeadMat(stack).name.getString() + " " + IgneousUtils.CapitaliseString(toolType.name));
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        Style shiftStyle = Style.EMPTY.setColor(Color.func_240743_a_(0x808080)).setItalic(true);
        Style brokenStyle = Style.EMPTY.setColor(Color.func_240743_a_(0x8B0000)).setItalic(true).setBold(true);

        TextComponent shiftUp = new TextComponent()
        {
            @Nonnull
            @Override
            public TextComponent copyRaw()
            {
                return this;
            }

            @Nonnull
            @Override
            public Style getStyle()
            {
                return shiftStyle;
            }

            @Override
            public String getUnformattedComponentText()
            {
                return "Hold shift for stats";
            }
        };
        TextComponent altUp = new TextComponent()
        {
            @Nonnull
            @Override
            public TextComponent copyRaw()
            {
                return this;
            }

            @Nonnull
            @Override
            public Style getStyle()
            {
                return shiftStyle;
            }

            @Override
            public String getUnformattedComponentText()
            {
                return "Hold alt for traits";
            }
        };

        TextComponent broken = new TextComponent()
        {
            @Nonnull
            @Override
            public TextComponent copyRaw()
            {
                return this;
            }

            @Override
            public String getUnformattedComponentText()
            {
                return "BROKEN";
            }

            @Nonnull
            @Override
            public Style getStyle()
            {
                return brokenStyle;
            }
        };

        TextComponent durability = new TextComponent()
        {
            @Nonnull
            @Override
            public TextComponent copyRaw()
            {
                return this;
            }

            @Override
            public String getUnformattedComponentText()
            {
                return "Durability: " + (stack.getMaxDamage() - stack.getDamage()) + "/" + stack.getMaxDamage();
            }

            @Override
            public Style getStyle()
            {
                return Style.EMPTY.setColor(com.connorrowe.igneoussmithy.tools.ColourHelper.lerpColours(0x98FB98, 0x8B0000, ((float) stack.getDamage()) / ((float) stack.getMaxDamage())));
            }
        };

        if (getBroken(stack))
        {
            tooltip.add(broken);
        } else
        {
            tooltip.add(durability);
        }

        if (Screen.hasShiftDown())
        {
            NonNullList<Material> materials = getMaterials(stack);

            tooltip.add(new StringTextComponent(materials.get(2).name.getString() + " Head"));
            tooltip.addAll(ToolPart.getMaterialTooltip(materials.get(2), PartType.HEAD));
            tooltip.add(new StringTextComponent(materials.get(1).name.getString() + " Binding"));
            tooltip.addAll(ToolPart.getMaterialTooltip(materials.get(1), PartType.BINDING));
            tooltip.add(new StringTextComponent(materials.get(0).name.getString() + " Handle"));
            tooltip.addAll(ToolPart.getMaterialTooltip(materials.get(0), PartType.HANDLE));
        } else
        {
            getAllTraits(stack).forEach(t -> tooltip.add(t.toTextComponent()));

            tooltip.add(shiftUp);
            //tooltip.add(altUp);
        }

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }

    public static List<Trait> getAllTraits(ItemStack stack)
    {
        List<Trait> traits = new ArrayList<>();
        NonNullList<Material> mats = getMaterials(stack);

        for (int i = 0; i < mats.size(); i++)
        {
            if (i == 2)
            {
                mats.get(i).headOnlyTraits.forEach(t ->
                {
                    if (!traits.contains(t))
                    {
                        traits.add(t);
                    }
                });
            }

            mats.get(i).allTraits.forEach(t ->
            {
                if (!traits.contains(t))
                {
                    traits.add(t);
                }
            });
        }

        return traits;
    }

    public List<Trait> getAllTraitsForEvent(ItemStack stack, Trait.TraitEvent traitEvent)
    {
        return getAllTraits(stack).stream().filter(t -> t.event.equals(traitEvent)).collect(Collectors.toList());
    }
}
