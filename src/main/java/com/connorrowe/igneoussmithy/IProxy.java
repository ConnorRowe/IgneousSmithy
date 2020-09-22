package com.connorrowe.igneoussmithy;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public interface IProxy
{
    @Nullable
    default PlayerEntity getClientPlayer()
    {
        return null;
    }

    MinecraftServer getServer();
}
