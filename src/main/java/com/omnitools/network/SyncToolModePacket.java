package com.omnitools.network;

import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncToolModePacket {
    private final String modeId;

    public SyncToolModePacket(String modeId) {
        this.modeId = modeId;
    }

    public static void encode(SyncToolModePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.modeId);
    }

    public static SyncToolModePacket decode(FriendlyByteBuf buf) {
        return new SyncToolModePacket(buf.readUtf());
    }

    public static void handle(SyncToolModePacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Player player = ctx.getSender();
            if (player != null) {
                ItemStack mainHandStack = player.getMainHandItem();
                ToolMode mode = ToolMode.fromId(msg.modeId);
                OmniToolItem.setMode(mainHandStack, mode);
            }
        });
        ctx.setPacketHandled(true);
    }
}
