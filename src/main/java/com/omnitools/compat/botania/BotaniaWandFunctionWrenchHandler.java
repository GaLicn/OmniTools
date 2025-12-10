package com.omnitools.compat.botania;

import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.xplat.XplatAbstractions;

public class BotaniaWandFunctionWrenchHandler implements IWrenchHandler {
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
        BlockEntity be = level.getBlockEntity(pos);
        Wandable wandable = XplatAbstractions.INSTANCE.findWandable(level, pos, state, be);
        return wandable != null;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        ItemStack stack = context.getStack();
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        Wandable wandable = XplatAbstractions.INSTANCE.findWandable(level, pos, state, be);
        if (wandable == null) {
            return InteractionResult.PASS;
        }

        boolean used = wandable.onUsedByWand(player, stack, face);
        return used ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
