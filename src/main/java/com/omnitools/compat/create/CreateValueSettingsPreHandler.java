package com.omnitools.compat.create;

import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * omni 扳手在扳手模式下，优先走 Create 的 ValueSettingsInputHandler
 */
public class CreateValueSettingsPreHandler {

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }

        // 只在普通扳手模式下启用 Create 的调节行为优先级
        if (OmniToolItem.getMode(stack) != ToolMode.WRENCH) {
            return;
        }

        if (event.isCanceled()) {
            return;
        }
        
        ValueSettingsInputHandler.onBlockActivated(event);
    }
}
