package com.omnitools.omniTools.compat.logisticsnetworks;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import me.almana.logisticsnetworks.data.NetworkRegistry;
import me.almana.logisticsnetworks.entity.LogisticsNodeEntity;
import me.almana.logisticsnetworks.menu.NodeMenu;
import me.almana.logisticsnetworks.registration.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

public class LogisticsNetworksNodeWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.WRENCH) {
            return false;
        }
        Level level = context.getLevel();
        return level != null && LogisticsNetworksCompatUtil.findNodeAt(level, context.getPos()) != null;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        LogisticsNodeEntity node = LogisticsNetworksCompatUtil.findNodeAt(level, context.getPos());
        if (node == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!node.isOwnedBy(player)) {
            player.displayClientMessage(Component.translatable("message.logisticsnetworks.not_owner"), true);
            return InteractionResult.SUCCESS;
        }

        if (node.getOwnerUUID() == null) {
            node.setOwnerUUID(player.getUUID());
        }

        if (player.isShiftKeyDown()) {
            return removeNode(level, node, player);
        }
        return openNodeGui(node, player);
    }

    private InteractionResult removeNode(Level level, LogisticsNodeEntity node, Player player) {
        if (level instanceof ServerLevel serverLevel && node.getNetworkId() != null) {
            NetworkRegistry.get(serverLevel).removeNodeFromNetwork(node.getNetworkId(), node.getUUID());
        }

        node.dropFilters();
        node.dropUpgrades();
        node.spawnAtLocation(Registration.LOGISTICS_NODE_ITEM.get());
        node.discard();

        level.playSound(null, node.blockPosition(), SoundEvents.METAL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.logisticsnetworks.node_removed"), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult openNodeGui(LogisticsNodeEntity node, Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("gui.logisticsnetworks.node_config");
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player currentPlayer) {
                    return new NodeMenu(containerId, playerInventory, node);
                }
            }, buf -> LogisticsNetworksCompatUtil.writeNodeSyncData(buf, node, player.registryAccess()));

            if (serverPlayer.containerMenu instanceof NodeMenu menu) {
                menu.sendNetworkListToClient(serverPlayer);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
