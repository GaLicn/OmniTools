package com.omnitools.compat;

import com.omnitools.compat.ae2.AE2RenameUseHandler;
import com.omnitools.compat.botania.BotaniaWandLinkWrenchHandler;
import com.omnitools.compat.botania.BotaniaWandFunctionWrenchHandler;
import com.omnitools.compat.create.CreateValueSettingsPreHandler;
import com.omnitools.compat.entangled.EntangledBinderUseHandler;
import com.omnitools.compat.entangled.EntangledBinderWrenchHandler;
import com.omnitools.compat.entangled.EntangledHighlightHandler;
import com.omnitools.compat.extendedae.ExtendedAEWirelessWrenchHandler;
import com.omnitools.compat.extendedae.ExtendedAEWirelessUseHandler;
import com.omnitools.compat.extendedae.ExtendedAERenamePreHandler;
import com.omnitools.compat.immersiveengineering.IEWrenchHandler;
import com.omnitools.compat.mebeamformer.MEBeamFormerUseHandler;
import com.omnitools.compat.mebeamformer.MEBeamFormerWrenchHandler;
import com.omnitools.compat.mekanism.MekanismTransmitterWrenchHandler;
import com.omnitools.compat.powah.PowahLinkWrenchHandler;
import com.omnitools.compat.draconicevolution.DraconicLinkWrenchHandler;
import com.omnitools.compat.draconicevolution.DraconicLinkUseHandler;
import com.omnitools.core.UseHandlerRegistry;
import com.omnitools.core.WrenchHandlerRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.DistExecutor;

public class CompatBootstrap {
    public static void registerHandlers() {
        // Immersive Engineering
        if (ModList.get().isLoaded("immersiveengineering")) {
            WrenchHandlerRegistry.register(new IEWrenchHandler());
        }

        // Draconic Evolution
        if (ModList.get().isLoaded("draconicevolution")) {
            WrenchHandlerRegistry.register(new DraconicLinkWrenchHandler());
            UseHandlerRegistry.register(new DraconicLinkUseHandler());
        }

        // Mekanism
        if (ModList.get().isLoaded("mekanism")) {
            WrenchHandlerRegistry.register(new MekanismTransmitterWrenchHandler());
        }

        // AE2
        if (ModList.get().isLoaded("ae2")) {
            UseHandlerRegistry.register(new AE2RenameUseHandler());
        }

        // ExtendedAE无线连接器
        if (ModList.get().isLoaded("expatternprovider")) {
            WrenchHandlerRegistry.register(new ExtendedAEWirelessWrenchHandler());
            UseHandlerRegistry.register(new ExtendedAEWirelessUseHandler());
        }

        // Powah
        if (ModList.get().isLoaded("powah")) {
            WrenchHandlerRegistry.register(new PowahLinkWrenchHandler());
        }

        // Botania
        if (ModList.get().isLoaded("botania")) {
            WrenchHandlerRegistry.register(new BotaniaWandLinkWrenchHandler());
            WrenchHandlerRegistry.register(new BotaniaWandFunctionWrenchHandler());
        }

        // ME Beam Former
        if (ModList.get().isLoaded("me_beam_former")) {
            WrenchHandlerRegistry.register(new MEBeamFormerWrenchHandler());
            UseHandlerRegistry.register(new MEBeamFormerUseHandler());
        }

        // Entangled
        if (ModList.get().isLoaded("entangled")) {
            WrenchHandlerRegistry.register(new EntangledBinderWrenchHandler());
            UseHandlerRegistry.register(new EntangledBinderUseHandler());
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, EntangledHighlightHandler::onRenderLevel);
        }

        // ExtendedAE - 事件监听
        if (ModList.get().isLoaded("expatternprovider")) {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, ExtendedAERenamePreHandler::onRightClickBlock);
        }

        // Create - 事件监听
        if (ModList.get().isLoaded("create")) {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CreateValueSettingsPreHandler::onRightClickBlock);
        }
    }
}
