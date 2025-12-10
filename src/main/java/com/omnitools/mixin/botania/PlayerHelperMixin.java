package com.omnitools.mixin.botania;

import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.common.helper.PlayerHelper;

@Mixin(value = PlayerHelper.class,remap = false)
public abstract class PlayerHelperMixin {

    @Inject(method = "hasHeldItemClass", at = @At("HEAD"), cancellable = true)
    private static void omnitools$pretendWandWhenOmniLink(Player player, Class<?> template, CallbackInfoReturnable<Boolean> cir) {
        if (player == null) {
            return;
        }
        // 只在 Botania 自己检查 WandOfTheForestItem.class 时，才把 LINK 模式的万能扳手视为“也在持有魔杖”
        if (!"vazkii.botania.common.item.WandOfTheForestItem".equals(template.getName())) {
            return;
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if (omnitools$isOmniWrenchInLinkMode(main) || omnitools$isOmniWrenchInLinkMode(off)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private static boolean omnitools$isOmniWrenchInLinkMode(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return false;
        }
        return (OmniToolItem.getMode(stack) == ToolMode.LINK ||OmniToolItem.getMode(stack) == ToolMode.WRENCH);
    }
}
