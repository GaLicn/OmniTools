package com.omnitools.client;

import com.omnitools.core.ModItems;
import com.omnitools.core.OmniVajraItem;
import com.omnitools.network.NetworkHandler;
import com.omnitools.network.SyncVajraMiningSpeedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = com.omnitools.OmniTools.MODID, value = Dist.CLIENT)
public class VajraClientEvents {
    private static final float SPEED_STEP = 10.0F;

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        double delta = event.getScrollDelta();
        if (delta == 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        if (!player.isShiftKeyDown()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.OMNI_VAJRA.get()) {
            return;
        }

        float oldSpeed = OmniVajraItem.getMiningSpeed(stack);
        float deltaSpeed = delta > 0 ? SPEED_STEP : -SPEED_STEP;
        float newSpeed = oldSpeed + deltaSpeed;

        OmniVajraItem.setMiningSpeed(stack, newSpeed);
        NetworkHandler.CHANNEL.sendToServer(new SyncVajraMiningSpeedPacket(OmniVajraItem.getMiningSpeed(stack)));

        player.displayClientMessage(
                Component.translatable("message.omnitools.vajra_mining_speed", OmniVajraItem.getMiningSpeed(stack)),
                true
        );
        event.setCanceled(true);
    }
}
