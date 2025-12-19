package com.omnitools.core;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.Set;

public class OmniVajraItem extends Item {
    private static final String MINING_SPEED_TAG = "VajraMiningSpeed";
    private static final String AUTO_PICKUP_TAG = "VajraAutoPickupEnabled";
    private static final float MIN_MINING_SPEED = 1.0F;
    private static final float MAX_MINING_SPEED = 1000.0F;

    private static final Set<ToolAction> MY_ACTIONS = Set.of(
            // 斧头
            ToolActions.AXE_STRIP,
            ToolActions.AXE_SCRAPE,
            ToolActions.AXE_WAX_OFF,

            // 铲子
            ToolActions.SHOVEL_FLATTEN,
            ToolActions.SHOVEL_DIG // 用来兼容灭火
    );

    private final Lazy<Multimap<Attribute, AttributeModifier>> attributeModifiers = Lazy.of(() -> {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Vajra damage", 20.0D, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Vajra speed", -0.0D, AttributeModifier.Operation.ADDITION));
        return builder.build();
    });

    public OmniVajraItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static float getMiningSpeed(ItemStack stack) {
        if (stack.isEmpty()) {
            return Tiers.NETHERITE.getSpeed();
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(MINING_SPEED_TAG)) {
            return tag.getFloat(MINING_SPEED_TAG);
        }
        return Tiers.NETHERITE.getSpeed();
    }

    public static void setMiningSpeed(ItemStack stack, float speed) {
        if (stack.isEmpty()) {
            return;
        }
        float clamped = Math.max(MIN_MINING_SPEED, Math.min(MAX_MINING_SPEED, speed));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat(MINING_SPEED_TAG, clamped);
    }

    public static boolean isAutoPickupEnabled(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(AUTO_PICKUP_TAG)) {
            return tag.getBoolean(AUTO_PICKUP_TAG);
        }
        return true;
    }

    public static void setAutoPickupEnabled(ItemStack stack, boolean enabled) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(AUTO_PICKUP_TAG, enabled);
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
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return MY_ACTIONS.contains(toolAction) || super.canPerformAction(stack, toolAction);
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
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        BlockState modifiedState = null;
        SoundEvent sound = null;
        int levelEventId = -1;

        if (stack.canPerformAction(ToolActions.AXE_STRIP)) {
            modifiedState = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
            if (modifiedState != null) {
                sound = SoundEvents.AXE_STRIP;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ToolActions.AXE_SCRAPE)) {
            modifiedState = state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
            if (modifiedState != null) {
                sound = SoundEvents.AXE_SCRAPE;
                levelEventId = 3005;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ToolActions.AXE_WAX_OFF)) {
            modifiedState = state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
            if (modifiedState != null) {
                sound = SoundEvents.AXE_WAX_OFF;
                levelEventId = 3004;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ToolActions.SHOVEL_FLATTEN)) {
            modifiedState = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
            if (modifiedState != null) {
                sound = SoundEvents.SHOVEL_FLATTEN;
            }
        }

        if (modifiedState == null && stack.canPerformAction(ToolActions.SHOVEL_DIG)) {
            modifiedState = state.getToolModifiedState(context, ToolActions.SHOVEL_DIG, false);
            if (modifiedState != null) {
                sound = SoundEvents.FIRE_EXTINGUISH;
                if (state.getBlock() instanceof CampfireBlock && state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
                    CampfireBlock.dowse(player, level, context.getClickedPos(), state);
                }
            }
        }

        if (modifiedState == null || sound == null) {
            return super.useOn(context);
        }

        level.playSound(player, context.getClickedPos(), sound, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (!level.isClientSide) {
            level.setBlock(context.getClickedPos(), modifiedState, 11);
            if (levelEventId != -1) {
                level.levelEvent(player, levelEventId, context.getClickedPos(), 0);
            }
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, context.getClickedPos());
            if (player != null) {
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, net.minecraft.core.BlockPos pos, LivingEntity entity) {
        if (!isAutoPickupEnabled(stack)) {
            return super.mineBlock(stack, level, state, pos, entity);
        }
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return true;
        }

        if (entity instanceof Player player) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, stack);
            for (ItemStack drop : drops) {
                if (!player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return true;
        }

        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? attributeModifiers.get() : super.getDefaultAttributeModifiers(slot);
    }
}
