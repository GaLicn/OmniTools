package com.omnitools.omniTools.core;

import java.util.List;

import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.api.WrenchContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public class OmniToolItem extends Item {
    private static final String MODE_TAG = "ToolMode";

    public OmniToolItem(Properties properties) {
        super(properties);
    }

    /**
     * 获取当前工具的模式
     */
    public static ToolMode getMode(ItemStack stack) {
        if (stack.isEmpty()) {
            return ToolMode.WRENCH;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(MODE_TAG)) {
                ToolMode mode = ToolMode.fromId(tag.getString(MODE_TAG));
                if (mode == ToolMode.RENAME && !isRenameModeEnabled()) {
                    return ToolMode.WRENCH;
                }
                return mode;
            }
        }
        return ToolMode.WRENCH;
    }

    /**
     * 设置工具的模式
     */
    public static void setMode(ItemStack stack, ToolMode mode) {
        if (stack.isEmpty()) {
            return;
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString(MODE_TAG, mode.getId());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * 循环切换到下一个模式
     */
    public static void cycleMode(ItemStack stack) {
        ToolMode current = getMode(stack);
        ToolMode[] modes = ToolMode.values();
        ToolMode next = current;
        for (int i = 1; i <= modes.length; i++) {
            ToolMode candidate = modes[(current.ordinal() + i) % modes.length];
            if (candidate != ToolMode.RENAME || isRenameModeEnabled()) {
                next = candidate;
                break;
            }
        }
        setMode(stack, next);
    }

    private static boolean isRenameModeEnabled() {
        return ModList.get() != null && ModList.get().isLoaded("ae2");
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.omnitools.omni_wrench.main").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.omnitools.omni_wrench.controls").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
        if (player != null) {
            UseContext useContext = new UseContext(level, player, hand, stack);
            InteractionResultHolder<ItemStack> result = UseHandlerRegistry.handle(useContext);
            if (result.getResult().consumesAction()) {
                return result;
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        var face = context.getClickedFace();

        if (player != null) {
            WrenchContext wrenchContext = new WrenchContext(
                    level,
                    pos,
                    face,
                    player,
                    context.getItemInHand()
            );

            InteractionResult result = WrenchHandlerRegistry.handle(wrenchContext);
            if (result.consumesAction()) {
                return result;
            }
        }

        return super.useOn(context);
    }
}
