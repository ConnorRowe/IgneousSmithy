package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.data.MaterialManager;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("SpellCheckingInspection")
public class DynamicTool extends ToolItem
{
    private static final String NBT_MATERIAL_NAMESPACE = "mat_namespace";
    private static final String NBT_MATERIAL_PATH = "mat_path";
    private static final String NBT_MAT_RESOURCE_LOCATIONS = "mat_res_locs";
    private static final String NBT_BROKEN = "broken";
    private static final String NBT_MODIFIERS = "modifiers";
    private static final String NBT_MAX_MODIFIERS = "max_modifiers";

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

    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker)
    {
        if (this.toolType.equals(ToolType.SWORD))
        {
            if (!getBroken(stack) && stack.getItem() instanceof DynamicTool && !attacker.world.isRemote)
            {
                getAllTraitsForEvent(stack, Trait.TraitEvent.hitEntity).forEach(t -> t.traitConsumer.execute(stack, attacker, target, attacker.world, 0));

                AtomicReference<Float> damage = new AtomicReference<>(DynamicTool.getHeadMat(stack).attackDamage);

                getAllTraitsForEvent(stack, Trait.TraitEvent.calcAttackDamage).forEach(t -> damage.set(t.traitConsumer.execute(stack, attacker, target, attacker.world, damage.get())));

                if (attacker instanceof PlayerEntity)
                    target.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) attacker), damage.get());
                else
                    target.attackEntityFrom(DamageSource.GENERIC, damage.get());
            }
            return true;
        } else
            return super.hitEntity(stack, target, attacker);
    }

    public static void initialiseStack(ItemStack stack)
    {
        CompoundNBT compound = stack.getOrCreateTag();

        compound.putBoolean(NBT_BROKEN, false);
        compound.put(NBT_MODIFIERS, new ListNBT());
        compound.putInt(NBT_MAX_MODIFIERS, 3);
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

    public static boolean addModifier(ItemStack stack, Modifier modifier)
    {
        boolean success = false;

        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_MODIFIERS))
            tag.put(NBT_MODIFIERS, new ListNBT());

        ListNBT modifiers = tag.getList(NBT_MODIFIERS, 10);

        if (modifiers.size() < tag.getInt(NBT_MAX_MODIFIERS))
        {
            CompoundNBT compound = new CompoundNBT();
            compound.putString("name", modifier.name);

            modifiers.add(compound);
            success = true;
        }

        return success;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, BlockState state, BlockPos pos, LivingEntity entityLiving)
    {
        getAllTraitsForEvent(stack, Trait.TraitEvent.onBlockDestroyed).forEach(trait -> trait.traitConsumer.execute(stack, entityLiving, null, worldIn, 0));

        return super.onBlockDestroyed(stack, worldIn, state, pos, entityLiving);
    }

    public static NonNullList<Modifier> getModifiers(ItemStack stack)
    {
        if (!stack.getOrCreateTag().contains(NBT_MODIFIERS))
            stack.getOrCreateTag().put(NBT_MODIFIERS, new ListNBT());

        ListNBT modifiers = stack.getOrCreateTag().getList(NBT_MODIFIERS, 10);
        NonNullList<Modifier> modifiersOut = NonNullList.withSize(modifiers.size(), Modifier.NULL);

        for (int i = 0; i < modifiers.size(); i++)
        {
            CompoundNBT modifier = modifiers.getCompound(i);
            modifiersOut.set(i, Modifier.get(modifier.getString("name")));
        }

        return modifiersOut;
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
                t.traitConsumer.execute(stack, null, entityIn, worldIn, 0));
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
                t.traitConsumer.execute(stack, entity, null, null, 0));

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

        if (getToolTypes(stack).contains(state.getHarvestTool()) || state.getHarvestTool() == null)
            return getHeadMat(stack).efficiency;
        return toolType.effectiveBlocks.contains(state.getBlock()) ? getHeadMat(stack).efficiency : 1.0F;
    }

    @Nonnull
    @Override
    public Set<net.minecraftforge.common.ToolType> getToolTypes(@Nonnull ItemStack stack)
    {
        return toolType.forgeType == null ? Collections.emptySet() : ImmutableSet.of(toolType.forgeType);
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
        return new StringTextComponent(getHeadMat(stack).name.getString()).appendString(" ")
                .append(new TranslationTextComponent(toolType.nameKey));

    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
    {
        if (!getHeadMat(stack).equals(MaterialManager.FAKE_MAT) && getHeadMat(stack) != null)
        {
            Style shiftStyle = Style.EMPTY.setColor(Color.func_240743_a_(0x808080)).setItalic(true);
            Style brokenStyle = Style.EMPTY.setColor(Color.func_240743_a_(0x8B0000)).setItalic(true).setBold(true);
            Style durabilityStyle = Style.EMPTY.setColor(com.connorrowe.igneoussmithy.tools.ColourHelper.lerpColours(0x98FB98, 0x8B0000, ((float) stack.getDamage()) / ((float) stack.getMaxDamage())));

            ITextComponent shiftUp = new TranslationTextComponent("tooltip.igneoussmithy.view_stats").setStyle(shiftStyle);
            ITextComponent altUp = new TranslationTextComponent("tooltip.igneoussmithy.view_traits").setStyle(shiftStyle);
            ITextComponent broken = new TranslationTextComponent("tooltip.igneoussmithy.broken").setStyle(brokenStyle);

            ITextComponent durability = new TranslationTextComponent("tooltip.igneoussmithy.durability", stack.getMaxDamage() - stack.getDamage(), stack.getMaxDamage()).setStyle(durabilityStyle);

            boolean hasTraits = getAllTraits(stack).size() > 0;

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

                tooltip.add(materials.get(2).name.deepCopy().appendString(" ").append(new TranslationTextComponent(toolType.equals(ToolType.SWORD) ? "part.igneoussmithy.blade" : "part.igneoussmithy.head")));
                tooltip.addAll(ToolPart.getMaterialTooltip(materials.get(2), PartType.HEAD));
                tooltip.add(materials.get(1).name.deepCopy().appendString(" ").append(new TranslationTextComponent("part.igneoussmithy.binding")));
                tooltip.addAll(ToolPart.getMaterialTooltip(materials.get(1), PartType.BINDING));
                tooltip.add(materials.get(0).name.deepCopy().appendString(" ").append(new TranslationTextComponent("part.igneoussmithy.handle")));
                tooltip.addAll(ToolPart.getMaterialTooltip(materials.get(0), PartType.HANDLE));
            } else if (Screen.hasAltDown() && hasTraits)
            {
                getAllTraits(stack).forEach(t -> tooltip.add(t.getDescription()));
            } else
            {
                getAllTraits(stack).forEach(t -> tooltip.add(t.getName()));
                tooltip.add(shiftUp);
                if (hasTraits)
                    tooltip.add(altUp);
            }
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

        getModifiers(stack).forEach(m ->
        {
            if (!traits.contains(m.trait))
            {
                traits.add(m.trait);
            }
        });

        return traits;
    }

    public List<Trait> getAllTraitsForEvent(ItemStack stack, Trait.TraitEvent traitEvent)
    {
        return getAllTraits(stack).stream().filter(t -> t.event.equals(traitEvent)).collect(Collectors.toList());
    }
}
