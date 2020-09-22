package com.connorrowe.igneoussmithy.items;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Material
{
    public static Material DEFAULT = new Material();

    public Material()
    {
        this.materialId = null;
        this.packName = "";
        this.name = new StringTextComponent("DEFAULT");
        this.colour = 0xFF00FF;
        this.rarity = 0;
        this.harvestLevel = 0;
        this.durability = 0;
        this.attackDamage = 0;
        this.efficiency = 0;

    }

    public ITextComponent name;
    public int colour;
    public int rarity;
    public int harvestLevel;
    public int durability;
    public float attackDamage;
    public float efficiency;
    public String texture;
    public float bindingMultiplier;
    public int bindingDurability;
    public Item repairItem;
    public List<Trait> headOnlyTraits;
    public List<Trait> allTraits;
    private ResourceLocation materialId;
    private String packName;

    public ResourceLocation getId()
    {
        return this.materialId;
    }

    public void deserialize(ResourceLocation id, String packName, JsonObject json, IResourceManager resourceManager)
    {
        this.materialId = id;
        this.packName = packName;
        this.name = ITextComponent.Serializer.func_240641_a_(getPrimitiveElement("name", json));
        this.colour = Integer.parseInt(getPrimitiveElement("colour", json).getAsString(), 16);
        this.rarity = getPrimitiveElement("rarity", json).getAsInt();
        this.harvestLevel = getPrimitiveElement("harvest_level", json).getAsInt();
        this.durability = getPrimitiveElement("durability", json).getAsInt();
        this.attackDamage = getPrimitiveElement("attack_damage", json).getAsFloat();
        this.efficiency = getPrimitiveElement("efficiency", json).getAsFloat();
        this.texture = getPrimitiveElement("texture", json).getAsString();
        this.getBindingStats(json);
        this.repairItem = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryCreate(getPrimitiveElement("repair_item", json).getAsString()));
        this.headOnlyTraits = new ArrayList<>();
        this.allTraits = new ArrayList<>();
        this.getTraits(json);
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