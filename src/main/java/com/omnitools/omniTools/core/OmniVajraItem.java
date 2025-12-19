package com.omnitools.omniTools.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;

public class OmniVajraItem extends Item {
    private static final String MINING_SPEED_TAG = "VajraMiningSpeed";
    private static final String AUTO_PICKUP_TAG = "VajraAutoPickupEnabled";
    private static final float MIN_MINING_SPEED = 1.0F;
    private static final float MAX_MINING_SPEED = 1000.0F;

    private static final Set<ItemAbility> MY_ABILITIES = Set.of(
            // 斧头
            ItemAbilities.AXE_STRIP,
            ItemAbilities.AXE_SCRAPE,
            ItemAbilities.AXE_WAX_OFF,

            // 铲子
            ItemAbilities.SHOVEL_FLATTEN,
            ItemAbilities.SHOVEL_DOUSE
    );

    public OmniVajraItem(Properties properties) {
        super(properties.component(DataComponents.TOOL, createToolProperties()));
    }

    private static Tool createToolProperties() {
        return new Tool(
                List.of(
                        Tool.Rule.deniesDrops(Tiers.NETHERITE.getIncorrectBlocksForDrops()),
                        new Tool.Rule(
                                new AnyHolderSet<>(BuiltInRegistries.BLOCK.asLookup()),
                                Optional.empty(),
                                Optional.of(true)
                        )
                ),
                Tiers.NETHERITE.getSpeed(),
                0
        );
    }

    public static float getMiningSpeed(ItemStack stack) {
        if (stack.isEmpty()) {
            return Tiers.NETHERITE.getSpeed();
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(MINING_SPEED_TAG)) {
                return tag.getFloat(MINING_SPEED_TAG);
            }
        }
        return Tiers.NETHERITE.getSpeed();
    }

    public static void setMiningSpeed(ItemStack stack, float speed) {
        if (stack.isEmpty()) {
            return;
        }
        float clamped = Math.max(MIN_MINING_SPEED, Math.min(MAX_MINING_SPEED, speed));
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putFloat(MINING_SPEED_TAG, clamped);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isAutoPickupEnabled(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(AUTO_PICKUP_TAG)) {
                return tag.getBoolean(AUTO_PICKUP_TAG);
            }
        }
        return true;
    }

    public static void setAutoPickupEnabled(ItemStack stack, boolean enabled) {
        if (stack.isEmpty()) {
            return;
        }
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putBoolean(AUTO_PICKUP_TAG, enabled);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void toggleAutoPickup(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) {
            return;
        }
        boolean enabled = !isAutoPickupEnabled(stack);
        setAutoPickupEnabled(stack, enabled);
        player.displayClientMessage(
                Component.translatable(
                        "message.omnitools.vajra_pickup_mode",
                        Component.translatable(enabled ? "message.omnitools.vajra_pickup_mode_on" : "message.omnitools.vajra_pickup_mode_off")
                ),
                true
        );
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return getMiningSpeed(stack);
    }
    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return MY_ABILITIES.contains(ability)
                || super.canPerformAction(stack, ability);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            toggleAutoPickup(stack, level, player);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var state = level.getBlockState(pos);
        var stack = context.getItemInHand();
        var player = context.getPlayer();

        if (player != null && player.isShiftKeyDown()) {
            toggleAutoPickup(stack, level, player);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        BlockState modifiedState = null;
        SoundEvent sound = null;
        int levelEventId = -1;

        if (stack.canPerformAction(ItemAbilities.AXE_STRIP)) {
            modifiedState = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
            if (modifiedState != null) {
                sound = SoundEvents.AXE_STRIP;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ItemAbilities.AXE_SCRAPE)) {
            modifiedState = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false);
            if (modifiedState != null) {
                sound = SoundEvents.AXE_SCRAPE;
                levelEventId = 3005;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ItemAbilities.AXE_WAX_OFF)) {
            modifiedState = state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false);
            if (modifiedState != null) {
                sound = SoundEvents.AXE_WAX_OFF;
                levelEventId = 3004;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ItemAbilities.SHOVEL_FLATTEN)) {
            modifiedState = state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
            if (modifiedState != null) {
                sound = SoundEvents.SHOVEL_FLATTEN;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ItemAbilities.SHOVEL_DOUSE)) {
            modifiedState = state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false);
            if (modifiedState != null) {
                sound = SoundEvents.FIRE_EXTINGUISH;
                if (state.getBlock() instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
                    CampfireBlock.dowse(player, level, pos, state);
                }
            }
        }

        if (modifiedState == null || sound == null) {
            return super.useOn(context);
        }

        level.playSound(player, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (!level.isClientSide) {
            level.setBlock(pos, modifiedState, 11);
            if (levelEventId != -1) {
                level.levelEvent(player, levelEventId, pos, 0);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (player != null) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!isAutoPickupEnabled(stack)) {
            return super.mineBlock(stack, level, state, pos, entity);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return true; // 客户端什么都不做
        }

        if (entity instanceof Player player) {

            BlockEntity blockEntity = level.getBlockEntity(pos);

            List<ItemStack> drops = Block.getDrops(
                    state,
                    serverLevel,
                    pos,
                    blockEntity,
                    player,
                    stack
            );

            for (ItemStack drop : drops) {
                if (!player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
            }

            // 移除方块（不生成掉落物）
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);


            return true;
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

}
