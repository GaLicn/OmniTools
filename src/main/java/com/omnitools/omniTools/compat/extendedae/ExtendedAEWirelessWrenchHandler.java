package com.omnitools.omniTools.compat.extendedae;

import com.glodblock.github.extendedae.common.EAESingletons;
import com.glodblock.github.extendedae.common.blocks.BlockWirelessConnector;
import com.glodblock.github.extendedae.common.blocks.BlockWirelessHub;
import com.glodblock.github.extendedae.common.me.wireless.WirelessFail;
import com.glodblock.github.extendedae.common.tileentities.TileWirelessConnector;
import com.glodblock.github.extendedae.common.tileentities.TileWirelessHub;
import com.glodblock.github.extendedae.config.EAEConfig;
import it.unimi.dsi.fastutil.Pair;
import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ExtendedAEWirelessWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(context.getPos());
        return be instanceof TileWirelessConnector || be instanceof TileWirelessHub;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        // 客户端直接消费，服务端执行业务逻辑
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack stack = context.getStack();

        if (be instanceof TileWirelessConnector connector) {
            return handleConnector(server, connector, pos, player, stack);
        }
        if (be instanceof TileWirelessHub hub) {
            return handleHub(server, hub, pos, player, stack);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleConnector(ServerLevel level, TileWirelessConnector tile, BlockPos thisPos, Player player, ItemStack stack) {
        var locator = stack.get(EAESingletons.WIRELESS_LOCATOR);
        // 已有频率且绑定源：尝试连接
        if (locator != null && locator.left() != 0) {
            long freq = locator.left();
            GlobalPos globalPos = locator.right();
            if (globalPos == null) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.MISSING.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            var otherPos = globalPos.pos();
            var otherDim = globalPos.dimension();
            var thisDim = level.dimension();
            if (otherPos.equals(thisPos) && otherDim.equals(thisDim)) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.SELF_REFERENCE.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            if (!otherDim.equals(thisDim)) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.CROSS_DIMENSION.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            if (Math.sqrt(otherPos.distSqr(thisPos)) > EAEConfig.wirelessMaxRange) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.OUT_OF_RANGE.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            ServerLevel otherWorld = level.getServer().getLevel(otherDim);
            if (otherWorld == null) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.MISSING.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            BlockEntity otherTile = otherWorld.getBlockEntity(otherPos);
            if (otherTile instanceof TileWirelessConnector otherConnector) {
                otherConnector.setFrequency(freq);
                tile.setFrequency(freq);
                stack.remove(EAESingletons.WIRELESS_LOCATOR); // Connector 成功后清除
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ())), true);
                return InteractionResult.SUCCESS;
            } else if (otherTile instanceof TileWirelessHub otherHub) {
                int port = otherHub.allocatePort();
                if (port < 0) {
                    player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.OUT_OF_PORT.getTranslation()), true);
                    return InteractionResult.SUCCESS;
                }
                otherHub.setFrequency(freq, port);
                tile.setFrequency(freq);
                stack.remove(EAESingletons.WIRELESS_LOCATOR); // Connector 成功后清除
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ())), true);
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.MISSING.getTranslation()), true);
            return InteractionResult.SUCCESS;
        }

        // 未绑定：写入频率与位置
        long freq = tile.getNewFreq();
        GlobalPos globalPos = GlobalPos.of(level.dimension(), thisPos);
        stack.set(EAESingletons.WIRELESS_LOCATOR, Pair.of(freq, globalPos));
        player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(Component.translatable("chat.wireless_bind", thisPos.getX(), thisPos.getY(), thisPos.getZ())), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleHub(ServerLevel level, TileWirelessHub tile, BlockPos thisPos, Player player, ItemStack stack) {
        var locator = stack.get(EAESingletons.WIRELESS_LOCATOR);
        int port = tile.allocatePort();
        if (port < 0) {
            player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.OUT_OF_PORT.getTranslation()), true);
            return InteractionResult.SUCCESS;
        }

        // 已有频率且绑定源：尝试连接，Hub 侧保留 locator 便于连续连接
        if (locator != null && locator.left() != 0) {
            long freq = locator.left();
            GlobalPos globalPos = locator.right();
            if (globalPos == null) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.MISSING.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            var otherPos = globalPos.pos();
            var otherDim = globalPos.dimension();
            var thisDim = level.dimension();
            if (otherPos.equals(thisPos) && otherDim.equals(thisDim)) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.SELF_REFERENCE.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            if (!otherDim.equals(thisDim)) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.CROSS_DIMENSION.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            if (Math.sqrt(otherPos.distSqr(thisPos)) > EAEConfig.wirelessMaxRange) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.OUT_OF_RANGE.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            ServerLevel otherWorld = level.getServer().getLevel(otherDim);
            if (otherWorld == null) {
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.MISSING.getTranslation()), true);
                return InteractionResult.SUCCESS;
            }
            BlockEntity otherTile = otherWorld.getBlockEntity(otherPos);
            if (otherTile instanceof TileWirelessConnector otherConnector) {
                otherConnector.setFrequency(freq);
                tile.setFrequency(freq, port);
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ())), true);
                return InteractionResult.SUCCESS;
            } else if (otherTile instanceof TileWirelessHub otherHub) {
                int otherPort = otherHub.allocatePort();
                if (otherPort < 0) {
                    player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.OUT_OF_PORT.getTranslation()), true);
                    return InteractionResult.SUCCESS;
                }
                otherHub.setFrequency(freq, otherPort);
                tile.setFrequency(freq, port);
                player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ())), true);
                return InteractionResult.SUCCESS;
            }
            player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(WirelessFail.MISSING.getTranslation()), true);
            return InteractionResult.SUCCESS;
        }

        // 未绑定：写入频率与位置（Hub 侧也可作为源）
        long freq = tile.getNewFreq();
        GlobalPos globalPos = GlobalPos.of(level.dimension(), thisPos);
        stack.set(EAESingletons.WIRELESS_LOCATOR, Pair.of(freq, globalPos));
        player.displayClientMessage(Component.translatable("omnitools.compat.extendedae").append(" ").append(Component.translatable("chat.wireless_bind", thisPos.getX(), thisPos.getY(), thisPos.getZ())), true);
        return InteractionResult.SUCCESS;
    }
}
