package com.omnitools.omniTools.compat.logisticsnetworks;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import me.almana.logisticsnetworks.data.NodeClipboardConfig;
import me.almana.logisticsnetworks.entity.LogisticsNodeEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LogisticsNetworksClipboardWrenchHandler implements IWrenchHandler {
    private static final Component PREFIX = Component.translatable("omnitools.compat.logisticsnetworks").append(" ");

    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
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

        ItemStack stack = context.getStack();
        if (player.isShiftKeyDown()) {
            NodeClipboardConfig clipboard = NodeClipboardConfig.fromNode(node);
            LogisticsNetworksCompatUtil.setClipboard(stack, clipboard, player.registryAccess());
            player.displayClientMessage(PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.copied")), true);
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        NodeClipboardConfig clipboard = LogisticsNetworksCompatUtil.getClipboard(stack, serverPlayer.registryAccess());
        if (clipboard == null) {
            String key = LogisticsNetworksCompatUtil.hasClipboard(stack)
                    ? "message.logisticsnetworks.clipboard.invalid"
                    : "message.logisticsnetworks.clipboard.empty";
            player.displayClientMessage(PREFIX.copy().append(Component.translatable(key)), true);
            return InteractionResult.SUCCESS;
        }
        if (clipboard.isEffectivelyEmpty()) {
            player.displayClientMessage(PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.empty")), true);
            return InteractionResult.SUCCESS;
        }

        NodeClipboardConfig.PasteResult result = clipboard.applyToNode(serverPlayer, node, stack);
        switch (result) {
            case SUCCESS -> {
                LogisticsNetworksCompatUtil.markNodeNetworkDirty(node);
                player.displayClientMessage(PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.paste.success")), true);
            }
            case MISSING_ITEMS -> player.displayClientMessage(
                    PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.paste.missing_items")), true);
            case INVENTORY_FULL -> player.displayClientMessage(
                    PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.paste.no_space")), true);
            case INCOMPATIBLE_TARGET -> player.displayClientMessage(
                    PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.paste.incompatible")), true);
            case CLIPBOARD_INVALID -> player.displayClientMessage(
                    PREFIX.copy().append(Component.translatable("message.logisticsnetworks.clipboard.invalid")), true);
        }

        return InteractionResult.SUCCESS;
    }
}
