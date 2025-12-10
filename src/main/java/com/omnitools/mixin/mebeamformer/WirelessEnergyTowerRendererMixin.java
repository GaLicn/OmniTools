package com.omnitools.mixin.mebeamformer;

import com.mebeamformer.client.render.WirelessEnergyTowerRenderer;
import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * 我自己的模组，适配起来就是简单，就是爽哈哈
 */
@Mixin(WirelessEnergyTowerRenderer.class)
public abstract class WirelessEnergyTowerRendererMixin {

    @ModifyVariable(
            method = "render",
            at = @At(value = "STORE"),
            ordinal = 0,
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;"),
                    to = @At(value = "INVOKE", target = "Lcom/mebeamformer/blockentity/WirelessEnergyTowerBlockEntity;getClientLinks()Ljava/util/List;")
            )
    )
    private boolean omnitools$extendHoldingBindingTool(boolean holdingBindingTool) {
        if (holdingBindingTool) {
            return true;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return false;
        }

        // 检查两只手是否有处于 LINK 模式的 omni 扳手
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() == ModItems.OMNI_WRENCH.get()
                    && OmniToolItem.getMode(stack) == ToolMode.LINK) {
                return true;
            }
        }

        return false;
    }
}
