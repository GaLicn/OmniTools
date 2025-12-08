package com.omnitools.omniTools.compat.mebeamformer;

import com.mebeamformer.item.LaserBindingTool;
import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MEBeamFormerUseHandler implements IUseHandler {
    private static LaserBindingTool CACHED_TOOL = null;

    private static LaserBindingTool getLaserBindingTool() {
        if (CACHED_TOOL != null) {
            return CACHED_TOOL;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof LaserBindingTool tool) {
                CACHED_TOOL = tool;
                return tool;
            }
        }
        return null;
    }

    @Override
    public boolean canHandle(UseContext context) {
        // 只在 ME 光束模式下处理
        return context.getCurrentMode() == com.omnitools.omniTools.core.ToolMode.ME_BEAM_FORMER;
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResultHolder.pass(context.getStack());
        }
        LaserBindingTool tool = getLaserBindingTool();
        if (tool == null) {
            return InteractionResultHolder.pass(context.getStack());
        }
        return tool.use(level, player, context.getHand());
    }
}
