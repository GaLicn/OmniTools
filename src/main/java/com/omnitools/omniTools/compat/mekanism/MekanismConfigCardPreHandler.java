package com.omnitools.omniTools.compat.mekanism;

import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ToolMode;
import com.omnitools.omniTools.core.WrenchHandlerRegistry;
import mekanism.api.IConfigCardAccess;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class MekanismConfigCardPreHandler {

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }
        Level level = event.getLevel();
        if (level == null) {
            return;
        }

        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }

        if (OmniToolItem.getMode(stack) != ToolMode.CONFIGURATION) {
            return;
        }

        BlockPos pos = event.getPos();
        Direction face = event.getFace();
        IConfigCardAccess configCardAccess = getConfigCardAccess(level, pos, face);
        if (configCardAccess == null) {
            return;
        }

        Vec3 clickLocation = event.getHitVec() != null ? event.getHitVec().getLocation() : Vec3.atCenterOf(pos);
        WrenchContext context = new WrenchContext(level, pos, face, clickLocation, player, stack);
        InteractionResult result = WrenchHandlerRegistry.handle(context);
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    @Nullable
    private static IConfigCardAccess getConfigCardAccess(Level level, BlockPos pos, Direction face) {
        IConfigCardAccess configCardAccess = WorldUtils.getCapability(level, Capabilities.CONFIG_CARD, pos, face);
        if (configCardAccess != null) {
            return configCardAccess;
        }
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile == null) {
            return null;
        }
        return WorldUtils.getCapability(level, Capabilities.CONFIG_CARD, pos, null, tile, null);
    }
}
