package com.omnitools.compat.voltaic;

import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import net.minecraft.core.BlockPos;
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

        BlockPos pos = context.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof IWrenchable;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (!(block instanceof IWrenchable wrenchable)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();
        IWrenchItem wrenchItem = stack.getItem() instanceof IWrenchItem item ? item : null;
        boolean isSneaking = player.isShiftKeyDown();

        if (isSneaking) {
            if (wrenchItem != null && !wrenchItem.shouldPickup(stack, pos, player)) {
                return InteractionResult.PASS;
            }
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            wrenchable.onPickup(stack, pos, player);
            return InteractionResult.CONSUME;
        }

        if (wrenchItem != null && !wrenchItem.shouldRotate(stack, pos, player)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        wrenchable.onRotate(stack, pos, player);
        return InteractionResult.CONSUME;
    }
}
