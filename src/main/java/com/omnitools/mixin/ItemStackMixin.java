package com.omnitools.mixin;

import com.omnitools.core.ModItems;
import com.omnitools.core.OmniToolItem;
import com.omnitools.core.ToolMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void omnitools$hideWrenchTags(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) {
            return;
        }
        if (self.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }
        if (!omnitools$isWrenchTag(tag)) {
            return;
        }
        ToolMode mode = OmniToolItem.getMode(self);
        if (mode == ToolMode.LINK || mode == ToolMode.RENAME) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean omnitools$isWrenchTag(TagKey<Item> tag) {
        ResourceLocation id = tag.location();
        // 1) 通用扳手标签：c:tools/wrench、forge:tools/wrench 等（只判断 path）
        if ("tools/wrench".equals(id.getPath())) {
            return true;
        }
        // 2) Mekanism 的配置器标签：mekanism:configurators，同样视为扳手类工具
        if ("mekanism".equals(id.getNamespace()) && "configurators".equals(id.getPath())) {
            return true;
        }
        return false;
    }
}
