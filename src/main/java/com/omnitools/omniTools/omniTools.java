package com.omnitools.omniTools;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import com.omnitools.omniTools.compat.CompatBootstrap;
import com.omnitools.omniTools.core.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(omniTools.MODID)
public class omniTools {
    public static final String MODID = "omnitools";

    public static final Logger LOGGER = LogUtils.getLogger();

    public omniTools(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("OmniTools mod is loading...");
        ModItems.register(modEventBus);
        CompatBootstrap.registerHandlers();
    }
}
