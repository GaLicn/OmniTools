package com.omnitools.omniTools.compat.logisticsnetworks;

import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LogisticsNetworksClipboardUseHandler implements IUseHandler {
    private static final Component PREFIX = Component.translatable("omnitools.compat.logisticsnetworks").append(" ");

    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return false;
        }
        return LogisticsNetworksCompatUtil.hasClipboard(context.getStack());
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (level == null || player == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide) {
            LogisticsNetworksCompatUtil.setClipboard(stack, null, player.registryAccess());
            player.displayClientMessage(
                    PREFIX.copy().append(Component.translatable("message.omnitools.logisticsnetworks.clipboard_cleared")),
                    true
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
