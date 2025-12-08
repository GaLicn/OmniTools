package com.omnitools.omniTools.api;

import net.minecraft.world.InteractionResult;

public interface IWrenchHandler {
    boolean canHandle(WrenchContext context);

    InteractionResult handle(WrenchContext context);
}
