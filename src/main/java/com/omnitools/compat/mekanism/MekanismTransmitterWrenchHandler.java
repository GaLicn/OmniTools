package com.omnitools.compat.mekanism;

import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import mekanism.api.IConfigurable;
import mekanism.api.security.ISecurityUtils;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

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
        LazyOptional<IConfigurable> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE, face);
        return capability.isPresent();
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

        if (!ISecurityUtils.INSTANCE.canAccessOrDisplayError(player, tile)) {
            return InteractionResult.FAIL;
        }

        Direction face = context.getFace();
        Optional<IConfigurable> capability = CapabilityUtils.getCapability(tile, Capabilities.CONFIGURABLE, face).resolve();
        if (capability.isEmpty()) {
            return InteractionResult.PASS;
        }

        IConfigurable configurable = capability.get();
        // 不要求玩家按下潜行键，直接把普通右键当作潜行右键来处理
        return configurable.onSneakRightClick(player);
    }
}
