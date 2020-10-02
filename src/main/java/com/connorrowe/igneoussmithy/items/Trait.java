package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.*;

public class Trait
{
    private static final Map<String, Trait> MAP = Collections.synchronizedMap(new LinkedHashMap<>());
    private static final Random rand = new Random();

    static
    {
        rand.setSeed(IgneousSmithy.MODID.length());
    }

    public static void register()
    {
        create("malleable", "trait.igneoussmithy.malleable.name", "trait.igneoussmithy.malleable.desc", TraitEvent.damageItem, 0xCD7F32, (stack, player, other, world, value) ->
        {
            if (rand.nextBoolean())
            {
                stack.attemptDamageItem(-1, rand, null);
            }

            return 1f;
        });

        create("brittle", "trait.igneoussmithy.brittle.name", "trait.igneoussmithy.brittle.desc", TraitEvent.damageItem, 0xAFEEEE, (stack, player, other, world, value) ->
        {
            if (rand.nextFloat() < .25f)
            {
                stack.attemptDamageItem(10, rand, null);
            }

            return 1f;
        });

        create("mending", "trait.igneoussmithy.mending.name", "trait.igneoussmithy.mending.desc", TraitEvent.inventoryTick, 0x556B2F, (stack, player, other, world, value) ->
        {
            float baseHealRate = .001f;
            if (world != null && world.isRaining())
                baseHealRate *= 2;

            if (rand.nextFloat() < baseHealRate)
            {
                stack.attemptDamageItem(-1, rand, null);
            }

            return 1f;
        });

        create("treasure", "trait.igneoussmithy.treasure.name", "trait.igneoussmithy.treasure.desc", TraitEvent.onBlockDestroyed, 0x191970, (stack, player, other, world, value) ->
        {
            ImmutableList<Item> possibleItems = ImmutableList.of(Items.COAL, Items.IRON_INGOT, Items.GOLD_NUGGET, Items.LAPIS_LAZULI, Items.REDSTONE);
            float baseChance = 0.01f;
            if (world != null)
            {
                if (rand.nextFloat() < baseChance)
                {
                    player.entityDropItem(possibleItems.get(randRange(0, possibleItems.size())));
                }
            }

            return 1f;
        });

        create("jagged", "trait.igneoussmithy.jagged.name", "trait.igneoussmithy.jagged.desc", TraitEvent.calcAttackDamage, 0xF0FFFF, (stack, player, other, world, value) ->
        {
            float damageOut = value;
            float baseChance = 0.25f;

            if (rand.nextFloat() < baseChance)
                damageOut *= 1.25;

            return damageOut;
        });
    }

    public String nameKey;
    public String descriptionKey;
    public TraitEvent event;
    public int colour;
    public ITraitConsumer traitConsumer;

    private static void create(String id, String nameKey, String descriptionKey, TraitEvent traitEvent, int colour, ITraitConsumer traitConsumer)
    {
        Trait newTrait = new Trait();
        newTrait.nameKey = nameKey;
        newTrait.descriptionKey = descriptionKey;
        newTrait.event = traitEvent;
        newTrait.colour = colour;
        newTrait.traitConsumer = traitConsumer;

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
        return new TranslationTextComponent(nameKey).setStyle(Style.EMPTY.setColor(Color.func_240743_a_(colour)));
    }

    public ITextComponent getDescription()
    {
        return new TranslationTextComponent(descriptionKey).setStyle(Style.EMPTY.setColor(Color.func_240743_a_(colour)));
    }

    public static class TraitEvent
    {
        public static final TraitEvent onBlockDestroyed = create("onBlockDestroyed");
        public static final TraitEvent hitEntity = create("hitEntity");
        public static final TraitEvent onItemUse = create("onItemUse");
        public static final TraitEvent onItemRightClick = create("onItemRightClick");
        public static final TraitEvent damageItem = create("damageItem");
        public static final TraitEvent inventoryTick = create("inventoryTick");
        public static final TraitEvent calcAttackDamage = create("calcAttackDamage");

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
}
