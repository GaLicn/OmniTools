package com.omnitools.compat.ae2;

import appeng.menu.MenuOpener;
import appeng.menu.implementations.QuartzKnifeMenu;
import appeng.menu.locator.MenuLocators;
import com.omnitools.api.IUseHandler;
import com.omnitools.api.UseContext;
import com.omnitools.core.ToolMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AE2RenameUseHandler implements IUseHandler {
    @Override
    public boolean canHandle(UseContext context) {
        return context.getCurrentMode() == ToolMode.RENAME;
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResultHolder.pass(context.getStack());
        }

        if (!level.isClientSide()) {
            MenuOpener.open(QuartzKnifeMenu.TYPE, player, MenuLocators.forHand(player, context.getHand()));
        }

        player.swing(context.getHand());
        InteractionResult result = InteractionResult.sidedSuccess(level.isClientSide());
        return new InteractionResultHolder<>(result, context.getStack());
    }
}
