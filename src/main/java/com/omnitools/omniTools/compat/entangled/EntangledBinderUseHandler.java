package com.omnitools.omniTools.compat.entangled;

import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.entangled.EntangledBinderItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntangledBinderUseHandler implements IUseHandler {
    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }
        ItemStack stack = context.getStack();
        return player.isCrouching() && stack.has(EntangledBinderItem.BINDER_TARGET);
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getStack();
        if (player == null) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide()) {
            stack.remove(EntangledBinderItem.BINDER_TARGET);
            player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_binder.clear").color(ChatFormatting.YELLOW).get()), true);
        }

        return InteractionResultHolder.success(stack);
    }
}
