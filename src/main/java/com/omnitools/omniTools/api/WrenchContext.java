package com.omnitools.omniTools.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WrenchContext {
    private final Level level;
    private final BlockPos pos;
    private final Direction face;
    private final Player player;
    private final ItemStack stack;

    public WrenchContext(Level level, BlockPos pos, Direction face, Player player, ItemStack stack) {
        this.level = level;
        this.pos = pos;
        this.face = face;
        this.player = player;
        this.stack = stack;
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction getFace() {
        return face;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemStack getStack() {
        return stack;
    }
}
