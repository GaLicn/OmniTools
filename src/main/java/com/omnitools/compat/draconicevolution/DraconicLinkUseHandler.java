package com.omnitools.compat.draconicevolution;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.draconicevolution.api.energy.ICrystalBinder;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.omnitools.api.IUseHandler;
import com.omnitools.api.UseContext;
import com.omnitools.core.ToolMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class DraconicLinkUseHandler implements IUseHandler {
    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return false;
        }
        ItemStack stack = context.getStack();
        return stack.hasTag() && stack.getTag().contains(ICrystalBinder.BINDER_TAG, 11);
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        var player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (player == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            if (stack.hasTag()) {
                stack.getTag().remove(ICrystalBinder.BINDER_TAG);
            }
            ChatHelper.sendIndexed(player, Component.translatable("gui.draconicevolution.energy_net.pos_cleared"), TileCrystalBase.MSG_ID);
        }

        return InteractionResultHolder.success(stack);
    }
}
