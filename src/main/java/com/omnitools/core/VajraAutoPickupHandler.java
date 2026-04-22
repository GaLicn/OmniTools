package com.omnitools.core;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class VajraAutoPickupHandler {
    private static final long MAX_CONTEXT_AGE = 5L;
    private static final double MAX_MATCH_DISTANCE_SQR = 4.0D;
    private static final int MAX_CONTEXTS_PER_PLAYER = 16;
    private static final Map<UUID, Deque<PendingAutoPickup>> PENDING_PICKUPS = new HashMap<>();

    private VajraAutoPickupHandler() {
    }

    public static void markAutoPickup(Player player, BlockPos pos) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Deque<PendingAutoPickup> contexts = PENDING_PICKUPS.computeIfAbsent(serverPlayer.getUUID(), ignored -> new ArrayDeque<>());
        cleanupExpiredContexts(contexts, serverPlayer.serverLevel());
        contexts.addLast(new PendingAutoPickup(serverPlayer.level().dimension(), pos.immutable(), serverPlayer.level().getGameTime()));
        while (contexts.size() > MAX_CONTEXTS_PER_PLAYER) {
            contexts.removeFirst();
        }
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || event.loadedFromDisk()) {
            return;
        }
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack originalStack = itemEntity.getItem();
        if (originalStack.isEmpty()) {
            return;
        }

        PendingMatch match = findMatchingContext(serverLevel, itemEntity);
        if (match == null) {
            return;
        }

        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(match.playerId());
        if (player == null || !player.isAlive()) {
            removePlayerContext(match.playerId(), match.context());
            return;
        }

        ItemStack remaining = originalStack.copy();
        player.getInventory().add(remaining);
        if (remaining.getCount() == originalStack.getCount()) {
            return;
        }

        if (remaining.isEmpty()) {
            event.setCanceled(true);
        } else {
            itemEntity.setItem(remaining);
        }
    }

    private static PendingMatch findMatchingContext(ServerLevel level, ItemEntity itemEntity) {
        long gameTime = level.getGameTime();
        PendingMatch bestMatch = null;
        double bestDistance = Double.MAX_VALUE;
        Iterator<Map.Entry<UUID, Deque<PendingAutoPickup>>> playerIterator = PENDING_PICKUPS.entrySet().iterator();
        while (playerIterator.hasNext()) {
            Map.Entry<UUID, Deque<PendingAutoPickup>> entry = playerIterator.next();
            Deque<PendingAutoPickup> contexts = entry.getValue();
            cleanupExpiredContexts(contexts, level);
            if (contexts.isEmpty()) {
                playerIterator.remove();
                continue;
            }

            Iterator<PendingAutoPickup> contextIterator = contexts.descendingIterator();
            while (contextIterator.hasNext()) {
                PendingAutoPickup context = contextIterator.next();
                if (context.gameTime() > gameTime) {
                    continue;
                }
                if (!context.dimension().equals(level.dimension())) {
                    continue;
                }

                double distance = Vec3.atCenterOf(context.pos()).distanceToSqr(itemEntity.position());
                if (distance > MAX_MATCH_DISTANCE_SQR) {
                    continue;
                }
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestMatch = new PendingMatch(entry.getKey(), context);
                }
            }
        }

        return bestMatch;
    }

    private static void removePlayerContext(UUID playerId, PendingAutoPickup contextToRemove) {
        Deque<PendingAutoPickup> contexts = PENDING_PICKUPS.get(playerId);
        if (contexts == null) {
            return;
        }

        contexts.remove(contextToRemove);
        if (contexts.isEmpty()) {
            PENDING_PICKUPS.remove(playerId);
        }
    }

    private static void cleanupExpiredContexts(Deque<PendingAutoPickup> contexts, ServerLevel level) {
        long minGameTime = level.getGameTime() - MAX_CONTEXT_AGE;
        while (!contexts.isEmpty()) {
            PendingAutoPickup context = contexts.peekFirst();
            if (!context.dimension().equals(level.dimension()) || context.gameTime() < minGameTime) {
                contexts.removeFirst();
                continue;
            }
            break;
        }
    }

    private record PendingAutoPickup(ResourceKey<Level> dimension, BlockPos pos, long gameTime) {
    }

    private record PendingMatch(UUID playerId, PendingAutoPickup context) {
    }
}
