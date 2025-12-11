package com.omnitools.compat.powah;

import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import owmii.powah.Powah;
import owmii.powah.block.energizing.EnergizingOrbTile;
import owmii.powah.block.energizing.EnergizingRodTile;

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
        var nbt = stack.getOrCreateTag();
        
        if (nbt.contains("OrbPos")) {
            BlockPos orbPos = NbtUtils.readBlockPos(nbt.getCompound("OrbPos"));
            if (level.getBlockEntity(orbPos) instanceof EnergizingOrbTile) {
                Vec3 v3d = Vec3.atCenterOf(orbPos);
                Vec3 rodPos = Vec3.atCenterOf(pos);
                if ((int) v3d.distanceTo(rodPos) <= Powah.config().general.energizing_range) {
                    rod.setOrbPos(orbPos);
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.done").withStyle(ChatFormatting.GOLD)), true);
                } else {
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.fail").withStyle(ChatFormatting.RED)), true);
                }
            }
            nbt.remove("OrbPos");
        } else {
            nbt.put("RodPos", NbtUtils.writeBlockPos(pos));
            player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.start").withStyle(ChatFormatting.YELLOW)), true);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleOrb(Level level, BlockPos pos, Player player, ItemStack stack) {
        var nbt = stack.getOrCreateTag();
        
        if (nbt.contains("RodPos")) {
            BlockPos rodPos = NbtUtils.readBlockPos(nbt.getCompound("RodPos"));
            if (level.getBlockEntity(rodPos) instanceof EnergizingRodTile rod) {
                Vec3 v3d = Vec3.atCenterOf(rodPos);
                Vec3 orbPos = Vec3.atCenterOf(pos);
                if ((int) v3d.distanceTo(orbPos) <= Powah.config().general.energizing_range) {
                    rod.setOrbPos(pos);
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.done").withStyle(ChatFormatting.GOLD)), true);
                } else {
                    player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.fail").withStyle(ChatFormatting.RED)), true);
                }
            }
            nbt.remove("RodPos");
        } else {
            nbt.put("OrbPos", NbtUtils.writeBlockPos(pos));
            player.displayClientMessage(Component.translatable("omnitools.compat.powah").append(" ").append(Component.translatable("chat.powah.wrench.link.start").withStyle(ChatFormatting.YELLOW)), true);
        }
        return InteractionResult.SUCCESS;
    }
}
