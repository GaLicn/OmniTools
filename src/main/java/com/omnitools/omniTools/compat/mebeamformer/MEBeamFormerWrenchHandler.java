package com.omnitools.omniTools.compat.mebeamformer;

import com.mebeamformer.item.LaserBindingTool;
import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MEBeamFormerWrenchHandler implements IWrenchHandler {
    private static LaserBindingTool CACHED_TOOL = null;

    private static LaserBindingTool getLaserBindingTool() {
        if (CACHED_TOOL != null) {
            return CACHED_TOOL;
        }
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof LaserBindingTool tool) {
                CACHED_TOOL = tool;
                return tool;
            }
        }
        return null;
    }

    @Override
    public boolean canHandle(WrenchContext context) {
        // 只在连接模式下处理
        return context.getCurrentMode() == com.omnitools.omniTools.core.ToolMode.LINK;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level world = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();
        BlockPos pos = context.getPos();
        Direction face = context.getFace();

        // 推断当前是主手还是副手
        InteractionHand hand = player.getMainHandItem() == stack
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;

        // 构造一个等价的 UseOnContext，位置取方块中心即可
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(pos),
                face,
                pos,
                false
        );
        UseOnContext useOnContext = new UseOnContext(player, hand, hit);

        // 直接复用 LaserBindingTool 的逻辑
        LaserBindingTool tool = getLaserBindingTool();
        if (tool == null) {
            return InteractionResult.PASS;
        }
        return tool.useOn(useOnContext);
    }
}
