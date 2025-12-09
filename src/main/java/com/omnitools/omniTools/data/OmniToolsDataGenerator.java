package com.omnitools.omniTools.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class OmniToolsDataGenerator {
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var pack = generator.getVanillaPack(true);
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();

        pack.addProvider(packOutput -> new OmniToolsRecipeProvider(packOutput, registries));
    }
}
