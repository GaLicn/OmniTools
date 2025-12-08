package com.omnitools.omniTools.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

/**
 * 只负责 Immersive Engineering 多方块的成型逻辑，避免把 IE 逻辑塞进核心物品类。
 */
public class IEWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        // 只在服务端并且世界加载了 IE 的情况下才尝试处理
        return !context.getLevel().isClientSide();
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level world = context.getLevel();
        var player = context.getPlayer();
        var pos = context.getPos();
        Direction side = context.getFace();

        if (player == null) {
            return InteractionResult.PASS;
        }

        Direction multiblockSide;
        if (side.getAxis() == Direction.Axis.Y) {
            multiblockSide = Direction.fromYRot(player.getYRot()).getOpposite();
        } else {
            multiblockSide = side;
        }

        for (IMultiblock mb : MultiblockHandler.getMultiblocks()) {
            if (mb.isBlockTrigger(world.getBlockState(pos), multiblockSide, world)) {
                if (mb.createStructure(world, pos, multiblockSide, player)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
