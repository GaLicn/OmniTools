package com.omnitools.compat;

import com.omnitools.compat.immersiveengineering.IEWrenchHandler;
import com.omnitools.core.UseHandlerRegistry;
import com.omnitools.core.WrenchHandlerRegistry;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;

public class CompatBootstrap {
    public static void registerHandlers() {
        if (ModList.get().isLoaded("immersiveengineering")) {
            WrenchHandlerRegistry.register(new IEWrenchHandler());
        }
    }
}
