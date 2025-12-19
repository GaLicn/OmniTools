package com.omnitools.network;

import com.omnitools.core.ModItems;
import com.omnitools.core.OmniVajraItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncVajraMiningSpeedPacket {
    private final float miningSpeed;

    public SyncVajraMiningSpeedPacket(float miningSpeed) {
        this.miningSpeed = miningSpeed;
    }

    public static void encode(SyncVajraMiningSpeedPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.miningSpeed);
    }

    public static SyncVajraMiningSpeedPacket decode(FriendlyByteBuf buf) {
        return new SyncVajraMiningSpeedPacket(buf.readFloat());
    }

    public static void handle(SyncVajraMiningSpeedPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            Player player = ctx.getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() == ModItems.OMNI_VAJRA.get()) {
                OmniVajraItem.setMiningSpeed(stack, msg.miningSpeed);
            }
        });
        ctx.setPacketHandled(true);
    }
}
