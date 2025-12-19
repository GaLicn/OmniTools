package com.omnitools.omniTools.compat.mekanism;

import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import mekanism.common.MekanismLang;
import mekanism.common.registries.MekanismDataComponents;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MekanismConfigCardUseHandler implements IUseHandler {
    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }
        return player.isShiftKeyDown();
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (level != null && player != null && !level.isClientSide) {
            stack.remove(MekanismDataComponents.CONFIGURATION_DATA);
            player.displayClientMessage(MekanismLang.CONFIG_CARD_CLEARED.translate(), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level != null && level.isClientSide);
    }
}
