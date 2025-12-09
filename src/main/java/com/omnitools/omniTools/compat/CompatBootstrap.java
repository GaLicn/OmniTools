package com.omnitools.omniTools.compat;

import com.omnitools.omniTools.compat.ae2.AE2RenameUseHandler;
import com.omnitools.omniTools.compat.create.CreateValueSettingsPreHandler;
import com.omnitools.omniTools.compat.entangled.EntangledBinderUseHandler;
import com.omnitools.omniTools.compat.entangled.EntangledBinderWrenchHandler;
import com.omnitools.omniTools.compat.extendedae.ExtendedAERenamePreHandler;
import com.omnitools.omniTools.compat.immersiveengineering.IEWrenchHandler;
import com.omnitools.omniTools.compat.mebeamformer.MEBeamFormerUseHandler;
import com.omnitools.omniTools.compat.mebeamformer.MEBeamFormerWrenchHandler;
import com.omnitools.omniTools.compat.mekanism.MekanismTransmitterWrenchHandler;
import com.omnitools.omniTools.compat.powah.PowahLinkWrenchHandler;
import com.omnitools.omniTools.core.UseHandlerRegistry;
import com.omnitools.omniTools.core.WrenchHandlerRegistry;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public class CompatBootstrap {
    public static void registerHandlers() {
        if (ModList.get().isLoaded("immersiveengineering")) {
            WrenchHandlerRegistry.register(new IEWrenchHandler());
        }
        if (ModList.get().isLoaded("mekanism")) {
            WrenchHandlerRegistry.register(new MekanismTransmitterWrenchHandler());
        }
        if (ModList.get().isLoaded("ae2")) {
            UseHandlerRegistry.register(new AE2RenameUseHandler());
        }
        if (ModList.get().isLoaded("extendedae")) {
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ExtendedAERenamePreHandler::onRightClickBlock);
        }
        if (ModList.get().isLoaded("me_beam_former")) {
            WrenchHandlerRegistry.register(new MEBeamFormerWrenchHandler());
            UseHandlerRegistry.register(new MEBeamFormerUseHandler());
        }
        if (ModList.get().isLoaded("powah")) {
            WrenchHandlerRegistry.register(new PowahLinkWrenchHandler());
        }
        if (ModList.get().isLoaded("entangled")) {
            WrenchHandlerRegistry.register(new EntangledBinderWrenchHandler());
            UseHandlerRegistry.register(new EntangledBinderUseHandler());
        }
        if (ModList.get().isLoaded("create")) {
            NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CreateValueSettingsPreHandler::onRightClickBlock);
        }
    }
}
