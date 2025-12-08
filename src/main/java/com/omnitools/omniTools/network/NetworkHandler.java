package com.omnitools.omniTools.network;

import com.omnitools.omniTools.omniTools;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(omniTools.MODID).versioned("1.0");
        registrar.playToServer(
                SyncToolModePacket.TYPE,
                SyncToolModePacket.CODEC,
                (packet, context) -> packet.handle(context)
        );
    }
}
