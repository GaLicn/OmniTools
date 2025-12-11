package com.omnitools.mixin.draconicevolution;

import com.brandon3055.draconicevolution.client.handler.ClientEventHandler;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让 DE 的能量网络光束在手持 OmniTools 扳手（LINK 模式）时也常显。
 */
@Mixin(value = ClientEventHandler.class, remap = false)
public abstract class ClientEventHandlerMixin {
    @Inject(method = "tickEnd", at = @At("TAIL"))
    private static void omnitools$forceBeamVisible(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.END || event.type != TickEvent.Type.CLIENT || event.side != LogicalSide.CLIENT) {
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        boolean holding = isOmniLink(player.getMainHandItem()) || isOmniLink(player.getOffhandItem());
        if (holding) {
            ClientEventHandler.playerHoldingWrench = true;
        }
    }

    private static boolean isOmniLink(ItemStack stack) {
        return stack.getItem() instanceof OmniToolItem && OmniToolItem.getMode(stack) == ToolMode.LINK;
    }
}
