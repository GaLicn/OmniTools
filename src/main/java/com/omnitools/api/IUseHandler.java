package com.omnitools.api;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

public interface IUseHandler {
    boolean canHandle(UseContext context);

    InteractionResultHolder<ItemStack> handle(UseContext context);
}
