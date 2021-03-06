package com.connorrowe.igneoussmithy.data;

import com.connorrowe.igneoussmithy.IgneousSmithy;
import com.connorrowe.igneoussmithy.items.Material;
import com.connorrowe.igneoussmithy.items.ToolPart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("deprecation")
public class MaterialManager implements IResourceManagerReloadListener
{
    public static final MaterialManager INSTANCE = new MaterialManager();

    private static final Gson GSON = (new GsonBuilder()).disableHtmlEscaping().create();
    private static final String DATA_PATH = "igneous_materials";
    private static final Map<ResourceLocation, Material> MAP = Collections.synchronizedMap(new LinkedHashMap<>());

    public static final Material FAKE_MAT = makeFakeMaterial();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        Collection<ResourceLocation> resources = resourceManager.getAllResourceLocations(DATA_PATH, s -> s.endsWith(".json"));
        if (resources.isEmpty())
            return;

        synchronized (MAP)
        {
            MAP.clear();
            IgneousSmithy.LOGGER.info("Reloading materials...");

            for (ResourceLocation id : resources)
            {
                String path = id.getPath().substring(DATA_PATH.length() + 1, id.getPath().length() - ".json".length());
                ResourceLocation name = new ResourceLocation(id.getNamespace(), path);

                String packName;
                try (IResource iResource = resourceManager.getResource(id))
                {
                    packName = iResource.getPackName();
                    JsonObject json = JSONUtils.fromJson(GSON, IOUtils.toString(iResource.getInputStream(), StandardCharsets.UTF_8), JsonObject.class);
                    if (json == null)
                    {
                        IgneousSmithy.LOGGER.error("Could not load material {} as it's null or empty", name);
                    } else
                    {
                        Material newMat = new Material();
                        if (newMat.deserialize(id, packName, json, resourceManager))
                            MAP.put(newMat.getId(), newMat);
                        else
                            IgneousSmithy.LOGGER.error("Error deserializing material {}", name);
                    }
                } catch (IllegalArgumentException | JsonParseException ex)
                {
                    IgneousSmithy.LOGGER.error("Parsing error loading material {}", name, ex);
                } catch (IOException ex)
                {
                    IgneousSmithy.LOGGER.error("Could not read material {}", name, ex);
                }
            }
        }
    }

    public static List<Material> getValues()
    {
        synchronized (MAP)
        {
            return new ArrayList<>(MAP.values());
        }
    }

    @Nullable
    public static Material get(@Nullable ResourceLocation id)
    {
        if (id == null) return null;

        if (id.getPath().equals("fake")) return FAKE_MAT;

        synchronized (MAP)
        {
            return MAP.get(id);
        }
    }

    @Nullable
    public static Material getRecipeResult(Item reagent)
    {
        Material matOut = Material.DEFAULT;

        synchronized (MAP)
        {
            for (ResourceLocation r : MAP.keySet())
            {
                for (Item i : MAP.get(r).repairItems)
                {
                    if (i.equals(reagent))
                    {
                        matOut = MAP.get(r);
                        break;
                    }
                }
                if (!matOut.equals(Material.DEFAULT))
                    break;

                for (ResourceLocation t : MAP.get(r).repairTags)
                {
                    ITag<Item> tag = ItemTags.getCollection().get(t);
                    if (tag != null)
                    {
                        if (tag.contains(reagent))
                        {
                            matOut = MAP.get(r);
                            break;
                        }
                    }
                }
            }
        }

        return matOut;
    }


    private static Material makeFakeMaterial()
    {
        Material material = new Material();
        material.materialId = new ResourceLocation(IgneousSmithy.MODID, "fake");
        material.packName = IgneousSmithy.MODID;
        material.name = new StringTextComponent("Any");
        material.colour = 0x303030;
        material.applyColour = true;
        material.rarity = 0;
        material.harvestLevel = 0;
        material.durability = 0;
        material.attackDamage = 0;
        material.efficiency = 0;
        material.repairTags = new ArrayList<>();
        material.repairItems = new ArrayList<>();

        synchronized (MAP)
        {
            MAP.put(material.materialId, material);
        }

        return material;
    }

    public static Ingredient getPartIngredient(ToolPart part)
    {
        ItemStack[] stacks = new ItemStack[MAP.size()];

        List<ItemStack> stacksList = new ArrayList<>(MAP.size());

        synchronized (MAP)
        {
            for (Material mat : MAP.values())
            {
                ItemStack stack = new ItemStack(part);
                ToolPart.setMaterial(stack, mat);
                stacksList.add(stack);
            }
        }

        Collections.shuffle(stacksList);
        stacksList.toArray(stacks);

        return Ingredient.fromStacks(stacks);
    }
}
