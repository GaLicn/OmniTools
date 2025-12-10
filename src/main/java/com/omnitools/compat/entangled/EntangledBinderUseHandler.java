package com.omnitools.compat.entangled;

import com.omnitools.api.IUseHandler;
import com.omnitools.api.UseContext;
import com.omnitools.core.ToolMode;
import com.supermartijn642.core.TextComponents;
import net.minecraft.ChatFormatting;
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
        var nbt = stack.getTag();
        return player.isCrouching() && nbt != null && nbt.getBoolean("bound");
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
            var nbt = stack.getTag();
            if (nbt != null) {
                nbt.remove("bound");
                nbt.remove("dimension");
                nbt.remove("boundx");
                nbt.remove("boundy");
                nbt.remove("boundz");
                nbt.remove("blockstate");
            }
            player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.clear").color(ChatFormatting.YELLOW).get(), true);
        }

        return InteractionResultHolder.success(stack);
    }
}
