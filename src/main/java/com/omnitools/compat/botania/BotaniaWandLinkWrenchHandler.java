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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.block.Bound;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.common.block.ForceRelayBlock;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.WandOfTheForestItem;

import java.util.Optional;

public class BotaniaWandLinkWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        ItemStack stack = context.getStack();
        BlockPos pos = context.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof ForceRelayBlock) {
            return true;
        }
        Optional<BlockPos> boundPos = WandOfTheForestItem.getBindingAttempt(stack);
        if (boundPos.isPresent()) {
            BlockPos src = boundPos.get();
            BlockEntity srcTile = level.getBlockEntity(src);
            if (srcTile instanceof WandBindable) {
                return true;
            }
        }
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof WandBindable;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();
        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        BlockEntity be = level.getBlockEntity(pos);

        // 连接模式等价于 Botania 魔杖的绑定模式
        WandOfTheForestItem.setBindMode(stack, true);

        // 先处理活塞中继（Force Relay），让连接模式可以绑定/预览它
        if (block instanceof ForceRelayBlock relayBlock) {
            if (player.isShiftKeyDown()) {
                if (level.isClientSide()) {
                    // 客户端直接认为操作会成功，交给服务端实际处理
                    return InteractionResult.SUCCESS;
                }
                boolean used = relayBlock.onUsedByWand(player, stack, level, pos);
                return used ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        }

        Optional<BlockPos> boundPos = WandOfTheForestItem.getBindingAttempt(stack);

        // 先尝试完成一次绑定：手上已有选中的源方块，再对另一方块 SHIFT+右键
        if (player.isShiftKeyDown() && boundPos.isPresent()) {
            BlockPos src = boundPos.get();
            BlockPos dest = pos;
            if (!dest.equals(src)) {
                WandOfTheForestItem.setBindingAttempt(stack, Bound.UNBOUND_POS);
                BlockEntity srcTile = level.getBlockEntity(src);
                if (srcTile instanceof WandBindable srcBindable) {
                    if (srcBindable.bindTo(player, stack, dest, face)) {
                        WandOfTheForestItem.doParticleBeamWithOffset(level, src, dest);
                        WandOfTheForestItem.setBindingAttempt(stack, Bound.UNBOUND_POS);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!(be instanceof WandBindable bindable)) {
            return InteractionResult.PASS;
        }

        // 没有完成绑定，就尝试选中/取消选中当前 WandBindable 方块
        if (player.isShiftKeyDown() && bindable.canSelect(player, stack, pos, face)) {
            if (boundPos.filter(pos::equals).isPresent()) {
                WandOfTheForestItem.setBindingAttempt(stack, Bound.UNBOUND_POS);
            } else {
                WandOfTheForestItem.setBindingAttempt(stack, pos);
            }

            if (level.isClientSide()) {
                player.playSound(BotaniaSounds.ding, 0.11F, 1F);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
