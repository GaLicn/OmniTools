package com.omnitools.omniTools.core;

import com.omnitools.omniTools.omniTools;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(omniTools.MODID);

    public static final DeferredItem<OmniToolItem> OMNI_WRENCH = ITEMS.register("omni_wrench",
            () -> new OmniToolItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
