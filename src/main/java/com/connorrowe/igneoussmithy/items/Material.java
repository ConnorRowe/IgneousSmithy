package com.connorrowe.igneoussmithy.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Material
{
    public static Material DEFAULT = new Material();
    private static final Map<ResourceLocation, ITag<Item>> itemTags = TagCollectionManager.func_242178_a/*getInstance?*/().func_241836_b/*getItemTags?*/().func_241833_a/*getTagMap?*/();

    public Material()
    {
        this.materialId = null;
        this.packName = "";
        this.name = new StringTextComponent("DEFAULT");
        this.colour = 0xFF00FF;
        this.applyColour = true;
        this.rarity = 0;
        this.harvestLevel = 0;
        this.durability = 0;
        this.attackDamage = 0;
        this.efficiency = 0;
    }

    public ITextComponent name;
    public int colour;
    public boolean applyColour;
    public int rarity;
    public int harvestLevel;
    public int durability;
    public float attackDamage;
    public float efficiency;
    public String texture;
    public float bindingMultiplier;
    public int bindingDurability;
    public List<Item> repairItems;
    public List<ResourceLocation> repairTags;
    public List<Trait> headOnlyTraits;
    public List<Trait> allTraits;
    private ResourceLocation materialId;
    private String packName;

    public ResourceLocation getId()
    {
        return this.materialId;
    }

    /**
     * @param id              Used to retrieve the material in the future.
     * @param packName        Name of the datapack
     * @param json            JsonObject
     * @param resourceManager Resource manager from reload event
     * @return False if there was an error loading - otherwise true
     */
    public boolean deserialize(ResourceLocation id, String packName, JsonObject json, IResourceManager resourceManager)
    {
        this.materialId = id;
        this.packName = packName;
        this.name = ITextComponent.Serializer.func_240641_a_(getPrimitiveElement("name", json));
        this.colour = Integer.parseInt(getPrimitiveElement("colour", json).getAsString(), 16);
        this.applyColour = getPrimitiveElement("apply_colour", json).getAsBoolean();
        this.rarity = getPrimitiveElement("rarity", json).getAsInt();
        this.harvestLevel = getPrimitiveElement("harvest_level", json).getAsInt();
        this.durability = getPrimitiveElement("durability", json).getAsInt();
        this.attackDamage = getPrimitiveElement("attack_damage", json).getAsFloat();
        this.efficiency = getPrimitiveElement("efficiency", json).getAsFloat();
        this.texture = getPrimitiveElement("texture", json).getAsString();
        this.getBindingStats(json);

        // Repair stuff is used to craft parts - probably should rename
        this.repairItems = new ArrayList<>();
        this.repairTags = new ArrayList<>();
        this.getRepairables(json);

        this.headOnlyTraits = new ArrayList<>();
        this.allTraits = new ArrayList<>();
        this.getTraits(json);

        return this.checkRepairables();
    }

    private void getBindingStats(JsonObject json)
    {
        JsonObject bindingStats = getJsonObject("binding", json);
        this.bindingMultiplier = getPrimitiveElement("multiplier", bindingStats).getAsFloat();
        this.bindingDurability = getPrimitiveElement("durability", bindingStats).getAsInt();
    }

    private void getTraits(JsonObject json)
    {
        JsonObject traits = getJsonObject("traits", json);
        JsonArray all = getJsonArray("all", traits);
        JsonArray headOnly = getJsonArray("head_only", traits);

        for (JsonElement e : all)
        {
            this.allTraits.add(Trait.get(e.getAsString()));
        }
        for (JsonElement e : headOnly)
        {
            this.headOnlyTraits.add(Trait.get(e.getAsString()));
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void getRepairables(JsonObject json)
    {
        JsonArray repair = getJsonArray("repair", json);

        for (JsonElement e : repair)
        {
            if (e.getAsJsonObject() == null)
                break;

            JsonObject o = e.getAsJsonObject();

            // Get all item entries
            if (o.has("item"))
            {
                this.repairItems.add(ForgeRegistries.ITEMS.getValue(ResourceLocation.tryCreate(
                        o.getAsJsonPrimitive("item").getAsString())));

            } else if (o.has("tag")) // Get all tag entries
            {
                this.repairTags.add(ResourceLocation.tryCreate(o.getAsJsonPrimitive("tag").getAsString()));
            }
        }
    }

    /**
     * @return True if any loaded repair entries are valid, false if none were valid
     */
    @SuppressWarnings("SpellCheckingInspection")
    private boolean checkRepairables()
    {
        boolean success = false;
        for (Item item : this.repairItems)
        {
            if (item != null)
            {
                success = true;
                break;
            }
        }
        if (!success)
        {
            for (ResourceLocation res : this.repairTags)
            {
                ITag<Item> tag = itemTags.get(res);
                if (tag != null)
                {
                    success = true;
                    break;
                }
            }
        }

        return success;
    }

    private static JsonObject getJsonObject(String objectName, JsonObject json)
    {
        JsonElement element = json.get(objectName);
        if (element != null && element.isJsonObject())
        {
            return element.getAsJsonObject();
        } else
        {
            throw new JsonSyntaxException("Expected '" + objectName + "' object");
        }
    }

    private static JsonElement getPrimitiveElement(String elementName, JsonObject json)
    {
        JsonElement element = json.get(elementName);
        if (element != null && element.isJsonPrimitive())
        {
            return element;
        } else
        {
            throw new JsonSyntaxException("Expected '" + elementName + "' element");
        }
    }

    private static JsonArray getJsonArray(String arrayName, JsonObject json)
    {
        JsonElement element = json.get(arrayName);
        if (element != null && element.isJsonArray())
        {
            return element.getAsJsonArray();
        } else
        {
            throw new JsonSyntaxException("Expected '" + arrayName + "' object");
        }
    }

    public List<Trait> getTraitsForEvent(Trait.TraitEvent traitEvent, boolean headOnly)
    {
        return (headOnly ? headOnlyTraits : allTraits).stream().filter(t -> t.event.equals(traitEvent)).collect(Collectors.toList());
    }
}