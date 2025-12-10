package com.omnitools.core;

import com.omnitools.OmniTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OmniTools.MODID);

    public static final RegistryObject<CreativeModeTab> OMNITOOLS_TAB = TABS.register(
            "omnitools",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.omnitools"))
                    .icon(() -> new ItemStack(ModItems.OMNI_WRENCH.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.OMNI_WRENCH.get());
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
