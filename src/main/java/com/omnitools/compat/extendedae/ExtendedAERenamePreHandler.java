package com.omnitools.compat.extendedae;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.AEBasePart;
import appeng.util.InteractionUtil;
import com.glodblock.github.extendedae.container.ContainerRenamer;
import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ExtendedAERenamePreHandler {

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

        if (OmniToolItem.getMode(stack) != ToolMode.RENAME) {
            return;
        }

        if (InteractionUtil.isInAlternateUseMode(player)) {
            return;
        }

        BlockHitResult hitResult = event.getHitVec();
        var pos = hitResult.getBlockPos();
        var tile = level.getBlockEntity(pos);
        if (!(tile instanceof AEBaseBlockEntity)) {
            return;
        }

        if (tile instanceof CableBusBlockEntity cable) {
            var hitVec = hitResult.getLocation();
            Vec3 hitInBlock = new Vec3(hitVec.x - pos.getX(), hitVec.y - pos.getY(), hitVec.z - pos.getZ());
            var part = cable.selectPartLocal(hitInBlock).part;
            if (part instanceof AEBasePart aePart) {
                if (!level.isClientSide) {
                    MenuOpener.open(ContainerRenamer.TYPE, player, MenuLocators.forPart(aePart));
                }
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            }
        } else {
            if (!level.isClientSide) {
                MenuOpener.open(ContainerRenamer.TYPE, player, MenuLocators.forBlockEntity(tile));
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }
}
