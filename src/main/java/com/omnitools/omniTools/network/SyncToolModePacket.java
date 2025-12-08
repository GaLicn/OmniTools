package com.omnitools.omniTools.network;

import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ToolMode;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncToolModePacket(String modeId) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("omnitools", "sync_tool_mode");
    public static final Type<SyncToolModePacket> TYPE = new Type<>(ID);
    
    public static final StreamCodec<ByteBuf, SyncToolModePacket> CODEC = ByteBufCodecs.STRING_UTF8.map(
            SyncToolModePacket::new,
            SyncToolModePacket::modeId
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player != null) {
                ItemStack mainHandStack = player.getMainHandItem();
                ToolMode mode = ToolMode.fromId(this.modeId);
                OmniToolItem.setMode(mainHandStack, mode);
            }
        });
    }
}
