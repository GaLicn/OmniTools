package com.omnitools.omniTools.compat.powah;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import owmii.powah.Powah;
import owmii.powah.block.energizing.EnergizingOrbTile;
import owmii.powah.block.energizing.EnergizingRodTile;
import owmii.powah.components.PowahComponents;
import owmii.powah.util.math.V3d;

public class PowahLinkWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(context.getPos());
        return be instanceof EnergizingRodTile || be instanceof EnergizingOrbTile;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        ItemStack stack = context.getStack();

        if (be instanceof EnergizingRodTile rod) {
            return handleRod(level, pos, player, stack, rod);
        } else if (be instanceof EnergizingOrbTile) {
            return handleOrb(level, pos, player, stack);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult handleRod(Level level, BlockPos pos, Player player, ItemStack stack, EnergizingRodTile rod) {
        BlockPos orbPos = stack.get(PowahComponents.LINK_ORB_POS);
        if (orbPos != null) {
            if (level.getBlockEntity(orbPos) instanceof EnergizingOrbTile) {
                V3d v3d = V3d.from(orbPos);
                if ((int) v3d.distance(pos) <= Powah.config().general.energizing_range) {
                    rod.setOrbPos(orbPos);
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.done").withStyle(ChatFormatting.GOLD)), true);
                } else {
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.fail").withStyle(ChatFormatting.RED)), true);
                }
            }
            stack.remove(PowahComponents.LINK_ORB_POS);
        } else {
            stack.set(PowahComponents.LINK_ROD_POS, pos);
            player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.start").withStyle(ChatFormatting.YELLOW)), true);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleOrb(Level level, BlockPos pos, Player player, ItemStack stack) {
        BlockPos rodPos = stack.get(PowahComponents.LINK_ROD_POS);
        if (rodPos != null) {
            if (level.getBlockEntity(rodPos) instanceof EnergizingRodTile rod) {
                V3d v3d = V3d.from(rodPos);
                if ((int) v3d.distance(pos) <= Powah.config().general.energizing_range) {
                    rod.setOrbPos(pos);
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.done").withStyle(ChatFormatting.GOLD)), true);
                } else {
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.fail").withStyle(ChatFormatting.RED)), true);
                }
            }
            stack.remove(PowahComponents.LINK_ROD_POS);
        } else {
            stack.set(PowahComponents.LINK_ORB_POS, pos);
            player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.start").withStyle(ChatFormatting.YELLOW)), true);
        }
        return InteractionResult.SUCCESS;
    }
}
