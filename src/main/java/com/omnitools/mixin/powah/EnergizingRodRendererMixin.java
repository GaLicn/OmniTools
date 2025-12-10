package com.omnitools.mixin.powah;

import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import owmii.powah.block.energizing.EnergizingRodTile;
import owmii.powah.client.render.tile.EnergizingRodRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = EnergizingRodRenderer.class, remap = false)
public abstract class EnergizingRodRendererMixin {

    /**
     * 扩展powha中用于控制是否渲染的 flag 条件：
     * 允许手持我的扳手且ToolMode为LINK时同样会渲染
     */
    @ModifyVariable(method = "render", at = @At(value = "LOAD"), ordinal = 0)
    private boolean omnitools$extendLinkFlag(boolean original, EnergizingRodTile te, float pt,
                                             com.mojang.blaze3d.vertex.PoseStack matrix,
                                             MultiBufferSource rtb, Minecraft mc,
                                             ClientLevel world, LocalPlayer player,
                                             int light, int ov) {
        // 原本已经是 true 就不动
        if (original) {
            return true;
        }
        if (player == null) {
            return false;
        }

        // 检查手上是否有处于 LINK 模式的 omni 扳手
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
