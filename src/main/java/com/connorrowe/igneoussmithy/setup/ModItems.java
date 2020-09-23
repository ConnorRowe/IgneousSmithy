package com.connorrowe.igneoussmithy.setup;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.items.*;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;

public class ModItems
{
    public static final RegistryObject<Item> BALL_PEEN_HAMMER = Registration.ITEMS.register("ball_peen_hammer", () -> new Item(partProps()));

    public static final RegistryObject<ToolHead> PICKAXE_HEAD = Registration.ITEMS.register("pickaxe_head", () -> new ToolHead(ToolType.PICKAXE));
    public static final RegistryObject<ToolHead> SHOVEL_HEAD = Registration.ITEMS.register("shovel_head", () -> new ToolHead(ToolType.SHOVEL));
    public static final RegistryObject<ToolPart> BINDING = Registration.ITEMS.register("binding", () -> new ToolPart(PartType.BINDING));
    public static final RegistryObject<ToolPart> HANDLE = Registration.ITEMS.register("handle", () -> new ToolPart(PartType.HANDLE));

    public static final RegistryObject<DynamicTool> DYNAMIC_PICKAXE = Registration.ITEMS.register("dynamic_pickaxe", () -> new DynamicTool(ToolType.PICKAXE, new ToolLayer("handle", 0), new ToolLayer("pickaxe_bind", 1), new ToolLayer("pickaxe_head", 2)));
    public static final RegistryObject<DynamicTool> DYNAMIC_SHOVEL = Registration.ITEMS.register("dynamic_shovel", () -> new DynamicTool(ToolType.SHOVEL, new ToolLayer("handle", 0), new ToolLayer("shovel_bind", 1), new ToolLayer("shovel_head", 2)));

    public static final RegistryObject<Diagram> DIAGRAM_PICKAXE_HEAD = Registration.ITEMS.register("diagram_pickaxe_head", () -> new Diagram(PartType.HEAD, PICKAXE_HEAD::get, 3));
    public static final RegistryObject<Diagram> DIAGRAM_SHOVEL_HEAD = Registration.ITEMS.register("diagram_shovel_head", () -> new Diagram(PartType.HEAD, SHOVEL_HEAD::get, 3));
    public static final RegistryObject<Diagram> DIAGRAM_BINDING = Registration.ITEMS.register("diagram_binding", () -> new Diagram(PartType.BINDING, BINDING, 1));
    public static final RegistryObject<Diagram> DIAGRAM_HANDLE = Registration.ITEMS.register("diagram_handle", () -> new Diagram(PartType.HANDLE, HANDLE, 2));

    private static Item.Properties partProps()
    {
        return new Item.Properties().maxStackSize(1).group(IgneousSmithy.IgneousGroup.instance);
    }

    static void register()
    {
    }
}
