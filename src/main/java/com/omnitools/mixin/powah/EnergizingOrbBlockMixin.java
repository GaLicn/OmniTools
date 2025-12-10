package com.omnitools.mixin.powah;

import com.omnitools.core.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import owmii.powah.block.energizing.EnergizingOrbBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnergizingOrbBlock.class)
public abstract class EnergizingOrbBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void omnitools$preventOmniWrenchInsert(BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult,
                                                   CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() == ModItems.OMNI_WRENCH.get()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
