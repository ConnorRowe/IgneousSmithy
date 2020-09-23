package com.connorrowe.igneoussmithy;

import com.connorrowe.igneoussmithy.client.MagmaticAnvilRenderer;
import com.connorrowe.igneoussmithy.client.model.PartLoader;
import com.connorrowe.igneoussmithy.client.model.ToolLoader;
import com.connorrowe.igneoussmithy.data.MaterialManager;
import com.connorrowe.igneoussmithy.items.Trait;
import com.connorrowe.igneoussmithy.setup.ModTileEntities;
import com.connorrowe.igneoussmithy.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;

class SideProxy implements IProxy
{
    private final MinecraftServer server = null;

    SideProxy()
    {
        Registration.register();
        IgneousSmithy.IgneousGroup.load();
        Trait.register();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcEnqueue);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::imcProcess);

        MinecraftForge.EVENT_BUS.addListener(this::addReloadListener);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
    }

    private void imcEnqueue(InterModEnqueueEvent event)
    {
    }

    private void imcProcess(InterModProcessEvent event)
    {
    }

    private void addReloadListener(AddReloadListenerEvent event)
    {
        // Load material JSONs
        event.addListener(MaterialManager.INSTANCE);
    }

    private void registerCommands(RegisterCommandsEvent event)
    {
    }

    private void serverStarted(FMLServerStartedEvent event)
    {
        IgneousSmithy.LOGGER.info("Materials loaded: {}", MaterialManager.getValues().size());
    }

    @Override
    public MinecraftServer getServer()
    {
        return server;
    }

    @Mod.EventBusSubscriber(modid = IgneousSmithy.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    static class Client extends SideProxy
    {
        Client()
        {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        }

        @SubscribeEvent
        public static void registerModels(final ModelRegistryEvent evt)
        {
            ModelLoaderRegistry.registerLoader(new ResourceLocation(IgneousSmithy.MODID, "tool_loader"), ToolLoader.INSTANCE);
            ModelLoaderRegistry.registerLoader(new ResourceLocation(IgneousSmithy.MODID, "part_loader"), PartLoader.INSTANCE);
        }

        @SubscribeEvent
        public static void registerTextures(final TextureStitchEvent.Pre evt)
        {
            AtlasTexture map = evt.getMap();

            // For a new "texture" to be used in the material data JSONs, it has to be added here first
            String[] knownMaterialTextures = new String[]{"shiny", "dull", "cactus", "bone"};

            if (map.getTextureLocation() == PlayerContainer.LOCATION_BLOCKS_TEXTURE)
            {
                for (String t : knownMaterialTextures)
                {
                    evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/tool/pickaxe_head_" + t));
                    evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/tool/pickaxe_bind_" + t));
                    evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/tool/shovel_head_" + t));
                    evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/tool/shovel_bind_" + t));
                    evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/tool/handle_" + t));
                    evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/part/binding_" + t));
                }

                evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "item/tool/broken"));
                evt.addSprite(new ResourceLocation(IgneousSmithy.MODID, "block/test"));
            }
        }


        private void clientSetup(FMLClientSetupEvent event)
        {
            ClientRegistry.bindTileEntityRenderer(ModTileEntities.MAGMATIC_ANVIL.get(), MagmaticAnvilRenderer::new);
        }

        @Nullable
        @Override
        public PlayerEntity getClientPlayer()
        {
            return Minecraft.getInstance().player;
        }
    }

    static class Server extends SideProxy
    {
        Server()
        {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::serverSetup);
        }

        private void serverSetup(FMLDedicatedServerSetupEvent event)
        {
        }
    }
}
