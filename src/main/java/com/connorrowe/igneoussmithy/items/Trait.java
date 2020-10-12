package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;

import javax.annotation.Nullable;
import java.util.*;

public class Trait
{
    private static final Map<String, Trait> MAP = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Random rand = new Random();
    private static final WeightedList<Item> treasureLootTable = new WeightedList<>();

    static
    {
        rand.setSeed(IgneousSmithy.MODID.length());

        treasureLootTable.func_226314_a_(rand);
        treasureLootTable.func_226313_a_(Items.COAL, 40);
        treasureLootTable.func_226313_a_(Items.BONE, 40);
        treasureLootTable.func_226313_a_(Items.GOLD_NUGGET, 10);
        treasureLootTable.func_226313_a_(Items.IRON_NUGGET, 15);
        treasureLootTable.func_226313_a_(Items.LAPIS_LAZULI, 8);
        treasureLootTable.func_226313_a_(Items.REDSTONE, 8);
        treasureLootTable.func_226313_a_(Items.DIAMOND, 1);
    }

    public static void register()
    {
        create("malleable", "trait.igneoussmithy.malleable.name", "trait.igneoussmithy.malleable.desc", TraitEvent.damageItem, 0xCD7F32, 3, (stack, traitLevel, player, other, world, value, object) ->
        {
            if (rand.nextBoolean())
            {
                stack.attemptDamageItem(-1, rand, null);
            }

            return 1f;
        });

        create("brittle", "trait.igneoussmithy.brittle.name", "trait.igneoussmithy.brittle.desc", TraitEvent.damageItem, 0xAFEEEE, 1, (stack, traitLevel, player, other, world, value, object) ->
        {
            if (rand.nextFloat() < .25f * traitLevel)
            {
                stack.attemptDamageItem(10, rand, null);
            }

            return 1f;
        });

        create("mending", "trait.igneoussmithy.mending.name", "trait.igneoussmithy.mending.desc", TraitEvent.inventoryTick, 0x556B2F, 2, (stack, traitLevel, player, other, world, value, object) ->
        {
            float baseHealRate = .001f * traitLevel;
            if (world != null && world.isRaining())
                baseHealRate *= 2;

            if (rand.nextFloat() < baseHealRate)
            {
                stack.attemptDamageItem(-1, rand, null);
            }

            return 1f;
        });

        create("treasure", "trait.igneoussmithy.treasure.name", "trait.igneoussmithy.treasure.desc", TraitEvent.onBlockDestroyed, 0x191970, 1, (stack, traitLevel, player, other, world, value, object) ->
        {
            float baseChance = 0.015f;
            if (world != null && player != null)
            {
                if (rand.nextFloat() < baseChance)
                {
                    player.entityDropItem(treasureLootTable.func_226318_b_(rand));
                }
            }

            return 1f;
        });

        create("jagged", "trait.igneoussmithy.jagged.name", "trait.igneoussmithy.jagged.desc", TraitEvent.calcAttackDamage, 0xF0FFFF, 3, (stack, traitLevel, player, other, world, value, object) ->
        {
            float damageOut = value;
            float baseChance = 0.25f;

            if (rand.nextFloat() < baseChance)
                damageOut *= (1.25 + (.25 * traitLevel));

            return damageOut;
        });

        create("on_fire", "trait.igneoussmithy.on_fire.name", "trait.igneoussmithy.on_fire.desc", TraitEvent.onBlockDestroyed, 0xFF8C00, 1, (stack, traitLevel, player, other, world, value, object) ->
        {
            float baseChance = 0.08f;

            if (rand.nextFloat() < baseChance && world != null && !world.isRemote && object instanceof BlockPos)
            {
                BlockPos pos = (BlockPos) object;

                List<Direction> randDirs = Arrays.asList(Direction.values().clone());
                Collections.shuffle(randDirs, rand);

                for (Direction dir : randDirs)
                {
                    BlockPos offset = pos.offset(dir);
                    if (world.getBlockState(offset).isAir(world, offset) && !world.getBlockState(offset.down()).isAir(world, offset.down()) && dir != Direction.UP)
                    {
                        world.setBlockState(offset, Blocks.FIRE.getDefaultState());
                        break;
                    }
                }
            }

            return 1f;
        });

        create("hot_head", "trait.igneoussmithy.hot_head.name", "trait.igneoussmithy.hot_head.desc", TraitEvent.onAddedToItem, 0xFF8C00, 1, (stack, traitLevel, player, other, world, value, object) ->
        {

            if (value > 0f)
            {
                Map<Enchantment, Integer> stackEnchants = EnchantmentHelper.getEnchantments(stack);
                stackEnchants.put(Enchantments.FIRE_ASPECT, traitLevel);

                EnchantmentHelper.setEnchantments(stackEnchants, stack);
            } else
            {
                // remove enchant
            }
            return 1f;
        });

        create("fortune", "trait.igneoussmithy.fortune.name", "trait.igneoussmithy.fortune.desc", TraitEvent.onAddedToItem, 0x87CEEB, 3, (stack, traitLevel, player, other, world, value, object) ->
        {
            Map<Enchantment, Integer> stackEnchants = EnchantmentHelper.getEnchantments(stack);

            if (value > 0f)
            {
                stackEnchants.put(Enchantments.FORTUNE, traitLevel);
            } else
            {
                // remove enchant
                stackEnchants.remove(Enchantments.FORTUNE);
            }

            EnchantmentHelper.setEnchantments(stackEnchants, stack);

            return 1f;
        });
    }

    public String nameKey;
    public String descriptionKey;
    public TraitEvent event;
    public int colour;
    public ITraitConsumer traitConsumer;
    public int maxLevels;
    public int currentLevel;

    private static void create(String id, String nameKey, String descriptionKey, TraitEvent traitEvent, int colour, int maxLevels, ITraitConsumer traitConsumer)
    {
        Trait newTrait = new Trait();
        newTrait.nameKey = nameKey;
        newTrait.descriptionKey = descriptionKey;
        newTrait.event = traitEvent;
        newTrait.colour = colour;
        newTrait.traitConsumer = traitConsumer;
        newTrait.currentLevel = 1;
        newTrait.maxLevels = maxLevels;

        MAP.put(id, newTrait);
    }

    public static List<Trait> getValues()
    {
        synchronized (MAP)
        {
            return new ArrayList<>(MAP.values());
        }
    }

    @Nullable
    public static Trait get(@Nullable String id)
    {
        if (id == null) return null;

        synchronized (MAP)
        {
            return MAP.get(id);
        }
    }

    public ITextComponent getName()
    {
        return new TranslationTextComponent(nameKey).setStyle(Style.EMPTY.setColor(Color.func_240743_a_(colour))).append(new StringTextComponent(maxLevels > 1 ? (" " + toRomanNumeral(currentLevel)) : ""));
    }

    public ITextComponent getDescription()
    {
        return new TranslationTextComponent(descriptionKey).setStyle(Style.EMPTY.setColor(Color.func_240743_a_(colour)));
    }

    public Trait copy()
    {
        Trait traitCopy = new Trait();
        traitCopy.nameKey = this.nameKey;
        traitCopy.descriptionKey = this.descriptionKey;
        traitCopy.event = this.event;
        traitCopy.colour = this.colour;
        traitCopy.traitConsumer = this.traitConsumer;
        traitCopy.currentLevel = this.currentLevel;
        traitCopy.maxLevels = this.maxLevels;

        return traitCopy;
    }

    public static class TraitEvent
    {
        public static final TraitEvent onBlockDestroyed = create("onBlockDestroyed");
        public static final TraitEvent hitEntity = create("hitEntity");
        public static final TraitEvent onItemUse = create("onItemUse");
        public static final TraitEvent onItemRightClick = create("onItemRightClick");
        public static final TraitEvent damageItem = create("damageItem");
        public static final TraitEvent inventoryTick = create("inventoryTick");
        // Value is base damage, return is modified damage out
        public static final TraitEvent calcAttackDamage = create("calcAttackDamage");
        // Value will be > 0 when added, and 0 when removed
        public static final TraitEvent onAddedToItem = create("calcAttackDamage");
        public static final TraitEvent none = create("none");

        private String id;

        private static TraitEvent create(String id)
        {
            TraitEvent traitEvent = new TraitEvent();
            traitEvent.id = id;

            return traitEvent;
        }
    }

    private static int randRange(int min, int max)
    {
        return (int) ((rand.nextDouble() * (max - min)) + min);
    }

    private static String toRomanNumeral(int val)
    {
        if (val == 1)
            return "I";
        else if (val == 2)
            return "II";
        else if (val == 3)
            return "III";
        else if (val == 4)
            return "IV";
        else if (val == 5)
            return "V";
        else if (val == 6)
            return "VI";
        else if (val == 7)
            return "VII";
        else if (val == 8)
            return "VIII";
        else if (val == 9)
            return "IX";
        else if (val == 10)
            return "X";
        else
            return String.valueOf(val);
    }
}
