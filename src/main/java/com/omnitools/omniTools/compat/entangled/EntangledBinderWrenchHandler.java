package com.omnitools.omniTools.compat.entangled;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.entangled.EntangledBlock;
import com.supermartijn642.entangled.EntangledBlockEntity;
import com.supermartijn642.entangled.EntangledBinderItem;
import com.supermartijn642.entangled.EntangledConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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

            if (EntangledBinderItem.isBound(stack)) {
                ResourceLocation targetDimension = EntangledBinderItem.getBoundDimension(stack);
                BlockPos targetPos = EntangledBinderItem.getBoundPosition(stack);
                if (EntangledBlock.canBindTo(level.dimension().location(), pos, targetDimension, targetPos)) {
                    entangled.bind(targetPos, targetDimension);
                    player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_block.bind").color(ChatFormatting.YELLOW).get()), true);
                } else if (CommonUtils.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension)) == null) {
                    player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_binder.unknown_dimension", targetDimension).color(ChatFormatting.RED).get()), true);
                } else if (!level.dimension().location().equals(targetDimension) && !EntangledConfig.allowDimensional.get()) {
                    player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_block.wrong_dimension").color(ChatFormatting.RED).get()), true);
                } else if (pos.equals(targetPos)) {
                    player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_block.self").color(ChatFormatting.RED).get()), true);
                } else {
                    player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_block.too_far").color(ChatFormatting.RED).get()), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_block.no_selection").color(ChatFormatting.RED).get()), true);
            }

            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        EntangledBinderItem.BinderTarget target = stack.get(EntangledBinderItem.BINDER_TARGET);
        ResourceLocation currentDimension = level.dimension().location();
        if (target != null && target.dimension().equals(currentDimension) && target.pos().equals(pos)) {
            return InteractionResult.SUCCESS;
        }

        BlockState targetState = level.getBlockState(pos);
        Optional<BlockState> optionalState = Optional.of(targetState);
        stack.set(EntangledBinderItem.BINDER_TARGET, new EntangledBinderItem.BinderTarget(currentDimension, pos, optionalState));
        player.displayClientMessage(Component.translatable("omnitools.compat.entangled").append(" ").append(TextComponents.translation("entangled.entangled_binder.select").color(ChatFormatting.YELLOW).get()), true);
        return InteractionResult.SUCCESS;
    }
}
