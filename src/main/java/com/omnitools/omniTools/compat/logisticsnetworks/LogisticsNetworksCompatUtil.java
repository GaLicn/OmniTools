package com.omnitools.omniTools.compat.logisticsnetworks;

import me.almana.logisticsnetworks.data.NodeClipboardConfig;
import me.almana.logisticsnetworks.data.NetworkRegistry;
import me.almana.logisticsnetworks.entity.LogisticsNodeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class LogisticsNetworksCompatUtil {
    private static final String KEY_ROOT = "omnitools_logisticsnetworks";
    private static final String KEY_CLIPBOARD = "clipboard";

    private LogisticsNetworksCompatUtil() {
    }

    @Nullable
    static LogisticsNodeEntity findNodeAt(Level level, BlockPos pos) {
        List<LogisticsNodeEntity> nodes = level.getEntitiesOfClass(LogisticsNodeEntity.class, new AABB(pos).inflate(0.5));
        for (LogisticsNodeEntity node : nodes) {
            if (node.getAttachedPos().equals(pos) && node.isActive()) {
                return node;
            }
        }
        return null;
    }

    static void markNodeNetworkDirty(LogisticsNodeEntity node) {
        if (node.getNetworkId() != null && node.level() instanceof ServerLevel serverLevel) {
            NetworkRegistry.get(serverLevel).markNetworkDirty(node.getNetworkId());
        }
    }

    static void writeNodeSyncData(FriendlyByteBuf buf, LogisticsNodeEntity node, HolderLookup.Provider provider) {
        buf.writeVarInt(node.getId());
        for (int i = 0; i < LogisticsNodeEntity.CHANNEL_COUNT; i++) {
            buf.writeNbt(node.getChannel(i).save(provider));
        }
        for (int i = 0; i < LogisticsNodeEntity.UPGRADE_SLOT_COUNT; i++) {
            buf.writeNbt(node.getUpgradeItem(i).saveOptional(provider));
        }
    }

    @Nullable
    static NodeClipboardConfig getClipboard(ItemStack stack, HolderLookup.Provider provider) {
        CompoundTag root = getRootTag(stack);
        if (!root.contains(KEY_CLIPBOARD, Tag.TAG_COMPOUND)) {
            return null;
        }
        return NodeClipboardConfig.load(root.getCompound(KEY_CLIPBOARD), provider);
    }

    static boolean hasClipboard(ItemStack stack) {
        return getRootTag(stack).contains(KEY_CLIPBOARD, Tag.TAG_COMPOUND);
    }

    static void setClipboard(ItemStack stack, @Nullable NodeClipboardConfig clipboard, HolderLookup.Provider provider) {
        if (stack.isEmpty()) {
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, customTag -> {
            CompoundTag root = getRootTag(customTag);
            if (clipboard == null) {
                root.remove(KEY_CLIPBOARD);
            } else {
                root.put(KEY_CLIPBOARD, clipboard.save(provider));
            }
            writeRoot(customTag, root);
        });
    }

    private static CompoundTag getRootTag(ItemStack stack) {
        return getRootTag(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    private static CompoundTag getRootTag(CompoundTag customTag) {
        if (customTag.contains(KEY_ROOT, Tag.TAG_COMPOUND)) {
            return customTag.getCompound(KEY_ROOT).copy();
        }
        return new CompoundTag();
    }

    private static void writeRoot(CompoundTag customTag, CompoundTag root) {
        if (root.isEmpty()) {
            customTag.remove(KEY_ROOT);
        } else {
            customTag.put(KEY_ROOT, root);
        }
    }
}
