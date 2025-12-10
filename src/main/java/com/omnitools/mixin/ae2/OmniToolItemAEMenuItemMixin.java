package com.omnitools.mixin.ae2;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import com.omnitools.core.OmniToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OmniToolItem.class)
public abstract class OmniToolItemAEMenuItemMixin implements IMenuItem {

    public ItemMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new ItemMenuHost(player, inventorySlot, stack);
    }
}
