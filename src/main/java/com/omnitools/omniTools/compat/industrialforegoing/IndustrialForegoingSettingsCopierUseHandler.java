package com.omnitools.omniTools.compat.industrialforegoing;

import com.buuz135.industrial.utils.IFAttachments;
import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IndustrialForegoingSettingsCopierUseHandler implements IUseHandler {

    private static final Component PREFIX = Component.translatable("omnitools.compat.industrialforegoing").append(" ");

    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }
        return player.isShiftKeyDown();
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (!stack.has(IFAttachments.SETTINGS_COPIER)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level != null && player != null && !level.isClientSide) {
            stack.remove(IFAttachments.SETTINGS_COPIER);
            player.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 1.0F);
            player.displayClientMessage(PREFIX.copy().append(Component.translatable("text.industrialforegoing.machine_settings_copier.settings_clear")), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level != null && level.isClientSide);
    }
}
