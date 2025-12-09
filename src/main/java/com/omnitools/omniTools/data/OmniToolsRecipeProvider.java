package com.omnitools.omniTools.data;

import java.util.concurrent.CompletableFuture;

import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.omniTools;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class OmniToolsRecipeProvider extends net.minecraft.data.recipes.RecipeProvider {

    public OmniToolsRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(omniTools.MODID, path);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        addCraftingRecipes(output);
    }

    private void addCraftingRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OMNI_WRENCH.get())
                .pattern(" bi")
                .pattern(" ab")
                .pattern("a  ")
                .define('i', Items.NETHERITE_INGOT)
                .define('a', Items.OBSIDIAN)
                .define('b', Items.DIAMOND)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.STICK), has(Items.STICK))
                .save(output, id("crafting/omni_wrench"));
    }
}
