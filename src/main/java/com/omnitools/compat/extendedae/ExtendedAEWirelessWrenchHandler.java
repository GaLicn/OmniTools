package com.omnitools.compat.extendedae;

import appeng.menu.MenuOpener;
import com.glodblock.github.extendedae.common.blocks.BlockWirelessConnector;
import com.glodblock.github.extendedae.common.blocks.BlockWirelessHub;
import com.glodblock.github.extendedae.common.me.wireless.WirelessFail;
import com.glodblock.github.extendedae.common.tileentities.TileWirelessConnector;
import com.glodblock.github.extendedae.common.tileentities.TileWirelessHub;
import com.glodblock.github.extendedae.config.EPPConfig;
import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
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
        // 客户端直接消费，服务端执行实际逻辑
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
        var nbt = stack.getOrCreateTag();
        long freq = nbt.getLong("freq");

        if (freq == 0) {
            nbt.putLong("freq", tile.getNewFreq());
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, GlobalPos.of(level.dimension(), thisPos))
                    .result()
                    .ifPresent(tag -> nbt.put("bind", tag));
            player.displayClientMessage(Component.translatable("chat.wireless_bind", thisPos.getX(), thisPos.getY(), thisPos.getZ()), true);
            return InteractionResult.SUCCESS;
        }

        GlobalPos globalPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("bind"))
                .resultOrPartial(Util.prefix("Connector position", com.glodblock.github.extendedae.ExtendedAE.LOGGER::error))
                .map(com.mojang.datafixers.util.Pair::getFirst)
                .orElse(null);
        if (globalPos == null) {
            player.displayClientMessage(WirelessFail.MISSING.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        var otherPos = globalPos.pos();
        var otherDim = globalPos.dimension();
        var thisDim = level.dimension();

        if (otherPos.equals(thisPos) && otherDim.equals(thisDim)) {
            player.displayClientMessage(WirelessFail.SELF_REFERENCE.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }
        if (!otherDim.equals(thisDim)) {
            player.displayClientMessage(WirelessFail.CROSS_DIMENSION.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }
        if (Math.sqrt(otherPos.distSqr(thisPos)) > EPPConfig.wirelessMaxRange) {
            player.displayClientMessage(WirelessFail.OUT_OF_RANGE.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        ServerLevel otherWorld = level.getServer().getLevel(otherDim);
        if (otherWorld == null) {
            player.displayClientMessage(WirelessFail.MISSING.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        BlockEntity otherTile = otherWorld.getBlockEntity(otherPos);
        if (otherTile instanceof TileWirelessConnector otherConnector) {
            otherConnector.setFrequency(freq);
            tile.setFrequency(freq);
            clearBindData(stack);
            player.displayClientMessage(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ()), true);
            return InteractionResult.SUCCESS;
        } else if (otherTile instanceof TileWirelessHub otherHub) {
            int port = otherHub.allocatePort();
            if (port < 0) {
                player.displayClientMessage(WirelessFail.OUT_OF_PORT.getTranslation(), true);
                return InteractionResult.SUCCESS;
            }
            otherHub.setFrequency(freq, port);
            tile.setFrequency(freq);
            clearBindData(stack);
            player.displayClientMessage(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ()), true);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(WirelessFail.MISSING.getTranslation(), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleHub(ServerLevel level, TileWirelessHub tile, BlockPos thisPos, Player player, ItemStack stack) {
        int port = tile.allocatePort();
        if (port < 0) {
            player.displayClientMessage(WirelessFail.OUT_OF_PORT.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        var nbt = stack.getOrCreateTag();
        long freq = nbt.getLong("freq");

        if (freq == 0) {
            nbt.putLong("freq", tile.getNewFreq());
            GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, GlobalPos.of(level.dimension(), thisPos))
                    .result()
                    .ifPresent(tag -> nbt.put("bind", tag));
            player.displayClientMessage(Component.translatable("chat.wireless_bind", thisPos.getX(), thisPos.getY(), thisPos.getZ()), true);
            return InteractionResult.SUCCESS;
        }

        GlobalPos globalPos = GlobalPos.CODEC.decode(NbtOps.INSTANCE, nbt.get("bind"))
                .resultOrPartial(Util.prefix("Connector position", com.glodblock.github.extendedae.ExtendedAE.LOGGER::error))
                .map(com.mojang.datafixers.util.Pair::getFirst)
                .orElse(null);
        if (globalPos == null) {
            player.displayClientMessage(WirelessFail.MISSING.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        var otherPos = globalPos.pos();
        var otherDim = globalPos.dimension();
        var thisDim = level.dimension();

        if (otherPos.equals(thisPos) && otherDim.equals(thisDim)) {
            player.displayClientMessage(WirelessFail.SELF_REFERENCE.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }
        if (!otherDim.equals(thisDim)) {
            player.displayClientMessage(WirelessFail.CROSS_DIMENSION.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }
        if (Math.sqrt(otherPos.distSqr(thisPos)) > EPPConfig.wirelessMaxRange) {
            player.displayClientMessage(WirelessFail.OUT_OF_RANGE.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        ServerLevel otherWorld = level.getServer().getLevel(otherDim);
        if (otherWorld == null) {
            player.displayClientMessage(WirelessFail.MISSING.getTranslation(), true);
            return InteractionResult.SUCCESS;
        }

        BlockEntity otherTile = otherWorld.getBlockEntity(otherPos);
        if (otherTile instanceof TileWirelessConnector otherConnector) {
            otherConnector.setFrequency(freq);
            tile.setFrequency(freq, port);
            player.displayClientMessage(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ()), true);
            return InteractionResult.SUCCESS;
        } else if (otherTile instanceof TileWirelessHub otherHub) {
            int otherPort = otherHub.allocatePort();
            if (otherPort < 0) {
                player.displayClientMessage(WirelessFail.OUT_OF_PORT.getTranslation(), true);
                return InteractionResult.SUCCESS;
            }
            otherHub.setFrequency(freq, otherPort);
            tile.setFrequency(freq, port);
            player.displayClientMessage(Component.translatable("chat.wireless_connect", thisPos.getX(), thisPos.getY(), thisPos.getZ()), true);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(WirelessFail.MISSING.getTranslation(), true);
        return InteractionResult.SUCCESS;
    }

    private void clearBindData(ItemStack stack) {
        var nbt = stack.getTag();
        if (nbt != null) {
            nbt.remove("freq");
            nbt.remove("bind");
        }
    }
}
