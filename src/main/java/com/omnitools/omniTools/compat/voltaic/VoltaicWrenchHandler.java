package com.omnitools.omniTools.compat.voltaic;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import voltaic.api.IWrenchItem;
import voltaic.prefab.tile.IWrenchable;

public class VoltaicWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.WRENCH) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(context.getPos());
        return state.getBlock() instanceof IWrenchable;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }
        BlockState state = level.getBlockState(context.getPos());
        Block block = state.getBlock();
        if (!(block instanceof IWrenchable wrenchable)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();
        boolean shouldRotate = true;
        boolean shouldPickup = true;
        if (stack.getItem() instanceof IWrenchItem wrenchItem) {
            shouldRotate = wrenchItem.shouldRotate(stack, context.getPos(), player);
            shouldPickup = wrenchItem.shouldPickup(stack, context.getPos(), player);
        }

        boolean isSneaking = player.isShiftKeyDown();
        if (isSneaking && !shouldPickup) {
            return InteractionResult.PASS;
        }
        if (!isSneaking && !shouldRotate) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (isSneaking) {
            wrenchable.onPickup(stack, context.getPos(), player);
            return InteractionResult.CONSUME;
        }
        wrenchable.onRotate(stack, context.getPos(), player);
        return InteractionResult.CONSUME;
    }
}
