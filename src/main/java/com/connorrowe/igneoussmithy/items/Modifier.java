package com.connorrowe.igneoussmithy.items;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class Modifier
{
    private static final Map<String, Modifier> MAP = Collections.synchronizedMap(new LinkedHashMap<>());

    public static void register()
    {
        create("lucky_charm", Trait.get("treasure"), ImmutableSet.of(ToolType.PICKAXE), new ResourceLocation(IgneousSmithy.MODID, "lucky_charm"), 0xFFFFFF, null);
        create("jagged", Trait.get("jagged"), ImmutableSet.of(ToolType.SWORD), new ResourceLocation(IgneousSmithy.MODID, "jagged_quartz"), 0xFFFFFF, null);
    }

    private static void create(String name, Trait trait, Set<ToolType> applicableTools, ResourceLocation texture, int colour, @Nullable Consumer<ItemStack> modifyStack)
    {
        synchronized (MAP)
        {
            MAP.put(name, new Modifier(name, trait, applicableTools, texture, colour, modifyStack));
        }
    }

    public static final Modifier NULL = new Modifier("", null, null, null, 0, null);

    public final String name;
    public final Trait trait;
    public final Set<ToolType> applicableTools;
    public final ResourceLocation texture;
    public final int colour;
    private final Consumer<ItemStack> modifyStack;

    private Modifier(String name, Trait trait, Set<ToolType> applicableTools, ResourceLocation texture, int colour, @Nullable Consumer<ItemStack> modifyStack)
    {
        this.name = name;
        this.trait = trait;
        this.applicableTools = applicableTools;
        this.texture = texture;
        this.colour = colour;
        this.modifyStack = modifyStack;
    }

    public void executeModifyStack(ItemStack stack)
    {
        if (this.modifyStack != null)
        {
            this.modifyStack.accept(stack);
        }
    }

    @Nullable
    public static Modifier get(@Nullable String name)
    {
        if (name == null) return null;

        synchronized (MAP)
        {
            return MAP.get(name);
        }
    }
}
