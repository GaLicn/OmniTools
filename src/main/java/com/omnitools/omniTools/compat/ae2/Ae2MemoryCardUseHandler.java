package com.omnitools.omniTools.compat.ae2;

import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import appeng.api.ids.AEComponents;
import appeng.core.localization.PlayerMessages;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.tools.MemoryCardItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class Ae2MemoryCardUseHandler implements IUseHandler {
    private static final Component PREFIX = Component.translatable("omnitools.compat.ae2").append(" ");

    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Player player = context.getPlayer();
        return player != null && player.isShiftKeyDown();
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (level != null && player != null && !level.isClientSide) {
            boolean hasData = false;
            for (var holder : BuiltInRegistries.DATA_COMPONENT_TYPE.getTagOrEmpty(ConventionTags.EXPORTED_SETTINGS)) {
                if (stack.get(holder.value()) != null) {
                    hasData = true;
                    break;
                }
            }
            if (stack.get(AEComponents.MEMORY_CARD_COLORS) != null) {
                hasData = true;
            }

            if (hasData) {
                MemoryCardItem.clearCard(stack);
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.SettingCleared.text()), true);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }
            return InteractionResultHolder.pass(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level != null && level.isClientSide);
    }
}
