package com.omnitools.core;

import com.omnitools.api.UseContext;
import com.omnitools.api.WrenchContext;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.List;

public class OmniToolItem extends Item {
    private static final String MODE_TAG = "ToolMode";

    public OmniToolItem(Properties properties) {
        super(properties);
    }

    public static ToolMode getMode(ItemStack stack) {
        if (stack.isEmpty()) {
            return ToolMode.WRENCH;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(MODE_TAG)) {
            ToolMode mode = ToolMode.fromId(tag.getString(MODE_TAG));
            if (mode == ToolMode.RENAME && !isRenameModeEnabled()) {
                return ToolMode.WRENCH;
            }
            return mode;
        }
        return ToolMode.WRENCH;
    }

    public static void setMode(ItemStack stack, ToolMode mode) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(MODE_TAG, mode.getId());
    }

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

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.omnitools.omni_wrench.main").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.omnitools.omni_wrench.controls").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
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
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null) {
            WrenchContext wrenchContext = new WrenchContext(
                    level,
                    context.getClickedPos(),
                    context.getClickedFace(),
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
