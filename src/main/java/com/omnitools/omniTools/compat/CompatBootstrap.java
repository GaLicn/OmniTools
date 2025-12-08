package com.omnitools.omniTools.compat;

import com.omnitools.omniTools.compat.immersiveengineering.IEWrenchHandler;
import com.omnitools.omniTools.core.WrenchHandlerRegistry;
import net.neoforged.fml.ModList;

public class CompatBootstrap {
    public static void registerHandlers() {
        if (ModList.get().isLoaded("immersiveengineering")) {
            WrenchHandlerRegistry.register(new IEWrenchHandler());
        }
    }
}
