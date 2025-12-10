package com.omnitools.network;

import com.omnitools.OmniTools;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(OmniTools.MODID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int id = 0;

    private static int nextId() {
        return id++;
    }

    public static void register() {
        CHANNEL.messageBuilder(SyncToolModePacket.class, nextId(), NetworkDirection.PLAY_TO_SERVER)
                .encoder(SyncToolModePacket::encode)
                .decoder(SyncToolModePacket::decode)
                .consumerNetworkThread(SyncToolModePacket::handle)
                .add();
    }
}
