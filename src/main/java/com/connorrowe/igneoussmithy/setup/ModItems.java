package com.connorrowe.igneoussmithy.setup;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.items.*;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Supplier;

public class ModItems
{
    public static final RegistryObject<Item> BALL_PEEN_HAMMER = register("ball_peen_hammer", () -> new Item(defProps(1)));
    public static final RegistryObject<Item> BLAZING_INGOT = register("blazing_ingot", () -> new Item(defProps()));
    public static final RegistryObject<Item> OBSIDIAN_SHARD = register("obsidian_shard", () -> new Item(defProps()));

    public static final RegistryObject<ToolHead> PICKAXE_HEAD = register("pickaxe_head", () -> new ToolHead(ToolType.PICKAXE));
    public static final RegistryObject<ToolHead> SHOVEL_HEAD = register("shovel_head", () -> new ToolHead(ToolType.SHOVEL));
    public static final RegistryObject<ToolHead> HATCHET_HEAD = register("hatchet_head", () -> new ToolHead(ToolType.HATCHET));
    public static final RegistryObject<ToolHead> SWORD_HEAD = register("sword_head", () -> new ToolHead(ToolType.SWORD));
    public static final RegistryObject<ToolPart> BINDING = register("binding", () -> new ToolPart(PartType.BINDING));
    public static final RegistryObject<ToolPart> HANDLE = register("handle", () -> new ToolPart(PartType.HANDLE));

    public static final RegistryObject<DynamicTool> DYNAMIC_PICKAXE = register("dynamic_pickaxe", () -> new DynamicTool(ToolType.PICKAXE, new ToolLayer("handle", 0), new ToolLayer("pickaxe_bind", 1), new ToolLayer("pickaxe_head", 2)));
    public static final RegistryObject<DynamicTool> DYNAMIC_SHOVEL = register("dynamic_shovel", () -> new DynamicTool(ToolType.SHOVEL, new ToolLayer("handle", 0), new ToolLayer("shovel_bind", 1), new ToolLayer("shovel_head", 2)));
    public static final RegistryObject<DynamicTool> DYNAMIC_HATCHET = register("dynamic_hatchet", () -> new DynamicTool(ToolType.HATCHET, new ToolLayer("handle", 0), new ToolLayer("hatchet_bind", 1), new ToolLayer("hatchet_head", 2)));
    public static final RegistryObject<DynamicTool> DYNAMIC_SWORD = register("dynamic_sword", () -> new DynamicTool(ToolType.SWORD, new ToolLayer("sword_handle", 0), new ToolLayer("sword_bind", 1), new ToolLayer("sword_head", 2)));

    public static final RegistryObject<Diagram> DIAGRAM_PICKAXE_HEAD = register("diagram_pickaxe_head", () -> new Diagram(PartType.HEAD, PICKAXE_HEAD::get, 3));
    public static final RegistryObject<Diagram> DIAGRAM_SHOVEL_HEAD = register("diagram_shovel_head", () -> new Diagram(PartType.HEAD, SHOVEL_HEAD::get, 3));
    public static final RegistryObject<Diagram> DIAGRAM_HATCHET_HEAD = register("diagram_hatchet_head", () -> new Diagram(PartType.HEAD, HATCHET_HEAD::get, 3));
    public static final RegistryObject<Diagram> DIAGRAM_SWORD_HEAD = register("diagram_sword_head", () -> new Diagram(PartType.HEAD, SWORD_HEAD::get, 3));
    public static final RegistryObject<Diagram> DIAGRAM_BINDING = register("diagram_binding", () -> new Diagram(PartType.BINDING, BINDING, 1));
    public static final RegistryObject<Diagram> DIAGRAM_HANDLE = register("diagram_handle", () -> new Diagram(PartType.HANDLE, HANDLE, 2));

    public static final RegistryObject<ModifierItem> LUCKY_CHARM = register("lucky_charm", () -> new ModifierItem("lucky_charm"));
    public static final RegistryObject<ModifierItem> JAGGED_QUARTZ = register("jagged_quartz", () -> new ModifierItem("jagged"));
    public static final RegistryObject<ModifierItem> MENDING_MYCELIUM = register("mending_mycelium", () -> new ModifierItem("mending_mycelium"));
    public static final RegistryObject<ModifierItem> FORTUITOUS_GEODE = register("fortuitous_geode", () -> new ModifierItem("fortuitous_geode"));
    public static final RegistryObject<ModifierItem> SILKEN_WRAPPINGS = register("silken_wrappings", () -> new ModifierItem("silken_wrappings"));

    private static Item.Properties defProps()
    {
        return defProps(64);
    }

    private static Item.Properties defProps(int maxStackSize)
    {
        return new Item.Properties().maxStackSize(maxStackSize).group(IgneousSmithy.IgneousGroup.instance);
    }

    static void register()
    {
    }

    private static <T extends Item> RegistryObject<T> register(String name, Supplier<T> itemSupplier)
    {
        return Registration.ITEMS.register(name, itemSupplier);
    }
}
