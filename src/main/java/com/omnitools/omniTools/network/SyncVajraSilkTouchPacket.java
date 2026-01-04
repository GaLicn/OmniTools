package com.omnitools.omniTools.network;

import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniVajraItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncVajraSilkTouchPacket(boolean enabled) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("omnitools", "sync_vajra_silk_touch");
    public static final Type<SyncVajraSilkTouchPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<ByteBuf, SyncVajraSilkTouchPacket> CODEC = ByteBufCodecs.BOOL.map(
            SyncVajraSilkTouchPacket::new,
            SyncVajraSilkTouchPacket::enabled
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player == null) {
                return;
            }

            ItemStack mainHandStack = player.getMainHandItem();
            if (mainHandStack.getItem() == ModItems.OMNI_VAJRA.get()) {
                OmniVajraItem.setSilkTouchEnabled(mainHandStack, player.level(), this.enabled);
            }
        });
    }
}
