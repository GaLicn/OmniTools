package com.omnitools.compat.extendedae;

import com.omnitools.api.IUseHandler;
import com.omnitools.api.UseContext;
import com.omnitools.core.ToolMode;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 兼容 ExtendedAE 无线工具的“潜行对空气右键清除绑定”。
 */
public class ExtendedAEWirelessUseHandler implements IUseHandler {
    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return false;
        }
        ItemStack stack = context.getStack();
        var nbt = stack.getTag();
        return nbt != null && (nbt.contains("freq") || nbt.contains("bind"));
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        ItemStack stack = context.getStack();
        var nbt = stack.getTag();
        if (nbt != null) {
            nbt.remove("freq");
            nbt.remove("bind");
        }
        return InteractionResultHolder.success(stack);
    }
}
