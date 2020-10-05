package com.connorrowe.igneoussmithy.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface ITraitConsumer
{
    float execute(ItemStack stack, @Nullable LivingEntity player, @Nullable Entity other, @Nullable World world, float value, @Nullable Object object);
}
