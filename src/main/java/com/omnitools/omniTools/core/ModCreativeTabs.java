package com.omnitools.omniTools.core;

import com.omnitools.omniTools.omniTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, omniTools.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OMNITOOLS_TAB =
            TABS.register("omnitools", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.omnitools"))
                    .icon(() -> new ItemStack(ModItems.OMNI_WRENCH.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.OMNI_WRENCH.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
