package com.omnitools.core;

import com.omnitools.OmniTools;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OmniTools.MODID);

    public static final RegistryObject<OmniToolItem> OMNI_WRENCH = ITEMS.register(
            "omni_wrench",
            () -> new OmniToolItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
