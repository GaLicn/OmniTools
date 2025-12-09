package com.omnitools.omniTools.compat.mekanism;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import mekanism.api.IConfigurable;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MekanismTransmitterWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        Level level = context.getLevel();
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (context.getCurrentMode() != ToolMode.WRENCH) {
            return false;
        }

        BlockPos pos = context.getPos();
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile == null) {
            return false;
        }

        Direction face = context.getFace();
        IConfigurable configurable = WorldUtils.getCapability(level, Capabilities.CONFIGURABLE, pos, null, tile, face);
        return configurable != null;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null || level.isClientSide()) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getPos();
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile == null) {
            return InteractionResult.PASS;
        }

        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, level, pos, tile)) {
            return InteractionResult.FAIL;
        }

        Direction face = context.getFace();
        IConfigurable configurable = WorldUtils.getCapability(level, Capabilities.CONFIGURABLE, pos, null, tile, face);
        if (configurable == null) {
            return InteractionResult.PASS;
        }

        // 不要求玩家按下潜行键，直接把普通右键当作潜行右键来处理
        return configurable.onSneakRightClick(player);
    }
}
