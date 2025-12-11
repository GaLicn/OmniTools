package com.omnitools.omniTools.compat.extendedae;

import com.glodblock.github.extendedae.common.EAESingletons;
import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

public class ExtendedAEWirelessUseHandler implements IUseHandler {
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
        return stack.get(EAESingletons.WIRELESS_LOCATOR) != null;
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        ItemStack stack = context.getStack();
        stack.remove(EAESingletons.WIRELESS_LOCATOR);
        return InteractionResultHolder.success(stack);
    }
}
