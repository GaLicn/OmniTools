package com.omnitools.omniTools.compat.industrialforegoing;

import com.buuz135.industrial.api.IMachineSettings;
import com.buuz135.industrial.utils.IFAttachments;
import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class IndustrialForegoingSettingsCopierWrenchHandler implements IWrenchHandler {

    private static final Component PREFIX = Component.translatable("omnitools.compat.industrialforegoing").append(" ");

    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        return level.getBlockEntity(context.getPos()) instanceof IMachineSettings;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        var blockEntity = level.getBlockEntity(context.getPos());
        if (!(blockEntity instanceof IMachineSettings machineSettings)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();

        if (!level.isClientSide) {
            if (stack.has(IFAttachments.SETTINGS_COPIER)) {
                machineSettings.loadSettings(player, stack.get(IFAttachments.SETTINGS_COPIER));
                player.playSound(SoundEvents.ANVIL_USE, 0.1F, 1.0F);
                player.displayClientMessage(PREFIX.copy().append(Component.translatable("text.industrialforegoing.machine_settings_copier.settings_stored")), true);
            } else {
                CompoundTag tag = new CompoundTag();
                machineSettings.saveSettings(player, tag);
                stack.set(IFAttachments.SETTINGS_COPIER, tag);
                player.playSound(SoundEvents.ARROW_HIT_PLAYER, 0.5F, 1.0F);
                player.displayClientMessage(PREFIX.copy().append(Component.translatable("text.industrialforegoing.machine_settings_copier.settings_copied")), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
