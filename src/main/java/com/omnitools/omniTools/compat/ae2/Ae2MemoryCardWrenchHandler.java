package com.omnitools.omniTools.compat.ae2;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import appeng.api.ids.AEComponents;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.localization.PlayerMessages;
import appeng.util.SettingsFrom;
import appeng.items.tools.MemoryCardItem;
import appeng.parts.AEBasePart;
import appeng.util.InteractionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class Ae2MemoryCardWrenchHandler implements IWrenchHandler {

    private static final Component PREFIX = Component.translatable("omnitools.compat.ae2").append(" ");

    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        return resolveTarget(level, context.getPos(), context.getFace()) != null;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        Target target = resolveTarget(level, context.getPos(), context.getFace());
        if (target == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();
        boolean alt = InteractionUtil.isInAlternateUseMode(player);

        if (alt) {
            // 复制配置到扳手
            DataComponentMap.Builder builder = DataComponentMap.builder();
            exportSettings(target, builder, player);
            DataComponentMap settings = builder.build();
            if (!settings.isEmpty()) {
                MemoryCardItem.clearCard(stack);
                stack.applyComponents(settings);
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.SavedSettings.text()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // 粘贴配置到目标
        Component storedName = stack.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
        Component targetName = target.name();
        if (storedName != null && storedName.equals(targetName)) {
            importSettings(target, stack.getComponents(), player);
            player.displayClientMessage(PREFIX.copy().append(PlayerMessages.LoadedSettings.text()), true);
        } else {
            Set<DataComponentType<?>> imported = importGenericSettings(target, stack.getComponents(), player);
            if (imported.isEmpty()) {
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.InvalidMachine.text()), true);
            } else {
                Component restored = joinComponents(imported.stream()
                        .map(type -> Component.translatable(MemoryCardItem.getSettingTranslationKey(type)))
                        .collect(Collectors.toList()));
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.InvalidMachinePartiallyRestored.text(restored)), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void exportSettings(Target target, DataComponentMap.Builder builder, Player player) {
        if (target.kind == Kind.BLOCK) {
            target.block.exportSettings(SettingsFrom.MEMORY_CARD, builder, player);
        } else {
            target.part.exportSettings(SettingsFrom.MEMORY_CARD, builder);
        }
    }

    private void importSettings(Target target, DataComponentMap input, Player player) {
        if (target.kind == Kind.BLOCK) {
            target.block.importSettings(SettingsFrom.MEMORY_CARD, input, player);
        } else {
            target.part.importSettings(SettingsFrom.MEMORY_CARD, input, player);
        }
    }

    private Set<DataComponentType<?>> importGenericSettings(Target target, DataComponentMap input, Player player) {
        return target.kind == Kind.BLOCK
                ? MemoryCardItem.importGenericSettings(target.block, input, player)
                : MemoryCardItem.importGenericSettings(target.part, input, player);
    }

    private Component joinComponents(Collection<Component> components) {
        if (components == null || components.isEmpty()) {
            return Component.literal("");
        }
        List<Component> parts = new ArrayList<>();
        boolean first = true;
        for (Component c : components) {
            if (!first) {
                parts.add(Component.literal(", "));
            }
            parts.add(c);
            first = false;
        }
        Component result = Component.empty();
        for (Component c : parts) {
            result = result.copy().append(c);
        }
        return result;
    }

    private Target resolveTarget(Level level, BlockPos pos, Direction face) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AEBaseBlockEntity ae) {
            Component name = Component.translatable(ae.getBlockState().getBlock().getDescriptionId());
            return new Target(Kind.BLOCK, ae, null, name);
        }
        if (be instanceof IPartHost host) {
            IPart part = host.getPart(face);
            if (part == null) {
                part = host.getPart(null);
            }
            if (part instanceof AEBasePart aePart) {
                IPartItem<?> item = aePart.getPartItem();
                Component name = item == null ? Component.literal("part") : Component.translatable(item.asItem().getDescriptionId());
                return new Target(Kind.PART, null, aePart, name);
            }
        }
        return null;
    }

    private enum Kind {BLOCK, PART}

    private record Target(Kind kind, AEBaseBlockEntity block, AEBasePart part, Component name) {
    }
}
