package com.omnitools.api;

import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UseContext {
    private final Level level;
    private final Player player;
    private final InteractionHand hand;
    private final ItemStack stack;

    public UseContext(Level level, Player player, InteractionHand hand, ItemStack stack) {
        this.level = level;
        this.player = player;
        this.hand = hand;
        this.stack = stack;
    }

    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
        return player;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ToolMode getCurrentMode() {
        return OmniToolItem.getMode(stack);
    }
}
