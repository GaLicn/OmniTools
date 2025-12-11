package com.omnitools.compat.draconicevolution;

import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.utils.ItemNBTHelper;
import com.brandon3055.draconicevolution.api.energy.ICrystalBinder;
import com.brandon3055.draconicevolution.api.energy.ICrystalLink;
import com.brandon3055.draconicevolution.blocks.energynet.tileentity.TileCrystalBase;
import com.omnitools.api.IWrenchHandler;
import com.omnitools.api.WrenchContext;
import com.omnitools.core.ToolMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;


public class DraconicLinkWrenchHandler implements IWrenchHandler {

    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        BlockEntity be = level.getBlockEntity(context.getPos());
        if (be instanceof ICrystalLink) {
            return true;
        }
        ItemStack stack = context.getStack();
        return isBound(stack);
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }
        // 仅在服务端执行业务逻辑，客户端直接认为操作成功
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getStack();
        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        BlockEntity tile = level.getBlockEntity(pos);

        boolean isLinkable = tile instanceof ICrystalLink;
        boolean isBound = isBound(stack);

        // 选中源：SHIFT + 右键可链接方块（仅当尚未绑定）
        if (isLinkable && player.isShiftKeyDown() && !isBound) {
            bind(stack, pos);
            ChatHelper.sendIndexed(player, Component.translatable("gui.draconicevolution.energy_net.pos_saved_to_tool"), TileCrystalBase.MSG_ID);
            return InteractionResult.SUCCESS;
        }

        // 未绑定但点到可链接方块，提示需要先绑定
        if (isLinkable && !isBound) {
            ChatHelper.sendIndexed(player, Component.translatable("gui.draconicevolution.energy_net.tool_not_bound"), TileCrystalBase.MSG_ID);
            return InteractionResult.SUCCESS;
        }

        // 已绑定：尝试对目标执行 binderUsed（建立或断开链接），无需必须蹲下
        if (isBound) {
            BlockPos boundPos = getBound(stack);
            if (boundPos.equals(pos)) {
                ChatHelper.sendIndexed(player, Component.translatable("gui.draconicevolution.energy_net.link_to_self"), TileCrystalBase.MSG_ID);
                return InteractionResult.SUCCESS;
            }

            BlockEntity boundTile = level.getBlockEntity(boundPos);
            if (boundTile instanceof ICrystalLink link) {
                link.binderUsed(player, pos, face);
            } else {
                ChatHelper.sendIndexed(player, Component.translatable("gui.draconicevolution.energy_net.bound_to_invalid"), TileCrystalBase.MSG_ID);
            }
            // 与原版 Binder 行为一致，无论 binderUsed 返回值与否都消费操作
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private boolean isBound(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(ICrystalBinder.BINDER_TAG, 11);
    }

    private void bind(ItemStack stack, BlockPos pos) {
        ItemNBTHelper.getCompound(stack).putIntArray(ICrystalBinder.BINDER_TAG, new int[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    private BlockPos getBound(ItemStack stack) {
        int[] intArray = stack.getTag().getIntArray(ICrystalBinder.BINDER_TAG);
        return new BlockPos(intArray[0], intArray[1], intArray[2]);
    }
}
