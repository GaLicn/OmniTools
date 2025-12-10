package com.omnitools.compat.entangled;

import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockEntity;
import com.supermartijn642.entangled.EntangledConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class EntangledBinderWrenchHandler implements IWrenchHandler {
    @Override
    public boolean canHandle(WrenchContext context) {
        return context.getCurrentMode() == ToolMode.LINK;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getPos();
        ItemStack stack = context.getStack();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block instanceof EntangledBlock) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            BlockEntity entity = level.getBlockEntity(pos);
            if (!(entity instanceof EntangledBlockEntity entangled)) {
                return InteractionResult.PASS;
            }

            var nbt = stack.getTag();
            if (nbt != null && nbt.getBoolean("bound")) {
                ResourceLocation targetDimension = new ResourceLocation(nbt.getString("dimension"));
                BlockPos targetPos = new BlockPos(nbt.getInt("boundx"), nbt.getInt("boundy"), nbt.getInt("boundz"));
                if (EntangledBlock.canBindTo(level.dimension().location(), pos, targetDimension, targetPos)) {
                    entangled.bind(targetPos, targetDimension);
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get(), true);
                } else if (CommonUtils.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension)) == null) {
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(ChatFormatting.RED).get(), true);
                } else if (!level.dimension().location().equals(targetDimension) && !EntangledConfig.allowDimensional.get()) {
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get(), true);
                } else if (pos.equals(targetPos)) {
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.self").color(ChatFormatting.RED).get(), true);
                } else {
                    player.displayClientMessage(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get(), true);
                }
            } else {
                player.displayClientMessage(TextComponents.translation("entangled.entangled_block.no_selection").color(ChatFormatting.RED).get(), true);
            }

            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        var nbt = stack.getOrCreateTag();
        ResourceLocation currentDimension = level.dimension().location();
        if (nbt.getBoolean("bound") && nbt.getString("dimension").equals(currentDimension.toString()) && 
            nbt.getInt("boundx") == pos.getX() && nbt.getInt("boundy") == pos.getY() && nbt.getInt("boundz") == pos.getZ()) {
            return InteractionResult.SUCCESS;
        }

        BlockState targetState = level.getBlockState(pos);
        nbt.putBoolean("bound", true);
        nbt.putString("dimension", currentDimension.toString());
        nbt.putInt("boundx", pos.getX());
        nbt.putInt("boundy", pos.getY());
        nbt.putInt("boundz", pos.getZ());
        nbt.putInt("blockstate", Block.getId(targetState));
        player.displayClientMessage(TextComponents.translation("entangled.entangled_binder.select").color(ChatFormatting.YELLOW).get(), true);
        return InteractionResult.SUCCESS;
    }
}
