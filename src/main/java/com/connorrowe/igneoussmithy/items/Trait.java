package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;

import javax.annotation.Nonnull;
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
        create("malleable", "Malleable", TraitEvent.damageItem, 0xCD7F32, (stack, player, other, world) ->
        {
            if (rand.nextBoolean())
            {
                stack.attemptDamageItem(-1, rand, null);
            }

            return true;
        });

        create("brittle", "Brittle", TraitEvent.damageItem, 0xAFEEEE, (stack, player, other, world) ->
        {
            if (rand.nextFloat() < .25f)
            {
                stack.attemptDamageItem(10, rand, null);
            }

            return true;
        });

        create("mending", "Mending", TraitEvent.inventoryTick, 0x556B2F, (stack, player, other, world) ->
        {
            float baseHealRate = .001f;
            if (world != null && world.isRaining())
                baseHealRate *= 2;

            if (rand.nextFloat() < baseHealRate)
            {
                stack.attemptDamageItem(-1, rand, null);
            }

            return true;
        });
    }

    public String name;
    public TraitEvent event;
    public int colour;
    public ITraitConsumer traitConsumer;

    private static void create(String id, String name, TraitEvent traitEvent, int colour, ITraitConsumer traitConsumer)
    {
        Trait newTrait = new Trait();
        newTrait.name = name;
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

    public ITextComponent toTextComponent()
    {

        return new TextComponent()
        {
            @Nonnull
            @Override
            public TextComponent copyRaw()
            {
                return this;
            }

            @Nonnull
            @Override
            public String getUnformattedComponentText()
            {
                return name;
            }

            @Nonnull
            @Override
            public Style getStyle()
            {
                return Style.EMPTY.setColor(Color.func_240743_a_(colour));
            }
        };
    }

    public static class TraitEvent
    {
        public static final TraitEvent onBlockDestroyed = create("onBlockDestroyed");
        public static final TraitEvent hitEntity = create("hitEntity");
        public static final TraitEvent onItemUse = create("onItemUse");
        public static final TraitEvent onItemRightClick = create("onItemRightClick");
        public static final TraitEvent damageItem = create("damageItem");
        public static final TraitEvent inventoryTick = create("inventoryTick");

        private String id;

        private static TraitEvent create(String id)
        {
            TraitEvent traitEvent = new TraitEvent();
            traitEvent.id = id;

            return traitEvent;
        }
    }
}
