package com.omnitools.omniTools.compat.mekanism;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.core.ToolMode;
import mekanism.api.IConfigCardAccess;
import mekanism.api.SerializationConstants;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.EnumColor;
import mekanism.api.text.TextComponentUtil;
import mekanism.common.MekanismLang;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class MekanismConfigCardWrenchHandler implements IWrenchHandler {
    @Nullable
    private static IConfigCardAccess getConfigCardAccess(Level level, BlockPos pos, Direction face) {
        IConfigCardAccess configCardAccess = WorldUtils.getCapability(level, Capabilities.CONFIG_CARD, pos, face);
        if (configCardAccess != null) {
            return configCardAccess;
        }
        BlockEntity tile = WorldUtils.getTileEntity(level, pos);
        if (tile == null) {
            return null;
        }
        return WorldUtils.getCapability(level, Capabilities.CONFIG_CARD, pos, null, tile, null);
    }

    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }

        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        IConfigCardAccess configCardAccess = getConfigCardAccess(level, pos, face);
        return configCardAccess != null;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        IConfigCardAccess configCardAccess = getConfigCardAccess(level, pos, face);
        if (configCardAccess == null) {
            return InteractionResult.PASS;
        }

        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, level, pos)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getStack();
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                String translationKey = configCardAccess.getConfigCardName();
                CompoundTag data = configCardAccess.getConfigurationData(level.registryAccess(), player);
                data.putString(SerializationConstants.DATA_NAME, translationKey);
                NBTUtils.writeRegistryEntry(data, SerializationConstants.DATA_TYPE, BuiltInRegistries.BLOCK, configCardAccess.getConfigurationDataType());
                stack.set(MekanismDataComponents.CONFIGURATION_DATA, data);
                player.displayClientMessage(MekanismLang.CONFIG_CARD_GOT.translate(EnumColor.INDIGO, TextComponentUtil.translate(translationKey)), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.value().trigger(serverPlayer, true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        CompoundTag data = getData(stack);
        Block storedType = getStoredType(data);
        if (storedType == null) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            if (configCardAccess.isConfigurationDataCompatible(storedType)) {
                configCardAccess.setConfigurationData(level.registryAccess(), player, data);
                configCardAccess.configurationDataSet();
                player.displayClientMessage(MekanismLang.CONFIG_CARD_SET.translate(EnumColor.INDIGO, getConfigCardName(data)), true);
                if (player instanceof ServerPlayer serverPlayer) {
                    MekanismCriteriaTriggers.CONFIGURATION_CARD.value().trigger(serverPlayer, false);
                }
            } else {
                player.displayClientMessage(MekanismLang.CONFIG_CARD_UNEQUAL.translateColored(EnumColor.RED), true);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    private static CompoundTag getData(ItemStack stack) {
        CompoundTag data = stack.get(MekanismDataComponents.CONFIGURATION_DATA);
        if (data == null || data.isEmpty()) {
            return null;
        }
        return data;
    }

    @Nullable
    private static Block getStoredType(@Nullable CompoundTag data) {
        if (data == null || !data.contains(SerializationConstants.DATA_TYPE, Tag.TAG_STRING)) {
            return null;
        }
        ResourceLocation blockRegistryName = ResourceLocation.tryParse(data.getString(SerializationConstants.DATA_TYPE));
        return blockRegistryName == null ? null : BuiltInRegistries.BLOCK.get(blockRegistryName);
    }

    private static Component getConfigCardName(@Nullable CompoundTag data) {
        if (data == null || !data.contains(SerializationConstants.DATA_NAME, Tag.TAG_STRING)) {
            return MekanismLang.NONE.translate();
        }
        return TextComponentUtil.translate(data.getString(SerializationConstants.DATA_NAME));
    }
}