package com.omnitools.omniTools.compat.logisticsnetworks;

import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ToolMode;
import com.omnitools.omniTools.core.WrenchHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class LogisticsNetworksNodePreHandler {
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null || event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        if (level == null) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }

        ToolMode mode = OmniToolItem.getMode(stack);
        if (mode != ToolMode.WRENCH && mode != ToolMode.CONFIGURATION) {
            return;
        }

        BlockPos pos = event.getPos();
        if (LogisticsNetworksCompatUtil.findNodeAt(level, pos) == null) {
            return;
        }

        Direction face = event.getFace() == null ? Direction.UP : event.getFace();
        Vec3 clickLocation = event.getHitVec() != null ? event.getHitVec().getLocation() : Vec3.atCenterOf(pos);
        WrenchContext context = new WrenchContext(level, pos, face, clickLocation, player, stack);
        InteractionResult result = WrenchHandlerRegistry.handle(context);
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }
}
